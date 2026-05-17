/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.Sheep
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.common.util.BlockSnapshot
 *  net.neoforged.neoforge.event.EventHooks
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.items.tools.powered;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEDamageTypes;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.core.network.clientbound.MatterCannonPacket;
import appeng.items.contents.CellConfig;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.LookDirection;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatterCannonItem
extends AEBasePoweredItem
implements IBasicCellItem {
    private static final Logger LOG = LoggerFactory.getLogger(MatterCannonItem.class);
    private static final int ENERGY_PER_SHOT = 1600;

    public MatterCannonItem(Item.Properties props) {
        super(AEConfig.instance().getMatterCannonBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800.0 + 800.0 * (double)Upgrades.getEnergyCardMultiplier(this.getUpgrades(stack));
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        this.addCellInformationToTooltip(stack, lines);
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return this.getCellTooltipImage(stack);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        LookDirection direction;
        ItemStack stack = p.getItemInHand(hand);
        if (this.fireCannon(level, stack, p, direction = InteractionUtil.getPlayerRay(p, 255.0))) {
            return new InteractionResultHolder(InteractionResult.sidedSuccess((boolean)level.isClientSide()), (Object)stack);
        }
        return new InteractionResultHolder(InteractionResult.FAIL, (Object)stack);
    }

    public boolean fireCannon(Level level, ItemStack stack, Player player, LookDirection dir) {
        Object object;
        StorageCell inv = StorageCells.getCellInventory(stack, null);
        if (inv == null) {
            return false;
        }
        KeyCounter itemList = inv.getAvailableStacks();
        Object2LongMap.Entry<AEKey> req = itemList.getFirstEntry(AEItemKey.class);
        if (req == null || !((object = req.getKey()) instanceof AEItemKey)) {
            if (!level.isClientSide()) {
                player.displayClientMessage((Component)PlayerMessages.AmmoDepleted.text(), true);
            }
            return true;
        }
        AEItemKey itemKey = (AEItemKey)object;
        int shotPower = 1;
        IUpgradeInventory cu = this.getUpgrades(stack);
        if (cu != null) {
            shotPower += cu.getInstalledUpgrades(AEItems.SPEED_CARD);
        }
        shotPower = Math.min(shotPower, (int)req.getLongValue());
        if (this.getAECurrentPower(stack) < 1600.0) {
            return false;
        }
        shotPower = Math.min(shotPower, (int)this.getAECurrentPower(stack) / 1600);
        this.extractAEPower(stack, 1600 * shotPower, Actionable.MODULATE);
        if (level.isClientSide()) {
            return true;
        }
        long aeAmmo = inv.extract((AEKey)req.getKey(), 1L, Actionable.MODULATE, new PlayerSource(player));
        if (aeAmmo == 0L) {
            return true;
        }
        Vec3 rayFrom = dir.getA();
        Vec3 rayTo = dir.getB();
        Vec3 direction = rayTo.subtract(rayFrom);
        direction.normalize();
        double x = rayFrom.x;
        double y = rayFrom.y;
        double z = rayFrom.z;
        float penetration = this.getPenetration(itemKey) * (float)shotPower;
        if (penetration <= 0.0f) {
            Item item = itemKey.getItem();
            if (item instanceof PaintBallItem) {
                PaintBallItem paintBallItem = (PaintBallItem)item;
                this.shootPaintBalls(paintBallItem.getColor(), paintBallItem.isLumen(), level, player, rayFrom, rayTo, direction, x, y, z);
                return true;
            }
        } else {
            this.standardAmmo(penetration, level, player, rayFrom, rayTo, direction, x, y, z);
        }
        return true;
    }

    private void shootPaintBalls(AEColor color, boolean lit, Level level, @Nullable Player p, Vec3 Vector3d, Vec3 Vector3d1, Vec3 direction, double d0, double d1, double d2) {
        AABB bb = new AABB(Math.min(Vector3d.x, Vector3d1.x), Math.min(Vector3d.y, Vector3d1.y), Math.min(Vector3d.z, Vector3d1.z), Math.max(Vector3d.x, Vector3d1.x), Math.max(Vector3d.y, Vector3d1.y), Math.max(Vector3d.z, Vector3d1.z)).inflate(16.0, 16.0, 16.0);
        Entity entity = null;
        Vec3 entityIntersection = null;
        List list = level.getEntities((Entity)p, bb, e -> !(e instanceof ItemEntity) && e.isAlive());
        double closest = 9999999.0;
        for (Entity entity1 : list) {
            double nd;
            if (p.isPassenger() && entity1.hasPassenger((Entity)p)) continue;
            float f1 = 0.3f;
            AABB boundingBox = entity1.getBoundingBox().inflate((double)0.3f, (double)0.3f, (double)0.3f);
            Vec3 intersection = boundingBox.clip(Vector3d, Vector3d1).orElse(null);
            if (intersection == null || !((nd = Vector3d.distanceToSqr(intersection)) < closest)) continue;
            entity = entity1;
            entityIntersection = intersection;
            closest = nd;
        }
        ClipContext rayTraceContext = new ClipContext(Vector3d, Vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p);
        BlockHitResult pos = level.clip(rayTraceContext);
        Vec3 vec = new Vec3(d0, d1, d2);
        if (entity != null && pos.getType() != HitResult.Type.MISS && pos.getLocation().distanceToSqr(vec) > closest) {
            pos = new EntityHitResult(entity, entityIntersection);
        } else if (entity != null && pos.getType() == HitResult.Type.MISS) {
            pos = new EntityHitResult(entity, entityIntersection);
        }
        AppEng.instance().sendToAllNearExcept(null, d0, d1, d2, 256.0, level, new MatterCannonPacket(d0, d1, d2, (float)direction.x, (float)direction.y, (float)direction.z, (byte)(pos.getType() == HitResult.Type.MISS ? 32.0 : pos.getLocation().distanceToSqr(vec) + 1.0)));
        if (pos.getType() != HitResult.Type.MISS) {
            if (pos instanceof EntityHitResult) {
                EntityHitResult entityResult = (EntityHitResult)pos;
                Entity entityHit = entityResult.getEntity();
                if (entityHit instanceof Sheep) {
                    Sheep sh = (Sheep)entityHit;
                    sh.setColor(color.dye);
                }
                entityHit.hurt(level.damageSources().playerAttack(p), 0.0f);
            } else if (pos instanceof BlockHitResult) {
                BlockEntity te;
                BlockHitResult blockResult = pos;
                Direction side = blockResult.getDirection();
                BlockPos hitPos = blockResult.getBlockPos().relative(side);
                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitPos), p)) {
                    return;
                }
                if (EventHooks.onBlockPlace((Entity)p, (BlockSnapshot)BlockSnapshot.create((ResourceKey)p.level().dimension(), (LevelAccessor)level, (BlockPos)hitPos), (Direction)blockResult.getDirection())) {
                    return;
                }
                BlockState whatsThere = level.getBlockState(hitPos);
                if (whatsThere.canBeReplaced() && level.isEmptyBlock(hitPos)) {
                    level.setBlock(hitPos, AEBlocks.PAINT.block().defaultBlockState(), 3);
                }
                if ((te = level.getBlockEntity(hitPos)) instanceof PaintSplotchesBlockEntity) {
                    Vec3 hp = pos.getLocation().subtract((double)hitPos.getX(), (double)hitPos.getY(), (double)hitPos.getZ());
                    ((PaintSplotchesBlockEntity)te).addBlot(color, lit, side.getOpposite(), hp);
                }
            }
        }
    }

    private void standardAmmo(float penetration, Level level, Player p, Vec3 Vector3d, Vec3 Vector3d1, Vec3 direction, double d0, double d1, double d2) {
        boolean hasDestroyed = true;
        while (penetration > 0.0f && hasDestroyed) {
            hasDestroyed = false;
            AABB bb = new AABB(Math.min(Vector3d.x, Vector3d1.x), Math.min(Vector3d.y, Vector3d1.y), Math.min(Vector3d.z, Vector3d1.z), Math.max(Vector3d.x, Vector3d1.x), Math.max(Vector3d.y, Vector3d1.y), Math.max(Vector3d.z, Vector3d1.z)).inflate(16.0, 16.0, 16.0);
            Entity entity = null;
            Vec3 entityIntersection = null;
            List list = level.getEntities((Entity)p, bb, e -> !(e instanceof ItemEntity) && e.isAlive());
            double closest = 9999999.0;
            for (Entity entity1 : list) {
                double nd;
                if (p.isPassenger() && entity1.hasPassenger((Entity)p)) continue;
                float f1 = 0.3f;
                AABB boundingBox = entity1.getBoundingBox().inflate((double)0.3f, (double)0.3f, (double)0.3f);
                Vec3 intersection = boundingBox.clip(Vector3d, Vector3d1).orElse(null);
                if (intersection == null || !((nd = Vector3d.distanceToSqr(intersection)) < closest)) continue;
                entity = entity1;
                entityIntersection = intersection;
                closest = nd;
            }
            ClipContext rayTraceContext = new ClipContext(Vector3d, Vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p);
            Vec3 vec = new Vec3(d0, d1, d2);
            BlockHitResult pos = level.clip(rayTraceContext);
            if (entity != null && pos.getType() != HitResult.Type.MISS && pos.getLocation().distanceToSqr(vec) > closest) {
                pos = new EntityHitResult(entity, entityIntersection);
            } else if (entity != null && pos.getType() == HitResult.Type.MISS) {
                pos = new EntityHitResult(entity, entityIntersection);
            }
            AppEng.instance().sendToAllNearExcept(null, d0, d1, d2, 256.0, level, new MatterCannonPacket(d0, d1, d2, (float)direction.x, (float)direction.y, (float)direction.z, (byte)(pos.getType() == HitResult.Type.MISS ? 32.0 : pos.getLocation().distanceToSqr(vec) + 1.0)));
            if (pos.getType() == HitResult.Type.MISS) continue;
            DamageSource dmgSrc = level.damageSources().source(AEDamageTypes.MATTER_CANNON, (Entity)p);
            if (pos instanceof EntityHitResult) {
                EntityHitResult entityResult = (EntityHitResult)pos;
                Entity entityHit = entityResult.getEntity();
                int dmg = MatterCannonItem.getDamageFromPenetration(penetration);
                if (entityHit instanceof LivingEntity) {
                    LivingEntity el = (LivingEntity)entityHit;
                    penetration -= (float)dmg;
                    if (!el.hurt(dmgSrc, (float)dmg)) continue;
                    el.knockback(0.0, -direction.x, -direction.z);
                    if (el.isAlive()) continue;
                    hasDestroyed = true;
                    continue;
                }
                if (entityHit instanceof ItemEntity) {
                    hasDestroyed = true;
                    entityHit.discard();
                    continue;
                }
                if (!entityHit.hurt(dmgSrc, (float)dmg)) continue;
                hasDestroyed = !entityHit.isAlive();
                continue;
            }
            if (!(pos instanceof BlockHitResult)) continue;
            BlockHitResult blockResult = pos;
            if (!AEConfig.instance().isMatterCanonBlockDamageEnabled()) {
                penetration = 0.0f;
                continue;
            }
            BlockPos blockPos = blockResult.getBlockPos();
            BlockState bs = level.getBlockState(blockPos);
            float hardness = bs.getDestroySpeed((BlockGetter)level, blockPos) * 9.0f;
            if (!((double)hardness >= 0.0) || !(penetration > hardness) || !this.canDestroyBlock(level, blockPos, p)) continue;
            hasDestroyed = true;
            penetration -= hardness;
            penetration *= 0.6f;
            level.destroyBlock(blockPos, true);
        }
    }

    private boolean canDestroyBlock(Level level, BlockPos pos, Player player) {
        if (!Platform.hasPermissions(new DimensionalBlockPos(level, pos), player)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        return !((BlockEvent.BreakEvent)NeoForge.EVENT_BUS.post((Event)event)).isCanceled();
    }

    public static int getDamageFromPenetration(float penetration) {
        return (int)Math.ceil(penetration / 20.0f);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 4, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        this.setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(AEKeyType.items()), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return (FuzzyMode)((Object)is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)FuzzyMode.IGNORE_ALL));
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, (Object)fzMode);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        if (requestedAddition instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)requestedAddition;
            float pen = this.getPenetration(itemKey);
            if (pen > 0.0f) {
                return false;
            }
            return !(itemKey.getItem() instanceof PaintBallItem);
        }
        return true;
    }

    private float getPenetration(AEItemKey what) {
        MinecraftServer server = AppEng.instance().getCurrentServer();
        if (server == null) {
            LOG.warn("Tried to get penetration of matter cannon ammo for {} while no server was running", (Object)what);
            return 0.0f;
        }
        Collection recipes = server.getRecipeManager().byType(AERecipeTypes.MATTER_CANNON_AMMO);
        for (RecipeHolder holder : recipes) {
            MatterCannonAmmo ammoRecipe = (MatterCannonAmmo)holder.value();
            if (!what.matches(ammoRecipe.getAmmo())) continue;
            return ammoRecipe.getWeight();
        }
        return 0.0f;
    }

    @Override
    public boolean storableInStorageCell() {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }
}

