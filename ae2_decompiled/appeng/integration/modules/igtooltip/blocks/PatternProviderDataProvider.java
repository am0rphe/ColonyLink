/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.client.AEKeyRendering;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.InGameTooltip;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public final class PatternProviderDataProvider
implements BodyProvider<PatternProviderLogicHost>,
ServerDataProvider<PatternProviderLogicHost> {
    private static final String NBT_LOCK_REASON = "craftingLockReason";
    private static final String NBT_LOCK_UNTIL_RESULT_STACK = "craftingLockUntilResultStack";

    @Override
    public void buildTooltip(PatternProviderLogicHost host, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag stack;
        String lockReason = context.serverData().getString(NBT_LOCK_REASON);
        if (!lockReason.isEmpty()) {
            tooltip.addLine((Component)Component.Serializer.fromJson((String)lockReason, (HolderLookup.Provider)context.registries()));
        }
        if (!(stack = context.serverData().getCompound(NBT_LOCK_UNTIL_RESULT_STACK)).isEmpty()) {
            MutableComponent stackAmount;
            MutableComponent stackName;
            GenericStack genericStack = GenericStack.readTag(context.registries(), stack);
            if (genericStack == null) {
                stackName = Component.literal((String)"ERROR");
                stackAmount = Component.literal((String)"ERROR");
            } else {
                stackName = AEKeyRendering.getDisplayName(genericStack.what());
                stackAmount = Component.literal((String)genericStack.what().formatAmount(genericStack.amount(), AmountFormat.FULL));
            }
            tooltip.addLine((Component)InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void provideServerData(Player player, PatternProviderLogicHost host, CompoundTag serverData) {
        PatternProviderLogic logic = host.getLogic();
        MutableComponent reason = null;
        switch (logic.getCraftingLockedReason()) {
            case LOCK_UNTIL_PULSE: {
                reason = InGameTooltip.CraftingLockedUntilPulse.text();
                break;
            }
            case LOCK_WHILE_HIGH: {
                reason = InGameTooltip.CraftingLockedByRedstoneSignal.text();
                break;
            }
            case LOCK_WHILE_LOW: {
                reason = InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
                break;
            }
            case LOCK_UNTIL_RESULT: {
                GenericStack stack = logic.getUnlockStack();
                if (stack != null) {
                    serverData.put(NBT_LOCK_UNTIL_RESULT_STACK, (Tag)GenericStack.writeTag((HolderLookup.Provider)player.registryAccess(), stack));
                } else {
                    CompoundTag errorDummy = new CompoundTag();
                    errorDummy.putString("error", "error");
                    serverData.put(NBT_LOCK_UNTIL_RESULT_STACK, (Tag)errorDummy);
                }
                return;
            }
        }
        if (reason != null) {
            serverData.putString(NBT_LOCK_REASON, Component.Serializer.toJson((Component)reason.copy().withStyle(ChatFormatting.RED), (HolderLookup.Provider)player.registryAccess()));
        }
    }
}

