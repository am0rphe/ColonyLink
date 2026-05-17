/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.HolderSet
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.worldgen.BootstrapContext
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.levelgen.GenerationStep$Decoration
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.level.levelgen.structure.Structure$StructureSettings
 *  net.minecraft.world.level.levelgen.structure.StructureSet
 *  net.minecraft.world.level.levelgen.structure.StructureType
 *  net.minecraft.world.level.levelgen.structure.TerrainAdjustment
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
 *  net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
 *  net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType
 *  net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package appeng.init.worldgen;

import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InitStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create((ResourceKey)Registries.STRUCTURE_TYPE, (String)"ae2");
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create((ResourceKey)Registries.STRUCTURE_PIECE, (String)"ae2");

    private InitStructures() {
    }

    public static void initDatagenStructures(BootstrapContext<Structure> context) {
        HolderGetter biomes = context.lookup(Registries.BIOME);
        context.register(MeteoriteStructure.KEY, (Object)new MeteoriteStructure(new Structure.StructureSettings((HolderSet)biomes.getOrThrow(MeteoriteStructure.BIOME_TAG_KEY), Map.of(), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, TerrainAdjustment.NONE)));
    }

    public static void initDatagenStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter structures = context.lookup(Registries.STRUCTURE);
        Holder.Reference meteorite = structures.getOrThrow(MeteoriteStructure.KEY);
        StructureSet structureSet = new StructureSet(List.of(StructureSet.entry((Holder)meteorite)), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654));
        context.register(MeteoriteStructure.STRUCTURE_SET_KEY, (Object)structureSet);
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_PIECES.register("ae2mtrt", () -> MeteoriteStructurePiece.TYPE);
        STRUCTURE_TYPES.register("ae2mtrt", () -> MeteoriteStructure.TYPE);
        STRUCTURE_PIECES.register(eventBus);
        STRUCTURE_TYPES.register(eventBus);
    }
}

