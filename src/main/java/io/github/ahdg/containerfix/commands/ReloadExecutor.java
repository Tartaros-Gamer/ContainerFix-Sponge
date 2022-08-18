package io.github.ahdg.containerfix.commands;

import com.google.inject.Inject;
import io.github.ahdg.containerfix.conf.ConfManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

public class ReloadExecutor implements CommandExecutor {
    @Inject
    private ConfManager config;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        try {
            config.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        src.sendMessage(Text.of("已重载 ContainerFix 配置文件!"));
        return CommandResult.success();
    }
}
