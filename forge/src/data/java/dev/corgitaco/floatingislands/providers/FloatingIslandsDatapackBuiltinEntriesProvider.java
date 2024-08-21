package dev.corgitaco.floatingislands.providers;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import corgitaco.corgilib.serialization.jankson.JanksonJsonOps;
import corgitaco.corgilib.shadow.blue.endless.jankson.JsonElement;
import dev.corgitaco.floatingislands.Constants;
import dev.corgitaco.floatingislands.level.gen.FloatingIslandGeneratorSettings;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.apache.commons.lang3.mutable.MutableObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class FloatingIslandsDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {
    private final Predicate<String> check;

    public FloatingIslandsDatapackBuiltinEntriesProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture, RegistrySetBuilder registrySetBuilder, Set<String> modids) {
        super(packOutput, providerCompletableFuture, registrySetBuilder, modids);
        this.check = modids::contains;
    }

    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose(registryProvider -> {
            DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JanksonJsonOps.INSTANCE, registryProvider);
            // Gather all CompletableFutures from dumping registry data
            CompletableFuture<?>[] futures = DataPackRegistriesHooks.getDataPackRegistriesWithDimensions()
                    .flatMap(registryData -> dumpRegistryData(output, registryProvider, dynamicOps, registryData).stream())
                    .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    public <T, E> Optional<CompletableFuture<?>> dumpRegistryData(CachedOutput output, HolderLookup.Provider registryProvider, DynamicOps<E> dynamicOps, RegistryDataLoader.RegistryData<T> registryData) {
        ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
        return registryProvider.lookup(resourceKey).map(registryLookup -> {
            PackOutput.PathProvider pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, ForgeHooks.prefixNamespace(resourceKey.location()));
            CompletableFuture<?>[] futures = registryLookup.listElements()
                    .filter(holder -> this.check.test(holder.key().location().getNamespace()))
                    .map(holder -> dumpValue(pathProvider.json(holder.key().location()), output, dynamicOps, registryData.elementCodec(), holder.value(), registryProvider))
                    .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    private static <T, E> CompletableFuture<?> dumpValue(Path path, CachedOutput output, DynamicOps<E> dynamicOps, Codec<T> encoder, T value, HolderLookup.Provider provider) {
        Optional<E> encodedElement = encoder.encodeStart(dynamicOps, value).resultOrPartial(errorMsg -> {
            Constants.LOG.error("Couldn't serialize element {}: {}", path, errorMsg);
        });

        E e = encodedElement.orElseThrow();

        String result;

        if (value instanceof FloatingIslandGeneratorSettings settings) {
            String s = "";
        }

        if (e instanceof JsonElement jsonElement) {
            result = jsonElement.toJson(true, true);
        } else {
            result = e.toString();
        }

        return saveStable(output, result, path);
    }


    static CompletableFuture<?> saveStable(CachedOutput cachedOutput, String value, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
                 OutputStreamWriter writer = new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8)) {

                writer.write(value);

                writer.flush();

                cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
            } catch (IOException e) {
                Constants.LOG.error("Failed to save file to {}", path, e);
            }
        }, Util.backgroundExecutor());
    }

}
