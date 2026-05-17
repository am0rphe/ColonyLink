/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.integration.modules.igtooltip.parts.PartTooltipProviders;
import appeng.util.Platform;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PartHostTooltips {
    private PartHostTooltips() {
    }

    @Nullable
    public static Component getName(BlockEntity object, TooltipContext context) {
        return PartHostTooltips.getName((IPartHost)object, context);
    }

    @Nullable
    public static Component getName(IPartHost object, TooltipContext context) {
        SelectedPart selected = PartHostTooltips.getPart(object, context.hitLocation());
        if (selected.facade != null) {
            return selected.facade.getItemStack().getHoverName();
        }
        if (selected.part != null) {
            for (NameProvider<IPart> provider : PartTooltipProviders.getProviders(selected.part).nameProviders()) {
                Component name = provider.getName(selected.part, context);
                if (name == null) continue;
                return name;
            }
            return selected.part.getPartItem().asItem().getDescription();
        }
        return null;
    }

    @Nullable
    public static String getModName(BlockEntity blockEntity, TooltipContext context) {
        return PartHostTooltips.getModName((IPartHost)blockEntity, context);
    }

    @Nullable
    public static String getModName(IPartHost object, TooltipContext context) {
        Item item;
        SelectedPart selected = PartHostTooltips.getPart(object, context.hitLocation());
        if (selected.facade != null) {
            item = selected.facade.getItemStack().getItem();
        } else if (selected.part != null) {
            item = selected.part.getPartItem().asItem();
        } else {
            return null;
        }
        return Platform.getModName(BuiltInRegistries.ITEM.getKey((Object)item).getNamespace());
    }

    @Nullable
    public static ItemStack getIcon(BlockEntity object, TooltipContext context) {
        return PartHostTooltips.getIcon((IPartHost)object, context);
    }

    @Nullable
    public static ItemStack getIcon(IPartHost object, TooltipContext context) {
        SelectedPart selected = PartHostTooltips.getPart(object, context.hitLocation());
        if (selected.facade != null) {
            return selected.facade.getItemStack();
        }
        if (selected.part != null) {
            for (IconProvider<IPart> provider : PartTooltipProviders.getProviders(selected.part).iconProviders()) {
                ItemStack icon = provider.getIcon(selected.part, context);
                if (icon == null) continue;
                return icon;
            }
            return new ItemStack(selected.part.getPartItem());
        }
        return null;
    }

    public static void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        PartHostTooltips.buildTooltip((IPartHost)object, context, tooltip);
    }

    public static void buildTooltip(IPartHost object, TooltipContext context, TooltipBuilder tooltip) {
        SelectedPart selected = PartHostTooltips.getPart(object, context.hitLocation());
        if (selected.part != null) {
            CompoundTag partTag = context.serverData().getCompound(PartHostTooltips.getPartDataName(selected.side));
            PartHostTooltips.buildPartTooltip(selected.part, partTag, context, tooltip);
        }
    }

    private static <T extends IPart> void buildPartTooltip(T part, CompoundTag partTag, TooltipContext blockContext, TooltipBuilder tooltip) {
        TooltipContext partContext = new TooltipContext(partTag, blockContext.hitLocation(), blockContext.player());
        for (BodyProvider<T> provider : PartTooltipProviders.getProviders(part).bodyProviders()) {
            provider.buildTooltip(part, partContext, tooltip);
        }
    }

    public static void provideServerData(Player player, BlockEntity object, CompoundTag serverData) {
        PartHostTooltips.provideServerData(player, (IPartHost)object, serverData);
    }

    public static void provideServerData(Player player, IPartHost object, CompoundTag serverData) {
        CompoundTag partTag = new CompoundTag();
        for (Direction location : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = object.getPart(location);
            if (part == null) continue;
            for (ServerDataProvider<IPart> provider : PartTooltipProviders.getProviders(part).serverDataProviders()) {
                provider.provideServerData(player, part, partTag);
            }
            if (partTag.isEmpty()) continue;
            serverData.put(PartHostTooltips.getPartDataName(location), (Tag)partTag);
            partTag = new CompoundTag();
        }
    }

    private static String getPartDataName(@Nullable Direction location) {
        return "cableBusPart" + (location == null ? "center" : location.name());
    }

    private static SelectedPart getPart(IPartHost partHost, Vec3 hitLocation) {
        return partHost.selectPartWorld(hitLocation);
    }
}

