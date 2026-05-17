/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 */
package appeng.server.testworld;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.server.testplots.SpawnExtraGridTestTools;
import appeng.server.testworld.BuildAction;
import appeng.server.testworld.GridInitHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public record SpawnExtraGridTestToolsChest(BlockPos chestPos, BlockPos gridPos, ResourceLocation plotId) implements BuildAction
{
    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(this.chestPos);
    }

    @Override
    public void build(ServerLevel level, Player player, BlockPos origin) {
        BlockPos absChestPod = this.chestPos.offset((Vec3i)origin);
        BlockPos absGridPos = this.gridPos.offset((Vec3i)origin);
        level.setBlock(absChestPod, AEBlocks.SMOOTH_SKY_STONE_CHEST.block().defaultBlockState(), 3);
        GridInitHelper.doAfterGridInit(level, List.of(absGridPos), false, (grid, gridNode) -> {
            SkyChestBlockEntity chest = AEBlockEntities.SKY_CHEST.getBlockEntity((BlockGetter)level, absChestPod);
            if (chest != null) {
                InternalInventory inventory = chest.getInternalInventory();
                NeoForge.EVENT_BUS.post((Event)new SpawnExtraGridTestTools(this.plotId, inventory, (IGrid)grid));
            }
        });
    }
}

