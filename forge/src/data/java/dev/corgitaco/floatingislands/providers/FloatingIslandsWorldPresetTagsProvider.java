package dev.corgitaco.floatingislands.providers;

import java.util.concurrent.CompletableFuture;

import dev.corgitaco.floatingislands.Constants;
import dev.corgitaco.floatingislands.core.world.data.FloatingIslandWorldPresets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FloatingIslandsWorldPresetTagsProvider extends TagsProvider<WorldPreset> {
	public FloatingIslandsWorldPresetTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, ExistingFileHelper existingFileHelper) {
		super(packOutput, Registries.WORLD_PRESET, completableFuture, Constants.MOD_ID, existingFileHelper);
	}

	protected void addTags(HolderLookup.Provider provider) {
		this.tag(WorldPresetTags.NORMAL).add(FloatingIslandWorldPresets.FLOATING_ISLANDS);
	}
}
