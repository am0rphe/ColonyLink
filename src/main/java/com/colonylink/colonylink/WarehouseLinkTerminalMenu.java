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
 * Slot layout:
 *   0        Warehouse Link Card  (x=201, y=1)
 *   1–9      3×3 craft grid       (x=147+c*18, y=273+r*18)
 *   10       Craft result         (x=255, y=291)
 *   11–37    Inventory 3×9        (x=130+c*18, y=347+r*18)
 *   38–46    Hotbar               (x=130+c*18, y=405)
 *   47       Domum Target         (x=139, y=283) — in pattern_gui
 *   48       Blank Pattern input  (x=253, y=267) — x=269-16=253 relative to GUI
 *   49       DomumPattern output  (x=253, y=314) — read-only
 *
 * AE2 trick to hide the craft slots (Cutter tab): slots 1-10 move to x=-2000 when the
 * Cutter tab is active; the Screen calls setTab() to switch.
 *
 * Slot coords are relative to the GUI (vanilla adds leftPos/topPos). Morph's coords are
 * absolute within the GUI, so they are used as-is.
 */
public class WarehouseLinkTerminalMenu extends AbstractContainerMenu
{
    // GUI coordinates (unchanged v2.0.0)

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

    // v1.4.2 — Domum Pattern GUI coordinates
    // pattern_gui.png overlaid at x=129, y=266 (166×68)
    // All coords relative to the GUI (0,0 = top-left corner of the background PNG)

    /** Domum Target slot — free left area of pattern_gui */
    static final int DOMUM_TARGET_X = 139;  // in pattern_gui, left area
    static final int DOMUM_TARGET_Y = 283;  // vertically centred (266+17=283)

    /** Blank Pattern input slot — x=269, y=267 (Morph's values, absolute GUI coords) */
    static final int BLANK_PATTERN_X = 269;
    static final int BLANK_PATTERN_Y = 267;

    /** DomumPattern output slot — x=269, y=314 (Morph's values, absolute GUI coords) */
    static final int DOMUM_OUT_X = 269;
    static final int DOMUM_OUT_Y = 314;

    // Slot indices

    public static final int CARD_SLOT_INDEX     = 0;
    public static final int CRAFT_GRID_START    = 1;
    public static final int CRAFT_OUTPUT_SLOT   = 10;
    public static final int PLAYER_INV_START    = 11;
    public static final int PLAYER_HOTBAR_START = 38;
    public static final int DOMUM_TARGET_SLOT   = 47;
    public static final int BLANK_PATTERN_SLOT  = 48;
    public static final int DOMUM_OUTPUT_SLOT   = 49;  // read-only

    // Shared slider
    public static volatile boolean warehouseFirst = true;

    // Fields
    private final WarehouseLinkTerminalPart  part;
    private final Player                     player;
    private final TransientCraftingContainer craftingGrid;
    private final ResultContainer            craftResult = new ResultContainer();

    // Active tab (0=Craft, 1=Cutter)
    // Shared with the Screen via setTab()
    private int activeTab = 0;

    // Domum containers

    // v1.4.2 — Domum slots are persisted in the Part (not the Menu). The Menu accesses the
    // Part's handlers via part.domumTargetSlot etc. If part == null (client side without a
    // Part), temporary local handlers are created.
    final ItemStackHandler domumTargetSlot;
    final ItemStackHandler blankPatternSlot;
    final ItemStackHandler domumOutputSlot;

    // Constructors

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

        // Use the Part's persistent handlers when available
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

