package io.github.razekteixeira.livingreefs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import io.github.razekteixeira.livingreefs.LivingCoral;
import io.github.razekteixeira.livingreefs.ReefTicker;

/**
 * Gives living coral random ticks. The coral classes inherit both methods from here without
 * overriding them, so injecting into the base keeps vanilla's own coral code (death when dry)
 * untouched and avoids adding methods to the coral classes.
 *
 * <p>The random tick flag is cached per block state when blocks are bootstrapped, before tags load,
 * so the choice is made by class, not by tag.
 */
@Mixin(BlockBehaviour.class)
abstract class BlockBehaviourMixin {
	@Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
	private void livingreefs$livingCoralTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (LivingCoral.is(this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "randomTick", at = @At("HEAD"))
	private void livingreefs$livingCoralRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
			CallbackInfo ci) {
		if (LivingCoral.is(this)) {
			ReefTicker.onRandomTick(state, level, pos, random);
		}
	}
}
