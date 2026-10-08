package io.github.razekteixeira.livingreefs.core;

import org.jspecify.annotations.Nullable;

/** The blocks around a coral, addressed by offsets from it. Lets the planners run without a world. */
public interface ReefView {
	/**
	 * One block.
	 *
	 * @param water     still water with nothing in it (a water source block), the only thing coral grows into
	 * @param substrate a full block from the {@code reef_substrate} tag that coral can grow on
	 * @param wet       waterlogged or touching water
	 * @param coralId   the block id when it is living or dead coral of any form, otherwise null
	 */
	record Cell(boolean water, boolean substrate, boolean wet, @Nullable String coralId) {
		public static final Cell WATER = new Cell(true, false, true, null);
		public static final Cell OTHER = new Cell(false, false, false, null);

		public boolean isCoral() {
			return coralId != null;
		}
	}

	Cell cell(int dx, int dy, int dz);
}
