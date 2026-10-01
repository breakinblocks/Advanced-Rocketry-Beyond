package advRocketry.util;

import net.minecraft.network.chat.Component;

public final class AssemblyFailure extends IllegalArgumentException {
  private final transient Component component;

  public AssemblyFailure(Component component) {
    super(component.getString());
    this.component = component;
  }

  public Component component() {
    return component;
  }

  public static Component describe(IllegalArgumentException exception) {
    return exception instanceof AssemblyFailure failure
        ? failure.component()
        : Component.literal(String.valueOf(exception.getMessage()));
  }
}
