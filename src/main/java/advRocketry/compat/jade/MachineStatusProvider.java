package advRocketry.compat.jade;

import advRocketry.Main;
import advRocketry.life.OxygenBlockEntity;
import advRocketry.orbit.OrbitalBlockEntity;
import advRocketry.orbit.StationLogic;
import advRocketry.orbit.WarpCoreLogic;
import advRocketry.processing.ProcessingBlock;
import advRocketry.processing.ProcessingBlockEntity;
import advRocketry.rocket.RocketBlockEntity;
import advRocketry.space.GalaxyData;
import advRocketry.space.Planet;
import advRocketry.util.ComponentText;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum MachineStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "machine_status");

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var registries = accessor.getLevel().registryAccess();
        switch (accessor.getBlockEntity()) {
            case OrbitalBlockEntity orbital -> {
                data.putString("status", Component.Serializer.toJson(orbital.status, registries));
                if (orbital.warpController()) {
                    var station = StationLogic.at(accessor.getLevel(), accessor.getPosition());
                    if (station != null && station.inWarp()) {
                        Planet target = GalaxyData.get(accessor.getLevel().getServer())
                                .planets
                                .get(station.warpDestination);
                        data.putString(
                                "status",
                                Component.Serializer.toJson(
                                        Component.translatable(
                                                "jade.adv_rocketry.warp_transit", target == null ? "?" : target.name),
                                        registries));
                        data.putInt("progress", (int) (100
                                * station.warpProgress(accessor.getLevel()
                                        .getServer()
                                        .overworld()
                                        .getGameTime())));
                    } else if (station != null) {
                        Planet orbit = GalaxyData.get(accessor.getLevel().getServer())
                                .planets
                                .get(station.orbitPlanet);
                        data.putString(
                                "status",
                                Component.Serializer.toJson(
                                        Component.translatable(
                                                "jade.adv_rocketry.orbiting", orbit == null ? "?" : orbit.name),
                                        registries));
                    }
                }
                if (orbital.warpCore() && WarpCoreLogic.complete(orbital)) {
                    data.putInt("dilithium", WarpCoreLogic.dilithiumInInput(orbital));
                    data.putInt("warp_fuel", WarpCoreLogic.stationFuel(orbital));
                }
            }
            case RocketBlockEntity rocket -> {
                data.putString("status", Component.Serializer.toJson(rocket.status, registries));
                if (rocket.buildTotal > 0 && rocket.buildRemaining > 0)
                    data.putInt("progress", 100 * (rocket.buildTotal - rocket.buildRemaining) / rocket.buildTotal);
            }
            case ProcessingBlockEntity machine -> {
                data.putBoolean("formed", accessor.getBlockState().getValue(ProcessingBlock.FORMED));
                data.putBoolean("running", machine.running());
                if (machine.durationTicks() > 0)
                    data.putInt("progress", 100 * machine.progressTicks() / machine.durationTicks());
            }
            case OxygenBlockEntity vent -> {
                data.putBoolean("running", vent.active);
                data.putInt("room", vent.roomSize());
                if (vent.roomSize() == 0 && vent.leak() != null)
                    data.putLong("leak", vent.leak().asLong());
            }
            default -> {}
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        var registries = accessor.getLevel().registryAccess();
        if (data.contains("formed") && !data.getBoolean("formed"))
            tooltip.add(Component.translatable("jade.adv_rocketry.unformed"));
        if (data.contains("status") && !data.getString("status").isEmpty())
            tooltip.add(ComponentText.read(data.getString("status"), registries));
        if (data.contains("running"))
            tooltip.add(Component.translatable(
                    data.getBoolean("running") ? "jade.adv_rocketry.running" : "jade.adv_rocketry.idle"));
        if (data.contains("progress"))
            tooltip.add(Component.translatable("jade.adv_rocketry.progress", data.getInt("progress")));
        if (data.contains("room")) tooltip.add(Component.translatable("jade.adv_rocketry.room", data.getInt("room")));
        if (data.contains("leak")) {
            BlockPos leak = BlockPos.of(data.getLong("leak"));
            tooltip.add(Component.translatable("jade.adv_rocketry.leak", leak.getX(), leak.getY(), leak.getZ()));
        }
        if (data.contains("dilithium"))
            tooltip.add(Component.translatable("jade.adv_rocketry.dilithium", data.getInt("dilithium")));
        if (data.contains("warp_fuel"))
            tooltip.add(Component.translatable(
                    "jade.adv_rocketry.warp_fuel", data.getInt("warp_fuel"), WarpCoreLogic.MAX_FUEL));
    }
}
