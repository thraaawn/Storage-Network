package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.gui.IPublicGuiContainer;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.inventory.FilterItemStackHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCable extends GuiCableBase implements IPublicGuiContainer {


  protected GuiCableButton btnPlus, btnMinus, btnWhite;

  public GuiCable(ContainerCable containerCable) {
    super(containerCable);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xMiddle = (this.width - this.xSize) / 2;
    int yMiddle = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xMiddle, yMiddle, 0, 0, this.xSize, this.ySize);

    int u = 176, v = 34;

    // Draw ghost slots
    int rows = 9, cols = 2;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        int x = xMiddle + 7 + SLOT_SIZE * row + (col / 3) * 108;
        int y = yMiddle + 25 + SLOT_SIZE * col;//if col > 3, add jump
        if (col > 2) {
          y -= 3 * SLOT_SIZE;
        }
        this.drawTexturedModalRect(x, y, u, v, SLOT_SIZE, SLOT_SIZE);
      }
    }
  }

  @Override
  public void initGui() {
    super.initGui();

    btnImport = new GuiCableButton(CableMessageType.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
    this.addButton(btnImport);

    btnMinus = new GuiCableButton(CableMessageType.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
    this.addButton(btnMinus);

    btnPlus = new GuiCableButton(CableMessageType.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
    this.addButton(btnPlus);

    btnWhite = new GuiCableButton(CableMessageType.TOGGLE_WHITELIST, guiLeft + 58, guiTop + 5, "");
    this.addButton(btnWhite);

    btnWhite.visible = containerCable.tile.getBlockType() != ModBlocks.exKabel;

    int x = 88;
    int y = 62;
    checkOreBtn = new GuiCheckBox(10, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.ore"), true);
    this.addButton(checkOreBtn);

    y += 12;
    checkMetaBtn = new GuiCheckBox(11, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.meta"), true);
    this.addButton(checkMetaBtn);

    x += 50;
    checkNbtBtn = new GuiCheckBox(12, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.nbt"), true);
    this.addButton(checkNbtBtn);
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);

    FilterItemStackHandler filterHandler = getFilterHandler();
    if(filterHandler == null) {
      return;
    }

    if (button.id == btnWhite.id) {
      filterHandler.isWhitelist = !filterHandler.isWhitelist;
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id));
    }
  }

  @Override
  protected void drawTooltips(int mouseX, int mouseY) {
    super.drawTooltips(mouseX, mouseY);

    FilterItemStackHandler filterHandler = getFilterHandler();
    if(filterHandler != null && btnWhite != null && btnWhite.isMouseOver()) {
      String s = filterHandler.isWhitelist ? I18n.format("gui.storagenetwork.gui.whitelist") : I18n.format("gui.storagenetwork.gui.blacklist");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
    }

    if (btnPlus != null && btnPlus.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.up")), mouseX, mouseY, fontRenderer);
    }
    if (btnMinus != null && btnMinus.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.down")), mouseX, mouseY, fontRenderer);
    }

  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawGradientRectP(int left, int top, int right, int bottom, int startColor, int endColor) {
    super.drawGradientRect(left, top, right, bottom, startColor, endColor);
  }

  @Override
  public boolean isPointInRegionP(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
    return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  @Override
  public void renderToolTipP(ItemStack stack, int x, int y) {
    super.renderToolTip(stack, x, y);
  }
}
