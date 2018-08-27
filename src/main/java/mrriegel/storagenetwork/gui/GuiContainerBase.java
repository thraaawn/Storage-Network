package mrriegel.storagenetwork.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class GuiContainerBase extends GuiContainer {

  public GuiContainerBase(Container inventorySlotsIn) {
    super(inventorySlotsIn);
  }

  @Override
  public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
    super.drawGradientRect(left, top, right, bottom, startColor, endColor);
  }

  public FontRenderer getFont() {
    return this.fontRenderer;
  }

  @Override
  public boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
    return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  @Override
  public void renderToolTip(ItemStack stack, int x, int y) {
    super.renderToolTip(stack, x, y);
  }
}
