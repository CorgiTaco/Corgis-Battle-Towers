package dev.corgitaco.battletowers.client.entity.render;

import dev.corgitaco.battletowers.client.entity.render.model.TurretGeoModel;
import dev.corgitaco.battletowers.world.entity.CBTEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.function.BiConsumer;

public class CBTEntityRenderers {

    public static void registerEntityRenderers(BiConsumer<EntityType<? extends Entity>, EntityRendererProvider> consumer) {
        consumer.accept(CBTEntityTypes.DURIAN_TURRET.get(), context -> new GeoEntityRenderer(context, new TurretGeoModel(BuiltInRegistries.ENTITY_TYPE.getKey(CBTEntityTypes.DURIAN_TURRET.get()))));
        consumer.accept(CBTEntityTypes.DURIAN_BOMB.get(), context -> new GeoEntityRenderer(context, new TurretGeoModel(BuiltInRegistries.ENTITY_TYPE.getKey(CBTEntityTypes.DURIAN_BOMB.get()))));
        consumer.accept(CBTEntityTypes.DURIAN_STINK.get(), NoopRenderer::new);
    }
}
