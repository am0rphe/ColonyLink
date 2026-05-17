/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.BasicEmiRecipe
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  dev.emi.emi.api.render.EmiTexture
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  dev.emi.emi.api.widget.WidgetHolder
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.emi;

import appeng.core.definitions.AEParts;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AppEngRecipeCategory;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;

class EmiP2PAttunementRecipe
extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("p2p_attunement", (EmiRenderable)EmiStack.of(AEParts.ME_P2P_TUNNEL), ItemModText.P2P_TUNNEL_ATTUNEMENT);
    private final EmiIngredient input;
    private final EmiStack p2pTunnel;
    private final Component description;

    public EmiP2PAttunementRecipe(EmiIngredient input, EmiStack p2pTunnel, Component description) {
        super(CATEGORY, null, 150, 36);
        this.input = input;
        this.p2pTunnel = p2pTunnel;
        this.description = description;
        this.inputs.add(input);
        this.outputs.add(p2pTunnel);
    }

    public void addWidgets(WidgetHolder widgets) {
        int originX = this.width / 2 - 41;
        int originY = this.height / 2 - 13;
        widgets.addSlot(this.input, originX + 3, originY + 4).appendTooltip(this.description);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, originX + 27, originY + 4);
        widgets.addSlot((EmiIngredient)this.p2pTunnel, originX + 60, originY + 4);
    }

    public boolean supportsRecipeTree() {
        return false;
    }

    public boolean hideCraftable() {
        return true;
    }
}

