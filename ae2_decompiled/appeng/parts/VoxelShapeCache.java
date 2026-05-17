/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.BooleanOp
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package appeng.parts;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class VoxelShapeCache {
    private static final LoadingCache<List<AABB>, VoxelShape> CACHE = CacheBuilder.newBuilder().maximumSize(10000L).build((CacheLoader)new CacheLoader<List<AABB>, VoxelShape>(){

        public VoxelShape load(List<AABB> key) {
            return VoxelShapeCache.create(key);
        }
    });

    private VoxelShapeCache() {
    }

    public static VoxelShape get(List<AABB> boxes) {
        return (VoxelShape)CACHE.getUnchecked(boxes);
    }

    private static VoxelShape create(List<AABB> boxes) {
        int i;
        if (boxes.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape shape = Shapes.create((AABB)boxes.get(i));
        for (i = 0; i < boxes.size(); ++i) {
            AABB box = boxes.get(i);
            shape = Shapes.joinUnoptimized((VoxelShape)shape, (VoxelShape)Shapes.create((AABB)box), (BooleanOp)BooleanOp.OR);
        }
        return shape.optimize();
    }
}

