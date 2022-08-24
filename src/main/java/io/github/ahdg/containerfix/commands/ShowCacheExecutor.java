package io.github.ahdg.containerfix.commands;

import com.google.inject.Inject;
import io.github.ahdg.containerfix.ContainerFix;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

public class ShowCacheExecutor implements CommandExecutor {

    private ContainerFix plugin;
    public ShowCacheExecutor(ContainerFix instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        if (plugin.getActiveGUI().isEmpty()) {
            src.sendMessage(Text.of("[ContainerFix]: 目前没有任何容器的打开行为被缓存!"));
            return CommandResult.success();
        }
        Map<BlockSnapshot, UUID> snapshot = plugin.getActiveGUI();
        for (BlockSnapshot block : snapshot.keySet()) {
            final Location<World> location = block.getLocation().get();
            final String blockName = block.getState().getType().getName();
            final String playerName = Sponge.getServer().getPlayer(snapshot.get(block)).get().getName();
            Text text = Text.builder(blockName + "(" + playerName + ")")
                    .onHover(TextActions.showText(Text.of("X:" + location.getBlockX()
                    + "Y:" + location.getBlockY()
                    + "Z:" + location.getBlockZ())))
                    .build();
            src.sendMessage(text);
        }
        return CommandResult.success();
    }
}
