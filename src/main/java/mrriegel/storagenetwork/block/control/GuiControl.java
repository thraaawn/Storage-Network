package mrriegel.storagenetwork.block.control;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.StorageNetwork;
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
    int x, y;
    for (ProcessWrapper p : processors) {
      //draw me 
      x = guiLeft + 30;
      y = guiTop + 8;
      /// TODO target blockname  text
      //AND OR  recipe ing list as text 
      this.drawCenteredString(this.fontRenderer, p.alwaysOn + ":" + p.currentRequests,
          x - 12, y + 3, FONT);
      mc.getRenderItem().renderItemAndEffectIntoGUI(p.output, x, y);
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
