package mrriegel.storagenetwork.block.cable;

import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.IPublicGuiContainer;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCableProcessing extends GuiCableBase implements IPublicGuiContainer {

  public GuiCableProcessing(ContainerCable inventorySlotsIn) {
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
    rows = 3;
    cols = 6;//3 on each side
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
    // left side 
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        int index = col + (3 * row);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack stack = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        x = col * SQ + 8;
        y = row * SQ + 26;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, num, guiLeft, guiTop, true));
      }
    }
    //right side
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        int index = 9 + col + (3 * row);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack stack = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        //
        x = col * SQ + 116;
        y = row * SQ + 26;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, num, guiLeft, guiTop, true));
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
      EnumFacing f = tile.getFacingTopRow();
      pbtnTopface.displayString = f.name().substring(0, 2);
    } //if recipe is empty or invalid, tell that
    int x = -90;
    int y = 48;
    if (tile.isBottomEmpty() || tile.isTopEmpty()) {
      ///also tooltip here?  
      x = -102;
      this.drawString("tile.storagenetwork:recipe.invalid", x, y);
      if (tile.isBottomEmpty())
        this.drawString("tile.storagenetwork:recipe.invalidright", x, y += 12);
      if (tile.isTopEmpty())
        this.drawString("tile.storagenetwork:recipe.invalidleft", x, y += 12);
    }
    else {
      this.drawString("tile.storagenetwork:recipe.valid",
          x, y);
    }
    ProcessRequestModel p = tile.getProcessModel();
    x = -90;
    y = 4;
    this.drawString("tile.storagenetwork:controller.name",
        x, y);
    x += 12;
    y += 18;
    TextFormatting f = (p.isAlwaysActive()) ? TextFormatting.GREEN
        : TextFormatting.BLUE;
    String txt = StorageNetwork.lang("processing.alwayson." + p.isAlwaysActive());
    if (!p.isAlwaysActive()) {
      txt += p.getCount();
    }
    this.drawString(f + txt, x, y);
  }

  @Override
  public void initGui() {
    super.initGui();
    int x = 0, y = 0;
    //we need some way to let players know if recipe is invalid
    //    buttonRecipe = new GuiCableButton(CableMessageType.TOGGLE_P_RESTARTTRIGGER, guiLeft + 5, guiTop + 5, "S");
    //    buttonRecipe.setCable(tile);
    //    this.addButton(buttonRecipe);
    btnImport = new GuiCableButton(CableMessageType.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
    btnImport.setCable(tile);
    this.addButton(btnImport);
    btnImport.x += 56;
    //move priority over 
    //add custom buttons 
    //a click will swap it to EXPORTING with CableDataMessage 
    pbtnReset = new GuiCableButton(CableMessageType.TOGGLE_P_RESTARTTRIGGER, guiLeft + 154, guiTop + 5, "R");
    pbtnReset.setCable(tile);
    this.addButton(pbtnReset);
    int column = 76, ctr = 24;
    pbtnBottomface = new GuiCableButton(CableMessageType.P_FACE_BOTTOM, guiLeft + column + 20, guiTop + ctr, "");
    pbtnBottomface.setCable(tile);
    this.addButton(pbtnBottomface);
    pbtnTopface = new GuiCableButton(CableMessageType.P_FACE_TOP, guiLeft + column - 12, guiTop + ctr, "");
    pbtnTopface.setCable(tile);
    this.addButton(pbtnTopface);
    x = 64;
    y = 62;
    checkOreBtn = new GuiCheckBox(10, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.ore"), true);
    checkOreBtn.setIsChecked(tile.getOre());
    this.addButton(checkOreBtn);
    y += 12;
    checkMetaBtn = new GuiCheckBox(11, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.meta"), true);
    checkMetaBtn.setIsChecked(tile.getMeta());
    this.addButton(checkMetaBtn);
    //
    y -= 24;
    checkboxNBT = new GuiCheckBox(12, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.nbt"), true);
    checkboxNBT.setIsChecked(tile.getNbt());
    this.addButton(checkboxNBT);
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
