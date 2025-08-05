package com.blocks.factory;

import com.items.items;
import com.liquids.liquids;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.meta.*;
import mindustry.gen.*;
import mindustry.content.*;
import mindustry.graphics.*;
import java.util.HashSet;
import java.util.Set;

public class sublimator extends Block {
    public Item outputItem = items.CursedGold;
    public Liquid inputLiquid = liquids.KS1688457;
    public float liquidCost = 0.5f;
    public int itemAmount = 5; // 每次生产的物品数量
    public float craftTime = 20f * 60f; // 20秒（以帧为单位，60fps）

    public sublimator(String name) {
        super(name);

        // 基础属性
        requirements(Category.crafting, ItemStack.with(Items.copper, 50, Items.lead, 30));
        size = 5;
        hasLiquids = true;
        hasItems = true;
        solid = true;
        health = 600;
        buildTime = 10f * 60f;;

        // 添加液体消耗
        consume(new ConsumeLiquid(inputLiquid, liquidCost));
    }

    public class LiquidCraftingBuild extends Building {
        float progress = 0f; // 生产进度
        Set<KSImpactor.TargetBuild> linkedImpactors = new HashSet<>();

        // 添加链接
        public void addImpactorLink(KSImpactor.TargetBuild impactor) {
            linkedImpactors.add(impactor);
        }

        // 移除链接
        public void removeImpactorLink(KSImpactor.TargetBuild impactor) {
            linkedImpactors.remove(impactor);
        }

        // 计算链接的Impactor数量
        public Set<KSImpactor.TargetBuild> countLinkedImpactors() {
            return linkedImpactors;
        }

        @Override
        public void updateTile() {
            // 检查是否有足够液体
            linkedImpactors = countLinkedImpactors();

            if (liquids.get(inputLiquid) >= liquidCost) {
                progress += delta(); // 增加进度（delta()是帧时间）

                // 进度完成时生产物品
                if (progress >= craftTime) {
                    items.add(outputItem, itemAmount); // 添加物品
                    liquids.remove(inputLiquid, liquidCost); // 移除液体
                    progress = 0f; // 重置进度
                }
            } else {
                progress = 0f; // 液体不足时重置进度
            }
        }
    }
}