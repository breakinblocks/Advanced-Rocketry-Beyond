package advRocketry.datagen;

import advRocketry.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

final class ModSounds extends SoundDefinitionsProvider {
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
                .with(sound(event.getLocation()))
                .subtitle("subtitles." + Main.MODID + "." + event.getLocation().getPath()));
  }
}
