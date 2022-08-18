package io.github.ahdg.containerfix;

import com.google.inject.Inject;
import io.github.ahdg.containerfix.conf.ConfManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("重载 ContainerFix 的配置文件"))
                .permission("containerfix.reload")
                .executor(new Reload())
                .build();
        Sponge.getCommandManager().register(this, reload, "containerfix", "ctf");
        logger.info("已加载 ContainerFix，配置文件可在 /config/containerfix.conf 找到");
    }

    public Map<BlockSnapshot, Player> activeGUI = new HashMap<>();

    public class Reload implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) {
            config.reload();
            src.sendMessage(Text.of("已重载 ContainerFix 配置文件!"));
            return CommandResult.success();
        }
    }

    @Listener(order = Order.LAST)
    public void onOpenGUI(InteractBlockEvent.Secondary event, @First Player player) {
        if (player.hasPermission("containerfix.ignore")) return;
        BlockSnapshot blockSnapshot = event.getTargetBlock();
        String blockName = blockSnapshot.getState().getType().getName();
        if (!Arrays.asList(config.get().AntiGUIKeepContainerList).contains(blockName)
                && !Arrays.asList(config.get().AntiMultiOpenContainerList).contains(blockName)) return;
        if (Arrays.asList(config.get().AntiMultiOpenContainerList).contains(blockName) && activeGUI.containsKey(blockSnapshot)) {
            event.setCancelled(true);
            player.sendMessage(Text.builder(config.get().MessagesMultiOpen).color(TextColors.AQUA).build());
        }
        activeGUI.put(blockSnapshot, player);
    }

    @Listener(order = Order.LAST)
    public void onCloseGUI(InteractInventoryEvent.Close event, @First Player player) {
        if (activeGUI.isEmpty()) return;
        for (BlockSnapshot block : activeGUI.keySet()) {
            if (activeGUI.get(block) == player) {
                activeGUI.remove(block);
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) {
        if (player.hasPermission("containerfix.ignore") || activeGUI.isEmpty()) return;
        for (Transaction<BlockSnapshot> blockSnap : event.getTransactions()) {
            final Optional<Location<World>> location = blockSnap.getFinal().getLocation();
            final BlockType block = blockSnap.getFinal().getState().getType();
            for (BlockSnapshot activeBlockSnap : activeGUI.keySet()) {
                if (activeBlockSnap.getLocation() == location && activeBlockSnap.getState().getType() == block) {
                    if (!config.get().PreventBreak) {
                        activeGUI.get(activeBlockSnap).closeInventory();
                        activeGUI.remove(activeBlockSnap);
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Text.builder(config.get().MessagesAntiGUIKeep).color(TextColors.AQUA).build());
                    }
                }
            }
        }
    }
}
