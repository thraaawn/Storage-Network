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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCable extends GuiContainer implements IPublicGuiContainer {

  private static final int SQ = 18;
  private static final int TEXTBOX_WIDTH = 26;
  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  private GuiCableButton btnPlus, btnMinus, btnWhite, btnOperationToggle, btnImport, btnInputOutputStorage;
  private TileCable tile;
  private GuiTextField searchBar;
  private List<ItemSlotNetwork> list;
  private ItemSlotNetwork operationItemSlot;
  private GuiCheckBox checkOreBtn;
  private GuiCheckBox checkMetaBtn;

  public GuiCable(ContainerCable inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 171;
    this.tile = inventorySlotsIn.getTile();
    list = Lists.newArrayList();
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

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int i = (this.width - this.xSize) / 2;
    int j = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    int x = 0, y = 0;
    int u = 176, v = 112;
    if (tile.getBlockType() == ModBlocks.processKabel) {
      //TODO: hmm no field. it syncs server-> client only when gui is NOT open 
      if (tile.getRequest().getStatus() == ProcessStatus.EXPORTING) {
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(i + 7, j + 65, u, v, SQ - 8, SQ);//the extra slot
      }
      else if (tile.getRequest().getStatus() == ProcessStatus.IMPORTING) {
        // 
        u = 188;
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(i + 7, j + 65, u, v, SQ - 8, SQ);//the extra slot
      }
    }
    u = 176;
    v = 34;
    for (int ii = 0; ii < 9; ii++) {
      for (int jj = 0; jj < 2; jj++) {
        x = i + 7 + ii * 18;
        y = j + 25 + SQ * jj;
        //        if (jj == 1 && tile.getBlockType() == ModBlocks.processKabel) {
        //          //move space down
        //          y += 6;
        //        }
        this.drawTexturedModalRect(x, y, u, v, SQ, SQ);
      }
    }
    if (tile.isUpgradeable()) {
      for (int ii = 0; ii < ItemUpgrade.NUM; ii++) {
        this.drawTexturedModalRect(i + 97 + ii * SQ, j + 5, u, v, SQ, SQ);
      }
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1 && btnOperationToggle != null) {
      btnOperationToggle.enabled = true;
      btnOperationToggle.visible = true;
      this.mc.getTextureManager().bindTexture(texture);
      this.drawTexturedModalRect(i + 7, j + 65, u, v, SQ, SQ);//the extra slot
      //also draw textbox
      this.drawTexturedModalRect(i + 50, j + 67, 0, 171, TEXTBOX_WIDTH, 12);
      searchBar.drawTextBox();
    }
    else if (btnOperationToggle != null) {
      btnOperationToggle.enabled = false;
      btnOperationToggle.visible = false;
    }
    list = Lists.newArrayList();
    for (int row = 0; row < 2; row++) {
      for (int col = 0; col < 9; col++) {
        int index = col + (9 * row);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack s = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        boolean numShow = tile instanceof TileCable ? tile.getUpgradesOfType(ItemUpgrade.STOCK) > 0
            || tile.getBlockType() == ModBlocks.processKabel
            : false;
        //actually
        list.add(new ItemSlotNetwork(this, s, guiLeft + 8 + col * SQ, guiTop + 26 + row * SQ, num, guiLeft, guiTop, numShow));
      }
    }
    for (ItemSlotNetwork s : list) {
      s.drawSlot(mouseX, mouseY);
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      operationItemSlot.drawSlot(mouseX, mouseY);
    }

      fontRenderer.drawString(String.valueOf(tile.getPriority()), guiLeft + 30 - fontRenderer.getStringWidth(String.valueOf(tile.getPriority())) / 2, guiTop + 10, 4210752);
  }

  private void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : list) {
      if (s != null && s.getStack() != null && !s.getStack().isEmpty() && s.isMouseOverSlot(mouseX, mouseY)) this.renderToolTip(s.getStack(), mouseX, mouseY);
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) operationItemSlot.drawTooltip(mouseX, mouseY);
    if (btnImport != null && btnImport.isMouseOver()) drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.gui.import")), mouseX, mouseY);
    if (btnInputOutputStorage != null && btnInputOutputStorage.isMouseOver()) drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.fil.tooltip_" + tile.getWay().toString())), mouseX, mouseY);
    if (mouseX > guiLeft + 20 && mouseX < guiLeft + 50 && mouseY > guiTop + 2 && mouseY < guiTop + 30) this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority")), mouseX, mouseY, fontRenderer);
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
    if (btnOperationToggle != null && btnOperationToggle.isMouseOver()) {
      String s = I18n.format("gui.storagenetwork.operate.tooltip", I18n.format("gui.storagenetwork.operate.tooltip." + (tile.isMode() ? "more" : "less")), tile.getLimit(), tile.getOperationStack() != null ? tile.getOperationStack().getDisplayName() : "Items");
      //   String s = I18n.format("gui.storagenetwork.operate.tooltip");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
    }
  }

  @Override
  public void initGui() {
    super.initGui();
    if (tile.getBlockType() == ModBlocks.processKabel) {
      //custom buttonies 
      btnMinus = new GuiCableButton(CableDataMessage.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
      btnMinus.setCable(tile);
      this.addButton(btnMinus);
      btnPlus = new GuiCableButton(CableDataMessage.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
      btnPlus.setCable(tile);
      this.addButton(btnPlus);
    }
    else {
      btnMinus = new GuiCableButton(CableDataMessage.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
      btnMinus.setCable(tile);
      this.addButton(btnMinus);
      btnPlus = new GuiCableButton(CableDataMessage.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
      btnPlus.setCable(tile);
      this.addButton(btnPlus);
      btnImport = new GuiCableButton(CableDataMessage.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
      btnImport.setCable(tile);
      this.addButton(btnImport);
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
        //      
        checkOreBtn = new GuiCheckBox(10, guiLeft + 78, guiTop + 64, I18n.format("gui.storagenetwork.checkbox.ore"), true);
        checkOreBtn.setIsChecked(tile.getOre());
        this.addButton(checkOreBtn);
        checkMetaBtn = new GuiCheckBox(11, guiLeft + 78, guiTop + 76, I18n.format("gui.storagenetwork.checkbox.meta"), true);
        checkMetaBtn.setIsChecked(tile.getMeta());
        this.addButton(checkMetaBtn);
      }
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
    for (int i = 0; i < list.size(); i++) {
      ItemSlotNetwork itemSlot = list.get(i);
      if (itemSlot.isMouseOverSlot(mouseX, mouseY)) {
        ContainerCable container = (ContainerCable) inventorySlots;
        StackWrapper stackWrapper = container.getTile().getFilter().get(i);
        if (!stackCarriedByMouse.isEmpty() && !container.isInFilter(new StackWrapper(stackCarriedByMouse, 1))) {
          container.getTile().getFilter().put(i, new StackWrapper(stackCarriedByMouse, stackCarriedByMouse.getCount()));
        }
        else {
          if (stackWrapper != null) {
            if (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT || stackWrapper.getSize() <= 0) {
              container.getTile().getFilter().put(i, null);
              //              stackWrapper.setSize(stackWrapper.getSize() + (isShiftKeyDown() ? 10 : 1));
            }
            else if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
              stackWrapper.setSize(stackWrapper.getSize() - (isShiftKeyDown() ? 10 : 1));
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
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    if (btnMinus != null && button.id == btnMinus.id) {
      tile.setPriority(tile.getPriority() - 1);
    }
    else if (btnPlus != null && button.id == btnPlus.id) {
      tile.setPriority(tile.getPriority() + 1);
    }
    else if (btnWhite != null && button.id == btnWhite.id) {
      tile.setWhite(!tile.isWhitelist());
    }
    else if (btnOperationToggle != null && button.id == btnOperationToggle.id) {
      if (tile instanceof TileCable) tile.setMode(!tile.isMode());
    }
    else if (checkMetaBtn != null && checkOreBtn != null && (button.id == checkMetaBtn.id || button.id == checkOreBtn.id)) {
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(-1, null, checkOreBtn.isChecked(), checkMetaBtn.isChecked()));
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
