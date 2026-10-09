package io.github.razekteixeira.livingreefs.gametest.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import io.github.razekteixeira.livingreefs.LivingReefs;
import io.github.razekteixeira.livingreefs.LivingReefsCommands;
import io.github.razekteixeira.livingreefs.LivingReefsConfig;
import io.github.razekteixeira.livingreefs.ReefConditions;
import io.github.razekteixeira.livingreefs.ReefSpecies;
import io.github.razekteixeira.livingreefs.core.Settings;

/**
 * Captures the media for the project page in a real client: a coral restoration site next to a
 * natural warm ocean reef, its time-lapse ({@code timelapse_NNN}), bleaching beside a magma vent and
 * recovery after it is removed, {@code /reef check} in chat, and HUD-free {@code gallery_*} shots.
 * Every shot is taken under water, with the conduit power effect for clear water.
 *
 * <p>It also logs a survey of the light that natural reef coral receives ("[reef survey]"), which is
 * how the default {@code minLight} was calibrated.
 *
 * <p>Run with {@code ./gradlew runClientGameTest}; screenshots land in {@code build/run/clientGameTest/screenshots}.
 * Growth is sped up with {@code random_tick_speed} and higher chances for the time-lapse, bleaching and
 * recovery only (the {@code /reef check} shots use the defaults); every
 * coral change is the mod's own. Placed by hand: the cleared site, five starting fragments, sea
 * pickles, still tropical fish and the magma vent.
 */
public class LivingReefsCaptures implements FabricClientGameTest {
	private static final String SEED = "living-reefs";
	private static final int TIMELAPSE_FRAMES = 48;
	private static final int TICKS_PER_FRAME = 25;
	private static final int SEA_LEVEL = 63;
	/** The highest y the camera may take and stay under the surface. */
	private static final int UNDER_SURFACE = SEA_LEVEL - 3;
	private static final int SITE_RADIUS = 9;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(client -> client.options.guiScale().set(2));

