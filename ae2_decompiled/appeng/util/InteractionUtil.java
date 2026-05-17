/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package appeng.util;

import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.tools.NetworkToolItem;
import appeng.util.LookDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class InteractionUtil {
    private InteractionUtil() {
    }

    public static boolean canWrenchDisassemble(ItemStack tool) {
        return tool.is(ConventionTags.WRENCH);
    }

    public static boolean canWrenchRotate(ItemStack tool) {
        if (tool.getItem() instanceof NetworkToolItem) {
            return false;
        }
        return tool.is(ConventionTags.WRENCH);
    }

    public static boolean isInAlternateUseMode(Player player) {
        return player.isShiftKeyDown();
    }

    public static LookDirection getPlayerRay(Player playerIn, double reachDistance) {
        double x = playerIn.xo + (playerIn.getX() - playerIn.xo);
        double y = playerIn.yo + (playerIn.getY() - playerIn.yo) + (double)playerIn.getEyeHeight();
        double z = playerIn.zo + (playerIn.getZ() - playerIn.zo);
        float playerPitch = playerIn.xRotO + (playerIn.getXRot() - playerIn.xRotO);
        float playerYaw = playerIn.yRotO + (playerIn.getYRot() - playerIn.yRotO);
        float yawRayX = Mth.sin((float)(-playerYaw * ((float)Math.PI / 180) - (float)Math.PI));
        float yawRayZ = Mth.cos((float)(-playerYaw * ((float)Math.PI / 180) - (float)Math.PI));
        float pitchMultiplier = -Mth.cos((float)(-playerPitch * ((float)Math.PI / 180)));
        float eyeRayY = Mth.sin((float)(-playerPitch * ((float)Math.PI / 180)));
        float eyeRayX = yawRayX * pitchMultiplier;
        float eyeRayZ = yawRayZ * pitchMultiplier;
        Vec3 from = new Vec3(x, y, z);
        Vec3 to = from.add((double)eyeRayX * reachDistance, (double)eyeRayY * reachDistance, (double)eyeRayZ * reachDistance);
        return new LookDirection(from, to);
    }
}

