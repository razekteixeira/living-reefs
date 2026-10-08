package io.github.razekteixeira.livingreefs.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class SettingsTest {
	private static Settings parse(String json) {
		return Settings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
	}

	@Test
	void emptyFileMeansDefaults() {
		assertEquals(Settings.DEFAULTS, parse("{}"));
	}

	@Test
	void partialFileKeepsOtherDefaults() {
		Settings s = parse("{\"growthChance\": 0.5, \"boosts\": {\"conduit\": 3}, \"permissionLevels\": {\"admin\": 2}}");
		assertEquals(0.5, s.growthChance());
		assertEquals(3.0, s.boosts().conduit());
		assertEquals(Settings.DEFAULTS.boosts().seaPickles(), s.boosts().seaPickles());
		assertEquals(2, s.permissionLevels().admin());
		assertEquals(Settings.DEFAULTS.permissionLevels().use(), s.permissionLevels().use());
		assertEquals(Settings.DEFAULTS.maxNearbyCoral(), s.maxNearbyCoral());
	}

	@Test
	void outOfRangeValuesAreClampedWithAWarningEach() {
		Settings.Checked checked = parse("{\"growthChance\": 2, \"bleachChance\": -1, \"spreadRadius\": 0, \"minLight\": 40,"
				+ " \"boosts\": {\"tropicalFish\": 0.5}, \"permissionLevels\": {\"use\": 9}}").checked();
		Settings s = checked.settings();
		assertEquals(1.0, s.growthChance());
		assertEquals(0.0, s.bleachChance());
		assertEquals(1, s.spreadRadius());
		assertEquals(15, s.minLight());
		assertEquals(1.0, s.boosts().tropicalFish());
		assertEquals(4, s.permissionLevels().use());
		assertEquals(6, checked.warnings().size(), checked.warnings().toString());
	}

	@Test
	void notANumberIsClampedNotKept() {
		Settings nan = new Settings(Double.NaN, 0.04, 0.25, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true, Settings.PermissionLevels.DEFAULTS);
		Settings.Checked checked = nan.checked();
		assertEquals(0.0, checked.settings().growthChance());
		assertEquals(1, checked.warnings().size());
	}

	@Test
	void defaultsNeedNoCorrection() {
		assertTrue(Settings.DEFAULTS.checked().warnings().isEmpty());
		assertEquals(Settings.DEFAULTS, Settings.DEFAULTS.checked().settings());
	}

	@Test
	void writeCodecRoundTripsEveryField() {
		Settings custom = new Settings(0.1, 0.2, 0.3, 3, 4, 20, 5, 6, new Settings.Boosts(2, 3, 4), false,
				new Settings.PermissionLevels(1, 2));
		var json = Settings.WRITE_CODEC.encodeStart(JsonOps.INSTANCE, custom).getOrThrow();
		assertEquals(custom, Settings.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
		// The default instance is written in full, so the generated file documents every option.
		String defaults = Settings.WRITE_CODEC.encodeStart(JsonOps.INSTANCE, Settings.DEFAULTS).getOrThrow().toString();
		for (String option : new String[] {"growthChance", "recoveryChance", "bleachChance", "spreadRadius", "minWaterAbove",
				"maxNearbyCoral", "maxReefHeight", "minLight", "tropicalFish", "seaPickles", "conduit", "particles", "use", "admin"}) {
			assertTrue(defaults.contains("\"" + option + "\""), option + " in " + defaults);
		}
	}
}
