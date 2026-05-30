package com.colonylink.colonylink;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

/**
 * WarehouseLinkTerminalMenu — v1.4.2
 *
 * Layout des slots :
 *   0        Warehouse Link Card  (x=201, y=1)
 *   1–9      Grille craft 3×3    (x=147+c*18, y=273+r*18)
 *   10       Résultat craft       (x=255, y=291)
 *   11–37    Inventaire 3×9       (x=130+c*18, y=347+r*18)
 *   38–46    Hotbar               (x=130+c*18, y=405)
 *   47       Domum Target         (x=139, y=283) — dans pattern_gui
 *   48       Blank Pattern input  (x=253, y=267) — x=269-16=253 relatif GUI
 *   49       DomumPattern output  (x=253, y=314) — read-only
 *
 * Pattern AE2 pour masquer les slots craft (onglet Cutter) :
 *   Les slots 1-10 sont déplacés à x=-2000 quand l'onglet Cutter est actif.
 *   Le Screen appelle setTab() pour switcher.
 *
 * Coordonnées slots : relatives au GUI (leftPos/topPos ajoutés par vanilla).
 * Les coords données par Morph sont absolues dans le GUI → on les utilise telles quelles.
 */
public class WarehouseLinkTerminalMenu extends AbstractContainerMenu
{
    // ── Coordonnées GUI (inchangées v2.0.0) ──────────────────────────────────

    static final int GUI_W = 420;
    static final int GUI_H = 432;

    static final int X_ITEMS_WH  = 8;
    static final int X_ITEMS_AE  = 233;
    static final int Y_ITEMS     = 31;
    static final int SLOT_SZ     = 16;
    static final int SLOT_PITCH  = 18;
    static final int PANEL_COLS  = 9;
    static final int PANEL_ROWS  = 11;

    static final int CARD_SLOT_X = 201;
    static final int CARD_SLOT_Y = 1;

    static final int CRAFT_GRID_X = 147;
    static final int CRAFT_GRID_Y = 273;

    static final int CRAFT_OUT_X = 255;
    static final int CRAFT_OUT_Y = 291;

    static final int INV_X    = 130;
    static final int INV_Y    = 347;
    static final int HOTBAR_Y = 405;

    static final int SEARCH_WH_X = 9;
    static final int SEARCH_WH_W = 124;
    static final int SEARCH_AE_X = 233;
    static final int SEARCH_AE_W = 125;
    static final int SEARCH_Y    = 16;
    static final int SEARCH_H    = 9;

    static final int SCROLL_WH_X  = 176;
    static final int SCROLL_WH_W  = 9;
    static final int SCROLL_AE_X  = 401;
    static final int SCROLL_AE_W  = 9;
    static final int SCROLL_TOP_Y = 33;
    static final int SCROLL_BOT_Y = 227;

    static final int SLIDER_X = 189;
    static final int SLIDER_Y = 241;
    static final int SLIDER_W = 40;
    static final int SLIDER_H = 13;

    static final int BTN1_X = 204;
    static final int BTN1_Y = 45;
    static final int BTN2_X = 204;
    static final int BTN2_Y = 62;
    static final int BTN_SZ = 12;

    // ── v1.4.2 — Coordonnées Domum Pattern GUI ────────────────────────────────
    // pattern_gui.png en sur-impression à x=129, y=266 (166×68)
    // Toutes les coords sont relatives au GUI (0,0 = coin haut-gauche du fond PNG)

    /** Slot Domum Target — zone libre gauche de pattern_gui */
    static final int DOMUM_TARGET_X = 139;  // dans pattern_gui, zone gauche
    static final int DOMUM_TARGET_Y = 283;  // centré verticalement (266+17=283)

    /** Slot Blank Pattern input — x=269, y=267 (donné par Morph, coords GUI absolues) */
    static final int BLANK_PATTERN_X = 269;
    static final int BLANK_PATTERN_Y = 267;

    /** Slot DomumPattern output — x=269, y=314 (donné par Morph, coords GUI absolues) */
    static final int DOMUM_OUT_X = 269;
    static final int DOMUM_OUT_Y = 314;

    // ── Indices de slots ──────────────────────────────────────────────────────

