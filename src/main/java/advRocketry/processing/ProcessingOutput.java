package advRocketry.processing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ProcessingOutput(
    Optional<Holder<Item>> item,
    Optional<TagKey<Item>> tag,
    int count,
    DataComponentPatch components,
    float chance) {
  public static final Codec<ProcessingOutput> CODEC =
      RecordCodecBuilder.<ProcessingOutput>create(
              instance ->
                  instance
                      .group(
                          ItemStack.ITEM_NON_AIR_CODEC
                              .optionalFieldOf("id")
                              .forGetter(ProcessingOutput::item),
                          TagKey.codec(Registries.ITEM)
                              .optionalFieldOf("tag")
                              .forGetter(ProcessingOutput::tag),
                          Codec.intRange(1, Integer.MAX_VALUE)
                              .optionalFieldOf("count", 1)
                              .forGetter(ProcessingOutput::count),
                          DataComponentPatch.CODEC
                              .optionalFieldOf("components", DataComponentPatch.EMPTY)
                              .forGetter(ProcessingOutput::components),
                          Codec.floatRange(0, 1)
                              .optionalFieldOf("chance", 1f)
                              .forGetter(ProcessingOutput::chance))
                      .apply(instance, ProcessingOutput::new))
          .validate(
              output ->
                  output.item.isPresent() != output.tag.isPresent()
                      ? DataResult.success(output)
                      : DataResult.error(() -> "Output needs exactly one of id or tag"));

  public static ProcessingOutput of(ItemLike item, int count) {
    return new ProcessingOutput(
        Optional.of(item.asItem().builtInRegistryHolder()),
        Optional.empty(),
        count,
        DataComponentPatch.EMPTY,
        1);
  }

  public static ProcessingOutput of(TagKey<Item> tag, int count) {
    return new ProcessingOutput(
        Optional.empty(), Optional.of(tag), count, DataComponentPatch.EMPTY, 1);
  }

  public ProcessingOutput withChance(float chance) {
    return new ProcessingOutput(item, tag, count, components, chance);
  }

  public ItemStack stack(int amount) {
    if (amount <= 0) return ItemStack.EMPTY;
    if (item.isPresent()) return new ItemStack(item.get(), amount, components);
    ItemStack first = TagItems.representative(tag.orElseThrow());
    return first.isEmpty() ? first : first.copyWithCount(amount);
  }

  public ItemStack stack() {
    return stack(count);
  }

  public boolean resolvable() {
    return !stack().isEmpty();
  }

  public List<ItemStack> displayStacks() {
    if (item.isPresent()) return List.of(stack());
    List<ItemStack> stacks = new ArrayList<>();
    for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag.orElseThrow()))
      stacks.add(new ItemStack(holder, count));
    return stacks;
  }

  public boolean matches(ItemStack stack) {
    return item.map(stack::is).orElseGet(() -> stack.is(tag.orElseThrow()));
  }
}
