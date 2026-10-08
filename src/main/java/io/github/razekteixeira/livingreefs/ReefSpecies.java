package io.github.razekteixeira.livingreefs;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import io.github.razekteixeira.livingreefs.core.SpeciesTable;

/** The species table for the blocks of this game, with fast lookups from {@link Block} objects. */
public final class ReefSpecies {
	private static volatile @Nullable ReefSpecies current;

	private final SpeciesTable table;
	private final Map<Block, String> idsByBlock = new IdentityHashMap<>();

	private ReefSpecies(SpeciesTable table) {
		this.table = table;
	}

	/** The table for the registered blocks, built on first use (blocks are registered at startup and never change). */
	public static ReefSpecies get() {
		ReefSpecies species = current;
		if (species == null) {
			species = build();
			current = species;
		}
		return species;
	}

	private static ReefSpecies build() {
		List<String> ids = new ArrayList<>();
		for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
			ids.add(id.toString());
		}
		ReefSpecies species = new ReefSpecies(SpeciesTable.of(ids));
		for (Block block : BuiltInRegistries.BLOCK) {
			String id = BuiltInRegistries.BLOCK.getKey(block).toString();
			if (species.table.lookup(id).isPresent()) {
				species.idsByBlock.put(block, id);
			}
		}
		LivingReefs.LOGGER.info("Living Reefs knows {} coral blocks", species.idsByBlock.size());
		return species;
	}

	public SpeciesTable table() {
		return table;
	}

	/** The coral id of a block, or null when the block is not paired coral. */
	public @Nullable String coralId(Block block) {
		return idsByBlock.get(block);
	}

	public Optional<SpeciesTable.Entry> entry(Block block) {
		String id = idsByBlock.get(block);
		return id == null ? Optional.empty() : table.lookup(id);
	}

	public static Optional<Block> block(String id) {
		Identifier identifier = Identifier.tryParse(id);
		return identifier == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(identifier);
	}
}
