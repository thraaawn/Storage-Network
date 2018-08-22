package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.gui.GuiContainerBase;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.FilterMessage;
import mrriegel.storagenetwork.network.LimitMessage;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiCable extends GuiContainerBase {

  private static final int TEXTBOX_WIDTH = 26;
  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  private GuiCableButton btnPlus, btnMinus, btnWhite, btnOperationToggle, btnImport, btnInputOutputStorage;
  private TileCable tile;
  private GuiTextField searchBar;
  private List<ItemSlotNetwork> list;
  private ItemSlotNetwork operation;
  private GuiCheckBox checkOre;
  private GuiCheckBox checkMeta;

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
    //    if (tile.getBlockType() != ModBlocks.storageKabel) {
    for (int ii = 0; ii < 9; ii++) {
      for (int jj = 0; jj < 2; jj++) {
        this.drawTexturedModalRect(i + 7 + ii * 18, j + 25 + 18 * jj, 176, 34, 18, 18);
      }
    }
    //    }
    if (tile.isUpgradeable()) {
      for (int ii = 0; ii < ItemUpgrade.NUM; ii++) {
        this.drawTexturedModalRect(i + 97 + ii * 18, j + 5, 176, 34, 18, 18);
      }
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1 && btnOperationToggle != null) {
      btnOperationToggle.enabled = true;
      btnOperationToggle.visible = true;
      this.mc.getTextureManager().bindTexture(texture);
      this.drawTexturedModalRect(i + 7, j + 65, 176, 34, 18, 18);//the extra slot
      //also draw textbox
      this.drawTexturedModalRect(i + 50, j + 67, 0, 171, TEXTBOX_WIDTH, 12);
      searchBar.drawTextBox();
    }
    else if (btnOperationToggle != null) {
      btnOperationToggle.enabled = false;
      btnOperationToggle.visible = false;
    }
    //    if (tile.getBlockType() != ModBlocks.storageKabel) {
    list = Lists.newArrayList();
    for (int jj = 0; jj < 2; jj++) {
      for (int ii = 0; ii < 9; ii++) {
        int index = ii + (9 * jj);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack s = wrap == null ? ItemStack.EMPTY : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        boolean numShow = tile instanceof TileCable ? tile.getUpgradesOfType(ItemUpgrade.STOCK) > 0 : false;
        list.add(new ItemSlotNetwork(this, s, guiLeft + 8 + ii * 18, guiTop + 26 + jj * 18, num, guiLeft, guiTop, numShow));
      }
    }
    for (ItemSlotNetwork s : list) {
      s.drawSlot(mouseX, mouseY);
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      operation.drawSlot(mouseX, mouseY);
    }
    //    }
    fontRenderer.drawString(String.valueOf(tile.getPriority()), guiLeft + 30 - fontRenderer.getStringWidth(String.valueOf(tile.getPriority())) / 2, guiTop + 10, 4210752);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
  }

  private void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : list) {
      if (s != null && s.getStack() != null && !s.getStack().isEmpty() && s.isMouseOverSlot(mouseX, mouseY))
        this.renderToolTip(s.getStack(), mouseX, mouseY);
    }
    if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1)
      operation.drawTooltip(mouseX, mouseY);
    if (btnImport != null && btnImport.isMouseOver())
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.gui.import")), mouseX, mouseY);
    if (btnInputOutputStorage != null && btnInputOutputStorage.isMouseOver())
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.fil.tooltip_" + tile.getWay().toString())), mouseX, mouseY);
    if (mouseX > guiLeft + 20 && mouseX < guiLeft + 50 && mouseY > guiTop + 2 && mouseY < guiTop + 30)
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.priority")), mouseX, mouseY, fontRenderer);
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
      this.addButton(btnInputOutputStorage);
    }
    if (tile.isStorage() == false) {
      Keyboard.enableRepeatEvents(true);
      searchBar = new GuiTextField(99, fontRenderer, guiLeft + 54, guiTop + 69,
          TEXTBOX_WIDTH, fontRenderer.FONT_HEIGHT);
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
      this.addButton(btnOperationToggle);
      operation = new ItemSlotNetwork(this, tile.getOperationStack(), guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false);
      //      
      checkOre = new GuiCheckBox(10, guiLeft + 78, guiTop + 64, I18n.format("gui.storagenetwork.checkbox.ore"), true);
      checkOre.setIsChecked(tile.getOre());
      this.addButton(checkOre);
      checkMeta = new GuiCheckBox(11, guiLeft + 78, guiTop + 76, I18n.format("gui.storagenetwork.checkbox.meta"), true);
      checkMeta.setIsChecked(tile.getMeta());
      this.addButton(checkMeta);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (operation != null && operation.isMouseOverSlot(mouseX, mouseY) && tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      tile.setOperationStack(mc.player.inventory.getItemStack());
      operation.setStack(mc.player.inventory.getItemStack());
      int num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
      PacketRegistry.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), mc.player.inventory.getItemStack()));
      return;
    }
    for (int i = 0; i < list.size(); i++) {
      ItemSlotNetwork e = list.get(i);
      if (e.isMouseOverSlot(mouseX, mouseY)) {
        ContainerCable con = (ContainerCable) inventorySlots;
        StackWrapper x = con.getTile().getFilter().get(i);
        if (mc.player.inventory.getItemStack() != null) {
          if (!con.isInFilter(new StackWrapper(mc.player.inventory.getItemStack(), 1))) {
            con.getTile().getFilter().put(i, new StackWrapper(mc.player.inventory.getItemStack(), mc.player.inventory.getItemStack().getCount()));
          }
        }
        else {
          if (x != null) {
            if (mouseButton == 0)
              x.setSize(x.getSize() + (isShiftKeyDown() ? 10 : 1));
            else if (mouseButton == 1)
              x.setSize(x.getSize() - (isShiftKeyDown() ? 10 : 1));
            else if (mouseButton == 2) {
              con.getTile().getFilter().put(i, null);
            }
            if (x != null && x.getSize() <= 0) {
              con.getTile().getFilter().put(i, null);
            }
          }
        }
        con.slotChanged();
        PacketRegistry.INSTANCE.sendToServer(new FilterMessage(i, tile.getFilter().get(i), tile.getOre(), tile.getMeta()));
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
      if (tile instanceof TileCable)
        tile.setMode(!tile.isMode());
    }
    else if (checkMeta != null && checkOre != null &&
        (button.id == checkMeta.id || button.id == checkOre.id)) {
      PacketRegistry.INSTANCE.sendToServer(new FilterMessage(-1, null, checkOre.isChecked(), checkMeta.isChecked()));
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    //    if (!(tile instanceof TileCable)) {
    //      super.keyTyped(typedChar, keyCode);
    //      return;
    //    }
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      String s = "";
      if (tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
        s = searchBar.getText();
      }
      if ((tile.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) && this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
        if (!StringUtils.isNumeric(searchBar.getText()) && !searchBar.getText().isEmpty())
          searchBar.setText(s);
        int num = 0;
        try {
          num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
        }
        catch (Exception e) {
          searchBar.setText("0");
        }
        tile.setLimit(num);
        PacketRegistry.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), operation.getStack()));
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
}
