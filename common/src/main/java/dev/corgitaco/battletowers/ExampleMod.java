package dev.corgitaco.battletowers;

import com.mojang.logging.LogUtils;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructurePieceTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructureTypes;
import dev.corgitaco.battletowers.world.level.levelgen.structure.CBTStructures;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ExampleMod {

    /** The mod id for  examplemod. */
    public static final String MOD_ID = "examplemod";

    /** The logger for examplemod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Initializes the mod.
     */
    public static void init() {
        CBTStructureTypes.structureTypes();
        CBTStructurePieceTypes.init();

    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
