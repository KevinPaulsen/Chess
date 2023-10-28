package dataextractor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class represents a hash table that is written to an indexed file.
 */

public class DiskHashTable implements Map<Long, GameEntry>, Iterable<GameEntry> {

    private static final int MAX_BUFFER_SIZE = 1_000_000;
    private static final long TABLE_SIZE = 20_000_000_000L;
    private static final long ENTRY_SIZE = 128;
    private final String filePath;
    private final HashMap<Long, GameEntry> bufferMap;
    private final long size;

    public DiskHashTable(String filePath) {
        this.filePath = filePath;
        this.bufferMap = new HashMap<>();
        this.size = 0;
    }

    /**
     * Opens a RandomAccessFile with the given path.
     *
     * @param filePath the path of the file to open.
     * @return the RandomAccessFile
     */
    private static RandomAccessFile getRandomAccessFile(String filePath) {
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(filePath, "rw");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return accessFile;
    }

    /**
     * Opens a RandomAccessFile with the given path.
     *
     * @param randomAccessFile the RandomAccessFile to close
     */
    private static void close(RandomAccessFile randomAccessFile) {
        try {
            randomAccessFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Seek the RandomAccessFile to the given position
     *
     * @param randomAccessFile the RandomAccessFile.
     * @param pos              the position to seek to.
     */
    private static void seek(RandomAccessFile randomAccessFile, long pos) {
        pos = Math.abs(pos) % TABLE_SIZE;
        pos = pos - (pos % ENTRY_SIZE);
        try {
            randomAccessFile.seek(pos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Read from the randomAccessFile bufferSize number of bytes.
     *
     * @param randomAccessFile the file to read from.
     * @param bufferSize       the size of the buffer to read into.
     * @return the bytes read.
     * @throws IllegalStateException if bufferSize number of bytes was not read
     */
    private static byte[] read(RandomAccessFile randomAccessFile, int bufferSize) {
        int bytesRead = -1;
        byte[] buffer = new byte[bufferSize];
        try {
            bytesRead = randomAccessFile.read(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (bytesRead != buffer.length) {
            throw new IllegalStateException("Did not read the expected number of bytes.");
        }
        return buffer;
    }

    /**
     * Reads the next int from the RandomAccessFile.
     *
     * @param randomAccessFile the file to read from.
     * @return the next int read.
     */
    private static int readInt(RandomAccessFile randomAccessFile) {
        int result = 0;
        try {
            if (randomAccessFile.getFilePointer() < randomAccessFile.length()) {
                result = randomAccessFile.readInt();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Reads the next long from the RandomAccessFile.
     *
     * @param randomAccessFile the file to read from.
     * @return the next long read.
     */
    private static long readLong(RandomAccessFile randomAccessFile) {
        long result = 0;
        try {
            result = randomAccessFile.readLong();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Write the byte[] buffer to the given accessFile.
     *
     * @param randomAccessFile the file to write to.
     * @param buffer           the buffer to write.
     */
    private static void write(RandomAccessFile randomAccessFile, byte[] buffer) {
        try {
            if (buffer.length != ENTRY_SIZE) {
                int x = 0;
            }
            randomAccessFile.write(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Write an array of integers to the given file.
     *
     * @param randomAccessFile the file to write to.
     * @param integers         the integers to write to the file.
     */
    private static void writeInts(RandomAccessFile randomAccessFile, int... integers) {
        ByteBuffer buffer = ByteBuffer.allocate(integers.length * Integer.BYTES);
        for (int val : integers) {
            buffer.putInt(val);
        }
        write(randomAccessFile, buffer.array());
    }

    /**
     * Checks that key is not null and is a Long.
     *
     * @param key the key to check.
     * @throws NullPointerException if key is null.
     * @throws ClassCastException   if key is not a Long.
     */
    private static void checkKey(Object key) {
        if (key == null)
            throw new NullPointerException("Key cannot be null");
        if (!(key instanceof Long))
            throw new ClassCastException("Key is not of type Long");
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return (int) size;
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.  More formally, returns {@code true} if and only if
     * this map contains a mapping for a key {@code k} such that
     * {@code Objects.equals(key, k)}.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified
     * key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="{@docRoot}/java.base/java/util/Collection
     *                              .html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java.base/java/util/Collection
     *                              .html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean containsKey(Object key) {
        checkKey(key);

        long pos = (long) key;
        RandomAccessFile accessFile = getRandomAccessFile(filePath);
        seek(accessFile, pos);
        int sizeOfEntry = readInt(accessFile);
        close(accessFile);

        return sizeOfEntry != 0;
    }

    /**
     * This method is not supported.
     */
    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that
     * {@code Objects.equals(key, k)},
     * then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>If this map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this map contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="{@docRoot}/java.base/java/util/Collection
     *                              .html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java.base/java/util/Collection
     *                              .html#optional-restrictions">optional</a>)
     */
    @Override
    public GameEntry get(Object key) {
        checkKey(key);

        long pos = (long) key;
        RandomAccessFile accessFile = getRandomAccessFile(filePath);
        seek(accessFile, pos);
        int recordSize = readInt(accessFile);
        byte[] buf = read(accessFile, recordSize);
        close(accessFile);

        return GameEntry.toGameEntry(buf);
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * {@code m} is said to contain a mapping for a key {@code k} if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * {@code true}.)
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with {@code key},
     * if the implementation supports {@code null} values.)
     * @throws ClassCastException       if the class of the specified key or value
     *                                  prevents it from being stored in this map
     * @throws NullPointerException     if the specified key or value is null
     *                                  and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *                                  or value prevents it from being stored in this map
     */
    @Override
    public GameEntry put(Long key, GameEntry value) {
        checkKey(key);
        if (value == null)
            throw new IllegalStateException("Null values are not supported");

        if (bufferMap.size() > MAX_BUFFER_SIZE) {
            writeBufferMap();
        }

        GameEntry currentEntry = bufferMap.get(key);
        if (currentEntry == null) {
            bufferMap.put(key, value);
        } else {
            currentEntry.addTimesReached();
            currentEntry.addScore(value.getWhiteWins(), value.getBlackWins());
        }
        return currentEntry;
    }

    /**
     * Operation not supported.
     */
    @Override
    public GameEntry remove(Object key) {
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).  The effect of this call is equivalent to that
     * of calling put on this map once for each mapping from key {@code k}
     * to value {@code v} in the specified map.  The behavior of this
     * operation is undefined if the specified map is modified while the
     * operation is in progress.
     *
     * @param m mappings to be stored in this map
     * @throws UnsupportedOperationException if the {@code putAll} operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the class of a key or value in the
     *                                       specified map prevents it from being stored in this map
     * @throws NullPointerException          if the specified map is null, or if
     *                                       this map does not permit null keys or values, and the
     *                                       specified map contains null keys or values
     * @throws IllegalArgumentException      if some property of a key or value in
     *                                       the specified map prevents it from being stored in
     *                                       this map
     */
    @Override
    public void putAll(Map<? extends Long, ? extends GameEntry> m) {
        for (Entry<? extends Long, ? extends GameEntry> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this map
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Clear operation is not supported by this map");
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear}
     * operations.  It does not support the {@code add} or {@code addAll}
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    @Override
    public Set<Long> keySet() {
        return null;
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own {@code remove} operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<GameEntry> values() {
        return null;
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation, or through the
     * {@code setValue} operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations.  It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Entry<Long, GameEntry>> entrySet() {
        return null;
    }

    public void writeBufferMap() {
        long startTime = System.currentTimeMillis();
        System.out.println("Writing to file...");
        RandomAccessFile accessFile = getRandomAccessFile(filePath);
        for (Entry<Long, GameEntry> entry : bufferMap.entrySet()) {
            seek(accessFile, entry.getKey());
            int size = readInt(accessFile);

            if (size == 0) {
                seek(accessFile, entry.getKey());
                write(accessFile, GameEntry.toByteArray(entry.getValue()));
            } else {
                int timesReached = readInt(accessFile) + entry.getValue().getTimesReached();
                int score = readInt(accessFile) + entry.getValue().getWhiteWins();
                writeInts(accessFile, timesReached, score);
            }

        }
        close(accessFile);
        bufferMap.clear();
        System.out.printf("Finished writing to file... (%dms)\n",
                System.currentTimeMillis() - startTime);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<GameEntry> iterator() {
        return new DiskHashTableIterator(this);
    }

    private static class DiskHashTableIterator implements Iterator<GameEntry> {

        private final DiskHashTable diskHashTable;
        long position;
        private GameEntry current;

        public DiskHashTableIterator(DiskHashTable diskHashTable) {
            this.diskHashTable = diskHashTable;
            this.position = 0;
            next();
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
            return current != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public GameEntry next() {
            if (position > 0 && current == null) {
                throw new NoSuchElementException();
            }
            GameEntry result = current;
            while (!diskHashTable.containsKey(position)) {
                position += ENTRY_SIZE;
            }
            current = diskHashTable.get(position);
            position += ENTRY_SIZE;
            return result;
        }
    }
}
