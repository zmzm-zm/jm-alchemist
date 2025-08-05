package com.items;

import arc.struct.ObjectMap;
import mindustry.type.Item;
import arc.graphics.Color;

public class dynamicItems extends Item {

    public ObjectMap<String, Float> stats = new ObjectMap<>();

    public dynamicItems(String name) {
        super(name);
    }
}