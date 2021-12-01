package mackycheese21.ferricoxide.ast;

import mackycheese21.ferricoxide.AnalysisException;

import java.util.*;
import java.util.stream.Collectors;

public class IdentifierMap<T> {

    private final List<Map<String, T>> stack;
    private final T defaultValue;

    public IdentifierMap(T defaultValue) {
        stack = new ArrayList<>();
        push();
        this.defaultValue = defaultValue;
    }

    public boolean mapHas(String id) {
        for (Map<String, T> map : stack) {
            if (map.containsKey(id)) return true;
        }
        return defaultValue != null;
    }

    public void push() {
        stack.add(0, new HashMap<>());
    }

    public void pop() {
        stack.remove(0);
    }

    @Override
    public String toString() {
        return stack.toString();
    }

    public T mapGet(String id) {
        for (Map<String, T> map : stack) {
            if (map.containsKey(id)) return map.get(id);
        }
        if (defaultValue != null) return defaultValue;
        throw AnalysisException.noSuchKey(id);
    }

    public void mapAdd(String id, T value) {
        if (mapHas(id)) throw AnalysisException.keyAlreadyExists(id);
        stack.get(0).put(id, value);
    }

    public void mapSet(String id, T value) {
        if (!mapHas(id)) throw AnalysisException.noSuchKey(id);
        for (Map<String, T> map : stack) {
            if (map.containsKey(id)) {
                map.put(id, value);
                break;
            }
        }
        throw new UnsupportedOperationException("unreachable");
    }

}
