package io.github.razekteixeira.livingreefs;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;

import io.github.razekteixeira.livingreefs.core.Action;
import io.github.razekteixeira.livingreefs.core.Conditions;
import io.github.razekteixeira.livingreefs.core.Facing;
import io.github.razekteixeira.livingreefs.core.Placement;
import io.github.razekteixeira.livingreefs.core.ReefRules;
import io.github.razekteixeira.livingreefs.core.Settings;
import io.github.razekteixeira.livingreefs.core.SpreadPlanner;

/**
 * Runs on every random tick of a living coral block (see {@code BlockBehaviourMixin}).
 *
 * <p>Most ticks end after one random number: only a roll below {@link ReefRules#gate} reads the
 * world. Vanilla decides which coral gets ticked and when, so this follows simulation distance,
 * {@code random_tick_speed} and {@code /tick freeze} without any bookkeeping of its own.
 */
public final class ReefTicker {
	public static final ReefStats STATS = new ReefStats();

	private ReefTicker() {
	}

	public static void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		long started = System.nanoTime();
		Result result = tick(state, level, pos, random);
		STATS.recordTick(level.getGameTime(), System.nanoTime() - started, result.assessed(), result.action());
	}

	private record Result(boolean assessed, Action action) {
		static final Result SKIPPED = new Result(false, Action.NONE);
	}

	private static Result tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		GameRules rules = level.getGameRules();
		boolean growth = rules.get(ReefGameRules.CORAL_GROWTH);
		boolean bleaching = rules.get(ReefGameRules.CORAL_BLEACHING);
		if (!growth && !bleaching) {
			return Result.SKIPPED;
		}
		Settings settings = LivingReefsConfig.get();
		double roll = random.nextDouble();
		if (roll >= ReefRules.gate(settings)) {
			return Result.SKIPPED;
		}
		return new Result(true, act(state, level, pos, random, settings, roll, growth, bleaching));
	}

	/**
	 * Assesses one coral and carries out what its health and the roll call for. Returns the action
	 * that changed the world, or {@link Action#NONE}. Public so GameTests can drive it with exact
	 * settings and rolls instead of waiting for random ticks.
	 */
	public static Action act(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Settings settings,
			double roll, boolean growth, boolean bleaching) {
		ReefSpecies species = ReefSpecies.get();
		String id = species.coralId(state.getBlock());
		if (id == null) {
			return Action.NONE;
		}
		Conditions conditions = ReefConditions.read(level, pos, state);
		ReefRules.Assessment assessment = ReefRules.assess(conditions, settings);
		Action action = ReefRules.decide(assessment, settings, roll, growth, bleaching);
		boolean changed = switch (action) {
			case GROW -> place(level, pos, SpreadPlanner.grow(new WorldReefView(level, pos, species), species.table(), id, settings,
					random::nextInt), settings);
			case RECOVER -> place(level, pos, SpreadPlanner.recover(new WorldReefView(level, pos, species), species.table(), id,
					random::nextInt), settings);
			case BLEACH -> bleach(level, pos, state, species, settings);
			case NONE -> false;
		};
		return changed ? action : Action.NONE;
	}

	/** Places a planned coral, or revives one, keeping the properties of what it replaces where they apply. */
	private static boolean place(ServerLevel level, BlockPos source, Optional<Placement> planned, Settings settings) {
		if (planned.isEmpty()) {
			return false;
		}
		Placement p = planned.get();
		Optional<Block> block = ReefSpecies.block(p.id());
		if (block.isEmpty()) {
			return false;
		}
		BlockPos target = source.offset(p.dx(), p.dy(), p.dz());
		// New coral replaces water and starts waterlogged (the default for coral plants and fans);
		// revived coral keeps the waterlogging and facing of its dead twin.
		BlockState state = block.get().withPropertiesOf(level.getBlockState(target));
		if (p.facing() != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, direction(p.facing()));
		}
		if (!state.canSurvive(level, target)) {
			return false;
		}
		level.setBlock(target, state, Block.UPDATE_ALL);
		particles(level, target, ParticleTypes.HAPPY_VILLAGER, settings);
		return true;
	}

	/** Turns the coral into its dead twin. It stays in water, so it keeps its waterlogging and facing. */
	private static boolean bleach(ServerLevel level, BlockPos pos, BlockState state, ReefSpecies species, Settings settings) {
		String id = species.coralId(state.getBlock());
		Optional<Block> dead = Optional.ofNullable(id).flatMap(species.table()::twin).flatMap(ReefSpecies::block);
		if (dead.isEmpty()) {
			return false;
		}
		level.setBlock(pos, dead.get().withPropertiesOf(state), Block.UPDATE_ALL);
		particles(level, pos, ParticleTypes.WHITE_ASH, settings);
		return true;
	}

	private static void particles(ServerLevel level, BlockPos pos, SimpleParticleType type, Settings settings) {
		if (settings.particles()) {
			level.sendParticles(type, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.0);
		}
	}

	static Direction direction(Facing facing) {
		return switch (facing) {
			case NORTH -> Direction.NORTH;
			case SOUTH -> Direction.SOUTH;
			case WEST -> Direction.WEST;
			case EAST -> Direction.EAST;
		};
	}
}
