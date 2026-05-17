/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  guideme.Guide
 *  guideme.GuidesCommon
 *  guideme.PageAnchor
 *  guideme.compiler.TagCompiler
 *  guideme.extensions.Extension
 *  guideme.scene.ImplicitAnnotationStrategy
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.phys.HitResult
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.IExtensionPoint
 *  net.neoforged.fml.InterModComms
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 *  net.neoforged.fml.event.lifecycle.InterModEnqueueEvent
 *  net.neoforged.fml.loading.FMLLoader
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterLayerDefinitions
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.neoforged.neoforge.client.event.InputEvent$Key
 *  net.neoforged.neoforge.client.event.InputEvent$MouseScrollingEvent
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterAdditional
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterGeometryLoaders
 *  net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent
 *  net.neoforged.neoforge.client.event.RegisterColorHandlersEvent$Block
 *  net.neoforged.neoforge.client.event.RegisterColorHandlersEvent$Item
 *  net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
 *  net.neoforged.neoforge.client.gui.ConfigurationScreen
 *  net.neoforged.neoforge.client.gui.IConfigScreenFactory
 *  net.neoforged.neoforge.client.settings.IKeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyConflictContext
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.core;

import appeng.api.parts.CableRenderMode;
import appeng.blockentity.networking.CableBusTESR;
import appeng.client.EffectType;
import appeng.client.Hotkeys;
import appeng.client.commands.ClientCommands;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.ConfigValueTagExtension;
import appeng.client.guidebook.PartAnnotationStrategy;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.crafting.CraftingMonitorRenderer;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.GlassBakedModel;
import appeng.client.render.overlay.OverlayManager;
import appeng.client.render.tesr.ChargerBlockEntityRenderer;
import appeng.client.render.tesr.ChestBlockEntityRenderer;
import appeng.client.render.tesr.CrankRenderer;
import appeng.client.render.tesr.DriveLedBlockEntityRenderer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.client.render.tesr.SkyStoneTankBlockEntityRenderer;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.BlockAttackHook;
import appeng.hooks.RenderBlockOutlineHook;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitEntityLayerDefinitions;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitScreens;
import appeng.init.client.InitStackRenderHandlers;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.siteexport.AESiteExporter;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;
import appeng.util.Platform;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import guideme.Guide;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import guideme.compiler.TagCompiler;
import guideme.extensions.Extension;
import guideme.scene.ImplicitAnnotationStrategy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value="ae2", dist={Dist.CLIENT})
public class AppEngClient
extends AppEngBase {
    private static final Logger LOG = LoggerFactory.getLogger(AppEngClient.class);
    private static AppEngClient INSTANCE;
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER;
    private static final KeyMapping PART_PLACEMENT_OPPOSITE;
    private final Guide guide;

    public AppEngClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
        InitBuiltInModels.init();
        this.registerClientCommands();
        modEventBus.addListener(this::registerClientTooltipComponents);
        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::modelRegistryEventAdditionalModels);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);
        modEventBus.addListener(this::registerHotkeys);
        modEventBus.addListener(this::registerDimensionSpecialEffects);
        modEventBus.addListener(InitScreens::init);
        modEventBus.addListener(this::enqueueImcMessages);
        BlockAttackHook.install();
        RenderBlockOutlineHook.install();
        this.guide = this.createGuide();
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, e -> this.updateCableRenderMode());
        modEventBus.addListener(this::clientSetup);
        INSTANCE = this;
        NeoForge.EVENT_BUS.addListener(evt -> {
            PendingCraftingJobs.clearPendingJobs();
            PinnedKeys.clearPinnedKeys();
        });
        NeoForge.EVENT_BUS.addListener(e -> {
            this.tickPinnedKeys(Minecraft.getInstance());
            Hotkeys.checkHotkeys();
        });
        container.registerExtensionPoint(IConfigScreenFactory.class, (IExtensionPoint)((IConfigScreenFactory)(mc, parent) -> new ConfigurationScreen(container, parent)));
    }

    private void enqueueImcMessages(InterModEnqueueEvent event) {
        InterModComms.sendTo((String)"darkmodeeverywhere", (String)"dme-shaderblacklist", () -> "appeng.");
        InterModComms.sendTo((String)"framedblocks", (String)"add_ct_property", () -> GlassBakedModel.GLASS_STATE);
    }

    private void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(), SpatialStorageSkyProperties.INSTANCE);
    }

    private void registerClientCommands() {
        NeoForge.EVENT_BUS.addListener(evt -> {
            CommandDispatcher dispatcher = evt.getDispatcher();
            LiteralArgumentBuilder builder = Commands.literal((String)"ae2client");
            if (AEConfig.instance().isDebugToolsEnabled()) {
                for (ClientCommands.CommandBuilder commandBuilder : ClientCommands.DEBUG_COMMANDS) {
                    commandBuilder.build((LiteralArgumentBuilder<CommandSourceStack>)builder);
                }
            }
            dispatcher.register(builder);
        });
    }

    private Guide createGuide() {
        return Guide.builder((ResourceLocation)AppEng.makeId("guide")).folder("ae2guide").extension(ImplicitAnnotationStrategy.EXTENSION_POINT, (Extension)new PartAnnotationStrategy()).extension(TagCompiler.EXTENSION_POINT, (Extension)new ConfigValueTagExtension()).build();
    }

    private void tickPinnedKeys(Minecraft minecraft) {
        if (minecraft.screen == null) {
            PinnedKeys.prune();
        }
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void registerHotkey(String id) {
        Hotkeys.registerHotkey(id);
    }

    private void registerHotkeys(RegisterKeyMappingsEvent e) {
        e.register(MOUSE_WHEEL_ITEM_MODIFIER);
        e.register(PART_PLACEMENT_OPPOSITE);
        Hotkeys.finalizeRegistration(arg_0 -> ((RegisterKeyMappingsEvent)e).register(arg_0));
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType)ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        event.registerSpriteSet(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        event.registerSpriteSet(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        event.registerSpriteSet((ParticleType)ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        event.registerSpriteSet((ParticleType)ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        event.registerSpriteSet((ParticleType)ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        InitBlockColors.init(event.getBlockColors());
    }

    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        InitItemColors.init(event);
    }

    private void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(StorageCellTooltipComponent.class, StorageCellClientTooltipComponent::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                this.postClientSetup(minecraft);
            }
            catch (Throwable e) {
                LOG.error("AE2 failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });
        NeoForge.EVENT_BUS.addListener(this::wheelEvent);
        NeoForge.EVENT_BUS.addListener(this::ctrlEvent);
        NeoForge.EVENT_BUS.register((Object)OverlayManager.getInstance());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)AEEntities.TINY_TNT_PRIMED.get(), TinyTNTPrimedRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CRANK.get(), CrankRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.INSCRIBER.get(), InscriberTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_CHEST.get(), SkyChestTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CHARGER.get(), ChargerBlockEntityRenderer.FACTORY);
        event.registerBlockEntityRenderer(AEBlockEntities.DRIVE.get(), DriveLedBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.ME_CHEST.get(), ChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CRAFTING_MONITOR.get(), CraftingMonitorRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.MOLECULAR_ASSEMBLER.get(), MolecularAssemblerRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CABLE_BUS.get(), CableBusTESR::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_STONE_TANK.get(), SkyStoneTankBlockEntityRenderer::new);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        InitEntityLayerDefinitions.init((modelLayerLocation, layerDefinition) -> event.registerLayerDefinition(modelLayerLocation, () -> layerDefinition));
    }

    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitStackRenderHandlers.init();
        if (!FMLLoader.isProduction() && Boolean.getBoolean("appeng.runGuideExportAndExit")) {
            Path outputFolder = Paths.get(System.getProperty("appeng.guideExportFolder"), new String[0]);
            new AESiteExporter(minecraft, outputFolder, this.guide).exportOnNextTickAndExit();
        }
    }

    public void modelRegistryEventAdditionalModels(ModelEvent.RegisterAdditional event) {
        InitAdditionalModels.init(event);
    }

    public void modelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        InitItemModelsProperties.init();
    }

    private void wheelEvent(InputEvent.MouseScrollingEvent me) {
        if (me.getScrollDeltaY() == 0.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (MOUSE_WHEEL_ITEM_MODIFIER.isDown()) {
            boolean mainHand = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof IMouseWheelItem;
            boolean offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;
            if (mainHand || offHand) {
                MouseWheelPacket message = new MouseWheelPacket(me.getScrollDeltaY() > 0.0);
                PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
                me.setCanceled(true);
            }
        }
    }

    private void ctrlEvent(InputEvent.Key event) {
        LocalPlayer player;
        if (event.getKey() == PART_PLACEMENT_OPPOSITE.getKey().getValue() && (player = Minecraft.getInstance().player) != null) {
            boolean isDown = event.getAction() == 1 || event.getAction() == 2;
            Boolean previousIsDown = (Boolean)player.getData(AEAttachmentTypes.HOLDING_CTRL);
            if (previousIsDown != isDown) {
                player.setData(AEAttachmentTypes.HOLDING_CTRL, (Object)isDown);
                PacketDistributor.sendToServer((CustomPacketPayload)new UpdateHoldingCtrlPacket(isDown), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
    }

    public boolean shouldAddParticles(RandomSource r) {
        return switch ((ParticleStatus)Minecraft.getInstance().options.particles().get()) {
            default -> throw new MatchException(null, null);
            case ParticleStatus.ALL -> true;
            case ParticleStatus.DECREASED -> r.nextBoolean();
            case ParticleStatus.MINIMAL -> false;
        };
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    @Override
    public void spawnEffect(EffectType effect, Level level, double posX, double posY, double posZ, Object o) {
        if (AEConfig.instance().isEnableEffects()) {
            switch (effect) {
                case Vibrant: {
                    this.spawnVibrant(level, posX, posY, posZ);
                    return;
                }
                case Energy: {
                    this.spawnEnergy(level, posX, posY, posZ);
                    return;
                }
                case Lightning: {
                    this.spawnLightning(level, posX, posY, posZ);
                    return;
                }
            }
        }
    }

    private void spawnVibrant(Level level, double x, double y, double z) {
        if (AppEngClient.instance().shouldAddParticles(level.getRandom())) {
            double d0 = (double)(level.getRandom().nextFloat() - 0.5f) * 0.26;
            double d1 = (double)(level.getRandom().nextFloat() - 0.5f) * 0.26;
            double d2 = (double)(level.getRandom().nextFloat() - 0.5f) * 0.26;
            Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0, 0.0, 0.0);
        }
    }

    private void spawnEnergy(Level level, double posX, double posY, double posZ) {
        RandomSource random = level.getRandom();
        float x = (float)((double)(Math.abs(random.nextInt()) % 100) * 0.01 - 0.5) * 0.7f;
        float y = (float)((double)(Math.abs(random.nextInt()) % 100) * 0.01 - 0.5) * 0.7f;
        float z = (float)((double)(Math.abs(random.nextInt()) % 100) * 0.01 - 0.5) * 0.7f;
        Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)EnergyParticleData.FOR_BLOCK, posX + (double)x, posY + (double)y, posZ + (double)z, (double)(-x) * 0.1, (double)(-y) * 0.1, (double)(-z) * 0.1);
    }

    private void spawnLightning(Level level, double posX, double posY, double posZ) {
        Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)ParticleTypes.LIGHTNING, posX, posY + (double)0.3f, posZ, 0.0, 0.0, 0.0);
    }

    private void updateCableRenderMode() {
        CableRenderMode currentMode = this.getCableRenderMode();
        if (currentMode == this.prevCableRenderMode) {
            return;
        }
        this.prevCableRenderMode = currentMode;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        int viewDistance = (int)Math.ceil(mc.levelRenderer.getLastViewDistance());
        ChunkPos.rangeClosed((ChunkPos)mc.player.chunkPosition(), (int)viewDistance).forEach(chunkPos -> {
            LevelChunk chunk = mc.level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                for (int i = 0; i < chunk.getSectionsCount(); ++i) {
                    LevelChunkSection section = chunk.getSection(i);
                    if (!section.maybeHas(state -> state.is((Block)AEBlocks.CABLE_BUS.block()))) continue;
                    mc.levelRenderer.setSectionDirty(chunkPos.x, chunk.getSectionYFromSectionIndex(i), chunkPos.z);
                }
            }
        });
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return CableRenderMode.STANDARD;
        }
        return this.getCableRenderModeForPlayer((Player)mc.player);
    }

    @Override
    public void openGuideAtAnchor(PageAnchor anchor) {
        GuidesCommon.openGuide((Player)Minecraft.getInstance().player, (ResourceLocation)this.guide.getId(), (PageAnchor)anchor);
    }

    public Guide getGuide() {
        return this.guide;
    }

    static {
        MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping("key.ae2.mouse_wheel_item_modifier", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 340, "key.ae2.category");
        PART_PLACEMENT_OPPOSITE = new KeyMapping("key.ae2.part_placement_opposite", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 341, "key.ae2.category");
    }
}

