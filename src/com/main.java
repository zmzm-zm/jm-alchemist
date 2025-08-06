package com;

import com.blocks.factory.KSImpactor;
import com.blocks.factory.sublimation;
import com.blocks.factory.accumulator;

import com.items.items;
import com.liquids.liquids;
import mindustry.mod.*;

public class main extends Mod{

    public main(){

    }

    @Override
    public void loadContent() {

        items.load();
        liquids.load();

        new sublimation("sublimation"){{
            localizedName = "升华器";
            description = "蓄能完毕后，产生诅咒黄金————咒金";
        }};

        new KSImpactor("KSImpactor"){{
            localizedName = "KS冲击器";
            description = "把KS1688457强行冲进‘升化器’内";
            details = "这种鲁莽的手段看起来非常不可靠，但是据百年来的炼金史来看......他应该不会产生什么不可逆的后果。\n但愿吧";
        }};

        new accumulator("accumulator"){{
            localizedName = "蓄能器";
            description = "从环境中吸取KS1688457，他需要一个开阔的空间";
        }};
    }

}
