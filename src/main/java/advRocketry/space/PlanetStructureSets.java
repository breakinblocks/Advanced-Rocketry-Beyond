package advRocketry.space;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.StructureSet;

final class PlanetStructureSets {
    private PlanetStructureSets() {}

    static HolderLookup<StructureSet> forPlanet(HolderLookup<StructureSet> parent, List<String> allowed) {
        if (allowed.isEmpty()) return parent;
        return new HolderLookup<>() {
            @Override
            public Stream<Holder.Reference<StructureSet>> listElements() {
                return parent.listElements().filter(set -> allowed.stream().anyMatch(entry -> matches(set, entry)));
            }

            @Override
            public Stream<HolderSet.Named<StructureSet>> listTags() {
                return parent.listTags();
            }

            @Override
            public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> key) {
                return parent.get(key);
            }

            @Override
            public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> tag) {
                return parent.get(tag);
            }
        };
    }

    private static boolean matches(Holder<StructureSet> set, String entry) {
        if (entry.startsWith("#"))
            return set.is(TagKey.create(Registries.STRUCTURE_SET, ResourceLocation.parse(entry.substring(1))));
        return set.is(ResourceLocation.parse(entry));
    }
}