    public static final int CARD_SLOT_INDEX     = 0;
    public static final int CRAFT_GRID_START    = 1;
    public static final int CRAFT_OUTPUT_SLOT   = 10;
    public static final int PLAYER_INV_START    = 11;
    public static final int PLAYER_HOTBAR_START = 38;
    public static final int DOMUM_TARGET_SLOT   = 47;
    public static final int BLANK_PATTERN_SLOT  = 48;
    public static final int DOMUM_OUTPUT_SLOT   = 49;  // read-only

    // ── Slider partagé ────────────────────────────────────────────────────────
    public static volatile boolean warehouseFirst = true;

    // ── Champs ────────────────────────────────────────────────────────────────
    private final WarehouseLinkTerminalPart  part;
    private final Player                     player;
    private final TransientCraftingContainer craftingGrid;
    private final ResultContainer            craftResult = new ResultContainer();

    // ── Onglet actif (0=Craft, 1=Cutter) ─────────────────────────────────────
    // Partagé avec le Screen via setTab()
    private int activeTab = 0;

    // ── Conteneurs Domum ──────────────────────────────────────────────────────

    // v1.4.2 — Les slots Domum sont persistés dans le Part (pas dans le Menu).
    // Le Menu accède aux handlers du Part via part.domumTargetSlot etc.
    // Si part == null (côté client sans Part), on crée des handlers locaux temporaires.
    final ItemStackHandler domumTargetSlot;
    final ItemStackHandler blankPatternSlot;
    final ItemStackHandler domumOutputSlot;

    // ── Constructeurs ─────────────────────────────────────────────────────────

    public WarehouseLinkTerminalMenu(int id, Inventory inv, FriendlyByteBuf buf)
    {
        this(id, inv, resolvePartFromBuf(inv, buf));
    }

    private static WarehouseLinkTerminalPart resolvePartFromBuf(Inventory inv, FriendlyByteBuf buf)
    {
        net.minecraft.core.BlockPos pos = buf.readBlockPos();
        int sideOrd = buf.readByte() & 0xFF;
        net.minecraft.core.Direction side =
                sideOrd < net.minecraft.core.Direction.values().length
                        ? net.minecraft.core.Direction.values()[sideOrd]
                        : net.minecraft.core.Direction.NORTH;
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (!(be instanceof appeng.api.parts.IPartHost host)) return null;
        appeng.api.parts.IPart raw = host.getPart(side);
        return raw instanceof WarehouseLinkTerminalPart p ? p : null;
    }

