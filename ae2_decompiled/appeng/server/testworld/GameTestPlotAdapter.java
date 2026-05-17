/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.gametest.framework.GameTestGenerator
 *  net.minecraft.gametest.framework.GameTestInfo
 *  net.minecraft.gametest.framework.StructureUtils
 *  net.minecraft.gametest.framework.TestFunction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.StructureBlockEntity
 *  net.minecraft.world.level.block.state.properties.StructureMode
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
 */
package appeng.server.testworld;

import appeng.server.testplots.TestPlots;
import appeng.server.testworld.Plot;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.Test;
import appeng.util.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class GameTestPlotAdapter {
    @GameTestGenerator
    public List<TestFunction> gameTestAdapter() {
        ArrayList<TestFunction> result = new ArrayList<TestFunction>();
        for (Plot plot : TestPlots.createPlots()) {
            Test test = plot.getTest();
            if (test == null) continue;
            result.add(new TestFunction("ae2", "ae2." + plot.getId().getPath(), plot.getId().toString(), Rotation.NONE, test.maxTicks, (long)test.setupTicks, true, false, 1, 1, test.skyAccess, gameTestHelper -> test.getTestFunction().accept(new PlotTestHelper(GameTestPlotAdapter.getPlotTranslation(plot.getBounds()), gameTestHelper.testInfo))));
        }
        return result;
    }

    public static StructureTemplate getStructureTemplate(String structureName) {
        ResourceLocation id = ResourceLocation.tryParse((String)structureName);
        if (id == null) {
            return null;
        }
        Plot plot = TestPlots.getById(id);
        if (plot != null) {
            StructureTemplate template = new StructureTemplate();
            CompoundTag tag = new CompoundTag();
            ListTag sizeList = new ListTag();
            BoundingBox bounds = plot.getBounds();
            sizeList.add((Object)IntTag.valueOf((int)bounds.getXSpan()));
            sizeList.add((Object)IntTag.valueOf((int)bounds.getYSpan()));
            sizeList.add((Object)IntTag.valueOf((int)bounds.getZSpan()));
            tag.put("size", (Tag)sizeList);
            template.load((HolderGetter)BuiltInRegistries.BLOCK.asLookup(), tag);
            return template;
        }
        return null;
    }

    public static StructureBlockEntity createStructure(Plot plot, GameTestInfo info, BlockPos pos, ServerLevel level) {
        BoundingBox plotBounds = plot.getBounds();
        Vec3i size = new Vec3i(plotBounds.getXSpan(), plotBounds.getYSpan(), plotBounds.getZSpan());
        BoundingBox boundingbox = StructureUtils.getStructureBoundingBox((BlockPos)pos, (Vec3i)size, (Rotation)Rotation.NONE);
        boundingbox.intersectingChunks().forEach(cp -> level.setChunkForced(cp.x, cp.z, true));
        StructureUtils.clearSpaceForStructure((BoundingBox)boundingbox, (ServerLevel)level);
        level.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureBlock = (StructureBlockEntity)level.getBlockEntity(pos);
        structureBlock.setMode(StructureMode.LOAD);
        structureBlock.setIgnoreEntities(false);
        structureBlock.setStructureName(ResourceLocation.parse((String)info.getStructureName()));
        structureBlock.setMetaData(info.getTestName());
        structureBlock.setStructureSize(size);
        BoundingBox bounds = plot.getBounds();
        BlockPos origin = pos.offset((Vec3i)structureBlock.getStructurePos()).offset((Vec3i)GameTestPlotAdapter.getPlotTranslation(bounds));
        plot.build(level, Platform.getFakePlayer(level, null), origin);
        return structureBlock;
    }

    private static BlockPos getPlotTranslation(BoundingBox bounds) {
        return new BlockPos(bounds.minX() < 0 ? -bounds.minX() : 0, bounds.minY() < 0 ? -bounds.minY() : 0, bounds.minZ() < 0 ? -bounds.minZ() : 0);
    }
}

