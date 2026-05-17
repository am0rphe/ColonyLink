/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.primitives.Ints
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.network.serverbound;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.ICraftingGridMenu;
import appeng.items.storage.ViewCellItem;
import appeng.me.storage.NullInventory;
import appeng.util.CraftingRecipeUtil;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public record FillCraftingGridFromRecipePacket(@Nullable ResourceLocation recipeId, NonNullList<ItemStack> ingredientTemplates, boolean craftMissing) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, FillCraftingGridFromRecipePacket> STREAM_CODEC = StreamCodec.ofMember(FillCraftingGridFromRecipePacket::write, FillCraftingGridFromRecipePacket::decode);
    public static final CustomPacketPayload.Type<FillCraftingGridFromRecipePacket> TYPE = CustomAppEngPayload.createType("fill_crafting_grid_from_recipe");

    public FillCraftingGridFromRecipePacket(@Nullable ResourceLocation recipeId, NonNullList<ItemStack> ingredientTemplates, boolean craftMissing) {
        this.recipeId = recipeId;
        this.ingredientTemplates = NonNullList.copyOf(ingredientTemplates.stream().map(ItemStack::copy).toList());
        this.craftMissing = craftMissing;
    }

    public CustomPacketPayload.Type<FillCraftingGridFromRecipePacket> type() {
        return TYPE;
    }

    public static FillCraftingGridFromRecipePacket decode(RegistryFriendlyByteBuf stream) {
        ResourceLocation recipeId = null;
        if (stream.readBoolean()) {
            recipeId = stream.readResourceLocation();
        }
        NonNullList ingredientTemplates = NonNullList.withSize((int)stream.readInt(), (Object)ItemStack.EMPTY);
        for (int i = 0; i < ingredientTemplates.size(); ++i) {
            ingredientTemplates.set(i, (Object)((ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)stream)));
        }
        boolean craftMissing = stream.readBoolean();
        return new FillCraftingGridFromRecipePacket(recipeId, (NonNullList<ItemStack>)ingredientTemplates, craftMissing);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (this.recipeId != null) {
            data.writeBoolean(true);
            data.writeResourceLocation(this.recipeId);
        } else {
            data.writeBoolean(false);
        }
        data.writeInt(this.ingredientTemplates.size());
        for (ItemStack stack : this.ingredientTemplates) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)data, (Object)stack);
        }
        data.writeBoolean(this.craftMissing);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        KeyCounter cachedStorage;
        MEStorage networkStorage;
        IStorageService storageService;
        ICraftingService craftingService;
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof ICraftingGridMenu)) {
            return;
        }
        ICraftingGridMenu cct = (ICraftingGridMenu)menu;
        IEnergySource energy = cct.getEnergySource();
        IGridNode node = cct.getGridNode();
        if (node != null && cct.getLinkStatus().connected()) {
            craftingService = node.getGrid().getCraftingService();
            storageService = node.getGrid().getStorageService();
            networkStorage = storageService.getInventory();
            cachedStorage = storageService.getCachedInventory();
        } else {
            craftingService = null;
            storageService = null;
            networkStorage = NullInventory.of();
            cachedStorage = new KeyCounter();
        }
        InternalInventory craftMatrix = cct.getCraftingMatrix();
        IPartitionList filter = ViewCellItem.createItemFilter(cct.getViewCells());
        NonNullList<Ingredient> ingredients = this.getDesiredIngredients((Player)player);
        LinkedHashMap toAutoCraft = new LinkedHashMap();
        boolean touchedGridStorage = false;
        for (int x = 0; x < craftMatrix.size(); ++x) {
            ItemStack currentItem = craftMatrix.getStackInSlot(x);
            Ingredient ingredient = (Ingredient)ingredients.get(x);
            if (!currentItem.isEmpty()) {
                if (ingredient.test(currentItem)) continue;
                AEItemKey in = AEItemKey.of(currentItem);
                long inserted = StorageHelper.poweredInsert(energy, networkStorage, in, currentItem.getCount(), cct.getActionSource());
                if (inserted > 0L) {
                    touchedGridStorage = true;
                }
                if (inserted < (long)currentItem.getCount()) {
                    currentItem = currentItem.copy();
                    currentItem.shrink((int)inserted);
                } else {
                    currentItem = ItemStack.EMPTY;
                }
                player.getInventory().add(currentItem);
                craftMatrix.setItemDirect(x, currentItem.isEmpty() ? ItemStack.EMPTY : currentItem);
            }
            if (ingredient.isEmpty()) continue;
            if (currentItem.isEmpty()) {
                List<AEItemKey> request = this.findBestMatchingItemStack(ingredient, filter, cachedStorage);
                for (AEItemKey what : request) {
                    long extracted = StorageHelper.poweredExtraction(energy, networkStorage, what, 1L, cct.getActionSource());
                    if (extracted <= 0L) continue;
                    touchedGridStorage = true;
                    currentItem = what.toStack(Ints.saturatedCast((long)extracted));
                    break;
                }
            }
            if (currentItem.isEmpty()) {
                currentItem = this.takeIngredientFromPlayer(cct, player, ingredient);
            }
            craftMatrix.setItemDirect(x, currentItem);
            if (!currentItem.isEmpty() || !this.craftMissing || craftingService == null) continue;
            int slot = x;
            this.findCraftableKey(ingredient, craftingService).ifPresent(key -> toAutoCraft.computeIfAbsent(key, k -> new IntArrayList()).add(slot));
        }
        menu.slotsChanged(craftMatrix.toContainer());
        if (!toAutoCraft.isEmpty()) {
            if (touchedGridStorage) {
                storageService.invalidateCache();
            }
            List<ICraftingGridMenu.AutoCraftEntry> stacks = toAutoCraft.entrySet().stream().map(e -> new ICraftingGridMenu.AutoCraftEntry((AEItemKey)e.getKey(), (List)e.getValue())).toList();
            cct.startAutoCrafting(stacks);
        }
    }

    private ItemStack takeIngredientFromPlayer(ICraftingGridMenu cct, ServerPlayer player, Ingredient ingredient) {
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < playerInv.items.size(); ++i) {
            ItemStack result;
            ItemStack item;
            if (cct.isPlayerInventorySlotLocked(i) || !ingredient.test(item = playerInv.getItem(i)) || (result = item.split(1)).isEmpty()) continue;
            return result;
        }
        return ItemStack.EMPTY;
    }

    private NonNullList<Ingredient> getDesiredIngredients(Player player) {
        RecipeHolder recipe;
        if (this.recipeId != null && (recipe = (RecipeHolder)player.level().getRecipeManager().byKey(this.recipeId).orElse(null)) != null) {
            return CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe.value());
        }
        NonNullList ingredients = NonNullList.withSize((int)9, (Object)Ingredient.EMPTY);
        Preconditions.checkArgument((ingredients.size() == this.ingredientTemplates.size() ? 1 : 0) != 0, (String)"Got %d ingredient templates from client, expected %d", (int)this.ingredientTemplates.size(), (int)ingredients.size());
        for (int i = 0; i < ingredients.size(); ++i) {
            ItemStack template = (ItemStack)this.ingredientTemplates.get(i);
            if (template.isEmpty()) continue;
            ingredients.set(i, (Object)Ingredient.of((ItemStack[])new ItemStack[]{template}));
        }
        return ingredients;
    }

    private List<AEItemKey> findBestMatchingItemStack(Ingredient ingredient, IPartitionList filter, KeyCounter storage) {
        return Arrays.stream(ingredient.getItems()).map(AEItemKey::of).filter(r -> r != null && (filter == null || filter.isListed((AEKey)r))).flatMap(s -> storage.findFuzzy((AEKey)s, FuzzyMode.IGNORE_ALL).stream()).filter(e -> ((AEItemKey)e.getKey()).matches(ingredient)).sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue())).map(e -> (AEItemKey)e.getKey()).toList();
    }

    private Optional<AEItemKey> findCraftableKey(Ingredient ingredient, ICraftingService craftingService) {
        return Arrays.stream(ingredient.getItems()).map(AEItemKey::of).map(s -> (AEItemKey)craftingService.getFuzzyCraftable((AEKey)s, key -> ((AEItemKey)key).matches(ingredient))).filter(Objects::nonNull).findAny();
    }
}

