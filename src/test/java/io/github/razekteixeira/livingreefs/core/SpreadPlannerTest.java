package io.github.razekteixeira.livingreefs.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class SpreadPlannerTest {
	static final SpeciesTable TABLE = SpeciesTable.of(SpeciesTableTest.vanillaIds());
	static final Settings S = Settings.DEFAULTS;
	static final String TUBE_BLOCK = "minecraft:tube_coral_block";

	static final ReefView.Cell SAND = new ReefView.Cell(false, true, true, null);
	static final ReefView.Cell GLASS = ReefView.Cell.OTHER;

	static ReefView.Cell coral(String id) {
		// Coral blocks are substrate (they are in the reef_substrate tag); plants and fans are not.
		boolean block = id.endsWith("_coral_block");
		return new ReefView.Cell(false, block, true, id);
	}

	/** Open water over a sand floor at y = -1, with the source coral at the origin. */
	static final class FakeView implements ReefView {
		final Map<List<Integer>, Cell> cells = new HashMap<>();

		FakeView(String sourceId) {
			set(0, 0, 0, coral(sourceId));
		}

		FakeView set(int x, int y, int z, Cell cell) {
			cells.put(List.of(x, y, z), cell);
			return this;
		}

		@Override
		public Cell cell(int dx, int dy, int dz) {
			Cell cell = cells.get(List.of(dx, dy, dz));
			if (cell != null) {
				return cell;
			}
			return dy == -1 ? SAND : dy < -1 ? GLASS : Cell.WATER;
		}
	}

	/** Replays fixed values and records each bound it was asked for. */
	static final class ScriptedDice implements Dice {
		final Deque<Integer> values = new ArrayDeque<>();
		final List<Integer> bounds = new ArrayList<>();

		ScriptedDice(int... values) {
			for (int v : values) {
				this.values.add(v);
			}
		}

		@Override
		public int nextInt(int bound) {
			bounds.add(bound);
			int v = values.isEmpty() ? 0 : values.poll();
			if (v < 0 || v >= bound) {
				throw new AssertionError("scripted value " + v + " outside bound " + bound);
			}
			return v;
		}
	}

	/** Dice values that pick the offset (dx, dy, dz) for spread radius 2, then the given extra values. */
	static ScriptedDice towards(int dx, int dy, int dz, int... then) {
		int[] values = new int[3 + then.length];
		values[0] = dx + 2;
		values[1] = dz + 2;
		values[2] = dy + 1;
		System.arraycopy(then, 0, values, 3, then.length);
		return new ScriptedDice(values);
	}

	@Test
	void growsTheSameSpeciesOnSubstrate() {
		FakeView view = new FakeView(TUBE_BLOCK);
		assertEquals(Optional.of(new Placement(1, 0, 0, "minecraft:tube_coral", null)),
				SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)));
		assertEquals(Optional.of(new Placement(1, 0, 0, "minecraft:tube_coral_fan", null)),
				SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 1)));
		assertEquals(Optional.of(new Placement(-2, 0, 2, TUBE_BLOCK, null)),
				SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(-2, 0, 2, 2)));
	}

	@Test
	void plantsAndFansNeverBuildCoralBlocks() {
		ScriptedDice dice = towards(1, 0, 0, 1);
		assertEquals(Optional.of(new Placement(1, 0, 0, "minecraft:brain_coral_fan", null)),
				SpreadPlanner.grow(new FakeView("minecraft:brain_coral"), TABLE, "minecraft:brain_coral", S, dice));
		assertEquals(2, dice.bounds.getLast(), "only plant and fan to choose from");
	}

	@Test
	void growsOnlyIntoStillWater() {
		FakeView view = new FakeView(TUBE_BLOCK).set(1, 0, 0, ReefView.Cell.OTHER);
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isEmpty());
		view.set(1, 0, 0, coral("minecraft:horn_coral"));
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isEmpty());
	}

	@Test
	void respectsWaterClearance() {
		// minWaterAbove is 2: a non-water block two above the target blocks growth, three above does not.
		FakeView blocked = new FakeView(TUBE_BLOCK).set(1, 2, 0, ReefView.Cell.OTHER);
		assertTrue(SpreadPlanner.grow(blocked, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isEmpty());
		FakeView clear = new FakeView(TUBE_BLOCK).set(1, 3, 0, ReefView.Cell.OTHER);
		assertTrue(SpreadPlanner.grow(clear, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isPresent());
	}

	@Test
	void respectsDensityCap() {
		// The source already counts as one coral near the target at (1, 0, 0).
		FakeView view = new FakeView(TUBE_BLOCK);
		int added = 0;
		outer:
		for (int x = 0; x <= 3; x++) {
			for (int z = -2; z <= 2; z++) {
				if ((x == 0 && z == 0) || (x == 1 && z == 0)) {
					continue;
				}
				if (added == S.maxNearbyCoral() - 2) {
					break outer;
				}
				view.set(x, 1, z, coral("minecraft:fire_coral"));
				added++;
			}
		}
		assertEquals(S.maxNearbyCoral() - 1, SpreadPlanner.nearbyCoral(view, 1, 0, 0));
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isPresent(), "one below the cap");
		view.set(3, 0, 2, coral("minecraft:dead_fire_coral"));
		assertEquals(S.maxNearbyCoral(), SpreadPlanner.nearbyCoral(view, 1, 0, 0));
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 0, 0, 0)).isEmpty(), "at the cap");
	}

	@Test
	void rejectsNonSubstrate() {
		// Two blocks away, so the source coral block is not beside the target to hold a wall fan.
		FakeView view = new FakeView(TUBE_BLOCK).set(2, -1, 0, GLASS);
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(2, 0, 0, 0)).isEmpty());
		view.set(2, -1, 0, SAND);
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(2, 0, 0, 0)).isPresent());
	}

	@Test
	void wallFanFacesAwayFromSupport() {
		// Target floats one block above the floor, next to a sand block on its east side.
		FakeView view = new FakeView(TUBE_BLOCK).set(2, 1, 0, SAND);
		Optional<Placement> placed = SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 1, 0, 0));
		assertEquals(Optional.of(new Placement(1, 1, 0, "minecraft:tube_coral_wall_fan", Facing.WEST)), placed);
	}

	@Test
	void floatingTargetWithoutSupportIsSkipped() {
		FakeView view = new FakeView(TUBE_BLOCK);
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, towards(1, 1, 0, 0)).isEmpty());
	}

	@Test
	void coralBlockHeightIsCapped() {
		// The target (1, 0, 0) sits on a stack of maxReefHeight coral blocks: only plant and fan are allowed.
		FakeView view = new FakeView(TUBE_BLOCK);
		for (int k = 1; k <= S.maxReefHeight(); k++) {
			view.set(1, -k, 0, coral("minecraft:dead_tube_coral_block"));
		}
		assertEquals(S.maxReefHeight(), SpreadPlanner.reefHeightBelow(view, TABLE, 1, 0, 0));
		ScriptedDice capped = towards(1, 0, 0, 0);
		assertTrue(SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, capped).isPresent());
		assertEquals(2, capped.bounds.getLast());

		// One block shorter, the stack can take another coral block.
		view.set(1, -S.maxReefHeight(), 0, SAND);
		ScriptedDice open = towards(1, 0, 0, 2);
		assertEquals(Optional.of(new Placement(1, 0, 0, TUBE_BLOCK, null)), SpreadPlanner.grow(view, TABLE, TUBE_BLOCK, S, open));
		assertEquals(3, open.bounds.getLast());
	}

	@Test
	void theSourceSpotItselfIsNoTarget() {
		assertTrue(SpreadPlanner.grow(new FakeView(TUBE_BLOCK), TABLE, TUBE_BLOCK, S, towards(0, 0, 0, 0)).isEmpty());
	}

	@Test
	void deadOrUnknownSourcesNeverSpread() {
		assertTrue(SpreadPlanner.grow(new FakeView("minecraft:dead_tube_coral_block"), TABLE, "minecraft:dead_tube_coral_block", S,
				towards(1, 0, 0, 0)).isEmpty());
		assertTrue(SpreadPlanner.grow(new FakeView("minecraft:stone"), TABLE, "minecraft:stone", S, towards(1, 0, 0, 0)).isEmpty());
	}

	@Test
	void recoveryRevivesWetDeadCoralOfTheSameSpecies() {
		ReefView.Cell wetDeadFan = new ReefView.Cell(false, false, true, "minecraft:dead_tube_coral_fan");
		FakeView view = new FakeView(TUBE_BLOCK).set(0, 1, 0, wetDeadFan);
		assertEquals(Optional.of(new Placement(0, 1, 0, "minecraft:tube_coral_fan", null)),
				SpreadPlanner.recover(view, TABLE, TUBE_BLOCK, new ScriptedDice(1, 2, 1)));
	}

	@Test
	void recoveryNeverTouchesDryOrOtherSpeciesOrLiving() {
		ReefView.Cell dryDead = new ReefView.Cell(false, false, false, "minecraft:dead_tube_coral_fan");
		ReefView.Cell otherSpecies = new ReefView.Cell(false, false, true, "minecraft:dead_brain_coral_fan");
		ReefView.Cell living = new ReefView.Cell(false, false, true, "minecraft:tube_coral_fan");
		for (ReefView.Cell cell : List.of(dryDead, otherSpecies, living, ReefView.Cell.WATER)) {
			FakeView view = new FakeView(TUBE_BLOCK).set(0, 1, 0, cell);
			assertTrue(SpreadPlanner.recover(view, TABLE, TUBE_BLOCK, new ScriptedDice(1, 2, 1)).isEmpty(), cell.toString());
		}
	}

	@Test
	void openSpotsCountWhatGrowthCouldUse() {
		FakeView view = new FakeView(TUBE_BLOCK);
		// Radius 2: 24 spots on the sand around the source, plus the one on top of the source block.
		assertEquals(25, SpreadPlanner.openSpots(view, TABLE, TUBE_BLOCK, S));
		view.set(1, 0, 0, GLASS);
		view.set(-2, 2, -2, GLASS);
		// One spot filled, one without two blocks of water above it.
		assertEquals(23, SpreadPlanner.openSpots(view, TABLE, TUBE_BLOCK, S));
		Settings crowded = new Settings(S.growthChance(), S.recoveryChance(), S.bleachChance(), S.spreadRadius(), S.minWaterAbove(),
				1, S.maxReefHeight(), S.minLight(), S.boosts(), S.particles(), S.permissionLevels());
		// The source itself is one coral near every spot, so a cap of 1 leaves no room anywhere.
		assertEquals(0, SpreadPlanner.openSpots(view, TABLE, TUBE_BLOCK, crowded));
		assertEquals(0, SpreadPlanner.openSpots(view, TABLE, "minecraft:dead_tube_coral_block", S));
	}
}
