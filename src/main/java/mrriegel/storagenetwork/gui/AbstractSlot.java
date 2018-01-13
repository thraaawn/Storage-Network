package mrriegel.storagenetwork.gui;

import net.minecraft.client.Minecraft;

public abstract class AbstractSlot {
  public int x, y, size, guiLeft, guiTop;
  public boolean number, square, smallFont, toolTip;
  protected Minecraft mc;
  public AbstractSlot(int x, int y, int size, int guiLeft, int guiTop, boolean number, boolean square, boolean smallFont, boolean toolTip) {
    super();
    this.x = x;
    this.y = y;
    this.size = size;
    this.guiLeft = guiLeft;
    this.guiTop = guiTop;
    this.number = number;
    this.square = square;
    this.smallFont = smallFont;
    this.toolTip = toolTip;
    mc = Minecraft.getMinecraft();
  }
  public abstract boolean isMouseOverSlot(int mouseX, int mouseY) ;
  public abstract void drawSlot(int mx, int my);
  public abstract void drawTooltip(int mx, int my);
}
