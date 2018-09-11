package mrriegel.storagenetwork.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public interface IPublicGuiContainer {

	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);

	public FontRenderer getFont();

	public boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY);

	public void renderToolTip(ItemStack stack, int x, int y);

}
