/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.crafting;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AEColor;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.blockentity.crafting.CraftingMonitorModelData;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.crafting.CraftingCubeBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;

public class MonitorBakedModel
extends CraftingCubeBakedModel {
    private final TextureAtlasSprite chassisTexture;
    private final TextureAtlasSprite baseTexture;
    private final TextureAtlasSprite lightDarkTexture;
    private final TextureAtlasSprite lightMediumTexture;
    private final TextureAtlasSprite lightBrightTexture;

    public MonitorBakedModel(TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite chassisTexture, TextureAtlasSprite baseTexture, TextureAtlasSprite lightDarkTexture, TextureAtlasSprite lightMediumTexture, TextureAtlasSprite lightBrightTexture) {
        super(ringCorner, ringHor, ringVer);
        this.chassisTexture = chassisTexture;
        this.baseTexture = baseTexture;
        this.lightDarkTexture = lightDarkTexture;
        this.lightMediumTexture = lightMediumTexture;
        this.lightBrightTexture = lightBrightTexture;
    }

    @Override
    protected void addInnerCube(Direction side, BlockState state, ModelData modelData, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        Direction forward = IOrientationStrategy.get(state).getSide(state, RelativeSide.FRONT);
        if (side != forward) {
            builder.setTexture(this.chassisTexture);
            builder.addCube(x1, y1, z1, x2, y2, z2);
            return;
        }
        builder.setTexture(this.baseTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
        AEColor color = MonitorBakedModel.getColor(modelData);
        boolean powered = (Boolean)state.getValue((Property)CraftingMonitorBlock.POWERED);
        builder.setEmissiveMaterial(powered);
        builder.setColorRGB(color.whiteVariant);
        builder.setTexture(this.lightBrightTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
        builder.setColorRGB(color.mediumVariant);
        builder.setTexture(this.lightMediumTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
        builder.setColorRGB(color.blackVariant);
        builder.setTexture(this.lightDarkTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
        builder.setColor(-1);
        builder.setEmissiveMaterial(false);
    }

    private static AEColor getColor(ModelData modelData) {
        if (modelData.has(CraftingMonitorModelData.COLOR)) {
            return (AEColor)((Object)modelData.get(CraftingMonitorModelData.COLOR));
        }
        return AEColor.TRANSPARENT;
    }
}

