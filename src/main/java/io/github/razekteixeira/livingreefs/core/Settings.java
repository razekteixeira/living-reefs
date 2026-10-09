package io.github.razekteixeira.livingreefs.core;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Server owner settings, read from {@code config/livingreefs.json}. Missing fields take their
 * defaults and out-of-range values are clamped, so a partial or hand-edited file always works.
 *
 * <p>Chances apply per random tick of a coral block. At the default {@code random_tick_speed} of 3 a
 * block gets a random tick about every 68 seconds, so a growth chance of 0.04 means a thriving coral
 * tries to spread about once every 28 minutes of loaded game time.
 */
public record Settings(
		double growthChance,
		double recoveryChance,
		double bleachChance,
		int spreadRadius,
		int minWaterAbove,
		int maxNearbyCoral,
		int maxReefHeight,
		int minLight,
		Boosts boosts,
		boolean particles,
		PermissionLevels permissionLevels) {

	/** Growth chance multipliers for what helps a reef: grazing fish, sea pickles, an active conduit. */
	public record Boosts(double tropicalFish, double seaPickles, double conduit) {
		public static final Boosts DEFAULTS = new Boosts(1.5, 1.5, 2.0);

		static final Codec<Boosts> WRITE_CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.DOUBLE.fieldOf("tropicalFish").forGetter(Boosts::tropicalFish),
				Codec.DOUBLE.fieldOf("seaPickles").forGetter(Boosts::seaPickles),
				Codec.DOUBLE.fieldOf("conduit").forGetter(Boosts::conduit)
		).apply(i, Boosts::new));

		static final Codec<Boosts> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.DOUBLE.optionalFieldOf("tropicalFish", DEFAULTS.tropicalFish).forGetter(Boosts::tropicalFish),
				Codec.DOUBLE.optionalFieldOf("seaPickles", DEFAULTS.seaPickles).forGetter(Boosts::seaPickles),
				Codec.DOUBLE.optionalFieldOf("conduit", DEFAULTS.conduit).forGetter(Boosts::conduit)
		).apply(i, Boosts::new));

		/** The largest multiplier all boosts together can give. */
		public double max() {
			return tropicalFish * seaPickles * conduit;
		}
	}

	/**
	 * Vanilla permission levels used when no permissions mod is installed. With one (for example
	 * LuckPerms), the nodes {@code livingreefs.use} and {@code livingreefs.admin} take precedence.
	 */
	public record PermissionLevels(int use, int admin) {
		public static final PermissionLevels DEFAULTS = new PermissionLevels(0, 3);

		static final Codec<PermissionLevels> WRITE_CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("use").forGetter(PermissionLevels::use),
				Codec.INT.fieldOf("admin").forGetter(PermissionLevels::admin)
		).apply(i, PermissionLevels::new));

		static final Codec<PermissionLevels> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.optionalFieldOf("use", DEFAULTS.use).forGetter(PermissionLevels::use),
				Codec.INT.optionalFieldOf("admin", DEFAULTS.admin).forGetter(PermissionLevels::admin)
		).apply(i, PermissionLevels::new));
	}

	public static final Settings DEFAULTS = new Settings(0.04, 0.04, 0.25, 2, 2, 14, 3, 1, Boosts.DEFAULTS, true,
			PermissionLevels.DEFAULTS);

	/**
	 * Writes every field, including defaults, so the generated file documents all options.
	 * ({@code optionalFieldOf} would omit values equal to their default.)
	 */
	public static final Codec<Settings> WRITE_CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.DOUBLE.fieldOf("growthChance").forGetter(Settings::growthChance),
			Codec.DOUBLE.fieldOf("recoveryChance").forGetter(Settings::recoveryChance),
			Codec.DOUBLE.fieldOf("bleachChance").forGetter(Settings::bleachChance),
			Codec.INT.fieldOf("spreadRadius").forGetter(Settings::spreadRadius),
			Codec.INT.fieldOf("minWaterAbove").forGetter(Settings::minWaterAbove),
			Codec.INT.fieldOf("maxNearbyCoral").forGetter(Settings::maxNearbyCoral),
			Codec.INT.fieldOf("maxReefHeight").forGetter(Settings::maxReefHeight),
			Codec.INT.fieldOf("minLight").forGetter(Settings::minLight),
			Boosts.WRITE_CODEC.fieldOf("boosts").forGetter(Settings::boosts),
			Codec.BOOL.fieldOf("particles").forGetter(Settings::particles),
			PermissionLevels.WRITE_CODEC.fieldOf("permissionLevels").forGetter(Settings::permissionLevels)
	).apply(i, Settings::new));

	/** Reads leniently: every field is optional. */
	public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.DOUBLE.optionalFieldOf("growthChance", DEFAULTS.growthChance).forGetter(Settings::growthChance),
			Codec.DOUBLE.optionalFieldOf("recoveryChance", DEFAULTS.recoveryChance).forGetter(Settings::recoveryChance),
			Codec.DOUBLE.optionalFieldOf("bleachChance", DEFAULTS.bleachChance).forGetter(Settings::bleachChance),
			Codec.INT.optionalFieldOf("spreadRadius", DEFAULTS.spreadRadius).forGetter(Settings::spreadRadius),
			Codec.INT.optionalFieldOf("minWaterAbove", DEFAULTS.minWaterAbove).forGetter(Settings::minWaterAbove),
			Codec.INT.optionalFieldOf("maxNearbyCoral", DEFAULTS.maxNearbyCoral).forGetter(Settings::maxNearbyCoral),
			Codec.INT.optionalFieldOf("maxReefHeight", DEFAULTS.maxReefHeight).forGetter(Settings::maxReefHeight),
			Codec.INT.optionalFieldOf("minLight", DEFAULTS.minLight).forGetter(Settings::minLight),
			Boosts.CODEC.optionalFieldOf("boosts", Boosts.DEFAULTS).forGetter(Settings::boosts),
			Codec.BOOL.optionalFieldOf("particles", DEFAULTS.particles).forGetter(Settings::particles),
			PermissionLevels.CODEC.optionalFieldOf("permissionLevels", PermissionLevels.DEFAULTS).forGetter(Settings::permissionLevels)
	).apply(i, Settings::new));

	/** Settings with every value clamped to its allowed range, plus one warning per clamped value. */
	public record Checked(Settings settings, List<String> warnings) {
	}

	public Checked checked() {
		List<String> warnings = new ArrayList<>();
		Settings clamped = new Settings(
				clamp("growthChance", growthChance, 0, 1, warnings),
				clamp("recoveryChance", recoveryChance, 0, 1, warnings),
				clamp("bleachChance", bleachChance, 0, 1, warnings),
				clamp("spreadRadius", spreadRadius, 1, 4, warnings),
				clamp("minWaterAbove", minWaterAbove, 1, 8, warnings),
				clamp("maxNearbyCoral", maxNearbyCoral, 1, 124, warnings),
				clamp("maxReefHeight", maxReefHeight, 1, 8, warnings),
				clamp("minLight", minLight, 0, 15, warnings),
				new Boosts(
						clamp("boosts.tropicalFish", boosts.tropicalFish, 1, 10, warnings),
						clamp("boosts.seaPickles", boosts.seaPickles, 1, 10, warnings),
						clamp("boosts.conduit", boosts.conduit, 1, 10, warnings)),
				particles,
				new PermissionLevels(
						clamp("permissionLevels.use", permissionLevels.use, 0, 4, warnings),
						clamp("permissionLevels.admin", permissionLevels.admin, 0, 4, warnings)));
		return new Checked(clamped, List.copyOf(warnings));
	}

	private static int clamp(String name, int value, int min, int max, List<String> warnings) {
		int clamped = Math.max(min, Math.min(max, value));
		if (clamped != value) {
			warnings.add(name + " = " + value + " is outside " + min + ".." + max + ", using " + clamped);
		}
		return clamped;
	}

	private static double clamp(String name, double value, double min, double max, List<String> warnings) {
		// NaN fails every comparison, so it is replaced by the minimum instead of slipping through.
		double clamped = Double.isNaN(value) ? min : Math.max(min, Math.min(max, value));
		if (Double.compare(clamped, value) != 0) {
			warnings.add(name + " = " + value + " is outside " + min + ".." + max + ", using " + clamped);
		}
		return clamped;
	}
}
