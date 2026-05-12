package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

// ════════════════════════════════════════════════════════════════════════════
// GuiStatePacketRS — Ouvre/ferme le GUI de la wand RS2
// ════════════════════════════════════════════════════════════════════════════
class GuiStatePacketRS implements CustomPacketPayload
{
    public final boolean open;
    public final BlockPos builderPos;
    public final int activeTabIndex;

    public GuiStatePacketRS(boolean open, BlockPos builderPos, int activeTabIndex)
    {
        this.open = open;
        this.builderPos = builderPos;
        this.activeTabIndex = activeTabIndex;
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "gui_state_rs");
    public static final CustomPacketPayload.Type<GuiStatePacketRS> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiStatePacketRS> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.open);
                buf.writeBlockPos(packet.builderPos);
                buf.writeInt(packet.activeTabIndex);
            },
            buf -> new GuiStatePacketRS(buf.readBoolean(), buf.readBlockPos(), buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GuiStatePacketRS packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            if (packet.open)
            {
                ColonyLinkServerTicker.addViewerRS(
                        serverPlayer.getUUID(), packet.builderPos, packet.activeTabIndex);
                ColonyLinkServerTicker.sendImmediateUpdateRS(
                        serverPlayer, packet.builderPos, packet.activeTabIndex);

                for (ItemStack s : serverPlayer.getInventory().items)
                {
                    if (s.getItem() instanceof ColonyLinkWandRS)
                    {
                        ColonyLinkWandRSLinkableHandler.setActiveTab(s, packet.activeTabIndex);
                        break;
                    }
                }
            }
            else
            {
                ColonyLinkServerTicker.removeViewerRS(serverPlayer.getUUID());

                if (packet.activeTabIndex == -1)
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§a[ColonyLink RS] §fSneak + right-click a Builder's Hut to pair a new builder."));
            }
        });
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SendToBuilderPacketRS — Envoie un item au builder via RS2
// ════════════════════════════════════════════════════════════════════════════
class SendToBuilderPacketRS implements CustomPacketPayload
{
    public final ItemStack stack;
    public final BlockPos builderPos;
    public final int realCount;

    public SendToBuilderPacketRS(ItemStack stack, BlockPos builderPos, int realCount)
    {
        this.stack = stack;
        this.builderPos = builderPos;
        this.realCount = realCount;
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "send_to_builder_rs");
    public static final CustomPacketPayload.Type<SendToBuilderPacketRS> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SendToBuilderPacketRS> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ItemStack.STREAM_CODEC.encode(buf, packet.stack);
                buf.writeBlockPos(packet.builderPos);
                buf.writeInt(packet.realCount);
            },
            buf -> new SendToBuilderPacketRS(
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readBlockPos(),
                    buf.readInt()
            )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SendToBuilderPacketRS packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer)
                SendToBuilderHandlerRS.handleSendToBuilder(
                        serverPlayer, packet.stack, packet.builderPos, packet.realCount);
        });
    }
}

// ════════════════════════════════════════════════════════════════════════════
// CraftRequestPacketRS — Demande de craft via RS2
// ════════════════════════════════════════════════════════════════════════════
class CraftRequestPacketRS implements CustomPacketPayload
{
    public final ItemStack stack;
    public final int realCount;
    public final boolean craftAll;
    public final List<ItemStack> allStacks;
    public final List<Integer> allCounts;

    /** Constructeur craft simple */
    public CraftRequestPacketRS(ItemStack stack, int realCount)
    {
        this.stack = stack;
        this.realCount = realCount;
        this.craftAll = false;
        this.allStacks = List.of(stack);
        this.allCounts = List.of(realCount);
    }

    /** Constructeur craft all */
    public CraftRequestPacketRS(List<ItemStack> stacks, List<Integer> counts)
    {
        this.stack = stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
        this.realCount = counts.isEmpty() ? 0 : counts.get(0);
        this.craftAll = true;
        this.allStacks = stacks;
        this.allCounts = counts;
    }

    private CraftRequestPacketRS(ItemStack stack, int realCount, boolean craftAll,
                                  List<ItemStack> allStacks, List<Integer> allCounts)
    {
        this.stack = stack;
        this.realCount = realCount;
        this.craftAll = craftAll;
        this.allStacks = allStacks;
        this.allCounts = allCounts;
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "craft_request_rs");
    public static final CustomPacketPayload.Type<CraftRequestPacketRS> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftRequestPacketRS> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.craftAll);
                buf.writeInt(packet.allStacks.size());
                for (int i = 0; i < packet.allStacks.size(); i++)
                {
                    ItemStack.STREAM_CODEC.encode(buf, packet.allStacks.get(i));
                    buf.writeInt(packet.allCounts.get(i));
                }
            },
            buf -> {
                boolean craftAll = buf.readBoolean();
                int size = buf.readInt();
                List<ItemStack> stacks = new ArrayList<>();
                List<Integer> counts = new ArrayList<>();
                for (int i = 0; i < size; i++)
                {
                    stacks.add(ItemStack.STREAM_CODEC.decode(buf));
                    counts.add(buf.readInt());
                }
                ItemStack first = stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
                int firstCount = counts.isEmpty() ? 0 : counts.get(0);
                return new CraftRequestPacketRS(first, firstCount, craftAll, stacks, counts);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CraftRequestPacketRS packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            CraftHandlerRS.handleCraftRequests(serverPlayer, packet.allStacks, packet.allCounts);
        });
    }
}

// ════════════════════════════════════════════════════════════════════════════
// RemoveBuilderPacketRS — Retire un builder de la wand RS2
// ════════════════════════════════════════════════════════════════════════════
class RemoveBuilderPacketRS implements CustomPacketPayload
{
    public final int tabIndex;

    public RemoveBuilderPacketRS(int tabIndex) { this.tabIndex = tabIndex; }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "remove_builder_rs");
    public static final CustomPacketPayload.Type<RemoveBuilderPacketRS> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveBuilderPacketRS> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.tabIndex),
            buf -> new RemoveBuilderPacketRS(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RemoveBuilderPacketRS packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ItemStack wandStack = null;
            for (ItemStack s : player.getInventory().items)
                if (s.getItem() instanceof ColonyLinkWandRS) { wandStack = s; break; }
            if (wandStack == null) return;

            int index = packet.tabIndex;
            List<BuilderEntry> entries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
            if (index < 0 || index >= entries.size()) return;

            String removedName = entries.get(index).builderName();
            ColonyLinkWandRSLinkableHandler.removeEntryAt(wandStack, index);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[ColonyLink RS] Builder §f" + removedName + " §eunlinked from RS2 wand."));

            List<BuilderEntry> updated = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
            if (updated.isEmpty())
            {
                ColonyLinkServerTicker.removeViewerRS(player.getUUID());
            }
            else
            {
                int newActive = ColonyLinkWandRSLinkableHandler.getActiveTab(wandStack);
                BuilderEntry newEntry = updated.get(newActive);
                ColonyLinkServerTicker.addViewerRS(player.getUUID(), newEntry.builderPos(), newActive);
                ColonyLinkServerTicker.sendImmediateUpdateRS(player, newEntry.builderPos(), newActive);
            }
        });
    }
}
