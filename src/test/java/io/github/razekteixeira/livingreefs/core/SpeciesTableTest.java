package io.github.razekteixeira.livingreefs.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class SpeciesTableTest {
	static final List<String> TYPES = List.of("tube", "brain", "bubble", "fire", "horn");

	/** Every vanilla coral id, living and dead, plus some unrelated blocks. */
	static List<String> vanillaIds() {
		List<String> ids = new ArrayList<>(List.of("minecraft:stone", "minecraft:sand", "minecraft:water",
				"minecraft:dead_bush", "minecraft:sea_pickle", "minecraft:kelp"));
		for (String type : TYPES) {
			for (String suffix : List.of("_coral_block", "_coral", "_coral_fan", "_coral_wall_fan")) {
				ids.add("minecraft:" + type + suffix);
				ids.add("minecraft:dead_" + type + suffix);
			}
		}
		return ids;
	}

	@Test
	void pairsEveryVanillaCoral() {
		SpeciesTable table = SpeciesTable.of(vanillaIds());
		assertEquals(40, table.size());
		assertEquals(Optional.of(new SpeciesTable.Entry("minecraft:tube", CoralForm.WALL_FAN, false)),
				table.lookup("minecraft:tube_coral_wall_fan"));
		assertEquals(Optional.of(new SpeciesTable.Entry("minecraft:fire", CoralForm.FAN, true)),
				table.lookup("minecraft:dead_fire_coral_fan"));
		assertEquals(Optional.of(new SpeciesTable.Entry("minecraft:brain", CoralForm.PLANT, false)),
				table.lookup("minecraft:brain_coral"));
		assertEquals(Optional.of(new SpeciesTable.Entry("minecraft:horn", CoralForm.BLOCK, true)),
				table.lookup("minecraft:dead_horn_coral_block"));
	}

	@Test
	void twinsGoBothWays() {
		SpeciesTable table = SpeciesTable.of(vanillaIds());
		assertEquals(Optional.of("minecraft:dead_bubble_coral_block"), table.twin("minecraft:bubble_coral_block"));
		assertEquals(Optional.of("minecraft:bubble_coral_wall_fan"), table.twin("minecraft:dead_bubble_coral_wall_fan"));
		assertEquals(Optional.of("minecraft:horn_coral_fan"), table.id("minecraft:horn", CoralForm.FAN, false));
	}

	@Test
	void ignoresBlocksThatAreNotCoral() {
		SpeciesTable table = SpeciesTable.of(vanillaIds());
		assertTrue(table.lookup("minecraft:dead_bush").isEmpty());
		assertTrue(table.lookup("minecraft:sea_pickle").isEmpty());
		assertTrue(table.lookup("minecraft:stone").isEmpty());
	}

	@Test
	void pairsModdedCoralThatFollowsThePattern() {
		SpeciesTable table = SpeciesTable.of(List.of("reefmod:rose_coral", "reefmod:dead_rose_coral",
				"reefmod:rose_coral_block", "reefmod:dead_rose_coral_block"));
		assertEquals(Optional.of("reefmod:dead_rose_coral"), table.twin("reefmod:rose_coral"));
		assertEquals(Optional.of(new SpeciesTable.Entry("reefmod:rose", CoralForm.BLOCK, false)),
				table.lookup("reefmod:rose_coral_block"));
	}

	@Test
	void formsWithoutBothTwinsAreLeftOut() {
		// No dead fan: the living fan must not be listed, or bleaching it would have no block to become.
		SpeciesTable table = SpeciesTable.of(List.of("reefmod:rose_coral", "reefmod:dead_rose_coral", "reefmod:rose_coral_fan"));
		assertTrue(table.lookup("reefmod:rose_coral_fan").isEmpty());
		assertTrue(table.id("reefmod:rose", CoralForm.FAN, false).isEmpty());
		assertEquals(2, table.size());
	}

	@Test
	void parseRejectsMalformedIds() {
		assertTrue(SpeciesTable.parse("minecraft:_coral").isEmpty());
		assertTrue(SpeciesTable.parse("coral_block").isEmpty());
		assertTrue(SpeciesTable.parse("minecraft:dead_dead_tube_coral").isEmpty());
		assertTrue(SpeciesTable.parse(":tube_coral").isEmpty());
	}
}
