package mrriegel.storagenetwork.remote;

import mrriegel.storagenetwork.RigelNetworkGuiRequest;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.input.Keyboard;

public class GuiRemote extends RigelNetworkGuiRequest {

	public GuiRemote(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		texture = new ResourceLocation(StorageNetwork.MODID + ":textures/gui/remote.png");
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		searchBar = new GuiTextField(0, fontRendererObj, guiLeft + 81, guiTop + 96 + 64, 85, fontRendererObj.FONT_HEIGHT);
		searchBar.setMaxStringLength(30);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setVisible(true);
		searchBar.setTextColor(16777215);
		direction = new Button(0, guiLeft + 7, guiTop + 93 + 64, "");
		buttonList.add(direction);
		sort = new Button(1, guiLeft + 21, guiTop + 93 + 64, "");
		buttonList.add(sort);
		// left = new Button(2, guiLeft + 44, guiTop + 93 + 64, "<");
		// buttonList.add(left);
		// right = new Button(3, guiLeft + 58, guiTop + 93 + 64, ">");
		// buttonList.add(right);
		jei = new Button(4, guiLeft + 169, guiTop + 93 + 64, "");
		if (ConfigHandler.jeiLoaded)
			buttonList.add(jei);

	}

	@Override
	public int getLines() {
		return 8;
	}

	@Override
	public int getColumns() {
		return 9;
	}

	@Override
	public boolean getDownwards() {
		return NBTHelper.getBoolean(mc.player.inventory.getCurrentItem(), "down");
	}

	@Override
	public void setDownwards(boolean d) {
		NBTHelper.setBoolean(mc.player.inventory.getCurrentItem(), "down", d);

	}

	@Override
	public EnumSortType getSort() {
		return EnumSortType.valueOf(NBTHelper.getString(mc.player.inventory.getCurrentItem(), "sort"));
	}

	@Override
	public void setSort(EnumSortType s) {
		NBTHelper.setString(mc.player.inventory.getCurrentItem(), "sort", s.toString());
	}

	@Override
	public BlockPos getPos() {
		return BlockPos.ORIGIN;
	}

	// @Override
	// protected BlockPos getMaster() {
	// ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
	// return new BlockPos(NBTHelper.getInteger(stack, "x"),
	// NBTHelper.getInteger(stack, "y"), NBTHelper.getInteger(stack, "z"));
	// }

	@Override
	protected int getDim() {
		return NBTHelper.getInteger(mc.player.inventory.getCurrentItem(), "dim");
	}

	@Override
	protected boolean inField(int mouseX, int mouseY) {
		return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90 + 64);
	}

	@Override
	protected boolean inSearchbar(int mouseX, int mouseY) {
		return isPointInRegion(81, 96 + 64, 85, fontRendererObj.FONT_HEIGHT, mouseX, mouseY);
	}

	@Override
	protected boolean inX(int mouseX, int mouseY) {
		return false;
	}

}
