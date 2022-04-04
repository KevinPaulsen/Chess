package dataextractor;

import chess.model.GameModel;

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
    private static final String PROCESSED_FILENAME = "data/scoreToByteData.txt";
    private static final String FEN_FILENAME = "data/scoreToFen.txt";
    private static final String FILTERED_FILENAME = "data/filteredScoreToFen.txt";
    private static final int MIN_GAMES = 10;
    private static final int TOTAL_GAMES = 3_817_909;
    private static final int NUM_POSITIONS = 129_315_717;

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
        File dataFolder = new File("data/Lichess Elite Database");
        for (final File fileEntry : Objects.requireNonNull(dataFolder.listFiles())) {
            if (fileEntry.toString().endsWith(".pgn")) {
                System.out.printf("Writing '%s'\n", fileEntry);
                GameDataExtractor extractor = new GameDataExtractor(fileEntry.getPath());
                extractor.writeGame(writer);
            }
        }
        closeWriter(writer);
    }

    private static void processGames(LineFileReader fileReader, Map<Long, GameProcessor.PositionData> posToWins,
                                     AtomicInteger counter) {
        while (true) {
            try {
                GameProcessor.processGame(fileReader.next(), posToWins);
                counter.incrementAndGet();
            } catch (NoSuchElementException ex) {
                break;
            }
        }
    }

    private static void processData() {
        Map<Long, GameProcessor.PositionData> posToWins = new ConcurrentHashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        System.out.printf("Starting to process %d games...\n", TOTAL_GAMES);

        LineFileReader fileReader = new LineFileReader(FLATTENED_FILENAME);
        parallelize(10, 10,
                () -> processGames(fileReader, posToWins, counter),
                () -> System.out.printf("%3.2f%% done processing games\n", 100 * (float) counter.get() / TOTAL_GAMES));
        fileReader.close();

        System.out.println("Now beginning File writing...");


        final int initSize = posToWins.size();
        counter.set(0);

        System.out.printf("Writing %d entries, estimated %f GB\n", initSize, (initSize * 62L) / 1_000_000_000f);

        Writer writer = getFileWriter(FEN_FILENAME);
        parallelize(1, 10, () -> {
            Iterator<GameProcessor.PositionData> positionDataIterator = posToWins.values().iterator();
            while (positionDataIterator.hasNext()) {
                GameProcessor.PositionData data = positionDataIterator.next();
                writeTo(writer, Arrays.toString(data.score()) + "\t");
                writeTo(writer, GameModel.getFEN(data.byteRep()));
                writeTo(writer, "\n");
                positionDataIterator.remove();
                counter.incrementAndGet();
            }
        }, () -> System.out.printf("%3.2f%% done\n", 100.0 * counter.get() / initSize));
        closeWriter(writer);


    }

    @SuppressWarnings("unchecked")
    private static void parallelize(int numThreads, int printPeriod, Runnable r, Runnable printer) {

        // Create Executors
        final ExecutorService gameProcessingExecutor = Executors.newFixedThreadPool(numThreads);
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        // Start game processing threads
        CompletableFuture<Void>[] futures = new CompletableFuture[numThreads];
        for (int threadNum = 0; threadNum < numThreads; threadNum++) {
            futures[threadNum] = CompletableFuture.runAsync(r, gameProcessingExecutor);
        }

        final ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(printer, printPeriod,
                printPeriod, TimeUnit.SECONDS);

        // Wait for all threads to finish, then print finish message
        CompletableFuture.allOf(futures)
                .thenRun(() -> {
                    scheduledFuture.cancel(true);
                    printer.run();
                })
                .join();

        // Shutdown executors
        gameProcessingExecutor.shutdown();
        scheduledExecutor.shutdown();
    }

    private static void query() {
        Map<Integer, Integer> countToFrequency = new ConcurrentHashMap<>();

        LineFileReader fileReader = new LineFileReader(FEN_FILENAME);
        AtomicInteger counter = new AtomicInteger(0);
        parallelize(8, 2, () -> {
            while (true) {
                try {
                    String line = fileReader.next();
                    String[] sections = Filter.getSections(line);
                    int count = Integer.parseInt(sections[0]) + Integer.parseInt(sections[1]);

                    if (count > 20) {
                        count = 20;
                    }
                    countToFrequency.merge(count, 1, Integer::sum);
                    counter.getAndIncrement();
                } catch (NoSuchElementException ex) {
                    break;
                }
            }
        }, () -> System.out.printf("%3.2f%% done querying\n", 100.0 * counter.get() / NUM_POSITIONS));
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

        AtomicInteger counter = new AtomicInteger(0);

        parallelize(8, 2, () -> {
            while (true) {
                try {
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
                    counter.incrementAndGet();
                } catch (NoSuchElementException ex) {
                    break;
                }
            }
        }, () -> System.out.printf("%3.2f%% done filtering\n", 100.0 * counter.get() / NUM_POSITIONS));

        closeWriter(fileWriter);
        fileReader.close();
    }

    public void writeGame(Writer writer) {
        while (reader.hasNext() && writer != null) {
            writeTo(writer, reader.next());
        }
    }

    private interface Filter {
        boolean shouldPrune(String line);

        private static String[] getSections(String line) {
            String[] sections = line.split("\t");
            String[] score = sections[0].substring(1, sections[0].length() - 1).split(", ");

            return new String[]{score[0], score[1], sections[1]};
        }
    }

    private record NumGameFilter(int numGames) implements Filter {

        @Override
        public boolean shouldPrune(String line) {
            String[] sections = Filter.getSections(line);
            return Integer.parseInt(sections[0]) + Integer.parseInt(sections[1]) < numGames;
        }
    }

    public static void main(String[] args) {
        //flattenData();
        processData();
        //query();
        //filterData(new NumGameFilter(6));
    }
}
