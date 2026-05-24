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
 * WarehouseLinkTerminalMenu — v2.0.0
 *
 * Layout calqué pixel-par-pixel sur warehouse_terminalcolony.png (420×432 px).
 *
 * Slots :
 *   0        Warehouse Link Card  (x=202, y=2)
 *   1–9      Grille craft 3×3    (x=147+c*18, y=273+r*18)
 *   10       Résultat craft       (x=254, y=290)
 *   11–37    Inventaire 3×9       (x=130+c*18, y=348+r*18)
 *   38–46    Hotbar               (x=130+c*18, y=406)
 *
 * Coords de slot = coin intérieur (+1 par rapport au coin du cadre).
 */
public class WarehouseLinkTerminalMenu extends AbstractContainerMenu
{
    // ── Source unique de vérité — dérivée du PNG 420×432 ─────────────────────

    // Taille globale du GUI
    static final int GUI_W = 420;
    static final int GUI_H = 432;

    // Panels items (render only — pas de slots vanilla ici)
    static final int X_ITEMS_WH  = 8;    // coin haut-gauche du 1er slot WH
    static final int X_ITEMS_AE  = 233;  // coin haut-gauche du 1er slot AE
    static final int Y_ITEMS     = 31;   // y du 1er slot (WH et AE identiques)
    static final int SLOT_SZ     = 16;   // 16×16 px par slot
    static final int SLOT_PITCH  = 18;   // 16 + 2px d'écartement
    static final int PANEL_COLS  = 9;
    static final int PANEL_ROWS  = 11;

    // Slot card WH (dans la colonne centrale) — décalé -1x, -1y vs v2.0.0
    static final int CARD_SLOT_X = 201;  // x201..216
    static final int CARD_SLOT_Y = 1;    // y1..16

    // Grille craft 3×3
    static final int CRAFT_GRID_X = 147;  // coin haut-gauche slot (0,0)
    static final int CRAFT_GRID_Y = 273;
    // cols : 147, 165, 183  (CRAFT_GRID_X + c*18)
    // rows : 273, 291, 309  (CRAFT_GRID_Y + r*18)

    // Slot output craft — x255..270, y291..306 (16×16)
    static final int CRAFT_OUT_X = 255;  // intérieur = +1 → 256
    static final int CRAFT_OUT_Y = 291;  // intérieur = +1 → 292

    // Inventaire joueur
    static final int INV_X    = 130;  // intérieur = +1 → 131
    static final int INV_Y    = 347;  // intérieur = +1 → 348
    // rows : 347, 365, 383  (INV_Y + r*18)
    static final int HOTBAR_Y = 405;  // intérieur = +1 → 406

    // Search bars (render only)
    static final int SEARCH_WH_X = 9;
    static final int SEARCH_WH_W = 124;  // x9..133
    static final int SEARCH_AE_X = 233;
    static final int SEARCH_AE_W = 125;  // x233..358
    static final int SEARCH_Y    = 16;   // +1 vs v2.0.0
    static final int SEARCH_H    = 9;

    // Scrollbars verticales (render only)
    static final int SCROLL_WH_X  = 176;
    static final int SCROLL_WH_W  = 9;   // x176..185
    static final int SCROLL_AE_X  = 401;
    static final int SCROLL_AE_W  = 9;   // x401..410
    static final int SCROLL_TOP_Y = 33;  // y haut du thumb (position initiale)
    static final int SCROLL_BOT_Y = 227; // y bas du thumb (fin de course)

    // Slider horizontal WH/ME priority (render only) — un seul checkbox 22×12 centré
    static final int SLIDER_X = 189;
    static final int SLIDER_Y = 241;
    static final int SLIDER_W = 40;
    static final int SLIDER_H = 13;

    // Boutons push centre colonne (render only)
    static final int BTN1_X  = 204;   // WH ↔ ME
    static final int BTN1_Y  = 45;
    static final int BTN2_X  = 204;   // → Inventaire
    static final int BTN2_Y  = 62;
    static final int BTN_SZ  = 12;    // 12×12 px

    // Indices de slots
    public static final int CARD_SLOT_INDEX     = 0;
    public static final int CRAFT_GRID_START    = 1;
    public static final int CRAFT_OUTPUT_SLOT   = 10;
    public static final int PLAYER_INV_START    = 11;
    public static final int PLAYER_HOTBAR_START = 38;

    // Slider partagé avec Screen
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

        // Slot 0 — Warehouse Link Card (coin intérieur = +1 sur xy)
        this.addSlot(new SlotItemHandler(
                part != null ? part.getWarehouseCardSlot()
                        : new net.neoforged.neoforge.items.ItemStackHandler(1),
                0,
                CARD_SLOT_X + 1,   // 202
                CARD_SLOT_Y + 1)   // 2
        {
            @Override public boolean mayPlace(ItemStack s)
            { return s.getItem() instanceof WarehouseLinkCard; }
        });

        // Slots 1-9 — Grille craft 3×3
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(craftingGrid, r * 3 + c,
                        CRAFT_GRID_X + c * SLOT_PITCH + 1,  // 148, 166, 184
                        CRAFT_GRID_Y + r * SLOT_PITCH + 1)); // 274, 292, 310

        // Slot 10 — Output craft
        this.addSlot(new ResultSlot(inv.player, craftingGrid, craftResult, 0,
                CRAFT_OUT_X + 1,   // 256
                CRAFT_OUT_Y + 1)   // 292
        {
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
                        INV_X + c * SLOT_PITCH + 1,    // 131 + c*18
                        INV_Y + r * SLOT_PITCH + 1));  // 348 + r*18

        // Slots 38-46 — Hotbar
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv,
                    c,
                    INV_X + c * SLOT_PITCH + 1,  // 131 + c*18
                    HOTBAR_Y + 1));               // 406
    }

    // ── Craft ─────────────────────────────────────────────────────────────────

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

    // ── Shift-clic / quickMove ────────────────────────────────────────────────

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
        else  // inventaire / hotbar
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