package com.blocks.factory;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.gen.Building;
import com.items.items;

public class KSImpactor extends Block {
    public float maxLinkR = 8.0f;
    public float liquidTransferRate = 0.02f;

    public KSImpactor(String name) {
        super(name);
        requirements(Category.crafting, ItemStack.with(items.CursedGold, 50));

        solid = true;
        update = true;
        hasLiquids = true;
        hasPower = true;
        configurable = true; // 启用配置功能
        saveConfig = true; // 保存配置

        size = 2;
        health = 10;
        buildTime = 1.0f * 60f;
    }

    public class TargetBuild extends Building {
        public Building linkedTarget;

        public boolean onConfigureTileTapped(Building other){
            // 如果点击的是自己，清除链接
            if(other == this){
                if(linkedTarget != null){
                    ((sublimation.LiquidCraftingBuild)linkedTarget).removeImpactorLink(this);
                    linkedTarget = null;
                }
                return false;
            }

            // 检查目标是否有效
            if(isValidTargetBuilding(other) && other.within(this, maxLinkR)){
                // 如果已有链接，先移除旧链接
                if(linkedTarget != null){
                    ((sublimation.LiquidCraftingBuild)linkedTarget).removeImpactorLink(this);
                }

                // 建立新链接
                linkedTarget = other;
                ((sublimation.LiquidCraftingBuild)linkedTarget).addImpactorLink(this);
                return true;
            }

            return false;
        }

        @Override
        public void updateTile() {
            if(linkedTarget != null && liquids.currentAmount() > 0) {
                if(isValidTarget(linkedTarget)) {
                    moveLiquid(linkedTarget, liquids.current());
                }
            }
        }

        @Override
        public void onRemoved() {
            if(linkedTarget != null && linkedTarget instanceof sublimation.LiquidCraftingBuild) {
                ((sublimation.LiquidCraftingBuild)linkedTarget).removeImpactorLink(this);
            }
            super.onRemoved();
        }

        boolean isValidTarget(Building target) {
            return target != null
                    && !target.dead
                    && target.block.hasLiquids
                    && target.team == team()
                    && target.within(this, maxLinkR)
                    && target.liquids.currentAmount() < target.block.liquidCapacity;
        }

    }

    boolean isValidTargetBuilding(Building target) {
        return target != null
                && (target.block instanceof sublimation);
    }
}