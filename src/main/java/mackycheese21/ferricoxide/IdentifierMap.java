package mackycheese21.ferricoxide;

import java.util.HashMap;
import java.util.Map;

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

    public T mapGet(String id) {
        if(!map.containsKey(id)) {
            if(defaultValue == null) throw new RuntimeException(id);
            else return defaultValue;
        } else {
            return map.get(id);
        }
    }

    public void mapAdd(String id, T value) {
        if(map.containsKey(id)) throw new RuntimeException(id);
        mapPut(id, value);
    }

    public void mapPut(String id, T value) {
        map.put(id, value);
    }

}
