package io.github.razekteixeira.livingreefs.core;

/** The four shapes a coral comes in, each with a living and a dead block. */
public enum CoralForm {
	// Longest suffix first: "_coral_wall_fan" also ends with "_coral_fan".
	BLOCK("_coral_block"),
	WALL_FAN("_coral_wall_fan"),
	FAN("_coral_fan"),
	PLANT("_coral");

	private final String suffix;

	CoralForm(String suffix) {
		this.suffix = suffix;
	}

	/** The id suffix after the coral type, for example {@code _coral_fan} in {@code tube_coral_fan}. */
	public String suffix() {
		return suffix;
	}
}
