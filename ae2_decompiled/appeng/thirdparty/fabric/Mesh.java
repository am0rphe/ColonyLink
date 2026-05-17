/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlas
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.SpriteFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;

public interface Mesh {
    public void forEach(Consumer<QuadView> var1);

    default public Collection<BakedQuad> toBakedBlockQuads() {
        SpriteFinder finder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        ArrayList<BakedQuad> result = new ArrayList<BakedQuad>();
        this.forEach(qv -> result.add(qv.toBakedQuad(finder.find((QuadView)qv))));
        return result;
    }
}

