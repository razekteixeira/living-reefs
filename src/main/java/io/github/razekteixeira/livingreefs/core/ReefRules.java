package io.github.razekteixeira.livingreefs.core;

import java.util.ArrayList;
import java.util.List;

/** Turns what a coral sees into its health and, with one random number, into what it does next. */
public final class ReefRules {
	/** Why a coral has its health, in the order they are shown by {@code /reef check}. */
	public enum Reason {
		DRY,
		HEAT,
		FROZEN_WATER,
		CONDUIT_SHELTER,
		REEF_WATER,
		CONDUIT_WATER,
		NEUTRAL_WATER,
		TOO_DARK,
		LIGHT_OK,
		FISH,
		SEA_PICKLES,
		CONDUIT_BOOST
	}

	/**
	 * @param growthChance the chance per random tick to spread, boosts included (0 unless thriving)
	 */
	public record Assessment(Health health, double growthChance, List<Reason> reasons) {
	}

	private ReefRules() {
	}

	public static Assessment assess(Conditions c, Settings s) {
		List<Reason> reasons = new ArrayList<>();
		if (!c.wet()) {
			reasons.add(Reason.DRY);
			return new Assessment(Health.DORMANT, 0, List.copyOf(reasons));
		}
		boolean stress = false;
		if (c.heatNearby()) {
			reasons.add(Reason.HEAT);
			stress = true;
		}
		if (c.water() == WaterKind.FROZEN) {
			reasons.add(Reason.FROZEN_WATER);
			stress = true;
		}
		if (stress) {
			if (!c.conduitNearby()) {
				return new Assessment(Health.STRESSED, 0, List.copyOf(reasons));
			}
			// An active conduit shelters its reef: stress is ignored, but the coral does not grow either.
			reasons.add(Reason.CONDUIT_SHELTER);
			return new Assessment(Health.DORMANT, 0, List.copyOf(reasons));
		}
		boolean reefWater;
		if (c.water() == WaterKind.REEF) {
			reasons.add(Reason.REEF_WATER);
			reefWater = true;
		} else if (c.conduitNearby()) {
			reasons.add(Reason.CONDUIT_WATER);
			reefWater = true;
		} else {
			reasons.add(Reason.NEUTRAL_WATER);
			reefWater = false;
		}
		boolean lit = c.light() >= s.minLight();
		reasons.add(lit ? Reason.LIGHT_OK : Reason.TOO_DARK);
		if (!reefWater || !lit) {
			return new Assessment(Health.DORMANT, 0, List.copyOf(reasons));
		}
		double chance = s.growthChance();
		if (c.tropicalFish() > 0) {
			reasons.add(Reason.FISH);
			chance *= s.boosts().tropicalFish();
		}
		if (c.seaPickles() > 0) {
			reasons.add(Reason.SEA_PICKLES);
			chance *= s.boosts().seaPickles();
		}
		if (c.conduitNearby()) {
			reasons.add(Reason.CONDUIT_BOOST);
			chance *= s.boosts().conduit();
		}
		return new Assessment(Health.THRIVING, Math.min(1, chance), List.copyOf(reasons));
	}

	/**
	 * The largest chance of any action. A random tick whose roll is at or above it can return before
	 * reading the world, which keeps the common case cheap.
	 */
	public static double gate(Settings s) {
		double thriving = Math.min(1, s.growthChance() * s.boosts().max()) + s.recoveryChance();
		return Math.min(1, Math.max(thriving, s.bleachChance()));
	}

	/**
	 * Picks the action for a roll in [0, 1). Growth takes the low end of the roll, recovery the band
	 * after it, so their chances add up instead of competing.
	 */
	public static Action decide(Assessment a, Settings s, double roll, boolean growthEnabled, boolean bleachingEnabled) {
		return switch (a.health()) {
			case STRESSED -> bleachingEnabled && roll < s.bleachChance() ? Action.BLEACH : Action.NONE;
			case THRIVING -> {
				if (!growthEnabled) {
					yield Action.NONE;
				}
				if (roll < a.growthChance()) {
					yield Action.GROW;
				}
				yield roll < a.growthChance() + s.recoveryChance() ? Action.RECOVER : Action.NONE;
			}
			case DORMANT -> Action.NONE;
		};
	}
}
