package mrriegel.storagenetwork.block.control;

import mrriegel.storagenetwork.StorageNetwork;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiControl extends GuiContainer {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request_full.png");
  private TileControl tile;

  public GuiControl(ContainerControl inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    tile = inventorySlotsIn.getTileRequest();
  }

  @Override
  public void initGui() {
    super.initGui();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
  }
  //  @Override
  //  public BlockPos getPos() {
  //    return tile.getPos();
  //  }
  //
  //  @Override
  //  protected int getDim() {
  //    return tile.getWorld().provider.getDimension();
  //  }
  //
  //  @Override
  //  protected boolean isScreenValid() {
  //    return true;
  //  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    renderTextures();
  }

  private void renderTextures() {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xCenter = (this.width - this.xSize) / 2;
    int yCenter = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xCenter, yCenter, 0, 0, this.xSize, this.ySize);
  }
}
