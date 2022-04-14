package dataextractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class LineFileReader implements Iterator<String> {

    private final Scanner fileScanner;

    int count = 0;
    private static final int MAX_COUNT = 100_000;

    public LineFileReader(String fileName) {
        this.fileScanner = getFileScanner(fileName);
    }

    static Scanner getFileScanner(String fileName) {
        File pgnFile = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(pgnFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            scanner = null;
        }
        return scanner;
    }

    void close() {
        fileScanner.close();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public synchronized boolean hasNext() {
        return fileScanner.hasNextLine();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public synchronized String next() {
        return fileScanner.nextLine();
    }
}
