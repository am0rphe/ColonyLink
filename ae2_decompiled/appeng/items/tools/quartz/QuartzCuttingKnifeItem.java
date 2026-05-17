/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools.quartz;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.items.AEBaseItem;
import appeng.items.tools.quartz.QuartzToolType;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class QuartzCuttingKnifeItem
extends AEBaseItem
implements IMenuItem {
    public QuartzCuttingKnifeItem(Item.Properties props, QuartzToolType type) {
        super(props);
    }

    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide() && player != null) {
            MenuOpener.open(QuartzKnifeMenu.TYPE, context.getPlayer(), MenuLocators.forItemUseContext(context));
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(QuartzKnifeMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }
        p.swing(hand);
        return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)p.getItemInHand(hand));
    }

    @Override
    @Nullable
    public ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new ItemMenuHost<QuartzCuttingKnifeItem>(this, player, locator);
    }
}

