/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.Util
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.flag.FeatureFlag
 *  net.minecraft.world.item.SmithingTemplateItem
 */
package appeng.items.tools.fluix;

import appeng.api.ids.AEItemIds;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.SmithingTemplateItem;

public class FluixSmithingTemplateItem
extends SmithingTemplateItem {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.parse((String)"item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.parse((String)"item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.parse((String)"item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.parse((String)"item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.parse((String)"item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_BLOCK = AppEng.makeId("item/empty_slot_block");

    public FluixSmithingTemplateItem() {
        super((Component)GuiText.QuartzTools.text().withStyle(DESCRIPTION_FORMAT), (Component)Component.translatable((String)Util.makeDescriptionId((String)"item", (ResourceLocation)AEItemIds.FLUIX_CRYSTAL)).withStyle(DESCRIPTION_FORMAT), (Component)Component.translatable((String)Util.makeDescriptionId((String)"item", (ResourceLocation)AEItemIds.FLUIX_UPGRADE_SMITHING_TEMPLATE)).withStyle(TITLE_FORMAT), (Component)GuiText.PutAQuartzTool.text(), (Component)GuiText.PutAFluixBlock.text(), List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL), List.of(EMPTY_SLOT_BLOCK), new FeatureFlag[0]);
    }
}

