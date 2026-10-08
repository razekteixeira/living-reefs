package io.github.razekteixeira.livingreefs.core;

/**
 * What a coral sees around itself, read from the world once per decision.
 *
 * @param wet           waterlogged or touching water (dry coral is left to vanilla, which kills it)
 * @param water         the biome's water kind
 * @param light         the brighter of sky and block light at the coral
 * @param heatNearby    a heat source block (magma, lava) within 2 blocks
 * @param conduitNearby an active conduit within 16 blocks
 * @param seaPickles    lit sea pickle blocks within 2 blocks
 * @param tropicalFish  tropical fish within 6 blocks
 */
public record Conditions(
		boolean wet,
		WaterKind water,
		int light,
		boolean heatNearby,
		boolean conduitNearby,
		int seaPickles,
		int tropicalFish) {
}
