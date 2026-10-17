package advRocketry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

public final class TestTags {
  private TestTags() {}

  public interface Binding extends AutoCloseable {
    @Override
    void close();
  }

  @SafeVarargs
  public static <T> Binding add(Registry<T> registry, TagKey<T> tag, Holder<T>... extra) {
    Map<TagKey<T>, List<Holder<T>>> original = new LinkedHashMap<>();
    registry
        .getTags()
        .forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
    Map<TagKey<T>, List<Holder<T>>> modified = new LinkedHashMap<>(original);
    List<Holder<T>> merged = new ArrayList<>(original.getOrDefault(tag, List.of()));
    merged.addAll(List.of(extra));
    modified.put(tag, merged);
    registry.bindTags(modified);
    return () -> registry.bindTags(original);
  }
}
