/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.CustomData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.misc;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public class MissingContentItem
extends Item {
    public MissingContentItem(Item.Properties properties) {
        super(properties);
    }

    @Nullable
    public BrokenStackInfo getBrokenStackInfo(ItemStack stack) {
        CustomData itemStackData = (CustomData)stack.get(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA);
        CustomData genericStackData = (CustomData)stack.get(AEComponents.MISSING_CONTENT_AEKEY_DATA);
        if (itemStackData != null && itemStackData.contains("id")) {
            long amount;
            CompoundTag brokenDataTag = itemStackData.getUnsafe();
            if (!brokenDataTag.contains("id", 8)) {
                return null;
            }
            String missingId = brokenDataTag.getString("id");
            try {
                amount = Math.max(1L, brokenDataTag.getLong("count"));
            }
            catch (Exception ignored) {
                amount = 1L;
            }
            return new BrokenStackInfo((Component)Component.literal((String)missingId), AEKeyType.items(), amount);
        }
        if (genericStackData != null && genericStackData.contains("id")) {
            long amount;
            CompoundTag brokenDataTag = genericStackData.getUnsafe();
            if (!brokenDataTag.contains("id", 8)) {
                return null;
            }
            MutableComponent missingId = Component.literal((String)brokenDataTag.getString("id"));
            AEKeyType keyType = null;
            try {
                String keyTypeString = brokenDataTag.getString("#t");
                keyType = (AEKeyType)AEKeyTypesInternal.getRegistry().get(ResourceLocation.parse((String)keyTypeString));
                if (keyType == null) {
                    missingId.append(" (").append(keyTypeString).append(")");
                }
            }
            catch (Exception keyTypeString) {
                // empty catch block
            }
            try {
                amount = Math.max(1L, brokenDataTag.getLong("#"));
            }
            catch (Exception ignored) {
                amount = 1L;
            }
            return new BrokenStackInfo((Component)missingId, keyType, amount);
        }
        return null;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advanced) {
        super.appendHoverText(stack, context, lines, advanced);
        String error = (String)stack.get(AEComponents.MISSING_CONTENT_ERROR);
        if (error != null) {
            lines.add((Component)Component.literal((String)error).withStyle(ChatFormatting.GRAY));
        }
    }

    public record BrokenStackInfo(Component displayName, @Nullable AEKeyType keyType, long amount) {
    }
}

