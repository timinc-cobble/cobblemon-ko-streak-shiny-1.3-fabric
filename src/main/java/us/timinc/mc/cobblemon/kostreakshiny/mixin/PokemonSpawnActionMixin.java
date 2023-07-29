package us.timinc.mc.cobblemon.kostreakshiny.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.spawning.context.SpawningContext;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.timinc.mc.cobblemon.kostreakshiny.KoStreakShiny;

import java.util.Comparator;
import java.util.Optional;
import kotlin.random.Random;

@Mixin(value = PokemonSpawnAction.class, remap = false)
public abstract class PokemonSpawnActionMixin {
    @Shadow
    private PokemonProperties props;

    @Inject(method = "createEntity", at = @At("HEAD"))
    private void modifyShinyRate(CallbackInfoReturnable<Entity> cir) {
        if (props.getShiny() != null) {
            return;
        }

        SpawningContext ctx = ((PokemonSpawnAction) (Object) this).getCtx();
        Level world = ctx.getWorld();
        Optional<Player> possibleMaxPlayer = world.getNearbyPlayers(TargetingConditions.forNonCombat()
                        .ignoreLineOfSight()
                        .ignoreInvisibilityTesting(),
                null,
                AABB.ofSize(Vec3.atCenterOf(ctx.getPosition()), 64.0, 64.0, 64.0)).stream().max(Comparator.comparingInt(player -> KoStreakShiny.INSTANCE.getPlayerKoStreak(player, props.getSpecies())));
        if (possibleMaxPlayer.isEmpty()) {
            return;
        }

        Player maxPlayer = possibleMaxPlayer.get();
        int maxKoStreak = KoStreakShiny.INSTANCE.getPlayerKoStreak(maxPlayer, props.getSpecies());
        int shinyChances;
        if (maxKoStreak > 500) {
            shinyChances = 4;
        } else if (maxKoStreak > 300) {
            shinyChances = 3;
        } else if (maxKoStreak > 100) {
            shinyChances = 2;
        } else {
            shinyChances = 1;
        }

        int shinyRate = (int) Cobblemon.config.getShinyRate();
        int shinyRoll = Random.Default.nextInt(shinyRate);
        props.setShiny(shinyRoll < shinyChances);
    }
}