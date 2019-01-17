package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.IPublicGuiContainer;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import mrriegel.storagenetwork.network.CableLimitMessage;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCable extends GuiCableBase implements IPublicGuiContainer {

  public GuiCable(ContainerCable inventorySlotsIn) {
    super(inventorySlotsIn);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xMiddle = (this.width - this.xSize) / 2;
    int yMiddle = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xMiddle, yMiddle, 0, 0, this.xSize, this.ySize);
    int x = 0, y = 0;
    int u = 176, v = 112;
    //reset sprite u/v
    u = 176;
    v = 34;
    int rows = 9, cols = 2;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        x = xMiddle + 7 + SQ * row + (col / 3) * 108;
        y = yMiddle + 25 + SQ * col;//if col > 3, add jump
        if (col > 2) {
          y -= 3 * SQ;
        }
        this.drawTexturedModalRect(x, y, u, v, SQ, SQ);
      }
    }
    if (tile.isUpgradeable()) {
      for (int ug = 0; ug < ItemUpgrade.NUM; ug++) {
        this.drawTexturedModalRect(xMiddle + 97 + ug * SQ, yMiddle + 5, u, v, SQ, SQ);
      }
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1 && btnOperationToggle != null) {
      btnOperationToggle.enabled = true;
      btnOperationToggle.visible = true;
      this.mc.getTextureManager().bindTexture(texture);
      this.drawTexturedModalRect(xMiddle + 7, yMiddle + 65, u, v, SQ, SQ);//the extra slot
      //also draw textbox
      this.drawTexturedModalRect(xMiddle + 50, yMiddle + 67, 0, 171, TEXTBOX_WIDTH, 12);
      searchBar.drawTextBox();
    }
    else if (btnOperationToggle != null) {
      btnOperationToggle.enabled = false;
      btnOperationToggle.visible = false;
    }
    itemSlotsGhost = Lists.newArrayList();
    rows = 2;
    cols = 9;
    fontRenderer.drawString(String.valueOf(tile.getPriority()),
        guiLeft + 30 - fontRenderer.getStringWidth(String.valueOf(tile.getPriority())) / 2,
        5 + btnMinus.y, 4210752);
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        int index = col + (cols * row);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack stack = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        boolean numShow = tile instanceof TileCable ? tile.getUpgradesOfType(ItemUpgrade.STOCK) > 0
            : false;
        x = 8 + col * SQ;
        y = 26 + row * SQ;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, num, guiLeft, guiTop, numShow));
      }
    }
    for (ItemSlotNetwork s : itemSlotsGhost) {
      s.drawSlot(mouseX, mouseY);
    }
    if (operationItemSlot != null && tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      operationItemSlot.drawSlot(mouseX, mouseY);
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    if (pbtnBottomface != null) {
      EnumFacing f = tile.getFacingBottomRow();
      pbtnBottomface.displayString = f.name().substring(0, 2);
    }
    if (pbtnTopface != null) {
      //      this.tile.getRequest().notifyAll();
      EnumFacing f = tile.getFacingTopRow();
      pbtnTopface.displayString = f.name().substring(0, 2);
    }
  }

  @Override
  public void initGui() {
    super.initGui();
    int x = 0, y = 0;
    btnImport = new GuiCableButton(CableMessageType.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
    btnImport.setCable(tile);
    this.addButton(btnImport);
    btnMinus = new GuiCableButton(CableMessageType.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
    btnMinus.setCable(tile);
    this.addButton(btnMinus);
    btnPlus = new GuiCableButton(CableMessageType.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
    btnPlus.setCable(tile);
    this.addButton(btnPlus);
    btnWhite = new GuiCableButton(CableMessageType.TOGGLE_WHITELIST, guiLeft + 58, guiTop + 5, "");
    btnWhite.setCable(tile);
    this.addButton(btnWhite);
    btnWhite.visible = tile.getBlockType() != ModBlocks.exKabel;
    if (tile.isStorage()) {
      btnInputOutputStorage = new GuiCableButton(CableMessageType.TOGGLE_WAY, guiLeft + 115, guiTop + 5, "");
      btnInputOutputStorage.setCable(tile);
      this.addButton(btnInputOutputStorage);
    }
    else {
      Keyboard.enableRepeatEvents(true);
      searchBar = new GuiTextField(99, fontRenderer, guiLeft + 54, guiTop + 69, TEXTBOX_WIDTH, fontRenderer.FONT_HEIGHT);
      searchBar.setMaxStringLength(3);
      searchBar.setEnableBackgroundDrawing(false);
      searchBar.setVisible(true);
      searchBar.setTextColor(16777215);
      searchBar.setCanLoseFocus(false);
      searchBar.setFocused(true);
      searchBar.setText(tile.getLimit() + "");
      searchBar.width = 20;
      btnOperationToggle = new GuiCableButton(CableMessageType.TOGGLE_MODE, guiLeft + 28, guiTop + 66, "");
      //      btnOperationToggle = new Button(4, guiLeft + 60, guiTop + 64, "");
      btnOperationToggle.setCable(tile);
      this.addButton(btnOperationToggle);
      operationItemSlot = new ItemSlotNetwork(this, tile.getOperationStack(), guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false);
    }
    x = 88;
    y = 62;
    checkOreBtn = new GuiCheckBox(10, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.ore"), true);
    checkOreBtn.setIsChecked(tile.getOre());
    this.addButton(checkOreBtn);
    y += 12;
    checkMetaBtn = new GuiCheckBox(11, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.meta"), true);
    checkMetaBtn.setIsChecked(tile.getMeta());
    this.addButton(checkMetaBtn);
    // 
    x += 50;
    checkboxNBT = new GuiCheckBox(12, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.nbt"), true);
    checkboxNBT.setIsChecked(tile.getNbt());
    this.addButton(checkboxNBT);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      String s = "";
      if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
        s = searchBar.getText();
      }
      if ((tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) && this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
        if (!StringUtils.isNumeric(searchBar.getText()) && !searchBar.getText().isEmpty()) searchBar.setText(s);
        int num = 0;
        try {
          num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
        }
        catch (Exception e) {
          searchBar.setText("0");
        }
        tile.setLimit(num);
        PacketRegistry.INSTANCE.sendToServer(new CableLimitMessage(num, tile.getPos(), operationItemSlot.getStack()));
      }
      else {
        super.keyTyped(typedChar, keyCode);
      }
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
