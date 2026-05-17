/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.client.gui.me.crafting;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.me.crafting.CraftConfirmMenu;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CraftErrorScreen
extends AESubScreen<CraftConfirmMenu, CraftConfirmScreen> {
    public CraftErrorScreen(CraftConfirmScreen parent, CraftingSubmitErrorCode errorCode, Object details) {
        super(parent, "/screens/craft_error.json");
        MutableComponent errorText = switch (errorCode) {
            default -> throw new MatchException(null, null);
            case CraftingSubmitErrorCode.INCOMPLETE_PLAN -> GuiText.CraftErrorIncompletePlan.text();
            case CraftingSubmitErrorCode.NO_CPU_FOUND -> GuiText.CraftErrorNoCpuFound.text();
            case CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND -> {
                MutableComponent text = GuiText.CraftErrorNoSuitableCpu.text();
                if (details instanceof UnsuitableCpus) {
                    UnsuitableCpus unsuitableCpus = (UnsuitableCpus)details;
                    ArrayList<MutableComponent> stats = new ArrayList<MutableComponent>();
                    if (unsuitableCpus.offline() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuOffline.text(unsuitableCpus.offline()));
                    }
                    if (unsuitableCpus.busy() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuBusy.text(unsuitableCpus.busy()));
                    }
                    if (unsuitableCpus.tooSmall() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuTooSmall.text(unsuitableCpus.tooSmall()));
                    }
                    if (unsuitableCpus.excluded() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuExcluded.text(unsuitableCpus.excluded()));
                    }
                    MutableComponent suffix = Component.literal((String)"(");
                    for (int i = 0; i < stats.size(); ++i) {
                        Component stat = (Component)stats.get(i);
                        if (i != 0) {
                            suffix = suffix.append(", ");
                        }
                        suffix = suffix.append(stat);
                    }
                    suffix = suffix.append(")");
                    text = text.append(" ").append((Component)suffix);
                }
                yield text;
            }
            case CraftingSubmitErrorCode.CPU_BUSY -> GuiText.CraftErrorCpuBusy.text();
            case CraftingSubmitErrorCode.CPU_OFFLINE -> GuiText.CraftErrorCpuOffline.text();
            case CraftingSubmitErrorCode.CPU_TOO_SMALL -> GuiText.CraftErrorCpuTooSmall.text();
            case CraftingSubmitErrorCode.MISSING_INGREDIENT -> {
                if (details instanceof GenericStack) {
                    GenericStack genericStack = (GenericStack)details;
                    this.addClientSideSlot(new ClientDisplaySlot(genericStack), SlotSemantics.MISSING_INGREDIENT);
                }
                yield GuiText.CraftErrorMissingIngredient.text();
            }
        };
        this.setTextContent("errorText", (Component)errorText);
        this.widgets.addButton("replan", (Component)GuiText.CraftErrorReplan.text(), () -> {
            this.returnToParent();
            ((CraftConfirmMenu)this.menu).replan();
        });
        this.widgets.addButton("retry", (Component)GuiText.CraftErrorRetry.text(), () -> {
            this.returnToParent();
            ((CraftConfirmMenu)this.menu).startJob();
        });
        this.widgets.addButton("cancel", (Component)GuiText.Cancel.text(), () -> {
            this.returnToParent();
            ((CraftConfirmMenu)this.menu).goBack();
        });
    }

    @Override
    protected void onReturnToParent() {
        ((CraftConfirmMenu)this.menu).clearError();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
    }
}

