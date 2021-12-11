package dataextractor;

import chess.util.BigFastMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameDataExtractor {

    private static final String FLATTENED_FILENAME = "data/flattened_data.txt";
    private static final String PROCESSED_FILENAME = "data/wins_to_big_rep.txt";
    private static final int MIN_GAMES = 15;
    private static final int TOTAL_GAMES = 3_817_909;
    private static final int PRUNE_NUM = 250_000;

    private final PGNReader reader;

    public GameDataExtractor(String pgnPathname) {
        this.reader = new PGNReader(pgnPathname);
    }

    public void writeGame(Writer writer) {
        while (reader.hasNext() && writer != null) {
            writeTo(writer, reader.next());
        }
    }

    private static void writeTo(Writer writer, String string) {
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

    private static void pruneMap(Map<BigFastMap, int[]> posToWins, int timesPruned) {
        int factor = TOTAL_GAMES / (PRUNE_NUM * timesPruned);
        posToWins.entrySet().removeIf(bigFastMapEntry -> Arrays.stream(bigFastMapEntry.getValue()).sum() * factor < MIN_GAMES);
    }

    private static void processData() {
        Map<BigFastMap, int[]> posToWins = new HashMap<>();

        FlattenedFileReader fileReader = new FlattenedFileReader(FLATTENED_FILENAME);
        int numPlayed = 0;
        while (fileReader.hasNext()) {
            GameProcessor.processGame(fileReader.next(), posToWins);
            numPlayed++;
            if (numPlayed % 10_000 == 0) {
                System.out.printf("Successfully processed %d games\n", numPlayed);
                if (numPlayed % PRUNE_NUM == 0) {
                    System.out.println("Pruning...");
                    int startSize = posToWins.size();
                    pruneMap(posToWins, numPlayed / PRUNE_NUM);
                    System.out.printf("Successfully pruned %d entries.\n", startSize - posToWins.size());
                }
            }
        }

        System.out.println("Now beginning File writing...");

        System.out.println("Writing...");
        Writer writer = getFileWriter(PROCESSED_FILENAME);
        for (Map.Entry<BigFastMap, int[]> entry : posToWins.entrySet()) {
            if (Arrays.stream(entry.getValue()).sum() >= MIN_GAMES) {
                writeTo(writer, Arrays.toString(entry.getValue()) + "\t");
                writeTo(writer, entry.getKey().toShortString());
                writeTo(writer, "\n");
            }
        }
        closeWriter(writer);
    }

    public static void main(String[] args) {
        //flattenData();
        processData();
    }
}
