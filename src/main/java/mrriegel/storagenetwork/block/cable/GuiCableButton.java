package mrriegel.storagenetwork.block.cable;

import org.lwjgl.opengl.GL11;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiCableButton extends GuiButton {

  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  private TileCable cable;

  public GuiCableButton(CableMessageType id, int x, int y, String z) {
    super(id.ordinal(), x, y, 16, 16, z);
  }

  public GuiCableButton(CableMessageType id, int x, int y, int w, int h, String z) {
    super(id.ordinal(), x, y, w, h, z);
  }

  @Override
  public void drawButton(Minecraft mcc, int x, int y, float p) {//drawButon
    if (this.visible) {
      FontRenderer fontrenderer = mcc.fontRenderer;
      mcc.getTextureManager().bindTexture(texture);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
      int k = this.getHoverState(this.hovered);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.blendFunc(770, 771);
      this.drawTexturedModalRect(this.x, this.y, 160 + 16 * k, 52, 16, 16);
      if (id == 3) {
        if (cable.isWhitelist())
          this.drawTexturedModalRect(this.x + 1, this.y + 3, 176, 83, 13, 10);
        else
          this.drawTexturedModalRect(this.x + 1, this.y + 3, 190, 83, 13, 10);
      }
      if (id == 4) {
        if (cable.isMode())
          this.displayString = ">";//   this.drawTexturedModalRect(this.x + 0, this.y + 0, 176, 94, 16, 15);
        else
          this.displayString = "<";//    this.drawTexturedModalRect(this.x + 0, this.y + 0, 176 + 16, 94, 16, 15);
      }
      if (id == 6) {
        this.drawTexturedModalRect(this.x + 2, this.y + 2, 176 + cable.getWay().ordinal() * 12, 114, 12, 12);
      }
      this.mouseDragged(mcc, x, y);
      int l = 14737632;
      if (packedFGColour != 0) {
        l = packedFGColour;
      }
      else if (!this.enabled) {
        l = 10526880;
      }
      else if (this.hovered) {
        l = 16777120;
      }
      this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
    }
  }

  public TileCable getCable() {
    return cable;
  }

  public void setCable(TileCable cable) {
    this.cable = cable;
  }
}