		// Consistent settings would give a flat world; a real seed gives a real warm ocean.
		try (TestSingleplayerContext world = context.worldBuilder()
				.setUseConsistentSettings(false)
				.adjustSettings(settings -> {
					settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
					settings.setAllowCommands(true);
					settings.setSeed(SEED);
				})
				.create()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamerule advance_time false");
			server.runCommand("gamerule advance_weather false");
			server.runCommand("gamerule spawn_mobs false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamemode spectator @p");
			server.runCommand("effect give @p night_vision infinite 0 true");
			server.runCommand("effect give @p conduit_power infinite 0 true");

			BlockPos ocean = server.computeOnServer(LivingReefsCaptures::findWarmOcean);
			teleport(context, world, server, ocean.atY(SEA_LEVEL + 20), ocean.atY(SEA_LEVEL));
			BlockPos reef = server.computeOnServer(s -> deepReef(s, ocean));
			server.runOnServer(s -> survey(s, reef));
			BlockPos site = server.computeOnServer(s -> restorationSite(s, reef));
			int floor = site.getY();
			LivingReefs.LOGGER.info("[captures] reef at {}, restoration site at {}", reef, site);

			// The natural reef as it generated.
			cinematic(context, world, server, under(reef.offset(-7, 3, -7)), reef.offset(0, 1, 0), 6000, "gallery_reef");

			// Time-lapse: five fragments planted on cleared sand grow into a reef.
			Settings original = LivingReefsConfig.get();
			server.runOnServer(s -> LivingReefsConfig.set(new Settings(0.4, 0.05, 0.6, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true,
					Settings.PermissionLevels.DEFAULTS)));
			teleport(context, world, server, under(site.offset(0, 5, -10)), site.offset(0, 1, 1));
			hideHud(context);
			server.runCommand("gamerule random_tick_speed 256");
			for (int frame = 0; frame < TIMELAPSE_FRAMES; frame++) {
				context.takeScreenshot("timelapse_%03d".formatted(frame));
				context.waitTicks(TICKS_PER_FRAME);
			}
			server.runCommand("gamerule random_tick_speed 3");
			showHud(context);
			server.runOnServer(s -> fish(s, site));
			// The chat shots show what a player would see: default settings and random tick speed.
			server.runOnServer(s -> LivingReefsConfig.set(original));

			// /reef check on a thriving coral next to sea pickles, with tropical fish around.
			BlockPos thriving = server.computeOnServer(s -> growingCoral(s, site.offset(0, 1, 0)));
			teleport(context, world, server, thriving.offset(0, 1, -3), thriving);
			// A fish right in front of the camera would cover the chat; the ones further away stay.
			BlockPos eye = thriving.offset(0, 1, -3);
			server.runCommand("kill @e[type=minecraft:tropical_fish,x=%d,y=%d,z=%d,distance=..3]".formatted(eye.getX(), eye.getY(), eye.getZ()));
			context.waitTicks(10);
			clearChat(context);
			typeCommand(context, world, "reef check");
			context.takeScreenshot("check");
			// The same report through the server, so its exact text lands in the log for the site's chat panel.
			server.runCommand("reef check %d %d %d".formatted(thriving.getX(), thriving.getY(), thriving.getZ()));

			// Bleaching: a magma vent opens under the east side of the restored reef, then closes again.
			BlockPos ventEye = under(site.offset(2, 4, -10));
			BlockPos ventView = site.offset(2, 1, 0);
			cinematic(context, world, server, ventEye, ventView, 6000, "bleach_before");
			String vent = "%d %d %d %d %d %d".formatted(site.getX() + 1, floor, site.getZ() - 3, site.getX() + 4, floor, site.getZ() + 1);
			server.runCommand("fill " + vent + " minecraft:magma_block");
			// /reef check on a living coral right above the vent, before it bleaches.
			BlockPos stressed = server.computeOnServer(s -> nearestCoral(s, site.offset(2, 1, -1)));
			teleport(context, world, server, stressed.offset(0, 1, -3), stressed);
			clearChat(context);
			typeCommand(context, world, "reef check");
			context.takeScreenshot("check_stressed");
			server.runCommand("reef check %d %d %d".formatted(stressed.getX(), stressed.getY(), stressed.getZ()));
			clearChat(context);
			server.runOnServer(s -> LivingReefsConfig.set(new Settings(0.4, 0.05, 0.6, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true,
					Settings.PermissionLevels.DEFAULTS)));
			server.runCommand("gamerule random_tick_speed 256");
			context.waitTicks(300);
			server.runCommand("gamerule random_tick_speed 3");

			// The vent closes: the bleached patch shows clearly, then living coral takes it back.
			server.runCommand("fill " + vent + " minecraft:sand");
			server.runCommand("kill @e[type=minecraft:item]");
			context.waitTicks(60);
			cinematic(context, world, server, ventEye, ventView, 6000, "bleach_after");
			server.runOnServer(s -> LivingReefsConfig.set(new Settings(0.05, 0.8, 0.6, 2, 2, 14, 3, 1, Settings.Boosts.DEFAULTS, true,
					Settings.PermissionLevels.DEFAULTS)));
			server.runCommand("gamerule random_tick_speed 256");
			context.waitTicks(2400);
			server.runCommand("gamerule random_tick_speed 3");
			cinematic(context, world, server, ventEye, ventView, 6000, "recovered");

			// Gallery: the restored reef up close, and after dark lit by sea pickles.
			server.runOnServer(s -> LivingReefsConfig.set(original));
			cinematic(context, world, server, under(site.offset(-6, 2, -7)), site.offset(0, 1, 0), 6000, "gallery_restored");
			server.runOnServer(s -> pickles(s, site));
			// Conduit power sees in the dark under water just like night vision, so both go for the night shot.
			server.runCommand("effect clear @p night_vision");
			server.runCommand("effect clear @p conduit_power");
			// Default brightness: full brightness made the night shot look like day.
			context.runOnClient(client -> client.options.gamma().set(0.5));
			cinematic(context, world, server, under(site.offset(5, 3, -8)), site.offset(0, 1, 0), 18000, "gallery_night");
		}
	}

	private static BlockPos under(BlockPos eye) {
		return eye.getY() > UNDER_SURFACE ? eye.atY(UNDER_SURFACE) : eye;
	}

	private static BlockPos findWarmOcean(MinecraftServer server) {
		ServerLevel level = server.overworld();
		Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(biome -> biome.is(Biomes.WARM_OCEAN),
				BlockPos.ZERO.atY(SEA_LEVEL), 12_800, 32, 64);
		if (found == null) {
			throw new AssertionError("no warm ocean within 12800 blocks of seed " + SEED);
		}
		return found.getFirst();
	}

	/** The coral at least six blocks under the surface with the most coral within six blocks: a deep, dense reef. */
	private static BlockPos deepReef(MinecraftServer server, BlockPos around) {
		List<BlockPos> coral = coralNear(server.overworld(), around, 48);
		List<BlockPos> deep = coral.stream().filter(pos -> SEA_LEVEL - 1 - pos.getY() >= 6).toList();
		if (deep.isEmpty()) {
			throw new AssertionError("no coral six blocks deep near " + around);
		}
		BlockPos best = deep.getFirst();
		long bestCount = -1;
		for (BlockPos candidate : deep) {
			long count = coral.stream().filter(other -> other.distSqr(candidate) <= 36).count();
			if (count > bestCount) {
				bestCount = count;
				best = candidate;
			}
		}
		ServerLevel level = server.overworld();
		return best.atY(level.getHeight(Heightmap.Types.OCEAN_FLOOR, best.getX(), best.getZ()) - 1);
	}

	private static List<BlockPos> coralNear(ServerLevel level, BlockPos around, int radius) {
		List<BlockPos> found = new ArrayList<>();
		ReefSpecies species = ReefSpecies.get();
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR, around.getX() + x, around.getZ() + z);
				for (int y = top - 6; y <= top + 3; y++) {
					BlockPos pos = new BlockPos(around.getX() + x, y, around.getZ() + z);
					var entry = species.entry(level.getBlockState(pos).getBlock());
					if (entry.isPresent() && !entry.get().dead()) {
						found.add(pos);
					}
				}
			}
		}
		return found;
	}

	/** Logs the light natural reef coral gets, to calibrate the {@code minLight} default. */
	private static void survey(MinecraftServer server, BlockPos reef) {
		ServerLevel level = server.overworld();
		List<BlockPos> coral = coralNear(level, reef, 40);
		int[] light = coral.stream().mapToInt(pos -> ReefConditions.lightAt(level, pos)).sorted().toArray();
		int[] depth = coral.stream().mapToInt(pos -> SEA_LEVEL - 1 - pos.getY()).sorted().toArray();
		long dark = Arrays.stream(light).filter(l -> l < 1).count();
		LivingReefs.LOGGER.info("[reef survey] coral={} light min={} median={} max={} below1={} depth min={} median={} max={}",
				light.length, light[0], light[light.length / 2], light[light.length - 1], dark,
				depth[0], depth[depth.length / 2], depth[depth.length - 1]);
	}

	/**
	 * Finds open water 20 to 40 blocks from the reef (water at the surface over every column of a
	 * 19 x 19 patch, at least six blocks deep), levels it to sand at its median depth on a sandstone
	 * base (so the sand cannot fall into deeper water below), and plants five coral fragments and two
	 * sea pickles: a player restoring a reef.
	 */
	private static BlockPos restorationSite(MinecraftServer server, BlockPos reef) {
		ServerLevel level = server.overworld();
		BlockPos site = null;
		int floor = 0;
		search:
		for (int distance = 20; distance <= 40; distance += 5) {
			for (int[] dir : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
				BlockPos candidate = reef.offset(dir[0] * distance, 0, dir[1] * distance);
				int median = openWaterFloor(level, candidate);
				if (median > 0) {
					site = candidate;
					floor = median;
					break search;
				}
			}
		}
		if (site == null) {
			throw new AssertionError("no open water next to the reef at " + reef);
		}
		var commands = server.getCommands();
		var source = server.createCommandSourceStack().withLevel(level).withSuppressedOutput();
		int x = site.getX();
		int z = site.getZ();
		int r = SITE_RADIUS;
		commands.performPrefixedCommand(source, "fill %d %d %d %d %d %d minecraft:sandstone".formatted(x - r, floor - 4, z - r, x + r, floor - 1, z + r));
		commands.performPrefixedCommand(source, "fill %d %d %d %d %d %d minecraft:sand".formatted(x - r, floor, z - r, x + r, floor, z + r));
		commands.performPrefixedCommand(source, "fill %d %d %d %d %d %d minecraft:water".formatted(x - r, floor + 1, z - r, x + r, SEA_LEVEL - 1, z + r));
		String[] fragments = {"tube_coral_block", "brain_coral_block", "fire_coral_block", "horn_coral_block", "bubble_coral_block"};
		int[][] spots = {{-3, -2}, {3, -2}, {0, 2}, {-3, 4}, {3, 4}};
		for (int i = 0; i < fragments.length; i++) {
			commands.performPrefixedCommand(source, "setblock %d %d %d minecraft:%s".formatted(x + spots[i][0], floor + 1, z + spots[i][1], fragments[i]));
		}
		commands.performPrefixedCommand(source, "setblock %d %d %d minecraft:sea_pickle[pickles=4,waterlogged=true]".formatted(x - 1, floor + 1, z));
		commands.performPrefixedCommand(source, "setblock %d %d %d minecraft:sea_pickle[pickles=3,waterlogged=true]".formatted(x + 1, floor + 1, z + 1));
		return site.atY(floor);
	}

	/** The median sea floor height when every column of the patch is open water, otherwise 0. */
	private static int openWaterFloor(ServerLevel level, BlockPos centre) {
		List<Integer> floors = new ArrayList<>();
		for (int dx = -SITE_RADIUS; dx <= SITE_RADIUS; dx++) {
			for (int dz = -SITE_RADIUS; dz <= SITE_RADIUS; dz++) {
				int x = centre.getX() + dx;
				int z = centre.getZ() + dz;
				if (!level.getBlockState(new BlockPos(x, SEA_LEVEL - 1, z)).is(Blocks.WATER)
						|| level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) > SEA_LEVEL) {
					return 0;
				}
				floors.add(level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1);
			}
		}
		floors.sort(null);
		int median = floors.get(floors.size() / 2);
		return median <= SEA_LEVEL - 7 && median >= SEA_LEVEL - 14 ? median : 0;
	}

	/** Still tropical fish in the common vanilla colours, around the site. */
	private static void fish(MinecraftServer server, BlockPos site) {
		var commands = server.getCommands();
		var source = server.createCommandSourceStack().withLevel(server.overworld()).withSuppressedOutput();
		int[][] spots = {{-2, 3, -1}, {1, 2, -2}, {2, 4, 1}, {-1, 3, 2}, {4, 2, 0}, {-4, 2, 1}};
		for (int i = 0; i < spots.length; i++) {
			int variant = TropicalFish.COMMON_VARIANTS.get(i * 3 % TropicalFish.COMMON_VARIANTS.size()).getPackedId();
			commands.performPrefixedCommand(source, "summon minecraft:tropical_fish %d %d %d {NoAI:1b,Variant:%d,Rotation:[%d.0f,0.0f]}".formatted(
					site.getX() + spots[i][0], site.getY() + spots[i][1], site.getZ() + spots[i][2], variant, i * 60));
		}
	}

	/** Puts a group of four sea pickles on every fourth exposed coral block of the site, for the night shot. */
	private static void pickles(MinecraftServer server, BlockPos site) {
		ServerLevel level = server.overworld();
		int placed = 0;
		for (BlockPos pos : coralNear(level, site, SITE_RADIUS)) {
			boolean block = level.getBlockState(pos).getBlock().getDescriptionId().endsWith("_coral_block");
			if (block && level.getBlockState(pos.above()).is(Blocks.WATER) && placed++ % 4 == 0) {
				level.setBlockAndUpdate(pos.above(), Blocks.SEA_PICKLE.defaultBlockState()
						.setValue(net.minecraft.world.level.block.SeaPickleBlock.PICKLES, 4));
			}
		}
	}

	/**
	 * The thriving coral nearest to {@code around} that still has room to spread, found with the
	 * report {@code /reef check} prints, so the check shot shows a coral that is still growing.
	 */
	private static BlockPos growingCoral(MinecraftServer server, BlockPos around) {
		ServerLevel level = server.overworld();
		ReefSpecies species = ReefSpecies.get();
		return coralNear(level, around, 8).stream()
				.sorted(Comparator.comparingDouble(pos -> pos.distSqr(around)))
				.filter(pos -> {
					BlockState state = level.getBlockState(pos);
					return species.entry(state.getBlock()).map(entry -> LivingReefsCommands.report(level, pos, state, entry).stream()
							.map(Component::getString).collect(Collectors.joining("\n"))).filter(text -> text.contains(" is thriving")
							&& text.contains(" to spread to")).isPresent();
				})
				.findFirst()
				.orElseThrow(() -> new AssertionError("no thriving coral with room to spread near " + around));
	}

	private static BlockPos nearestCoral(MinecraftServer server, BlockPos around) {
		return coralNear(server.overworld(), around, 8).stream()
				.min((a, b) -> Double.compare(a.distSqr(around), b.distSqr(around)))
				.orElseThrow(() -> new AssertionError("no living coral near " + around));
	}

	private static void teleport(ClientGameTestContext context, TestSingleplayerContext world, TestServerContext server, BlockPos eye, BlockPos target) {
		server.runCommand("tp @p %.1f %d %.1f".formatted(eye.getX() + 0.5, eye.getY(), eye.getZ() + 0.5));
		context.waitTicks(10);
		context.getInput().lookAt(target);
		world.getConnection().waitForChunksRender();
		context.waitTicks(10);
	}

	/** A HUD-free shot from {@code eye} towards {@code target} at the given time of day. */
	private static void cinematic(ClientGameTestContext context, TestSingleplayerContext world, TestServerContext server,
			BlockPos eye, BlockPos target, int timeOfDay, String name) {
		server.runCommand("time set " + timeOfDay);
		teleport(context, world, server, eye, target);
		hideHud(context);
		context.waitTicks(20);
		context.takeScreenshot(name);
		showHud(context);
	}

	private static void hideHud(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (!client.gui.hud.isHidden()) {
				client.gui.hud.toggle();
			}
		});
	}

	private static void showHud(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (client.gui.hud.isHidden()) {
				client.gui.hud.toggle();
			}
		});
	}

	private static void typeCommand(ClientGameTestContext context, TestSingleplayerContext world, String command) {
		context.getInput().pressKey(options -> options.keyChat);
		context.getInput().typeChars("/" + command);
		context.getInput().holdKeyFor(InputConstants.KEY_RETURN, 0);
		world.getConnection().waitForServerboundPackets();
		world.getConnection().waitForClientboundPackets();
		context.waitTicks(2);
	}

	private static void clearChat(ClientGameTestContext context) {
		context.runOnClient(client -> client.gui.hud.getChat().clearMessages(false));
	}
}
