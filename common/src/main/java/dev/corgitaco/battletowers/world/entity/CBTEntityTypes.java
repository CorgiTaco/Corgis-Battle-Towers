package dev.corgitaco.battletowers.world.entity;

import dev.corgitaco.battletowers.platform.RegistrationService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CBTEntityTypes {

    public static final Supplier<EntityType<DurianTurretEntity>> DURIAN_TURRET = create("durian_turret", () -> EntityType.Builder.of(DurianTurretEntity::new, MobCategory.MISC).clientTrackingRange(256).sized(2, 1).canSpawnFarFromPlayer());
    public static final Supplier<EntityType<DurianBombEntity>> DURIAN_BOMB = create("durian_bomb", () -> EntityType.Builder.of(DurianBombEntity::new, MobCategory.MISC).clientTrackingRange(256).sized(0.5F, 0.5F).canSpawnFarFromPlayer());
    public static final Supplier<EntityType<DurianStinkEntity>> DURIAN_STINK = create("durian_stink", () -> EntityType.Builder.of(DurianStinkEntity::new, MobCategory.MISC).clientTrackingRange(256).sized(2, 1).canSpawnFarFromPlayer());

    public static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> consumer) {
        consumer.accept(DURIAN_TURRET.get(), Mob.createMobAttributes().add(Attributes.FOLLOW_RANGE, 255).build());
    }


    private static <T extends Entity> Supplier<EntityType<T>> create(String id, Supplier<EntityType.Builder<T>> builder) {
        return RegistrationService.INSTANCE.register(BuiltInRegistries.ENTITY_TYPE, id, (Supplier) () -> builder.get().build(id));
    }

    public static void entityTypes() {
    }
}
