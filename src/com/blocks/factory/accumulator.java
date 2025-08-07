package com.blocks.factory;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;
import com.liquids.liquids;

import java.awt.*;

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
        liquidCapacity = 9f;
        liquidPressure = 8.2f;
        buildTime = 2f * 60f;

        //建造需要
        requirements(
                Category.crafting,
                ItemStack.with(
                        Items.copper, 1
                )
        );

        outputLiquid = new LiquidStack(liquids.KS1688457, 0.87f);

        craftTime = 2f * 60f;
    }

    public class AccumulatorBuild extends GenericCrafter.GenericCrafterBuild {

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
            // 修正循环范围：包含NeedSize（-6到6，共13×13=169个Tile）
            for (int TargetX = -NeedSize; TargetX <= NeedSize; TargetX++) {
                for (int TargetY = -NeedSize; TargetY <= NeedSize; TargetY++) {
                    Tile Other = Vars.world.tile(ThisTile.x + TargetX, ThisTile.y + TargetY);
                    if (Other != null && !Other.block().isAir()) {
                        BrrierNum++;
                    }
                }
            }
            return BrrierNum;
        }

        @Override
        public void update() {
            //super.update();

            if(timer.get(0,1.2f * 60f)) {
                BrrierNum = GetBrrierNum();
                Vars.ui.showInfoToast("障碍物：" + BrrierNum + " 效率：" + efficiency, 1f);
            }

            efficiency = Mathf.clamp(1f - ((float)BrrierNum / 120f), 0f, 1f);

            if (liquids.get(outputLiquid.liquid) >= liquidCapacity - 0.01f) {
                efficiency = 0f;
            }

            if (efficiency > 0) {
                progress += (efficiency / craftTime) * Time.delta;

                while (progress >= 1f) {
                    if (liquids.get(outputLiquid.liquid) + outputLiquid.amount <= liquidCapacity) {
                        liquids.add(outputLiquid.liquid, outputLiquid.amount);
                        progress -= 1f;
                    } else {
                        progress = 0f;
                        break;
                    }
                }
            } else {
                progress = 0f;
            }

            dumpLiquid(outputLiquid.liquid);

        }




        @Override
        public void draw() {
            super.draw();
            if (!isVisible()) return;

            // 移到 draw() 方法内
            if (efficiency > 0.01f) {
                Draw.z(Layer.effect);
                Color[] colors = {
                        Color.valueOf("#feff89"),
                        Color.valueOf("#e7e89c"),
                        Color.valueOf("#c8c849")
                };
                float[] weights = {0.4f, 0.3f, 0.3f};

                float R = Mathf.clamp(18f * (1f - efficiency), 0.08f, 18f);
                int particleCount = Mathf.clamp((int)(35f * efficiency), 10, 35);

                for (int i = 0; i < particleCount; i++) {
                    float angle = Time.time * 0.5f + i * 360f / particleCount;
                    float px = x + Mathf.cosDeg(angle) * R * (1f - efficiency * 0.8f);
                    float py = y + Mathf.sinDeg(angle) * R * (1f - efficiency * 0.8f);

                    Color NowColor = getColor(weights, colors);

                    Draw.color(NowColor, 0.8f * efficiency);

                    Fill.circle(px, py, 1f + efficiency * 2f);
                }
            }
            Draw.reset();
        }

        private Color getColor(float[] weights, Color[] colors) {
            float rand = Mathf.random();
            float cumulativeWeight = 0f;
            for (int i = 0; i < weights.length; i++) {
                cumulativeWeight += weights[i];
                if (rand <= cumulativeWeight) {
                    return colors[i];
                }
            }
            return colors[0];
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