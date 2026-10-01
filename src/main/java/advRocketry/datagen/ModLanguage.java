package advRocketry.datagen;

import advRocketry.Main;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class ModLanguage extends LanguageProvider {
  private final Map<String, String> entries = new TreeMap<>();

  ModLanguage(PackOutput output) {
    super(output, Main.MODID, "en_us");
  }

  @Override
  public void add(String key, String value) {
    entries.put(key, value);
    super.add(key, value);
  }

  @Override
  protected void addTranslations() {
    addItem();
    addBlock();
    addFluidType();
    addKey();
    addEnchantment();
    addEntity();
    addMessage();
    addHud();
    addAtmosphere();
    addAdvancement();
    addAdvRocketry();
    addAsteroid();
    addJadeText();
    addSatelliteTypes();
    addSubtitles();
    addStatusText();
    addMessageText();
    addGuiText();
    List<String> missing = new ArrayList<>();
    for (Item item : BuiltInRegistries.ITEM)
      if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Main.MODID)
          && !entries.containsKey(item.getDescriptionId())) missing.add(item.getDescriptionId());
    if (!missing.isEmpty()) throw new IllegalStateException("Missing English names: " + missing);
  }

  private void addItem() {
    add("item.adv_rocketry.dilithium_dust", "Dilithium Dust");
    add("item.adv_rocketry.iron_dust", "Iron Dust");
    add("item.adv_rocketry.iron_plate", "Iron Plate");
    add("item.adv_rocketry.iron_sheet", "Iron Sheet");
    add("item.adv_rocketry.iron_rod", "Iron Rod");
    add("item.adv_rocketry.gold_dust", "Gold Dust");
    add("item.adv_rocketry.gold_plate", "Gold Plate");
    add("item.adv_rocketry.silicon_boule", "Silicon Boule");
    add("item.adv_rocketry.silicon_dust", "Silicon Dust");
    add("item.adv_rocketry.silicon_ingot", "Silicon Ingot");
    add("item.adv_rocketry.silicon_nugget", "Silicon Nugget");
    add("item.adv_rocketry.silicon_plate", "Silicon Plate");
    add("item.adv_rocketry.copper_dust", "Copper Dust");
    add("item.adv_rocketry.copper_nugget", "Copper Nugget");
    add("item.adv_rocketry.copper_plate", "Copper Plate");
    add("item.adv_rocketry.copper_sheet", "Copper Sheet");
    add("item.adv_rocketry.copper_rod", "Copper Rod");
    add("item.adv_rocketry.tin_dust", "Tin Dust");
    add("item.adv_rocketry.tin_ingot", "Tin Ingot");
    add("item.adv_rocketry.tin_nugget", "Tin Nugget");
    add("item.adv_rocketry.tin_plate", "Tin Plate");
    add("item.adv_rocketry.steel_dust", "Steel Dust");
    add("item.adv_rocketry.steel_fan", "Steel Fan");
    add("item.adv_rocketry.steel_gear", "Steel Gear");
    add("item.adv_rocketry.steel_ingot", "Steel Ingot");
    add("item.adv_rocketry.steel_nugget", "Steel Nugget");
    add("item.adv_rocketry.steel_plate", "Steel Plate");
    add("item.adv_rocketry.steel_sheet", "Steel Sheet");
    add("item.adv_rocketry.steel_rod", "Steel Rod");
    add("item.adv_rocketry.titanium_dust", "Titanium Dust");
    add("item.adv_rocketry.titanium_gear", "Titanium Gear");
    add("item.adv_rocketry.titanium_ingot", "Titanium Ingot");
    add("item.adv_rocketry.titanium_nugget", "Titanium Nugget");
    add("item.adv_rocketry.titanium_plate", "Titanium Plate");
    add("item.adv_rocketry.titanium_sheet", "Titanium Sheet");
    add("item.adv_rocketry.titanium_rod", "Titanium Rod");
    add("item.adv_rocketry.aluminum_dust", "Aluminum Dust");
    add("item.adv_rocketry.aluminum_ingot", "Aluminum Ingot");
    add("item.adv_rocketry.aluminum_nugget", "Aluminum Nugget");
    add("item.adv_rocketry.aluminum_plate", "Aluminum Plate");
    add("item.adv_rocketry.aluminum_sheet", "Aluminum Sheet");
    add("item.adv_rocketry.iridium_dust", "Iridium Dust");
    add("item.adv_rocketry.iridium_ingot", "Iridium Ingot");
    add("item.adv_rocketry.iridium_nugget", "Iridium Nugget");
    add("item.adv_rocketry.iridium_plate", "Iridium Plate");
    add("item.adv_rocketry.iridium_rod", "Iridium Rod");
    add("item.adv_rocketry.titanium_aluminide_dust", "Titanium Aluminide Dust");
    add("item.adv_rocketry.titanium_aluminide_gear", "Titanium Aluminide Gear");
    add("item.adv_rocketry.titanium_aluminide_ingot", "Titanium Aluminide Ingot");
    add("item.adv_rocketry.titanium_aluminide_nugget", "Titanium Aluminide Nugget");
    add("item.adv_rocketry.titanium_aluminide_plate", "Titanium Aluminide Plate");
    add("item.adv_rocketry.titanium_aluminide_sheet", "Titanium Aluminide Sheet");
    add("item.adv_rocketry.titanium_aluminide_rod", "Titanium Aluminide Rod");
    add("item.adv_rocketry.titanium_iridium_dust", "Titanium Iridium Dust");
    add("item.adv_rocketry.titanium_iridium_gear", "Titanium Iridium Gear");
    add("item.adv_rocketry.titanium_iridium_ingot", "Titanium Iridium Ingot");
    add("item.adv_rocketry.titanium_iridium_nugget", "Titanium Iridium Nugget");
    add("item.adv_rocketry.titanium_iridium_plate", "Titanium Iridium Plate");
    add("item.adv_rocketry.titanium_iridium_sheet", "Titanium Iridium Sheet");
    add("item.adv_rocketry.titanium_iridium_rod", "Titanium Iridium Rod");
    add("item.adv_rocketry.basic_circuit", "Basic Circuit");
    add("item.adv_rocketry.tracking_circuit", "Tracking Circuit");
    add("item.adv_rocketry.advanced_circuit", "Advanced Circuit");
    add("item.adv_rocketry.control_circuit_board", "Control Circuit Board");
    add("item.adv_rocketry.item_io_circuit_board", "Item Io Circuit Board");
    add("item.adv_rocketry.fluid_io_circuit_board", "Fluid Io Circuit Board");
    add("item.adv_rocketry.basic_circuit_plate", "Basic Circuit Plate");
    add("item.adv_rocketry.advanced_circuit_plate", "Advanced Circuit Plate");
    add("item.adv_rocketry.silicon_wafer", "Silicon Wafer");
    add("item.adv_rocketry.basic_lens", "Basic Lens");
    add("item.adv_rocketry.user_interface", "User Interface");
    add("item.adv_rocketry.carbon_scrubber_cartridge", "Carbon Scrubber Cartridge");
    add("item.adv_rocketry.elevator_chip", "Elevator Chip");
    add("item.adv_rocketry.beacon_finder", "Beacon Finder");
    add("item.adv_rocketry.atmosphere_upgrade", "Anti-Fog Visor");
    add("item.adv_rocketry.hover_upgrade", "Hover Upgrade");
    add("item.adv_rocketry.flight_speed_upgrade", "Flight Speed Control Upgrade");
    add("item.adv_rocketry.padded_boots_upgrade", "Padded Landing Boots");
    add("item.adv_rocketry.iron_saw_blade", "Iron Saw Blade");
    add("item.adv_rocketry.solar_cell", "Solar Cell");
    add("item.adv_rocketry.thermite", "Thermite");
    add("item.adv_rocketry.jackhammer", "Jackhammer");
    add("item.adv_rocketry.basic_laser_gun", "Basic Laser Gun");
    add("item.adv_rocketry.optical_sensor", "Optical Sensor");
    add("item.adv_rocketry.linker", "Linker");
    add("item.adv_rocketry.item_holoprojector", "Holographic Projector");
    add("item.adv_rocketry.battery", "Battery");
    add("item.adv_rocketry.advanced_battery", "Advanced Battery");
    add("item.adv_rocketry.item_data_storage", "Item Data Storage");
    add("item.adv_rocketry.atm_analyzer", "Atm Analyzer");
    add("item.adv_rocketry.biome_changer_remote", "Biome Changer Remote");
    add("item.adv_rocketry.satellite_id_chip", "Satellite Id Chip");
    add("item.adv_rocketry.satellite_biome_changer", "Satellite Biome Changer");
    add("item.adv_rocketry.legs_upgrade", "Bionic Leg Upgrade");
    add("item.adv_rocketry.night_vision_upgrade", "Earthbright Visor");
    add("item.adv_rocketry.jetpack", "Jetpack");
    add("item.adv_rocketry.portable_pressure_tank_aluminum", "Portable Pressure Tank Aluminum");
    add("item.adv_rocketry.portable_pressure_tank_steel", "Portable Pressure Tank Steel");
    add("item.adv_rocketry.dilithium_crystal", "Dilithium Crystal");
    add("item.adv_rocketry.portable_pressure_tank_titanium", "Portable Pressure Tank Titanium");
    add("item.adv_rocketry.portable_pressure_tank_iridium", "Portable Pressure Tank Iridium");
    add("item.adv_rocketry.carbon_brick", "Carbon Brick");
    add("item.adv_rocketry.oxygen_bucket", "Oxygen Bucket");
    add("item.adv_rocketry.hydrogen_bucket", "Hydrogen Bucket");
    add("item.adv_rocketry.nitrogen_bucket", "Nitrogen Bucket");
    add("item.adv_rocketry.helium_bucket", "Helium Bucket");
    add("item.adv_rocketry.helium3_bucket", "Helium-3 Bucket");
    add("item.adv_rocketry.ammonia_bucket", "Ammonia Bucket");
    add("item.adv_rocketry.methane_bucket", "Methane Bucket");
    add("item.adv_rocketry.rocket_fuel_bucket", "Rocket Fuel Bucket");
    add("item.adv_rocketry.enriched_lava_bucket", "Enriched Lava Bucket");
    add("item.adv_rocketry.hovercraft", "Hovercraft");
    add("item.adv_rocketry.unlit_torch", "Extinguished Torch");
    add("item.adv_rocketry.asteroid_chip", "Asteroid Chip");
    add("item.adv_rocketry.basic_satellite_solar_panel", "Basic Satellite Solar Panel");
    add("item.adv_rocketry.composition_sensor", "Composition Sensor");
    add("item.adv_rocketry.large_satellite_solar_panel", "Large Satellite Solar Panel");
    add("item.adv_rocketry.mass_detector", "Mass Detector");
    add("item.adv_rocketry.microwave_transmitter", "Microwave Transmitter");
    add("item.adv_rocketry.ore_mapper", "Ore Mapper");
    add("item.adv_rocketry.ore_scanner", "Ore Scanner");
    add("item.adv_rocketry.planet_id_chip", "Planet Id Chip");
    add("item.adv_rocketry.satellite", "Satellite");
    add("item.adv_rocketry.satellite_data_unit", "Satellite Data Unit");
    add("item.adv_rocketry.seal_detector", "Seal Detector");
    add("item.adv_rocketry.space_boots", "Space Boots");
    add("item.adv_rocketry.space_chestplate", "Space Chestplate");
    add("item.adv_rocketry.space_helmet", "Space Helmet");
    add("item.adv_rocketry.space_leggings", "Space Leggings");
    add("item.adv_rocketry.space_station", "Space Station");
    add("item.adv_rocketry.station_id_chip", "Station Id Chip");
  }

  private void addBlock() {
    add("block.adv_rocketry.gold_coil", "Gold Coil");
    add("block.adv_rocketry.copper_coil", "Copper Coil");
    add("block.adv_rocketry.tin_block", "Tin Block");
    add("block.adv_rocketry.tin_ore", "Tin Ore");
    add("block.adv_rocketry.steel_block", "Steel Block");
    add("block.adv_rocketry.titanium_block", "Titanium Block");
    add("block.adv_rocketry.titanium_coil", "Titanium Coil");
    add("block.adv_rocketry.titanium_ore", "Titanium Ore");
    add("block.adv_rocketry.aluminum_block", "Aluminum Block");
    add("block.adv_rocketry.aluminum_coil", "Aluminum Coil");
    add("block.adv_rocketry.aluminum_ore", "Aluminum Ore");
    add("block.adv_rocketry.iridium_block", "Iridium Block");
    add("block.adv_rocketry.iridium_coil", "Iridium Coil");
    add("block.adv_rocketry.iridium_ore", "Iridium Ore");
    add("block.adv_rocketry.titanium_aluminide_block", "Titanium Aluminide Block");
    add("block.adv_rocketry.titanium_iridium_block", "Titanium Iridium Block");
    add("block.adv_rocketry.dilithium_ore", "Dilithium Ore");
    add("block.adv_rocketry.geode", "Geode");
    add("block.adv_rocketry.blast_brick", "Blast Brick");
    add("block.adv_rocketry.quartz_crucible", "Quartz Crucible");
    add("block.adv_rocketry.saw_blade_assembly", "Saw Blade Assembly");
    add("block.adv_rocketry.centrifuge_casing", "Centrifuge Casing");
    add("block.adv_rocketry.lens_block", "Lens Block");
    add("block.adv_rocketry.alien_log", "Alien Log");
    add("block.adv_rocketry.alien_planks", "Alien Planks");
    add("block.adv_rocketry.alien_sapling", "Alien Sapling");
    add("block.adv_rocketry.electric_mushroom", "Electric Mushroom");
    add("block.adv_rocketry.nuclear_core", "Nuclear Core");
    add("block.adv_rocketry.gas_intake", "Gas Intake");
    add("block.adv_rocketry.concrete", "Concrete");
    add("block.adv_rocketry.moon_turf", "Moon Turf");
    add("block.adv_rocketry.moon_turf_dark", "Moon Turf Dark");
    add("block.adv_rocketry.hot_turf", "Hot Turf");
    add("block.adv_rocketry.basalt", "Basalt");
    add("block.adv_rocketry.vitrified_sand", "Vitrified Sand");
    add("block.adv_rocketry.charcoal_log", "Charcoal Log");
    add("block.adv_rocketry.alien_leaves", "Alien Leaves");
    add("block.adv_rocketry.amethyst_crystal_block", "Amethyst Crystal Block");
    add("block.adv_rocketry.sapphire_crystal_block", "Sapphire Crystal Block");
    add("block.adv_rocketry.emerald_crystal_block", "Emerald Crystal Block");
    add("block.adv_rocketry.ruby_crystal_block", "Ruby Crystal Block");
    add("block.adv_rocketry.citrine_crystal_block", "Citrine Crystal Block");
    add("block.adv_rocketry.wulfentite_crystal_block", "Wulfentite Crystal Block");
    add("block.adv_rocketry.lathe", "Lathe");
    add("block.adv_rocketry.rolling_machine", "Rolling Machine");
    add("block.adv_rocketry.crystallizer", "Crystallizer");
    add("block.adv_rocketry.electrolyzer", "Electrolyzer");
    add("block.adv_rocketry.chemical_reactor", "Chemical Reactor");
    add("block.adv_rocketry.cutting_machine", "Cutting Machine");
    add("block.adv_rocketry.precision_assembler", "Precision Assembler");
    add("block.adv_rocketry.precision_laser_etcher", "Precision Laser Etcher");
    add("block.adv_rocketry.centrifuge", "Centrifuge");
    add("block.adv_rocketry.electric_arc_furnace", "Electric Arc Furnace");
    add("block.adv_rocketry.block_item_input_block", "Input Hatch");
    add("block.adv_rocketry.block_item_output_block", "Output Hatch");
    add("block.adv_rocketry.block_fluid_input_block", "Fluid Input Hatch");
    add("block.adv_rocketry.block_fluid_output_block", "Fluid Output Hatch");
    add("block.adv_rocketry.block_energy_input_block", "Power Input Plug");
    add("block.adv_rocketry.block_energy_output_block", "Power Output Plug");
    add("block.adv_rocketry.block_structure_block", "Machine Structure");
    add("block.adv_rocketry.block_advanced_structure_block", "Advanced Machine Structure");
    add("block.adv_rocketry.block_motor_block", "Motor");
    add("block.adv_rocketry.block_coilcopper", "Copper Coil");
    add("block.adv_rocketry.structure_tower", "Structure Tower");
    add("block.adv_rocketry.vacuum_laser", "Vacuum Laser");
    add("block.adv_rocketry.solar_panel", "Solar Panel");
    add("block.adv_rocketry.data_bus", "Data Bus");
    add("block.adv_rocketry.plate_press", "Plate Press");
    add("block.adv_rocketry.advanced_motor", "Advanced Motor");
    add("block.adv_rocketry.enhanced_motor", "Enhanced Motor");
    add("block.adv_rocketry.elite_motor", "Elite Motor");
    add("block.adv_rocketry.coal_generator", "Coal Generator");
    add("block.adv_rocketry.creative_energy_input", "Creative Power Input");
    add("block.adv_rocketry.planet_selector", "Planet Selector");
    add("block.adv_rocketry.landing_float", "Landing Float");
    add("block.adv_rocketry.deployable_rocket_builder", "Deployable Rocket Assembler");
    add("block.adv_rocketry.holographic_planet_selector", "Holographic Planet Selector");
    add(
        "block.adv_rocketry.advanced_bipropellant_rocket_motor",
        "Advanced Bipropellant Rocket Motor");
    add("block.adv_rocketry.advanced_rocket_motor", "Advanced Rocket Motor");
    add("block.adv_rocketry.airlock_door", "Airlock Door");
    add("block.adv_rocketry.ammonia", "Ammonia");
    add("block.adv_rocketry.area_gravity_controller", "Area Gravity Controller");
    add("block.adv_rocketry.astrobody_data_processor", "Astrobody Data Processor");
    add("block.adv_rocketry.atmosphere_detector", "Atmosphere Detector");
    add("block.adv_rocketry.atmosphere_terraformer", "Atmosphere Terraformer");
    add("block.adv_rocketry.beacon", "Beacon");
    add("block.adv_rocketry.biome_scanner", "Biome Scanner");
    add("block.adv_rocketry.bipropellant_fuel_tank", "Bipropellant Fuel Tank");
    add("block.adv_rocketry.bipropellant_rocket_motor", "Bipropellant Rocket Motor");
    add("block.adv_rocketry.black_hole_generator", "Black Hole Generator");
    add("block.adv_rocketry.carbon_scrubber", "Carbon Scrubber");
    add("block.adv_rocketry.circle_light", "Circle Light");
    add("block.adv_rocketry.docking_port", "Docking Port");
    add("block.adv_rocketry.enriched_lava", "Enriched Lava");
    add("block.adv_rocketry.fluid_pump", "Fluid Pump");
    add("block.adv_rocketry.force_field", "Force Field");
    add("block.adv_rocketry.force_field_projector", "Force Field Projector");
    add("block.adv_rocketry.fueling_station", "Fueling Station");
    add("block.adv_rocketry.fuel_tank", "Fuel Tank");
    add("block.adv_rocketry.guidance_access_hatch", "Guidance Access Hatch");
    add("block.adv_rocketry.guidance_computer", "Guidance Computer");
    add("block.adv_rocketry.helium", "Helium");
    add("block.adv_rocketry.helium3", "Helium-3");
    add("block.adv_rocketry.hydrogen", "Hydrogen");
    add("block.adv_rocketry.landing_pad", "Landing Pad");
    add("block.adv_rocketry.laser_light", "Laser Light");
    add("block.adv_rocketry.launch_pad", "Launch Pad");
    add("block.adv_rocketry.liquid_tank", "Liquid Tank");
    add("block.adv_rocketry.methane", "Methane");
    add("block.adv_rocketry.microwave_receiver", "Microwave Receiver");
    add("block.adv_rocketry.mining_drill", "Mining Drill");
    add("block.adv_rocketry.monitoring_station", "Monitoring Station");
    add("block.adv_rocketry.nitrogen", "Nitrogen");
    add("block.adv_rocketry.nuclear_fuel_tank", "Nuclear Fuel Tank");
    add("block.adv_rocketry.nuclear_rocket_motor", "Nuclear Rocket Motor");
    add("block.adv_rocketry.observatory", "Observatory");
    add("block.adv_rocketry.orbital_laser", "Orbital Laser");
    add("block.adv_rocketry.oxidizer_fuel_tank", "Oxidizer Fuel Tank");
    add("block.adv_rocketry.oxygen", "Oxygen");
    add("block.adv_rocketry.oxygen_charger", "Oxygen Charger");
    add("block.adv_rocketry.oxygen_vent", "Oxygen Vent");
    add("block.adv_rocketry.pipe_sealer", "Pipe Sealer");
    add("block.adv_rocketry.railgun", "Railgun");
    add("block.adv_rocketry.rocket_builder", "Rocket Builder");
    add("block.adv_rocketry.rocket_fire", "Rocket Fire");
    add("block.adv_rocketry.rocket_fluid_loader", "Rocket Fluid Loader");
    add("block.adv_rocketry.rocket_fluid_unloader", "Rocket Fluid Unloader");
    add("block.adv_rocketry.rocket_fuel", "Rocket Fuel");
    add("block.adv_rocketry.rocket_item_loader", "Rocket Item Loader");
    add("block.adv_rocketry.rocket_item_unloader", "Rocket Item Unloader");
    add("block.adv_rocketry.rocket_motor", "Rocket Motor");
    add("block.adv_rocketry.satellite_builder", "Satellite Builder");
    add("block.adv_rocketry.satellite_hatch", "Satellite Hatch");
    add("block.adv_rocketry.satellite_terminal", "Satellite Terminal");
    add("block.adv_rocketry.seat", "Seat");
    add("block.adv_rocketry.solar_array", "Solar Array");
    add("block.adv_rocketry.solar_array_panel", "Solar Array Panel");
    add("block.adv_rocketry.space_elevator", "Space Elevator");
    add("block.adv_rocketry.station_altitude_controller", "Station Altitude Controller");
    add("block.adv_rocketry.station_builder", "Station Builder");
    add("block.adv_rocketry.station_gravity_controller", "Station Gravity Controller");
    add("block.adv_rocketry.station_orientation_controller", "Station Orientation Controller");
    add("block.adv_rocketry.suit_workstation", "Suit Workstation");
    add("block.adv_rocketry.thermite_torch", "Thermite Torch");
    add("block.adv_rocketry.thermite_wall_torch", "Thermite Wall Torch");
    add("block.adv_rocketry.unlit_torch", "Extinguished Torch");
    add("block.adv_rocketry.unlit_wall_torch", "Unlit Wall Torch");
    add("block.adv_rocketry.warp_controller", "Warp Controller");
    add("block.adv_rocketry.warp_core", "Warp Core");
    add("block.adv_rocketry.wireless_transceiver", "Wireless Transceiver");
    add("block.adv_rocketry.energy_cable", "Energy Cable");
    add("block.adv_rocketry.fluid_pipe", "Fluid Pipe");
    add("block.adv_rocketry.item_conduit", "Item Conduit");
  }

  private void addFluidType() {
    add("fluid_type.adv_rocketry.oxygen", "Oxygen");
    add("fluid_type.adv_rocketry.hydrogen", "Hydrogen");
    add("fluid_type.adv_rocketry.nitrogen", "Nitrogen");
    add("fluid_type.adv_rocketry.helium", "Helium");
    add("fluid_type.adv_rocketry.helium3", "Helium3");
    add("fluid_type.adv_rocketry.ammonia", "Ammonia");
    add("fluid_type.adv_rocketry.methane", "Methane");
    add("fluid_type.adv_rocketry.rocket_fuel", "Rocket Fuel");
    add("fluid_type.adv_rocketry.enriched_lava", "Enriched Lava");
  }

  private void addKey() {
    add("key.adv_rocketry.toggle_jetpack", "Toggle Jetpack");
    add("key.categories.adv_rocketry", "Advanced Rocketry");
    add("key.adv_rocketry.rcs", "Toggle Rocket RCS");
    add("key.adv_rocketry.space_down", "Descend in Space Flight");
    add("key.adv_rocketry.hovercraft_up", "Ascend in Hovercraft");
    add("key.adv_rocketry.hovercraft_down", "Descend in Hovercraft");
  }

  private void addEnchantment() {
    add("enchantment.adv_rocketry.spacebreathing", "Airtight Seal");
  }

  private void addEntity() {
    add("entity.adv_rocketry.hologram_body", "Hologram");
  }

  private void addMessage() {
    add("message.adv_rocketry.atmosphere_readout", "Atmosphere: %s (%s atm)");
    add("message.adv_rocketry.breathable_readout", "Breathable: %s");
    add("message.adv_rocketry.yes", "yes");
    add("message.adv_rocketry.no", "no");
  }

  private void addHud() {
    add("hud.adv_rocketry.oxygen", "Oxygen");
    add("hud.adv_rocketry.oxygen_amount", "%s / %s mB");
    add("hud.adv_rocketry.suit", "Suit");
    add("hud.adv_rocketry.atmosphere", "Atmosphere");
    add("hud.adv_rocketry.pressure", "%s atm");
    add("hud.adv_rocketry.breathable", "Breathable");
    add("hud.adv_rocketry.unbreathable", "Not breathable");
    add("hud.adv_rocketry.atmosphere_warning", "Unsafe atmosphere: %s");
  }

  private void addAtmosphere() {
    add("atmosphere.adv_rocketry.air", "Air");
    add("atmosphere.adv_rocketry.pressurized_air", "Pressurized air");
    add("atmosphere.adv_rocketry.vacuum", "Vacuum");
    add("atmosphere.adv_rocketry.low_oxygen", "Low oxygen");
    add("atmosphere.adv_rocketry.high_pressure", "High pressure");
    add("atmosphere.adv_rocketry.super_high_pressure", "Super high pressure");
    add("atmosphere.adv_rocketry.very_hot", "Very hot");
    add("atmosphere.adv_rocketry.superheated", "Superheated");
    add("atmosphere.adv_rocketry.no_oxygen", "No oxygen");
    add("atmosphere.adv_rocketry.high_pressure_no_oxygen", "High pressure no oxygen");
    add("atmosphere.adv_rocketry.super_high_pressure_no_oxygen", "Super high pressure no oxygen");
    add("atmosphere.adv_rocketry.very_hot_no_oxygen", "Very hot no oxygen");
    add("atmosphere.adv_rocketry.superheated_no_oxygen", "Superheated no oxygen");
  }

  private void addAdvancement() {
    add("advancement.holographic", "Holographic");
    add("advancement.holographic.desc", "Craft a Holographic Projector");
    add("advancement.flattening", "Flattening");
    add("advancement.flattening.desc", "Craft a Plate Press");
    add("advancement.feelTheHeat", "Feel The Heat!");
    add("advancement.feelTheHeat.desc", "Craft an Electric Arc Furnace");
    add("advancement.electrifying", "Electrifying!");
    add("advancement.electrifying.desc", "Craft an Electrolyzer");
    add("advancement.spinDoctor", "Spin Doctor");
    add("advancement.spinDoctor.desc", "Craft a Lathe");
    add("advancement.rollin", "Rollin'");
    add("advancement.rollin.desc", "Craft a Rolling Machine");
    add("advancement.crystalline", "Crystalline");
    add("advancement.crystalline.desc", "Craft a Crystallizer");
    add("advancement.warp", "Warp");
    add("advancement.warp.desc", "Craft a Warp Core");
    add("advancement.moonLanding", "Moon Landing!");
    add("advancement.moonLanding.desc", "Land on the Moon");
    add("advancement.oneSmallStep", "One Small Step...");
    add("advancement.oneSmallStep.desc", "Be the first player to land on the Moon");
    add("advancement.weReallyWentToTheMoon", "We Really Went to the Moon!");
    add("advancement.weReallyWentToTheMoon.desc", "Find the Apollo 11 landing site on the Moon");
    add("advancement.dilithium", "Dilithium");
    add("advancement.dilithium.desc", "Find Dilithium ore");
    add("advancement.givingItAllShesGot", "Giving it all she's got!");
    add("advancement.givingItAllShesGot.desc", "Ride a space station through a warp");
    add("advancement.flightOfThePhoenix", "Flight of the Phoenix");
    add("advancement.flightOfThePhoenix.desc", "Be aboard the first station on the server to warp");
    add("advancement.beerOnTheSun", "Adult Beverages on the sun");
    add("advancement.beerOnTheSun.desc", "You'll need more TNT to get to orbit");
    add("advancement.suitedUp", "Suited Up");
    add("advancement.suitedUp.desc", "Wear a full spacesuit");
    add("advancement.advancedRocketry", "Advanced Rocketry");
    add("advancement.advancedRocketry.desc", "Reach for the stars");
    add("advancement.spinCycle", "Spin Cycle");
    add("advancement.spinCycle.desc", "Craft a Centrifuge");
    add("advancement.cutItOut", "Cut It Out");
    add("advancement.cutItOut.desc", "Craft a Cutting Machine");
    add("advancement.steadyHands", "Steady Hands");
    add("advancement.steadyHands.desc", "Craft a Precision Assembler");
    add("advancement.integrated", "Integrated");
    add("advancement.integrated.desc", "Make a Basic Circuit");
    add("advancement.etchASketch", "Etch a Sketch");
    add("advancement.etchASketch.desc", "Craft a Precision Laser Etcher");
    add("advancement.madScientist", "Mad Scientist");
    add("advancement.madScientist.desc", "Craft a Chemical Reactor");
    add("advancement.highOctane", "High Octane");
    add("advancement.highOctane.desc", "Get a bucket of Rocket Fuel");
    add("advancement.powerUp", "Power Up");
    add("advancement.powerUp.desc", "Craft a Coal Generator");
    add("advancement.tubular", "Tubular");
    add("advancement.tubular.desc", "Place an Energy Cable, Fluid Pipe or Item Conduit");
    add("advancement.rocketScience", "Rocket Science");
    add("advancement.rocketScience.desc", "Craft a Rocket Builder");
    add("advancement.liftoff", "Liftoff!");
    add("advancement.liftoff.desc", "Launch a rocket with you on board");
    add("advancement.twiceTheThrust", "Twice the Thrust");
    add("advancement.twiceTheThrust.desc", "Craft a Bipropellant Rocket Motor");
    add("advancement.atomicAge", "Atomic Age");
    add("advancement.atomicAge.desc", "Craft a Nuclear Rocket Motor");
    add("advancement.someAssemblyRequired", "Some Assembly Required");
    add("advancement.someAssemblyRequired.desc", "Craft a Station Builder");
    add("advancement.homeAwayFromHome", "Home Away From Home");
    add("advancement.homeAwayFromHome.desc", "Fly up to a space station");
    add("advancement.uncharted", "Uncharted");
    add("advancement.uncharted.desc", "Discover a new planet from a space station");
    add("advancement.laserFocus", "Laser Focus");
    add("advancement.laserFocus.desc", "Craft an Orbital Laser");
    add("advancement.specialDelivery", "Special Delivery");
    add("advancement.specialDelivery.desc", "Craft a Railgun");
    add("advancement.goingUp", "Going Up");
    add("advancement.goingUp.desc", "Ride a Space Elevator");
    add("advancement.eventHorizon", "Event Horizon");
    add("advancement.eventHorizon.desc", "Craft a Black Hole Generator");
    add("advancement.whatGoesUp", "What Goes Up...");
    add("advancement.whatGoesUp.desc", "Craft an Area Gravity Controller");
    add("advancement.freshAir", "Fresh Air");
    add("advancement.freshAir.desc", "Place an Oxygen Vent");
    add("advancement.airtight", "Airtight");
    add(
        "advancement.airtight.desc",
        "Breathe easy in a sealed room where the air outside would kill you");
    add("advancement.rocketMan", "Rocket Man");
    add("advancement.rocketMan.desc", "Get a Jetpack");
    add("advancement.stargazer", "Stargazer");
    add("advancement.stargazer.desc", "Craft an Observatory");
    add("advancement.numberCruncher", "Number Cruncher");
    add("advancement.numberCruncher.desc", "Craft an Astrobody Data Processor");
    add("advancement.eyeInTheSky", "Eye in the Sky");
    add("advancement.eyeInTheSky.desc", "Craft a Satellite Builder");
    add("advancement.landscaping", "Landscaping");
    add("advancement.landscaping.desc", "Craft a Biome Changer Remote");
    add("advancement.climateControl", "Climate Control");
    add("advancement.climateControl.desc", "Craft an Atmosphere Terraformer");
  }

  private void addAdvRocketry() {
    add("adv_rocketry.guide_name", "Advanced Rocketry Field Guide");
    add("adv_rocketry.guide_tooltip", "Machines, life support, space travel and automation.");
  }

  private void addAsteroid() {
    add("asteroid.adv_rocketry.small", "Small Asteroid");
    add("asteroid.adv_rocketry.light", "Light Asteroid");
    add("asteroid.adv_rocketry.iridium", "Iridium Enriched Asteroid");
    add("asteroid.adv_rocketry.strange", "Strange Asteroid");
  }

  private void addStatusText() {
    add(
        "status.adv_rocketry.area_gravity.gravity_toward_within_blocks",
        "Gravity %s%% toward %s within %s blocks");
    add(
        "status.adv_rocketry.astrobody_processor.asteroid_research_complete",
        "Asteroid research complete");
    add("status.adv_rocketry.astrobody_processor.research_channels", "Research channels: %s");
    add(
        "status.adv_rocketry.black_hole_generator.black_hole_generator_structure_incomplete",
        "Black-hole generator structure incomplete");
    add("status.adv_rocketry.black_hole_generator.generating_fe_t", "Generating %s FE/t");
    add(
        "status.adv_rocketry.black_hole_generator.insert_fuel_into_an_item_input",
        "Insert fuel into an item input port");
    add(
        "status.adv_rocketry.black_hole_generator.requires_a_station_orbiting_a_black",
        "Requires a station orbiting a black hole");
    add(
        "status.adv_rocketry.deployable_assembly.ready_mb_propellant_and_mb_gas",
        "Ready: %s mB propellant and %s mB gas storage");
    add(
        "status.adv_rocketry.holographic_selector.choose_a_landable_planet",
        "Choose a landable planet");
    add("status.adv_rocketry.holographic_selector.selected", "Selected %s");
    add(
        "status.adv_rocketry.holographic_selector.selector_must_be_on_a_space",
        "Selector must be on a space station");
    add(
        "status.adv_rocketry.holographic_selector.showing_planet_and_moons",
        "Showing planet and moons");
    add("status.adv_rocketry.holographic_selector.showing_star", "Showing star %s");
    add("status.adv_rocketry.holographic_selector.showing_star_system", "Showing star system");
    add("status.adv_rocketry.holographic_selector.showing_stars", "Showing stars");
    add("status.adv_rocketry.holographic_selector.station_targeting", "Station targeting %s");
    add("status.adv_rocketry.observatory.asteroid_chip_programmed", "Asteroid chip programmed");
    add("status.adv_rocketry.observatory.found_asteroid_targets", "Found %s asteroid targets");
    add(
        "status.adv_rocketry.observatory.scan_needs_100_distance_data",
        "Scan needs 100 distance data");
    add("status.adv_rocketry.orbital.array_chunk_is_not_loaded", "Array chunk is not loaded");
    add(
        "status.adv_rocketry.orbital.array_lane_is_obstructed_at",
        "Array lane is obstructed at %s");
    add("status.adv_rocketry.orbital.array_needs_solar_panels", "Array needs solar panels");
    add("status.adv_rocketry.orbital.assembling_satellite", "Assembling satellite #%s");
    add("status.adv_rocketry.orbital.assembly_already_in_progress", "Assembly already in progress");
    add(
        "status.adv_rocketry.orbital.assembly_paused_energy_input_missing",
        "Assembly paused: energy input missing");
    add(
        "status.adv_rocketry.orbital.assembly_paused_needs_10_fe_tick",
        "Assembly paused: needs 10 FE/tick");
    add("status.adv_rocketry.orbital.beacon_disabled", "Beacon disabled");
    add("status.adv_rocketry.orbital.beacon_enabled", "Beacon enabled");
    add("status.adv_rocketry.orbital.builder_job_complete", "Builder job complete");
    add(
        "status.adv_rocketry.orbital.chip_copier_needs_an_energy_input",
        "Chip copier needs an energy input directly below");
    add(
        "status.adv_rocketry.orbital.controller_must_be_on_a_deployed",
        "Controller must be on a deployed station");
    add("status.adv_rocketry.orbital.copying_id_chip", "Copying ID chip");
    add(
        "status.adv_rocketry.orbital.data_unit_contains_a_different_data",
        "Data unit contains a different data type");
    add("status.adv_rocketry.orbital.downloaded_data", "Downloaded %s data");
    add(
        "status.adv_rocketry.orbital.insert_a_linked_satellite_chip_and",
        "Insert a linked satellite chip and data unit");
    add("status.adv_rocketry.orbital.invalid_satellite_module", "Invalid satellite module");
    add(
        "status.adv_rocketry.orbital.needs_two_energy_output_ports",
        "Needs two energy output ports");
    add("status.adv_rocketry.orbital.no_data_available", "No data available");
    add("status.adv_rocketry.orbital.ready", "Ready");
    add(
        "status.adv_rocketry.orbital.remove_leftover_loose_modules_before_assembly",
        "Remove leftover loose modules before assembly");
    add(
        "status.adv_rocketry.orbital.satellite_builder_needs_an_energy_input",
        "Satellite builder needs an energy input directly below");
    add("status.adv_rocketry.orbital.satellite_is_not_deployed", "Satellite is not deployed");
    add(
        "status.adv_rocketry.orbital.satellite_is_outside_this_planetary_system",
        "Satellite is outside this planetary system");
    add(
        "status.adv_rocketry.orbital.supply_a_chassis_controller_power_module",
        "Supply a chassis, controller, power module and blank ID chip");
    add(
        "status.adv_rocketry.orbital.supply_a_programmed_source_and_matching",
        "Supply a programmed source and matching blank chip");
    add("status.adv_rocketry.orbital.target_gravity", "Target gravity: %s%%");
    add("status.adv_rocketry.orbital.terminal_needs_fe", "Terminal needs FE");
    add(
        "status.adv_rocketry.orbital_laser.build_the_orbital_laser_multiblock",
        "Build the orbital laser multiblock");
    add(
        "status.adv_rocketry.orbital_laser.no_landable_planet_below_the_laser",
        "No landable planet below the laser");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_has_no_eligible_planet",
        "Orbital laser has no eligible planet below");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_is_disabled_above_this",
        "Orbital laser is disabled above this planet");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_must_be_on_a",
        "Orbital laser must be on a space station");
    add("status.adv_rocketry.orbital_laser.orbital_laser_needs_fe", "Orbital laser needs %s FE");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_output_jammed",
        "Orbital laser output jammed");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_running_above",
        "Orbital laser running above %s");
    add("status.adv_rocketry.orbital_laser.orbital_laser_stopped", "Orbital laser stopped");
    add(
        "status.adv_rocketry.orbital_laser.orbital_laser_structure_incomplete",
        "Orbital laser structure incomplete");
    add("status.adv_rocketry.orbital_laser.output_jam_cleared", "Output jam cleared");
    add(
        "status.adv_rocketry.orbital_laser.station_needs_a_landable_planet_below",
        "Station needs a landable planet below");
    add("status.adv_rocketry.orbital_laser.target", "Target %s, %s");
    add("status.adv_rocketry.orbital_laser.terrain_mining_selected", "Terrain mining selected");
    add("status.adv_rocketry.orbital_laser.terrain_shaft_complete", "Terrain shaft complete");
    add("status.adv_rocketry.orbital_laser.void_drilling_selected", "Void drilling selected");
    add("status.adv_rocketry.planet_discovery.all_planets_discovered", "All planets discovered");
    add("status.adv_rocketry.planet_discovery.discovered", "Discovered %s");
    add(
        "status.adv_rocketry.planet_discovery.discovery_already_running",
        "Discovery already running");
    add(
        "status.adv_rocketry.planet_discovery.discovery_requires_a_deployed_station",
        "Discovery requires a deployed station");
    add("status.adv_rocketry.planet_discovery.imported", "Imported %s");
    add(
        "status.adv_rocketry.planet_discovery.insert_a_blank_planet_id_chip",
        "Insert a blank planet ID chip");
    add(
        "status.adv_rocketry.planet_discovery.insert_a_programmed_planet_id_chip",
        "Insert a programmed planet ID chip");
    add(
        "status.adv_rocketry.planet_discovery.need_100_distance_mass_and_composition",
        "Need 100 distance, mass and composition data");
    add("status.adv_rocketry.planet_discovery.no_planet_found", "No planet found");
    add("status.adv_rocketry.planet_discovery.searching_for_planets", "Searching for planets");
    add("status.adv_rocketry.railgun.minimum_redstone", "Minimum %s, redstone %s");
    add("status.adv_rocketry.railgun.transferred_items", "Transferred %s items");
    add("status.adv_rocketry.rocket.assembling_rocket", "Assembling rocket");
    add(
        "status.adv_rocketry.rocket.assembly_requires_100_fe_per_tick",
        "Assembly requires 100 FE per tick");
    add("status.adv_rocketry.rocket.awaiting_scan", "Awaiting scan");
    add("status.adv_rocketry.rocket_assembly.assembled", "%s assembled");
    add(
        "status.adv_rocketry.rocket_assembly.no_ready_rocket_linked_to_this",
        "No ready rocket linked to this builder");
    add(
        "status.adv_rocketry.rocket_assembly.ready_at_least_mb_required_for",
        "Ready: at least %s mB required for orbit");
    add("status.adv_rocketry.rocket_assembly.rocket_launched", "Rocket launched");
    add(
        "status.adv_rocketry.space_elevator.a_capsule_is_already_at_this",
        "A capsule is already at this anchor");
    add("status.adv_rocketry.space_elevator.capsule_launched", "Capsule launched");
    add(
        "status.adv_rocketry.space_elevator.passenger_could_not_board_the_capsule",
        "Passenger could not board the capsule");
    add(
        "status.adv_rocketry.space_elevator.space_elevator_link_or_frame_is",
        "Space elevator link or frame is incomplete");
    add(
        "status.adv_rocketry.space_elevator.space_elevator_requires_50_000_fe",
        "Space elevator requires 50,000 FE");
    add("status.adv_rocketry.station_assembly.station_packed", "Station #%s packed");
    add(
        "status.adv_rocketry.station_motion.altitude_controller_requires_a_station",
        "Altitude controller requires a station");
    add(
        "status.adv_rocketry.station_motion.orientation_controller_requires_a_station",
        "Orientation controller requires a station");
    add(
        "status.adv_rocketry.station_motion.station_rotation_target_reset",
        "Station rotation target reset");
    add("status.adv_rocketry.station_motion.target_altitude_km", "Target altitude: %s km");
    add("status.adv_rocketry.station_motion.target_rotation_hour", "Target %s rotation: %s/hour");
    add("status.adv_rocketry.terraformer.atmospheric_pressure_100", "Atmospheric pressure: %s/100");
    add(
        "status.adv_rocketry.terraformer.atmospheric_pressure_at_limit",
        "Atmospheric pressure at limit");
    add(
        "status.adv_rocketry.terraformer.decreasing_atmospheric_pressure",
        "Decreasing atmospheric pressure");
    add(
        "status.adv_rocketry.terraformer.increasing_atmospheric_pressure",
        "Increasing atmospheric pressure");
    add(
        "status.adv_rocketry.terraformer.need_a_linked_biome_changer_in",
        "Need a linked biome changer in this orbit");
    add(
        "status.adv_rocketry.terraformer.terraformer_must_be_on_a_planet",
        "Terraformer must be on a planet");
    add(
        "status.adv_rocketry.terraformer.terraformer_needs_1_000_fe_tick",
        "Terraformer needs 1,000 FE/tick");
    add(
        "status.adv_rocketry.terraformer.terraformer_needs_nitrogen_and_oxygen",
        "Terraformer needs nitrogen and oxygen");
    add(
        "status.adv_rocketry.terraformer.terraformer_structure_incomplete",
        "Terraformer structure incomplete");
    add("status.adv_rocketry.terraformer.terraforming", "Terraforming %s/%s");
    add("status.adv_rocketry.terraformer.terraforming_enabled", "Terraforming enabled");
    add("status.adv_rocketry.terraformer.terraforming_paused", "Terraforming paused");
    add(
        "status.adv_rocketry.transport.conflicting_fluids_clear_buffers_or_locks",
        "Conflicting fluids: clear buffers or locks");
    add("status.adv_rocketry.transport.idle", "Idle");
    add(
        "status.adv_rocketry.transport.too_large_split_at_4_096",
        "Too large: split at 4,096 sections");
    add("status.adv_rocketry.transport.transferring", "Transferring");
    add(
        "status.adv_rocketry.warp.choose_another_landable_destination_planet",
        "Choose another landable destination planet");
    add(
        "status.adv_rocketry.warp.destination_planet_has_not_been_discovered",
        "Destination planet has not been discovered");
    add(
        "status.adv_rocketry.warp.disconnect_the_space_elevator_before_warping",
        "Disconnect the space elevator before warping");
    add(
        "status.adv_rocketry.warp.station_is_already_in_warp_transit",
        "Station is already in warp transit");
    add("status.adv_rocketry.warp.station_needs_warp_fuel", "Station needs %s warp fuel");
    add(
        "status.adv_rocketry.warp.warp_controller_is_missing_required_artifacts",
        "Warp controller is missing required artifacts");
    add(
        "status.adv_rocketry.warp.warp_controller_must_be_on_a",
        "Warp controller must be on a space station");
    add(
        "status.adv_rocketry.warp.warp_controller_needs_1_000_fe",
        "Warp controller needs 1,000 FE");
    add("status.adv_rocketry.warp.warp_core_frame_is_incomplete", "Warp core frame is incomplete");
    add("status.adv_rocketry.warp.warping_to_fuel", "Warping to %s (%s fuel)");
  }

  private void addMessageText() {
    add(
        "message.adv_rocketry.asteroid_chip.unprogrammed_asteroid_chip",
        "Unprogrammed asteroid chip");
    add("message.adv_rocketry.atmosphere_detector_block.detecting", "Detecting %s");
    add("message.adv_rocketry.beacon_finder.beacon_distance", "Beacon %s blocks %s");
    add("message.adv_rocketry.beacon_finder.east", "east");
    add("message.adv_rocketry.beacon_finder.no_active_beacon", "No active beacon on this planet");
    add("message.adv_rocketry.beacon_finder.north", "north");
    add("message.adv_rocketry.beacon_finder.south", "south");
    add("message.adv_rocketry.beacon_finder.west", "west");
    add("message.adv_rocketry.biome_changer.already_working", "Biome changer is already working");
    add(
        "message.adv_rocketry.biome_changer.needs_satellite_storage",
        "Biome changer needs at least 1,920 FE of satellite storage");
    add(
        "message.adv_rocketry.biome_changer.no_biome_changer_in_orbit",
        "No biome changer in this orbit");
    add("message.adv_rocketry.biome_changer.queued", "Biome change queued over 32×32 blocks");
    add(
        "message.adv_rocketry.biome_changer.sneak_use_to_select",
        "Sneak-use the remote on a biome to select it");
    add("message.adv_rocketry.biome_changer_remote.biome", "Biome: %s");
    add("message.adv_rocketry.biome_changer_remote.satellite", "Satellite #%s");
    add("message.adv_rocketry.biome_changer_remote.selected_biome", "Selected biome %s");
    add(
        "message.adv_rocketry.biome_changer_remote.sneak_use_to_sample_use_to",
        "Sneak-use to sample; use to change nearby terrain");
    add("message.adv_rocketry.data_unit.empty_0", "Empty: 0 / %s");
    add(
        "message.adv_rocketry.deployable_assembly.deployable_assembler_must_face_horizontally",
        "Deployable assembler must face horizontally");
    add(
        "message.adv_rocketry.deployable_assembly.deployable_gas_rockets_cannot_carry_passengers",
        "Deployable gas rockets cannot carry passengers");
    add(
        "message.adv_rocketry.deployable_assembly.deployable_rocket_needs_working_engines_and",
        "Deployable rocket needs working engines and fuel tanks");
    add(
        "message.adv_rocketry.deployable_assembly.deployable_rockets_require_a_gas_giant",
        "Deployable rockets require a gas-giant orbit station");
    add(
        "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_base_rail",
        "Deployment frame needs a base rail at least three blocks wide");
    add(
        "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_top_rail",
        "Deployment frame needs a top rail at least three blocks long");
    add(
        "message.adv_rocketry.deployable_assembly.deployment_frame_needs_a_tower_at",
        "Deployment frame needs a tower at least three blocks high");
    add(
        "message.adv_rocketry.deployable_assembly.engines_face_outward",
        "Deployable rocket engines must face outward");
    add(
        "message.adv_rocketry.deployable_assembly.gas_rocket_needs_an_intake_and",
        "Gas rocket needs an intake and fluid storage");
    add(
        "message.adv_rocketry.holo_projector.sneak_use_machines_and_materials_use",
        "Sneak + use: machines and materials; use on block: preview; sneak + scroll: select layer");
    add("message.adv_rocketry.jetpack.hover", "Jetpack hover %s");
    add("message.adv_rocketry.jetpack.off", "off");
    add("message.adv_rocketry.jetpack.on", "on");
    add("message.adv_rocketry.jetpack.thrust", "Jetpack %s");
    add("message.adv_rocketry.landing_pad.unnamed", "Unnamed");
    add("message.adv_rocketry.landing_pad_block.landing_pad", "Landing pad: %s");
    add(
        "message.adv_rocketry.linker.complete_both_frames_station_must_be",
        "Complete both frames; station must be level, stopped, and at 35,500-36,300 km");
    add(
        "message.adv_rocketry.linker.infrastructure_link_updated_select_a_rocket",
        "Infrastructure link updated; select a rocket or its assembler");
    add(
        "message.adv_rocketry.linker.infrastructure_linked_to_assembler",
        "Infrastructure linked to assembler");
    add("message.adv_rocketry.linker.link_position_cleared", "Link position cleared");
    add("message.adv_rocketry.linker.rocket_monitor_linked", "Rocket monitor linked");
    add("message.adv_rocketry.linker.selected", "Selected %s");
    add("message.adv_rocketry.linker.selected_2", "Selected: %s");
    add("message.adv_rocketry.linker.space_elevator_unlinked", "Space elevator unlinked");
    add("message.adv_rocketry.linker.space_elevators_linked", "Space elevators linked");
    add("message.adv_rocketry.linker.wireless_data_network_linked", "Wireless data network linked");
    add("message.adv_rocketry.machine_ports.advanced_rocketry", "Advanced Rocketry");
    add("message.adv_rocketry.ore_scanner.linked_to_ore_mapper", "Linked to ore mapper #%s");
    add(
        "message.adv_rocketry.ore_scanner.no_ore_mapper_in_this_orbit",
        "No ore mapper in this orbit");
    add("message.adv_rocketry.ore_scanner.orbital_ore_map", "Orbital Ore Map");
    add("message.adv_rocketry.ore_scanner.ore_mapper_needs_1_000_fe", "Ore mapper needs 1,000 FE");
    add("message.adv_rocketry.ore_scanner.unprogrammed", "Unprogrammed");
    add("message.adv_rocketry.planet_id_chip.unprogrammed_planet_chip", "Unprogrammed planet chip");
    add("message.adv_rocketry.pressure_tank.mb", "%s: %s / %s mB");
    add("message.adv_rocketry.processing_block.incomplete", "Incomplete %s");
    add(
        "message.adv_rocketry.rocket.choose_a_landable_planet_in_the",
        "Choose a landable planet in the guidance computer");
    add("message.adv_rocketry.rocket.collecting", "Collecting %s");
    add(
        "message.adv_rocketry.rocket.deployable_rockets_require_a_gas_giant",
        "Deployable rockets require a gas-giant orbit station");
    add(
        "message.adv_rocketry.rocket.gas_mission_needs_an_intake_fluid",
        "Gas mission needs an intake, fluid storage and gas-giant orbit");
    add(
        "message.adv_rocketry.rocket.guidance_target_is_not_available",
        "Guidance target is not available");
    add(
        "message.adv_rocketry.rocket.insufficient_propellant_for_orbit_requires_mb",
        "Insufficient propellant for orbit: requires %s mB");
    add(
        "message.adv_rocketry.rocket.interstellar_travel_requires_a_warp_capable",
        "Interstellar travel requires a warp-capable station");
    add(
        "message.adv_rocketry.rocket.no_space_station_orbits_this_planet",
        "No space station orbits this planet");
    add("message.adv_rocketry.rocket.rocket_infrastructure_linked", "Rocket infrastructure linked");
    add(
        "message.adv_rocketry.rocket.rocket_must_be_landed_and_within",
        "Rocket must be landed and within infrastructure link range");
    add(
        "message.adv_rocketry.rocket.rocket_must_launch_from_a_space",
        "Rocket must launch from a space station");
    add("message.adv_rocketry.rocket.selected_rocket", "Selected rocket %s");
    add(
        "message.adv_rocketry.rocket.the_asteroid_chip_names_an_unknown",
        "The asteroid chip names an unknown asteroid");
    add(
        "message.adv_rocketry.rocket.the_rocket_cannot_be_disassembled_here",
        "The rocket cannot be disassembled here");
    add(
        "message.adv_rocketry.rocket.the_rocket_needs_an_available_seat",
        "The rocket needs an available seat");
    add(
        "message.adv_rocketry.rocket.the_rocket_needs_clear_space_before",
        "The rocket needs clear space before disassembly");
    add(
        "message.adv_rocketry.rocket.the_rocket_tanks_cannot_hold_its",
        "The rocket tanks cannot hold its fuel");
    add(
        "message.adv_rocketry.rocket.this_flight_requires_a_trans_body",
        "This flight requires a trans-body injection upgrade");
    add(
        "message.adv_rocketry.rocket_assembly.a_guidance_computer_is_required",
        "A guidance computer is required");
    add(
        "message.adv_rocketry.rocket_assembly.a_structure_tower_at_least_four",
        "A structure tower at least four blocks high must border the pad");
    add("message.adv_rocketry.rocket_assembly.assembly_was_cancelled", "%s assembly was cancelled");
    add(
        "message.adv_rocketry.rocket_assembly.do_not_mix_propellant_tank_types",
        "Do not mix propellant tank types");
    add("message.adv_rocketry.rocket_assembly.engines_face_down", "Rocket engines must face down");
    add(
        "message.adv_rocketry.rocket_assembly.insufficient_tank_capacity_for_orbit_requires",
        "Insufficient tank capacity for orbit: requires %s fuel points");
    add(
        "message.adv_rocketry.rocket_assembly.launch_pad_exceeds_16_by_16",
        "Launch pad exceeds 16 by 16 blocks");
    add(
        "message.adv_rocketry.rocket_assembly.launch_pad_must_be_a_solid",
        "Launch pad must be a solid rectangle no larger than 16 by 16");
    add("message.adv_rocketry.rocket_assembly.name_deployable", "Deployable rocket");
    add("message.adv_rocketry.rocket_assembly.name_rocket", "Rocket");
    add("message.adv_rocketry.rocket_assembly.noun_deployable", "deployable rocket");
    add("message.adv_rocketry.rocket_assembly.noun_rocket", "rocket");
    add(
        "message.adv_rocketry.rocket_assembly.place_the_builder_beside_a_launch",
        "Place the builder beside a launch pad, one block above it");
    add(
        "message.adv_rocketry.rocket_assembly.the_contains_a_blacklisted_block",
        "The %s contains a blacklisted block");
    add(
        "message.adv_rocketry.rocket_assembly.the_contains_an_unmovable_block",
        "The %s contains an unmovable block");
    add(
        "message.adv_rocketry.rocket_assembly.the_extends_into_unloaded_chunks",
        "The %s extends into unloaded chunks");
    add(
        "message.adv_rocketry.rocket_assembly.thrust_must_exceed_rocket_mass_under",
        "Thrust must exceed rocket mass under local gravity");
    add(
        "message.adv_rocketry.rocket_assembly.use_one_engine_and_fuel_type",
        "Use one %s engine and fuel type");
    add("message.adv_rocketry.rocket_console.no_onboard_inventory", "No onboard inventory");
    add("message.adv_rocketry.rocket_console.rocket_controls", "Rocket Controls");
    add("message.adv_rocketry.rocket_part_block.engine_facing", "Engine facing %s");
    add(
        "message.adv_rocketry.rocket_structure.do_not_mix_oxidizers_in_rocket",
        "Do not mix oxidizers in rocket tanks");
    add(
        "message.adv_rocketry.rocket_structure.do_not_mix_propellants_in_rocket",
        "Do not mix propellants in rocket tanks");
    add(
        "message.adv_rocketry.rocket_structure.rocket_contains_an_invalid_block_palette",
        "Rocket contains an invalid block palette index");
    add(
        "message.adv_rocketry.rocket_structure.rocket_tank_contains_an_unconfigured_fuel",
        "Rocket tank contains an unconfigured fuel");
    add("message.adv_rocketry.satellite.power_fe_t_fe_storage", "Power: %s FE/t, %s FE storage");
    add("message.adv_rocketry.satellite.satellite", "Satellite #%s");
    add("message.adv_rocketry.satellite.type", "Type: %s");
    add("message.adv_rocketry.satellite_chip.no_satellite_programmed", "No satellite programmed");
    add(
        "message.adv_rocketry.satellite_chip.satellite_awaiting_launch",
        "Satellite #%s awaiting launch");
    add(
        "message.adv_rocketry.satellite_chip.satellite_orbiting_fe_data",
        "Satellite #%s (%s) orbiting %s; %s FE, %s data");
    add("message.adv_rocketry.sealed", "This block will hold a seal");
    add(
        "message.adv_rocketry.showcase.showcase_built_blocks_multiblocks_items",
        "Showcase built: %s blocks, %s multiblocks, %s items%s");
    add("message.adv_rocketry.space_suit.oxygen_mb", "Oxygen: %s mB");
    add(
        "message.adv_rocketry.space_suit.sneak_and_use_to_configure_suit",
        "Sneak and use to configure suit modules");
    add("message.adv_rocketry.station.station", "Station #%s");
    add(
        "message.adv_rocketry.station_assembly.a_structure_tower_at_least_four",
        "A structure tower at least four blocks high must border the pad");
    add(
        "message.adv_rocketry.station_assembly.launch_pad_exceeds_16_by_16",
        "Launch pad exceeds 16 by 16 blocks");
    add(
        "message.adv_rocketry.station_assembly.launch_pad_must_be_a_solid",
        "Launch pad must be a solid 16 by 16 or smaller rectangle");
    add(
        "message.adv_rocketry.station_assembly.place_the_station_builder_beside_a",
        "Place the station builder beside a launch pad");
    add(
        "message.adv_rocketry.station_assembly.station_contains_an_unmovable_block",
        "Station contains an unmovable block");
    add(
        "message.adv_rocketry.station_assembly.station_extends_into_unloaded_chunks",
        "Station extends into unloaded chunks");
    add(
        "message.adv_rocketry.station_assembly.station_module_requires_a_satellite_hatch",
        "Station module requires a satellite hatch");
    add(
        "message.adv_rocketry.station_assembly.station_packing_is_not_allowed_at",
        "Station packing is not allowed at %s");
    add(
        "message.adv_rocketry.station_assembly.station_packing_requires_1_000_fe",
        "Station packing requires 1,000 FE");
    add(
        "message.adv_rocketry.station_assembly.supply_a_satellite_hatch_and_blank",
        "Supply a satellite hatch and blank station chip; clear both outputs");
    add(
        "message.adv_rocketry.station_chip.crouch_use_to_manage_return_destinations",
        "Crouch-use to manage return destinations");
    add("message.adv_rocketry.station_chip.guidance_set_to_station", "Guidance set to station #%s");
    add("message.adv_rocketry.station_chip.station", "Station #%s");
    add("message.adv_rocketry.station_chip.station_chip_destinations", "Station Chip Destinations");
    add(
        "message.adv_rocketry.station_chip.this_station_is_not_deployed_in",
        "This station is not deployed in the current orbit");
    add("message.adv_rocketry.station_chip.unprogrammed", "Unprogrammed");
    add("message.adv_rocketry.transport.fluid_lock", "Fluid lock: %s");
    add(
        "message.adv_rocketry.transport.network_too_large",
        "Network exceeds 4096 pipes; split it first");
    add(
        "message.adv_rocketry.transport.void_before_changing",
        "Void the existing pipe buffers before changing fluid type");
    add(
        "message.adv_rocketry.transport.voided",
        "Voided %s mB from loaded pipe buffers; fluid lock retained");
    add("message.adv_rocketry.unsealed", "This block will not hold a seal");
    add("message.adv_rocketry.wireless_transceiver_block.wireless_data", "Wireless data: %s");
  }

  private void addGuiText() {
    add("gui.adv_rocketry.cargo_port.auto_eject", "Auto-eject");
    add("gui.adv_rocketry.cargo_port.input_mode", "Input: %s");
    add("gui.adv_rocketry.cargo_port.off", "OFF");
    add("gui.adv_rocketry.cargo_port.on", "ON");
    add("gui.adv_rocketry.cargo_port.option", "%s: %s");
    add("gui.adv_rocketry.cargo_port.output_mode", "Output: %s");
    add("gui.adv_rocketry.cargo_port.planet", "Planet");
    add("gui.adv_rocketry.cargo_port.satellite", "Sat");
    add("gui.adv_rocketry.cargo_port.side_in", "in");
    add("gui.adv_rocketry.cargo_port.side_off", "off");
    add("gui.adv_rocketry.cargo_port.side_out", "out");
    add("gui.adv_rocketry.cargo_port.station", "Station");
    add("gui.adv_rocketry.coal_generator.fe", "%s FE");
    add("gui.adv_rocketry.coal_generator.fuel", "Fuel");
    add("gui.adv_rocketry.coal_generator.ticks", "%s ticks");
    add("gui.adv_rocketry.fluid.empty", "Empty");
    add("gui.adv_rocketry.fueling_station.1000_fe", "%s/1000 FE");
    add("gui.adv_rocketry.fueling_station.5000_mb", "%s/5000 mB");
    add("gui.adv_rocketry.fueling_station.output", "Output: %s");
    add(
        "gui.adv_rocketry.guide.airtight_seal",
        "Airtight Seal is added while preserving the armor's existing components.");
    add("gui.adv_rocketry.guide.input", "Input");
    add(
        "gui.adv_rocketry.guide.no_recipes",
        "No matching processing recipes are enabled on this server.");
    add("gui.adv_rocketry.guide.output", "Output");
    add(
        "gui.adv_rocketry.guide.processing_cost",
        "%s ticks; %s FE/t (base cost before machine upgrades).");
    add("gui.adv_rocketry.guide.random_output", "Random output: %s rolls per operation.");
    add("gui.adv_rocketry.machine.energy", "%s FE");
    add("gui.adv_rocketry.machine.fluid", "%s / %s mB");
    add("gui.adv_rocketry.machine.port.creative_energy_input", "Creative power");
    add("gui.adv_rocketry.machine.port.data_bus", "Data units");
    add("gui.adv_rocketry.machine.port.energy_input", "Energy input");
    add("gui.adv_rocketry.machine.port.energy_output", "Energy output");
    add("gui.adv_rocketry.machine.port.fluid_input", "Fluid input");
    add("gui.adv_rocketry.machine.port.fluid_output", "Fluid output");
    add("gui.adv_rocketry.machine.port.item_input", "Item input");
    add("gui.adv_rocketry.machine.port.item_output", "Item output");
    add("gui.adv_rocketry.machine.port.solar", "Solar power");
    add("gui.adv_rocketry.orbital.altitude_km", "Altitude: %s km");
    add("gui.adv_rocketry.orbital.area_gravity", "Area gravity: %s%%");
    add("gui.adv_rocketry.orbital.assembly", "Assembly %s%%");
    add("gui.adv_rocketry.orbital.assembly_ready", "Assembly ready");
    add("gui.adv_rocketry.orbital.asteroid_chip", "Asteroid chip");
    add("gui.adv_rocketry.orbital.beacon_disabled", "Disabled");
    add("gui.adv_rocketry.orbital.beacon_enabled", "Enabled");
    add("gui.adv_rocketry.orbital.beacon_state", "%s / %s");
    add("gui.adv_rocketry.orbital.biome_changer_remote", "Biome changer remote");
    add("gui.adv_rocketry.orbital.biomes_below", "Biomes below %s");
    add("gui.adv_rocketry.orbital.black_hole_generator", "Black-hole generator");
    add("gui.adv_rocketry.orbital.build", "Build");
    add("gui.adv_rocketry.orbital.build_the_warp_core_frame", "Build the warp core frame");
    add("gui.adv_rocketry.orbital.channel_composition", "Composition");
    add("gui.adv_rocketry.orbital.channel_distance", "Distance");
    add("gui.adv_rocketry.orbital.channel_mass", "Mass");
    add("gui.adv_rocketry.orbital.channels", "Channels %s");
    add("gui.adv_rocketry.orbital.chip_dist_comp_mass_out", "Chip    Dist.   Comp.   Mass   Out");
    add("gui.adv_rocketry.orbital.clear_jam", "Clear jam");
    add("gui.adv_rocketry.orbital.composition", "Composition: %s");
    add("gui.adv_rocketry.orbital.controller_modules", "Controller  /  Modules");
    add("gui.adv_rocketry.orbital.copy_chip", "Copy chip");
    add("gui.adv_rocketry.orbital.current", "Current: %s%%");
    add("gui.adv_rocketry.orbital.cycle", "Cycle: %s%%");
    add("gui.adv_rocketry.orbital.decrease", "Decrease");
    add("gui.adv_rocketry.orbital.direction", "Direction");
    add("gui.adv_rocketry.orbital.discover", "Discover");
    add("gui.adv_rocketry.orbital.download", "Download");
    add("gui.adv_rocketry.orbital.energy_fe", "Energy: %s FE");
    add("gui.adv_rocketry.orbital.energy_fe_2", "%s / Energy %s FE / %s");
    add("gui.adv_rocketry.orbital.energy_fe_3", "Energy %s FE");
    add("gui.adv_rocketry.orbital.estimated_cargo", "Estimated cargo");
    add("gui.adv_rocketry.orbital.fe_scan_1000", "FE %s  Scan %s/1000");
    add("gui.adv_rocketry.orbital.frame_incomplete", "Frame incomplete");
    add("gui.adv_rocketry.orbital.frame_ready", "Frame ready");
    add("gui.adv_rocketry.orbital.geostationary_tether", "Geostationary tether");
    add("gui.adv_rocketry.orbital.id_chip_data_unit", "ID Chip  /  Data Unit");
    add("gui.adv_rocketry.orbital.import_chip", "Import chip");
    add("gui.adv_rocketry.orbital.increase", "Increase");
    add("gui.adv_rocketry.orbital.laser_idle", "Idle");
    add("gui.adv_rocketry.orbital.laser_jammed", "Jammed");
    add("gui.adv_rocketry.orbital.laser_running", "Running");
    add("gui.adv_rocketry.orbital.laser_terrain", "Terrain");
    add("gui.adv_rocketry.orbital.laser_void", "Void");
    add("gui.adv_rocketry.orbital.linker", "Linker");
    add("gui.adv_rocketry.orbital.mass", "Mass: %s");
    add("gui.adv_rocketry.orbital.microwave_satellite_chip", "Microwave satellite chip");
    add("gui.adv_rocketry.orbital.min", "Min -");
    add("gui.adv_rocketry.orbital.min_2", "Min +");
    add("gui.adv_rocketry.orbital.min_signal_fe", "Min %s / Signal %s / %s FE");
    add("gui.adv_rocketry.orbital.mission_time_x", "Mission time: %sx");
    add("gui.adv_rocketry.orbital.my_id", "My ID");
    add("gui.adv_rocketry.orbital.next", "Next");
    add("gui.adv_rocketry.orbital.next_target", "Next target");
    add("gui.adv_rocketry.orbital.no_biome_catalog_entries", "No biome catalog entries");
    add("gui.adv_rocketry.orbital.none", "None");
    add("gui.adv_rocketry.orbital.observe", "Observe");
    add("gui.adv_rocketry.orbital.pause_resume", "Pause / resume");
    add("gui.adv_rocketry.orbital.previous", "Previous");
    add("gui.adv_rocketry.orbital.program_chip", "Program chip");
    add("gui.adv_rocketry.orbital.radius", "Radius %s / %s");
    add("gui.adv_rocketry.orbital.radius_2", "Radius");
    add("gui.adv_rocketry.orbital.required_artifacts", "Required artifacts");
    add("gui.adv_rocketry.orbital.reset", "Reset");
    add("gui.adv_rocketry.orbital.rotation_per_hour", "Rotation per hour");
    add("gui.adv_rocketry.orbital.satellite_payload", "Satellite payload");
    add("gui.adv_rocketry.orbital.save_ids", "Save IDs");
    add("gui.adv_rocketry.orbital.scan_100_data_chip_500_fe", "Scan: 100 data / Chip: 500 FE");
    add(
        "gui.adv_rocketry.orbital.scanner_frame_or_station_unavailable",
        "Scanner frame or station unavailable");
    add("gui.adv_rocketry.orbital.scroll_for_more_cargo", "Scroll for more cargo");
    add("gui.adv_rocketry.orbital.select_a_target", "Select a target");
    add("gui.adv_rocketry.orbital.selected", "Selected: %s");
    add("gui.adv_rocketry.orbital.selector.back", "Back");
    add("gui.adv_rocketry.orbital.selector.center", "Center");
    add("gui.adv_rocketry.orbital.selector.next", "Next");
    add("gui.adv_rocketry.orbital.selector.redstone", "Redstone");
    add("gui.adv_rocketry.orbital.selector.size_down", "Size -");
    add("gui.adv_rocketry.orbital.selector.size_up", "Size +");
    add("gui.adv_rocketry.orbital.selector.star", "Star");
    add("gui.adv_rocketry.orbital.selector.target", "Target");
    add("gui.adv_rocketry.orbital.signal", "Signal");
    add("gui.adv_rocketry.orbital.solar_array", "Solar array");
    add("gui.adv_rocketry.orbital.star_scale_signal", "Star %s  Scale %s  Signal %s");
    add("gui.adv_rocketry.orbital.start_stop", "Start / Stop");
    add("gui.adv_rocketry.orbital.station_in_warp_transit", "Station in warp transit");
    add("gui.adv_rocketry.orbital.station_warp_fuel", "Station warp fuel: %s/%s");
    add("gui.adv_rocketry.orbital.target", "Target: %s");
    add("gui.adv_rocketry.orbital.target_2", "Target: %s%%");
    add("gui.adv_rocketry.orbital.target_3", "Target %s/%s");
    add("gui.adv_rocketry.orbital.target_4", "Target: %s, %s");
    add("gui.adv_rocketry.orbital.target_5", "Target");
    add("gui.adv_rocketry.orbital.target_id", "Target ID");
    add("gui.adv_rocketry.orbital.target_km", "Target: %s km");
    add("gui.adv_rocketry.orbital.toggle_beacon", "Toggle beacon");
    add("gui.adv_rocketry.orbital.travel", "Travel");
    add("gui.adv_rocketry.orbital.void_terrain", "Void / Terrain");
    add("gui.adv_rocketry.orbital.warp", "Warp");
    add("gui.adv_rocketry.orbital.warp_core_formed", "Warp core formed");
    add("gui.adv_rocketry.ore_scan.nearby_ore_density", "Nearby ore density");
    add("gui.adv_rocketry.oxygen.energy_fe", "Energy: %s / %s FE");
    add("gui.adv_rocketry.oxygen.inactive", "Inactive");
    add("gui.adv_rocketry.oxygen.mb", "%s: %s / %s mB");
    add("gui.adv_rocketry.oxygen.room_volume", "Room volume: %s");
    add("gui.adv_rocketry.oxygen.stand_here_to_refill_your_suit", "Stand here to refill your suit");
    add("gui.adv_rocketry.oxygen.supplying_breathable_air", "Supplying breathable air");
    add("gui.adv_rocketry.processing.base_time", "Base time before motor/coil bonuses");
    add("gui.adv_rocketry.processing.chance_per_item", "%s%% chance per item");
    add("gui.adv_rocketry.processing.energy", "%s FE/t");
    add("gui.adv_rocketry.processing.gravity_limit", "Gravity < %sg");
    add("gui.adv_rocketry.processing.inputs", "Inputs");
    add("gui.adv_rocketry.processing.not_consumed", "Not consumed");
    add("gui.adv_rocketry.processing.outputs", "Outputs");
    add("gui.adv_rocketry.processing.per_roll", "%s%% per roll");
    add("gui.adv_rocketry.processing.redstone_press", "Redstone press; no power");
    add("gui.adv_rocketry.processing.ticks", "%s ticks");
    add(
        "gui.adv_rocketry.processing_category.preserves_the_armor_s_components",
        "Preserves the armor's components");
    add("gui.adv_rocketry.processing_category.rolls", "%s; %s rolls");
    add("gui.adv_rocketry.projector.done", "Done");
    add("gui.adv_rocketry.projector.holographic_projector", "Holographic Projector");
    add("gui.adv_rocketry.projector.material", "%s × %s");
    add("gui.adv_rocketry.projector.material_optional", "Up to %s × %s");
    add("gui.adv_rocketry.projector.materials", "Materials (scroll)");
    add("gui.adv_rocketry.projector.or", " or ");
    add("gui.adv_rocketry.redstone_mode.inverted", "Inverted");
    add("gui.adv_rocketry.redstone_mode.off", "Off");
    add("gui.adv_rocketry.redstone_mode.on", "On");
    add("gui.adv_rocketry.rocket.assemble", "Assemble");
    add("gui.adv_rocketry.rocket.assembling", "Assembling: %s/%s");
    add("gui.adv_rocketry.rocket.asteroid_or_station_chip", "Asteroid or station chip");
    add("gui.adv_rocketry.rocket.automatic_pad", "Automatic pad");
    add(
        "gui.adv_rocketry.rocket.board_the_rocket_and_press_jump",
        "Board the rocket and press jump to launch");
    add("gui.adv_rocketry.rocket.destination", "Destination: %s");
    add("gui.adv_rocketry.rocket.destination_short", "To: %s");
    add("gui.adv_rocketry.rocket.disassemble", "Disassemble");
    add("gui.adv_rocketry.rocket.energy_fe", "Energy: %s FE");
    add(
        "gui.adv_rocketry.rocket.fill_with_the_matching_propellant_bucket",
        "Fill with the matching propellant bucket or pipe");
    add(
        "gui.adv_rocketry.rocket.hatch_chip_module_chip",
        "Hatch   Chip                 Module  Chip");
    add("gui.adv_rocketry.rocket.launch", "Launch");
    add("gui.adv_rocketry.rocket.mass_thrust", "Mass: %s   Thrust: %s");
    add("gui.adv_rocketry.rocket.next_destination", "Next destination");
    add("gui.adv_rocketry.rocket.pack_station", "Pack station module");
    add("gui.adv_rocketry.rocket.scan", "Scan");
    add("gui.adv_rocketry.rocket.tank_capacity_mb", "Tank capacity: %s mB");
    add("gui.adv_rocketry.rocket_cargo_port.fluid_16000_mb", "Fluid: %s/16000 mB");
    add("gui.adv_rocketry.rocket_cargo_port.output", "Output: %s");
    add("gui.adv_rocketry.rocket_console.destination_set_by_chip", "Destination set by chip");
    add("gui.adv_rocketry.rocket_console.fuel", "Fuel: %s / %s");
    add("gui.adv_rocketry.rocket_console.next_destination", "Next destination");
    add("gui.adv_rocketry.rocket_hud.asteroid_controls", "%s to disable | %s/%s up/down");
    add("gui.adv_rocketry.rocket_hud.asteroid_rcs", "Asteroid RCS");
    add(
        "gui.adv_rocketry.rocket_hud.controls",
        "Steer with movement keys | %s/%s up/down | %s RCS");
    add("gui.adv_rocketry.rocket_hud.off", "off");
    add("gui.adv_rocketry.rocket_hud.on", "on");
    add("gui.adv_rocketry.rocket_hud.orbit", "Orbit: %s");
    add("gui.adv_rocketry.rocket_hud.region", "%s | RCS %s");
    add("gui.adv_rocketry.rocket_hud.solar_navigation", "Solar navigation");
    add("gui.adv_rocketry.rocket_hud.speed", "Speed %s | Position %s, %s, %s");
    add("gui.adv_rocketry.rocket_monitoring.ascending", "Ascending");
    add("gui.adv_rocketry.rocket_monitoring.asteroid_mining", "Asteroid mining");
    add("gui.adv_rocketry.rocket_monitoring.fuel_mb", "Fuel: %s/%s mB");
    add("gui.adv_rocketry.rocket_monitoring.gas_collection", "Gas collection");
    add("gui.adv_rocketry.rocket_monitoring.height_m", "Height: %s m");
    add("gui.adv_rocketry.rocket_monitoring.in_space", "In space");
    add("gui.adv_rocketry.rocket_monitoring.inbound", "Inbound");
    add("gui.adv_rocketry.rocket_monitoring.launch", "Launch");
    add("gui.adv_rocketry.rocket_monitoring.launch_input", "Launch input: %s");
    add("gui.adv_rocketry.rocket_monitoring.mission_in_progress", "Mission in progress");
    add("gui.adv_rocketry.rocket_monitoring.no_mission", "No active mission");
    add("gui.adv_rocketry.rocket_monitoring.no_rocket", "No loaded rocket linked");
    add("gui.adv_rocketry.rocket_monitoring.outbound", "Outbound");
    add("gui.adv_rocketry.rocket_monitoring.ready", "Ready at launch pad");
    add("gui.adv_rocketry.rocket_monitoring.remaining", "Remaining: %s");
    add("gui.adv_rocketry.rocket_monitoring.returning", "Returning");
    add("gui.adv_rocketry.rocket_monitoring.vertical_speed_m_t", "Vertical speed: %s m/t");
    add("gui.adv_rocketry.rocket_monitoring.working", "Working");
    add("gui.adv_rocketry.station_chip.add_here", "Add here");
    add("gui.adv_rocketry.station_chip.last", "Last");
    add("gui.adv_rocketry.station_chip.clear_saved", "Clear saved");
    add("gui.adv_rocketry.station_chip.delete", "Delete");
    add("gui.adv_rocketry.station_chip.destination_name", "Destination name");
    add("gui.adv_rocketry.station_chip.done", "Done");
    add("gui.adv_rocketry.station_chip.no_saved_destinations", "No saved destinations");
    add("gui.adv_rocketry.station_chip.planet", "Planet %s%s");
    add("gui.adv_rocketry.structure_preview.projector_all_layers", "Projector: all layers");
    add("gui.adv_rocketry.structure_preview.projector_layer", "Projector: layer %s/%s");
    add("gui.adv_rocketry.transport.any_fluid", "Any fluid");
    add("gui.adv_rocketry.transport.clear_fluid_lock", "Clear fluid lock");
    add(
        "gui.adv_rocketry.transport.controls_extraction_at_this_pipe_only",
        "Controls extraction at this pipe only; insertion stays available.");
    add("gui.adv_rocketry.transport.endpoint_buffer_fe", "Endpoint buffer: %s FE");
    add(
        "gui.adv_rocketry.transport.endpoint_filter_click_with_carried_item",
        "Endpoint filter: click with carried item");
    add("gui.adv_rocketry.transport.lock_buffer_mb", "Lock: %s | Buffer: %s mB");
    add(
        "gui.adv_rocketry.transport.machine_side_insert_extract_disabled_disabling",
        "Machine side: insert / extract / disabled. Disabling a pipe side disconnects that"
            + " branch.");
    add("gui.adv_rocketry.transport.mode_disabled", "Disabled");
    add("gui.adv_rocketry.transport.mode_extract", "Extract");
    add("gui.adv_rocketry.transport.mode_insert", "Insert");
    add(
        "gui.adv_rocketry.transport.network_wide_round_robin_gives_each",
        "Network-wide round robin gives each accepting destination one item per turn.");
    add("gui.adv_rocketry.transport.redstone_ignore", "Ignore redstone");
    add("gui.adv_rocketry.transport.redstone_no_signal", "Needs no signal");
    add("gui.adv_rocketry.transport.redstone_signal", "Needs signal");
    add("gui.adv_rocketry.transport.routing_fill_first", "Routing: fill first");
    add("gui.adv_rocketry.transport.routing_round_robin", "Routing: round robin");
    add("gui.adv_rocketry.transport.sections", "%s | %s sections");
    add("gui.adv_rocketry.transport.side_mode", "%s: %s");
    add(
        "gui.adv_rocketry.transport.sneak_use_a_filled_bucket_to",
        "Sneak-use a filled bucket to lock the loaded network; an empty bucket voids pipe buffers"
            + " only.");
  }

  private void addSubtitles() {
    Map<String, String> subtitles = new LinkedHashMap<>();
    subtitles.put("airhissloop", "Air hisses");
    subtitles.put("basic_laser_gun", "Laser fires");
    subtitles.put("buttonblipa", "Button blips");
    subtitles.put("combustionrocket", "Rocket engine roars");
    subtitles.put("crystallizer", "Crystallizer hums");
    subtitles.put("cuttingmachine", "Cutting machine saws");
    subtitles.put("electricarcfurnace", "Arc furnace crackles");
    subtitles.put("electricshocksmall", "Electricity zaps");
    subtitles.put("electrolyser", "Electrolyzer bubbles");
    subtitles.put("gravityohhh", "Gravity shifts");
    subtitles.put("laserdrill", "Laser drill whines");
    subtitles.put("lathe", "Lathe turns");
    subtitles.put("machinelarge", "Terraformer rumbles");
    subtitles.put("precass", "Assembler whirs");
    subtitles.put("railgunbang", "Railgun fires");
    subtitles.put("rollingmachine", "Rolling machine presses");
    for (SoundEvent event : BuiltInRegistries.SOUND_EVENT) {
      if (!event.getLocation().getNamespace().equals(Main.MODID)) continue;
      String text = subtitles.get(event.getLocation().getPath());
      if (text == null)
        throw new IllegalStateException("No subtitle for sound " + event.getLocation());
      add("subtitles." + Main.MODID + "." + event.getLocation().getPath(), text);
    }
  }

  private void addSatelliteTypes() {
    add("satellite_type.adv_rocketry.optical", "Optical Telescope");
    add("satellite_type.adv_rocketry.composition", "Composition Sensor");
    add("satellite_type.adv_rocketry.mass", "Mass Detector");
    add("satellite_type.adv_rocketry.density", "Atmosphere Density Sensor");
    add("satellite_type.adv_rocketry.solar_energy", "Solar Power");
    add("satellite_type.adv_rocketry.ore_scanner", "Ore Mapper");
    add("satellite_type.adv_rocketry.biome_changer", "Biome Changer");
    add("research_type.adv_rocketry.optical", "Distance");
    add("research_type.adv_rocketry.composition", "Composition");
    add("research_type.adv_rocketry.mass", "Mass");
    add("research_type.adv_rocketry.atmosphere_density", "Atmosphere Density");
    add("research_type.adv_rocketry.humidity", "Humidity");
    add("research_type.adv_rocketry.temperature", "Temperature");
  }

  private void addJadeText() {
    add("config.jade.plugin_adv_rocketry.machine_status", "Advanced Rocketry machine status");
    add("jade.adv_rocketry.unformed", "Structure incomplete");
    add("jade.adv_rocketry.running", "Running");
    add("jade.adv_rocketry.idle", "Idle");
    add("jade.adv_rocketry.progress", "Progress: %s%%");
    add("jade.adv_rocketry.room", "Room volume: %s blocks");
  }
}
