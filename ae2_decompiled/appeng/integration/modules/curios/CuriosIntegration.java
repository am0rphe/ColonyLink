/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.capabilities.EntityCapability
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.integration.modules.curios;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class CuriosIntegration {
    public static final EntityCapability<IItemHandler, Void> ITEM_HANDLER = EntityCapability.createVoid((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"curios", (String)"item_handler"), IItemHandler.class);
}

