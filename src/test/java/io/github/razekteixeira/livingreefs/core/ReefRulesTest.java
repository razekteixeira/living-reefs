package io.github.razekteixeira.livingreefs.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.razekteixeira.livingreefs.core.ReefRules.Reason;

class ReefRulesTest {
	static final Settings S = Settings.DEFAULTS;

	/** A coral in warm, lit reef water with nothing special around. */
	static Conditions reef() {
		return new Conditions(true, WaterKind.REEF, 10, false, false, 0, 0);
	}

	@Test
	void reefWaterWithLightThrives() {
		ReefRules.Assessment a = ReefRules.assess(reef(), S);
		assertEquals(Health.THRIVING, a.health());
		assertEquals(S.growthChance(), a.growthChance(), 1e-12);
	}

	@Test
	void dormantOutsideReefWaters() {
		ReefRules.Assessment a = ReefRules.assess(new Conditions(true, WaterKind.NEUTRAL, 15, false, false, 0, 0), S);
		assertEquals(Health.DORMANT, a.health());
		assertEquals(0, a.growthChance());
		assertTrue(a.reasons().contains(Reason.NEUTRAL_WATER));
	}

	@Test
	void heatStresses() {
		ReefRules.Assessment a = ReefRules.assess(new Conditions(true, WaterKind.REEF, 15, true, false, 0, 0), S);
		assertEquals(Health.STRESSED, a.health());
		assertTrue(a.reasons().contains(Reason.HEAT));
	}

	@Test
	void frozenWaterStressesEvenWhenLit() {
		assertEquals(Health.STRESSED, ReefRules.assess(new Conditions(true, WaterKind.FROZEN, 15, false, false, 0, 0), S).health());
	}

	@Test
	void conduitMakesWaterReefAndProtects() {
		ReefRules.Assessment neutral = ReefRules.assess(new Conditions(true, WaterKind.NEUTRAL, 15, false, true, 0, 0), S);
		assertEquals(Health.THRIVING, neutral.health());
		assertTrue(neutral.reasons().contains(Reason.CONDUIT_WATER));

		ReefRules.Assessment heated = ReefRules.assess(new Conditions(true, WaterKind.REEF, 15, true, true, 0, 0), S);
		assertEquals(Health.DORMANT, heated.health());
		assertTrue(heated.reasons().contains(Reason.CONDUIT_SHELTER));
	}

	@Test
	void darkCoralIsDormant() {
		ReefRules.Assessment dark = ReefRules.assess(new Conditions(true, WaterKind.REEF, 0, false, false, 0, 0), S);
		assertEquals(Health.DORMANT, dark.health());
		assertTrue(dark.reasons().contains(Reason.TOO_DARK));
		// Exactly at the threshold is enough light.
		assertEquals(Health.THRIVING, ReefRules.assess(new Conditions(true, WaterKind.REEF, S.minLight(), false, false, 0, 0), S).health());
	}

	@Test
	void dryCoralIsLeftToVanilla() {
		ReefRules.Assessment a = ReefRules.assess(new Conditions(false, WaterKind.REEF, 15, true, false, 0, 0), S);
		assertEquals(Health.DORMANT, a.health());
		assertEquals(java.util.List.of(Reason.DRY), a.reasons());
	}

	@Test
	void boostsMultiplyGrowthChance() {
		double base = S.growthChance();
		assertEquals(base * 1.5, ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, false, false, 0, 3), S).growthChance(), 1e-12);
		assertEquals(base * 1.5, ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, false, false, 2, 0), S).growthChance(), 1e-12);
		assertEquals(base * 1.5 * 1.5 * 2.0, ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, false, true, 1, 1), S).growthChance(), 1e-12);
	}

	@Test
	void growthChanceNeverExceedsOne() {
		Settings high = new Settings(0.9, 0, 0, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true, Settings.PermissionLevels.DEFAULTS);
		assertEquals(1.0, ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, false, true, 1, 1), high).growthChance());
	}

	@Test
	void decideUsesTheRollBands() {
		ReefRules.Assessment thriving = ReefRules.assess(reef(), S);
		double g = thriving.growthChance();
		assertEquals(Action.GROW, ReefRules.decide(thriving, S, g - 1e-9, true, true));
		assertEquals(Action.RECOVER, ReefRules.decide(thriving, S, g, true, true));
		assertEquals(Action.RECOVER, ReefRules.decide(thriving, S, g + S.recoveryChance() - 1e-9, true, true));
		assertEquals(Action.NONE, ReefRules.decide(thriving, S, g + S.recoveryChance(), true, true));

		ReefRules.Assessment stressed = ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, true, false, 0, 0), S);
		assertEquals(Action.BLEACH, ReefRules.decide(stressed, S, S.bleachChance() - 1e-9, true, true));
		assertEquals(Action.NONE, ReefRules.decide(stressed, S, S.bleachChance(), true, true));
	}

	@Test
	void gameRulesSwitchActionsOff() {
		ReefRules.Assessment thriving = ReefRules.assess(reef(), S);
		ReefRules.Assessment stressed = ReefRules.assess(new Conditions(true, WaterKind.REEF, 10, true, false, 0, 0), S);
		assertEquals(Action.NONE, ReefRules.decide(thriving, S, 0, false, true));
		assertEquals(Action.NONE, ReefRules.decide(stressed, S, 0, true, false));
		assertEquals(Action.BLEACH, ReefRules.decide(stressed, S, 0, false, true));
		assertEquals(Action.GROW, ReefRules.decide(thriving, S, 0, true, false));
	}

	@Test
	void gateCoversEveryAction() {
		double gate = ReefRules.gate(S);
		double bestGrowth = S.growthChance() * S.boosts().max();
		assertTrue(gate >= bestGrowth + S.recoveryChance() - 1e-12, "gate " + gate);
		assertTrue(gate >= S.bleachChance());
		Settings bleachy = new Settings(0.01, 0.01, 0.9, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true, Settings.PermissionLevels.DEFAULTS);
		assertEquals(0.9, ReefRules.gate(bleachy), 1e-12);
	}
}
