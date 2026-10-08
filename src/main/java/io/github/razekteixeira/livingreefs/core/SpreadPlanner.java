package io.github.razekteixeira.livingreefs.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Decides where a thriving coral spreads to and what it revives. Each call makes one attempt at one
 * random spot, so the cost of a random tick stays small and bounded; a failed attempt simply waits
 * for the next tick.
 */
public final class SpreadPlanner {
	/** Radius of the box around a target in which existing coral counts towards {@link Settings#maxNearbyCoral()}. */
	public static final int DENSITY_RADIUS = 2;

	private SpreadPlanner() {
	}

	/**
	 * One growth attempt from the living coral {@code sourceId}. The result is a new coral of the same
	 * species in still water, never a replacement of any other block.
	 */
	public static Optional<Placement> grow(ReefView view, SpeciesTable table, String sourceId, Settings s, Dice dice) {
		Optional<SpeciesTable.Entry> source = table.lookup(sourceId).filter(e -> !e.dead());
		if (source.isEmpty()) {
			return Optional.empty();
		}
		int r = s.spreadRadius();
		int dx = dice.nextInt(2 * r + 1) - r;
		int dz = dice.nextInt(2 * r + 1) - r;
		int dy = dice.nextInt(3) - 1;
		if (dx == 0 && dy == 0 && dz == 0) {
			return Optional.empty();
		}
		if (!view.cell(dx, dy, dz).water() || !clearAbove(view, dx, dy, dz, s.minWaterAbove())
				|| nearbyCoral(view, dx, dy, dz) >= s.maxNearbyCoral()) {
			return Optional.empty();
		}
		String species = source.get().species();
		if (view.cell(dx, dy - 1, dz).substrate()) {
			List<CoralForm> forms = new ArrayList<>(List.of(CoralForm.PLANT, CoralForm.FAN));
			// Only coral blocks build the reef itself, and only up to the height cap.
			if (source.get().form() == CoralForm.BLOCK && reefHeightBelow(view, table, dx, dy, dz) < s.maxReefHeight()) {
				forms.add(CoralForm.BLOCK);
			}
			forms.removeIf(form -> table.id(species, form, false).isEmpty());
			if (forms.isEmpty()) {
				return Optional.empty();
			}
			CoralForm form = forms.get(dice.nextInt(forms.size()));
			return table.id(species, form, false).map(id -> new Placement(dx, dy, dz, id, null));
		}
		Optional<String> wallFan = table.id(species, CoralForm.WALL_FAN, false);
		if (wallFan.isEmpty()) {
			return Optional.empty();
		}
		int start = dice.nextInt(4);
		for (int i = 0; i < 4; i++) {
			Facing facing = Facing.values()[(start + i) % 4];
			// The fan points away from the block it hangs on.
			if (view.cell(dx - facing.dx(), dy, dz - facing.dz()).substrate()) {
				return Optional.of(new Placement(dx, dy, dz, wallFan.get(), facing));
			}
		}
		return Optional.empty();
	}

	/**
	 * One recovery attempt: a dead coral of the source's species next to it, still in water, comes
	 * back to life. Dry dead coral (decoration on land) and other species are never touched.
	 */
	public static Optional<Placement> recover(ReefView view, SpeciesTable table, String sourceId, Dice dice) {
		Optional<SpeciesTable.Entry> source = table.lookup(sourceId).filter(e -> !e.dead());
		if (source.isEmpty()) {
			return Optional.empty();
		}
		int dx = dice.nextInt(3) - 1;
		int dy = dice.nextInt(3) - 1;
		int dz = dice.nextInt(3) - 1;
		if (dx == 0 && dy == 0 && dz == 0) {
			return Optional.empty();
		}
		ReefView.Cell target = view.cell(dx, dy, dz);
		if (!target.isCoral() || !target.wet()) {
			return Optional.empty();
		}
		return table.lookup(target.coralId())
				.filter(e -> e.dead() && e.species().equals(source.get().species()))
				.flatMap(e -> table.id(e.species(), e.form(), false))
				.map(id -> new Placement(dx, dy, dz, id, null));
	}

	static boolean clearAbove(ReefView view, int dx, int dy, int dz, int blocks) {
		for (int k = 1; k <= blocks; k++) {
			if (!view.cell(dx, dy + k, dz).water()) {
				return false;
			}
		}
		return true;
	}

	/** Coral of any species, form or state in the box around the target, the target itself excluded. */
	public static int nearbyCoral(ReefView view, int dx, int dy, int dz) {
		int count = 0;
		for (int x = -DENSITY_RADIUS; x <= DENSITY_RADIUS; x++) {
			for (int y = -DENSITY_RADIUS; y <= DENSITY_RADIUS; y++) {
				for (int z = -DENSITY_RADIUS; z <= DENSITY_RADIUS; z++) {
					if ((x != 0 || y != 0 || z != 0) && view.cell(dx + x, dy + y, dz + z).isCoral()) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/** How many coral blocks are stacked directly below the target. */
	static int reefHeightBelow(ReefView view, SpeciesTable table, int dx, int dy, int dz) {
		int height = 0;
		for (int k = 1; k <= 8; k++) {
			ReefView.Cell below = view.cell(dx, dy - k, dz);
			boolean coralBlock = below.isCoral()
					&& table.lookup(below.coralId()).map(e -> e.form() == CoralForm.BLOCK).orElse(false);
			if (!coralBlock) {
				break;
			}
			height++;
		}
		return height;
	}
}
