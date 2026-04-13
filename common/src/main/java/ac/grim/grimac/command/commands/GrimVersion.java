package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.manager.cloud.CloudCommandAdapter;
import ac.grim.grimac.platform.api.sender.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public class GrimVersion implements BuildableCommand {

    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("version")
                        .permission("grim.version")
                        .handler(this::handleVersion)
        );
    }

    private void handleVersion(@NotNull CommandContext<Sender> context) {
        String current = GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
        context.sender().sendMessage(Component.text()
                .append(Component.text("LightningGrim Version: ").color(NamedTextColor.GRAY))
                .append(Component.text(current).color(NamedTextColor.AQUA))
                .build());
    }
}
