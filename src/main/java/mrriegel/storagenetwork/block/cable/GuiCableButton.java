package mrriegel.storagenetwork.block.cable;

import org.lwjgl.opengl.GL11;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class GuiCableButton extends GuiButton {

  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");

  private Consumer<GuiCableButton> customDrawMethod;

  public GuiCableButton(CableMessageType id, int x, int y, String z) {
    super(id.ordinal(), x, y, 16, 16, z);
  }

  public GuiCableButton(CableMessageType id, int x, int y, int w, int h, String z) {
    super(id.ordinal(), x, y, w, h, z);
  }

  public void setCustomDrawMethod(Consumer<GuiCableButton> customDrawMethod) {
    this.customDrawMethod = customDrawMethod;
  }

  @Override
  public void drawButton(Minecraft mcc, int x, int y, float p) {
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

      if(customDrawMethod != null) {
        customDrawMethod.accept(this);
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
}
