package advRocketry.command;

import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RocketryCommands {
  private RocketryCommands() {}

  public static void register(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("advancedrocketry")
                .then(
                    Commands.literal("showcase")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> Showcase.build(context.getSource()))));
  }
}
