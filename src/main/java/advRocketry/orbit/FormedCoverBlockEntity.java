package advRocketry.orbit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FormedCoverBlockEntity extends BlockEntity {
    private BlockState original = Blocks.AIR.defaultBlockState();

    public FormedCoverBlockEntity(BlockPos pos, BlockState state) {
        super(OrbitalRegistry.FORMED_COVER_ENTITY.get(), pos, state);
    }

    public BlockState original() {
        return original;
    }

    void original(BlockState state) {
        original = state;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("original", NbtUtils.writeBlockState(original));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        original = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("original"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
