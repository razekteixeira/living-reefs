package io.github.razekteixeira.livingreefs.core;

/** What the biome's water means for coral, from the {@code reef_waters} and {@code frozen_waters} biome tags. */
public enum WaterKind {
	/** Warm water where coral thrives. */
	REEF,
	/** Any other water: coral survives but does not spread. */
	NEUTRAL,
	/** Icy water: coral bleaches. */
	FROZEN
}