        // Restore the craft grid from the Part
        if (part != null)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack saved = part.craftingGridSlot.getStackInSlot(i);
                if (!saved.isEmpty())
                    craftingGrid.setItem(i, saved.copy());
            }
        }

        // domumOutputSlot always empty on open
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

            // 1.7.0-beta.2 — every player insertion goes through setByPlayer: a blank
            // card is linked here, for the inserting player, to the colony where that
            // player has hut access, Warehouse closest to the PLAYER (1.7.0-beta.4: a Terminal on a
            // Sable ship sits at a far-away plot position; the player stands near it).
            @Override public void setByPlayer(ItemStack newStack, ItemStack oldStack)
            {
                if (part != null && player instanceof net.minecraft.server.level.ServerPlayer sp
                        && CardLinks.isUnlinkedCard(newStack))
                {
                    BlockEntity hostBe = part.getHostBlockEntity();
                    if (hostBe != null)
                        CardLinks.autoLink(newStack, sp, sp.blockPosition(), null);
                }
                super.setByPlayer(newStack, oldStack);
            }
        });

        // Slots 1-9 — 3×3 craft grid
        // isActive() = (activeTab==0): invisible + non-clickable client-side on the Cutter tab.
        // Server-side isActive() is not called for craft slots -> no impact.
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                this.addSlot(new Slot(craftingGrid, r * 3 + c,
                        CRAFT_GRID_X + c * SLOT_PITCH + 1,
                        CRAFT_GRID_Y + r * SLOT_PITCH + 1)
                {
                    @Override public boolean isActive() { return activeTab == 0; }
                });

        // Slot 10 — Craft output
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

        // Slots 11-37 — Player inventory 3×9
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

        // v1.4.2 — Domum Pattern slots
        // Always at x=-2000 (off-screen) -> no collision with other slots.
        // The Screen renders them manually at the right visual coords and intercepts clicks
        // at those visual positions, routing them to these slots.
        // mayPlace/mayPickup without a tab check -> server-side safe.

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

        // Slot 49 — DomumPattern output (read-only for deposit, pickup allowed)
        this.addSlot(new SlotItemHandler(domumOutputSlot, 0, -2000, -2000)
        {
            @Override public boolean mayPlace(ItemStack s) { return false; }
            @Override public boolean mayPickup(Player p) { return true; }
        });
    }

    // Tab handling — AE2 pattern (x=-2000 = hidden slot)

    /**
     * Called by the Screen when the user switches tab.
     * Tab 0 = Crafting Table, Tab 1 = Domum Cutter.
     * Hidden slots keep their coordinates but isActive() = false, which blocks all vanilla
     * interaction (no drop, no pick).
     */
    public void setTab(int tab)
    {
        this.activeTab = tab;
    }

    public int getActiveTab() { return activeTab; }

    /**
     * A Craft slot is active only when the Craft tab is shown.
     * A Domum slot is active only when the Cutter tab is shown.
     * isActive() = false -> the slot is invisible AND non-interactive in vanilla.
     */
    boolean isCraftTabActive()  { return activeTab == 0; }
    boolean isCutterTabActive() { return activeTab == 1; }

    // Vanilla craft

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

    // Domum Pattern output preview

    private void updateDomumOutput()
    {
        if (player.level().isClientSide()) return;
        ItemStack target = domumTargetSlot.getStackInSlot(0);
        if (target.isEmpty() || !DomumCraftHandler.isDomumItem(target))
        { domumOutputSlot.setStackInSlot(0, ItemStack.EMPTY); return; }
        ItemStack preview = DomumPatternItem.encode(target, player.level().registryAccess());
        domumOutputSlot.setStackInSlot(0, preview);
    }

    /** Called from the client Screen when the player clicks "Encode". */
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

    // stillValid

    @Override
    public boolean stillValid(Player p)
    {
        if (part == null) return stillValidFail(p, "part null");
        BlockEntity hostBe = part.getHostBlockEntity();
        if (hostBe == null) return stillValidFail(p, "host block entity null");
        if (hostBe.isRemoved()) return stillValidFail(p, "host block entity removed");

        // v1.6.2 — range check: the terminal closes when the player walks away.
        // Combined with resolvePart's open-menu requirement, this stops
        // packet-driven transfers on a distant or someone else's terminal.
        // 1.7.0 — vanilla reach test (honours mods that patch it, e.g. Sable ships).
        if (!p.canInteractWithBlock(hostBe.getBlockPos(), ColonyLinkRedirectorMenu.MENU_REACH_BUFFER))
            return stillValidFail(p, "out of reach (block " + hostBe.getBlockPos().toShortString()
                    + ", player eyes " + p.getEyePosition() + ")");
        return true;
    }

    /** 1.7.0-beta.4 — logs once per menu (DEBUG) why stillValid closed it; returns false. */
    private boolean loggedStillValidFail = false;

    private boolean stillValidFail(Player p, String reason)
    {
        if (!loggedStillValidFail)
        {
            loggedStillValidFail = true;
            ColonyLink.LOGGER.debug("[ColonyLink] Warehouse Link Terminal menu closed for {}: {}",
                    p.getName().getString(), reason);
        }
        return false;
    }

    // quickMoveStack

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
                    // v1.7.1 — handle craft remainders (buckets, bottles).
                    CraftingInput craftInput = craftingGrid.asCraftInput();
                    var recipeOpt = p.level().getRecipeManager()
                            .getRecipeFor(RecipeType.CRAFTING, craftInput, p.level());
                    net.minecraft.core.NonNullList<ItemStack> remainders = recipeOpt.isPresent()
                            ? recipeOpt.get().value().getRemainingItems(craftInput)
                            : net.minecraft.core.NonNullList.withSize(craftingGrid.getContainerSize(), ItemStack.EMPTY);
                    for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                    {
                        ItemStack ing = craftingGrid.getItem(i);
                        if (!ing.isEmpty()) ing.shrink(1);
                        ItemStack rem = remainders.get(i);
                        if (!rem.isEmpty())
                        {
                            if (craftingGrid.getItem(i).isEmpty())
                                craftingGrid.setItem(i, rem);
                            else if (!p.getInventory().add(rem))
                                p.drop(rem, false);
                        }
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
            // The output slot is read-only for deposit but can be taken via shift-click
            // -> goes to the inventory
            if (!moveItemStackTo(ss, PLAYER_INV_START, invEnd, true))
                if (!moveItemStackTo(ss, PLAYER_HOTBAR_START, invEnd, false))
                    return ItemStack.EMPTY;
        }
        else // inventory / hotbar
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

                // Cutter tab active -> offer the Domum slots first
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

    // removed

    @Override
    public void removed(Player p)
    {
        super.removed(p);
        if (!p.level().isClientSide())
        {
            // Slot 49 output: return any un-taken encoded pattern to the player
            if (part != null)
            {
                ItemStack encoded = part.domumOutputSlot.getStackInSlot(0);
                if (!encoded.isEmpty())
                {
                    p.getInventory().placeItemBackInInventory(encoded);
                    part.domumOutputSlot.setStackInSlot(0, ItemStack.EMPTY);
                }
            }

            // Save the craft grid into the Part (persistence)
            if (part != null)
            {
                for (int i = 0; i < craftingGrid.getContainerSize(); i++)
                    part.craftingGridSlot.setStackInSlot(i, craftingGrid.getItem(i).copy());
            }
            else
            {
                // No Part: return the items to the player
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

    // Getters

    public WarehouseLinkTerminalPart  getPart()         { return part; }
    public TransientCraftingContainer getCraftingGrid() { return craftingGrid; }
    public ResultContainer            getCraftResult()  { return craftResult; }
    public ItemStack getDomumTarget() { return domumTargetSlot.getStackInSlot(0); }
    public ItemStack getDomumOutput() { return domumOutputSlot.getStackInSlot(0); }

    /** Position of the AE2 block hosting this Part (for packets). */
    public net.minecraft.core.BlockPos getHostPos()
    {
        if (part == null) return net.minecraft.core.BlockPos.ZERO;
        net.minecraft.world.level.block.entity.BlockEntity be = part.getHostBlockEntity();
        return be != null ? be.getBlockPos() : net.minecraft.core.BlockPos.ZERO;
    }

    /** The Part's side (Direction ordinal) for packets. */
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