package io.github.razekteixeira.livingreefs;

import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.CoralPlantBlock;
import net.minecraft.world.level.block.CoralWallFanBlock;

/** Which blocks get random ticks: the four living vanilla coral classes and anything extending them. */
public final class LivingCoral {
	private LivingCoral() {
	}

	public static boolean is(Object block) {
		return block instanceof CoralBlock || block instanceof CoralPlantBlock
				|| block instanceof CoralFanBlock || block instanceof CoralWallFanBlock;
	}
}
