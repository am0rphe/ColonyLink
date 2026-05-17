/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.document.LytRect
 *  guideme.document.block.LytBlock
 *  guideme.document.block.LytBox
 *  guideme.document.block.LytSlot
 *  guideme.layout.LayoutContext
 *  guideme.render.RenderContext
 */
package appeng.client.guidebook;

import appeng.client.guidebook.AE2GuideAssets;
import appeng.recipes.handlers.InscriberRecipe;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytBox;
import guideme.document.block.LytSlot;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;

public class LytInscriberRecipe
extends LytBox {
    private final LytSlot topSlot;
    private final LytSlot middleSlot;
    private final LytSlot bottomSlot;
    private final LytSlot resultSlot;

    public LytInscriberRecipe(InscriberRecipe recipe) {
        this.topSlot = new LytSlot(recipe.getTopOptional());
        this.append((LytBlock)this.topSlot);
        this.middleSlot = new LytSlot(recipe.getMiddleInput());
        this.append((LytBlock)this.middleSlot);
        this.bottomSlot = new LytSlot(recipe.getBottomOptional());
        this.append((LytBlock)this.bottomSlot);
        this.resultSlot = new LytSlot(recipe.getResultItem());
        this.append((LytBlock)this.resultSlot);
        this.resultSlot.setLargeSlot(true);
    }

    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        this.topSlot.layout(context, x, y, availableWidth);
        this.middleSlot.layout(context, x + 18, y + 23, availableWidth);
        this.bottomSlot.layout(context, x, y + 46, availableWidth);
        this.resultSlot.layout(context, x + 64, y + 20, availableWidth);
        return new LytRect(x, y, 90, 64);
    }

    public void render(RenderContext context) {
        LytRect bounds = this.getBounds();
        context.fillIcon(new LytRect(bounds.x() + 18, bounds.y() + 7, 46, 50), AE2GuideAssets.INSCRIBER_ARROWS);
        super.render(context);
    }
}

