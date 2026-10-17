package advRocketry.transport;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

/** Saved side policy. Live handler contents are never cached here. */
public final class TransportSideConfig {
  public enum Routing {
    LEGACY,
    NEAREST,
    FURTHEST,
    RANDOM,
    ROUND_ROBIN
  }

  public int priority;
  public int extractionLimit = Integer.MAX_VALUE;
  public int insertRedstone;
  public int extractRedstone;
  public int retryCap;
  public Routing routing = Routing.LEGACY;
  public boolean configured, deny, exact = true, sensor, disposal;
  public final Entry[] entries = new Entry[9];
  public int cursor;
  int scanCursor;
  int scannedSinceProgress;
  boolean scanIncomplete;
  final TransportRetry retry = new TransportRetry();

  public static final class Entry {
    private final ItemStack icon;
    private final FluidStack fluid;
    private final ResourceLocation tag;
    private final TagKey<Item> itemTag;
    private final TagKey<Fluid> fluidTag;

    public Entry(ItemStack icon, FluidStack fluid, ResourceLocation tag) {
      this.icon = icon;
      this.fluid = fluid;
      this.tag = tag;
      itemTag = tag == null ? null : TagKey.create(Registries.ITEM, tag);
      fluidTag = tag == null ? null : TagKey.create(Registries.FLUID, tag);
    }

    public ItemStack icon() {
      return icon;
    }

    public FluidStack fluid() {
      return fluid;
    }

    public ResourceLocation tag() {
      return tag;
    }

    public static Entry stack(ItemStack stack) {
      return new Entry(
          stack.copyWithCount(1),
          FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).copy(),
          null);
    }

    boolean matches(ItemStack candidate, boolean exact) {
      return tag != null
          ? candidate.is(itemTag)
          : !icon.isEmpty()
              && (exact
                  ? ItemStack.isSameItemSameComponents(icon, candidate)
                  : icon.is(candidate.getItem()));
    }

    boolean matches(FluidStack candidate, boolean exact) {
      return tag != null
          ? candidate.is(fluidTag)
          : !fluid.isEmpty()
              && (exact
                  ? FluidStack.isSameFluidSameComponents(fluid, candidate)
                  : fluid.getFluid() == candidate.getFluid());
    }
  }

  public boolean accepts(ItemStack stack) {
    if (!configured) return true;
    for (Entry entry : entries) if (entry != null && entry.matches(stack, exact)) return !deny;
    return deny;
  }

  public boolean accepts(FluidStack stack) {
    if (!configured) return true;
    for (Entry entry : entries) if (entry != null && entry.matches(stack, exact)) return !deny;
    return deny;
  }

  CompoundTag save(HolderLookup.Provider registries) {
    var tag = new CompoundTag();
    tag.putInt("priority", priority);
    tag.putInt("limit", extractionLimit);
    tag.putInt("insert_redstone", insertRedstone);
    tag.putInt("extract_redstone", extractRedstone);
    tag.putInt("retry_cap", retryCap);
    tag.putInt("routing", routing.ordinal());
    tag.putBoolean("configured", configured);
    tag.putBoolean("deny", deny);
    tag.putBoolean("exact", exact);
    tag.putBoolean("sensor", sensor);
    tag.putBoolean("disposal", disposal);
    var list = new ListTag();
    for (Entry entry : entries) {
      var saved = new CompoundTag();
      if (entry != null) {
        if (!entry.icon.isEmpty()) saved.put("icon", entry.icon.save(registries));
        if (!entry.fluid.isEmpty()) saved.put("fluid", entry.fluid.save(registries));
        if (entry.tag != null) saved.putString("tag", entry.tag.toString());
      }
      list.add(saved);
    }
    tag.put("entries", list);
    return tag;
  }

  static TransportSideConfig load(CompoundTag tag, HolderLookup.Provider registries) {
    var config = new TransportSideConfig();
    config.priority = Math.clamp(tag.getInt("priority"), -1000, 1000);
    config.extractionLimit =
        tag.contains("limit") ? Math.max(1, tag.getInt("limit")) : Integer.MAX_VALUE;
    config.insertRedstone = Math.clamp(tag.getInt("insert_redstone"), 0, 2);
    config.extractRedstone = Math.clamp(tag.getInt("extract_redstone"), 0, 2);
    config.retryCap = Math.clamp(tag.getInt("retry_cap"), 0, 80);
    config.routing =
        Routing.values()[Math.clamp(tag.getInt("routing"), 0, Routing.values().length - 1)];
    config.configured = tag.getBoolean("configured");
    config.deny = tag.getBoolean("deny");
    config.exact = !tag.contains("exact") || tag.getBoolean("exact");
    config.sensor = tag.getBoolean("sensor");
    config.disposal = tag.getBoolean("disposal");
    var list = tag.getList("entries", 10);
    for (int i = 0; i < Math.min(9, list.size()); i++) {
      var saved = list.getCompound(i);
      if (!saved.isEmpty())
        config.entries[i] =
            new Entry(
                ItemStack.parseOptional(registries, saved.getCompound("icon")),
                FluidStack.parseOptional(registries, saved.getCompound("fluid")),
                saved.contains("tag") ? ResourceLocation.tryParse(saved.getString("tag")) : null);
    }
    return config;
  }
}
