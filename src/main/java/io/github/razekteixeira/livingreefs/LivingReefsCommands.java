package io.github.razekteixeira.livingreefs;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Locale;
import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import me.lucko.fabric.api.permissions.v0.Permissions;

import io.github.razekteixeira.livingreefs.core.Conditions;
import io.github.razekteixeira.livingreefs.core.Health;
import io.github.razekteixeira.livingreefs.core.ReefRules;
import io.github.razekteixeira.livingreefs.core.Settings;
import io.github.razekteixeira.livingreefs.core.SpeciesTable;
import io.github.razekteixeira.livingreefs.core.SpreadPlanner;

/** {@code /livingreefs}, alias {@code /reef}: checking a coral and the statistics are open to everyone, reload needs level 3. */
public final class LivingReefsCommands {
	private static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(
			Component.literal("Look at a coral or give a position"));
	private static final SimpleCommandExceptionType NOT_CORAL = new SimpleCommandExceptionType(
			Component.literal("That block is not coral"));
	/** Without admin rights a position must be this close, so the command cannot map loaded chunks far away. */
	static final int CHECK_RANGE = 16;
	private static final SimpleCommandExceptionType TOO_FAR = new SimpleCommandExceptionType(
			Component.literal("Stand within " + CHECK_RANGE + " blocks of the coral to check it"));

