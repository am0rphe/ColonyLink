/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class AnnihilationPlaneDataProvider
implements BodyProvider<AnnihilationPlanePart>,
ServerDataProvider<AnnihilationPlanePart> {
    private static final String TAG_ENCHANTMENTS = "planeEnchantments";

    @Override
    public void buildTooltip(AnnihilationPlanePart plane, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (serverData.contains(TAG_ENCHANTMENTS, 10)) {
            tooltip.addLine((Component)InGameTooltip.EnchantedWith.text());
            CompoundTag enchantments = serverData.getCompound(TAG_ENCHANTMENTS);
            HolderLookup.RegistryLookup enchantmentRegistry = context.registries().lookupOrThrow(Registries.ENCHANTMENT);
            for (String enchantmentId : enchantments.getAllKeys()) {
                Optional enchantment = enchantmentRegistry.get(ResourceKey.create((ResourceKey)Registries.ENCHANTMENT, (ResourceLocation)ResourceLocation.parse((String)enchantmentId)));
                int level = enchantments.getInt(enchantmentId);
                enchantment.ifPresent(holder -> tooltip.addLine(Enchantment.getFullname((Holder)holder, (int)level)));
            }
        }
    }

    @Override
    public void provideServerData(Player player, AnnihilationPlanePart plane, CompoundTag serverData) {
        ItemEnchantments enchantments = plane.getEnchantments();
        if (enchantments != null && !enchantments.isEmpty()) {
            CompoundTag enchantmentsTag = new CompoundTag();
            for (Object2IntMap.Entry entry : enchantments.entrySet()) {
                enchantmentsTag.putInt(((Holder)entry.getKey()).getRegisteredName(), entry.getIntValue());
            }
            serverData.put(TAG_ENCHANTMENTS, (Tag)enchantmentsTag);
        }
    }
}

