package com.github.authme.configme.propertymap.list;

import com.github.authme.configme.exception.ConfigMeException;
import com.github.authme.configme.properties.Property;
import com.github.authme.configme.propertymap.PropertyEntry;
import com.github.authme.configme.propertymap.PropertyMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link PropertyMap}.
 *
 * It guarantees that the added entries:
 * <ul>
 *   <li>are grouped by path, e.g. all "DataSource.mysql" properties are together, and "DataSource.mysql" properties
 *   are within the broader "DataSource" group.</li>
 *   <li>are ordered by insertion, e.g. if the first "DataSource" property is inserted before the first "security"
 *   property, then "DataSource" properties will come before the "security" ones.</li>
 * </ul>
 */
public class OrderedPropertiesList implements PropertyMap {

    private LinkedHashMap<String, Object> rootEntries;

    public OrderedPropertiesList() {
        rootEntries = new LinkedHashMap<>();
    }

    public void add(Property<?> property, String... comments) {
        String[] paths = property.getPath().split("\\.");
        Map<String, Object> map = rootEntries;
        for (int i = 0; i < paths.length - 1; ++i) {
            map = getChildMap(map, paths[i]);
        }

        final String end = paths[paths.length - 1];
        if (map.containsKey(end)) {
            throw new IllegalStateException("Path at '" + property.getPath() + "' already exists");
        }
        map.put(end, new PropertyEntry(property, comments));
    }


    @Override
    public List<PropertyEntry> getEntries() {
        List<PropertyEntry> result = new ArrayList<>();
        collectEntries(rootEntries, result);
        return result;
    }

    private static Map<String, Object> getChildMap(Map<String, Object> parent, String path) {
        Object o = parent.get(path);
        if (o instanceof Map<?, ?>) {
            return asTypedMap(o);
        } else if (o == null) {
            Map<String, Object> map = new LinkedHashMap<>();
            parent.put(path, map);
            return map;
        } else { // uh oh
            if (o instanceof PropertyEntry) {
                throw new ConfigMeException("Unexpected entry found at path '" + path + "'");
            } else {
                throw new ConfigMeException("Value of unknown type found at '" + path + "': " + o);
            }
        }
    }

    private static void collectEntries(Map<String, Object> map, List<PropertyEntry> results) {
        for (Object o : map.values()) {
            if (o instanceof Map<?, ?>) {
                collectEntries(asTypedMap(o), results);
            } else if (o instanceof PropertyEntry) {
                results.add((PropertyEntry) o);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asTypedMap(Object o) {
        return (Map<String, Object>) o;
    }
}
