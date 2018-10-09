package mrriegel.storagenetwork.block.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;


public class GuiTextFieldProcCable extends GuiTextField {

  public GuiTextFieldProcCable(int componentId, FontRenderer fontrendererObj, int x, int y) {
    super(componentId, fontrendererObj, x, y, 22, fontrendererObj.FONT_HEIGHT);
  }
}
