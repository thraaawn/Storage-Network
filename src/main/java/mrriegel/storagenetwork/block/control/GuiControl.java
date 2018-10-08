package mrriegel.storagenetwork.block.control;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.GuiCableButton;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.RequestCableMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiControl extends GuiContainer {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request_full.png");
  private TileControl tile;
  private List<ProcessWrapper> processors;

  public GuiControl(ContainerControl inventorySlotsIn) {
    super(inventorySlotsIn);
    processors = new ArrayList<>();

    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    tile = inventorySlotsIn.getTileRequest();
    //  request the list of tiles 

    PacketRegistry.INSTANCE.sendToServer(new RequestCableMessage());
  }

  @Override
  public void initGui() {
    super.initGui();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
  }

  int FONT = 14737632;
  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    renderTextures();
    int x = guiLeft + 8;
    int y = guiTop + 8;
    int currentPage = 0;// offset for scroll? pge btns? 
    int spacer = 22;
    for (ProcessWrapper p : processors) {
      x = guiLeft + 8;
      //draw me  
      mc.getRenderItem().renderItemAndEffectIntoGUI(p.output, x, y);
      x += 22;
      /// TODO target blockname  text
      //AND OR  recipe ing list as text 
      String n = "";
      if (p.alwaysOn) {
        n = "processing.alwayson";
      }
      else {
        n = p.name;
      }
      //TODO maybe tooltip for this
      this.drawCenteredString(this.fontRenderer, n,
          x + 6, y + 4, FONT);
      //      this.drawCenteredString(this.fontRenderer, p.alwaysOn + "",
      //          x + 25, y + 4, FONT);
      // ADD BUTTON
      x += 54;
      GuiCableButton btnOnOff = new GuiCableButton(CableDataMessage.PRIORITY_DOWN,
          x + spacer, y, "1/0");
      this.addButton(btnOnOff);
      GuiCableButton btnMinus = new GuiCableButton(CableDataMessage.PRIORITY_DOWN,
          x + 2 * spacer, y, "-");
      this.addButton(btnMinus);
      GuiCableButton btnPlus = new GuiCableButton(CableDataMessage.PRIORITY_DOWN,
          x + 3 * spacer, y, "+");
      this.addButton(btnPlus);
      y += 25;
    }
  }

  private void renderTextures() {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xCenter = (this.width - this.xSize) / 2;
    int yCenter = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xCenter, yCenter, 0, 0, this.xSize, this.ySize);
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
