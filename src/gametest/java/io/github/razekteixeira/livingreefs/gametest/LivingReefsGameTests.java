package io.github.razekteixeira.livingreefs.gametest;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import io.github.razekteixeira.livingreefs.LivingReefsConfig;
import io.github.razekteixeira.livingreefs.ReefConditions;
import io.github.razekteixeira.livingreefs.ReefGameRules;
import io.github.razekteixeira.livingreefs.ReefSpecies;
import io.github.razekteixeira.livingreefs.ReefTicker;
import io.github.razekteixeira.livingreefs.core.Action;
import io.github.razekteixeira.livingreefs.core.Health;
import io.github.razekteixeira.livingreefs.core.ReefRules;
import io.github.razekteixeira.livingreefs.core.Settings;
import io.github.razekteixeira.livingreefs.core.SpeciesTable;

/**
 * Runs inside a real dedicated server with real blocks, biomes and random ticks, no client.
 *
 * <p>Most tests run in the {@code quiet} environment, where the mod's game rules are off so vanilla
 * random ticks change nothing, and drive {@link ReefTicker#act} with exact settings and rolls. The
 * end-to-end test runs alone in the {@code fast} environment with a high random tick speed.
 */
public class LivingReefsGameTests {
	private static final String QUIET = "livingreefs-gametest:quiet";
	private static final String FAST = "livingreefs-gametest:fast";
	/** A second fast environment: each environment is its own batch, so the two fast tests never share game rules. */
	private static final String FAST_RULES = "livingreefs-gametest:fast_rules";

	private static final Settings.PermissionLevels LEVELS = Settings.PermissionLevels.DEFAULTS;
	/** Always grows when thriving; light is not part of these tests (the test area's light is not controlled). */
	private static final Settings GROW = new Settings(1, 0, 0, 2, 2, 14, 3, 0, Settings.Boosts.DEFAULTS, false, LEVELS);
	private static final Settings RECOVER = new Settings(0, 1, 0, 2, 2, 14, 3, 0, Settings.Boosts.DEFAULTS, false, LEVELS);
	private static final Settings BLEACH = new Settings(0, 0, 1, 2, 2, 14, 3, 0, Settings.Boosts.DEFAULTS, false, LEVELS);

