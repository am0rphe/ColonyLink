package com.colonylink.colonylink;

import appeng.api.features.IGridLinkableHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler AE2 GridLinkable pour la wand.
 * Gère le lien WAP (AE2) — inchangé.
 * Ajoute les helpers NBT pour la liste de BuilderEntry (multi-builder v1.1.2).
 *
 * Clés NBT :
 *   linked_x/y/z/dim  → lien WAP AE2 (inchangé)
 *   builder_entries    → ListTag de BuilderEntry (nouveau)
 *   active_tab         → int, index de la tab active (nouveau)
 *
 * Compat v1.1.1 : si "last_builder_x" est présent (ancien format),
 * il est lu et converti en entrée unique dans builder_entries au premier accès.
 */
public class ColonyLinkWandLinkableHandler implements IGridLinkableHandler
{
    // ── Clés NBT WAP (inchangées) ────────────────────────────────────────
    private static final String NBT_LINKED_X   = "linked_x";
    private static final String NBT_LINKED_Y   = "linked_y";
    private static final String NBT_LINKED_Z   = "linked_z";
    private static final String NBT_LINKED_DIM = "linked_dim";

    // ── Clés NBT multi-builder ───────────────────────────────────────────
    public static final String NBT_BUILDER_ENTRIES = "builder_entries";
    public static final String NBT_ACTIVE_TAB      = "active_tab";

    // Clés legacy v1.1.1 (migration)
    private static final String LEGACY_BUILDER_X   = "last_builder_x";
    private static final String LEGACY_BUILDER_Y   = "last_builder_y";
    private static final String LEGACY_BUILDER_Z   = "last_builder_z";
    private static final String LEGACY_REDIRECTOR_X = "redirector_x";
    private static final String LEGACY_REDIRECTOR_Y = "redirector_y";
    private static final String LEGACY_REDIRECTOR_Z = "redirector_z";

    /** Lu depuis la config — permet jusqu'à 10 builders par wand. */
    public static int MAX_BUILDERS = 5; // valeur par défaut, écrasée par getMaxBuilders()

    /** Retourne la limite actuelle depuis la config (hot-reloadable). */
    public static int getMaxBuilders()
    {
        return ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get();
    }

    // ── IGridLinkableHandler (WAP AE2) ───────────────────────────────────

    @Override
    public boolean canLink(ItemStack stack)
    {
        return stack.getItem() instanceof ColonyLinkWand;
    }

