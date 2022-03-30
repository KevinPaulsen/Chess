package dataextractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class PGNReader implements Iterator<String> {

    private final Scanner PGNScanner;
    private String nextGameString;

    public PGNReader(String pgnPath) {
        this.PGNScanner = getPGNScanner(pgnPath);
        nextGameString = getNextGameString();
    }

    private static Scanner getPGNScanner(String pathName) {
        File pgnFile = new File(pathName);
        Scanner scanner;
        try {
            scanner = new Scanner(pgnFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            scanner = null;
        }
        return scanner;
    }

    private String formatLine(String line) {
        line = line.replaceAll("([0-9]+[.-])", "");
        line = line.replaceAll("\\s+", "\t");
        return line.strip() + "\n";
    }

    private String getNextGameString() {
        StringBuilder gameStringBuilder = new StringBuilder();
        boolean readingGame = false;
        while (PGNScanner.hasNextLine()) {
            String line = PGNScanner.nextLine();

            if (line.length() > 0 && line.charAt(0) == '1') {
                gameStringBuilder.append(line);
                readingGame = true;
                break;
            }
        }

        while (PGNScanner.hasNextLine() && readingGame) {
            String line = PGNScanner.nextLine();

            if (line.equals("")) {
                readingGame = false;
            } else {
                gameStringBuilder.append(" ");
                gameStringBuilder.append(line);
            }
        }
        String result = formatLine(gameStringBuilder.toString());

        return result.contains("*") ? getNextGameString() : result;
    }

    /**
     * Returns {@code true} if the iteration has more games to read.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return !nextGameString.equals("\n");
    }

    /**
     * Returns the next game string in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String result = this.nextGameString;
        nextGameString = getNextGameString();
        return result;
    }
}
