package gdemas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BijectiveMap<K, V> {
    private final Map<K, V> keyToValueMap;
    private final Map<V, K> valueToKeyMap;

    public BijectiveMap() {
        keyToValueMap = new HashMap<>();
        valueToKeyMap = new HashMap<>();
    }

    public void put(K key, V value) {
        keyToValueMap.put(key, value);
        valueToKeyMap.put(value, key);
    }

    public V getValue(K key) {
        return keyToValueMap.get(key);
    }

    public K getKey(V value) {
        return valueToKeyMap.get(value);
    }

    public boolean containsKey(K key) {
        return keyToValueMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return valueToKeyMap.containsKey(value);
    }

    public void removeByKey(K key) {
        V value = keyToValueMap.get(key);
        keyToValueMap.remove(key);
        valueToKeyMap.remove(value);
    }

    public void removeByValue(V value) {
        K key = valueToKeyMap.get(value);
        valueToKeyMap.remove(value);
        keyToValueMap.remove(key);
    }

    public int size() {
        return keyToValueMap.size();
    }

    public boolean isEmpty() {
        return keyToValueMap.isEmpty();
    }

    public void clear() {
        keyToValueMap.clear();
        valueToKeyMap.clear();
    }

    public Collection<V> values() {
        return valueToKeyMap.keySet();
    }

    public Set<K> keySet() {
        return keyToValueMap.keySet();
    }
}