    @Override
    public void link(ItemStack stack, GlobalPos pos)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.putInt(NBT_LINKED_X, pos.pos().getX());
            tag.putInt(NBT_LINKED_Y, pos.pos().getY());
            tag.putInt(NBT_LINKED_Z, pos.pos().getZ());
            tag.putString(NBT_LINKED_DIM, pos.dimension().location().toString());
            return CustomData.of(tag);
        });
    }

    @Override
    public void unlink(ItemStack stack)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.remove(NBT_LINKED_X);
            tag.remove(NBT_LINKED_Y);
            tag.remove(NBT_LINKED_Z);
            tag.remove(NBT_LINKED_DIM);
            return CustomData.of(tag);
        });
    }

    // ── Helpers WAP ──────────────────────────────────────────────────────

    public static boolean isLinked(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(NBT_LINKED_DIM);
    }

    public static GlobalPos getLinkedPos(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        if (!tag.contains(NBT_LINKED_DIM)) return null;

        int x = tag.getInt(NBT_LINKED_X);
        int y = tag.getInt(NBT_LINKED_Y);
        int z = tag.getInt(NBT_LINKED_Z);
        String dimStr = tag.getString(NBT_LINKED_DIM);

        ResourceKey<Level> dimKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(dimStr)
        );
        return GlobalPos.of(dimKey, new BlockPos(x, y, z));
    }

    // ── Helpers multi-builder ────────────────────────────────────────────

    /**
     * Retourne la liste complète des BuilderEntry stockées dans la wand.
     * Applique la migration depuis le format v1.1.1 si nécessaire.
     */
    public static List<BuilderEntry> getBuilderEntries(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return new ArrayList<>();
        var tag = data.copyTag();

        // Migration legacy → nouveau format
        if (!tag.contains(NBT_BUILDER_ENTRIES) && tag.contains(LEGACY_BUILDER_X))
        {
            List<BuilderEntry> migrated = new ArrayList<>();
            BlockPos bPos = new BlockPos(
                    tag.getInt(LEGACY_BUILDER_X),
                    tag.getInt(LEGACY_BUILDER_Y),
                    tag.getInt(LEGACY_BUILDER_Z));
            BlockPos rPos = tag.contains(LEGACY_REDIRECTOR_X)
                    ? new BlockPos(
                    tag.getInt(LEGACY_REDIRECTOR_X),
                    tag.getInt(LEGACY_REDIRECTOR_Y),
                    tag.getInt(LEGACY_REDIRECTOR_Z))
                    : BlockPos.ZERO;
            migrated.add(new BuilderEntry(bPos, rPos, "Builder", "N/A"));
            return migrated;
        }

        if (!tag.contains(NBT_BUILDER_ENTRIES)) return new ArrayList<>();
        return BuilderEntry.listFromNbt(tag.getList(NBT_BUILDER_ENTRIES, 10));
    }

    /** Écrit la liste de BuilderEntry dans le NBT de la wand. */
    public static void setBuilderEntries(ItemStack stack, List<BuilderEntry> entries)
    {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            var tag = data.copyTag();
            tag.put(NBT_BUILDER_ENTRIES, BuilderEntry.listToNbt(entries));
            // Nettoyage des clés legacy si présentes
            tag.remove(LEGACY_BUILDER_X);
            tag.remove(LEGACY_BUILDER_Y);
            tag.remove(LEGACY_BUILDER_Z);
            tag.remove(LEGACY_REDIRECTOR_X);
            tag.remove(LEGACY_REDIRECTOR_Y);
            tag.remove(LEGACY_REDIRECTOR_Z);
            return CustomData.of(tag);
        });
    }

    /**
     * Ajoute un BuilderEntry à la liste.
     * Vérifie qu'un entry avec le même builderPos n'existe pas déjà (écrase si oui).
     * Limite : MAX_BUILDERS entries.
     * Retourne false si la limite est atteinte et que l'entry n'existe pas déjà.
     */
    public static boolean addOrUpdateEntry(ItemStack stack, BuilderEntry entry)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);

        // Cherche un entry existant avec la même position builder
        for (int i = 0; i < entries.size(); i++)
        {
            if (entries.get(i).builderPos().equals(entry.builderPos()))
            {
                entries.set(i, entry);
                setBuilderEntries(stack, entries);
                return true;
            }
        }

        if (entries.size() >= getMaxBuilders()) return false;

        entries.add(entry);
        setBuilderEntries(stack, entries);
        return true;
    }

    /**
     * Met à jour le redirectorPos d'un entry existant identifié par builderPos.
     * Retourne false si l'entry n'existe pas.
     */
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

    /** Supprime l'entry à l'index donné. Ajuste activeTab si nécessaire. */
    public static void removeEntryAt(ItemStack stack, int index)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        if (index < 0 || index >= entries.size()) return;
        entries.remove(index);
        setBuilderEntries(stack, entries);

        // Ajuste l'index actif
        int activeTab = getActiveTab(stack);
        if (entries.isEmpty())
            setActiveTab(stack, 0);
        else if (activeTab >= entries.size())
            setActiveTab(stack, entries.size() - 1);
    }

    // ── Tab active ───────────────────────────────────────────────────────

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

    /**
     * Retourne l'entry de la tab active, ou null si la liste est vide.
     */
    public static BuilderEntry getActiveEntry(ItemStack stack)
    {
        List<BuilderEntry> entries = getBuilderEntries(stack);
        if (entries.isEmpty()) return null;
        int tab = getActiveTab(stack);
        return entries.get(Math.min(tab, entries.size() - 1));
    }

    /**
     * Retourne le redirectorPos de l'entry active, ou null si absent.
     * Compatibilité avec les appels legacy dans ServerTicker / CraftHandler.
     */
    public static BlockPos getActiveRedirectorPos(ItemStack stack)
    {
        BuilderEntry entry = getActiveEntry(stack);
        if (entry == null) return null;
        return entry.hasRedirector() ? entry.redirectorPos() : null;
    }

    /**
     * Retourne le builderPos de l'entry active, ou null.
     * Remplace getLastBuilderPos() dans les appels legacy.
     */
    public static BlockPos getActiveBuilderPos(ItemStack stack)
    {
        BuilderEntry entry = getActiveEntry(stack);
        return entry != null ? entry.builderPos() : null;
    }
}