/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 *  net.neoforged.neoforge.event.RegisterGameTestsEvent
 *  net.neoforged.neoforge.event.server.ServerAboutToStartEvent
 *  net.neoforged.neoforge.event.server.ServerStoppedEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.registries.NewRegistryEvent
 *  net.neoforged.neoforge.registries.RegistryBuilder
 *  net.neoforged.neoforge.server.ServerLifecycleHooks
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core;

import appeng.api.ids.AEComponents;
import appeng.api.parts.CableRenderMode;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.InitNetwork;
import appeng.hooks.SkyStoneBreakSpeed;
import appeng.hooks.WrenchHook;
import appeng.hooks.ticking.TickHandler;
import appeng.hotkeys.HotkeyActions;
import appeng.init.InitAdvancementTriggers;
import appeng.init.InitCapabilityProviders;
import appeng.init.InitCauldronInteraction;
import appeng.init.InitDispenserBehavior;
import appeng.init.InitMenuTypes;
import appeng.init.InitStats;
import appeng.init.InitVillager;
import appeng.init.client.InitParticleTypes;
import appeng.init.internal.InitBlockEntityMoveStrategies;
import appeng.init.internal.InitGridLinkables;
import appeng.init.internal.InitGridServices;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitStorageCells;
import appeng.init.internal.InitUpgrades;
import appeng.init.worldgen.InitStructures;
import appeng.integration.Integrations;
import appeng.recipes.AERecipeSerializers;
import appeng.recipes.AERecipeTypes;
import appeng.server.AECommand;
import appeng.server.services.ChunkLoadingService;
import appeng.server.testworld.GameTestPlotAdapter;
import appeng.sounds.AppEngSounds;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public abstract class AppEngBase
implements AppEng {
    private final ThreadLocal<Player> partInteractionPlayer = new ThreadLocal();
    static AppEngBase INSTANCE;

    public AppEngBase(IEventBus modEventBus, ModContainer container) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;
        AEConfig.register(container);
        InitGridServices.init();
        InitBlockEntityMoveStrategies.init();
        AEParts.init();
        AEBlocks.DR.register(modEventBus);
        AEItems.DR.register(modEventBus);
        AEBlockEntities.DR.register(modEventBus);
        AEComponents.DR.register(modEventBus);
        AEEntities.DR.register(modEventBus);
        AERecipeTypes.DR.register(modEventBus);
        AERecipeSerializers.DR.register(modEventBus);
        InitStructures.register(modEventBus);
        AEAttachmentTypes.register(modEventBus);
        modEventBus.addListener(this::registerRegistries);
        modEventBus.addListener(MainCreativeTab::initExternal);
        modEventBus.addListener(InitNetwork::init);
        modEventBus.addListener(ChunkLoadingService.getInstance()::register);
        modEventBus.addListener(EventPriority.HIGH, InitCapabilityProviders::markProxyableCapabilities);
        modEventBus.addListener(InitCapabilityProviders::register);
        modEventBus.addListener(EventPriority.LOWEST, InitCapabilityProviders::registerGenericAdapters);
        modEventBus.addListener(event -> {
            if (event.getRegistryKey() == Registries.SOUND_EVENT) {
                this.registerSounds((Registry<SoundEvent>)BuiltInRegistries.SOUND_EVENT);
            } else if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
                this.registerCreativeTabs((Registry<CreativeModeTab>)BuiltInRegistries.CREATIVE_MODE_TAB);
            } else if (event.getRegistryKey() == Registries.CUSTOM_STAT) {
                InitStats.init((Registry<ResourceLocation>)event.getRegistry(Registries.CUSTOM_STAT));
            } else if (event.getRegistryKey() == Registries.TRIGGER_TYPE) {
                InitAdvancementTriggers.init(event.getRegistry(Registries.TRIGGER_TYPE));
            } else if (event.getRegistryKey() == Registries.PARTICLE_TYPE) {
                InitParticleTypes.init(event.getRegistry(Registries.PARTICLE_TYPE));
            } else if (event.getRegistryKey() == Registries.MENU) {
                InitMenuTypes.init(event.getRegistry(Registries.MENU));
            } else if (event.getRegistryKey() == Registries.CHUNK_GENERATOR) {
                Registry.register((Registry)BuiltInRegistries.CHUNK_GENERATOR, (ResourceLocation)SpatialStorageDimensionIds.CHUNK_GENERATOR_ID, SpatialStorageChunkGenerator.CODEC);
            } else if (event.getRegistryKey() == Registries.VILLAGER_PROFESSION) {
                InitVillager.initProfession((Registry<VillagerProfession>)event.getRegistry(Registries.VILLAGER_PROFESSION));
            } else if (event.getRegistryKey() == Registries.POINT_OF_INTEREST_TYPE) {
                InitVillager.initPointOfInterestType((Registry<PoiType>)event.getRegistry(Registries.POINT_OF_INTEREST_TYPE));
            } else if (event.getRegistryKey() == AEKeyType.REGISTRY_KEY) {
                this.registerKeyTypes((Registry<AEKeyType>)event.getRegistry(AEKeyType.REGISTRY_KEY));
            }
        });
        NeoForge.EVENT_BUS.addListener(InitVillager::initTrades);
        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerTests);
        TickHandler.instance().init();
        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(WrenchHook::onPlayerUseBlockEvent);
        NeoForge.EVENT_BUS.addListener(SkyStoneBreakSpeed::handleBreakFaster);
        HotkeyActions.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                AELog.warn(err);
            }
        });
    }

    public void postRegistrationInitialization() {
        InitGridLinkables.init();
        InitStorageCells.init();
        InitP2PAttunements.init();
        InitCauldronInteraction.init();
        InitDispenserBehavior.init();
        InitUpgrades.init();
    }

    public void registerKeyTypes(Registry<AEKeyType> registry) {
        Registry.register(registry, (ResourceLocation)AEKeyType.items().getId(), (Object)AEKeyType.items());
        Registry.register(registry, (ResourceLocation)AEKeyType.fluids().getId(), (Object)AEKeyType.fluids());
    }

    public void registerCommands(RegisterCommandsEvent evt) {
        new AECommand().register((CommandDispatcher<CommandSourceStack>)evt.getDispatcher());
    }

    public void registerSounds(Registry<SoundEvent> registry) {
        AppEngSounds.register(registry);
    }

    public void registerRegistries(NewRegistryEvent e) {
        Registry registry = e.create(new RegistryBuilder(AEKeyType.REGISTRY_KEY).sync(true).maxId(127));
        AEKeyTypesInternal.setRegistry((Registry<AEKeyType>)registry);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent evt) {
        ChunkLoadingService.getInstance().onServerAboutToStart(evt);
    }

    private void serverStopping(ServerStoppingEvent event) {
        ChunkLoadingService.getInstance().onServerStopping(event);
    }

    private void serverStopped(ServerStoppedEvent event) {
        TickHandler.instance().shutdown();
    }

    public void registerCreativeTabs(Registry<CreativeModeTab> registry) {
        MainCreativeTab.init(registry);
        FacadeCreativeTab.init(registry);
    }

    @Override
    public Collection<ServerPlayer> getPlayers() {
        MinecraftServer server = this.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayers();
        }
        return Collections.emptyList();
    }

    @Override
    public void sendToAllNearExcept(Player p, double x, double y, double z, double dist, Level level, ClientboundPacket packet) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ServerPlayer except = null;
            if (p instanceof ServerPlayer) {
                except = (ServerPlayer)p;
            }
            PacketDistributor.sendToPlayersNear((ServerLevel)serverLevel, (ServerPlayer)except, (double)x, (double)y, (double)z, (double)dist, (CustomPacketPayload)packet, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    @Override
    public void setPartInteractionPlayer(Player player) {
        this.partInteractionPlayer.set(player);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return this.getCableRenderModeForPlayer(this.partInteractionPlayer.get());
    }

    @Override
    @Nullable
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    protected final CableRenderMode getCableRenderModeForPlayer(@Nullable Player player) {
        if (player != null && (AEItems.NETWORK_TOOL.is(player.getItemInHand(InteractionHand.MAIN_HAND)) || AEItems.NETWORK_TOOL.is(player.getItemInHand(InteractionHand.OFF_HAND)))) {
            return CableRenderMode.CABLE_VIEW;
        }
        return CableRenderMode.STANDARD;
    }

    private void registerTests(RegisterGameTestsEvent e) {
        if ("true".equals(System.getProperty("appeng.tests"))) {
            e.register(GameTestPlotAdapter.class);
        }
    }
}

