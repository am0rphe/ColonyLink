/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.integration.modules.igtooltip;

import appeng.api.integrations.igtooltip.BaseClassRegistration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseClassRegistrationImpl
implements BaseClassRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(BaseClassRegistrationImpl.class);
    private final List<BaseClass> baseClasses = new ArrayList<BaseClass>();
    private final Set<BaseClass> partHostClasses = new HashSet<BaseClass>();

    @Override
    public void addBaseBlockEntity(Class<? extends BlockEntity> blockEntityClass, Class<? extends Block> blockClass) {
        BaseClass defaultClass = new BaseClass(blockEntityClass, blockClass);
        for (BaseClass registeredClass : this.baseClasses) {
            if (!registeredClass.isSuperclassOf(defaultClass)) continue;
            LOG.info("Not registering {}, because superclass {} is already registered.", (Object)defaultClass, (Object)registeredClass);
            return;
        }
        this.baseClasses.removeIf(otherClass -> {
            if (defaultClass.isSuperclassOf((BaseClass)otherClass)) {
                LOG.info("Replacing default server-data registration for {} with superclass {}.", (Object)defaultClass, otherClass);
                return true;
            }
            return false;
        });
        this.baseClasses.add(defaultClass);
    }

    @Override
    public <T extends BlockEntity> void addPartHost(Class<T> blockEntityClass, Class<? extends Block> blockClass) {
        this.partHostClasses.add(new BaseClass(blockEntityClass, blockClass));
    }

    public List<BaseClass> getBaseClasses() {
        return this.baseClasses;
    }

    public Set<BaseClass> getPartHostClasses() {
        return this.partHostClasses;
    }

    public record BaseClass(Class<? extends BlockEntity> blockEntity, Class<? extends Block> block) {
        public boolean isSuperclassOf(BaseClass other) {
            return this.blockEntity.isAssignableFrom(other.blockEntity);
        }
    }
}

