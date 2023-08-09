package us.timinc.mc.cobblemon.kostreakshiny

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import us.timinc.mc.cobblemon.counter.Counter
import us.timinc.mc.cobblemon.kostreakshiny.config.KoStreakShinyConfig
import java.util.*
import kotlin.random.Random.Default.nextInt

object KoStreakShiny : ModInitializer {
    const val MOD_ID = "ko_streak_shiny"
    private lateinit var koStreakShinyConfig: KoStreakShinyConfig

    override fun onInitialize() {
        AutoConfig.register(
            KoStreakShinyConfig::class.java
        ) { definition: Config?, configClass: Class<KoStreakShinyConfig?>? ->
            JanksonConfigSerializer(
                definition,
                configClass
            )
        }
        koStreakShinyConfig = AutoConfig.getConfigHolder(KoStreakShinyConfig::class.java)
            .config
    }

    fun modifyShinyRate(
        props: PokemonProperties,
        ctx: SpawningContext
    ) {
        if (props.shiny != null || props.species == null) {
            return
        }
        val world = ctx.world
        val possibleMaxPlayer = world.getNearbyPlayers(
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