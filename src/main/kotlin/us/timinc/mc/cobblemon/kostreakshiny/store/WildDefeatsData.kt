package us.timinc.mc.cobblemon.kostreakshiny.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class WildDefeatsData : PlayerDataExtension {
    var defeatList = mutableMapOf<String, Int>()

    fun addDefeat(name: String) {
        val prev = defeatList.getOrDefault(name, 0)
        println(prev)
        println(name)
        defeatList[name] = prev + 1
    }

    fun getDefeats(name: String): Int {
        return defeatList.getOrDefault(name, 0)
    }

    override fun deserialize(json: JsonObject): WildDefeatsData {
        val defeats = json.getAsJsonObject("defeats")
        for (key in defeats.keySet()) {
            defeatList[key] = defeats.get(key).asInt
        }
        return this
    }

    override fun name(): String {
        return "wildDefeats"
    }

    override fun serialize(): JsonObject {
        val json = JsonObject() // Create a new JSON object

        val defeatsArray = JsonArray() // Create a new JSON array to store the defeats


        json.add("defeats", defeatsArray) // Add the defeats array to the main JSON object

        return json // Return the serialized JSON object
    }
}
