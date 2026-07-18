package com.colonylink.colonylink;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * DomumQueueManager — v1.6.1
 *
 * Queue Domum PARTAGEE par reseau AE2 (grille). Une seule liste par IGrid, cote serveur.
 * Tous les Warehouse Link Terminals d'une meme grille lisent et ecrivent cette liste
 * unique : un item mis en file apparait donc sur TOUS les terminaux du reseau (cable
 * commun, Quantum Ring, etc.), pas seulement celui qui a recu le clic.
 *
 * Persistance : chaque terminal garde une copie NBT de la liste (champ domumQueue),
 * utilisee comme FILET pour re-semer le manager si la grille se reforme (reconnexion,
 * rechargement du monde, (re)connexion d'un Quantum Ring) — l'identite d'un IGrid n'est
 * pas stable. WeakHashMap : quand une grille meurt (plus aucune reference forte, ex.
 * apres un unload de monde), son entree est evacuee automatiquement par le GC.
 *
 * Server-thread only ; methodes synchronized par prudence (plusieurs terminaux tickent).
 * Le seed est un Supplier : il n'est evalue QUE lors du premier acces a une grille
 * (computeIfAbsent), donc aucun cout quand la liste partagee existe deja.
 */
public final class DomumQueueManager
{
    private static final Map<IGrid, List<ItemStack>> QUEUES = new WeakHashMap<>();

    private DomumQueueManager() {}

    private static List<ItemStack> queueFor(IGrid grid, Supplier<List<ItemStack>> seed)
    {
        return QUEUES.computeIfAbsent(grid, g -> {
            List<ItemStack> l = new ArrayList<>();
            if (seed != null)
            {
                List<ItemStack> s = seed.get();
                if (s != null)
                    for (ItemStack it : s)
                        if (!it.isEmpty()) l.add(it.copyWithCount(1));
            }
            return l;
        });
    }

    /** Copie de la queue partagee (affichage / sync / persistance NBT). */
    public static synchronized List<ItemStack> snapshot(IGrid grid, Supplier<List<ItemStack>> seed)
    {
        if (grid == null) return new ArrayList<>();
        return new ArrayList<>(queueFor(grid, seed));
    }

    /** Vrai si la grille n'a pas (encore) de liste ou si elle est vide. Ne seme pas. */
    public static synchronized boolean isEmpty(IGrid grid)
    {
        if (grid == null) return true;
        List<ItemStack> q = QUEUES.get(grid);
        return q == null || q.isEmpty();
    }

    public static synchronized boolean isQueued(IGrid grid, Supplier<List<ItemStack>> seed, ItemStack stack)
    {
        if (grid == null || stack.isEmpty()) return false;
        for (ItemStack s : queueFor(grid, seed))
            if (ItemStack.isSameItemSameComponents(s, stack)) return true;
        return false;
    }

    /** Ajoute si absent. Retourne true si ajoute (false = deja en file). */
    public static synchronized boolean add(IGrid grid, Supplier<List<ItemStack>> seed, ItemStack stack)
    {
        if (grid == null || stack.isEmpty()) return false;
        List<ItemStack> q = queueFor(grid, seed);
        for (ItemStack s : q)
            if (ItemStack.isSameItemSameComponents(s, stack)) return false;
        q.add(stack.copyWithCount(1));
        return true;
    }

    /** Retire par index (encode pattern / retrait manuel). */
    public static synchronized void removeAt(IGrid grid, Supplier<List<ItemStack>> seed, int index)
    {
        if (grid == null) return;
        List<ItemStack> q = queueFor(grid, seed);
        if (index >= 0 && index < q.size()) q.remove(index);
    }

    /** Retire tous les items devenus craftables sur la grille. Retourne true si change. */
    public static synchronized boolean pruneCraftable(IGrid grid, Supplier<List<ItemStack>> seed, ICraftingService crafting)
    {
        if (grid == null || crafting == null) return false;
        List<ItemStack> q = queueFor(grid, seed);
        boolean changed = false;
        for (int i = q.size() - 1; i >= 0; i--)
        {
            AEItemKey key = AEItemKey.of(q.get(i));
            if (key != null && crafting.isCraftable(key))
            {
                q.remove(i);
                changed = true;
            }
        }
        return changed;
    }
}