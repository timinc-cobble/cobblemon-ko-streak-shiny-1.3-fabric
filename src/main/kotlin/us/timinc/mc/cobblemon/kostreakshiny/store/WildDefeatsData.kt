package us.timinc.mc.cobblemon.kostreakshiny.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class WildDefeatsData : PlayerDataExtension {
    companion object {
        const val name = "wildDefeats"
    }

    var pokemonResourceIdentifier = ""
    var count = 0

    fun resetDefeats() {
        pokemonResourceIdentifier = ""
        count = 0
    }

    fun addDefeat(newPokemonResourceIdentifier: String) {
        if (newPokemonResourceIdentifier == pokemonResourceIdentifier) {
            count++
        } else {
            pokemonResourceIdentifier = newPokemonResourceIdentifier
            count = 1
        }
    }

    fun getDefeats(defeatedPokemonResourceIdentifier: String): Int {
        if (defeatedPokemonResourceIdentifier == pokemonResourceIdentifier) {
            return count
        }
        return 0
    }

    override fun deserialize(json: JsonObject): WildDefeatsData {
        pokemonResourceIdentifier = json.get("pokemonResourceIdentifier").asString
        count = json.get("count").asInt

        return this
    }

    override fun name(): String {
        return name
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()

        json.addProperty("name", name)
        json.addProperty("pokemonResourceIdentifier", pokemonResourceIdentifier)
        json.addProperty("count", count)

        return json
    }
}
