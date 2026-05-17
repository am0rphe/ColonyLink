/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.items.parts;

import appeng.api.parts.IPartModel;
import appeng.core.AELog;
import appeng.items.parts.PartModels;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class PartModelsHelper {
    public static List<ResourceLocation> createModels(Class<?> clazz) {
        ArrayList<ResourceLocation> locations = new ArrayList<ResourceLocation>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Object value;
            if (field.getAnnotation(PartModels.class) == null) continue;
            if (!Modifier.isStatic(field.getModifiers())) {
                AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: " + String.valueOf(field), new Object[0]);
                continue;
            }
            try {
                field.setAccessible(true);
                value = field.get(null);
            }
            catch (IllegalAccessException e) {
                AELog.error(e, "Cannot access field annotated with @PartModels: " + String.valueOf(field));
                continue;
            }
            PartModelsHelper.convertAndAddLocation(field, value, locations);
        }
        for (AccessibleObject accessibleObject : clazz.getDeclaredMethods()) {
            Object value;
            if (((Method)accessibleObject).getAnnotation(PartModels.class) == null) continue;
            if (!Modifier.isStatic(((Method)accessibleObject).getModifiers())) {
                AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: " + String.valueOf(accessibleObject), new Object[0]);
                continue;
            }
            if (((Executable)accessibleObject).getParameters().length != 0) {
                AELog.error("The @PartModels annotation can only be used on static methods without parameters. Was seen on: " + String.valueOf(accessibleObject), new Object[0]);
                continue;
            }
            Class<?> returnType = ((Method)accessibleObject).getReturnType();
            if (!ResourceLocation.class.isAssignableFrom(returnType) && !Collection.class.isAssignableFrom(returnType)) {
                AELog.error("The @PartModels annotation can only be used on static methods that return a ResourceLocation or Collection of ResourceLocations. Was seen on: " + String.valueOf(accessibleObject), new Object[0]);
                continue;
            }
            try {
                ((Method)accessibleObject).setAccessible(true);
                value = ((Method)accessibleObject).invoke(null, new Object[0]);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                AELog.error(e, "Failed to invoke the @PartModels annotated method " + String.valueOf(accessibleObject));
                continue;
            }
            PartModelsHelper.convertAndAddLocation(accessibleObject, value, locations);
        }
        if (clazz.getSuperclass() != null) {
            locations.addAll(PartModelsHelper.createModels(clazz.getSuperclass()));
        }
        return locations;
    }

    private static void convertAndAddLocation(Object source, Object value, List<ResourceLocation> locations) {
        if (value == null) {
            return;
        }
        if (value instanceof ResourceLocation) {
            locations.add((ResourceLocation)value);
        } else if (value instanceof IPartModel) {
            locations.addAll(((IPartModel)value).getModels());
        } else if (value instanceof Collection) {
            Collection values = (Collection)value;
            for (Object candidate : values) {
                if (!(candidate instanceof IPartModel)) {
                    AELog.error("List of locations obtained from {} contains a non resource location: {}", source, candidate);
                    continue;
                }
                locations.addAll(((IPartModel)candidate).getModels());
            }
        }
    }
}

