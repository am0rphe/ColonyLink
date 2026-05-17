/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;
import appeng.core.AppEng;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public final class AECapabilities {
    public static BlockCapability<MEStorage, @Nullable Direction> ME_STORAGE = BlockCapability.createSided((ResourceLocation)AppEng.makeId("me_storage"), MEStorage.class);
    public static BlockCapability<ICraftingMachine, @Nullable Direction> CRAFTING_MACHINE = BlockCapability.createSided((ResourceLocation)AppEng.makeId("crafting_machine"), ICraftingMachine.class);
    public static BlockCapability<GenericInternalInventory, @Nullable Direction> GENERIC_INTERNAL_INV = BlockCapability.createSided((ResourceLocation)AppEng.makeId("generic_internal_inv"), GenericInternalInventory.class);
    public static BlockCapability<IInWorldGridNodeHost, Void> IN_WORLD_GRID_NODE_HOST = BlockCapability.createVoid((ResourceLocation)AppEng.makeId("inworld_gridnode_host"), IInWorldGridNodeHost.class);
    public static BlockCapability<ICrankable, @Nullable Direction> CRANKABLE = BlockCapability.createSided((ResourceLocation)AppEng.makeId("crankable"), ICrankable.class);

    private AECapabilities() {
    }
}

