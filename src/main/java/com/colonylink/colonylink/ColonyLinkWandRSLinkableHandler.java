package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler NBT pour la wand RS2.
 *
 * Architecture Option C :
 * On stocke la position du Wireless Transmitter lié directement en NBT,
 * exactement comme ColonyLinkWandLinkableHandler stocke la position du WAP AE2.
 * On accède au Network RS2 directement via le BlockEntity du transmitter,
 * sans passer par NetworkItemHelper (qui nécessite un SlotReference).
 *
 * Clés NBT :
 *   rs_linked_x/y/z/dim  → position du Wireless Transmitter lié
 *   builder_entries       → partagé avec le format AE2 (même record BuilderEntry)
 *   active_tab            → index de l'onglet actif
 *
 * Le joueur appaire via : Sneak + clic droit sur un Wireless Transmitter.
 * Tous les blocs dont le registryName contient "wireless_transmitter" sont acceptés
 * (gère les variantes de couleur : refinedstorage:red_wireless_transmitter, etc.)
 */
public class ColonyLinkWandRSLinkableHandler
{
    // ── Clés NBT lien Wireless Transmitter ──────────────────────────────────
    private static final String NBT_RS_LINKED_X   = "rs_linked_x";
    private static final String NBT_RS_LINKED_Y   = "rs_linked_y";
    private static final String NBT_RS_LINKED_Z   = "rs_linked_z";
    private static final String NBT_RS_LINKED_DIM = "rs_linked_dim";

    // ── Clés NBT multi-builder (même format que AE2) ─────────────────────────
    public static final String NBT_BUILDER_ENTRIES = "rs_builder_entries";
    public static final String NBT_ACTIVE_TAB      = "rs_active_tab";

    // ── Lien Wireless Transmitter ────────────────────────────────────────────

