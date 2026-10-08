package io.github.razekteixeira.livingreefs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import io.github.razekteixeira.livingreefs.core.Conditions;
import io.github.razekteixeira.livingreefs.core.WaterKind;

/** Reads {@link Conditions} for one coral from the world, from loaded chunks only. */
public final class ReefConditions {
	/** Heat sources and sea pickles count within this many blocks (a 5 x 5 x 5 box). */
	public static final int NEAR = 2;
	public static final int FISH_RADIUS = 6;
	public static final int CONDUIT_RADIUS = 16;

	private ReefConditions() {
	}

	public static Conditions read(ServerLevel level, BlockPos pos, BlockState state) {
		boolean wet = WorldReefView.isWet(level, pos, state);
		boolean heat = false;
		int pickles = 0;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = -NEAR; x <= NEAR; x++) {
			for (int y = -NEAR; y <= NEAR; y++) {
				for (int z = -NEAR; z <= NEAR; z++) {
					BlockState near = WorldReefView.stateIfLoaded(level, cursor.setWithOffset(pos, x, y, z));
					if (near == null) {
						continue;
					}
					if (near.is(ReefTags.REEF_HEAT_SOURCES)) {
						heat = true;
					} else if (near.is(Blocks.SEA_PICKLE) && near.getValue(SeaPickleBlock.WATERLOGGED)) {
						pickles++;
					}
				}
			}
		}
		int fish = level.getEntitiesOfClass(TropicalFish.class, new AABB(pos).inflate(FISH_RADIUS)).size();
		return new Conditions(wet, waterKind(level.getBiome(pos)), lightAt(level, pos), heat, conduitNear(level, pos), pickles, fish);
	}

	static WaterKind waterKind(Holder<Biome> biome) {
		if (biome.is(ReefTags.FROZEN_WATERS)) {
			return WaterKind.FROZEN;
		}
		return biome.is(ReefTags.REEF_WATERS) ? WaterKind.REEF : WaterKind.NEUTRAL;
	}

	/**
	 * The brightest sky or block light at the coral or in the water beside it. A coral block is
	 * opaque, so its own position is always dark; the light that reaches its faces is what counts.
	 */
	public static int lightAt(ServerLevel level, BlockPos pos) {
		int light = brightness(level, pos);
		for (Direction direction : Direction.values()) {
			light = Math.max(light, brightness(level, pos.relative(direction)));
		}
		return light;
	}

	private static int brightness(ServerLevel level, BlockPos pos) {
		return Math.max(level.getBrightness(LightLayer.SKY, pos), level.getBrightness(LightLayer.BLOCK, pos));
	}

	/** Whether an active conduit is within {@link #CONDUIT_RADIUS} blocks, looking only at loaded chunks. */
	static boolean conduitNear(ServerLevel level, BlockPos pos) {
		int minChunkX = (pos.getX() - CONDUIT_RADIUS) >> 4;
		int maxChunkX = (pos.getX() + CONDUIT_RADIUS) >> 4;
		int minChunkZ = (pos.getZ() - CONDUIT_RADIUS) >> 4;
		int maxChunkZ = (pos.getZ() + CONDUIT_RADIUS) >> 4;
		long radiusSquared = (long) CONDUIT_RADIUS * CONDUIT_RADIUS;
		for (int cx = minChunkX; cx <= maxChunkX; cx++) {
			for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					if (blockEntity instanceof ConduitBlockEntity conduit && conduit.isActive()
							&& conduit.getBlockPos().distSqr(pos) <= radiusSquared) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
