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

public class accumulator extends GenericCrafter {

    // 构造函数
    public accumulator(String name) {
        super(name);

        // 基础属性
        health = 50;
        size = 3;
        hasLiquids = true;
        hasPower = false;
        hasItems = false;
        outputsLiquid = true;
        liquidCapacity = 9f;
        liquidPressure = 8.2f;
        buildTime = 2f * 60f;

        // 建造需要
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

        // 粒子系统属性
        private static final int MAX_PARTICLES = 300;
        private static final float MAX_RADIUS = 200f;
        private static final float SPAWN_INTERVAL = 0.03f;

        private Particle[] particles = new Particle[MAX_PARTICLES];
        private int particleCount = 0;
        private float spawnTimer = 0f;

        // 颜色配置（使用原代码中的颜色）
        private Color[] colorPalette = {
                Color.valueOf("#feff89"),  // 主色调
                Color.valueOf("#e7e89c"),  // 次要色调
                Color.valueOf("#c8c849")   // 高光色调
        };

        private float[] colorWeights = {0.7f, 0.4f, 0.4f};

        private Rand rand = new Rand();

        @Override
        public void placed() {
            super.placed();
            ThisTile = tile;
            rand.setSeed(Time.millis() + tile.pos());
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
            super.update();

            if(timer.get(0, 1.2f * 60f)) {
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

            // 更新粒子系统
            updateParticles();
        }

        private void updateParticles() {
            if (efficiency <= 0.01f) return;

            // 更新生成计时器
            spawnTimer += Time.delta / 1000f; // 转换为秒

            // 根据效率调整生成速率
            float adjustedSpawnInterval = SPAWN_INTERVAL / efficiency;

            // 生成新粒子
            while (spawnTimer >= adjustedSpawnInterval && particleCount < MAX_PARTICLES) {
                spawnTimer -= adjustedSpawnInterval;
                addParticle();
            }

            // 更新现有粒子
            for (int i = 0; i < particleCount; i++) {
                Particle p = particles[i];
                p.update(x, y);

                // 移除生命周期结束的粒子
                if (p.R <= 0) {
                    // 用最后一个粒子替换当前粒子，然后减少计数
                    particles[i] = particles[particleCount - 1];
                    particleCount--;
                    i--; // 重新检查当前位置
                }
            }
        }

        private void addParticle() {
            if (particleCount >= MAX_PARTICLES) return;

            // 随机属性
            float r = rand.random(MAX_RADIUS * 0.8f, MAX_RADIUS);
            float addAngle = rand.random(0.5f, 2.0f);
            float needsTime = rand.random(80.0f, 120.0f);

            // 创建新粒子
            Particle p = new Particle(
                    x, y, r, 0, 1f, needsTime,
                    addAngle, r, colorPalette, colorWeights
            );
            p.randAngle(rand);
            p.randColor(rand);

            // 添加到粒子数组
            particles[particleCount] = p;
            particleCount++;
        }

        @Override
        public void draw() {
            super.draw();
            if (!isVisible() || efficiency <= 0.01f) return;

            Draw.z(Layer.effect);

            // 绘制所有粒子
            for (int i = 0; i < particleCount; i++) {
                Particle p = particles[i];
                Draw.color(p.CurrentColor, p.A);

                int size = (int)(p.R * 0.1f);
                if (size <= 0) size = 1;

                // 绘制正方形粒子
                Fill.square(p.X, p.Y, size / 2f, 0);
            }

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

        // 粒子类
        private class Particle {
            float X, Y;
            float R;
            float Angle;
            float A;
            float CircleR;
            float NeedsTime;
            float AddAngle;
            Color[] ColorPalette;
            Color CurrentColor;
            float[] ColorsWeights;

            Particle(float x, float y, float r, float angle, float a,
                     float needsTime, float addAngle, float circleR,
                     Color[] palette, float[] weights) {
                X = x;
                Y = y;
                R = r;
                Angle = angle;
                A = a;
                NeedsTime = needsTime;
                AddAngle = addAngle;
                CircleR = circleR;
                ColorPalette = palette;
                ColorsWeights = weights;
                CurrentColor = palette[0];
            }

            void randColor(Rand rand) {
                float totalWeight = 0;
                for (float w : ColorsWeights) totalWeight += w;

                float randomValue = rand.random(totalWeight);
                float cumulativeWeight = 0;

                for (int i = 0; i < ColorPalette.length; i++) {
                    cumulativeWeight += ColorsWeights[i];
                    if (randomValue <= cumulativeWeight) {
                        CurrentColor = ColorPalette[i];
                        break;
                    }
                }
            }

            void randAngle(Rand rand) {
                Angle = rand.random(360f);
            }

            void update(float centerX, float centerY) {
                // 更新角度
                Angle += AddAngle * Time.delta / 16f; // 基于帧率的调整
                if (Angle >= 360f) Angle -= 360f;

                // 缩小半径
                R -= (CircleR / NeedsTime) * Time.delta / 16f;
                if (R < 0) R = 0;

                // 计算新位置
                float rad = (float)Math.toRadians(Angle);
                X = centerX + (float)Math.cos(rad) * R;
                Y = centerY + (float)Math.sin(rad) * R;

                // 随时间淡出
                A = Mathf.clamp(R / CircleR, 0f, 1f);
            }
        }
    }
}
