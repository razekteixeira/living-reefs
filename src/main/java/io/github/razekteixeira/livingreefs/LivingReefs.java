package io.github.razekteixeira.livingreefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class LivingReefs implements ModInitializer {
	public static final String MOD_ID = "livingreefs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info(LivingReefsConfig.load());
		ReefGameRules.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> LivingReefsCommands.register(dispatcher));
		ServerLifecycleEvents.SERVER_STARTED.register(server -> ReefSpecies.get());
		// Statistics are per server run, so they never leak into the next world in singleplayer.
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ReefTicker.STATS.reset());
		LOGGER.info("Living Reefs ready: /livingreefs (alias /reef)");
	}
}
