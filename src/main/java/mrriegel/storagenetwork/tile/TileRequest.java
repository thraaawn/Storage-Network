package mrriegel.storagenetwork.tile;

import java.util.HashMap;
import java.util.Map;

import mrriegel.storagenetwork.blocks.BlockKabel;
import mrriegel.storagenetwork.gui.ContainerRequest;
import mrriegel.storagenetwork.helper.Inv;
import mrriegel.storagenetwork.network.PacketHandler;
import mrriegel.storagenetwork.network.RequestMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class TileRequest extends TileEntity implements ITickable,IConnection {
	private BlockPos master;
	public Map<Integer, ItemStack> back = new HashMap<Integer, ItemStack>();
	public Map<Integer, ItemStack> matrix = new HashMap<Integer, ItemStack>();
	public boolean downwards;
	public Sort sort = Sort.NAME;

	public enum Sort {
		AMOUNT, NAME;
		private static Sort[] vals = values();

		public Sort next() {
			return vals[(this.ordinal() + 1) % vals.length];
		}
	}

	@Override
	public void update() {
		master = masterAround(worldObj, pos);
		if (worldObj.getTotalWorldTime() % 20 != 0 || worldObj.isRemote)
			return;
		for (int i = 0; i < 6; i++) {
			ItemStack s = back.get(i);
			if (s != null) {
				TileMaster tile = (TileMaster) worldObj.getTileEntity(master);
				int num = s.stackSize;
				int rest = tile.insertStack(s.copy(), null);
				if (rest == 0) {
					back.put(i, null);
					markDirty();
					worldObj.markBlockForUpdate(pos);
					for (EntityPlayer p : MinecraftServer.getServer()
							.getConfigurationManager().playerEntityList) {
						Container c = p.openContainer;
						if (c instanceof ContainerRequest
								&& ((ContainerRequest) c).tile.getPos().equals(
										this.pos))
							((ContainerRequest) c).back
									.setInventorySlotContents(i, back.get(i));
					}
					PacketHandler.INSTANCE.sendToServer(new RequestMessage(0,
							master.getX(), master.getY(), master.getZ(), null));
					break;
				} else if (rest == num)
					continue;
				else {
					back.put(i, Inv.copyStack(s, rest));
					markDirty();
					worldObj.markBlockForUpdate(pos);
					for (EntityPlayer p : MinecraftServer.getServer()
							.getConfigurationManager().playerEntityList) {
						Container c = p.openContainer;
						if (c instanceof ContainerRequest)
							((ContainerRequest) c).back
									.setInventorySlotContents(i, back.get(i));
					}
					PacketHandler.INSTANCE.sendToServer(new RequestMessage(0,
							master.getX(), master.getY(), master.getZ(), null));
					break;
				}
			}
		}
	}

	BlockPos masterAround(World worldIn, BlockPos pos) {
		for (BlockPos p : TileMaster.getSides(pos)) {
			if (worldIn.getTileEntity(p) instanceof TileKabel) {
				if (((TileKabel) worldIn.getTileEntity(p)).getMaster() != null) {
					return ((TileKabel) worldIn.getTileEntity(p)).getMaster();
				}
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		master = new Gson().fromJson(compound.getString("master"),
				new TypeToken<BlockPos>() {
				}.getType());
		downwards = compound.getBoolean("dir");
		sort = Sort.valueOf(compound.getString("sort"));
		NBTTagList invList = compound.getTagList("back",
				Constants.NBT.TAG_COMPOUND);
		back = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < invList.tagCount(); i++) {
			NBTTagCompound stackTag = invList.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot");
			back.put(slot, ItemStack.loadItemStackFromNBT(stackTag));
		}
		invList = compound.getTagList("matrix", Constants.NBT.TAG_COMPOUND);
		matrix = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < invList.tagCount(); i++) {
			NBTTagCompound stackTag = invList.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot");
			matrix.put(slot, ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setString("master", new Gson().toJson(master));
		compound.setBoolean("dir", downwards);
		compound.setString("sort", sort.toString());
		NBTTagList invList = new NBTTagList();
		for (int i = 0; i < 6; i++) {
			if (back.get(i) != null) {
				NBTTagCompound stackTag = new NBTTagCompound();
				stackTag.setByte("Slot", (byte) i);
				back.get(i).writeToNBT(stackTag);
				invList.appendTag(stackTag);
			}
		}
		compound.setTag("back", invList);
		invList = new NBTTagList();
		for (int i = 0; i < 9; i++) {
			if (matrix.get(i) != null) {
				NBTTagCompound stackTag = new NBTTagCompound();
				stackTag.setByte("Slot", (byte) i);
				matrix.get(i).writeToNBT(stackTag);
				invList.appendTag(stackTag);
			}
		}
		compound.setTag("matrix", invList);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound syncData = new NBTTagCompound();
		this.writeToNBT(syncData);
		return new S35PacketUpdateTileEntity(new BlockPos(this.getPos().getX(),
				this.getPos().getY(), this.getPos().getZ()), 1, syncData);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public BlockPos getMaster() {
		return master;
	}

	public void setMaster(BlockPos master) {
		this.master = master;
	}
}
