package dataextractor;

import chess.model.GameModel;
import chess.model.features.BoardRepFeature;
import chess.model.features.Feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameDataExtractor {

    private static final String FLATTENED_FILENAME = "data/flattened_data.txt";
    private static final String DATA_FOLDER = "data/Lichess Elite Database";
    private static final String FEN_FILENAME = "data/scoreToFen.txt";
    private static final String FILTERED_FILENAME = "data/filteredWinsToFen.txt";
    private static final String FILTERED_SCORE = "data/filteredScoreToFen.txt";
    private static final String MODEL_DATA_PATH = "model_learning/data/";

    private static final int MIN_GAMES = 6;
    private static final int TOTAL_GAMES = 3_817_909;
    private static final int NUM_POSITIONS = 129_315_717;
    private static final int NUM_FILTERED = 995_529;

    private final PGNReader reader;

    public GameDataExtractor(String pgnPathname) {
        this.reader = new PGNReader(pgnPathname);
    }

    private synchronized static void writeTo(Writer writer, String string) {
        try {
            writer.write(string);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Writer getFileWriter(String fileName) {
        Writer writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
            writer = null;
        }
        return writer;
    }

    private static void closeWriter(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void flattenData() {
        Writer writer = getFileWriter(FLATTENED_FILENAME);
        File dataFolder = new File(DATA_FOLDER);
        for (final File fileEntry : Objects.requireNonNull(dataFolder.listFiles())) {
            if (fileEntry.toString().endsWith(".pgn")) {
                System.out.printf("Writing '%s'\n", fileEntry);
                GameDataExtractor extractor = new GameDataExtractor(fileEntry.getPath());
                extractor.writeGame(writer);
            }
        }
        closeWriter(writer);
    }

    private static void processData() {
        Map<Long, GameProcessor.PositionData> posToWins = new ConcurrentHashMap<>();

        System.out.printf("Starting to process %d games...\n", TOTAL_GAMES);

        LineFileReader fileReader = new LineFileReader(FLATTENED_FILENAME);
        parallelize(10, TOTAL_GAMES, () -> GameProcessor.processGame(fileReader.next(), posToWins));
        fileReader.close();

        System.out.println("Now beginning File writing...");


        final int initSize = posToWins.size();

        System.out.printf("Writing %d entries, estimated %f GB\n", initSize, (initSize * 62L) / 1_000_000_000f);

        Writer writer = getFileWriter(FEN_FILENAME);
        Iterator<GameProcessor.PositionData> positionDataIterator = posToWins.values().iterator();
        parallelize(1, initSize, () -> {
            GameProcessor.PositionData data = positionDataIterator.next();
            writeTo(writer, Arrays.toString(data.score()));
            writeTo(writer, "\t");
            writeTo(writer, GameModel.getFEN(data.byteRep()));
            writeTo(writer, "\n");
            positionDataIterator.remove();
        });
        closeWriter(writer);


    }

    private static void parallelize(int numThreads, int total, Runnable r) {

        AtomicInteger counter = new AtomicInteger(0);

        // Create Executors
        final ExecutorService gameProcessingExecutor = Executors.newFixedThreadPool(numThreads - 1);
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        // Start game processing threads
        for (int threadNum = 0; threadNum < numThreads - 1; threadNum++) {
            CompletableFuture.runAsync(() -> runUntilNoSuchElement(r, counter), gameProcessingExecutor);
        }

        final ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() ->
                        printProgress(100, (float) counter.get() / total), 0, 250, TimeUnit.MILLISECONDS);

        runUntilNoSuchElement(r, counter);

        scheduledFuture.cancel(true);

        printProgress(100, 1);
        System.out.println("\nDone!");

        // Shutdown executors
        gameProcessingExecutor.shutdown();
        scheduledExecutor.shutdown();
    }

    private static void printProgress(int length, float progress) {
        System.out.printf(String.format("|%%-%ds|\r", length), "#".repeat((int) (length * progress)));
    }

    private static void runUntilNoSuchElement(Runnable r, AtomicInteger counter) {
        while (true) {
            try {
                r.run();
                counter.getAndIncrement();
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    private static void query() {
        Map<Integer, Integer> countToFrequency = new ConcurrentHashMap<>();

        LineFileReader fileReader = new LineFileReader(FEN_FILENAME);
        parallelize(8, NUM_POSITIONS, () -> {
            String line = fileReader.next();
            String[] sections = Filter.getSections(line);
            int count = Integer.parseInt(sections[0]) + Integer.parseInt(sections[1]);

            if (count > 20) {
                count = 20;
            }
            countToFrequency.merge(count, 1, Integer::sum);
        });
        fileReader.close();


        int[] frequencies = new int[countToFrequency.keySet().stream().max(Integer::compareTo).orElse(0) + 1];
        countToFrequency.forEach((key, value) -> frequencies[frequencies.length - 1 - key] = value);

        int sum = 0;
        for (int count = 0; count < frequencies.length; count++) {
            if (frequencies[count] > 0) {
                sum += frequencies[count];
                System.out.printf("%4d -> %8d\n", count, sum);
            }
        }
    }

    private static void filterData(Filter... filters) {
        LineFileReader fileReader = new LineFileReader(FEN_FILENAME);
        Writer fileWriter = getFileWriter(FILTERED_FILENAME);

        parallelize(8, NUM_POSITIONS, () -> {
            String line = fileReader.next();

            boolean shouldPrune = false;
            for (Filter filter : filters) {
                if (filter.shouldPrune(line)) {
                    shouldPrune = true;
                    break;
                }
            }
            if (!shouldPrune) {
                writeTo(fileWriter, line + "\n");
            }
        });

        closeWriter(fileWriter);
        fileReader.close();
    }

    private static void formatScore() {
        LineFileReader reader = new LineFileReader(FILTERED_FILENAME);
        Writer fileWriter = getFileWriter(FILTERED_SCORE);

        parallelize(4, NUM_POSITIONS, () -> {
            String line = reader.next();
            String[] sections = line.split("\t");
            String[] scores = sections[0].substring(1, sections[0].length() - 1).split(", ");
            int whiteWin = Integer.parseInt(scores[0]);
            int blackWin = Integer.parseInt(scores[1]);

            float whiteScore = (float) whiteWin / (whiteWin + blackWin);
            float blackScore = (float) blackWin / (whiteWin + blackWin);

            writeTo(fileWriter, String.format("%10.7f\t%s\n", (whiteScore - blackScore), sections[1]));
        });

        closeWriter(fileWriter);
        reader.close();
    }

    public static void main(String[] args) {
        flattenData();
        //processData();
        //query();
        //filterData(new NumGameFilter(MIN_GAMES));
        //formatScore();
        //extractFeature(new BoardRepFeature());
    }

    private static void extractFeature(Feature feature) {
        LineFileReader filteredScoreReader = new LineFileReader(FILTERED_SCORE);
        Writer writer = getFileWriter(MODEL_DATA_PATH + feature.getClass().getSimpleName() + ".txt");

        parallelize(6, NUM_FILTERED, () -> {
            String line = filteredScoreReader.next();
            String[] sections = line.split("\t");

            GameModel game = new GameModel(sections[1]);
            String featureString = feature.featureString(game);
            if (!sections[0].equals(" 0.0000000")) {
                String builder = sections[0]
                        + ","
                        + featureString
                        + "\n";
                writeTo(writer, builder);
            }
        });

        closeWriter(writer);
        filteredScoreReader.close();
    }

    public void writeGame(Writer writer) {
        while (reader.hasNext() && writer != null) {
            writeTo(writer, reader.next());
        }
    }

    private interface Filter {
        private static String[] getSections(String line) {
            String[] sections = line.split("\t");
            String[] score = sections[0].substring(1, sections[0].length() - 1).split(", ");

            return new String[]{score[0], score[1], sections[1]};
        }

        boolean shouldPrune(String line);
    }

    private record NumGameFilter(int numGames) implements Filter {

        @Override
        public boolean shouldPrune(String line) {
            String[] sections = Filter.getSections(line);
            return Integer.parseInt(sections[0]) + Integer.parseInt(sections[1]) < numGames;
        }
    }
}
