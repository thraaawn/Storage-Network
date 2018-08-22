package mrriegel.storagenetwork.gui;

import javax.annotation.Nonnull;
import mrriegel.storagenetwork.util.UtilInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/**
 * used as the MAIN grid in the network item display
 * 
 * also as ghost/filter items in the cable filter slots
 * 
 * @author
 *
 */
public class ItemSlotNetwork {

  public int x, y, size, guiLeft, guiTop;
  public boolean number, square, smallFont, toolTip;
  protected Minecraft mc;
  protected GuiContainerBase parent;

  public ItemStack stack;

  public ItemSlotNetwork(GuiContainerBase parent, @Nonnull ItemStack stack, int x, int y, int size, int guiLeft, int guiTop, boolean number, boolean square, boolean smallFont, boolean toolTip) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.guiLeft = guiLeft;
    this.guiTop = guiTop;
    this.number = number;
    this.square = square;
    this.smallFont = smallFont;
    this.toolTip = toolTip;
    this.parent = parent;
    mc = Minecraft.getMinecraft();
    this.stack = stack;
  }

  public boolean isMouseOverSlot(int mouseX, int mouseY) {
    return parent.isPointInRegion(x - guiLeft, y - guiTop, 16, 16, mouseX, mouseY);
  }

  public void drawSlot(int mx, int my) {
    GlStateManager.pushMatrix();
    if (!stack.isEmpty()) {
      RenderHelper.enableGUIStandardItemLighting();
      mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
      String amount = UtilInventory.formatLargeNumber(size);
      if (number) {
        if (smallFont) {
          GlStateManager.pushMatrix();
          GlStateManager.scale(.5f, .5f, .5f);
          mc.getRenderItem().renderItemOverlayIntoGUI(parent.getFont(), stack, x * 2 + 16, y * 2 + 16, amount);
          GlStateManager.popMatrix();
        }
        else {
          mc.getRenderItem().renderItemOverlayIntoGUI(parent.getFont(), stack, x, y, amount);
        }
      }
    }
    if (square && this.isMouseOverSlot(mx, my)) {
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      int j1 = x;
      int k1 = y;
      GlStateManager.colorMask(true, true, true, false);
      parent.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
      GlStateManager.colorMask(true, true, true, true);
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
    }
    GlStateManager.popMatrix();
  }

  public void drawTooltip(int mx, int my) {
    if (toolTip && this.isMouseOverSlot(mx, my) && !stack.isEmpty()) {
      parent.renderToolTip(stack, mx, my);
    }
  }
}