	private LivingReefsCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		var root = dispatcher.register(literal("livingreefs").requires(LivingReefsCommands::canUse)
				.then(literal("check")
						.executes(c -> check(c, lookedAt(c)))
						.then(argument("pos", BlockPosArgument.blockPos())
								.executes(c -> check(c, nearbyPos(c)))))
				.then(literal("stats").executes(LivingReefsCommands::stats))
				.then(literal("reload").requires(LivingReefsCommands::canAdmin).executes(LivingReefsCommands::reload)));
		dispatcher.register(literal("reef").requires(LivingReefsCommands::canUse).redirect(root));
	}

	// Permission nodes win when a permissions mod is installed; otherwise the configured vanilla levels apply.
	// Levels are read on every check so /livingreefs reload takes effect immediately.

	static boolean canUse(CommandSourceStack source) {
		return Permissions.check(source, "livingreefs.use", PermissionLevel.byId(LivingReefsConfig.get().permissionLevels().use()));
	}

	static boolean canAdmin(CommandSourceStack source) {
		return Permissions.check(source, "livingreefs.admin", PermissionLevel.byId(LivingReefsConfig.get().permissionLevels().admin()));
	}

	private static BlockPos lookedAt(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		ServerPlayer player = c.getSource().getPlayer();
		if (player == null) {
			throw NO_TARGET.create();
		}
		// Fluids are skipped, so looking through water at a coral finds the coral.
		HitResult hit = player.pick(player.blockInteractionRange(), 1.0f, false);
		if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
			return blockHit.getBlockPos();
		}
		throw NO_TARGET.create();
	}

	/**
	 * The {@code pos} argument. Admins may check any loaded position; everyone else only one near
	 * them, checked before the loaded test so far positions all give the same answer.
	 */
	private static BlockPos nearbyPos(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		CommandSourceStack source = c.getSource();
		BlockPos pos = BlockPosArgument.getBlockPos(c, "pos");
		if (!canAdmin(source) && !Vec3.atCenterOf(pos).closerThan(source.getPosition(), CHECK_RANGE)) {
			throw TOO_FAR.create();
		}
		return BlockPosArgument.getLoadedBlockPos(c, "pos");
	}

	private static int check(CommandContext<CommandSourceStack> c, BlockPos pos) throws CommandSyntaxException {
		ServerLevel level = c.getSource().getLevel();
		BlockState state = level.getBlockState(pos);
		ReefSpecies species = ReefSpecies.get();
		Optional<SpeciesTable.Entry> entry = species.entry(state.getBlock());
		if (entry.isEmpty()) {
			throw NOT_CORAL.create();
		}
		for (Component line : report(level, pos, state, entry.get())) {
			c.getSource().sendSuccess(() -> line, false);
		}
		return 1;
	}

	/** The lines of {@code /reef check}: health, every condition behind it, and what to expect next. */
	public static java.util.List<Component> report(ServerLevel level, BlockPos pos, BlockState state, SpeciesTable.Entry entry) {
		java.util.List<Component> lines = new java.util.ArrayList<>();
		MutableComponent head = Component.empty().append(state.getBlock().getName().copy().withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" at %d %d %d".formatted(pos.getX(), pos.getY(), pos.getZ())).withStyle(ChatFormatting.GRAY));
		if (entry.dead()) {
			boolean wet = WorldReefView.isWet(level, pos, state);
			lines.add(head.append(Component.literal(" is bleached").withStyle(ChatFormatting.WHITE)));
			lines.add(Component.literal(wet
					? "  It recovers when a thriving coral of the same kind is right next to it, away from heat."
					: "  It is dry, so it stays dead.").withStyle(ChatFormatting.GRAY));
			return lines;
		}
		Settings settings = LivingReefsConfig.get();
		Conditions conditions = ReefConditions.read(level, pos, state);
		ReefRules.Assessment assessment = ReefRules.assess(conditions, settings);
		ChatFormatting colour = switch (assessment.health()) {
			case THRIVING -> ChatFormatting.GREEN;
			case DORMANT -> ChatFormatting.YELLOW;
			case STRESSED -> ChatFormatting.RED;
		};
		lines.add(head.append(Component.literal(" is ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(assessment.health().name().toLowerCase(Locale.ROOT)).withStyle(colour, ChatFormatting.BOLD)));
		for (ReefRules.Reason reason : assessment.reasons()) {
			lines.add(reasonLine(reason, conditions, settings));
		}
		// The same test a growth attempt makes at each spot it can pick, including the density cap there.
		// Only a thriving coral spreads, so only then does it matter where it could go.
		int open = 0;
		if (assessment.health() == Health.THRIVING) {
			ReefSpecies species = ReefSpecies.get();
			open = SpreadPlanner.openSpots(new WorldReefView(level, pos, species), species.table(), species.coralId(state.getBlock()), settings);
			lines.add(Component.literal(open == 0 ? "  No room to spread nearby"
					: "  %d open spot%s to spread to".formatted(open, open == 1 ? "" : "s")).withStyle(ChatFormatting.GRAY));
		}
		lines.add(outlook(level, assessment, settings, conditions.wet(), open));
		return lines;
	}

	private static Component reasonLine(ReefRules.Reason reason, Conditions c, Settings s) {
		String good = "  ✔ ";
		String bad = "  ✘ ";
		String boost = "  + ";
		return switch (reason) {
			case DRY -> line(bad, "out of water", ChatFormatting.RED);
			case HEAT -> line(bad, "magma or lava within " + ReefConditions.NEAR + " blocks", ChatFormatting.RED);
			case FROZEN_WATER -> line(bad, "frozen water", ChatFormatting.RED);
			case CONDUIT_SHELTER -> line(good, "an active conduit shelters it from bleaching", ChatFormatting.AQUA);
			case REEF_WATER -> line(good, "warm reef water", ChatFormatting.GREEN);
			case CONDUIT_WATER -> line(good, "an active conduit makes this water a reef", ChatFormatting.GREEN);
			case NEUTRAL_WATER -> line(bad, "not reef water (warm oceans, or near an active conduit)", ChatFormatting.YELLOW);
			case TOO_DARK -> line(bad, "light " + c.light() + ", needs " + s.minLight() + " (sea pickles help)", ChatFormatting.YELLOW);
			case LIGHT_OK -> line(good, "light " + c.light() + ", needs " + s.minLight(), ChatFormatting.GREEN);
			case FISH -> line(boost, c.tropicalFish() + " tropical fish nearby (x" + trim(s.boosts().tropicalFish()) + ")", ChatFormatting.AQUA);
			case SEA_PICKLES -> line(boost, c.seaPickles() + " sea pickles nearby (x" + trim(s.boosts().seaPickles()) + ")", ChatFormatting.AQUA);
			case CONDUIT_BOOST -> line(boost, "active conduit (x" + trim(s.boosts().conduit()) + ")", ChatFormatting.AQUA);
		};
	}

	private static Component line(String mark, String text, ChatFormatting colour) {
		return Component.literal(mark).withStyle(colour).append(Component.literal(text).withStyle(ChatFormatting.GRAY));
	}

	/** Expected time until the next growth attempt or bleaching, from the random tick speed and the chance. */
	private static Component outlook(ServerLevel level, ReefRules.Assessment assessment, Settings settings, boolean wet, int open) {
		GameRules rules = level.getGameRules();
		int speed = rules.get(GameRules.RANDOM_TICK_SPEED);
		String text;
		if (!wet) {
			text = "Out of water: it dies like any vanilla coral";
		} else if (assessment.health() == Health.THRIVING && open == 0) {
			text = "Waits for room to spread";
		} else if (assessment.health() == Health.THRIVING) {
			double seconds = expectedSeconds(speed, assessment.growthChance());
			text = !rules.get(ReefGameRules.CORAL_GROWTH) ? "Growth is off (gamerule livingreefs:coral_growth)"
					: seconds < 0 ? "Never spreads with the current random_tick_speed and growthChance"
					: "Tries to spread about every " + duration(seconds) + " while players are near";
		} else if (assessment.health() == Health.STRESSED) {
			double seconds = expectedSeconds(speed, settings.bleachChance());
			text = !rules.get(ReefGameRules.CORAL_BLEACHING) ? "Bleaching is off (gamerule livingreefs:coral_bleaching)"
					: seconds < 0 ? "Never bleaches with the current random_tick_speed and bleachChance"
					: "Bleaches in about " + duration(seconds) + " if nothing changes";
		} else {
			text = "Stays as it is";
		}
		return Component.literal("  " + text).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
	}

	/**
	 * Mean seconds until an action with {@code chance} per random tick happens, or -1 for never. Each
	 * block of a section is picked with chance speed/4096 per game tick.
	 */
	static double expectedSeconds(int randomTickSpeed, double chance) {
		if (randomTickSpeed <= 0 || chance <= 0) {
			return -1;
		}
		return 4096.0 / (randomTickSpeed * chance) / 20.0;
	}

	static String duration(double seconds) {
		if (seconds < 90) {
			return Math.round(seconds) + " s";
		}
		if (seconds < 90 * 60) {
			return Math.round(seconds / 60) + " min";
		}
		return Math.round(seconds / 3600) + " h";
	}

	private static String trim(double value) {
		return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
	}

	private static int stats(CommandContext<CommandSourceStack> c) {
		ReefStats s = ReefTicker.STATS;
		c.getSource().sendSuccess(() -> Component.literal("Living Reefs since start: %,d coral random ticks, %,d checked, %,d grown, %,d recovered, %,d bleached"
				.formatted(s.randomTicks(), s.assessed(), s.grown(), s.recovered(), s.bleached())), false);
		c.getSource().sendSuccess(() -> Component.literal("Handler time: mean %.2f µs, max %.1f µs per tick; last second %.3f ms over %d ticks"
				.formatted(s.meanNanos() / 1000.0, s.maxNanos() / 1000.0, s.lastSecondNanos() / 1e6, s.lastSecondTicks()))
				.withStyle(ChatFormatting.GRAY), false);
		return (int) Math.min(Integer.MAX_VALUE, s.grown());
	}

	private static int reload(CommandContext<CommandSourceStack> c) {
		String status = LivingReefsConfig.load();
		// Permission levels may have changed: resend command trees so tab completion matches.
		MinecraftServer server = c.getSource().getServer();
		server.getPlayerList().getPlayers().forEach(player -> server.getCommands().sendCommands(player));
		c.getSource().sendSuccess(() -> Component.literal(status), true);
		return 1;
	}
}
