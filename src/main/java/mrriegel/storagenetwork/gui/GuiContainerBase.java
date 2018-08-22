package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.util.UtilInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class GuiContainerBase extends GuiContainer {

  public GuiContainerBase(Container inventorySlotsIn) {
    super(inventorySlotsIn);
  }

  public class ItemSlotNetwork extends AbstractSlot {

    public ItemStack stack;

    public ItemSlotNetwork(ItemStack stack, int x, int y, int size, int guiLeft, int guiTop, boolean number, boolean square, boolean smallFont, boolean toolTip) {
      super(x, y, size, guiLeft, guiTop, number, square, smallFont, toolTip);
      this.stack = stack;
    }

    @Override
    public boolean isMouseOverSlot(int mouseX, int mouseY) {
      return isPointInRegion(x - guiLeft, y - guiTop, 16, 16, mouseX, mouseY);
    }

    @Override
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
            mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, stack, x * 2 + 16, y * 2 + 16, amount);
            GlStateManager.popMatrix();
          }
          else {
            mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, stack, x, y, amount);
          }
        }
      }
      if (square && this.isMouseOverSlot(mx, my)) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int j1 = x;
        int k1 = y;
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
      GlStateManager.popMatrix();
    }

    @Override
    public void drawTooltip(int mx, int my) {
      if (toolTip && this.isMouseOverSlot(mx, my) && stack != null && !stack.isEmpty()) {
        renderToolTip(stack, mx, my);
      }
    }
  }
}
