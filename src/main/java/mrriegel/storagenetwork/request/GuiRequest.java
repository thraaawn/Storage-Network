package mrriegel.storagenetwork.request;

import mrriegel.storagenetwork.RigelNetworkGuiRequest;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.input.Keyboard;

public class GuiRequest extends RigelNetworkGuiRequest {
	TileRequest tile;

	public GuiRequest(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		tile = ((ContainerRequest) inventorySlots).tile;
		texture = new ResourceLocation(StorageNetwork.MODID + ":textures/gui/request.png");
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		searchBar = new GuiTextField(0, fontRendererObj, guiLeft + 81, guiTop + 96, 85, fontRendererObj.FONT_HEIGHT);
		searchBar.setMaxStringLength(30);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setVisible(true);
		searchBar.setTextColor(16777215);
		direction = new Button(0, guiLeft + 7, guiTop + 93, "");
		buttonList.add(direction);
		sort = new Button(1, guiLeft + 21, guiTop + 93, "");
		buttonList.add(sort);
		// left = new Button(2, guiLeft + 44, guiTop + 93, "<");
		// buttonList.add(left);
		// right = new Button(3, guiLeft + 58, guiTop + 93, ">");
		// buttonList.add(right);
		jei = new Button(4, guiLeft + 169, guiTop + 93, "");
		if (ConfigHandler.jeiLoaded)
			buttonList.add(jei);
	}

	@Override
	public int getLines() {
		return 4;
	}

	@Override
	public int getColumns() {
		return 9;
	}

	@Override
	public boolean getDownwards() {
		return tile.downwards;
	}

	@Override
	public void setDownwards(boolean d) {
		tile.downwards = d;
	}

	@Override
	public EnumSortType getSort() {
		return tile.sort;
	}

	@Override
	public void setSort(EnumSortType s) {
		tile.sort = s;
	}

	@Override
	public BlockPos getPos() {
		return tile.getPos();
	}

	// @Override
	// protected BlockPos getMaster() {
	// return tile.getMaster();
	// }

	@Override
	protected int getDim() {
		return tile.getWorld().provider.getDimension();
	}

	@Override
	protected boolean inField(int mouseX, int mouseY) {
		return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90);
	}

	@Override
	protected boolean inSearchbar(int mouseX, int mouseY) {
		return isPointInRegion(81, 96, 85, fontRendererObj.FONT_HEIGHT, mouseX, mouseY);
	}

	@Override
	protected boolean inX(int mouseX, int mouseY) {
		return isPointInRegion(63, 110, 7, 7, mouseX, mouseY);
	}

}
