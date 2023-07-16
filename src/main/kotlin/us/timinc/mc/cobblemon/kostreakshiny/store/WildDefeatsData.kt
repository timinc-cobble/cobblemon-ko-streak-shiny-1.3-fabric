package us.timinc.mc.cobblemon.kostreakshiny.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class WildDefeatsData : PlayerDataExtension {
    companion object {
        const val name = "wildDefeats"
    }

    val wildDefeatsData = mutableMapOf<String, Int>()

    fun resetDefeats() {
        wildDefeatsData.clear()
    }

    fun addDefeat(defeatedPokemonResourceIdentifier: String) {
        wildDefeatsData[defeatedPokemonResourceIdentifier] =
            getDefeats(defeatedPokemonResourceIdentifier) + 1
    }

    fun getDefeats(defeatedPokemonResourceIdentifier: String): Int {
        return wildDefeatsData.getOrDefault(defeatedPokemonResourceIdentifier, 0)
    }

    override fun deserialize(json: JsonObject): WildDefeatsData {
        val defeatsData = json.getAsJsonObject("defeats")
        for (pokemonResourceIdentifier in defeatsData.keySet()) {
            wildDefeatsData[pokemonResourceIdentifier] = defeatsData.get(pokemonResourceIdentifier).asInt
        }

        return this
    }

    override fun name(): String {
        return name
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", name)

        val defeatsData = JsonObject()
        for (pokemonResourceIdentifier in wildDefeatsData.keys) {
            defeatsData.addProperty(pokemonResourceIdentifier, wildDefeatsData[pokemonResourceIdentifier])
        }
        json.add("defeats", defeatsData)

        return json
    }
}
