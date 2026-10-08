package io.github.razekteixeira.livingreefs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/** The data-driven part of the rules. Defaults ship in {@code data/livingreefs/tags}; datapacks can change them. */
public final class ReefTags {
	/** Biomes whose water lets coral thrive. */
	public static final TagKey<Biome> REEF_WATERS = TagKey.create(Registries.BIOME, id("reef_waters"));
	/** Biomes whose water bleaches coral. */
	public static final TagKey<Biome> FROZEN_WATERS = TagKey.create(Registries.BIOME, id("frozen_waters"));
	/** Full blocks new coral can grow on. */
	public static final TagKey<Block> REEF_SUBSTRATE = TagKey.create(Registries.BLOCK, id("reef_substrate"));
	/** Blocks that bleach coral within two blocks. */
	public static final TagKey<Block> REEF_HEAT_SOURCES = TagKey.create(Registries.BLOCK, id("reef_heat_sources"));

	private ReefTags() {
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(LivingReefs.MOD_ID, path);
	}
}
