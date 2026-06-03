package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * v1.4.11 — Vérifications de chargement de chunks (côté MineColonies).
 *
 * Politique « tout ou rien » : aucune opération ColonyLink sur la colonie, le warehouse
 * ou le builder n'est tentée si l'un de leurs chunks n'est pas RÉELLEMENT vivant. On
 * avertit le joueur et on annule, plutôt que d'opérer sur des inventaires non simulés.
 *
 * IMPORTANT — chunk « présent » ≠ chunk « vivant » :
 *   MineColonies garde une colonie « chargée » via un ticket NON-TICKING quand aucun
 *   joueur n'est à portée. Les chunks et leurs données restent en mémoire :
 *     - {@link Level#hasChunkAt(BlockPos)} renvoie {@code true} (les LECTURES marchent :
 *       le Clipboard voit les items, la colonie est trouvée) ;
 *     - MAIS le monde ne SIMULE pas ces chunks. Une ÉCRITURE (insérer dans un rack) se
 *       fait sur une BlockEntity gelée et ne se propage/persiste pas → on affiche
 *       « sent to builder » alors que rien n'arrive (voire voiding selon le timing).
 *   On exige donc {@link ServerLevel#isPositionEntityTicking(BlockPos)} (chunk vivant :
 *   joueur à portée de simulation OU ticket ticking), pas la simple présence.
 *
 * Côté Applied Energistics : AUCUNE vérification manuelle ici, volontairement.
 *   - AE2 retire lui-même les nœuds déchargés de la grille (split réseau) : on ne PEUT
 *     pas énumérer « la partie déchargée » du réseau, elle n'est plus dans la grille.
 *   - Si le déchargement coupe le Redirector du contrôleur/courant, node.isActive()
 *     passe false → nos gardes existantes annulent déjà.
 *   - Si seul du stockage se décharge alors que le Redirector reste alimenté, extract/
 *     insert n'opèrent que sur le stockage chargé, sans jamais voider (surplus géré).
 * Donc node.isActive() + la gestion du reliquat suffisent : pas de scan AE2 manuel.
 */
public final class ColonyLinkChunkUtil
{
    private ColonyLinkChunkUtil() {}

    // ── Présence (lecture seule) ──────────────────────────────────────────────

    /** True si le chunk contenant {@code pos} est présent en mémoire. Ne force pas le chargement. */
    public static boolean isLoaded(Level level, BlockPos pos)
    {
        return level != null && pos != null && level.hasChunkAt(pos);
    }

    /** True si TOUTES les positions sont dans des chunks présents (vide/null → true). */
    public static boolean allLoaded(Level level, Iterable<BlockPos> positions)
    {
        if (level == null) return false;
        if (positions == null) return true;
        for (BlockPos p : positions)
            if (p != null && !level.hasChunkAt(p)) return false;
        return true;
    }

    // ── Vivant (simulé/ticking) — requis pour toute écriture ──────────────────

    /**
     * True si le chunk de {@code pos} est RÉELLEMENT vivant (entity-ticking), pas juste
     * présent. C'est la garantie qu'une écriture (insertion dans un rack) sera simulée et
     * persistée. Un chargement « gelé » (non-ticking) de MineColonies renvoie ici false.
     * Côté client (impossible à déterminer), on retombe sur la présence.
     */
    public static boolean isLive(Level level, BlockPos pos)
    {
        if (level == null || pos == null) return false;
        if (!level.hasChunkAt(pos)) return false;
        if (level instanceof ServerLevel sl) return sl.isPositionEntityTicking(pos);
        return true;
    }

    /** True si TOUTES les positions sont dans des chunks vivants (vide/null → true). */
    public static boolean allLive(Level level, Iterable<BlockPos> positions)
    {
        if (level == null) return false;
        if (positions == null) return true;
        for (BlockPos p : positions)
            if (p != null && !isLive(level, p)) return false;
        return true;
    }

    // ── Gardes haut niveau (exigent un chunk VIVANT) ──────────────────────────

    /**
     * True si le warehouse (bloc hut + tous ses conteneurs/racks) est entièrement VIVANT.
     * {@code null} → false (on échoue côté sûr).
     */
    public static boolean warehouseFullyLoaded(Level level, BuildingWareHouse warehouse)
    {
        if (warehouse == null) return false;
        if (!isLive(level, warehouse.getPosition())) return false;
        return allLive(level, warehouse.getContainers());
    }

    /**
     * True si TOUS les warehouses de la colonie sont entièrement VIVANTS.
     * Une colonie sans warehouse → true (l'appelant gère l'absence séparément).
     */
    public static boolean colonyWarehousesFullyLoaded(Level level, IColony colony)
    {
        if (colony == null) return false;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!(b instanceof BuildingWareHouse warehouse)) continue;
            if (!isLive(level, warehouse.getPosition())) return false;
            if (!allLive(level, warehouse.getContainers())) return false;
        }
        return true;
    }

    /**
     * True si le bâtiment à {@code buildingPos} (bloc + conteneurs) est entièrement VIVANT.
     * Si aucun bâtiment MineColonies n'existe à cette position (inventaire direct,
     * non-MineColonies), on vérifie simplement que le bloc est vivant.
     */
    public static boolean buildingFullyLoaded(Level level, IColony colony, BlockPos buildingPos)
    {
        if (buildingPos == null) return false;
        if (colony == null) return isLive(level, buildingPos);
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
        {
            if (!b.getPosition().equals(buildingPos)) continue;
            if (!isLive(level, b.getPosition())) return false;
            return allLive(level, b.getContainers());
        }
        return isLive(level, buildingPos);
    }
}