/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.Clearable
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.ICustomCableConnection;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.util.SettingsFrom;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public interface IPart
extends ICustomCableConnection,
Clearable {
    public IPartItem<?> getPartItem();

    @OnlyIn(value=Dist.CLIENT)
    default public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
    }

    default public boolean requireDynamicRender() {
        return false;
    }

    default public boolean isSolid() {
        return false;
    }

    default public boolean canConnectRedstone() {
        return false;
    }

    default public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
    }

    default public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
    }

    default public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
    }

    default public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
    }

    default public int getLightLevel() {
        return 0;
    }

    default public boolean isLadder(LivingEntity entity) {
        return false;
    }

    default public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
    }

    default public void onUpdateShape(Direction side) {
    }

    default public int isProvidingStrongPower() {
        return 0;
    }

    default public int isProvidingWeakPower() {
        return 0;
    }

    default public void writeToStream(RegistryFriendlyByteBuf data) {
    }

    @ApiStatus.Experimental
    default public void writeVisualStateToNBT(CompoundTag data) {
    }

    default public boolean readFromStream(RegistryFriendlyByteBuf data) {
        return false;
    }

    @ApiStatus.Experimental
    default public void readVisualStateFromNBT(CompoundTag data) {
    }

    @Nullable
    public IGridNode getGridNode();

    default public void onEntityCollision(Entity entity) {
    }

    default public void removeFromWorld() {
    }

    default public void addToWorld() {
    }

    @Nullable
    default public IGridNode getExternalFacingNode() {
        return null;
    }

    default public AECableType getExternalCableConnectionType() {
        return AECableType.GLASS;
    }

    public void setPartHostInfo(@Nullable Direction var1, IPartHost var2, BlockEntity var3);

    default public boolean onUseWithoutItem(Player player, Vec3 pos) {
        return false;
    }

    default public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        return false;
    }

    default public boolean onClicked(Player player, Vec3 pos) {
        return false;
    }

    default public boolean onShiftClicked(Player player, Vec3 pos) {
        return false;
    }

    default public void addPartDrop(List<ItemStack> drops, boolean wrenched) {
        ItemStack stack = new ItemStack(this.getPartItem());
        DataComponentMap.Builder builder = DataComponentMap.builder();
        this.exportSettings(SettingsFrom.DISMANTLE_ITEM, builder);
        stack.applyComponents(builder.build());
        drops.add(stack);
    }

    @MustBeInvokedByOverriders
    default public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
    }

    @MustBeInvokedByOverriders
    default public void clearContent() {
    }

    @Override
    public float getCableConnectionLength(AECableType var1);

    default public void animateTick(Level level, BlockPos pos, RandomSource r) {
    }

    default public void onPlacement(Player player) {
    }

    default public boolean canBePlacedOn(BusSupport what) {
        return what == BusSupport.CABLE;
    }

    default public IPartModel getStaticModels() {
        return new IPartModel(this){};
    }

    default public ModelData getModelData() {
        return ModelData.EMPTY;
    }

    public void getBoxes(IPartCollisionHelper var1);

    default public void addEntityCrashInfo(CrashReportCategory section) {
    }

    default public AECableType getDesiredConnectionType() {
        return AECableType.GLASS;
    }
}

