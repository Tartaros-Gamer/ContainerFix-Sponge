package io.github.ahdg.containerfix;

import com.google.inject.Inject;
import io.github.ahdg.containerfix.commands.ReloadExecutor;
import io.github.ahdg.containerfix.commands.ShowCacheExecutor;
import io.github.ahdg.containerfix.conf.ConfManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

@Plugin(
        id = "containerfix",
        name = "ContainerFix",
        description = "A plugin for solving GUI dupe bug among mods.",
        authors = {
                "ahdg"
        }
)
public class ContainerFix {


    @Inject private Logger logger;
    @Inject private ConfManager config;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        initCommands();
        config.get();
        logger.info("已加载 ContainerFix，配置文件可在 /config/containerfix.conf 找到");
    }

    private void initCommands() {
        CommandSpec reload = CommandSpec.builder()
                .permission("containerfix.reload")
                .description(Text.of("Reload Config."))
                .executor(new ReloadExecutor())
                .build();

        CommandSpec listGUI = CommandSpec.builder()
                .permission("containerfix.show")
                .description(Text.of("Show cached GUI."))
                .executor(new ShowCacheExecutor())
                .build();

        CommandSpec core = CommandSpec.builder()
                .description(Text.of("The basic command of ContainerFix."))
                .child(reload, "reload", "load")
                .child(listGUI, "show", "s", "list")
                .build();

        Sponge.getCommandManager().register(this, core, "containerfix", "ctf");
    }

    private final Map<BlockSnapshot, UUID> activeGUI = new HashMap<>();

    public Map<BlockSnapshot, UUID> getActiveGUI() {
        return activeGUI;
    }

    @Listener(order = Order.DEFAULT)
    public void onInvOpen(InteractInventoryEvent.Open event, @Root Player player) {
        if (player.hasPermission("containerfix.ignore")) return;
        event.getContext().get(EventContextKeys.BLOCK_HIT).ifPresent(blockSnapshot -> {
            if (!blockSnapshot.getLocation().isPresent()) return;
            String blockName = blockSnapshot.getState().getType().getName();
            if (Arrays.asList(config.get().AntiGUIKeepContainerList).contains(blockName)
                    || Arrays.asList(config.get().AntiMultiOpenContainerList).contains(blockName)) {
                activeGUI.put(blockSnapshot, player.getUniqueId());
            }
        });
    }
    @Listener(order = Order.LAST)
    public void onClickTarget(InteractBlockEvent.Secondary event, @First Player player) {
        if (player.hasPermission("containerfix.ignore") || activeGUI.isEmpty()) return;
        BlockSnapshot block = event.getTargetBlock();
        final String blockName = block.getState().getName();
        final Location<World> location = block.getLocation().get();
        final BlockType blockType = block.getState().getType();
        if (!Arrays.asList(config.get().AntiMultiOpenContainerList).contains(blockName)) return;
        for (BlockSnapshot blockSnapshot : activeGUI.keySet()) {
            if (blockSnapshot.getLocation().get().equals(location) && blockSnapshot.getState().getType().equals(blockType)) {
                event.setCancelled(true);
                player.sendMessage(Text.builder(config.get().MessagesMultiOpen).color(TextColors.AQUA).build());
            }
        }
    }

    @Listener(order = Order.BEFORE_POST)
    public void onInvClose(InteractInventoryEvent.Close event, @Root Player player) {
        if (activeGUI.isEmpty()) return;
        activeGUI.values().removeIf(playerUID -> playerUID == player.getUniqueId());
    }

    @Listener(order = Order.LAST)
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) {
        if (player.hasPermission("containerfix.ignore") || activeGUI.isEmpty()) return;
        for (Transaction<BlockSnapshot> blockSnap : event.getTransactions()) {
            if (!blockSnap.getFinal().getLocation().isPresent()) continue;
            final Location<World> location = blockSnap.getOriginal().getLocation().get();
            final BlockType block = blockSnap.getOriginal().getState().getType();
            Iterator<BlockSnapshot> iter = activeGUI.keySet().iterator();
            while (iter.hasNext()) {
                BlockSnapshot activeBlock = iter.next();
                if (activeBlock.getLocation().get().equals(location) && activeBlock.getState().getType().equals(block)) {
                    if (!config.get().PreventBreak) {
                        Sponge.getServer().getPlayer(activeGUI.get(activeBlock)).ifPresent(Player::closeInventory);
                        iter.remove();
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Text.builder(config.get().MessagesAntiGUIKeep).color(TextColors.AQUA).build());
                    }
                }
            }
        }
    }
}
