package com.colonylink.colonylink;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
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
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

/**
 * Menu — Warehouse Link Terminal v1.3.0 (fonctionnel).
 *
 * Slots :
 *   0        Warehouse Link Card
 *   1–9      Grille craft 3×3
 *   10       Résultat craft
 *   11–37    Inventaire joueur 3×9
 *   38–46    Hotbar joueur
 *
 * Craft :
 *   - Clic gauche sur slot 10  → PICKUP vanilla → onTake() → ingrédients consommés,
 *     résultat mis dans le curseur vanilla, slotsChanged() recalcule le prochain craft ← OK
 *   - Shift-clic sur slot 10   → quickMoveStack() : boucle craft max (craft jusqu'à
 *     épuisement des ingrédients ou inventaire plein), tout va dans l'inventaire
 *
 * Pick system (v1.3.0) :
 *   - Clic gauche/droit panel WH/ME → PICK_FROM_WH / PICK_FROM_ME packet
 *     Le serveur extrait l'item et le met dans setCarried() (curseur vanilla réel)
 *   - Deposit : vanilla (clic sur n'importe quel slot, y compris craft grid)
 *   - Plus de faux pickedStack côté client
 */
public class WarehouseLinkTerminalMenu extends AbstractContainerMenu
{
    // ── Layout (source unique de vérité pour Screen) ──────────────────────────

    static final int SLOT        = 18;
    static final int PANEL_COLS  = 9;
    static final int PANEL_W     = PANEL_COLS * SLOT;   // 162
    static final int SCROLL_W    = 8;
    static final int CTR_W       = 24;
    static final int GUI_W       = PANEL_W + SCROLL_W + CTR_W + SCROLL_W + PANEL_W; // 364
    static final int PANEL_ROWS  = 7;

    static final int H_HEADER      = 28;
    static final int ROWS_H        = PANEL_ROWS * SLOT;  // 126
    static final int H_CRAFT_LABEL = 14;
    static final int H_CRAFT_PAD   = 8;
    static final int H_CRAFT       = 3 * SLOT;           // 54
    static final int H_CRAFT_SEP   = 18;                 // 18 — loge les boutons craft 14px + marge
    static final int H_INV_LABEL   = 12;
    static final int H_INV         = 3 * SLOT;           // 54
    static final int H_INV_SEP     = 4;
    static final int H_HOTBAR      = SLOT;
    static final int H_BOTTOM_PAD  = 8;

    static final int GUI_H = H_HEADER + ROWS_H
            + H_CRAFT_LABEL + H_CRAFT_PAD + H_CRAFT
            + H_CRAFT_SEP + H_INV_LABEL + H_INV + H_INV_SEP + H_HOTBAR
            + H_BOTTOM_PAD;  // 338

    static final int X_WH        = 0;
    static final int X_SCROLL_WH = PANEL_W;
    static final int X_CTR       = PANEL_W + SCROLL_W;         // 170
    static final int X_SCROLL_ME = X_CTR + CTR_W;              // 194
    static final int X_ME        = X_CTR + CTR_W + SCROLL_W;   // 202

    static final int Y_ROWS        = H_HEADER;
    static final int Y_CRAFT_LABEL = H_HEADER + ROWS_H;                              // 154
    static final int Y_CRAFT_TOP   = Y_CRAFT_LABEL + H_CRAFT_LABEL + H_CRAFT_PAD;   // 176
    static final int Y_INV_LABEL   = Y_CRAFT_TOP + H_CRAFT + H_CRAFT_SEP;           // 248
    static final int Y_INV         = Y_INV_LABEL + H_INV_LABEL;                     // 260
    static final int Y_HOTBAR      = Y_INV + H_INV + H_INV_SEP;                     // 318

    // Craft grid centré
    static final int CRAFT_BLOCK_W = 3 * SLOT + 18 + SLOT;     // 90
    static final int CRAFT_GRID_X  = GUI_W / 2 - CRAFT_BLOCK_W / 2;   // 137
    static final int CRAFT_OUT_X   = CRAFT_GRID_X + 3 * SLOT + 18;    // 209

    // Boutons craft — 2 boutons centrés sous la grille (Option A)
    static final int BTN_W        = 16;
    static final int BTN_H        = 14;
    static final int BTN_GAP      = 6;

    private static final int CRAFT_ZONE_CENTER = (CRAFT_GRID_X + CRAFT_OUT_X + SLOT) / 2; // 182

    static final int BTN_STORAGE_X = CRAFT_ZONE_CENTER - BTN_GAP / 2 - BTN_W;  // 163
    static final int BTN_INV_X     = CRAFT_ZONE_CENTER + BTN_GAP / 2;           // 185
    static final int BTN_CRAFT_Y   = Y_CRAFT_TOP + H_CRAFT + (H_CRAFT_SEP - BTN_H) / 2; // 232

    // Bloc bas
    static final int BLOC_BAS_W   = 190;
    static final int BLOC_BAS_X   = GUI_W / 2 - BLOC_BAS_W / 2;  // 87

    // Inventaire centré
    static final int INV_LEFT = GUI_W / 2 - (9 * SLOT) / 2;  // 101

    // ── Indices slots ─────────────────────────────────────────────────────────

    public static final int CARD_SLOT_INDEX     = 0;
    public static final int CRAFT_GRID_START    = 1;
    public static final int CRAFT_OUTPUT_SLOT   = 10;
    public static final int PLAYER_INV_START    = 11;
    public static final int PLAYER_HOTBAR_START = 38;

