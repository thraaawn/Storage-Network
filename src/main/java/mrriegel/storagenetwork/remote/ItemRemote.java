package mrriegel.storagenetwork.remote;
import java.util.List;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.ConfigHandler;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.GuiHandler;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRemote extends Item {
  public ItemRemote() {
    super();
    this.setCreativeTab(CreativeTab.tab);
    this.setHasSubtypes(true);
    this.setMaxStackSize(1);
  }
  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
    if (isInCreativeTab(tab)) {
      for (int i = 0; i < 2; i++) {
        list.add(new ItemStack(this, 1, i));
      }
    }
  }
  @Override
  public String getUnlocalizedName(ItemStack stack) {
    return this.getUnlocalizedName() + "_" + stack.getItemDamage();
  }
  @Override
  public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add(I18n.format("tooltip.storagenetwork.remote_" + stack.getItemDamage()));
    if (stack.hasTagCompound() && NBTHelper.getBoolean(stack, "bound")) {
      tooltip.add("Dimension: " + NBTHelper.getInteger(stack, "dim") + ", x: " + NBTHelper.getInteger(stack, "x") + ", y: " + NBTHelper.getInteger(stack, "y") + ", z: " + NBTHelper.getInteger(stack, "z"));
    }
  }
  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
    ItemStack itemStackIn = playerIn.getHeldItem(hand);
    if (worldIn.isRemote)
      return super.onItemRightClick(worldIn, playerIn, hand);
    int x = NBTHelper.getInteger(itemStackIn, "x");
    int y = NBTHelper.getInteger(itemStackIn, "y");
    int z = NBTHelper.getInteger(itemStackIn, "z");
    World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(NBTHelper.getInteger(itemStackIn, "dim"));
    if (NBTHelper.getBoolean(itemStackIn, "bound") && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileMaster) {
      if ((itemStackIn.getItemDamage() == 0 && NBTHelper.getInteger(itemStackIn, "dim") == worldIn.provider.getDimension() && playerIn.getDistance(x, y, z) <= ConfigHandler.rangeWirelessAccessor) || itemStackIn.getItemDamage() == 1) {
        if (world.getChunkFromBlockCoords(new BlockPos(x, y, z)).isLoaded()) {
          if (NBTHelper.getString(itemStackIn, "sort") == null)
            NBTHelper.setString(itemStackIn, "sort", EnumSortType.NAME.toString());
          playerIn.openGui(StorageNetwork.instance, getGui(), world, x, y, z);
        }
        else
          playerIn.sendMessage(new TextComponentString("Cable Master not loaded."));
      }
      else if (itemStackIn.getItemDamage() == 0 && (NBTHelper.getInteger(itemStackIn, "dim") == worldIn.provider.getDimension() || playerIn.getDistance(x, y, z) > 32))
        if (!worldIn.isRemote)
          playerIn.sendMessage(new TextComponentString("Out of Range"));
    }
    return super.onItemRightClick(worldIn, playerIn, hand);
  }
  @Override
  public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = playerIn.getHeldItem(hand);
    if (worldIn.getTileEntity(pos) instanceof TileMaster) {
      NBTHelper.setInteger(stack, "x", pos.getX());
      NBTHelper.setInteger(stack, "y", pos.getY());
      NBTHelper.setInteger(stack, "z", pos.getZ());
      NBTHelper.setBoolean(stack, "bound", true);
      NBTHelper.setInteger(stack, "dim", worldIn.provider.getDimension());
      NBTHelper.setString(stack, "sort", EnumSortType.NAME.toString());
      return EnumActionResult.SUCCESS;
    }
    return super.onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
  }
  protected int getGui() {
    return GuiHandler.REMOTE;
  }
  public static TileMaster getTile(ItemStack stack) {
    if (stack == null || stack.isEmpty()) { return null; }
    TileEntity t = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(NBTHelper.getInteger(stack, "dim")).getTileEntity(new BlockPos(NBTHelper.getInteger(stack, "x"), NBTHelper.getInteger(stack, "y"), NBTHelper.getInteger(stack, "z")));
    return t instanceof TileMaster ? (TileMaster) t : null;
  }
  public static void copyTag(ItemStack from, ItemStack to) {
    NBTHelper.setInteger(to, "x", NBTHelper.getInteger(from, "x"));
    NBTHelper.setInteger(to, "y", NBTHelper.getInteger(from, "y"));
    NBTHelper.setInteger(to, "z", NBTHelper.getInteger(from, "z"));
    NBTHelper.setBoolean(to, "bound", NBTHelper.getBoolean(from, "bound"));
    NBTHelper.setInteger(to, "dim", NBTHelper.getInteger(from, "dim"));
    NBTHelper.setString(to, "sort", NBTHelper.getString(from, "sort"));
  }
}
