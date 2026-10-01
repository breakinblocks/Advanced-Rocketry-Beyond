package advRocketry.processing;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TagItems {
  private TagItems() {}

  public static ItemStack representative(TagKey<Item> tag) {
    return representative(BuiltInRegistries.ITEM.getTagOrEmpty(tag));
  }

  public static ItemStack representative(Iterable<Holder<Item>> items) {
    for (Holder<Item> item : items) return new ItemStack(item.value());
    return ItemStack.EMPTY;
  }
}
