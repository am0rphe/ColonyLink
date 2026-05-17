/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.reporting;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.implementations.parts.IStorageMonitorPart;
import appeng.api.networking.IGrid;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.render.BlockEntityRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.util.InteractionUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMonitorPart
extends AbstractDisplayPart
implements IStorageMonitorPart {
    @Nullable
    private AEKey configuredItem;
    private long amount;
    private boolean canCraft;
    private String lastHumanReadableText;
    private boolean isLocked;
    private IStackWatcher storageWatcher;
    private IStackWatcher craftingWatcher;

    public AbstractMonitorPart(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem, requireChannel);
        this.getMainNode().addService(IStorageWatcherNode.class, new IStorageWatcherNode(){

            @Override
            public void updateWatcher(IStackWatcher newWatcher) {
                AbstractMonitorPart.this.storageWatcher = newWatcher;
                AbstractMonitorPart.this.configureWatchers();
            }

            @Override
            public void onStackChange(AEKey what, long amount) {
                if (what.equals(AbstractMonitorPart.this.configuredItem)) {
                    String humanReadableText;
                    AbstractMonitorPart.this.amount = amount;
                    String string = humanReadableText = amount == 0L && AbstractMonitorPart.this.canCraft ? "Craft" : what.formatAmount(amount, AmountFormat.SLOT);
                    if (!humanReadableText.equals(AbstractMonitorPart.this.lastHumanReadableText)) {
                        AbstractMonitorPart.this.lastHumanReadableText = humanReadableText;
                        AbstractMonitorPart.this.getHost().markForUpdate();
                    }
                }
            }
        });
        this.getMainNode().addService(ICraftingWatcherNode.class, new ICraftingWatcherNode(){

            @Override
            public void updateWatcher(IStackWatcher newWatcher) {
                AbstractMonitorPart.this.craftingWatcher = newWatcher;
                AbstractMonitorPart.this.configureWatchers();
            }

            @Override
            public void onRequestChange(AEKey what) {
            }

            @Override
            public void onCraftableChange(AEKey what) {
                AbstractMonitorPart.this.getMainNode().ifPresent(AbstractMonitorPart.this::updateReportingValue);
            }
        });
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.isLocked = data.getBoolean("isLocked");
        this.configuredItem = data.contains("configuredItem", 10) ? AEKey.fromTagGeneric(registries, data.getCompound("configuredItem")) : null;
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean("isLocked", this.isLocked);
        if (this.configuredItem != null) {
            data.put("configuredItem", (Tag)this.configuredItem.toTagGeneric(registries));
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isLocked);
        data.writeBoolean(this.configuredItem != null);
        if (this.configuredItem != null) {
            AEKey.writeKey(data, this.configuredItem);
            data.writeVarLong(this.amount);
            data.writeBoolean(this.canCraft);
        }
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean needRedraw = super.readFromStream(data);
        boolean isLocked = data.readBoolean();
        needRedraw |= this.isLocked != isLocked;
        this.isLocked = isLocked;
        if (data.readBoolean()) {
            this.configuredItem = AEKey.readKey(data);
            this.amount = data.readVarLong();
            this.canCraft = data.readBoolean();
        } else {
            this.configuredItem = null;
            this.amount = 0L;
            this.canCraft = false;
        }
        return needRedraw;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putLong("amount", this.amount);
        data.putBoolean("canCraft", this.canCraft);
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.amount = data.getLong("amount");
        this.canCraft = data.getBoolean("canCraft");
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (this.isClientSide()) {
                return true;
            }
            if (!this.getMainNode().isActive()) {
                return false;
            }
            this.isLocked = !this.isLocked;
            player.displayClientMessage((Component)(this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).text(), true);
            this.getHost().markForSave();
            this.getHost().markForUpdate();
            return true;
        }
        return super.onUseWithoutItem(player, pos);
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (!this.isLocked && !InteractionUtil.isInAlternateUseMode(player)) {
            if (this.isClientSide()) {
                return true;
            }
            if (!this.getMainNode().isActive()) {
                return false;
            }
            if (AEItemKey.matches(this.configuredItem, heldItem)) {
                GenericStack containedStack = ContainerItemStrategies.getContainedStack(heldItem);
                if (containedStack != null) {
                    this.configuredItem = containedStack.what();
                }
            } else {
                this.configuredItem = AEItemKey.of(heldItem);
            }
            this.configureWatchers();
            this.getHost().markForSave();
            this.getHost().markForUpdate();
            return true;
        }
        return super.onUseItemOn(heldItem, player, hand, pos);
    }

    protected void configureWatchers() {
        if (this.storageWatcher != null) {
            this.storageWatcher.reset();
        }
        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }
        if (this.configuredItem != null) {
            if (this.storageWatcher != null) {
                this.storageWatcher.add(this.configuredItem);
            }
            if (this.craftingWatcher != null) {
                this.craftingWatcher.add(this.configuredItem);
            }
            this.getMainNode().ifPresent(this::updateReportingValue);
        }
    }

    protected void updateReportingValue(IGrid grid) {
        this.lastHumanReadableText = null;
        if (this.configuredItem != null) {
            this.amount = grid.getStorageService().getCachedInventory().get(this.configuredItem);
            this.canCraft = grid.getCraftingService().isCraftable(this.configuredItem);
        } else {
            this.amount = 0L;
            this.canCraft = false;
        }
        this.getHost().markForUpdate();
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (!this.isActive()) {
            return;
        }
        if (this.configuredItem == null) {
            return;
        }
        poseStack.pushPose();
        BlockOrientation orientation = BlockOrientation.get(this.getSide(), this.getSpin());
        poseStack.translate(0.5, 0.5, 0.5);
        BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
        poseStack.translate(0.0, 0.05, 0.5);
        BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, this.getDisplayed(), this.amount, this.canCraft, 0.4f, -0.23f, this.getColor().contrastTextColor, this.getLevel());
        poseStack.popPose();
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    @Nullable
    public AEKey getDisplayed() {
        return this.configuredItem;
    }

    public void setConfiguredItem(@Nullable AEKey configuredItem) {
        this.configuredItem = configuredItem;
        this.getHost().markForUpdate();
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    public boolean canCraft() {
        return this.canCraft;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        this.getHost().markForUpdate();
    }

    @Override
    public boolean showNetworkInfo(UseOnContext context) {
        return false;
    }

    protected IPartModel selectModel(IPartModel off, IPartModel on, IPartModel hasChannel, IPartModel lockedOff, IPartModel lockedOn, IPartModel lockedHasChannel) {
        if (this.isActive()) {
            if (this.isLocked()) {
                return lockedHasChannel;
            }
            return hasChannel;
        }
        if (this.isPowered()) {
            if (this.isLocked()) {
                return lockedOn;
            }
            return on;
        }
        if (this.isLocked()) {
            return lockedOff;
        }
        return off;
    }
}

