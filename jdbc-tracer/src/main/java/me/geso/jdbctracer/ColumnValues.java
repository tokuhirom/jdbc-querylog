package me.geso.jdbctracer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ColumnValues {
    private final Map<Integer, Object> values;

    ColumnValues() {
        values = new HashMap<>();
    }

    void put(Integer key, Object value) {
        values.put(key, value);
    }

    void clear() {
        values.clear();
    }

    @Override
    public String toString() {
        return "ColumnValues{" +
                "values=" + values +
                '}';
    }

    List<Object> values() {
        return values.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
