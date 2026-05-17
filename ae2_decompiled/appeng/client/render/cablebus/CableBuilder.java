/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.Direction
 */
package appeng.client.render.cablebus;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.cablebus.SmartCableTextures;
import appeng.core.AppEng;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;

class CableBuilder {
    private final EnumMap<CableCoreType, EnumMap<AEColor, TextureAtlasSprite>> coreTextures = new EnumMap(CableCoreType.class);
    private final EnumMap<AECableType, EnumMap<AEColor, TextureAtlasSprite>> connectionTextures;
    private final SmartCableTextures smartCableTextures;

    CableBuilder(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        EnumMap<AEColor, TextureAtlasSprite> colorTextures;
        for (CableCoreType cableCoreType : CableCoreType.values()) {
            colorTextures = new EnumMap<AEColor, TextureAtlasSprite>(AEColor.class);
            for (AEColor color : AEColor.values()) {
                colorTextures.put(color, bakedTextureGetter.apply(cableCoreType.getTexture(color)));
            }
            this.coreTextures.put(cableCoreType, colorTextures);
        }
        this.connectionTextures = new EnumMap(AECableType.class);
        for (Enum enum_ : AECableType.VALIDCABLES) {
            colorTextures = new EnumMap(AEColor.class);
            for (AEColor color : AEColor.values()) {
                colorTextures.put(color, bakedTextureGetter.apply(CableBuilder.getConnectionTexture((AECableType)enum_, color)));
            }
            this.connectionTextures.put((AECableType)enum_, colorTextures);
        }
        this.smartCableTextures = new SmartCableTextures(bakedTextureGetter);
    }

