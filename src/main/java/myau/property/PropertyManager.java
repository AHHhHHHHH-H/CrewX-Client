/*
 * Decompiled with CFR 0.152.
 */
package myau.property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import myau.module.Module;
import myau.property.Property;

public class PropertyManager {
    public LinkedHashMap<Class<?>, ArrayList<Property<?>>> properties = new LinkedHashMap();

    public Property<?> getProperty(Module module, String string) {
        for (Property<?> property : this.properties.get(module.getClass())) {
            if (!property.getName().replace("-", "").equalsIgnoreCase(string.replace("-", ""))) continue;
            return property;
        }
        return null;
    }
}

