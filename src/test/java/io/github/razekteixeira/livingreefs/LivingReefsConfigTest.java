package io.github.razekteixeira.livingreefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.razekteixeira.livingreefs.core.Settings;

class LivingReefsConfigTest {
	@AfterEach
	void restoreDefaults() {
		LivingReefsConfig.set(Settings.DEFAULTS);
	}

	@Test
	void missingFileIsCreatedWithDefaults(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("config/livingreefs.json");
		String status = LivingReefsConfig.load(file);
		assertTrue(status.startsWith("Wrote default settings"), status);
		String written = Files.readString(file);
		for (String option : new String[] {"growthChance", "recoveryChance", "bleachChance", "spreadRadius", "minWaterAbove",
				"maxNearbyCoral", "maxReefHeight", "minLight", "boosts", "particles", "permissionLevels"}) {
			assertTrue(written.contains("\"" + option + "\""), "generated file documents " + option + ":\n" + written);
		}
		assertEquals(Settings.DEFAULTS, LivingReefsConfig.get());
	}

	@Test
	void outOfRangeValuesAreClampedOnLoad(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("livingreefs.json");
		Files.writeString(file, "{\"spreadRadius\": 99}");
		String status = LivingReefsConfig.load(file);
		assertTrue(status.contains("1 corrected value"), status);
		assertEquals(4, LivingReefsConfig.get().spreadRadius());
	}

	@Test
	void brokenFileKeepsPreviousSettingsAndIsNotOverwritten(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("livingreefs.json");
		Files.writeString(file, "{\"growthChance\": 0.3}");
		LivingReefsConfig.load(file);
		assertEquals(0.3, LivingReefsConfig.get().growthChance());

		String broken = "{\"growthChance\": 0.9,, oops";
		Files.writeString(file, broken);
		String status = LivingReefsConfig.load(file);

		assertTrue(status.startsWith("Could not read"), status);
		assertEquals(0.3, LivingReefsConfig.get().growthChance(), "previous settings stay active");
		assertEquals(broken, Files.readString(file), "the owner's file is never overwritten");
	}

	@Test
	void wrongTypeIsReportedNotIgnored(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("livingreefs.json");
		Files.writeString(file, "{\"spreadRadius\": \"wide\"}");
		assertTrue(LivingReefsConfig.load(file).startsWith("Could not read"));
	}
}
