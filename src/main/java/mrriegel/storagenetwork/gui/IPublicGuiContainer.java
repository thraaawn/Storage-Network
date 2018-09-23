package mrriegel.storagenetwork.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

/**
 * These methods are for compat with FastBench, they were originally overrides in ContainerNetworkBase. Some names are strange to not conflict with the reobf process.
 * 
 * @author Shadows
 *
 */
public interface IPublicGuiContainer {

  public void drawGradientRectP(int left, int top, int right, int bottom, int startColor, int endColor);

  public FontRenderer getFont();

  public boolean isPointInRegionP(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY);

  public void renderToolTipP(ItemStack stack, int x, int y);
}
