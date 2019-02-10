package mrriegel.storagenetwork.datafixes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;

import static net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE;

public class ChunkDataReader {
  ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[16];

  public ChunkDataReader(NBTTagCompound chunkData) {
    NBTTagList nbttaglist = chunkData.getTagList("Sections", Constants.NBT.TAG_COMPOUND);

    boolean flag = true;

    for(int l = 0; l < nbttaglist.tagCount(); ++l) {
      NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(l);
      int i1 = nbttagcompound.getByte("Y");
      ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(i1 << 4, flag);
      byte[] abyte = nbttagcompound.getByteArray("Blocks");
      NibbleArray nibblearray = new NibbleArray(nbttagcompound.getByteArray("Data"));
      NibbleArray nibblearray1 = nbttagcompound.hasKey("Add", 7) ? new NibbleArray(nbttagcompound.getByteArray("Add")) : null;
      extendedblockstorage.getData().setDataFromNBT(abyte, nibblearray, nibblearray1);
      extendedblockstorage.setBlockLight(new NibbleArray(nbttagcompound.getByteArray("BlockLight")));
      if (flag) {
        extendedblockstorage.setSkyLight(new NibbleArray(nbttagcompound.getByteArray("SkyLight")));
      }

      extendedblockstorage.recalculateRefCounts();
      aextendedblockstorage[i1] = extendedblockstorage;
    }
  }

  public IBlockState getBlockState(int x, int y, int z) {
    if (y >= 0 && y >> 4 < this.aextendedblockstorage.length)
    {
      ExtendedBlockStorage extendedblockstorage = this.aextendedblockstorage[y >> 4];

      if (extendedblockstorage != NULL_BLOCK_STORAGE)
      {
        return extendedblockstorage.get(x & 15, y & 15, z & 15);
      }
    }

    return Blocks.AIR.getDefaultState();

  }

  public IBlockState getBlockState(BlockPos pos) {
    return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
  }
}
