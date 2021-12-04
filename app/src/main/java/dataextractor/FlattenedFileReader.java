package dataextractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FlattenedFileReader implements Iterator<String> {

    private final Scanner fileScanner;

    public FlattenedFileReader(String fileName) {
        this.fileScanner = getFileScanner(fileName);
    }

    private Scanner getFileScanner(String fileName) {
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

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return fileScanner.hasNextLine();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return fileScanner.nextLine();
    }
}
