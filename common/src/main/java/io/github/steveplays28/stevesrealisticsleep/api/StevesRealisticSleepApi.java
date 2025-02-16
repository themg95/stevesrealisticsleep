package io.github.steveplays28.stevesrealisticsleep.api;

import io.github.steveplays28.stevesrealisticsleep.extension.ServerWorldExtension;
import io.github.steveplays28.stevesrealisticsleep.mixin.accessor.ServerWorldAccessor;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static io.github.steveplays28.stevesrealisticsleep.util.SleepMathUtil.DAY_LENGTH;
import static io.github.steveplays28.stevesrealisticsleep.util.SleepMathUtil.calculateTicksUntilAwake;

/**
 * Realistic Sleep's API. This class contains methods and ways to grab information from Realistic Sleep that may be useful to other mods.
 */
public class StevesRealisticSleepApi {
	/**
	 * @param world The world (aka dimension)
	 * @return The sleep progress of the world as a percentage.
	 * @since v1.8.4
	 */
	@SuppressWarnings("unused")
	public static float getSleepProgress(@NotNull World world) {
		return ((float) (DAY_LENGTH - calculateTicksUntilAwake(getTimeOfDay(world))) / DAY_LENGTH) * 100f;
	}

	/**
	 * @param world The world (aka dimension)
	 * @return The time of day of the world in ticks, with the day count filtered out using a modulo operator. This means that when Minecraft returns the time of day of a world, the day count doesn't have any effect on the resulting time. Equal to <code>(int) world.getLevelProperties().getTimeOfDay() % DAY_LENGTH</code>.
	 * @since v1.8.4
	 */
	public static int getTimeOfDay(@NotNull World world) {
		return (int) world.getLevelProperties().getTimeOfDay() % DAY_LENGTH;
	}

	/**
	 * @param world The world (aka dimension)
	 * @return Whether players are sleeping in this world (time is passing at an accelerated rate). The world will be casted to either a <code>ServerWorld</code> or <code>ClientWorld</code> depending on the side this method is called from. Prefer the sides' respective API methods instead.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isSleeping(@NotNull World world) {
		if (world.isClient()) {
			return isSleeping((ClientWorld) world);
		} else {
			return isSleeping((ServerWorld) world);
		}
	}

	/**
	 * @param world The clientside world (aka dimension)
	 * @return Whether players are sleeping in this world (time is passing at an accelerated rate).
	 */
	public static boolean isSleeping(@NotNull ClientWorld world) {
		var players = world.getPlayers();
		var sleepingPlayers = players.stream().filter(LivingEntity::isSleeping);
		var playerCount = players.size();
		var sleepingPlayerCount = sleepingPlayers.count();
		double sleepingRatio = (double) sleepingPlayerCount / playerCount;
		double sleepingPercentage = sleepingRatio * 100;
		int playersRequiredToSleepPercentage = world.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);

		return sleepingPercentage >= playersRequiredToSleepPercentage;
	}

	/**
	 * @param world The serverside world (aka dimension)
	 * @return Whether players are sleeping in this world (time is passing at an accelerated rate).
	 */
	public static boolean isSleeping(@NotNull ServerWorld world) {
		var playerCount = world.getPlayers().size();
		var sleepingPlayerCount = ((ServerWorldAccessor) world).getSleepManager().getSleeping();
		double sleepingRatio = (double) sleepingPlayerCount / playerCount;
		double sleepingPercentage = sleepingRatio * 100d;
		int playersRequiredToSleepPercentage = world.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);

		return sleepingPercentage >= playersRequiredToSleepPercentage;
	}

	/**
	 * @param world The serverside world (aka dimension)
	 * @return The consecutive amount of sleep ticks. Returns <code>0</code> when no players are sleeping.
	 */
	@SuppressWarnings("unused")
	public static int getConsecutiveSleepTicks(ServerWorld world) {
		return ((ServerWorldExtension) world).consecutiveSleepTicks;
	}
}
