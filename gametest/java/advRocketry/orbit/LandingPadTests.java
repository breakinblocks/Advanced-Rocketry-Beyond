package advRocketry.orbit;

import advRocketry.processing.ProcessingRegistry;
import advRocketry.space.GalaxyData;
import advRocketry.space.PlanetRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("adv_rocketry")
@PrefixGameTestTemplate(false)
public final class LandingPadTests {
  @GameTest(template = "empty")
  public static void landingPadRegistersAndProgramsReturnTarget(GameTestHelper helper) {
    var server = helper.getLevel().getServer();
    GalaxyData galaxy = GalaxyData.get(server);
    var space = PlanetRuntime.create(server, galaxy.planets.get(GalaxyData.SPACE_ID));
    BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5)).atY(120);
    space.getChunkAt(pos);
    Station station = new Station();
    station.deployed = true;
    station.orbitPlanet = GalaxyData.EARTH_ID;
    station.x = pos.getX();
    station.y = pos.getY();
    station.z = pos.getZ();
    long id = galaxy.newStation(station);
    space.setBlock(pos, OrbitalRegistry.LANDING_PAD.get().defaultBlockState(), 2);
    var pad = (LandingPadBlockEntity) space.getBlockEntity(pos);
    pad.rename("Home");
    helper.assertTrue(
        "Home".equals(galaxy.stations.get(id).landingPads.get(pos.asLong()))
            && LandingPadLogic.arrivalPad(space, galaxy.stations.get(id)).equals(pos),
        "Station did not register its named landing pad");
    ItemStack linker = new ItemStack(ProcessingRegistry.PART_ITEMS.get("linker").get());
    LinkerItem.setTarget(
        linker, LinkTarget.EMPTY.withPosition(GlobalPos.of(space.dimension(), pos)));
    pad.linker.setStackInSlot(0, linker);
    LandingPadLogic.Target override = LandingPadLogic.override(space, pos.above(), galaxy);
    helper.assertTrue(
        override != null
            && override.planet() == GalaxyData.SPACE_ID
            && override.stationId() == id
            && override.x() == pos.getX() + .5,
        "Programmed landing pad did not override rocket coordinates");
    space.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    helper.assertTrue(
        !galaxy.stations.get(id).landingPads.containsKey(pos.asLong()),
        "Broken landing pad stayed registered");
    galaxy.stations.remove(id);
    galaxy.setDirty();
    helper.succeed();
  }
}
