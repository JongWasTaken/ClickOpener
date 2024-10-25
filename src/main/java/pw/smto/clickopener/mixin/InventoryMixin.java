package pw.smto.clickopener.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryMixin {

    @Inject(method = "canPlayerUse(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/player/PlayerEntity;F)Z", at = @At("HEAD"), cancellable = true)
    private static void canUse(BlockEntity blockEntity, PlayerEntity player, float range, CallbackInfoReturnable<Boolean> cir) {
        //cir.setReturnValue(true);
        //cir.cancel();
        //ClickOpenerMod.LOGGER.warn("canUse called!");
    }
}
