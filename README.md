# KO Streak Shiny

Increase your chances of getting a shiny Pokemon by KOing wild versions of it in a row!

## Help

[Discord](https://discord.com/invite/WKAR27SdSv)

## Features

### Defeat Streak

Whenever a player defeats a wild Pokémon, if it’s of the same species as the species the player is currently on a KO streak for, the KO streak is incremented by 1. If a player defeats a wild Pokémon of a different species than their current streak, it resets to 1.

### Bonus Shiny Chance

As a player’s wild Pokémon KO streak increases, any Pokémon of that streak’s species that spawns nearby will gain a higher chance of becoming shiny. The chances are organized by thresholds; when a player’s streak becomes long enough, they unlock additional chances. When there are multiple players nearby who have unlocked a threshold for the spawning Pokémon’s species, the player with the highest unlocked threshold will add their chances.

### Config

In the config, you can change the range at which a Pokémon considers a player’s streak when spawning. By default it’s 64 blocks. You can also configure the thresholds, which by default are at a streak of 101+ you get 2 chances, 301+ you get 3, and 501+ you get 4. If there isn’t a player nearby who has achieved a threshold, there’s just 1 chance. The other side of the chance is determined by the `shinyRate` in the Cobblemon config, which is 8196 by default.

## Dependencies

Cobblemon [Modrinth](https://modrinth.com/mod/cobblemon) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cobblemon)

Fabric Language Kotlin [Modrinth](https://modrinth.com/mod/fabric-language-kotlin) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)

Cloth Config [Modrinth](https://modrinth.com/mod/cloth-config) / [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