    /**
     * Lie la wand RS2 à un Wireless Transmitter.
     * Appelé depuis ColonyLinkWandRS.useOn() quand le joueur sneak+clique
     * sur un Wireless Transmitter.
     */
    public static void link(ItemStack stack, GlobalPos pos)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.putInt(NBT_RS_LINKED_X, pos.pos().getX());
            tag.putInt(NBT_RS_LINKED_Y, pos.pos().getY());
            tag.putInt(NBT_RS_LINKED_Z, pos.pos().getZ());
            tag.putString(NBT_RS_LINKED_DIM, pos.dimension().location().toString());
            return CustomData.of(tag);
        });
    }

    public static void unlink(ItemStack stack)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.remove(NBT_RS_LINKED_X);
            tag.remove(NBT_RS_LINKED_Y);
            tag.remove(NBT_RS_LINKED_Z);
            tag.remove(NBT_RS_LINKED_DIM);
            return CustomData.of(tag);
        });
    }

    public static boolean isLinked(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(NBT_RS_LINKED_DIM);
    }

    public static GlobalPos getLinkedPos(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains(NBT_RS_LINKED_DIM)) return null;

        int x = tag.getInt(NBT_RS_LINKED_X);
        int y = tag.getInt(NBT_RS_LINKED_Y);
        int z = tag.getInt(NBT_RS_LINKED_Z);
        String dimStr = tag.getString(NBT_RS_LINKED_DIM);

        ResourceKey<Level> dimKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(dimStr));
        return GlobalPos.of(dimKey, new BlockPos(x, y, z));
    }

    // ── Accès au réseau RS2 ──────────────────────────────────────────────────

    /**
     * Retourne le Network RS2 lié à la wand, ou null si non disponible.
     * Accède directement au BlockEntity du Wireless Transmitter via la position
     * stockée en NBT — pas de SlotReference nécessaire.
     */
    public static Network getNetwork(ItemStack stack, ServerLevel currentLevel)
    {
        GlobalPos linkedPos = getLinkedPos(stack);
        if (linkedPos == null) return null;

        ServerLevel transmitterLevel = currentLevel.getServer().getLevel(linkedPos.dimension());
        if (transmitterLevel == null) return null;

        var be = transmitterLevel.getBlockEntity(linkedPos.pos());
        if (!(be instanceof WirelessTransmitterBlockEntity transmitter)) return null;

        // WirelessTransmitterBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity
        // qui implémente NetworkItemTargetBlockEntity.getNetworkForItem()
        return transmitter.getNetworkForItem();
    }

    // ── Helpers multi-builder (même logique que AE2) ─────────────────────────

    public static List<BuilderEntry> getBuilderEntries(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return new ArrayList<>();
        var tag = data.copyTag();
        if (!tag.contains(NBT_BUILDER_ENTRIES)) return new ArrayList<>();
        return BuilderEntry.listFromNbt(tag.getList(NBT_BUILDER_ENTRIES, 10));
    }

    public static void setBuilderEntries(ItemStack stack, List<BuilderEntry> entries)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.put(NBT_BUILDER_ENTRIES, BuilderEntry.listToNbt(entries));
            return CustomData.of(tag);
        });
    }

    public static boolean addOrUpdateEntry(ItemStack stack, BuilderEntry entry)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        for (int i = 0; i < entries.size(); i++)
        {
            if (entries.get(i).builderPos().equals(entry.builderPos()))
            {
                entries.set(i, entry);
                setBuilderEntries(stack, entries);
                return true;
            }
        }
        if (entries.size() >= ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get()) return false;
        entries.add(entry);
        setBuilderEntries(stack, entries);
        return true;
    }

    public static boolean updateRedirectorForBuilder(ItemStack stack, BlockPos builderPos, BlockPos redirectorPos)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        for (int i = 0; i < entries.size(); i++)
        {
            if (entries.get(i).builderPos().equals(builderPos))
            {
                entries.set(i, entries.get(i).withRedirector(redirectorPos));
                setBuilderEntries(stack, entries);
                return true;
            }
        }
        return false;
    }

    public static void removeEntryAt(ItemStack stack, int index)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        if (index < 0 || index >= entries.size()) return;
        entries.remove(index);
        setBuilderEntries(stack, entries);
        int activeTab = getActiveTab(stack);
        if (entries.isEmpty())
            setActiveTab(stack, 0);
        else if (activeTab >= entries.size())
            setActiveTab(stack, entries.size() - 1);
    }

    public static int getActiveTab(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        var tag = data.copyTag();
        if (!tag.contains(NBT_ACTIVE_TAB)) return 0;
        int tab = tag.getInt(NBT_ACTIVE_TAB);
        List<BuilderEntry> entries = getBuilderEntries(stack);
        return entries.isEmpty() ? 0 : Math.min(tab, entries.size() - 1);
    }

    public static void setActiveTab(ItemStack stack, int tabIndex)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.putInt(NBT_ACTIVE_TAB, tabIndex);
            return CustomData.of(tag);
        });
    }

    public static BuilderEntry getActiveEntry(ItemStack stack)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        if (entries.isEmpty()) return null;
        int tab = getActiveTab(stack);
        return entries.get(Math.min(tab, entries.size() - 1));
    }

    public static BlockPos getActiveRedirectorPos(ItemStack stack)
    {
        BuilderEntry entry = getActiveEntry(stack);
        if (entry == null) return null;
        return entry.hasRedirector() ? entry.redirectorPos() : null;
    }

    public static BlockPos getActiveBuilderPos(ItemStack stack)
    {
        BuilderEntry entry = getActiveEntry(stack);
        return entry != null ? entry.builderPos() : null;
    }

    // ── Détection Wireless Transmitter ───────────────────────────────────────

    /**
     * Retourne true si le bloc à la position donnée est un Wireless Transmitter RS2.
     * Accepte toutes les variantes de couleur (refinedstorage:red_wireless_transmitter, etc.)
     */
    public static boolean isWirelessTransmitter(net.minecraft.world.level.Level level, BlockPos pos)
    {
        var be = level.getBlockEntity(pos);
        return be instanceof WirelessTransmitterBlockEntity;
    }
}
