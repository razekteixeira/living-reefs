package io.github.razekteixeira.livingreefs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import io.github.razekteixeira.livingreefs.core.ReefView;

/**
 * The world around a coral as the planners see it. Reads only chunks that are already loaded and
 * treats anything else as a plain block, so a growth attempt can never load or generate a chunk.
 */
final class WorldReefView implements ReefView {
	private final ServerLevel level;
	private final BlockPos origin;
	private final ReefSpecies species;
	private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

	WorldReefView(ServerLevel level, BlockPos origin, ReefSpecies species) {
		this.level = level;
		this.origin = origin;
		this.species = species;
	}

	@Override
	public Cell cell(int dx, int dy, int dz) {
		BlockPos pos = cursor.setWithOffset(origin, dx, dy, dz);
		BlockState state = stateIfLoaded(level, pos);
		if (state == null) {
			return Cell.OTHER;
		}
		boolean water = state.is(Blocks.WATER) && state.getFluidState().isSource();
		boolean substrate = state.is(ReefTags.REEF_SUBSTRATE) && state.isCollisionShapeFullBlock(level, pos);
		String coralId = species.coralId(state.getBlock());
		// Wetness only matters for coral (recovery), so the six neighbour reads are skipped otherwise.
		boolean wet = water || (coralId != null && isWet(level, pos.immutable(), state));
		return new Cell(water, substrate, wet, coralId);
	}

	/** The block state if its chunk is loaded, otherwise null. Never loads a chunk. */
	static BlockState stateIfLoaded(ServerLevel level, BlockPos pos) {
		if (level.isOutsideBuildHeight(pos)) {
			return null;
		}
		LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk == null ? null : chunk.getBlockState(pos);
	}

	/** Same test as vanilla coral uses: waterlogged, or water on any side. */
	static boolean isWet(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.getFluidState().is(FluidTags.WATER)) {
			return true;
		}
		for (Direction direction : Direction.values()) {
			BlockState neighbour = stateIfLoaded(level, pos.relative(direction));
			if (neighbour != null && neighbour.getFluidState().is(FluidTags.WATER)) {
				return true;
			}
		}
		return false;
	}
}
