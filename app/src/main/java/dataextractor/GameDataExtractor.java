package dataextractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class GameDataExtractor {

    private static final String FLATTENED_FILENAME = "data/flattened_data.txt";
    private static final String PROCESSED_FILENAME = "data/processed_data.txt";

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

    private static void processData() {
        Map<Long, String> posToFEN = new HashMap<>();
        Map<Long, int[]> posToWins = new HashMap<>();

        FlattenedFileReader fileReader = new FlattenedFileReader(FLATTENED_FILENAME);
        int numPlayed = 0;
        while (fileReader.hasNext() && numPlayed < 300_000) {
            GameProcessor.processGame(fileReader.next(), posToFEN, posToWins);
            numPlayed++;
            if (numPlayed % 1000 == 0) {
                System.out.printf("Successfully processed %d games\n", numPlayed);
            }
        }

        System.out.println("Now beginning File writing...");

        Writer writer = getFileWriter(PROCESSED_FILENAME);
        List<String> printStrings = new ArrayList<>(1_834_333);
        for (Map.Entry<Long, String> entry : posToFEN.entrySet()) {
            int sum = Arrays.stream(posToWins.get(entry.getKey())).sum();
            printStrings.add(sum + "\t" + Arrays.toString(posToWins.get(entry.getKey())) + "\t" + entry.getValue() + "\n");
        }
        System.out.println("Sorting...");
        printStrings.sort(((o1, o2) -> {
            int int1 = Integer.parseInt(o1.split("\t")[0]);
            int int2 = Integer.parseInt(o2.split("\t")[0]);
            return Integer.compare(int2, int1);
        }));
        System.out.println("Writing...");
        for (String printString : printStrings) {
            writeTo(writer, printString);
        }
        closeWriter(writer);
    }

    public static void main(String[] args) {
        //flattenData();
        processData();
    }
}
