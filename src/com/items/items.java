package com.items;

import arc.graphics.Color;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.content.Items;

public class items {
    public static Item CursedGold;

    public static void load() {
        CursedGold = new Item("cursed-gold") {{
            hardness = -1;
            cost = 1.0f;
            flammability = 0f;
            explosiveness = 0f;
            radioactivity = 0f;
            charge = 0f;
            color = Color.valueOf("#FFB100");
        }};
    }
}