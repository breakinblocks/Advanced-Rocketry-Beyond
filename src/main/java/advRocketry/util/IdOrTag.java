package advRocketry.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record IdOrTag(String id, int count) {
  private static final Codec<String> ID =
      Codec.STRING.validate(
          value ->
              ResourceLocation.tryParse(value.startsWith("#") ? value.substring(1) : value) == null
                  ? DataResult.error(() -> "Invalid id or tag: " + value)
                  : DataResult.success(value));
  private static final Codec<IdOrTag> FULL =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      ID.fieldOf("id").forGetter(IdOrTag::id),
                      ExtraCodecs.POSITIVE_INT
                          .optionalFieldOf("count", 1)
                          .forGetter(IdOrTag::count))
                  .apply(instance, IdOrTag::new));
  public static final Codec<IdOrTag> CODEC =
      Codec.withAlternative(FULL, ID.xmap(id -> new IdOrTag(id, 1), IdOrTag::id));

  public static IdOrTag of(String id) {
    return new IdOrTag(id, 1);
  }

  public boolean tag() {
    return id.startsWith("#");
  }

  public ResourceLocation location() {
    return ResourceLocation.parse(tag() ? id.substring(1) : id);
  }

  public List<Item> items() {
    if (tag())
      return StreamSupport.stream(
              BuiltInRegistries.ITEM
                  .getTagOrEmpty(TagKey.create(Registries.ITEM, location()))
                  .spliterator(),
              false)
          .map(Holder::value)
          .toList();
    Item item = BuiltInRegistries.ITEM.get(location());
    return item == Items.AIR ? List.of() : List.of(item);
  }

  public List<Block> blocks() {
    if (tag())
      return StreamSupport.stream(
              BuiltInRegistries.BLOCK
                  .getTagOrEmpty(TagKey.create(Registries.BLOCK, location()))
                  .spliterator(),
              false)
          .map(Holder::value)
          .toList();
    Block block = BuiltInRegistries.BLOCK.get(location());
    return block == Blocks.AIR ? List.of() : List.of(block);
  }

  public ItemStack stack() {
    List<Item> items = items();
    return items.isEmpty() ? ItemStack.EMPTY : new ItemStack(items.getFirst(), count);
  }

  public boolean matches(ItemStack stack) {
    if (stack.isEmpty()) return false;
    return tag()
        ? stack.is(TagKey.create(Registries.ITEM, location()))
        : BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(location());
  }

  public static ListTag save(List<IdOrTag> entries) {
    ListTag list = new ListTag();
    for (IdOrTag entry : entries) list.add(CODEC.encodeStart(NbtOps.INSTANCE, entry).getOrThrow());
    return list;
  }

  public static List<IdOrTag> load(ListTag list) {
    return list.stream()
        .map(tag -> CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null))
        .filter(entry -> entry != null)
        .toList();
  }
}
