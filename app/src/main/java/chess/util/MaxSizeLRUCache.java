package chess.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaxSizeLRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int max_entries;

    public MaxSizeLRUCache(int max_entries) {
        super((int) (max_entries / 0.75), 0.75f, true);
        this.max_entries = max_entries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > max_entries;
    }
}
