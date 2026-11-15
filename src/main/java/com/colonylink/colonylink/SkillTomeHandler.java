package com.colonylink.colonylink;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * v1.8.0 — Skill Tome right-click handler (common / dedicated-server safe).
 *
 * MineColonies' {@code EntityCitizen} always consumes the interaction and returns
 * SUCCESS, so {@code Item#interactLivingEntity} is never called on our item. The
 * reliable entry point is {@link PlayerInteractEvent.EntityInteract}, which fires
 * on both sides before the entity handles the click and is cancellable with a
 * result.
 *
 * Nothing here references a client-only class; {@code levelUp} and {@code markDirty}
 * run server-side and push their own sync/sound/particle packets to clients.
 */
public final class SkillTomeHandler
{
    private SkillTomeHandler() {}

    private static final int MAX_SKILL_LEVEL = 99;

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event)
    {
        // Only the main hand, to avoid a second trigger from the off-hand pass.
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SkillTomeItem tome)) return;
        if (!(event.getTarget() instanceof AbstractEntityCitizen citizen)) return;

        // Feature switch off: do nothing at all, let the click fall through to the
        // normal MineColonies behaviour (opens the citizen GUI).
        if (!ColonyLinkConfig.safeGet(ColonyLinkConfig.ENABLE_SKILL_BOOKS, true)) return;

        // From here the tome "claims" the click: suppress the citizen GUI on both sides.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (event.getLevel().isClientSide()) return;

        final Player player = event.getEntity();

        final ICitizenData data = citizen.getCitizenData();
        if (data == null) return; // entity not yet bound to colony data

        // Tavern visitors are AbstractEntityCitizen too but are backed by IVisitorData.
        // A tome spent on a visitor who later leaves would be wasted, so refuse it.
        if (data instanceof IVisitorData)
        {
            player.sendSystemMessage(Component.translatable("colonylink.tome.visitor"));
            return;
        }

        final IColony colony = data.getColony();
        if (colony == null
                || !colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.translatable("colonylink.tome.no_permission"));
            return;
        }

        final ICitizenSkillHandler skills = data.getCitizenSkillHandler();
        final Skill skill = tome.getSkill();

        // incrementLevel() clamps silently at 99, so we must test the cap ourselves
        // to avoid consuming a tome for no gain.
        if (skills.getLevel(skill) >= MAX_SKILL_LEVEL)
        {
            player.sendSystemMessage(Component.translatable(
                    "colonylink.tome.max_level", data.getName(), skill.name()));
            return;
        }

        skills.incrementLevel(skill, 1);
        skills.levelUp(data);   // notifies the job via onLevelUp() (recalcs level-dependent state)
        data.markDirty(0);      // persist + push a citizen view update to nearby players

        // v1.8.0 — Stats: a tome was consumed on a citizen of this colony (global).
        ColonyLinkStats.recordTomesUsed(colony, 1);

        // levelUp() only plays its sound when an XP threshold is crossed, which
        // incrementLevel() never touches — so no sound fires here. Play an explicit
        // level-up sound at the citizen so the player gets clear feedback.
        citizen.level().playSound(null, citizen.blockPosition(),
                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);

        if (!player.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.translatable(
                "colonylink.tome.success", data.getName(), skill.name(), skills.getLevel(skill)));
    }
}
