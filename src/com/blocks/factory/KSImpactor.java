package com.blocks.factory;

import arc.math.geom.Position;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.gen.Building;
import mindustry.type.Liquid;

import com.items.items;

public class KSImpactor extends Block {

    public float maxLinkR = 8.0f;

    public float liquidTransferRate = 0.02f;
    private Team team;

    public KSImpactor(String name) {
        super(name);
        requirements(Category.crafting, ItemStack.with(items.CursedGold, 50));

        solid = true;
        update = true;
        hasLiquids = true;
    }



    public class TargetBuild extends Building {
        public Building linkedTarget;

        @Override
        public void updateTile() {

            if(linkedTarget != null && liquids.currentAmount() > 0) {

                if(isValidTarget(linkedTarget)) {
                    moveLiquid(linkedTarget,liquids.current());
                }
            }
        }



        public void linkTo(Tile targetTile) {
            if (targetTile.build != null
                    && targetTile.build.block instanceof sublimator
                    && targetTile.dst2(tile) <= maxLinkR * maxLinkR) {
                linkedTarget = targetTile.build;
                // 通知目标sublimator有新的链接
                ((sublimator.LiquidCraftingBuild)targetTile.build).addImpactorLink(this);
            }
        }

        @Override
        public void onRemoved() {
            if(linkedTarget != null && linkedTarget instanceof sublimator.LiquidCraftingBuild) {
                ((sublimator.LiquidCraftingBuild)linkedTarget).removeImpactorLink(this);
            }
            super.onRemoved();
        }

    }

    boolean isValidTarget(Building target) {
        return target != null                   // 目标存在
                && !target.dead                     // 未被摧毁
                && target.block.hasLiquids          // 能接收液体
                && target.team == this.team             // 同队伍
                && target.dst((Position) this) <= maxLinkR       // 在范围内
                && target.liquids.currentAmount() < target.block.liquidCapacity; // 有空余容量
    }



    boolean isValidTargetBuilding(Building target) {
        return target != null
                && (target.block instanceof com.blocks.factory.sublimator); // 或其他允许的建筑
    }
}