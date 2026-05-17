/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.pattern;

import appeng.api.crafting.EncodedPatternDecoder;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.InvalidPatternTooltipStrategy;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.misc.MissingContentItem;
import appeng.items.misc.WrappedGenericStack;
import appeng.util.InteractionUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EncodedPatternItem<T extends IPatternDetails>
extends AEBaseItem {
    private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<ItemStack, ItemStack>();
    private final EncodedPatternDecoder<T> decoder;
    @Nullable
    private final InvalidPatternTooltipStrategy invalidPatternTooltip;

    public EncodedPatternItem(Item.Properties properties, EncodedPatternDecoder<T> decoder, @Nullable InvalidPatternTooltipStrategy invalidPatternTooltip) {
        super(properties);
        this.decoder = decoder;
        this.invalidPatternTooltip = invalidPatternTooltip;
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        this.clearPattern(player.getItemInHand(hand), player);
        return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)player.getItemInHand(hand));
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.clearPattern(stack, context.getPlayer()) ? InteractionResult.sidedSuccess((boolean)context.getLevel().isClientSide()) : InteractionResult.PASS;
    }

    private boolean clearPattern(ItemStack stack, Player player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (player.getCommandSenderWorld().isClientSide()) {
                return false;
            }
            Inventory inv = player.getInventory();
            ItemStack is = AEItems.BLANK_PATTERN.stack(stack.getCount());
            if (!is.isEmpty()) {
                for (int s = 0; s < player.getInventory().getContainerSize(); ++s) {
                    if (inv.getItem(s) != stack) continue;
                    inv.setItem(s, is);
                    return true;
                }
            }
        }
        return false;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flags) {
        PatternDetailsTooltip tooltip;
        AEItemKey what = AEItemKey.of(stack);
        if (what == null) {
            return;
        }
        Level clientLevel = AppEng.instance().getClientLevel();
        if (clientLevel == null) {
            return;
        }
        try {
            IPatternDetails details = (IPatternDetails)Objects.requireNonNull(this.decoder.decode(what, clientLevel), "decoder returned null");
            tooltip = details.getTooltip(clientLevel, flags);
        }
        catch (Exception e) {
            lines.add((Component)GuiText.InvalidPattern.text().copy().withStyle(ChatFormatting.RED));
            tooltip = this.invalidPatternTooltip != null ? this.invalidPatternTooltip.getTooltip(stack, clientLevel, e, flags) : null;
        }
        if (tooltip != null) {
            MutableComponent label = Component.empty().append(tooltip.getOutputMethod()).append(": ").withStyle(ChatFormatting.GRAY);
            MutableComponent and = Component.literal((String)" ").append((Component)GuiText.And.text()).append(" ").withStyle(ChatFormatting.GRAY);
            MutableComponent with = GuiText.With.text().copy().append(": ").withStyle(ChatFormatting.GRAY);
            boolean first = true;
            for (GenericStack output : tooltip.getOutputs()) {
                lines.add((Component)Component.empty().append((Component)(first ? label : and)).append(EncodedPatternItem.getTooltipEntryLine(output)));
                first = false;
            }
            first = true;
            for (GenericStack input : tooltip.getInputs()) {
                lines.add((Component)Component.empty().append((Component)(first ? with : and)).append(EncodedPatternItem.getTooltipEntryLine(input)));
                first = false;
            }
            for (PatternDetailsTooltip.Property property : tooltip.getProperties()) {
                if (property.value() != null) {
                    lines.add((Component)Component.empty().append(property.name()).append((Component)Component.literal((String)": ").withStyle(ChatFormatting.GRAY)).append(property.value()));
                    continue;
                }
                lines.add((Component)Component.empty().withStyle(ChatFormatting.GRAY).append(property.name()));
            }
        }
    }

    protected static Component getTooltipEntryLine(GenericStack stack) {
        MissingContentItem missingContentItem;
        MissingContentItem.BrokenStackInfo brokenStackInfo;
        AEItemKey itemKey;
        AEKey aEKey = stack.what();
        if (aEKey instanceof AEItemKey && (aEKey = (itemKey = (AEItemKey)aEKey).getReadOnlyStack().getItem()) instanceof MissingContentItem && (brokenStackInfo = (missingContentItem = (MissingContentItem)((Object)aEKey)).getBrokenStackInfo(itemKey.getReadOnlyStack())) != null) {
            return EncodedPatternItem.getTooltipEntryLine((Component)brokenStackInfo.displayName().copy().withStyle(ChatFormatting.RED), brokenStackInfo.keyType(), brokenStackInfo.amount());
        }
        return EncodedPatternItem.getTooltipEntryLine(stack.what().getDisplayName(), stack.what().getType(), stack.amount());
    }

    protected static Component getTooltipEntryLine(Component displayName, @Nullable AEKeyType amountType, long amount) {
        if (amount > 0L) {
            MutableComponent amountInfo = Component.literal((String)(amountType != null ? amountType.formatAmount(amount, AmountFormat.FULL) : String.valueOf(amount)));
            return amountInfo.append((Component)Component.literal((String)" x ").withStyle(ChatFormatting.GRAY)).append(displayName);
        }
        return displayName;
    }

    public ItemStack getOutput(ItemStack item) {
        ItemStack out = SIMPLE_CACHE.get(item);
        if (out != null) {
            return out;
        }
        Level level = AppEng.instance().getClientLevel();
        if (level == null) {
            return ItemStack.EMPTY;
        }
        IPatternDetails details = this.decode(item, level);
        out = ItemStack.EMPTY;
        if (details != null) {
            GenericStack output = details.getPrimaryOutput();
            AEKey aEKey = output.what();
            if (aEKey instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)aEKey;
                out = itemKey.toStack();
            } else {
                out = WrappedGenericStack.wrap(output.what(), 0L);
            }
        }
        SIMPLE_CACHE.put(item, out);
        return out;
    }

    @Nullable
    public IPatternDetails decode(ItemStack stack, Level level) {
        if (stack.getItem() != this || level == null) {
            return null;
        }
        AEItemKey what = AEItemKey.of(stack);
        try {
            return (IPatternDetails)Objects.requireNonNull(this.decoder.decode(what, level), "decoder returned null");
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public IPatternDetails decode(AEItemKey what, Level level) {
        if (what == null) {
            return null;
        }
        try {
            return this.decoder.decode(what, level);
        }
        catch (Exception e) {
            return null;
        }
    }
}

