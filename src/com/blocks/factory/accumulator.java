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
        private float[] particleSeeds = new float[40];


        @Override
        public void placed() {
            super.placed();
            ThisTile = tile;
            // 初始化粒子种子
            for(int i = 0; i < particleSeeds.length; i++){
                particleSeeds[i] = Mathf.random(0f, 1000f);
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

            // 当不可见或效率过低时不绘制特效
            if (!isVisible() || efficiency <= 0.01f) return;

            Draw.z(Layer.effect); // 设置绘制层级为特效层

            // 颜色配置 - 使用原有的三种黄色调
            Color[] colors = {
                    Color.valueOf("#feff89"), // 亮黄色
                    Color.valueOf("#e7e89c"), // 浅黄
                    Color.valueOf("#c8c849") // 深黄
            };

            // 粒子参数
            int particleCount = Mathf.clamp((int)(35f * efficiency), 10, 35);
            float baseSize = 2f + efficiency * 3f; // 基础粒子大小
            float lifeScale = 1f + efficiency * 2f; // 生命周期缩放

            // 绘制粒子效果
            for (int i = 0; i < particleCount; i++) {
                // 使用预生成的种子确保粒子行为一致
                float seed = particleSeeds[i % particleSeeds.length];

                // 计算粒子生命周期 (0-1循环)
                float life = ((Time.time + seed) % 8f) / 8f;

                // 粒子位置计算 - 螺旋上升效果
                float angle = life * 360f * 2f + seed; // 旋转角度
                float radius = life * 40f; // 随生命周期增加的半径

                // 计算粒子位置
                float px = x + Mathf.cosDeg(angle) * radius;
                float py = y + Mathf.sinDeg(angle) * radius + life * 20f; // 向上移动

                // 粒子大小随生命周期变化
                float size = baseSize * (1f - Math.abs(life - 0.5f) * 1.8f);

                // 透明度随生命周期变化
                float alpha = Mathf.clamp(1f - Math.abs(life - 0.5f) * 2f) * efficiency;

                // 选择颜色 - 使用权重随机选择
                Color color = colors[(int)(life * colors.length) % colors.length];

                // 绘制粒子
                Draw.color(color, alpha);
                Fill.circle(px, py, size);

                // 添加光晕效果
                if(size > 3f){
                    Draw.color(color, alpha * 0.3f);
                    Fill.circle(px, py, size * 1.8f);
                }
            }

            Draw.reset(); // 重置绘制状态
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