package advRocketry.datagen;

import advRocketry.Main;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModTagProvider<T> extends TagsProvider<T> {
  private final Consumer<ModTagProvider<T>> content;

  public ModTagProvider(
      PackOutput output,
      ResourceKey<? extends Registry<T>> registry,
      CompletableFuture<HolderLookup.Provider> lookup,
      ExistingFileHelper files,
      Consumer<ModTagProvider<T>> content) {
    super(output, registry, lookup, Main.MODID, files);
    this.content = content;
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {
    content.accept(this);
  }

  public TagKey<T> key(String id) {
    return TagKey.create(registryKey, ResourceLocation.parse(id));
  }

  public void values(String tag, String... ids) {
    values(key(tag), ids);
  }

  public void values(TagKey<T> tag, String... ids) {
    TagAppender<T> appender = tag(tag);
    for (String id : ids)
      if (id.startsWith("#")) appender.addTag(key(id.substring(1)));
      else appender.add(ResourceKey.create(registryKey, ResourceLocation.parse(id)));
  }

  public void value(TagKey<T> tag, ResourceLocation id) {
    tag(tag).add(ResourceKey.create(registryKey, id));
  }
}
