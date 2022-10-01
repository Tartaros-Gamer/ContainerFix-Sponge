package io.github.ahdg.containerfix.mods;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

public class ThermalExpansion {

    // https://forums.spongepowered.org/t/clickinventoryevent-get-clicked-item/37607
    @Listener(order = Order.BEFORE_POST)
    public void onSatchelClick(ClickInventoryEvent.NumberPress event, @Root Player player) {
        if (!player.isViewingInventory()) return;
        player.getItemInHand(HandTypes.OFF_HAND)
                        .ifPresent(itemStack -> {
                            if (itemStack.getType().getName().equals("thermalexpansion:satchel"))
                                event.setCancelled(true);
                        });
        for (SlotTransaction slot : event.getTransactions()) {
            if (slot.getOriginal().getType().getName().equals("thermalexpansion:satchel")) {
                event.setCancelled(true);
            }
        }
    }
}
