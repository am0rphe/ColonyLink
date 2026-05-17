/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.gametest.framework.TestCommand
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins.tests;

import appeng.server.testplots.TestPlots;
import appeng.server.testworld.Plot;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={TestCommand.class})
public class TestCommandMixin {
    @Inject(method={"verifyStructureExists"}, at={@At(value="HEAD")}, cancellable=true)
    private static void verifyStructureExists(ServerLevel level, String structureName, CallbackInfoReturnable<Boolean> cri) {
        Plot testPlot = TestPlots.getById(ResourceLocation.parse((String)structureName));
        if (testPlot != null) {
            cri.setReturnValue((Object)true);
        }
    }
}

