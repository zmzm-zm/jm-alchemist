package com.liquids;

import arc.struct.ObjectMap;
import mindustry.type.Liquid;
import arc.graphics.Color;

public class dynamicLiquids extends Liquid {

    public ObjectMap<String, Float> stats = new ObjectMap<>();

    public dynamicLiquids(String name) {
        super(name);
    }
}