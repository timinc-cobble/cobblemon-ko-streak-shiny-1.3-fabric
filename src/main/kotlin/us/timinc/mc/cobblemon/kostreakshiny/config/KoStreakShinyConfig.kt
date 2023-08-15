package us.timinc.mc.cobblemon.kostreakshiny.config

import draylar.omegaconfig.api.Comment
import draylar.omegaconfig.api.Config
import us.timinc.mc.cobblemon.kostreakshiny.KoStreakShiny

class KoStreakShinyConfig : Config {
    @Comment("The distance at which a spawning Pokemon takes into consideration this player's KO streak")
    val effectiveRange = 64

    @Comment("Thresholds for the KO counts : shiny chance bonus")
    val thresholds: Map<Int, Int> = mutableMapOf(Pair(100, 1), Pair(300, 2), Pair(500, 3))

    fun getThreshold(koCount: Int): Int {
        return thresholds.maxOfOrNull { entry -> if (entry.key < koCount) entry.value else 0 } ?: 0
    }

    override fun getName(): String {
        return KoStreakShiny.MOD_ID
    }
}