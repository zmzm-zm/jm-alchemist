package com.liquids;

import mindustry.type.Liquid;
import arc.graphics.Color;

public class liquids {
    public static dynamicLiquids KS1688457;

    public static void load() {
        KS1688457 = new dynamicLiquids("KS1688457") {{
            color = Color.valueOf("#faf391");
            lightColor = Color.valueOf("#f5e133");

            flammability = 0.0f;
            explosiveness = 0.0f;
            temperature = 0.0f;
            viscosity = 0.0f;

            stats.put("cursePower",5.7f);

            gas = true;

            localizedName = "KS1688457";
            description = "没人知道KS1688457究竟代表着什么，也没有人知道他的名字从何而来，但大家都知道的是————他是炼金之根基";
        }};
    }

}