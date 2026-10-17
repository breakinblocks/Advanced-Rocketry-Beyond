package advRocketry.datagen;

import advRocketry.Main;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

final class ModSounds extends SoundDefinitionsProvider {
  private static final Set<String> INDUSTRIAL_LOOPS =
      Set.of("crystallizer", "cuttingmachine", "electricarcfurnace", "electrolyser", "precass");

  ModSounds(PackOutput output, ExistingFileHelper files) {
    super(output, Main.MODID, files);
  }

  @Override
  public void registerSounds() {
    for (SoundEvent event : BuiltInRegistries.SOUND_EVENT)
      if (event.getLocation().getNamespace().equals(Main.MODID))
        add(
            event,
            definition()
                .with(
                    sound(
                        INDUSTRIAL_LOOPS.contains(event.getLocation().getPath())
                            ? ResourceLocation.fromNamespaceAndPath(Main.MODID, "machinelarge")
                            : event.getLocation()))
                .subtitle("subtitles." + Main.MODID + "." + event.getLocation().getPath()));
  }
}
