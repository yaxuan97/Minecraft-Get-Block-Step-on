package cn.yaxuan97.begierde.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Block.class)
public class BegierdeMixin {
    private static Map<Entity, BlockPos> map = new HashMap<Entity, BlockPos>();
    @Inject(at = @At("RETURN"), method = "onSteppedOn")
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (!world.isClient() && entity.isPlayer()) {
            if (map.containsKey(entity)){
                BlockPos lastpos = map.get(entity);
                if (pos.equals(lastpos)){
                    return;
                } else {
                    map.put(entity, pos);
                }
            } else {
                map.put(entity, pos);
            }
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            ItemStack itemStack = new ItemStack(state.getBlock());
            PlayerInventory playerInventory = serverPlayerEntity.getInventory();
            if ((playerInventory.getOccupiedSlotWithRoomForStack(itemStack) == -1) && playerInventory.getEmptySlot() == -1) return;
            ItemEntity itemEntity;
            boolean bl = playerInventory.insertStack(itemStack);
            if (bl && itemStack.isEmpty()) {
                itemStack.setCount(1);
                itemEntity = serverPlayerEntity.dropItem(itemStack, false);
                if (itemEntity != null) {
                    itemEntity.setDespawnImmediately();
                }
                serverPlayerEntity.world.playSound((
                        PlayerEntity) null,
                        serverPlayerEntity.getX(),
                        serverPlayerEntity.getY(),
                        serverPlayerEntity.getZ(),
                        SoundEvents.ENTITY_ITEM_PICKUP,
                        SoundCategory.PLAYERS,
                        0.2F,
                        ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                serverPlayerEntity.currentScreenHandler.sendContentUpdates();
            } else {
                itemEntity = serverPlayerEntity.dropItem(itemStack, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(serverPlayerEntity.getUuid());
                }
            }
        }
    }
}
