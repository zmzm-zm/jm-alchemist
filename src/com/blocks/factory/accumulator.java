package com.blocks.factory;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import com.liquids.liquids;


public class accumulator extends GenericCrafter {

    //构造函数
    public accumulator(String name) {
        super(name);

        //基础属性
        health = 50;
        size = 3;
        hasLiquids = true;
        hasPower = false;
        hasItems = false;
        outputsLiquid = true;
        liquidCapacity = 1.3f;
        liquidPressure = 0.12f;

        //建造需要
        requirements(
                Category.crafting,
                ItemStack.with(
                        Items.copper, 1
                )
        );

        outputLiquid = new LiquidStack(liquids.KS1688457, 0.12f);;

        craftTime = 3f * 60f;
    }
}