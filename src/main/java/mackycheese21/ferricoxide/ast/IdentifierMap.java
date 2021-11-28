package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.AnalysisException;

import java.util.*;

public class IdentifierMap<T> {

    private final Map<String, T> map;
    private final T defaultValue;

    public IdentifierMap(T defaultValue) {
        map = new HashMap<>();
        this.defaultValue = defaultValue;
    }

    public boolean mapHas(String id) {
        return defaultValue != null || map.containsKey(id);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public T mapGet(String id) {
        if (!map.containsKey(id)) {
            if (defaultValue == null) throw AnalysisException.noSuchKey(id);
            else return defaultValue;
        } else {
            return map.get(id);
        }
    }

    public void mapAdd(String id, T value) {
        if (map.containsKey(id)) throw AnalysisException.keyAlreadyExists(id);
        map.put(id, value);
    }

    public void mapSet(String id, T value) {
        if (!map.containsKey(id)) throw AnalysisException.noSuchKey(id);
        map.put(id, value);
    }

    public Set<String> keys() {
        return map.keySet();
    }
    public Collection<T> values() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }
}
