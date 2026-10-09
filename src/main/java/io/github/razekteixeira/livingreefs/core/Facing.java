package io.github.razekteixeira.livingreefs.core;

/** Horizontal directions, as offsets on the x and z axes. */
public enum Facing {
	NORTH(0, -1),
	SOUTH(0, 1),
	WEST(-1, 0),
	EAST(1, 0);

	private final int dx;
	private final int dz;

	Facing(int dx, int dz) {
		this.dx = dx;
		this.dz = dz;
	}

	public int dx() {
		return dx;
	}

	public int dz() {
		return dz;
	}
}
