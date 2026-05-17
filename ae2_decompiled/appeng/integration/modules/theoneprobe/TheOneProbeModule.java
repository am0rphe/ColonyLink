/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcjty.theoneprobe.api.IBlockDisplayOverride
 *  mcjty.theoneprobe.api.IProbeConfigProvider
 *  mcjty.theoneprobe.api.IProbeInfoProvider
 *  mcjty.theoneprobe.api.ITheOneProbe
 */
package appeng.integration.modules.theoneprobe;

import appeng.integration.modules.theoneprobe.AEConfigProvider;
import appeng.integration.modules.theoneprobe.BlockEntityInfoProvider;
import java.util.function.Function;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbeModule
implements Function<ITheOneProbe, Void> {
    @Override
    public Void apply(ITheOneProbe input) {
        input.registerProbeConfigProvider((IProbeConfigProvider)new AEConfigProvider());
        BlockEntityInfoProvider provider = new BlockEntityInfoProvider();
        input.registerProvider((IProbeInfoProvider)provider);
        input.registerBlockDisplayOverride((IBlockDisplayOverride)provider);
        return null;
    }
}

