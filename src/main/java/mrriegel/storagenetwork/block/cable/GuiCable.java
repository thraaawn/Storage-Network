package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel.ProcessStatus;
import mrriegel.storagenetwork.gui.IPublicGuiContainer;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableFilterMessage;
import mrriegel.storagenetwork.network.CableLimitMessage;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCable extends GuiContainer implements IPublicGuiContainer {

  private static final int PROCESS_SPACING = 42;
  private static final int SQ = 18;
  private static final int TEXTBOX_WIDTH = 26;
  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  private GuiCableButton btnPlus, btnMinus, btnWhite, btnOperationToggle, btnImport, btnInputOutputStorage;
  private TileCable tile;
  private GuiTextField searchBar;
  private List<ItemSlotNetwork> itemSlotsGhost;
  private ItemSlotNetwork operationItemSlot;
  private GuiCheckBox checkOreBtn;
  private GuiCheckBox checkMetaBtn;
  private GuiCableButton pbtnReset;
  private GuiCableButton pbtnBottomface;
  private GuiCableButton pbtnTopface;

  public GuiCable(ContainerCable inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 171;
    this.tile = inventorySlotsIn.getTile();
    itemSlotsGhost = Lists.newArrayList();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    super.renderHoveredToolTip(mouseX, mouseY);
    drawTooltips(mouseX, mouseY);
  }

  @Override
  public void drawBackground(int tint) {
    super.drawBackground(tint);
  }

  public final static int FONTCOLOR = 4210752;

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
    if (tile.getBlockType() == ModBlocks.processKabel) {
      // one texture per row
      //RED IS output, GREEN is input
      //RED output Means that you can pull out of chest into network, but not put in
      //GREEN input means yes you can also put into chest from network
      //this first TOP texture is green
      int col = -3;
      this.drawTexturedModalRect(xMiddle + col, yMiddle + 23, u, v, SQ - 8, SQ);
      //move over on spritesheet and down on gui
      u = 188;
      //this bottom one is RED
      //      this.mc.getTextureManager().bindTexture(texture);
      //PROCESS_SPAPROCESS_SPACINGCING
      this.drawTexturedModalRect(xMiddle + col, yMiddle + 26 + PROCESS_SPACING, u, v, SQ - 8, SQ);
    }
    //reset sprite u/v
    u = 176;
    v = 34;
    int rows = 9, cols = 2;
    if (tile.getBlockType() == ModBlocks.processKabel) {
      // 
      rows = 3;
      cols = 6;//3 on each side
    }
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
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        int index = col + (cols * row);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack stack = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        boolean numShow = tile instanceof TileCable ? tile.getUpgradesOfType(ItemUpgrade.STOCK) > 0
            || tile.getBlockType() == ModBlocks.processKabel
            : false;
        x = 8 + col * SQ;
        y = 26 + row * SQ;
        if (tile.getBlockType() == ModBlocks.processKabel) {
          x = 8 + SQ * row + (col / 3) * 108;
          y = 26 + SQ * col;//if col > 3, add jump
          if (col > 2) {
            y -= 3 * SQ;
          }
        }
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, num, guiLeft, guiTop, numShow));
      }
    }
    for (ItemSlotNetwork s : itemSlotsGhost) {
      s.drawSlot(mouseX, mouseY);
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      operationItemSlot.drawSlot(mouseX, mouseY);
    }
    fontRenderer.drawString(String.valueOf(tile.getPriority()),
        guiLeft + 30 - fontRenderer.getStringWidth(String.valueOf(tile.getPriority())) / 2,
        5 + btnMinus.y, 4210752);
  }

  private void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : itemSlotsGhost) {
      if (s != null && s.getStack() != null && !s.getStack().isEmpty() && s.isMouseOverSlot(mouseX, mouseY)) {
        this.renderToolTip(s.getStack(), mouseX, mouseY);
      }
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      operationItemSlot.drawTooltip(mouseX, mouseY);
    }
    if (pbtnReset != null && pbtnReset.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.refresh")), mouseX, mouseY);
    }
    if (btnImport != null && btnImport.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.gui.import")), mouseX, mouseY);
    }
    if (btnInputOutputStorage != null && btnInputOutputStorage.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.fil.tooltip_" + tile.getWay().toString())), mouseX, mouseY);
    }
    if (mouseX > guiLeft + 20 && mouseX < guiLeft + 50 && mouseY > guiTop + 2 && mouseY < guiTop + 30) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority")), mouseX, mouseY, fontRenderer);
    }
    if (btnWhite != null && btnWhite.isMouseOver()) {
      String s = tile.isWhitelist() ? I18n.format("gui.storagenetwork.gui.whitelist") : I18n.format("gui.storagenetwork.gui.blacklist");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
    }
    if (btnPlus != null && btnPlus.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.up")), mouseX, mouseY, fontRenderer);
    }
    if (btnMinus != null && btnMinus.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.down")), mouseX, mouseY, fontRenderer);
    }
    if (pbtnTopface != null && pbtnTopface.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.processing.recipe")), mouseX, mouseY, fontRenderer);
      //
    }
    if (pbtnBottomface != null && pbtnBottomface.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.processing.extract")), mouseX, mouseY, fontRenderer);
      //
    }
    if (btnOperationToggle != null && btnOperationToggle.isMouseOver()) {
      String s = I18n.format("gui.storagenetwork.operate.tooltip", I18n.format("gui.storagenetwork.operate.tooltip." + (tile.isMode() ? "more" : "less")), tile.getLimit(), tile.getOperationStack() != null ? tile.getOperationStack().getDisplayName() : "Items");
      //   String s = I18n.format("gui.storagenetwork.operate.tooltip");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
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
    //priority for everything 
    int x = 0, y = 0;
    btnMinus = new GuiCableButton(CableDataMessage.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
    btnMinus.setCable(tile);
    this.addButton(btnMinus);
    btnPlus = new GuiCableButton(CableDataMessage.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
    btnPlus.setCable(tile);
    this.addButton(btnPlus);
    btnImport = new GuiCableButton(CableDataMessage.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
    btnImport.setCable(tile);
    this.addButton(btnImport);
    if (tile.getBlockType() == ModBlocks.processKabel) {
      //move priority over 
      //add custom buttons 
      if (this.tile.getRequest().getStatus() == ProcessStatus.IMPORTING) {
        //a click will swap it to EXPORTING with CableDataMessage 
        pbtnReset = new GuiCableButton(CableDataMessage.TOGGLE_P_RESTARTTRIGGER, guiLeft + 120, guiTop + 4, "R");
        pbtnReset.setCable(tile);
        this.addButton(pbtnReset);
      }
      int column = 76, ctr = 28;
      pbtnBottomface = new GuiCableButton(CableDataMessage.P_FACE_BOTTOM, guiLeft + column + 20, guiTop + ctr, "");
      pbtnBottomface.setCable(tile);
      this.addButton(pbtnBottomface);
      pbtnTopface = new GuiCableButton(CableDataMessage.P_FACE_TOP, guiLeft + column - 12, guiTop + ctr, "");
      pbtnTopface.setCable(tile);
      this.addButton(pbtnTopface);
    }
    else {

      btnWhite = new GuiCableButton(CableDataMessage.TOGGLE_WHITELIST, guiLeft + 58, guiTop + 5, "");
      btnWhite.setCable(tile);
      this.addButton(btnWhite);
      btnWhite.visible = tile.getBlockType() != ModBlocks.exKabel;
      if (tile.isStorage()) {
        btnInputOutputStorage = new GuiCableButton(CableDataMessage.TOGGLE_WAY, guiLeft + 115, guiTop + 5, "");
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
        btnOperationToggle = new GuiCableButton(CableDataMessage.TOGGLE_MODE, guiLeft + 28, guiTop + 66, "");
        //      btnOperationToggle = new Button(4, guiLeft + 60, guiTop + 64, "");
        btnOperationToggle.setCable(tile);
        this.addButton(btnOperationToggle);
        operationItemSlot = new ItemSlotNetwork(this, tile.getOperationStack(), guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false);
      }
    }
    if (tile.isStorage() == false) {
      x = 88;
      y = 62;
      if (tile.getBlockType() == ModBlocks.processKabel) {
        x = 72;
        y = 62;
      }
      checkOreBtn = new GuiCheckBox(10, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.ore"), true);
      checkOreBtn.setIsChecked(tile.getOre());
      this.addButton(checkOreBtn);
      y += 12;
      checkMetaBtn = new GuiCheckBox(11, guiLeft + x, guiTop + y, I18n.format("gui.storagenetwork.checkbox.meta"), true);
      checkMetaBtn.setIsChecked(tile.getMeta());
      this.addButton(checkMetaBtn);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    ItemStack stackCarriedByMouse = mc.player.inventory.getItemStack().copy();
    if (operationItemSlot != null && operationItemSlot.isMouseOverSlot(mouseX, mouseY) && tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      tile.setOperationStack(stackCarriedByMouse);
      operationItemSlot.setStack(stackCarriedByMouse);
      int num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
      PacketRegistry.INSTANCE.sendToServer(new CableLimitMessage(num, tile.getPos(), stackCarriedByMouse));
      return;
    }
    boolean isRightClick = mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT;
    boolean isLeftClick = mouseButton == UtilTileEntity.MOUSE_BTN_LEFT;
    boolean isMiddleClick = mouseButton == UtilTileEntity.MOUSE_BTN_MIDDLE_CLICK;
    for (int i = 0; i < itemSlotsGhost.size(); i++) {
      ItemSlotNetwork itemSlot = itemSlotsGhost.get(i);
      if (itemSlot.isMouseOverSlot(mouseX, mouseY)) {
        ContainerCable container = (ContainerCable) inventorySlots;
        StackWrapper stackWrapper = container.getTile().getFilter().get(i);
        boolean doesExistAlready = container.isInFilter(new StackWrapper(stackCarriedByMouse, 1));
        //        if (tile.getBlockType() == ModBlocks.processKabel) {
        if (!stackCarriedByMouse.isEmpty() && !doesExistAlready) {
          int quantity = (isRightClick) ? 1 : stackCarriedByMouse.getCount();
          container.getTile().getFilter().put(i, new StackWrapper(stackCarriedByMouse, quantity));
        }
        else {
          if (stackWrapper != null) {
            if (isLeftClick || stackWrapper.getSize() <= 0) {
              container.getTile().getFilter().put(i, null);
              //              stackWrapper.setSize(stackWrapper.getSize() + (isShiftKeyDown() ? 10 : 1));
            }
            else if (isRightClick) {
              stackWrapper.setSize(stackWrapper.getSize() - 1);
            }
            else if (isMiddleClick) {
              stackWrapper.setSize(stackWrapper.getSize() + 1);
            }
          }
        }
        //        container.slotChanged();
        PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(i, tile.getFilter().get(i), tile.getOre(), tile.getMeta()));
        break;
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    if (btnMinus != null && button.id == btnMinus.id) {
      tile.setPriority(tile.getPriority() - 1);
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
    else if (btnPlus != null && button.id == btnPlus.id) {
      tile.setPriority(tile.getPriority() + 1);
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
    else if (btnWhite != null && button.id == btnWhite.id) {
      tile.setWhite(!tile.isWhitelist());
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
    else if (pbtnTopface != null && button.id == pbtnTopface.id) {
      int newFace = (tile.getFacingTopRow().ordinal() + 1) % EnumFacing.values().length;

      tile.processingTop = EnumFacing.values()[newFace];

      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos(), newFace));

    }
    else if (pbtnBottomface != null && button.id == pbtnBottomface.id) {
      //
      int newFace = (tile.getFacingBottomRow().ordinal() + 1) % EnumFacing.values().length;
      tile.processingBottom = EnumFacing.values()[newFace];
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos(), newFace));

    }
    else if (btnOperationToggle != null && button.id == btnOperationToggle.id) {
      if (tile instanceof TileCable) tile.setMode(!tile.isMode());
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
    else if (checkMetaBtn != null && checkOreBtn != null && (button.id == checkMetaBtn.id || button.id == checkOreBtn.id)) {
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(-1, null, checkOreBtn.isChecked(), checkMetaBtn.isChecked()));
    }
    else {
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
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
  public FontRenderer getFont() {
    return this.fontRenderer;
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
