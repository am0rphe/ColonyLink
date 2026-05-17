/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$AxisDirection
 *  org.joml.Vector3f
 */
package appeng.client.render.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

final class RenderHelper {
    private static EnumMap<Direction, List<Vector3f>> cornersForFacing = RenderHelper.generateCornersForFacings();

    private RenderHelper() {
    }

    static List<Vector3f> getFaceCorners(Direction side) {
        return cornersForFacing.get(side);
    }

    private static EnumMap<Direction, List<Vector3f>> generateCornersForFacings() {
        EnumMap<Direction, List<Vector3f>> result = new EnumMap<Direction, List<Vector3f>>(Direction.class);
        for (Direction facing : Direction.values()) {
            List corners;
            float offset = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.0f : 1.0f;
            switch (facing.getAxis()) {
                default: {
                    throw new MatchException(null, null);
                }
                case X: {
                    ArrayList arrayList = Lists.newArrayList((Object[])new Vector3f[]{new Vector3f(offset, 1.0f, 1.0f), new Vector3f(offset, 0.0f, 1.0f), new Vector3f(offset, 0.0f, 0.0f), new Vector3f(offset, 1.0f, 0.0f)});
                    break;
                }
                case Y: {
                    ArrayList arrayList = Lists.newArrayList((Object[])new Vector3f[]{new Vector3f(1.0f, offset, 1.0f), new Vector3f(1.0f, offset, 0.0f), new Vector3f(0.0f, offset, 0.0f), new Vector3f(0.0f, offset, 1.0f)});
                    break;
                }
                case Z: {
                    ArrayList arrayList = corners = Lists.newArrayList((Object[])new Vector3f[]{new Vector3f(0.0f, 1.0f, offset), new Vector3f(0.0f, 0.0f, offset), new Vector3f(1.0f, 0.0f, offset), new Vector3f(1.0f, 1.0f, offset)});
                }
            }
            if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                corners = Lists.reverse((List)corners);
            }
            result.put(facing, (List<Vector3f>)ImmutableList.copyOf((Collection)corners));
        }
        return result;
    }
}

