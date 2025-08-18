package com.blocks.factory;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
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

        // 粒子系统参数
        private final int particleCount = 35;      // 粒子总数
        private final float maxRadius = 140f;      // 最大旋转半径
        private final float minRadius = 1f;        // 最小旋转半径
        private final float cycleTime = 10f;       // 完整生命周期（秒）
        private final float rotationSpeed = 1f;    // 基础旋转速度

        // 粒子属性存储
        private float[] particleRadii;            // 当前半径
        private float[] particleAngles;           // 当前角度
        private float[] particleTimeOffsets;      // 时间偏移量（个体差异）
        private Color[] particleColors;           // 粒子颜色
        private Vec2[] particlePositions;         // 计算位置缓存

        @Override
        public void placed() {
            super.placed();
            ThisTile = tile;
            // 初始化粒子种子
            particleSeeds = new float[35]; // 保持原始35个粒子
            Rand rand = new Rand();
            for(int i = 0; i < particleSeeds.length; i++){
                particleSeeds[i] = rand.random(360f); // 随机角度种子
            }
        }

        // 初始化粒子属性
        private void initParticles() {
            Rand rand = new Rand();

            // 颜色配置（与绘图效果一致）
            Color[] colors = {
                    Color.valueOf("#feff89"),  // 主色调
                    Color.valueOf("#e7e89c"),  // 次要色调
                    Color.valueOf("#c8c849")   // 高光色调
            };
            float[] weights = {0.4f, 0.3f, 0.3f};

            // 初始化粒子数组
            particleRadii = new float[particleCount];
            particleAngles = new float[particleCount];
            particleTimeOffsets = new float[particleCount];
            particleColors = new Color[particleCount];
            particlePositions = new Vec2[particleCount];

            // 为每个粒子生成随机属性
            for (int i = 0; i < particleCount; i++) {
                // 初始半径在最大半径范围内随机
                particleRadii[i] = rand.random(minRadius, maxRadius * 0.9f);

                // 随机角度（0-360度）
                particleAngles[i] = rand.random(360f);

                // 时间偏移（使粒子运动不同步）
                particleTimeOffsets[i] = rand.random(cycleTime);

                // 随机分配颜色（按权重）
                float colorChoice = rand.nextFloat();
                float cumulative = 0f;
                for (int c = 0; c < weights.length; c++) {
                    cumulative += weights[c];
                    if (colorChoice <= cumulative) {
                        particleColors[i] = colors[c];
                        break;
                    }
                }

                // 初始化位置缓存
                particlePositions[i] = new Vec2();
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

            // 保持原始颜色配置
            Color[] colors = {
                    Color.valueOf("#feff89"),
                    Color.valueOf("#e7e89c"),
                    Color.valueOf("#c8c849")
            };
            float[] weights = {0.4f, 0.3f, 0.3f};

            // 保持原始粒子数量计算
            int particleCount = Mathf.clamp((int)(35f * efficiency), 10, 35);
            float maxRadius = 140f;
            float minRadius = 1f;
            float cycleTime = 10f;

            for (int i = 0; i < particleCount; i++) {
                // 完全保持原始运动逻辑
                float seed = particleSeeds[i];
                float progress = ((Time.time / cycleTime) + (seed * 0.001f)) % 1f;
                float spiralProgress = progress < 0.5f ? progress * 2f : (1f - progress) * 2f;
                float currentRadius = minRadius + (maxRadius - minRadius) * spiralProgress;
                float angle = Time.time * 1f + (i * 137.5f) + seed;

                float px = x + Mathf.cosDeg(angle) * currentRadius;
                float py = y + Mathf.sinDeg(angle) * currentRadius;
                float a = Mathf.clamp(0.8f * efficiency * spiralProgress * 0.7f, 0.25f, 1f);

                // 仅修改绘制部分：使用正方形替代圆形
                Color particleColor = getColor(weights, colors);
                Draw.color(particleColor, a);
                Fill.square(px, py, 8f + efficiency * 3f, 45); // 正方形，旋转45度形成菱形
            }

            Draw.reset();
        }

        // 保持原始颜色选择逻辑
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
            initParticles();      // 重新初始化粒子
        }
    }
}