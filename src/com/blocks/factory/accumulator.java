package com.blocks.factory;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Tile;
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
        liquidCapacity = 14.7f;
        liquidPressure = 0.12f;
        buildTime = 2f * 60f;

        //建造需要
        requirements(
                Category.crafting,
                ItemStack.with(
                        Items.copper, 1
                )
        );

        outputLiquid = new LiquidStack(liquids.KS1688457, 0.12f);

        craftTime = 3f * 60f;
    }

    public class AccumulatorBuild extends Building {

        private int NeedSize = 6;
        private int BrrierNum = 0;
        private Tile ThisTile;

        @Override
        public void placed() {
            super.placed();
            ThisTile = tile;
        }

        private int GetBrrierNum() {
            BrrierNum = 0;
            for (int TargetX = -NeedSize; TargetX < NeedSize; TargetX++) {
                for (int TargetY = -NeedSize; TargetY < NeedSize; TargetY++) {
                    Tile Other = Vars.world.tile(ThisTile.x + TargetX, ThisTile.y + TargetY);
                    if (Other != null && (Other.block() != null || Other.overlay() != null)) {
                        BrrierNum++;
                    }
                }
            }
            return BrrierNum;
        }

        @Override
        public void update() {
            super.update();

            if(timer.get(0,2f * 60f))
            {
                BrrierNum = GetBrrierNum();

                efficiency = Mathf.clamp(1f - (BrrierNum/ 50f), 0.05f, 1f);
            }
        }

        @Override
        public void draw() {
            super.draw();

            // 绘制调试条（绿色：效率，红色：背景）
            float barWidth = 50f; // 条的总宽度
            float currentWidth = barWidth * efficiency; // 当前效率对应的长度

            // 绘制背景（红色）
            Draw.color(Color.red);
            Fill.rect(x, y - 20f, barWidth, 5f);

            // 绘制效率条（绿色）
            Draw.color(Color.green);
            Fill.rect(x - (barWidth - currentWidth) / 2f, y - 20f, currentWidth, 5f);

            // 重置颜色
            Draw.reset();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(BrrierNum); // 保存障碍物数量
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            BrrierNum = read.i(); // 加载障碍物数量
            ThisTile = tile; // 重新关联Tile
        }
    }
}