package us.timinc.mc.cobblemon.kostreakshiny

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import us.timinc.mc.cobblemon.kostreakshiny.store.WildDefeatsData
import java.util.UUID
import kotlin.random.Random

object KoStreakShiny : ModInitializer {

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(WildDefeatsData.name, WildDefeatsData::class.java)

        CobblemonEvents.BATTLE_VICTORY.subscribe { battleVictoryEvent ->
            if (!battleVictoryEvent.battle.isPvW) return@subscribe

            handleWildDefeat(battleVictoryEvent)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            literal<CommandSourceStack>("checkkos")
                .then(
                    argument<CommandSourceStack?, String?>(
                        "name",
                        StringArgumentType.greedyString()
                    ).executes { checkScore(it) }).register(dispatcher)
            literal<CommandSourceStack>("resetkos")
                .executes { resetScore(it) }
                .register(dispatcher)
        }

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (entity !is PokemonEntity || entity.pokemon.isPlayerOwned()) {
                return@register
            }

            checkAndApplyBonusShinyChance(entity, world)
        }
    }

    private fun getPlayerKoStreak(player: Player): WildDefeatsData {
        val data = Cobblemon.playerData.get(player)
        return data.extraData.getOrPut(WildDefeatsData.name) { WildDefeatsData() } as WildDefeatsData
    }

    private fun checkAndApplyBonusShinyChance(entity: PokemonEntity, world: ServerLevel) {
        val maxKoStreak = world.getNearbyPlayers(
            TargetingConditions.forNonCombat()
                .ignoreLineOfSight()
                .ignoreInvisibilityTesting(),
            entity,
            AABB.ofSize(entity.eyePosition, 32.0, 32.0, 32.0)
        ).maxOfOrNull {
            getPlayerKoStreak(it).getDefeats(entity.pokemon.species.resourceIdentifier.toString())
        } ?: 0

        val shinyChances = when {
            maxKoStreak > 500 -> 4
            maxKoStreak > 300 -> 3
            maxKoStreak > 100 -> 2
            else -> 1
        }
        val shinyRate = Cobblemon.config.shinyRate.toInt()
        val shinyRoll = Random.nextInt(0, shinyRate)
        entity.pokemon.shiny = shinyRoll < shinyChances
    }

    private fun handleWildDefeat(battleVictoryEvent: BattleVictoryEvent) {
        val wildPokemons = battleVictoryEvent.battle.actors.flatMap { it.pokemonList }.map { it.originalPokemon }
            .filter { !it.isPlayerOwned() }

        battleVictoryEvent.winners
            .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
            .forEach { player ->
                val data = Cobblemon.playerData.get(player)
                val wildDefeats: WildDefeatsData =
                    data.extraData.getOrPut(WildDefeatsData.name) { WildDefeatsData() } as WildDefeatsData
                wildPokemons.forEach { wildPokemon ->
                    val resourceIdentifier = wildPokemon.species.resourceIdentifier.toString()
                    wildDefeats.addDefeat(resourceIdentifier)
                }
                Cobblemon.playerData.saveSingle(data)
            }
    }

    private fun checkScore(context: CommandContext<CommandSourceStack>): Int {
        val queriedPokemonResourceIdentifier = StringArgumentType.getString(context, "name")
        val player = context.source.playerOrException
        val data = Cobblemon.playerData.get(player)

        val wildDefeats = data.extraData.getOrPut(WildDefeatsData.name) { WildDefeatsData() } as WildDefeatsData
        val currentCount = wildDefeats.getDefeats(queriedPokemonResourceIdentifier.toString())

        if (currentCount == 0) {
            context.source.sendSuccess(Component.translatable("kostreakshiny.nostreak"), true)
        } else {
            val currentPokemonSpecies =
                PokemonSpecies.getByIdentifier(ResourceLocation(queriedPokemonResourceIdentifier.toString()))

            if (currentPokemonSpecies == null) {
                context.source.sendSuccess(
                    Component.translatable("kostreakshiny.error.invalidPokemonIdentifier"),
                    true
                )
            } else {
                context.source.sendSuccess(
                    Component.translatable(
                        "kostreakshiny.streak",
                        Component.literal(currentCount.toString()),
                        Component.literal(currentPokemonSpecies.name)
                    ), true
                )
            }
        }

        return Command.SINGLE_SUCCESS
    }

    private fun resetScore(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val data = Cobblemon.playerData.get(player)

        val wildDefeats = data.extraData.getOrPut(WildDefeatsData.name) { WildDefeatsData() } as WildDefeatsData
        wildDefeats.resetDefeats()
        Cobblemon.playerData.saveSingle(data)

        context.source.sendSuccess(Component.translatable("kostreakshiny.successfulReset"), true)

        return Command.SINGLE_SUCCESS
    }
}

// We can write extension functions to reduce nesting in our command logic if we wanted to
fun LiteralArgumentBuilder<CommandSourceStack>.register(dispatcher: CommandDispatcher<CommandSourceStack>) {
    dispatcher.register(this)
}