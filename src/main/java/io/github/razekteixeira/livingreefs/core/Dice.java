package io.github.razekteixeira.livingreefs.core;

/** The randomness the planners need, so tests can replay exact sequences. */
@FunctionalInterface
public interface Dice {
	/** A value from 0 (inclusive) to {@code bound} (exclusive). */
	int nextInt(int bound);
}
