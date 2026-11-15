package com.colonylink.colonylink;

import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.world.item.Item;

/**
 * v1.8.0 — Skill Tome.
 *
 * One item instance per MineColonies {@link Skill} (11 total). Right-clicking a
 * colony citizen with a tome grants +1 level in the matching skill and consumes
 * the book. The interaction itself lives in {@link SkillTomeHandler}; this class
 * only carries the skill identity.
 *
 * The skill is stored as a reference to the enum value (never an ordinal), so a
 * future skill addition/removal cannot silently shift the mapping.
 */
public class SkillTomeItem extends Item
{
    private final Skill skill;

    public SkillTomeItem(Properties properties, Skill skill)
    {
        super(properties);
        this.skill = skill;
    }

    public Skill getSkill()
    {
        return skill;
    }
}
