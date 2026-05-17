/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiRecipeCategory
 *  dev.emi.emi.api.render.EmiRenderable
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.emi;

import appeng.core.AppEng;
import appeng.core.localization.LocalizationEnum;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;

class AppEngRecipeCategory
extends EmiRecipeCategory {
    private final Component name;

    public AppEngRecipeCategory(String id, EmiRenderable icon, LocalizationEnum name) {
        super(AppEng.makeId(id), icon);
        this.name = name.text();
    }

    public Component getName() {
        return this.name;
    }
}

