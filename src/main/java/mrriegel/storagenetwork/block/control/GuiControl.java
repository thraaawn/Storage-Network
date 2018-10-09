package mrriegel.storagenetwork.block.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Keyboard;
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
  private List<GuiTextField> textBoxes = new ArrayList<>();
  private List<ProcessWrapper> processors = null;
  int currentPage = 0;// offset for scroll? pge btns?   
  Map<Integer, CableRow> allRows = new HashMap<>();
  private boolean buttonsInit;

  public GuiControl(ContainerControl inventorySlotsIn) {
    super(inventorySlotsIn);
    processors = new ArrayList<>();
    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    tile = inventorySlotsIn.getTileRequest();
    //  request the list of tiles 
    buttonsInit = false;
    PacketRegistry.INSTANCE.sendToServer(new RequestCableMessage());
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    searchBar = new GuiTextField(0, fontRenderer,
        10, 160, 158, fontRenderer.FONT_HEIGHT);
    searchBar.setMaxStringLength(30);
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused(true);
    //mock data only
    searchBar.setText("abc123abc123abc123abc123abc123abc");
    this.textBoxes.add(searchBar);
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }

  public class CableRow {

    public CableRow(ProcessWrapper p, GuiControlButton btnOnOff, GuiControlButton btnMinus, GuiControlButton btnPlus) {
      super();
      this.p = p;
      this.btnOnOff = btnOnOff;
      this.btnMinus = btnMinus;
      this.btnPlus = btnPlus;
    }

    ProcessWrapper p;
    GuiControlButton btnOnOff;
    GuiControlButton btnMinus;
    GuiControlButton btnPlus;
  }

  private void addButtons() {
    if (buttonsInit) {
      return;
    }
    textBoxes = new ArrayList<>();
    int x = guiLeft + 62;
    int y = guiTop + 8;
    final int spacer = 22;
    final int rowHeight = 25;
    int row = 0;
    int btnid = 1;
    for (ProcessWrapper p : processors) {
      GuiControlButton btnOnOff = new GuiControlButton(btnid++, CableMessageType.P_ONOFF,
          x + spacer, y, 16, 16, "");
      btnOnOff.cable = p;
      this.addButton(btnOnOff);
      GuiTextField txt = new GuiTextField(btnid++, fontRenderer,
          x - guiLeft + 64, y - guiTop + 4, 22, fontRenderer.FONT_HEIGHT);
      txt.setMaxStringLength(4);
      txt.setEnableBackgroundDrawing(false);
      txt.setVisible(true);
      txt.setTextColor(16777215);
      txt.setFocused(true);
      //mock data only
      txt.setText("" + p.count);
      textBoxes.add(txt);
      GuiControlButton btnMinus = new GuiControlButton(btnid++, CableMessageType.P_CTRL_LESS,
          x + 2 * spacer, y, 16, 16, "-");
      btnMinus.cable = p;
      this.addButton(btnMinus);
      GuiControlButton btnPlus = new GuiControlButton(btnid++, CableMessageType.P_CTRL_MORE,
          x + 3 * spacer + 12, y, 16, 16, "+");
      btnPlus.cable = p;
      this.addButton(btnPlus);
      y += rowHeight;
      this.allRows.put(row, new CableRow(p, btnOnOff, btnMinus, btnPlus));
      row++;
    }
    buttonsInit = true;
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
    for (GuiTextField txt : this.textBoxes) {
      txt.updateCursorCounter();
    }

    if (processors != null && processors.size() > 0) {
      addButtons();
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
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    for (GuiTextField txt : this.textBoxes) {
      txt.drawTextBox();
    }


  }

  int FONT = 14737632;

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    renderTextures();
    int x = guiLeft + 8;
    int y = guiTop + 8;
    int currentPage = 0;// offset for scroll? pge btns? 
    int spacer = 22;
    GlStateManager.pushMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    RenderHelper.enableGUIStandardItemLighting();
    for (ProcessWrapper p : processors) {
      x = guiLeft + 8;
      //draw me  
      mc.getRenderItem().renderItemAndEffectIntoGUI(p.output, x, y);
      x += 22;
      /// TODO target blockname  text
      //AND OR  recipe ing list as text 

      //TODO maybe tooltip for this
      y += 3;   
      this.drawString(this.fontRenderer, p.name, x, y, FONT);
      //      this.drawString(this.fontRenderer, p.count + "", x + 96, y, FONT);
      y += spacer;
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
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    for (GuiTextField txt : this.textBoxes) {
      if (txt.isFocused()) {
        txt.textboxKeyTyped(typedChar, keyCode);
        break;
      }
    }

  }

  /**
   * Set from network response packet
   * 
   * @param cables
   */
  public void setTiles(List<ProcessWrapper> cables) {
    processors = cables;
  }
}
