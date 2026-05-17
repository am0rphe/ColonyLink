/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.DirectionalPlaceContext
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.debug;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.AppEng;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CubeGeneratorBlockEntity
extends AEBaseBlockEntity
implements ServerTickingBlockEntity {
    private int size = 3;
    private ItemStack is = ItemStack.EMPTY;
    private int countdown = 200;
    private Player who = null;

    public CubeGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void serverTick() {
        if (!this.is.isEmpty()) {
            --this.countdown;
            if (this.countdown % 20 == 0) {
                AppEng.instance().getPlayers().forEach(p -> p.sendSystemMessage((Component)Component.literal((String)("Spawning in... " + this.countdown / 20))));
            }
            if (this.countdown <= 0) {
                this.spawn();
            }
        }
    }

    private void spawn() {
        this.level.removeBlock(this.worldPosition, false);
        Item i = this.is.getItem();
        Direction side = Direction.UP;
        int half = (int)Math.floor(this.size / 2);
        for (int y = 0; y < this.size; ++y) {
            for (int x = -half; x < half; ++x) {
                for (int z = -half; z < half; ++z) {
                    BlockPos p = this.worldPosition.offset(x, y - 1, z);
                    DirectionalPlaceContext useContext = new DirectionalPlaceContext(this.level, p, side, this.is, side.getOpposite());
                    i.useOn((UseOnContext)useContext);
                }
            }
        }
    }

    void click(Player player) {
        if (!this.isClientSide()) {
            ItemStack hand = player.getInventory().getSelected();
            this.who = player;
            if (hand.isEmpty()) {
                this.is = ItemStack.EMPTY;
                this.size = InteractionUtil.isInAlternateUseMode(player) ? --this.size : ++this.size;
                if (this.size < 3) {
                    this.size = 3;
                }
                if (this.size > 64) {
                    this.size = 64;
                }
                player.sendSystemMessage((Component)Component.literal((String)("Size: " + this.size)));
            } else {
                this.countdown = 200;
                this.is = hand;
            }
        }
    }
}

