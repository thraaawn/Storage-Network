package mrriegel.storagenetwork.block.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import mrriegel.storagenetwork.network.RequestCableMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiControl extends GuiContainer {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request_full.png");
  //
  private TileControl tile;
  protected GuiTextField searchBar;
  //list includes search bar 
  //private List<GuiTextField> textBoxes = new ArrayList<>();
  private List<ProcessWrapper> processors = null;
  Map<Integer, CableRow> allRows = new HashMap<>();
  //  List<CableRow> visibleRows = new ArrayList<>();
  private boolean rowsCreated;
  //how many rows to skip over
  private int page = 0;
  private int maxPage = 0;//TODO 

  public GuiControl(ContainerControl inventorySlotsIn) {
    super(inventorySlotsIn);
    processors = new ArrayList<>();
    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    tile = inventorySlotsIn.getTileRequest();
    //  request the list of tiles 
    rowsCreated = false;
    PacketRegistry.INSTANCE.sendToServer(new RequestCableMessage());
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    searchBar = new GuiTextField(0, fontRenderer,
        guiLeft + 10, guiTop + 160, 158, fontRenderer.FONT_HEIGHT);
    searchBar.setMaxStringLength(30);
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setFocused(true);
    searchBar.setTextColor(16777215);
    //mock data only  
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }

  public class CableRow {

    public CableRow(ProcessWrapper p) {
      super();
      this.p = p;
    }

    ProcessWrapper p;
    GuiControlButton btnOnOff;
    GuiControlButton btnMinus;
    GuiControlButton btnPlus;
    public GuiTextFieldProcCable txtBox;
    public int x;
    public int y;
    public int width;
    public int height;
    public int index;

    public boolean isInside(int mouseX, int mouseY) {
      return x < mouseX && mouseX < x + width &&
          y < mouseY && mouseY < y + height;
    }

    public void mouseClicked(int mouseX, int mouseY, int btn) {
      StorageNetwork.log("row clicked at " + p.output.getDisplayName());

    }

    public boolean compareSearch() {
      boolean searchDoesMatch = true;
      if (searchBar.getText().isEmpty() == false) {
        String slower = searchBar.getText().toLowerCase();
        searchDoesMatch = slower.contains(this.p.name.toLowerCase())
            || this.p.name.toLowerCase().contains(slower);
      }
      return searchDoesMatch;
    }
    public boolean isHidden() {
      //above the top, or below the bottom
      return this.y < guiTop || this.y > guiTop + 150;
    }

    public void drawScreen() {
      btnOnOff.visible = true;
      //     row.txtBox.setVisible(!row.p.alwaysOn);
      btnMinus.visible = (!p.alwaysOn);
      btnPlus.visible = (!p.alwaysOn);
    }

    public void hideComponents() {
      this.btnOnOff.visible = false;
      this.btnMinus.visible = false;
      this.btnPlus.visible = false;
    }

    public void updatePagePosition(final int page, int hiddenOffset) {
      //if im at index 3, but page has scrolled up once, 
      // and one above me has been hidden, i am at position 1
      final int mockIndex = index - page - hiddenOffset;
      final int rowHeight = 25;
      this.y = guiTop + 10 + mockIndex * rowHeight;
      btnMinus.y = this.y;
      btnPlus.y = this.y;
      btnOnOff.y = this.y;
    }
  }

  private void createAllRows() {
    if (rowsCreated) {
      return;
    }
    //textBoxes = new ArrayList<>();
    int x = guiLeft + 72;
    int y = guiTop + 10;
    final int spacer = 22;
    final int rowHeight = 25;
    int row = 0;
    int btnid = 1;
    Map<Integer, CableRow> rows = new HashMap<>();
    for (ProcessWrapper p : processors) {
      if (p.output.isEmpty()) {
        continue;
      }
      CableRow rowModel = new CableRow(p);
      rowModel.index = row;
      rowModel.x = guiLeft + 8;
      rowModel.y = y;
      rowModel.width = 150;
      rowModel.height = 20;
      //buttons come later
      GuiControlButton btnOnOff = new GuiControlButton(btnid++, CableMessageType.P_ONOFF,
          rowModel.x, rowModel.y, 16, 16, "");
      btnOnOff.cable = p;
      btnOnOff.visible = false;
      btnOnOff.addTooltip("onoff");
      rowModel.btnOnOff = btnOnOff;
      this.addButton(rowModel.btnOnOff);
      //      GuiTextFieldProcCable txt = new GuiTextFieldProcCable(btnid++, fontRenderer,
      //          x + 64, y + 4);
      //      txt.setMaxStringLength(4);
      //      txt.setEnableBackgroundDrawing(false);
      //      txt.setVisible(true);
      //      txt.setTextColor(16777215);
      //      //mock data only
      //      txt.setText("" + p.count);
      //  textBoxes.add(txt);
      int offset = 66;
      GuiControlButton btnMinus = new GuiControlButton(btnid++, CableMessageType.P_CTRL_LESS,
          rowModel.x + offset + 2 * spacer, rowModel.y, 16, 16, "-");
      btnMinus.cable = p;
      btnMinus.addTooltip("a");
      btnMinus.visible = false;
      this.addButton(btnMinus);
      rowModel.btnMinus = btnMinus;
      GuiControlButton btnPlus = new GuiControlButton(btnid++, CableMessageType.P_CTRL_MORE,
          rowModel.x + offset + 12 + 3 * spacer, rowModel.y, 16, 16, "+");
      btnPlus.cable = p;
      btnPlus.visible = false;
      btnPlus.addTooltip("plus");
      this.addButton(btnPlus);
      rowModel.btnPlus = btnPlus;
      //  rowModel.txtBox = txt;
      rows.put(row, rowModel);
      row++;
      y += rowHeight;
    }
    this.maxPage = rows.size() - 1;
    StorageNetwork.log("MP" + maxPage);
    this.allRows = rows;
    rowsCreated = true;
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    for (GuiButton b : this.buttonList) {
      if (button.id == b.id && b instanceof GuiControlButton) {
        GuiControlButton btn = (GuiControlButton) b;
        // do the thing 
        btn.actionPerformed();
        break;
      }
    }
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (processors != null && processors.size() > 0) {
      createAllRows();
    }
    if (searchBar != null) {
      searchBar.updateCursorCounter();
    }

    for (GuiButton btn : this.buttonList) {
      if (btn instanceof GuiControlButton) {
        GuiControlButton b = (GuiControlButton) btn;
        //update texture 
        if (b.cable.alwaysOn) {
          //set green
          b.textureX = 0;
          b.textureY = 449;
        }
        else {
          //set grey 
          b.textureX = 95;
          b.textureY = 449;
        }
      }
    }
  }

  @Override
  public void drawScreen(int x, int y, float par3) {
    //  drawDefaultBackground();
    super.drawScreen(x, y, par3);
    if (this.searchBar != null) {
      this.searchBar.drawTextBox();
    }
    //todo: visible rows
    int hiddenOffset = 0;
    for (CableRow row : this.allRows.values()) {
      //update row location based on page index
      if (row.compareSearch() == false) {
        hiddenOffset++;
      }
      row.updatePagePosition(this.page, hiddenOffset);
      // is it visible or hidden 
      if (row.isHidden()) {
        row.hideComponents();
      }
      else {
        row.drawScreen();
      }
    }
    for (GuiButton btn : this.buttonList) {
      if (btn.isMouseOver() && btn instanceof GuiControlButton) {
        //TOOLTIP 
        GuiControlButton button = (GuiControlButton) btn;
        this.drawHoveringText(button.getTooltips(), x, y);
        //      this.drawHoveringText(textLines, x, y)oh ;
      }
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
  }

  int FONT = 14737632;

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    renderTextures();
    GlStateManager.pushMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    RenderHelper.enableGUIStandardItemLighting();
    for (CableRow row : this.allRows.values()) {
      //draw me  
      if (row.isHidden() == false) {
        mc.getRenderItem().renderItemAndEffectIntoGUI(row.p.output, row.x + 20, row.y);
        /// TODO target blockname  text
        //AND OR  recipe ing list as text 
        //TODO maybe tooltip for this
        this.drawString(this.fontRenderer, row.p.name, row.x + 40, row.y + 3, FONT);
        this.drawString(this.fontRenderer, row.p.count + "", row.x + 128, row.y + 3, FONT);
      }
    }
    GlStateManager.popMatrix();
  }

  private void renderTextures() {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xCenter = (this.width - this.xSize) / 2;
    int yCenter = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xCenter, yCenter, 0, 0, this.xSize, this.ySize);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
    int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
    if (inField(mouseX, mouseY)) {
      int mouse = Mouse.getEventDWheel();
      if (mouse > 0 && page > 0) {
        page--;
      }
      if (mouse < 0 && page < maxPage) {
        page++;
      }
    }
  }

  protected boolean inField(int mouseX, int mouseY) {
    return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90);
  }

  @Override
  protected void mouseClicked(int x, int y, int btn) throws IOException {
    super.mouseClicked(x, y, btn);
    if (this.searchBar != null) {
      this.searchBar.mouseClicked(x, y, btn);
    }
    for (CableRow row : this.allRows.values()) {
      if (row.isInside(x, y)) {
        row.mouseClicked(x, y, btn);
      }
    }
    //    for (GuiTextField txtNew : this.textBoxes)
    //      txtNew.mouseClicked(x, y, btn);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    if (this.searchBar != null && this.searchBar.isFocused()) {
      this.searchBar.textboxKeyTyped(typedChar, keyCode);
    }
    //    for (GuiTextField txt : this.textBoxes) {
    //      if (txt.isFocused()) {
    //        txt.textboxKeyTyped(typedChar, keyCode);
    //        break;
    //      }
    //    }
  }

  /**
   * Set from network response packet
   * 
   * @param cables
   */
  public void setTiles(List<ProcessWrapper> cables) {
    processors = cables;
    processors = processors.stream().sorted((a, b) -> {
      return a.name.compareTo(b.name);
    }).collect(Collectors.toList());
  }
}
