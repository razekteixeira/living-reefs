package io.github.razekteixeira.livingreefs;

import io.github.razekteixeira.livingreefs.core.Action;

/**
 * Counts and timings since the server started, for {@code /reef stats} and the benchmark. Kept in
 * memory only: everything Living Reefs changes is already saved in the world's blocks.
 *
 * <p>Only touched from the server thread (random ticks and commands).
 */
public final class ReefStats {
	private long randomTicks;
	private long assessed;
	private long grown;
	private long recovered;
	private long bleached;
	private long totalNanos;
	private long maxNanos;

	private long second = Long.MIN_VALUE;
	private long secondNanos;
	private long secondTicks;
	private long lastSecondNanos;
	private long lastSecondTicks;

	/** One coral random tick that took {@code nanos}, at game time {@code gameTime}. */
	public void recordTick(long gameTime, long nanos, boolean wasAssessed, Action action) {
		randomTicks++;
		totalNanos += nanos;
		maxNanos = Math.max(maxNanos, nanos);
		if (wasAssessed) {
			assessed++;
		}
		switch (action) {
			case GROW -> grown++;
			case RECOVER -> recovered++;
			case BLEACH -> bleached++;
			case NONE -> {
			}
		}
		long now = gameTime / 20;
		if (now != second) {
			if (second != Long.MIN_VALUE) {
				lastSecondNanos = now == second + 1 ? secondNanos : 0;
				lastSecondTicks = now == second + 1 ? secondTicks : 0;
			}
			second = now;
			secondNanos = 0;
			secondTicks = 0;
		}
		secondNanos += nanos;
		secondTicks++;
	}

	public long randomTicks() {
		return randomTicks;
	}

	public long assessed() {
		return assessed;
	}

	public long grown() {
		return grown;
	}

	public long recovered() {
		return recovered;
	}

	public long bleached() {
		return bleached;
	}

	public long maxNanos() {
		return maxNanos;
	}

	public double meanNanos() {
		return randomTicks == 0 ? 0 : (double) totalNanos / randomTicks;
	}

	/** Time spent in coral random ticks during the last complete second of game time. */
	public long lastSecondNanos() {
		return lastSecondNanos;
	}

	public long lastSecondTicks() {
		return lastSecondTicks;
	}

	public void reset() {
		randomTicks = assessed = grown = recovered = bleached = totalNanos = maxNanos = 0;
		second = Long.MIN_VALUE;
		secondNanos = secondTicks = lastSecondNanos = lastSecondTicks = 0;
	}
}
