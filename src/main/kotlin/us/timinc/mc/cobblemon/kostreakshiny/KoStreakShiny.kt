package us.timinc.mc.cobblemon.kostreakshiny

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
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
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import us.timinc.mc.cobblemon.kostreakshiny.store.WildDefeatsData
import java.util.UUID

object KoStreakShiny : ModInitializer {

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register("wildDefeats", WildDefeatsData::class.java)

        CobblemonEvents.BATTLE_VICTORY.subscribe { battleVictoryEvent ->
            if (!battleVictoryEvent.battle.isPvW) return@subscribe

            val wildPokemon = battleVictoryEvent.battle.actors.flatMap { it.pokemonList }.map { it.originalPokemon }
                .filter { !it.isPlayerOwned() }

            battleVictoryEvent.winners
                .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
                .forEach {
                    val data = Cobblemon.playerData.get(it)
                    val wildDefeats: WildDefeatsData =
                        data.extraData.getOrPut("wildDefeats") { WildDefeatsData() } as WildDefeatsData
                    wildPokemon.forEach { wildDefeats.addDefeat(it.species.resourceIdentifier.toString()) }
                    Cobblemon.playerData.saveSingle(data)
                }
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            literal<CommandSourceStack>("checkkos")
                .then(argument<CommandSourceStack?, String?>("name", StringArgumentType.greedyString())
                    .executes { checkScore(it) }
                )
                .register(dispatcher)
        }

    }

    private fun checkScore(context: CommandContext<CommandSourceStack>): Int {
        println("Hi")
        val nameArg = StringArgumentType.getString(context, "name")
        println(nameArg)
        val player = context.source.playerOrException
        println(player.displayName)

        val data = Cobblemon.playerData.get(player)
        val wildDefeats: WildDefeatsData =
            data.extraData.getOrPut("wildDefeats") { WildDefeatsData() } as WildDefeatsData
        val currentDefeats = wildDefeats.getDefeats(nameArg)

        context.source.sendSuccess(Component.literal(currentDefeats.toString()), true)
        return Command.SINGLE_SUCCESS
    }

}

// We can write extension functions to reduce nesting in our command logic if we wanted to
fun LiteralArgumentBuilder<CommandSourceStack>.register(dispatcher: CommandDispatcher<CommandSourceStack>) {
    dispatcher.register(this)
}