    // ── Slider partagé avec le Screen ─────────────────────────────────────────
    public static volatile boolean warehouseFirst = true;

    // ── Champs ────────────────────────────────────────────────────────────────

    private final WarehouseLinkTerminalPart  part;
    private final Player                     player;
    private final TransientCraftingContainer craftingGrid;
    private final ResultContainer            craftResult = new ResultContainer();

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

        // Slot 0 : Warehouse Link Card
        this.addSlot(new SlotItemHandler(
                part != null ? part.getWarehouseCardSlot()
                        : new net.neoforged.neoforge.items.ItemStackHandler(1),
                0, X_CTR + (CTR_W - 16) / 2, (H_HEADER - 16) / 2)
        {
            @Override public boolean mayPlace(ItemStack s)
            { return s.getItem() instanceof WarehouseLinkCard; }
        });

        // Slots 1-9 : Grille craft
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(craftingGrid, r*3+c,
                        CRAFT_GRID_X + c*SLOT + 1,
                        Y_CRAFT_TOP  + r*SLOT + 1));

        // Slot 10 : Output craft
        this.addSlot(new ResultSlot(inv.player, craftingGrid, craftResult, 0,
                CRAFT_OUT_X + 1, Y_CRAFT_TOP + SLOT + 1)
        {
            @Override public boolean mayPlace(ItemStack s) { return false; }

            @Override public void onTake(Player p, ItemStack s)
            {
                super.onTake(p, s);
                updateCraftResult();
            }
        });

        // Slots 11-37 : Inventaire joueur
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(inv, c + r*9 + 9, INV_LEFT + c*SLOT + 1, Y_INV + r*SLOT + 1));

        // Slots 38-46 : Hotbar
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv, c, INV_LEFT + c*SLOT + 1, Y_HOTBAR + 1));
    }

    // ── Craft + sync card ────────────────────────────────────────────────────

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

    @Override
    public boolean stillValid(Player p)
    {
        return part != null && part.getHostBlockEntity() != null
                && !part.getHostBlockEntity().isRemoved();
    }

    // ── Shift-clic / MouseTweaks ──────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player p, int index)
    {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack ss = slot.getItem();
        ItemStack rs = ss.copy();

        int craftEnd = CRAFT_OUTPUT_SLOT + 1;
        int invEnd   = PLAYER_HOTBAR_START + 9;

        // ── Slot 0 : card → inventaire joueur uniquement ─────────────────────
        if (index == CARD_SLOT_INDEX)
        {
            if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false))
                if (!moveItemStackTo(ss, PLAYER_INV_START, PLAYER_HOTBAR_START, false))
                    return ItemStack.EMPTY;
        }
        // ── Grille craft (1-9) → inventaire joueur ────────────────────────────
        else if (index > CARD_SLOT_INDEX && index < CRAFT_OUTPUT_SLOT)
        {
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                return ItemStack.EMPTY;
        }
        // ── Output craft (10) : shift-clic = craft MAX ────────────────────────
        // Boucle : prend le résultat, le met dans l'inventaire, recommence
        // jusqu'à épuisement des ingrédients ou inventaire plein.
        else if (index == CRAFT_OUTPUT_SLOT)
        {
            if (!p.level().isClientSide())
            {
                // Craft au moins 1 fois (le premier résultat est déjà dans ss)
                int crafted = 0;
                while (true)
                {
                    ItemStack result = craftResult.getItem(0);
                    if (result.isEmpty()) break;

                    ItemStack toMove = result.copy();
                    if (!moveItemStackTo(toMove, PLAYER_INV_START, invEnd, true))
                        break; // inventaire plein

                    // Consommer les ingrédients
                    for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                    {
                        ItemStack ing = craftingGrid.getItem(i);
                        if (!ing.isEmpty()) ing.shrink(1);
                    }
                    craftResult.setItem(0, ItemStack.EMPTY);
                    slotsChanged(craftingGrid); // recalcule le prochain résultat
                    crafted++;

                    // Sécurité : max 64 itérations (1 stack max)
                    if (crafted >= ss.getMaxStackSize()) break;
                }
                if (crafted == 0) return ItemStack.EMPTY;
                // ss est maintenant vide (le slot output a été vidé dans la boucle)
                slot.set(ItemStack.EMPTY);
                return rs;
            }
            else
            {
                // Côté client : moveItemStackTo normal (feedback visuel)
                if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                    return ItemStack.EMPTY;
            }
        }
        // ── Inventaire / hotbar (11-46) ───────────────────────────────────────
        else
        {
            if (ss.getItem() instanceof WarehouseLinkCard)
            {
                if (!moveItemStackTo(ss, CARD_SLOT_INDEX, CRAFT_GRID_START, false))
                    return ItemStack.EMPTY;
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

        if (ss.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return rs;
    }

    // ── Fermeture ─────────────────────────────────────────────────────────────

    @Override
    public void removed(Player p)
    {
        super.removed(p);
        if (!p.level().isClientSide())
        {
            for (int i = 0; i < craftingGrid.getContainerSize(); i++)
            {
                ItemStack s = craftingGrid.getItem(i);
                if (!s.isEmpty()) p.getInventory().placeItemBackInInventory(s);
            }
            if (part != null && p instanceof net.minecraft.server.level.ServerPlayer sp)
                part.onGuiClosed(sp);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public WarehouseLinkTerminalPart  getPart()         { return part; }
    public TransientCraftingContainer getCraftingGrid() { return craftingGrid; }
    public ResultContainer            getCraftResult()  { return craftResult; }
}