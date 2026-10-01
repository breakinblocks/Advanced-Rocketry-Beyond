package advRocketry.datagen;

import advRocketry.DataRegistries;
import advRocketry.Main;
import advRocketry.multiblock.Multiblock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

final class ModMultiblocks {
  private ModMultiblocks() {}

  static void bootstrap(BootstrapContext<Multiblock> context) {
    new Builder(context, "lathe", 0, true)
        .layer("cMaI")
        .layer("PSSO")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('S', "adv_rocketry:block_structure_block")
        .key('a', true)
        .register();
    new Builder(context, "rolling_machine", 1, true)
        .layer("aaaaa", "SSSSS", "SSSBS")
        .layer("PcIaa", "SMMBS", "SLOBS")
        .key('B', "adv_rocketry:steel_block")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('S', "adv_rocketry:block_structure_block")
        .key('a', true)
        .register();
    new Builder(context, "crystallizer", 2, true)
        .layer("QQQ", "QQQ")
        .layer("OcI", "lPL")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('Q', "adv_rocketry:quartz_crucible")
        .key('l', "adv_rocketry:block_fluid_output_block")
        .register();
    new Builder(context, "electrolyzer", 3, true)
        .layer("   ", "PKP")
        .layer("lcl", "SLS")
        .key('K', "#adv_rocketry:coils")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('S', "adv_rocketry:block_structure_block")
        .key('l', "adv_rocketry:block_fluid_output_block")
        .register();
    new Builder(context, "chemical_reactor", 4, true)
        .layer(" c ", "LIL")
        .layer("PMP", "lOl")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('l', "adv_rocketry:block_fluid_output_block")
        .register();
    new Builder(context, "cutting_machine", 5, true)
        .layer("IcO", "MWP")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('W', "adv_rocketry:saw_blade_assembly")
        .register();
    new Builder(context, "precision_assembler", 6, true)
        .layer("SSSS", "SSSS", "SSSS")
        .layer("SGGS", "SaaS", "SSSS")
        .layer("c***", "*KK*", "*MM*")
        .key(
            '*',
            "adv_rocketry:block_item_input_block",
            "adv_rocketry:block_item_output_block",
            "adv_rocketry:block_fluid_input_block",
            "adv_rocketry:block_fluid_output_block",
            "adv_rocketry:block_energy_input_block",
            "adv_rocketry:creative_energy_input",
            "adv_rocketry:block_structure_block")
        .key('G', "minecraft:glass")
        .key('K', "#adv_rocketry:coils")
        .key('M', "#adv_rocketry:motors")
        .key('S', "adv_rocketry:block_structure_block")
        .key('a', true)
        .register();
    new Builder(context, "precision_laser_etcher", 7, true)
        .layer("sss", "asa", "sss")
        .layer("TaS", "aVS", "TaS")
        .layer("ScI", "PMO", "PSS")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('S', "adv_rocketry:block_structure_block")
        .key('T', "adv_rocketry:structure_tower")
        .key('V', "adv_rocketry:vacuum_laser")
        .key('a', true)
        .key('s', "#minecraft:slabs")
        .register();
    new Builder(context, "centrifuge", 8, true)
        .layer("aSl", "UUS", "UU ")
        .layer("aSl", "UUS", "UUl")
        .layer("cSl", "UUS", "UUl")
        .layer("PLl", "MOS", "SSl")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('S', "adv_rocketry:block_structure_block")
        .key('U', "adv_rocketry:centrifuge_casing")
        .key('a', true)
        .key('l', "adv_rocketry:block_fluid_output_block")
        .register();
    new Builder(context, "electric_arc_furnace", 9, true)
        .layer("     ", " PbP ", " bbb ", " bPb ", "     ")
        .layer(" bbb ", "bKaKb", "baaab", "baKab", " bbb ")
        .layer("bbbbb", "baaab", "baaab", "baaab", "bbbbb")
        .layer("b*c*b", "*bbb*", "*bbb*", "*bbb*", "b***b")
        .layer("bbbbb", "bbbbb", "bbbbb", "bbbbb", "bbbbb")
        .key(
            '*',
            "adv_rocketry:block_item_input_block",
            "adv_rocketry:block_item_output_block",
            "adv_rocketry:block_fluid_input_block",
            "adv_rocketry:block_fluid_output_block",
            "adv_rocketry:block_energy_input_block",
            "adv_rocketry:creative_energy_input",
            "adv_rocketry:blast_brick")
        .key('K', "#adv_rocketry:coils")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .key('a', true)
        .key('b', "adv_rocketry:blast_brick")
        .register();
    new Builder(context, "black_hole_generator", 10, true)
        .layer("   ", " A ", "   ")
        .layer("   ", " A ", "   ")
        .layer(" A ", " A ", "   ")
        .layer(" c ", "#A#", " # ")
        .layer("   ", " A ", "   ")
        .key('#', "adv_rocketry:block_advanced_structure_block", "#adv_rocketry:machine_ports")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .register();
    new Builder(context, "area_gravity_controller", 11, false)
        .layer("   ", " c ", "   ")
        .layer(" A ", "APA", " A ")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .key('P', "adv_rocketry:block_energy_input_block")
        .register();
    new Builder(context, "microwave_receiver", 12, false)
        .layer("p   p", " ppp ", " pcp ", " ppp ", "p   p")
        .key('p', "adv_rocketry:solar_panel")
        .register();
    new Builder(context, "beacon", 13, false)
        .layer("   ", " R ", "   ")
        .layer("   ", " S ", "   ")
        .layer("   ", " S ", "   ")
        .layer("   ", " S ", "   ")
        .layer(" c ", "SSS", " S ")
        .key('R', "minecraft:redstone_block")
        .key('S', "adv_rocketry:block_structure_block")
        .register();
    new Builder(context, "warp_core", 14, true)
        .layer("XXX", "XIX", "XXX")
        .layer(" S ", "SgS", " S ")
        .layer("XcX", "XgX", "XXX")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('S', "adv_rocketry:block_structure_block")
        .key('X', "adv_rocketry:titanium_block")
        .key('g', "minecraft:gold_block")
        .register();
    new Builder(context, "satellite_builder", 15, false)
        .layer("c")
        .layer("P")
        .key('P', "adv_rocketry:block_energy_input_block", "adv_rocketry:creative_energy_input")
        .register();
    new Builder(context, "solar_array", 16, true)
        .layer(
            "ece", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp",
            "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp", "ppp")
        .key('e', "adv_rocketry:block_energy_output_block")
        .key('p', true, "adv_rocketry:solar_array_panel")
        .register();
    new Builder(context, "biome_scanner", 17, false)
        .layer("     ", "     ", "  c  ", "     ", "     ")
        .layer("     ", "     ", "  M  ", "     ", "     ")
        .layer(" ZZZ ", "ZZTZZ", "ZTSTZ", "ZZTZZ", " ZZZ ")
        .layer("     ", "     ", "  R  ", "     ", "     ")
        .key('M', "#adv_rocketry:motors")
        .key('R', "minecraft:redstone_block")
        .key('S', "adv_rocketry:block_structure_block")
        .key('T', "adv_rocketry:structure_tower")
        .key('Z', "adv_rocketry:aluminum_block")
        .register();
    new Builder(context, "astrobody_data_processor", 18, true)
        .layer("scs", "sss")
        .layer("PIO", "ddd")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block")
        .key('d', "adv_rocketry:data_bus")
        .key('s', "#minecraft:slabs")
        .register();
    new Builder(context, "railgun", 19, true)
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "         ",
            "    C    ",
            "   CSC   ",
            "    C    ",
            "         ",
            "         ",
            "         ")
        .layer(
            "         ",
            "         ",
            "    B    ",
            "   AXA   ",
            "  BXXXB  ",
            "   AXA   ",
            "    B    ",
            "         ",
            "         ")
        .layer(
            "B  sss  B",
            " AsIcOsA ",
            " sAAAAAs ",
            "ssAAAAAss",
            "ssAAMAAss",
            "ssAAAAAss",
            " sAAAAAs ",
            " AsPPPsA ",
            "B  sss  B")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .key('B', "adv_rocketry:steel_block")
        .key('C', "adv_rocketry:block_coilcopper")
        .key('I', "adv_rocketry:block_item_input_block")
        .key('M', "#adv_rocketry:motors")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block")
        .key('S', "adv_rocketry:block_structure_block")
        .key('X', "adv_rocketry:titanium_block")
        .key('s', "#minecraft:slabs")
        .register();
    new Builder(context, "atmosphere_terraformer", 20, true)
        .layer(
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "        v        ",
            "     vAAAAAv     ",
            "     AAAAAAA     ",
            "     AAAAAAA     ",
            "    vAAAAAAAv    ",
            "     AAAAAAA     ",
            "     AAAAAAA     ",
            "     vAAAAAv     ",
            "        v        ",
            "                 ",
            "                 ",
            "                 ",
            "                 ")
        .layer(
            "                 ",
            "                 ",
            "                 ",
            "        A        ",
            "      AAAAA      ",
            "     AAAAAAA     ",
            "    AAAAAAAAA    ",
            "    AAAAAAAAA    ",
            "   AAAAAAAAAAA   ",
            "    AAAAAAAAA    ",
            "    AAAAAAAAA    ",
            "     AAAAAAA     ",
            "      AAAAA      ",
            "        A        ",
            "                 ",
            "                 ",
            "                 ")
        .layer(
            "                 ",
            "                 ",
            "                 ",
            "      AAAAA      ",
            "     AAAAAAA     ",
            "    AAAAAAAAA    ",
            "   AAAAAAAAAAA   ",
            "   AAAAAAAAAAA   ",
            "   AAAAAAAAAAA   ",
            "   AAAAAAAAAAA   ",
            "   AAAAAAAAAAA   ",
            "    AAAAAAAAA    ",
            "     AAAAAAA     ",
            "      AAAAA      ",
            "                 ",
            "                 ",
            "                 ")
        .layer(
            "                 ",
            "       y y       ",
            "       y y       ",
            "      AyAyA      ",
            "     AAAAAAA     ",
            "    AAAAAAAAA    ",
            "   AAAAAAAAAAA   ",
            " yyyAAAAAAAAAyyy ",
            "   AAAAAAAAAAA   ",
            " yyyAAAAAAAAAyyy ",
            "   AAAAAAAAAAA   ",
            "    AAAAAAAAA    ",
            "     AAAAAAA     ",
            "      AyAyA      ",
            "       y y       ",
            "       y y       ",
            "                 ")
        .layer(
            "       y y       ",
            "       y y       ",
            "                 ",
            "      AAAAA      ",
            "     AAAAAAA     ",
            "    AAAAAAAAA    ",
            "   AAAAAAAAAAA   ",
            "y      AAA      y",
            "   AAAAAAAAAAA   ",
            "y      AAA      y",
            "   AAAAAAAAAAA   ",
            "    AAAAAAAAA    ",
            "     AAAAAAA     ",
            "      AAAAA      ",
            "                 ",
            "       y y       ",
            "       y y       ")
        .layer(
            "       y y       ",
            "       y y       ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "y      AAA      y",
            "       AAA       ",
            "y      AAA      y",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "       y y       ",
            "       y y       ")
        .layer(
            "       y y       ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "y      AAA      y",
            "       AAA       ",
            "y      AAA      y",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "       y y       ")
        .layer(
            "       y y       ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "y      AAA      y",
            "       AAA       ",
            "y      AAA      y",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "       y y       ")
        .layer(
            "       y y       ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "y      AAA      y",
            "       PcP       ",
            "y      APA      y",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "                 ",
            "       y y       ")
        .layer(
            "       y y       ",
            "                 ",
            "                 ",
            "      kkkkk      ",
            "     kkFFFkk     ",
            "    kkkFFFkkk    ",
            "   kkkkkkkkkkk   ",
            "y  kFFkkkkkFFk  y",
            "   kFFkkkkkFFk   ",
            "y  kFFkkkkkFFk  y",
            "   kkkkkkkkkkk   ",
            "    kkkFFFkkk    ",
            "     kkFFFkk     ",
            "      kkkkk      ",
            "                 ",
            "                 ",
            "       y y       ")
        .layer(
            "       y y       ",
            "       y y       ",
            "       y y       ",
            "      kkkkk      ",
            "     kkFFFkk     ",
            "    kkkFFFkkk    ",
            "   kkkkkkkkkkk   ",
            "yyykFFkkkkkFFkyyy",
            "   kFFkkkkkFFk   ",
            "yyykFFkkkkkFFkyyy",
            "   kkkkkkkkkkk   ",
            "    kkkFFFkkk    ",
            "     kkFFFkk     ",
            "      kkkkk      ",
            "       y y       ",
            "       y y       ",
            "       y y       ")
        .layer(
            "                 ",
            "       y y       ",
            "       y y       ",
            "      kyLyk      ",
            "     kkFFFkk     ",
            "    kkkFFFkkk    ",
            "   kkkkkkkkkkk   ",
            " yyyFFkkkkkFFyyy ",
            "   LFFkkkkkFFL   ",
            " yyyFFkkkkkFFyyy ",
            "   kkkkkkkkkkk   ",
            "    kkkFFFkkk    ",
            "     kkFFFkk     ",
            "      kyLyk      ",
            "       y y       ",
            "       y y       ",
            "                 ")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .key('F', "adv_rocketry:fuel_tank")
        .key('L', "adv_rocketry:block_fluid_input_block")
        .key('P', "adv_rocketry:block_energy_input_block")
        .key('k', "adv_rocketry:concrete")
        .key('v', "adv_rocketry:oxygen_vent")
        .key('y', "minecraft:clay")
        .register();
    new Builder(context, "orbital_laser", 21, true)
        .layer(
            "           ",
            "           ",
            "           ",
            " A         ",
            "AAA        ",
            " A         ",
            "           ",
            "           ",
            "           ")
        .layer(
            "      AVVV ",
            "    ANNVVVP",
            "    AAAVVVP",
            "SAS N AVVV ",
            "AAANA      ",
            "SAS N AVVV ",
            "    AAAVVVP",
            "    ANNVVVP",
            "      AVVV ")
        .layer(
            "      AVVV ",
            "    ANNVVVP",
            "OcO AAAVVVP",
            "SSS N AVVV ",
            "SSSNA      ",
            "SSS N AVVV ",
            "    AAAVVVP",
            "    ANNVVVP",
            "      AVVV ")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .key('N', "adv_rocketry:lens_block")
        .key('O', "adv_rocketry:block_item_output_block")
        .key('P', "adv_rocketry:block_energy_input_block")
        .key('S', "adv_rocketry:block_structure_block")
        .key('V', "adv_rocketry:vacuum_laser")
        .register();
    new Builder(context, "space_elevator", 22, true)
        .layer(
            "aaaPcPaaa",
            "BaasssaaB",
            "aAsssssAa",
            "asAsssAsa",
            "sssAAAsss",
            "sssAMAsss",
            "sssAAAsss",
            "asAsssAsa",
            "aAsssssAa",
            "BaasssaaB")
        .key('A', "adv_rocketry:block_advanced_structure_block")
        .key('B', "adv_rocketry:steel_block")
        .key('M', "#adv_rocketry:motors")
        .key('P', "adv_rocketry:block_energy_input_block")
        .key('a', true)
        .key('s', "#minecraft:slabs")
        .register();
    new Builder(context, "observatory", 23, true)
        .layer("     ", " SnS ", " SSS ", " SSS ", "     ")
        .layer("     ", " SSS ", " SnS ", " SSS ", "     ")
        .layer(" SSS ", "SaaaS", "SaaaS", "SanaS", " SSS ")
        .layer(" *c* ", "*SSS*", "*SSS*", "*SSS*", " *** ")
        .layer(" *** ", "*TTT*", "*TMT*", "*TTT*", " *** ")
        .key('*', "minecraft:iron_block", "#adv_rocketry:machine_ports")
        .key('M', "#adv_rocketry:motors")
        .key('S', "adv_rocketry:block_structure_block")
        .key('T', "adv_rocketry:structure_tower")
        .key('a', true)
        .key('n', "minecraft:glass", "adv_rocketry:lens_block")
        .register();
  }

  private static final class Builder {
    private final BootstrapContext<Multiblock> context;
    private final HolderGetter<Block> blocks;
    private final String id;
    private final int order;
    private final boolean rotates;
    private final List<List<String>> layers = new ArrayList<>();
    private final Map<Character, Multiblock.Key> keys = new LinkedHashMap<>();

    private Builder(BootstrapContext<Multiblock> context, String id, int order, boolean rotates) {
      this.context = context;
      this.blocks = context.lookup(Registries.BLOCK);
      this.id = id;
      this.order = order;
      this.rotates = rotates;
    }

    private Builder layer(String... rows) {
      layers.add(List.of(rows));
      return this;
    }

    private Builder key(char symbol, String... entries) {
      return key(symbol, false, entries);
    }

    private Builder key(char symbol, boolean air, String... entries) {
      List<HolderSet<Block>> sets = new ArrayList<>();
      List<Holder<Block>> direct = new ArrayList<>();
      for (String entry : entries) {
        if (entry.startsWith("#"))
          sets.add(
              blocks.getOrThrow(
                  TagKey.create(Registries.BLOCK, ResourceLocation.parse(entry.substring(1)))));
        else
          direct.add(
              blocks.getOrThrow(
                  ResourceKey.create(Registries.BLOCK, ResourceLocation.parse(entry))));
      }
      if (!direct.isEmpty()) sets.addFirst(HolderSet.direct(direct));
      keys.put(symbol, new Multiblock.Key(sets, air));
      return this;
    }

    private void register() {
      Block controller =
          BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Main.MODID, id));
      if (controller == Blocks.AIR) throw new IllegalStateException("Unknown controller " + id);
      context.register(
          ResourceKey.create(
              DataRegistries.MULTIBLOCK, ResourceLocation.fromNamespaceAndPath(Main.MODID, id)),
          new Multiblock(controller, rotates, order, layers, keys));
    }
  }
}
