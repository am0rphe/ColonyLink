/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.BakedModel
 */
package appeng.client.render;

import appeng.client.render.DelegateBakedModel;
import net.minecraft.client.resources.model.BakedModel;

public final class BakedModelUnwrapper {
    private BakedModelUnwrapper() {
    }

    public static <T> T unwrap(BakedModel model, Class<T> targetClass) {
        if (targetClass.isInstance(model)) {
            return targetClass.cast(model);
        }
        if (model instanceof DelegateBakedModel) {
            if (targetClass.isInstance(model = ((DelegateBakedModel)model).getBaseModel())) {
                return targetClass.cast(model);
            }
            return BakedModelUnwrapper.unwrap(model, targetClass);
        }
        return null;
    }
}