	/** Sand floor at y 0, glass walls on the border, water from y 1 up to {@code depth}, warm ocean biome. */
	private static void reefPool(GameTestHelper helper, Block floor, int depth) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 0, z), floor);
				boolean wall = x == 0 || x == 7 || z == 0 || z == 7;
				for (int y = 1; y <= depth; y++) {
					helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.GLASS : Blocks.WATER);
				}
			}
		}
		BlockPos a = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos b = helper.absolutePos(new BlockPos(7, depth + 1, 7));
		command(helper, "fillbiome %d %d %d %d %d %d minecraft:warm_ocean".formatted(
				Math.min(a.getX(), b.getX()), a.getY(), Math.min(a.getZ(), b.getZ()),
				Math.max(a.getX(), b.getX()), b.getY(), Math.max(a.getZ(), b.getZ())));
	}

	private static void command(GameTestHelper helper, String command) {
		MinecraftServer server = helper.getLevel().getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(helper.getLevel()).withSuppressedOutput(), command);
	}

	private static BlockState waterlogged(Block block) {
		return block.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
	}

	/** Calls the coral's handler {@code times} times with a fixed roll, as random ticks would. */
	private static int actRepeatedly(GameTestHelper helper, BlockPos relative, Settings settings, double roll, int times) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(relative);
		RandomSource random = RandomSource.create(relative.asLong());
		int changed = 0;
		for (int i = 0; i < times; i++) {
			if (ReefTicker.act(level.getBlockState(pos), level, pos, random, settings, roll, true, true) != Action.NONE) {
				changed++;
			}
		}
		return changed;
	}

	/** Every coral block (any species, form or state) inside the pool, the source excluded. */
	private static List<String> coralInPool(GameTestHelper helper, BlockPos source) {
		List<String> found = new ArrayList<>();
		ReefSpecies species = ReefSpecies.get();
		for (int x = 0; x <= 7; x++) {
			for (int y = 0; y <= 6; y++) {
				for (int z = 0; z <= 7; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					String id = species.coralId(helper.getBlockState(pos).getBlock());
					if (id != null && !pos.equals(source)) {
						found.add(id);
					}
				}
			}
		}
		return found;
	}

	// AC1: the mixin gives every living coral state random ticks and leaves dead coral and other blocks alone.
	@GameTest(environment = QUIET)
	public void livingCoralRandomTicksDeadDoesNot(GameTestHelper helper) {
		SpeciesTable table = ReefSpecies.get().table();
		int living = 0;
		int dead = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			var entry = table.lookup(BuiltInRegistries.BLOCK.getKey(block).toString());
			if (entry.isEmpty() || !BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft")) {
				continue;
			}
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				helper.assertTrue(state.isRandomlyTicking() == !entry.get().dead(), state + " random ticking must be " + !entry.get().dead());
			}
			if (entry.get().dead()) {
				dead++;
			} else {
				living++;
			}
		}
		helper.assertValueEqual(living, 20, "living vanilla coral blocks");
		helper.assertValueEqual(dead, 20, "dead vanilla coral blocks");
		helper.assertTrue(!Blocks.SAND.defaultBlockState().isRandomlyTicking(), "sand stays non-ticking");
		helper.assertTrue(Blocks.KELP.defaultBlockState().isRandomlyTicking(), "kelp keeps its own random ticks");
		helper.succeed();
	}

	// AC3: growth keeps the species and only ever fills water: floor and walls stay as they were.
	@GameTest(environment = QUIET)
	public void growthKeepsSpeciesAndFillsOnlyWater(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		BlockPos source = new BlockPos(3, 1, 3);
		helper.setBlock(source, Blocks.TUBE_CORAL_BLOCK);
		int grown = actRepeatedly(helper, source, GROW, 0, 300);

		List<String> coral = coralInPool(helper, source);
		helper.assertTrue(grown >= 10, "expected at least 10 growths in 300 attempts, got " + grown);
		helper.assertValueEqual(coral.size(), grown, "every growth left one new coral");
		helper.assertTrue(coral.stream().allMatch(id -> id.startsWith("minecraft:tube_coral")), "only tube coral grew: " + coral);
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.assertBlockPresent(Blocks.SAND, new BlockPos(x, 0, z));
				if (x == 0 || x == 7 || z == 0 || z == 7) {
					helper.assertBlockPresent(Blocks.GLASS, new BlockPos(x, 1, z));
				}
			}
		}
		helper.succeed();
	}

	// AC2 and AC8: real random ticks grow a reef; turning the game rules off stops all change.
	@GameTest(environment = FAST, maxTicks = 600)
	public void randomTicksGrowAReef(GameTestHelper helper) {
		// Any earlier failure that left other settings behind is overwritten here, and restored at the end.
		reefPool(helper, Blocks.SAND, 5);
		BlockPos source = new BlockPos(3, 1, 3);
		helper.setBlock(source, Blocks.BRAIN_CORAL_BLOCK);
		ServerLevel level = helper.getLevel();
		Settings original = LivingReefsConfig.get();
		LivingReefsConfig.set(new Settings(0.5, 0, 0, 2, 2, 14, 3, 0, Settings.Boosts.DEFAULTS, false, LEVELS));
		int[] frozenCount = {0};
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(coralInPool(helper, source).size() >= 4,
						"random ticks grew " + coralInPool(helper, source).size() + " coral so far"))
				.thenExecute(() -> {
					level.getGameRules().set(ReefGameRules.CORAL_GROWTH, false, level.getServer());
					level.getGameRules().set(ReefGameRules.CORAL_BLEACHING, false, level.getServer());
					frozenCount[0] = coralInPool(helper, source).size();
				})
				.thenIdle(100)
				.thenExecute(() -> {
					LivingReefsConfig.set(original);
					helper.assertValueEqual(coralInPool(helper, source).size(), frozenCount[0], "no growth with the game rules off");
					helper.assertTrue(coralInPool(helper, source).stream().allMatch(id -> id.startsWith("minecraft:brain_coral")),
							"only brain coral grew");
				})
				.thenSucceed();
	}

	// AC4: too little water above stops growth.
	@GameTest(environment = QUIET)
	public void shallowWaterStopsGrowth(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 1);
		BlockPos source = new BlockPos(3, 1, 3);
		helper.setBlock(source, Blocks.TUBE_CORAL_BLOCK);
		helper.assertValueEqual(actRepeatedly(helper, source, GROW, 0, 300), 0, "growths in water one block deep");
		helper.assertValueEqual(coralInPool(helper, source).size(), 0, "new coral");
		helper.succeed();
	}

	// AC4: a floor that is not reef substrate (glass) gets no coral.
	@GameTest(environment = QUIET)
	public void glassFloorStopsGrowth(GameTestHelper helper) {
		reefPool(helper, Blocks.GLASS, 5);
		BlockPos source = new BlockPos(3, 1, 3);
		helper.setBlock(source, waterlogged(Blocks.TUBE_CORAL));
		helper.assertValueEqual(actRepeatedly(helper, source, GROW, 0, 300), 0, "growths over a glass floor");
		helper.succeed();
	}

	// AC3: over a glass floor the only support is the source coral block, so growth is wall fans on its
	// four sides (and coral on top); each fan must face away from the block it hangs on.
	@GameTest(environment = QUIET)
	public void wallFansGrowOnEverySide(GameTestHelper helper) {
		reefPool(helper, Blocks.GLASS, 6);
		BlockPos source = new BlockPos(3, 2, 3);
		helper.setBlock(new BlockPos(3, 1, 3), Blocks.GLASS);
		helper.setBlock(source, Blocks.TUBE_CORAL_BLOCK);
		actRepeatedly(helper, source, GROW, 0, 400);
		java.util.Set<Direction> facings = java.util.EnumSet.noneOf(Direction.class);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockState fan = helper.getBlockState(source.relative(side));
			if (fan.is(Blocks.TUBE_CORAL_WALL_FAN)) {
				helper.assertValueEqual(fan.getValue(BlockStateProperties.HORIZONTAL_FACING), side, "fan on the " + side + " side faces away");
				facings.add(side);
			}
		}
		helper.assertValueEqual(facings.size(), 4, "wall fans on all four sides: " + facings);
		helper.succeed();
	}

	// AC5: heat bleaches coral into its dead twin, keeping facing and waterlogging.
	@GameTest(environment = QUIET)
	public void heatBleachesCoral(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		// East, not the default north, so losing the facing would show.
		BlockPos block = new BlockPos(3, 1, 3);
		BlockPos fan = new BlockPos(4, 1, 3);
		helper.setBlock(block, Blocks.TUBE_CORAL_BLOCK);
		helper.setBlock(fan, waterlogged(Blocks.TUBE_CORAL_WALL_FAN).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
		helper.setBlock(new BlockPos(5, 1, 5), Blocks.MAGMA_BLOCK);

		helper.assertValueEqual(actRepeatedly(helper, fan, BLEACH, 0, 1), 1, "fan bleached");
		helper.assertValueEqual(actRepeatedly(helper, block, BLEACH, 0, 1), 1, "block bleached");
		helper.assertBlockState(fan, waterlogged(Blocks.DEAD_TUBE_CORAL_WALL_FAN).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
		helper.assertBlockPresent(Blocks.DEAD_TUBE_CORAL_BLOCK, block);
		helper.succeed();
	}

	// Control for AC5: the same bleach-everything settings do nothing without a heat source.
	@GameTest(environment = QUIET)
	public void noHeatNoBleaching(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		BlockPos block = new BlockPos(3, 1, 3);
		helper.setBlock(block, Blocks.TUBE_CORAL_BLOCK);
		helper.assertValueEqual(actRepeatedly(helper, block, BLEACH, 0, 50), 0, "bleaching without heat");
		helper.assertBlockPresent(Blocks.TUBE_CORAL_BLOCK, block);
		helper.succeed();
	}

	// AC6: recovery revives wet dead coral of the same species only.
	@GameTest(environment = QUIET)
	public void recoveryRevivesWetDeadCoralOnly(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		BlockPos source = new BlockPos(3, 1, 3);
		BlockPos wetSameKind = new BlockPos(3, 2, 3);
		BlockPos otherKind = new BlockPos(4, 1, 3);
		BlockPos dry = new BlockPos(2, 1, 2);
		helper.setBlock(source, Blocks.TUBE_CORAL_BLOCK);
		helper.setBlock(wetSameKind, waterlogged(Blocks.DEAD_TUBE_CORAL_FAN));
		helper.setBlock(otherKind, waterlogged(Blocks.DEAD_BRAIN_CORAL_FAN));
		// A dead tube coral block sealed in glass: next to the source, but dry.
		for (BlockPos seal : List.of(new BlockPos(1, 1, 2), new BlockPos(3, 1, 2), new BlockPos(2, 1, 1), new BlockPos(2, 1, 3), new BlockPos(2, 2, 2))) {
			helper.setBlock(seal, Blocks.GLASS);
		}
		helper.setBlock(dry, Blocks.DEAD_TUBE_CORAL_BLOCK);

		int revived = actRepeatedly(helper, source, RECOVER, 0, 400);
		helper.assertValueEqual(revived, 1, "exactly one coral revived");
		helper.assertBlockState(wetSameKind, waterlogged(Blocks.TUBE_CORAL_FAN));
		helper.assertBlockPresent(Blocks.DEAD_BRAIN_CORAL_FAN, otherKind);
		helper.assertBlockPresent(Blocks.DEAD_TUBE_CORAL_BLOCK, dry);
		helper.succeed();
	}

	// AC7: an active conduit shelters coral from heat.
	@GameTest(environment = QUIET, maxTicks = 300)
	public void conduitProtectsFromHeat(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 6);
		BlockPos conduit = new BlockPos(4, 4, 4);
		BlockPos coral = new BlockPos(1, 1, 1);
		helper.setBlock(coral, Blocks.TUBE_CORAL_BLOCK);
		helper.setBlock(new BlockPos(1, 1, 3), Blocks.MAGMA_BLOCK);
		ServerLevel level = helper.getLevel();
		BlockPos coralAbs = helper.absolutePos(coral);
		helper.assertValueEqual(health(level, coralAbs), Health.STRESSED, "before the conduit");

		helper.setBlock(conduit, waterlogged(Blocks.CONDUIT));
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					int ax = Math.abs(x);
					int ay = Math.abs(y);
					int az = Math.abs(z);
					if ((ax > 1 || ay > 1 || az > 1) && (x == 0 && (ay == 2 || az == 2) || y == 0 && (ax == 2 || az == 2) || z == 0 && (ax == 2 || ay == 2))) {
						helper.setBlock(conduit.offset(x, y, z), Blocks.PRISMARINE);
					}
				}
			}
		}
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(health(level, coralAbs) == Health.DORMANT, "waiting for the conduit to activate"))
				.thenExecute(() -> {
					helper.assertValueEqual(actRepeatedly(helper, coral, BLEACH, 0, 50), 0, "bleaching under the conduit");
					helper.assertBlockPresent(Blocks.TUBE_CORAL_BLOCK, coral);
				})
				.thenSucceed();
	}

	// AC8: the bleaching game rule alone stops bleaching on real random ticks, with growth still on.
	@GameTest(environment = FAST_RULES, maxTicks = 600)
	public void bleachingRuleSwitchesBleachingOff(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		BlockPos coral = new BlockPos(3, 1, 3);
		helper.setBlock(coral, Blocks.TUBE_CORAL_BLOCK);
		helper.setBlock(new BlockPos(5, 1, 3), Blocks.MAGMA_BLOCK);
		ServerLevel level = helper.getLevel();
		Settings original = LivingReefsConfig.get();
		LivingReefsConfig.set(new Settings(0, 0, 1, 2, 2, 14, 3, 0, Settings.Boosts.DEFAULTS, false, LEVELS));
		level.getGameRules().set(ReefGameRules.CORAL_BLEACHING, false, level.getServer());
		helper.startSequence()
				// At speed 1024 a block gets a random tick about every 4 game ticks: about 25 chances in 100 ticks.
				.thenIdle(100)
				.thenExecute(() -> {
					helper.assertBlockPresent(Blocks.TUBE_CORAL_BLOCK, coral);
					level.getGameRules().set(ReefGameRules.CORAL_BLEACHING, true, level.getServer());
				})
				.thenWaitUntil(() -> helper.assertBlockPresent(Blocks.DEAD_TUBE_CORAL_BLOCK, coral))
				.thenExecute(() -> LivingReefsConfig.set(original))
				.thenSucceed();
	}

	// AC9: a closed, dark pool keeps coral dormant; a light beside it lets it thrive. A coral block is
	// opaque, so this only works when the light of the water around it counts.
	@GameTest(environment = QUIET, maxTicks = 200)
	public void darkCoralWaitsForLight(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 5);
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				for (int y = 1; y <= 6; y++) {
					if (y == 6 || x == 0 || x == 7 || z == 0 || z == 7) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
					}
				}
			}
		}
		BlockPos coral = new BlockPos(3, 1, 3);
		helper.setBlock(coral, Blocks.TUBE_CORAL_BLOCK);
		Settings growInDarkness = new Settings(1, 0, 0, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, false, LEVELS);
		ServerLevel level = helper.getLevel();
		BlockPos abs = helper.absolutePos(coral);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertValueEqual(assess(level, abs, growInDarkness).reasons().contains(ReefRules.Reason.TOO_DARK),
						true, "too dark in the closed pool"))
				.thenExecute(() -> {
					helper.assertValueEqual(actRepeatedly(helper, coral, growInDarkness, 0, 100), 0, "growths in the dark");
					helper.setBlock(new BlockPos(5, 3, 3), Blocks.GLOWSTONE);
				})
				.thenWaitUntil(() -> helper.assertValueEqual(assess(level, abs, growInDarkness).health(), Health.THRIVING, "health once lit"))
				.thenExecute(() -> helper.assertTrue(actRepeatedly(helper, coral, growInDarkness, 0, 100) > 0, "growth once lit"))
				.thenSucceed();
	}

	// AC6: a dead coral next to a heat source is not revived, or it would only bleach again.
	@GameTest(environment = QUIET)
	public void noRecoveryNextToHeat(GameTestHelper helper) {
		reefPool(helper, Blocks.SAND, 6);
		BlockPos source = new BlockPos(3, 1, 3);
		BlockPos dead = new BlockPos(3, 2, 3);
		helper.setBlock(source, Blocks.TUBE_CORAL_BLOCK);
		helper.setBlock(dead, waterlogged(Blocks.DEAD_TUBE_CORAL_FAN));
		// Two above the dead fan, three above the source: only the fan is near the heat.
		helper.setBlock(new BlockPos(3, 4, 3), Blocks.MAGMA_BLOCK);
		helper.assertValueEqual(assess(helper.getLevel(), helper.absolutePos(source), RECOVER).health(), Health.THRIVING, "source health");
		helper.assertValueEqual(actRepeatedly(helper, source, RECOVER, 0, 400), 0, "revivals next to heat");
		helper.assertBlockPresent(Blocks.DEAD_TUBE_CORAL_FAN, dead);
		helper.succeed();
	}

	// S1: without admin rights, /reef check takes only positions near the player, so it cannot be used
	// to find loaded chunks (and the players in them) far away.
	@GameTest(environment = QUIET)
	public void checkFarAwayNeedsAdmin(GameTestHelper helper) throws CommandSyntaxException {
		reefPool(helper, Blocks.SAND, 5);
		BlockPos coral = new BlockPos(3, 1, 3);
		helper.setBlock(coral, Blocks.TUBE_CORAL_BLOCK);
		BlockPos c = helper.absolutePos(coral);
		MinecraftServer server = helper.getLevel().getServer();
		CapturingSource capture = new CapturingSource();
		CommandSourceStack player = server.createCommandSourceStack().withLevel(helper.getLevel()).withSource(capture)
				.withPermission(LevelBasedPermissionSet.forLevel(PermissionLevel.ALL));
		var dispatcher = server.getCommands().getDispatcher();
		String command = "reef check %d %d %d".formatted(c.getX(), c.getY(), c.getZ());

		dispatcher.execute(command, player.withPosition(Vec3.atCenterOf(c).add(0, 5, 0)));
		helper.assertTrue(capture.take().contains("Tube Coral Block at"), "a nearby check works without admin");
		try {
			dispatcher.execute(command, player.withPosition(Vec3.atCenterOf(c).add(200, 0, 0)));
			helper.fail("a far check without admin must fail");
		} catch (CommandSyntaxException e) {
			helper.assertTrue(e.getMessage().contains("Stand within"), e.getMessage());
		}
		dispatcher.execute(command, server.createCommandSourceStack().withLevel(helper.getLevel()).withSource(capture)
				.withPosition(Vec3.atCenterOf(c).add(200, 0, 0)));
		helper.assertTrue(capture.take().contains("Tube Coral Block at"), "an admin may check from far away");
		helper.succeed();
	}

	private static ReefRules.Assessment assess(ServerLevel level, BlockPos pos, Settings settings) {
		return ReefRules.assess(ReefConditions.read(level, pos, level.getBlockState(pos)), settings);
	}

	private static Health health(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return ReefRules.assess(ReefConditions.read(level, pos, state), BLEACH).health();
	}

	// AC13: /reef check explains real coral: a stressed fan, a bleached one; /reef stats answers.
	@GameTest(environment = QUIET)
	public void checkCommandExplainsHealth(GameTestHelper helper) throws CommandSyntaxException {
		// The report reads the global config; start from the defaults whatever an earlier test left.
		LivingReefsConfig.set(Settings.DEFAULTS);
		reefPool(helper, Blocks.SAND, 5);
		BlockPos fan = new BlockPos(3, 1, 3);
		BlockPos bleached = new BlockPos(2, 1, 5);
		helper.setBlock(fan, waterlogged(Blocks.TUBE_CORAL_FAN));
		helper.setBlock(new BlockPos(5, 1, 3), Blocks.MAGMA_BLOCK);
		helper.setBlock(bleached, waterlogged(Blocks.DEAD_BRAIN_CORAL_FAN));
		MinecraftServer server = helper.getLevel().getServer();
		CapturingSource capture = new CapturingSource();
		CommandSourceStack source = server.createCommandSourceStack().withLevel(helper.getLevel()).withSource(capture);
		var dispatcher = server.getCommands().getDispatcher();

		BlockPos f = helper.absolutePos(fan);
		dispatcher.execute("reef check %d %d %d".formatted(f.getX(), f.getY(), f.getZ()), source);
		String report = capture.take();
		helper.assertTrue(report.contains("Tube Coral Fan at " + f.getX() + " " + f.getY() + " " + f.getZ() + " is stressed"), report);
		helper.assertTrue(report.contains("magma or lava within 2 blocks"), report);
		helper.assertTrue(report.contains("Bleaching is off") , "the quiet environment turns bleaching off:\n" + report);

		BlockPos b = helper.absolutePos(bleached);
		dispatcher.execute("livingreefs check %d %d %d".formatted(b.getX(), b.getY(), b.getZ()), source);
		String dead = capture.take();
		helper.assertTrue(dead.contains("is bleached") && dead.contains("recovers"), dead);

		dispatcher.execute("reef stats", source);
		helper.assertTrue(capture.take().contains("coral random ticks"), "stats answer");
		helper.succeed();
	}

	/** Collects what commands print, which a suppressed source never renders. */
	private static final class CapturingSource implements CommandSource {
		final StringBuilder out = new StringBuilder();

		@Override
		public void sendSystemMessage(Component message) {
			out.append(message.getString()).append('\n');
		}

		@Override
		public boolean acceptsSuccess() {
			return true;
		}

		@Override
		public boolean acceptsFailure() {
			return true;
		}

		@Override
		public boolean shouldInformAdmins() {
			return false;
		}

		String take() {
			String text = out.toString();
			out.setLength(0);
			return text;
		}
	}
}
