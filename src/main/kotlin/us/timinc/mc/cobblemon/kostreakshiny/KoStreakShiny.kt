package us.timinc.mc.cobblemon.kostreakshiny

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import draylar.omegaconfig.OmegaConfig
import net.fabricmc.api.ModInitializer
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import us.timinc.mc.cobblemon.counter.Counter
import us.timinc.mc.cobblemon.kostreakshiny.config.KoStreakShinyConfig
import kotlin.random.Random.Default.nextInt

object KoStreakShiny : ModInitializer {
    const val MOD_ID = "ko_streak_shiny"
    private var koStreakShinyConfig: KoStreakShinyConfig =
        OmegaConfig.register(KoStreakShinyConfig::class.java)

    override fun onInitialize() {}

    fun modifyShinyRate(
        props: PokemonProperties,
        ctx: SpawningContext
    ) {
        if (props.shiny != null || props.species == null) {
            return
        }
        val world = ctx.world
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val possibleMaxPlayer = world.getNearbyPlayers(
            TargetingConditions.forNonCombat()
                .ignoreLineOfSight()
                .ignoreInvisibilityTesting(),
            null,
            AABB.ofSize(
                Vec3.atCenterOf(ctx.position),
                koStreakShinyConfig.effectiveRange.toDouble(),
                koStreakShinyConfig.effectiveRange.toDouble(),
                koStreakShinyConfig.effectiveRange.toDouble()
            )
        ).stream().max(Comparator.comparingInt { player: Player? ->
            Counter.getPlayerKoStreak(
                player!!, props.species!!
            )
        })
        if (possibleMaxPlayer.isEmpty) {
            return
        }

        val maxPlayer = possibleMaxPlayer.get()
        val maxKoStreak = Counter.getPlayerKoStreak(maxPlayer, props.species!!)
        val shinyChances = koStreakShinyConfig.getThreshold(maxKoStreak) + 1
        val shinyRate: Int = config.shinyRate.toInt()
        val shinyRoll = nextInt(shinyRate)
        props.shiny = shinyRoll < shinyChances
    }
}