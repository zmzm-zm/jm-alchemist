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
            localizedName = "咒金";
            description = "'神'在临走前留下了它的配方，上面乌黑的印记可能有什么特殊作用";
        }};
    }
}