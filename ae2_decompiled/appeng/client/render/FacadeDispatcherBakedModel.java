/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.common.util.ItemStackMap
 */
package appeng.client.render;

import appeng.client.render.DelegateBakedModel;
import appeng.client.render.FacadeBakedItemModel;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.items.parts.FacadeItem;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ItemStackMap;

public class FacadeDispatcherBakedModel
extends DelegateBakedModel {
    private final FacadeBuilder facadeBuilder;
    private final Map<ItemStack, FacadeBakedItemModel> cache = ItemStackMap.createTypeAndTagMap();

    public FacadeDispatcherBakedModel(BakedModel baseModel, FacadeBuilder facadeBuilder) {
        super(baseModel);
        this.facadeBuilder = facadeBuilder;
    }

    public synchronized List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        Item item = stack.getItem();
        if (!(item instanceof FacadeItem)) {
            return List.of(this);
        }
        FacadeItem itemFacade = (FacadeItem)item;
        ItemStack textureItem = itemFacade.getTextureItem(stack);
        FacadeBakedItemModel model = this.cache.get(textureItem);
        if (model == null) {
            model = new FacadeBakedItemModel(this.getBaseModel(), textureItem, this.facadeBuilder);
            this.cache.put(textureItem, model);
        }
        return List.of(model);
    }
}