    public WarehouseLinkTerminalMenu(int id, Inventory inv, WarehouseLinkTerminalPart part)
    {
        super(ColonyLinkRegistry.WAREHOUSE_LINK_TERMINAL_MENU_TYPE.get(), id);
        this.part         = part;
        this.player       = inv.player;
        this.craftingGrid = new TransientCraftingContainer(this, 3, 3);

        // Utilise les handlers persistants du Part si disponible
        if (part != null)
        {
            this.domumTargetSlot  = part.domumTargetSlot;
            this.blankPatternSlot = part.blankPatternSlot;
            this.domumOutputSlot  = part.domumOutputSlot;
        }
        else
        {
            this.domumTargetSlot  = new ItemStackHandler(1);
            this.blankPatternSlot = new ItemStackHandler(1);
            this.domumOutputSlot  = new ItemStackHandler(1);
        }

        // Restaure la grille craft depuis le Part
        if (part != null)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack saved = part.craftingGridSlot.getStackInSlot(i);
                if (!saved.isEmpty())
                    craftingGrid.setItem(i, saved.copy());
            }
        }

        // domumOutputSlot toujours vide à l'ouverture
        if (part != null) part.domumOutputSlot.setStackInSlot(0, ItemStack.EMPTY);

        // Slot 0 — Warehouse Link Card
        this.addSlot(new SlotItemHandler(
                part != null ? part.getWarehouseCardSlot()
                        : new ItemStackHandler(1),
                0,
                CARD_SLOT_X + 1,
                CARD_SLOT_Y + 1)
        {
            @Override public boolean mayPlace(ItemStack s)
            { return s.getItem() instanceof WarehouseLinkCard; }
        });

        // Slots 1-9 — Grille craft 3×3
        // isActive() = (activeTab==0) : invisible+non-cliquable côté client quand onglet Cutter.
        // Côté serveur isActive() n'est pas appelé pour les craft slots → aucun impact.
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(craftingGrid, r * 3 + c,
                        CRAFT_GRID_X + c * SLOT_PITCH + 1,
                        CRAFT_GRID_Y + r * SLOT_PITCH + 1)
                {
                    @Override public boolean isActive() { return activeTab == 0; }
                });

        // Slot 10 — Output craft
        this.addSlot(new ResultSlot(inv.player, craftingGrid, craftResult, 0,
                CRAFT_OUT_X + 1,
                CRAFT_OUT_Y + 1)
        {
            @Override public boolean isActive() { return activeTab == 0; }
            @Override public boolean mayPlace(ItemStack s) { return false; }

            @Override public void onTake(Player p, ItemStack s)
            {
                super.onTake(p, s);
                updateCraftResult();
            }
        });

        // Slots 11-37 — Inventaire joueur 3×9
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(inv,
                        c + r * 9 + 9,
                        INV_X + c * SLOT_PITCH + 1,
                        INV_Y + r * SLOT_PITCH + 1));

        // Slots 38-46 — Hotbar
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv,
                    c,
                    INV_X + c * SLOT_PITCH + 1,
                    HOTBAR_Y + 1));

        // ── v1.4.2 — Slots Domum Pattern ─────────────────────────────────────
        // Toujours à x=-2000 (hors-écran) → aucune collision avec les autres slots.
        // Le Screen les rend manuellement aux bonnes coords visuelles.
        // Le Screen intercepte les clics aux positions visuelles et les route vers ces slots.
        // mayPlace/mayPickup sans check d'onglet → safe côté serveur.

        // Slot 47 — Domum Target
        this.addSlot(new SlotItemHandler(domumTargetSlot, 0, -2000, -2000)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            { return stack.isEmpty() || DomumCraftHandler.isDomumItem(stack); }
        });

        // Slot 48 — Blank Pattern input
        this.addSlot(new SlotItemHandler(blankPatternSlot, 0, -2000, -2000)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                if (stack.isEmpty()) return true;
                net.minecraft.resources.ResourceLocation id =
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                return id != null && id.getPath().equals("blank_pattern");
            }
        });

        // Slot 49 — DomumPattern output (read-only pour dépôt, pickup autorisé)
        this.addSlot(new SlotItemHandler(domumOutputSlot, 0, -2000, -2000)
        {
            @Override public boolean mayPlace(ItemStack s) { return false; }
            @Override public boolean mayPickup(Player p) { return true; }
        });
    }

    // ── Gestion des onglets — pattern AE2 (x=-2000 = slot caché) ─────────────

    /**
     * Appelé par le Screen quand l'utilisateur change d'onglet.
     * Tab 0 = Crafting Table, Tab 1 = Cutter Domum.
     * Les slots masqués restent aux bonnes coordonnées mais isActive() = false,
     * ce qui empêche toute interaction vanilla (pas de drop, pas de pick).
     */
    public void setTab(int tab)
    {
        this.activeTab = tab;
    }

    public int getActiveTab() { return activeTab; }

    /**
     * Un slot Craft est actif seulement si l'onglet Craft est affiché.
     * Un slot Domum est actif seulement si l'onglet Cutter est affiché.
     * isActive() = false → le slot est invisible ET non-interactif côté vanilla.
     */
    boolean isCraftTabActive()  { return activeTab == 0; }
    boolean isCutterTabActive() { return activeTab == 1; }

    // ── Craft vanilla ─────────────────────────────────────────────────────────

    private boolean lastCardState = false;

    @Override
    public void slotsChanged(Container c)
    {
        super.slotsChanged(c);
        updateCraftResult();

        if (part != null && !player.level().isClientSide())
        {
            boolean cardNow = part.hasWarehouseCard();
            if (cardNow != lastCardState)
            {
                lastCardState = cardNow;
                part.requestImmediateSync();
            }
        }
    }

    private void updateCraftResult()
    {
        if (player.level().isClientSide()) return;
        CraftingInput input = craftingGrid.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe =
                player.level().getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, input, player.level());
        craftResult.setItem(0, recipe.isPresent()
                ? recipe.get().value().assemble(input, player.level().registryAccess())
                : ItemStack.EMPTY);
    }

    // ── Domum Pattern output preview ──────────────────────────────────────────

    private void updateDomumOutput()
    {
        if (player.level().isClientSide()) return;
        ItemStack target = domumTargetSlot.getStackInSlot(0);
        if (target.isEmpty() || !DomumCraftHandler.isDomumItem(target))
        { domumOutputSlot.setStackInSlot(0, ItemStack.EMPTY); return; }
        ItemStack preview = DomumPatternItem.encode(target, player.level().registryAccess());
        domumOutputSlot.setStackInSlot(0, preview);
    }

    /** Appelé depuis le Screen client quand le joueur clique "Encode". */
    public void sendEncodeRequest()
    {
        if (!player.level().isClientSide()) return;
        ItemStack target = domumTargetSlot.getStackInSlot(0);
        if (target.isEmpty() || !DomumCraftHandler.isDomumItem(target)) return;

        net.minecraft.core.BlockPos hostPos = net.minecraft.core.BlockPos.ZERO;
        int sideByte = 0;
        if (part != null)
        {
            var be = part.getHostBlockEntity();
            if (be != null) hostPos = be.getBlockPos();
            var side = part.getSide();
            if (side != null) sideByte = side.ordinal();
        }
        PacketDistributor.sendToServer(new DomumEncodePatternPacket(hostPos, sideByte, target.copy()));
    }

    // ── stillValid ────────────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player p)
    {
        return part != null && part.getHostBlockEntity() != null
                && !part.getHostBlockEntity().isRemoved();
    }

    // ── quickMoveStack ────────────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player p, int index)
    {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack ss = slot.getItem();
        ItemStack rs = ss.copy();

        int invEnd = PLAYER_HOTBAR_START + 9;

        if (index == CARD_SLOT_INDEX)
        {
            if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false))
                if (!moveItemStackTo(ss, PLAYER_INV_START, PLAYER_HOTBAR_START, false))
                    return ItemStack.EMPTY;
        }
        else if (index > CARD_SLOT_INDEX && index < CRAFT_OUTPUT_SLOT)
        {
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                return ItemStack.EMPTY;
        }
        else if (index == CRAFT_OUTPUT_SLOT)
        {
            if (!p.level().isClientSide())
            {
                int crafted = 0;
                while (true)
                {
                    ItemStack result = craftResult.getItem(0);
                    if (result.isEmpty()) break;
                    ItemStack toMove = result.copy();
                    if (!moveItemStackTo(toMove, PLAYER_INV_START, invEnd, true)) break;
                    for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                    {
                        ItemStack ing = craftingGrid.getItem(i);
                        if (!ing.isEmpty()) ing.shrink(1);
                    }
                    craftResult.setItem(0, ItemStack.EMPTY);
                    slotsChanged(craftingGrid);
                    crafted++;
                    if (crafted >= ss.getMaxStackSize()) break;
                }
                if (crafted == 0) return ItemStack.EMPTY;
                slot.set(ItemStack.EMPTY);
                return rs;
            }
            else
            {
                if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                    return ItemStack.EMPTY;
            }
        }
        else if (index == DOMUM_TARGET_SLOT)
        {
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                return ItemStack.EMPTY;
        }
        else if (index == BLANK_PATTERN_SLOT)
        {
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                return ItemStack.EMPTY;
        }
        else if (index == DOMUM_OUTPUT_SLOT)
        {
            // Point 1 : le slot output est read-only pour le dépôt
            // mais on peut le prendre via shift-clic → va dans l'inventaire
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false))
                    return ItemStack.EMPTY;
        }
        else // inventaire / hotbar
        {
            if (ss.getItem() instanceof WarehouseLinkCard)
            {
                if (!moveItemStackTo(ss, CARD_SLOT_INDEX, CRAFT_GRID_START, false))
                    return ItemStack.EMPTY;
            }
            else
            {
                net.minecraft.resources.ResourceLocation id =
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(ss.getItem());
                boolean isBlank = id != null && id.getNamespace().equals("ae2")
                        && id.getPath().equals("blank_pattern");
                boolean isDomum = DomumCraftHandler.isDomumItem(ss);

                // Si onglet Cutter actif → proposer les slots Domum en priorité
                if (activeTab == 1)
                {
                    if (isDomum)
                    {
                        if (!moveItemStackTo(ss, DOMUM_TARGET_SLOT, DOMUM_TARGET_SLOT + 1, false))
                            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                                return ItemStack.EMPTY;
                    }
                    else if (isBlank)
                    {
                        if (!moveItemStackTo(ss, BLANK_PATTERN_SLOT, BLANK_PATTERN_SLOT + 1, false))
                            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                                return ItemStack.EMPTY;
                    }
                    else
                    {
                        if (index < PLAYER_HOTBAR_START)
                        { if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false)) return ItemStack.EMPTY; }
                        else
                        { if (!moveItemStackTo(ss, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) return ItemStack.EMPTY; }
                    }
                }
                else
                {
                    if (p.level().isClientSide())
                    {
                        ItemStack stackToSend = ss.copy();
                        TerminalTransferPacket.Direction dir = warehouseFirst
                                ? TerminalTransferPacket.Direction.INV_TO_WH
                                : TerminalTransferPacket.Direction.INV_TO_ME;
                        net.minecraft.core.BlockPos hostPos = net.minecraft.core.BlockPos.ZERO;
                        int sideByte = 0;
                        if (part != null)
                        {
                            var be = part.getHostBlockEntity();
                            if (be != null) hostPos = be.getBlockPos();
                            var side = part.getSide();
                            if (side != null) sideByte = side.ordinal();
                        }
                        PacketDistributor.sendToServer(new TerminalTransferPacket(
                                stackToSend, stackToSend.getCount(), dir, hostPos, sideByte));
                        slot.set(ItemStack.EMPTY);
                        return rs;
                    }
                    else
                    {
                        if (index < PLAYER_HOTBAR_START)
                        { if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false)) return ItemStack.EMPTY; }
                        else
                        { if (!moveItemStackTo(ss, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) return ItemStack.EMPTY; }
                    }
                }
            }
        }

        if (ss.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return rs;
    }

    // ── removed ───────────────────────────────────────────────────────────────

    @Override
    public void removed(Player p)
    {
        super.removed(p);
        if (!p.level().isClientSide())
        {
            // Slot 49 output : si un pattern encodé non pris → le rendre au joueur
            if (part != null)
            {
                ItemStack encoded = part.domumOutputSlot.getStackInSlot(0);
                if (!encoded.isEmpty())
                {
                    p.getInventory().placeItemBackInInventory(encoded);
                    part.domumOutputSlot.setStackInSlot(0, ItemStack.EMPTY);
                }
            }

            // Sauvegarde la grille craft dans le Part (persistance)
            if (part != null)
            {
                for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                    part.craftingGridSlot.setStackInSlot(i, craftingGrid.getItem(i).copy());
            }
            else
            {
                // Pas de Part : rend les items au joueur
                for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                {
                    ItemStack s = craftingGrid.getItem(i);
                    if (!s.isEmpty()) p.getInventory().placeItemBackInInventory(s);
                }
            }
            if (part != null && p instanceof net.minecraft.server.level.ServerPlayer sp)
                part.onGuiClosed(sp);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public WarehouseLinkTerminalPart  getPart()         { return part; }
    public TransientCraftingContainer getCraftingGrid() { return craftingGrid; }
    public ResultContainer            getCraftResult()  { return craftResult; }
    public ItemStack getDomumTarget() { return domumTargetSlot.getStackInSlot(0); }
    public ItemStack getDomumOutput() { return domumOutputSlot.getStackInSlot(0); }

    /** Retourne la position du bloc AE2 qui héberge ce Part (pour les packets). */
    public net.minecraft.core.BlockPos getHostPos()
    {
        if (part == null) return net.minecraft.core.BlockPos.ZERO;
        net.minecraft.world.level.block.entity.BlockEntity be = part.getHostBlockEntity();
        return be != null ? be.getBlockPos() : net.minecraft.core.BlockPos.ZERO;
    }

    /** Retourne le côté du Part (ordinal de Direction) pour les packets. */
    public int getHostSide()
    {
        if (part == null) return 0;
        net.minecraft.core.Direction side = part.getSide() != null
                ? part.getSide().getOpposite() : net.minecraft.core.Direction.NORTH;
        return side.ordinal();
    }

    public boolean canEncode()
    {
        return DomumCraftHandler.isDomumItem(domumTargetSlot.getStackInSlot(0))
                && !blankPatternSlot.getStackInSlot(0).isEmpty();
    }
}