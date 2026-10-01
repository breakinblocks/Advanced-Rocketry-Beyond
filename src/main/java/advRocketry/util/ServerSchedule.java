package advRocketry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ServerSchedule {
  private record Task(int interval, Consumer<MinecraftServer> action) {}

  private static final List<Task> TASKS = new ArrayList<>();

  private ServerSchedule() {}

  public static void every(int interval, Consumer<MinecraftServer> action) {
    if (interval < 1) throw new IllegalArgumentException("Interval must be at least one tick");
    TASKS.add(new Task(interval, action));
  }

  public static void tick(ServerTickEvent.Post event) {
    MinecraftServer server = event.getServer();
    int now = server.getTickCount();
    for (Task task : TASKS) if (now % task.interval() == 0) task.action().accept(server);
  }
}
