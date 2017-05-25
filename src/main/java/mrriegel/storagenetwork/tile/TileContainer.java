package mrriegel.storagenetwork.tile;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import com.google.common.collect.Lists;

public class TileContainer extends TileConnectable implements ISidedInventory {
	private EnumFacing input, output;
	private NonNullList<ItemStack> inv;
	public static final int SIZE = 9;

	public TileContainer() {
		super();
		inv = NonNullList.withSize(SIZE, ItemStack.EMPTY);
		input = EnumFacing.UP;
		output = EnumFacing.DOWN;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		input = EnumFacing.byName(compound.getString("input"));
		input = input != null ? input : EnumFacing.UP;
		output = EnumFacing.byName(compound.getString("output"));
		output = output != null ? output : EnumFacing.DOWN;
    inv = NonNullList.withSize(SIZE, ItemStack.EMPTY);

		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			if (j >= 0 && j < this.inv.size()) {
				this.inv.set(j,new ItemStack(nbttagcompound));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (input != null)
			compound.setString("input", input.toString());
		if (output != null)
			compound.setString("output", output.toString());

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inv.size(); ++i) {
			if (this.inv.get(i).isEmpty()==false) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				this.inv.get(i).writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		compound.setTag("Items", nbttaglist);
		return super.writeToNBT(compound);
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return false;
	}

	public EnumFacing getInput() {
		return input;
	}

	public void setInput(EnumFacing input) {
		this.input = input;
	}

	public EnumFacing getOutput() {
		return output;
	}

	public void setOutput(EnumFacing output) {
		this.output = output;
	}

	public List<ItemStack> getTemplates() {
		List<ItemStack> lis = Lists.newArrayList();
		// for (int i = 0; i < INVSIZE; i++)
		// if (getStackInSlot(i) != null)
		// lis.add(getStackInSlot(i).copy());
		return lis;
	}

	@Override
	public void onChunkUnload() {
		if (master != null && world.getChunkFromBlockCoords(master).isLoaded() && world.getTileEntity(master) instanceof TileMaster)
			((TileMaster) world.getTileEntity(master)).refreshNetwork();
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] {};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return index >= 0 && index < this.inv.size() ? this.inv.get(index) : ItemStack.EMPTY;
	}

	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inv, index, count);

		if (itemstack != null) {
			this.markDirty();
		}

		return itemstack;
	}

	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		if (this.inv.get(index).isEmpty()==false) {
			ItemStack itemstack = this.inv.get(index) ;
			this.inv.set(index,ItemStack.EMPTY);
			return itemstack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
    this.inv.set(index,stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount( this.getInventoryStackLimit());
		}

		this.markDirty();
	}

	@Override
	public int getSizeInventory() {
		return SIZE;
	}

	@Override
	public String getName() {
		return "null";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
 
	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
    inv = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	}

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isUsableByPlayer(EntityPlayer player) {
    // TODO Auto-generated method stub
    return true;
  }
}
