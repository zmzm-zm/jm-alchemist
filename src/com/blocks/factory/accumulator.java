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
import arc.math.Mathf;

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
        private float[] particleSeeds;


        @Override
        public void placed() {
            super.placed();
            ThisTile = tile;

            particleSeeds = new float[35]; // 最大粒子数量
            for(int i = 0; i < particleSeeds.length; i++){
                particleSeeds[i] = Mathf.random(0f, 1000f); // 随机初始相位
            }
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
            if (!isVisible() || efficiency <= 0.01f) return;

            Draw.z(Layer.effect);

            // 颜色配置
            Color[] colors = {
                    Color.valueOf("#feff89"),
                    Color.valueOf("#e7e89c"),
                    Color.valueOf("#c8c849")
            };
            float[] weights = {0.4f, 0.3f, 0.3f};

            // 粒子参数
            int particleCount = Mathf.clamp((int)(35f * efficiency), 10, 35);
            float maxRadius = 140f; // 最大半径
            float minRadius = 1f;  // 最小半径（中心区域）
            float cycleTime = 10f;

            float spiralSpeed = 0.5f;

            for (int i = 0; i < particleCount; i++) {
                // 使用唯一种子计算独立参数
                float seed = particleSeeds[i];
                float progress = ((Time.time / cycleTime) + (seed * 0.001f)) % 1f;
                float spiralProgress = progress < 0.5f ? progress * 2f : (1f - progress) * 2f;
                float currentRadius = minRadius + (maxRadius - minRadius) * spiralProgress;

                // 角度加入种子影响
                float angle = Time.time * 1f + (i * 137.5f) + seed; // 137.5°黄金角度分散

                float px = x + Mathf.cosDeg(angle) * currentRadius;
                float py = y + Mathf.sinDeg(angle) * currentRadius;

                float a = Mathf.clamp(0.8f * efficiency * spiralProgress * 0.7f, 0.25f, 1f);


                Color particleColor = getColor(weights, colors);
                Draw.color(particleColor, a); // 透明度随进度变化
                Fill.circle(px, py, 8f + efficiency * 6f);
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