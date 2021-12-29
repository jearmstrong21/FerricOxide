package mackycheese21.ferricoxide;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MapStack<K, V> implements Map<K, V> {

    private final List<HashMap<K, V>> stack;

    public MapStack() {
        stack = new ArrayList<>();
    }

    public void push() {
        stack.add(0, new HashMap<>());
    }

    public void pop() {
        stack.remove(0);
    }

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public boolean isEmpty() {
        for (HashMap<K, V> map : stack) {
            if (!map.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        for (HashMap<K, V> map : stack) {
            if (map.containsKey(key)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (HashMap<K, V> map : stack) {
            if (map.containsValue(value)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        for (HashMap<K, V> map : stack) {
            if (map.containsKey(key)) return map.get(key);
        }
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        for (HashMap<K, V> map : stack) {
            if (map.containsKey(key)) {
                return map.put(key, value);
            }
        }
        return stack.get(0).put(key, value);
    }

    @Override
    public V remove(Object key) {
        for (HashMap<K, V> map : stack) {
            if (map.containsKey(key)) {
                return map.remove(key);
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        stack.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (HashMap<K, V> map : stack) {
            keySet.addAll(map.keySet());
        }
        return keySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
