
package mrriegel.storagenetwork.block.control;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.StorageNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiSliderInteger extends GuiButtonExt {

  private float sliderPosition = 1.0F;
  public boolean isMouseDown;
  private final int min;
  private int max;
  private final GuiControl responder;//was TE

  private boolean appendPlusSignLabel = true;

  /**
   * mimic of net.minecraft.client.gui.GuiSlider; uses integers instead of float
   * 
   * for input responder , we basically just need an IInventory & getPos()
   */
  public GuiSliderInteger(GuiControl guiResponder, int idIn, int x, int y,
      int widthIn, int heightIn,
      final int minIn, final int maxIn) {
    super(idIn, x, y, widthIn, heightIn, "");
    this.updateDisplay();
    responder = guiResponder;
    this.min = minIn;
    this.setMax(maxIn);
    appendPlusSignLabel = (min < 0);//if it can be negative, we should distinguish
    //  this.setSliderValue(responder.getField(responderField), false);
  }

  public void setSliderValue(float value, boolean notifyResponder) {
    this.sliderPosition = (value - this.min) / (this.getMax() - this.min);
    this.updateDisplay();
    if (notifyResponder) {
      notifyResponder();
    }
  }

  private void notifyResponder() {
    int val = (int) this.getSliderValue();

    this.responder.setPage(val);
    //    ModCyclic.network.sendToServer(new PacketTileSetField(this.responder.getPos(), this.responderField, val));
  }

  public float getSliderValue() {
    float val = this.min + (this.getMax() - this.min) * this.sliderPosition;
    return MathHelper.floor(val);
  }

  private void updateDisplay() {
    int val = (int) this.getSliderValue();
    if (val > 0 && appendPlusSignLabel) {
      this.displayString = "+" + val;
    }
    else {
      // zero is just "0", negavite sign is automatic
      this.displayString = "" + val;
    }
  }

  public void setTooltip(final String t) {
    List<String> remake = new ArrayList<String>();
    remake.add(StorageNetwork.lang(t));
    tooltip = remake;
  }

  private List<String> tooltip = new ArrayList<String>();

  public List<String> getTooltips() {
    return tooltip;
  }

  @Override
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY)) {
      setSliderPosFromMouse(mouseX, mouseY);
      this.notifyResponder();
      // this.updateDisplay();
      this.isMouseDown = true;
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    super.mouseDragged(mc, mouseX, mouseY);
    if (this.visible) {

      if (this.isMouseDown) {
        setSliderPosFromMouse(mouseX, mouseY);
        this.updateDisplay();
        this.notifyResponder();
      }
      //  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawTexturedModalRect(
          this.x,
          this.y + (int) (this.sliderPosition * (this.height - 8)),
          188, 69, width, 8);
    }
  }

  private void setSliderPosFromMouse(int mouseX, int mouseY) {
    float relativePos = (float) (mouseY - (this.y + 4)) / (float) (this.height - 8);
    this.sliderPosition = relativePos;
    if (this.sliderPosition < 0.0F) {
      this.sliderPosition = 0.0F;
    }
    if (this.sliderPosition > 1.0F) {
      this.sliderPosition = 1.0F;
    }
  }

  @Override
  public void mouseReleased(int mouseX, int mouseY) {
    super.mouseReleased(mouseX, mouseY);
    this.isMouseDown = false;
  }

  @Override
  protected int getHoverState(boolean mouseOver) {
    return 0;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

}
