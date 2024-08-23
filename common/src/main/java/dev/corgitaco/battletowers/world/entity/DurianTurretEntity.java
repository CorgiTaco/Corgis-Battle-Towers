package dev.corgitaco.battletowers.world.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DurianTurretEntity extends Mob implements GeoEntity, RangedAttackMob {

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);

    protected DurianTurretEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.lookControl = new DurianTurrentLookControl(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0, 30, 70, 255));
    }

    @Override
    public int getMaxHeadYRot() {
        return super.getMaxHeadYRot();
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();
    }

    @Override
    public float getEyeHeight(Pose pose) {
        return 0.01F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }


    @Override
    protected BodyRotationControl createBodyControl() {
        return new DurianTurretBodyRotationControl(this);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float v) {

        DurianBombEntity durianBombEntity = CBTEntityTypes.DURIAN_BOMB.get().create(level());

        Vec3 spawnPos = position();
        durianBombEntity.setPos(spawnPos);
        durianBombEntity.shootFromRotation(this, this.getXRot(), this.getYHeadRot(), 0, 0.2F, 3);

        Vec3 velocity = durianBombEntity.getDeltaMovement();
        durianBombEntity.xPower = velocity.x;
        durianBombEntity.yPower = velocity.y;
        durianBombEntity.zPower = velocity.z;

        durianBombEntity.setDeltaMovement(Vec3.ZERO);
        level().addFreshEntity(durianBombEntity);
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 deltaMovement) {
    }

    private static class DurianTurretBodyRotationControl extends BodyRotationControl {

        public DurianTurretBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {

        }
    }

    private static class DurianTurrentLookControl extends LookControl {

        public DurianTurrentLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void setLookAt(Entity entity, float deltaYaw, float deltaPitch) {
            super.setLookAt(entity, deltaYaw, this.mob.getMaxHeadXRot());
        }
    }
}
