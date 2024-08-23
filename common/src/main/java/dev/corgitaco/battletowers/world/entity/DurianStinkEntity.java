package dev.corgitaco.battletowers.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DurianStinkEntity extends Entity {
    public DurianStinkEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        float radius = 5;

        if (!level().isClientSide) {
            for (Entity entity : level().getEntities(this, getBoundingBox().inflate(radius))) {
                if (!(entity instanceof DurianTurretEntity)) {
                    entity.hurt(damageSources().generic(), 5);
                }
            }
        } else {
            if (random.nextDouble() < 0.3) {
                for (int i = 0; i < random.nextIntBetweenInclusive(10, 25); i++) {
                    float randX = Mth.randomBetween(random, -radius, radius);
                    float randZ = Mth.randomBetween(random, -radius, radius);
                    level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX() + randX, getY(), getZ() + randZ, 0, 0.1, 0);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }
}
