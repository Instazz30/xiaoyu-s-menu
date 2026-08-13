package com.xiaxiaoyu.xingbangmenu.template.component;

import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {

    private final Map<String, Component> components = new HashMap<>();

    public void register(Component component) {
        components.put(component.type(), component);
    }

    public Component get(String type) {
        Component c = components.get(type);
        if (c == null) throw new IllegalArgumentException("未知组件类型: " + type);
        return c;
    }
}
