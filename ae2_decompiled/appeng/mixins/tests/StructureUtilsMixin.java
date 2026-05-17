/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.gametest.framework.GameTestInfo
 *  net.minecraft.gametest.framework.StructureUtils
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.StructureBlockEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins.tests;

import appeng.server.testplots.TestPlots;
import appeng.server.testworld.GameTestPlotAdapter;
import appeng.server.testworld.Plot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={StructureUtils.class}, priority=0)
public abstract class StructureUtilsMixin {
    @Inject(method={"prepareTestStructure"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;")}, cancellable=true)
    private static void prepareTestStructure(GameTestInfo testInfo, BlockPos pos, Rotation rotation, ServerLevel level, CallbackInfoReturnable<StructureBlockEntity> cri) {
        ResourceLocation id = ResourceLocation.tryParse((String)testInfo.getStructureName());
        if (id == null) {
            return;
        }
        Plot testPlot = TestPlots.getById(id);
        if (testPlot == null) {
            return;
        }
        cri.setReturnValue((Object)GameTestPlotAdapter.createStructure(testPlot, testInfo, pos, level));
    }
}

