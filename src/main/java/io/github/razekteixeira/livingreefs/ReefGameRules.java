package io.github.razekteixeira.livingreefs;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;

/**
 * Per-world switches, like vanilla's {@code spread_vines}. Game rules are a server-side registry
 * keyed by name, so vanilla clients can join; they only miss these rules in the game rules screen.
 */
public final class ReefGameRules {
	/** {@code /gamerule livingreefs:coral_growth false} stops spreading and recovery. */
	public static final GameRule<Boolean> CORAL_GROWTH = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.UPDATES)
			.buildAndRegister(Identifier.fromNamespaceAndPath(LivingReefs.MOD_ID, "coral_growth"));

	/** {@code /gamerule livingreefs:coral_bleaching false} stops bleaching. */
	public static final GameRule<Boolean> CORAL_BLEACHING = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.UPDATES)
			.buildAndRegister(Identifier.fromNamespaceAndPath(LivingReefs.MOD_ID, "coral_bleaching"));

	private ReefGameRules() {
	}

	/** Loads the class, which registers the rules. */
	static void register() {
	}
}
