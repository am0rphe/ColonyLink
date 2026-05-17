/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.crafting;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.crafting.CraftingCubeBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class UnitBakedModel
extends CraftingCubeBakedModel {
    private final TextureAtlasSprite unitTexture;

    public UnitBakedModel(TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite unitTexture) {
        super(ringCorner, ringHor, ringVer);
        this.unitTexture = unitTexture;
    }

    @Override
    protected void addInnerCube(Direction facing, BlockState state, ModelData modelData, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        builder.setTexture(this.unitTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }
}