    static Material getConnectionTexture(AECableType cableType, AEColor color) {
        String textureFolder = switch (cableType) {
            case AECableType.GLASS -> "part/cable/glass/";
            case AECableType.COVERED -> "part/cable/covered/";
            case AECableType.SMART -> "part/cable/smart/";
            case AECableType.DENSE_COVERED -> "part/cable/dense_covered/";
            case AECableType.DENSE_SMART -> "part/cable/dense_smart/";
            default -> throw new IllegalStateException("Cable type " + String.valueOf((Object)cableType) + " does not support connections.");
        };
        return new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId(textureFolder + color.name().toLowerCase(Locale.ROOT)));
    }

    public void addCableCore(AECableType cableType, AEColor color, List<BakedQuad> quadsOut) {
        switch (cableType) {
            case GLASS: {
                this.addCableCore(CableCoreType.GLASS, color, quadsOut);
                break;
            }
            case COVERED: 
            case SMART: {
                this.addCableCore(CableCoreType.COVERED, color, quadsOut);
                break;
            }
            case DENSE_COVERED: 
            case DENSE_SMART: {
                this.addCableCore(CableCoreType.DENSE, color, quadsOut);
                break;
            }
        }
    }

    public void addCableCore(CableCoreType coreType, AEColor color, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        TextureAtlasSprite texture = this.coreTextures.get((Object)coreType).get((Object)color);
        cubeBuilder.setTexture(texture);
        switch (coreType) {
            case GLASS: {
                cubeBuilder.addCube(6.0f, 6.0f, 6.0f, 10.0f, 10.0f, 10.0f);
                break;
            }
            case COVERED: {
                cubeBuilder.addCube(5.0f, 5.0f, 5.0f, 11.0f, 11.0f, 11.0f);
                break;
            }
            case DENSE: {
                cubeBuilder.addCube(3.0f, 3.0f, 3.0f, 13.0f, 13.0f, 13.0f);
            }
        }
    }

    public void addGlassConnection(Direction facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, List<BakedQuad> quadsOut) {
        TextureAtlasSprite texture;
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            texture = this.connectionTextures.get((Object)AECableType.COVERED).get((Object)cableColor);
            cubeBuilder.setTexture(texture);
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
        }
        texture = this.connectionTextures.get((Object)AECableType.GLASS).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(6.0f, 0.0f, 6.0f, 10.0f, 6.0f, 10.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(10.0f, 6.0f, 6.0f, 16.0f, 10.0f, 10.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 0.0f, 10.0f, 10.0f, 6.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 10.0f, 10.0f, 10.0f, 16.0f);
                break;
            }
            case UP: {
                cubeBuilder.addCube(6.0f, 10.0f, 6.0f, 10.0f, 16.0f, 10.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(0.0f, 6.0f, 6.0f, 6.0f, 10.0f, 10.0f);
            }
        }
    }

    public void addStraightGlassConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite())));
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.GLASS).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        switch (facing) {
            case DOWN: 
            case UP: {
                cubeBuilder.addCube(6.0f, 0.0f, 6.0f, 10.0f, 16.0f, 10.0f);
                break;
            }
            case NORTH: 
            case SOUTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 0.0f, 10.0f, 10.0f, 16.0f);
                break;
            }
            case EAST: 
            case WEST: {
                cubeBuilder.addCube(0.0f, 6.0f, 6.0f, 16.0f, 10.0f, 10.0f);
            }
        }
    }

    public void addConstrainedGlassConnection(Direction facing, AEColor cableColor, int distanceFromEdge, List<BakedQuad> quadsOut) {
        if (distanceFromEdge >= 6) {
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.GLASS).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(6.0f, distanceFromEdge, 6.0f, 10.0f, 6.0f, 10.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(10.0f, 6.0f, 6.0f, 16 - distanceFromEdge, 10.0f, 10.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(6.0f, 6.0f, distanceFromEdge, 10.0f, 10.0f, 6.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 10.0f, 10.0f, 10.0f, 16 - distanceFromEdge);
                break;
            }
            case UP: {
                cubeBuilder.addCube(6.0f, 10.0f, 6.0f, 10.0f, 16 - distanceFromEdge, 10.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(distanceFromEdge, 6.0f, 6.0f, 6.0f, 10.0f, 10.0f);
            }
        }
    }

    public void addCoveredConnection(Direction facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.COVERED).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
        }
        CableBuilder.addCoveredCableSizedCube(facing, cubeBuilder);
    }

    public void addStraightCoveredConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.COVERED).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.setStraightCableUVs(cubeBuilder, facing, 0.3125f, 0.6875f);
        CableBuilder.addStraightCoveredCableSizedCube(facing, cubeBuilder);
    }

    private static void setStraightCableUVs(CubeBuilder cubeBuilder, Direction facing, float x, float y) {
        switch (facing) {
            case DOWN: 
            case UP: {
                cubeBuilder.setCustomUv(Direction.NORTH, x, 0.0f, y, x);
                cubeBuilder.setCustomUv(Direction.EAST, x, 0.0f, y, x);
                cubeBuilder.setCustomUv(Direction.SOUTH, x, 0.0f, y, x);
                cubeBuilder.setCustomUv(Direction.WEST, x, 0.0f, y, x);
                break;
            }
            case EAST: 
            case WEST: {
                cubeBuilder.setCustomUv(Direction.UP, 0.0f, x, x, y);
                cubeBuilder.setCustomUv(Direction.DOWN, 0.0f, x, x, y);
                cubeBuilder.setCustomUv(Direction.NORTH, 0.0f, x, x, y);
                cubeBuilder.setCustomUv(Direction.SOUTH, 0.0f, x, x, y);
                break;
            }
            case NORTH: 
            case SOUTH: {
                cubeBuilder.setCustomUv(Direction.UP, x, 0.0f, y, x);
                cubeBuilder.setCustomUv(Direction.DOWN, x, 0.0f, y, x);
                cubeBuilder.setCustomUv(Direction.EAST, 0.0f, x, x, y);
                cubeBuilder.setCustomUv(Direction.WEST, 0.0f, x, x, y);
            }
        }
    }

    public void addConstrainedCoveredConnection(Direction facing, AEColor cableColor, int distanceFromEdge, List<BakedQuad> quadsOut) {
        if (distanceFromEdge >= 5) {
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.COVERED).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
    }

    public void addSmartConnection(Direction facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut) {
        if (connectionType == AECableType.COVERED || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));
        switch (facing) {
            case DOWN: {
                cubeBuilder.setFlipU(Direction.EAST, true);
                cubeBuilder.setFlipU(Direction.NORTH, true);
                break;
            }
            case UP: {
                cubeBuilder.setFlipU(Direction.EAST, true);
                cubeBuilder.setFlipU(Direction.NORTH, true);
                cubeBuilder.setFlipV(Direction.DOWN, true);
                break;
            }
            case SOUTH: {
                cubeBuilder.setFlipU(Direction.NORTH, true);
                break;
            }
            case WEST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
                cubeBuilder.setFlipU(Direction.EAST, true);
                break;
            }
            case EAST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
            }
        }
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.SMART).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
            cubeBuilder.setEmissiveMaterial(true);
            cubeBuilder.setTexture(oddChannel);
            cubeBuilder.setColorRGB(cableColor.blackVariant);
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
            cubeBuilder.setTexture(evenChannel);
            cubeBuilder.setColorRGB(cableColor.whiteVariant);
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
            cubeBuilder.setEmissiveMaterial(false);
            cubeBuilder.setTexture(texture);
        }
        CableBuilder.addCoveredCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(true);
        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        CableBuilder.addCoveredCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        CableBuilder.addCoveredCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
    }

    public void addStraightSmartConnection(Direction facing, AEColor cableColor, int channels, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        switch (facing) {
            case EAST: 
            case WEST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
                break;
            }
            case DOWN: 
            case UP: {
                cubeBuilder.setFlipU(Direction.NORTH, true);
            }
        }
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.SMART).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.setStraightCableUVs(cubeBuilder, facing, 0.3125f, 0.6875f);
        CableBuilder.addStraightCoveredCableSizedCube(facing, cubeBuilder);
        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);
        cubeBuilder.setEmissiveMaterial(true);
        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        CableBuilder.addStraightCoveredCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        CableBuilder.addStraightCoveredCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
    }

    public void addConstrainedSmartConnection(Direction facing, AEColor cableColor, int distanceFromEdge, int channels, List<BakedQuad> quadsOut) {
        if (distanceFromEdge >= 5) {
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        switch (facing) {
            case DOWN: 
            case UP: {
                cubeBuilder.setFlipU(Direction.EAST, true);
                cubeBuilder.setFlipU(Direction.NORTH, true);
                break;
            }
            case EAST: 
            case WEST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
            }
        }
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.SMART).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);
        cubeBuilder.setEmissiveMaterial(true);
        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        CableBuilder.addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        CableBuilder.addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
    }

    public void addDenseCoveredConnection(Direction facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, List<BakedQuad> quadsOut) {
        if (connectionType == AECableType.COVERED || connectionType == AECableType.SMART || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.DENSE_COVERED).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.addDenseCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
        cubeBuilder.setTexture(texture);
    }

    public void addDenseSmartConnection(Direction facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut) {
        if (connectionType == AECableType.SMART) {
            this.addSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut);
            return;
        }
        if (connectionType == AECableType.COVERED || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }
        if (connectionType == AECableType.DENSE_COVERED) {
            this.addDenseCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));
        switch (facing) {
            case EAST: 
            case WEST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
                break;
            }
            case DOWN: 
            case UP: {
                cubeBuilder.setFlipU(Direction.NORTH, true);
                cubeBuilder.setFlipU(Direction.EAST, true);
            }
        }
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.DENSE_SMART).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.addDenseCableSizedCube(facing, cubeBuilder);
        channels = (channels + 3) / 4;
        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForDenseChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForDenseChannels(channels);
        cubeBuilder.setEmissiveMaterial(true);
        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        CableBuilder.addDenseCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        CableBuilder.addDenseCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
        cubeBuilder.setTexture(texture);
    }

    public void addStraightDenseCoveredConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.DENSE_COVERED).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.setStraightCableUVs(cubeBuilder, facing, 0.1875f, 0.8125f);
        CableBuilder.addStraightDenseCableSizedCube(facing, cubeBuilder);
    }

    public void addStraightDenseSmartConnection(Direction facing, AEColor cableColor, int channels, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);
        switch (facing) {
            case NORTH: {
                cubeBuilder.setFlipU(Direction.NORTH, true);
                break;
            }
            case WEST: {
                cubeBuilder.setFlipV(Direction.DOWN, true);
                cubeBuilder.setFlipU(Direction.EAST, true);
                break;
            }
            case DOWN: {
                cubeBuilder.setFlipU(Direction.NORTH, true);
                cubeBuilder.setFlipV(Direction.DOWN, true);
            }
        }
        TextureAtlasSprite texture = this.connectionTextures.get((Object)AECableType.DENSE_SMART).get((Object)cableColor);
        cubeBuilder.setTexture(texture);
        CableBuilder.setStraightCableUVs(cubeBuilder, facing, 0.1875f, 0.8125f);
        CableBuilder.addStraightDenseCableSizedCube(facing, cubeBuilder);
        channels = (channels + 3) / 4;
        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForDenseChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForDenseChannels(channels);
        cubeBuilder.setEmissiveMaterial(true);
        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        CableBuilder.addStraightDenseCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        CableBuilder.addStraightDenseCableSizedCube(facing, cubeBuilder);
        cubeBuilder.setEmissiveMaterial(false);
    }

    private static void addDenseCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(4.0f, 0.0f, 4.0f, 12.0f, 5.0f, 12.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(11.0f, 4.0f, 4.0f, 16.0f, 12.0f, 12.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(4.0f, 4.0f, 0.0f, 12.0f, 12.0f, 5.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(4.0f, 4.0f, 11.0f, 12.0f, 12.0f, 16.0f);
                break;
            }
            case UP: {
                cubeBuilder.addCube(4.0f, 11.0f, 4.0f, 12.0f, 16.0f, 12.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(0.0f, 4.0f, 4.0f, 5.0f, 12.0f, 12.0f);
            }
        }
    }

    private static void addStraightDenseCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: 
            case UP: {
                cubeBuilder.setUvRotation(Direction.EAST, 2);
                cubeBuilder.addCube(3.0f, -0.01f, 3.0f, 13.0f, 16.01f, 13.0f);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                break;
            }
            case EAST: 
            case WEST: {
                cubeBuilder.setUvRotation(Direction.SOUTH, 2);
                cubeBuilder.setUvRotation(Direction.NORTH, 2);
                cubeBuilder.addCube(-0.01f, 3.0f, 3.0f, 16.01f, 13.0f, 13.0f);
                cubeBuilder.setUvRotation(Direction.SOUTH, 0);
                cubeBuilder.setUvRotation(Direction.NORTH, 0);
                break;
            }
            case NORTH: 
            case SOUTH: {
                cubeBuilder.setUvRotation(Direction.EAST, 2);
                cubeBuilder.setUvRotation(Direction.WEST, 2);
                cubeBuilder.addCube(3.0f, 3.0f, -0.01f, 13.0f, 13.0f, 16.01f);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                cubeBuilder.setUvRotation(Direction.WEST, 0);
            }
        }
    }

    private static void addCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(6.0f, 0.0f, 6.0f, 10.0f, 5.0f, 10.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(11.0f, 6.0f, 6.0f, 16.0f, 10.0f, 10.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 0.0f, 10.0f, 10.0f, 5.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 11.0f, 10.0f, 10.0f, 16.0f);
                break;
            }
            case UP: {
                cubeBuilder.addCube(6.0f, 11.0f, 6.0f, 10.0f, 16.0f, 10.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(0.0f, 6.0f, 6.0f, 5.0f, 10.0f, 10.0f);
            }
        }
    }

    private static void addStraightCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: 
            case UP: {
                cubeBuilder.setUvRotation(Direction.EAST, 2);
                cubeBuilder.addCube(5.0f, 0.0f, 5.0f, 11.0f, 16.0f, 11.0f);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                break;
            }
            case EAST: 
            case WEST: {
                cubeBuilder.setUvRotation(Direction.SOUTH, 2);
                cubeBuilder.setUvRotation(Direction.NORTH, 2);
                cubeBuilder.addCube(0.0f, 5.0f, 5.0f, 16.0f, 11.0f, 11.0f);
                cubeBuilder.setUvRotation(Direction.SOUTH, 0);
                cubeBuilder.setUvRotation(Direction.NORTH, 0);
                break;
            }
            case NORTH: 
            case SOUTH: {
                cubeBuilder.setUvRotation(Direction.EAST, 2);
                cubeBuilder.setUvRotation(Direction.WEST, 2);
                cubeBuilder.addCube(5.0f, 5.0f, 0.0f, 11.0f, 11.0f, 16.0f);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                cubeBuilder.setUvRotation(Direction.WEST, 0);
            }
        }
    }

    private static void addCoveredCableSizedCube(Direction facing, int distanceFromEdge, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(6.0f, distanceFromEdge, 6.0f, 10.0f, 5.0f, 10.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(11.0f, 6.0f, 6.0f, 16 - distanceFromEdge, 10.0f, 10.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(6.0f, 6.0f, distanceFromEdge, 10.0f, 10.0f, 5.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(6.0f, 6.0f, 11.0f, 10.0f, 10.0f, 16 - distanceFromEdge);
                break;
            }
            case UP: {
                cubeBuilder.addCube(6.0f, 11.0f, 6.0f, 10.0f, 16 - distanceFromEdge, 10.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(distanceFromEdge, 6.0f, 6.0f, 5.0f, 10.0f, 10.0f);
            }
        }
    }

    private void addBigCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN: {
                cubeBuilder.addCube(5.0f, 0.0f, 5.0f, 11.0f, 4.0f, 11.0f);
                break;
            }
            case EAST: {
                cubeBuilder.addCube(12.0f, 5.0f, 5.0f, 16.0f, 11.0f, 11.0f);
                break;
            }
            case NORTH: {
                cubeBuilder.addCube(5.0f, 5.0f, 0.0f, 11.0f, 11.0f, 4.0f);
                break;
            }
            case SOUTH: {
                cubeBuilder.addCube(5.0f, 5.0f, 12.0f, 11.0f, 11.0f, 16.0f);
                break;
            }
            case UP: {
                cubeBuilder.addCube(5.0f, 12.0f, 5.0f, 11.0f, 16.0f, 11.0f);
                break;
            }
            case WEST: {
                cubeBuilder.addCube(0.0f, 5.0f, 5.0f, 4.0f, 11.0f, 11.0f);
            }
        }
    }

    public TextureAtlasSprite getCoreTexture(CableCoreType coreType, AEColor color) {
        return this.coreTextures.get((Object)coreType).get((Object)color);
    }
}

