package mrriegel.storagenetwork.cable;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.cable.TileCable.CableKind;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.GuiContainerBase;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.FilterMessage;
import mrriegel.storagenetwork.network.LimitMessage;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
  CableKind kind;
  Button btnPlus, btnMinus, btnWhite, btnOperationToggle, btnImport, btnInputOutputStorage;
  AbstractFilterTile tile;
  private GuiTextField searchBar;
  List<ItemSlot> list;
  ItemSlot operation;
  private GuiCheckBox checkOre;
  private GuiCheckBox checkMeta;
  public GuiCable(ContainerCable inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 171;
    this.tile = inventorySlotsIn.tile;
    if (tile instanceof TileCable) {
      this.kind = ((TileCable) tile).getKind();
    }
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
    if (tile instanceof TileCable) {
      TileCable cable = (TileCable) tile;
      if (cable.isUpgradeable()) {
        for (int ii = 0; ii < ItemUpgrade.NUM; ii++) {
          this.drawTexturedModalRect(i + 97 + ii * 18, j + 5, 176, 34, 18, 18);
        }
      }
      if (cable.getUpgradesOfType(ItemUpgrade.OPERATION) >= 1 && btnOperationToggle != null) {
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
    }
    //    if (tile.getBlockType() != ModBlocks.storageKabel) {
    list = Lists.newArrayList();
    for (int jj = 0; jj < 2; jj++) {
      for (int ii = 0; ii < 9; ii++) {
        int index = ii + (9 * jj);
        StackWrapper wrap = tile.getFilter().get(index);
        ItemStack s = wrap == null ? null : wrap.getStack();
        int num = wrap == null ? 0 : wrap.getSize();
        boolean numShow = tile instanceof TileCable ? ((TileCable) tile).getUpgradesOfType(ItemUpgrade.STOCK) > 0 : false;
        //   System.out.println("FILTER EH "+s);
        list.add(new ItemSlot(s, guiLeft + 8 + ii * 18, guiTop + 26 + jj * 18, num, guiLeft, guiTop, numShow, true, false, true));
      }
    }
    for (ItemSlot s : list) {
      s.drawSlot(mouseX, mouseY);
    }
    if (tile instanceof TileCable && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
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
    for (ItemSlot s : list) {
      if (s != null && s.stack != null && !s.stack.isEmpty() && s.isMouseOverSlot(mouseX, mouseY))
        this.renderToolTip(s.stack, mouseX, mouseY);
    }
    if (tile instanceof TileCable && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OPERATION) >= 1)
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
      String s = I18n.format("gui.storagenetwork.operate.tooltip", I18n.format("gui.storagenetwork.operate.tooltip." + (((TileCable) tile).isMode() ? "more" : "less")), ((TileCable) tile).getLimit(), ((TileCable) tile).getOperationStack() != null ? ((TileCable) tile).getOperationStack().getDisplayName() : "Items");
      //   String s = I18n.format("gui.storagenetwork.operate.tooltip");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
    }
  }
  @Override
  public void initGui() {
    super.initGui();
    btnMinus = new Button(CableDataMessage.PRIORITY_DOWN, guiLeft + 6, guiTop + 5, "-");
    this.addButton(btnMinus);
    btnPlus = new Button(CableDataMessage.PRIORITY_UP, guiLeft + 37, guiTop + 5, "+");
    this.addButton(btnPlus);
    btnImport = new Button(CableDataMessage.IMPORT_FILTER, guiLeft + 78, guiTop + 5, "I");
    this.addButton(btnImport);
    btnWhite = new Button(CableDataMessage.TOGGLE_WHITELIST, guiLeft + 58, guiTop + 5, "");
    this.addButton(btnWhite);
    btnWhite.visible = tile.getBlockType() != ModBlocks.exKabel;
    if (tile.isStorage()) {
      btnInputOutputStorage = new Button(6, guiLeft + 115, guiTop + 5, "");
      buttonList.add(btnInputOutputStorage);
    }
    if (tile.isStorage() == false) {
      //      btnInputOutputStorage = new Button(6, guiLeft + 115, guiTop + 5, "");
      //      this.addButton(btnInputOutputStorage);
      if (tile instanceof TileCable) {
        TileCable cable = (TileCable) tile;
        Keyboard.enableRepeatEvents(true);
        searchBar = new GuiTextField(99, fontRenderer, guiLeft + 54, guiTop + 69,
            TEXTBOX_WIDTH, fontRenderer.FONT_HEIGHT);
        searchBar.setMaxStringLength(3);
        searchBar.setEnableBackgroundDrawing(false);
        searchBar.setVisible(true);
        searchBar.setTextColor(16777215);
        searchBar.setCanLoseFocus(false);
        searchBar.setFocused(true);
        searchBar.setText(cable.getLimit() + "");
        searchBar.width = 20;
        btnOperationToggle = new Button(CableDataMessage.TOGGLE_MODE, guiLeft + 28, guiTop + 66, "");
        //      btnOperationToggle = new Button(4, guiLeft + 60, guiTop + 64, "");
        this.addButton(btnOperationToggle);
        operation = new ItemSlot(cable.getOperationStack(), guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false, true, false, true);
        //      
        checkOre = new GuiCheckBox(10, guiLeft + 78, guiTop + 64, I18n.format("gui.storagenetwork.checkbox.ore"), true);
        checkOre.setIsChecked(tile.getOre());
        this.addButton(checkOre);
        checkMeta = new GuiCheckBox(11, guiLeft + 78, guiTop + 76, I18n.format("gui.storagenetwork.checkbox.meta"), true);
        checkMeta.setIsChecked(tile.getMeta());
        this.addButton(checkMeta);
      }
    }
  }
  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (operation != null && operation.isMouseOverSlot(mouseX, mouseY) && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
      ((TileCable) tile).setOperationStack(mc.player.inventory.getItemStack());
      operation.stack = mc.player.inventory.getItemStack();
      int num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
      PacketRegistry.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), mc.player.inventory.getItemStack()));
      return;
    }
    for (int i = 0; i < list.size(); i++) {
      ItemSlot e = list.get(i);
      if (e.isMouseOverSlot(mouseX, mouseY)) {
        ContainerCable con = (ContainerCable) inventorySlots;
        StackWrapper x = con.tile.getFilter().get(i);
        if (mc.player.inventory.getItemStack() != null) {
          if (!con.isInFilter(new StackWrapper(mc.player.inventory.getItemStack(), 1))) {
            con.tile.getFilter().put(i, new StackWrapper(mc.player.inventory.getItemStack(), mc.player.inventory.getItemStack().getCount()));
          }
        }
        else {
          if (x != null) {
            if (mouseButton == 0)
              x.setSize(x.getSize() + (isShiftKeyDown() ? 10 : 1));
            else if (mouseButton == 1)
              x.setSize(x.getSize() - (isShiftKeyDown() ? 10 : 1));
            else if (mouseButton == 2) {
              con.tile.getFilter().put(i, null);
            }
            if (x != null && x.getSize() <= 0) {
              con.tile.getFilter().put(i, null);
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
        ((TileCable) tile).setMode(!((TileCable) tile).isMode());
    }
    else if (checkMeta != null && checkOre != null &&
        (button.id == checkMeta.id || button.id == checkOre.id)) {
      PacketRegistry.INSTANCE.sendToServer(new FilterMessage(-1, null, checkOre.isChecked(), checkMeta.isChecked()));
    }
  }
  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (!(tile instanceof TileCable)) {
      super.keyTyped(typedChar, keyCode);
      return;
    }
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      String s = "";
      if (((TileCable) tile).getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) {
        s = searchBar.getText();
      }
      if ((((TileCable) tile).getUpgradesOfType(ItemUpgrade.OPERATION) >= 1) && this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
        if (!StringUtils.isNumeric(searchBar.getText()) && !searchBar.getText().isEmpty())
          searchBar.setText(s);
        int num = 0;
        try {
          num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
        }
        catch (Exception e) {
          searchBar.setText("0");
        }
        ((TileCable) tile).setLimit(num);
        PacketRegistry.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), operation.stack));
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
  class Button extends GuiButton {
    public Button(int id, int x, int y, String z) {
      super(id, x, y, 16, 16, z);
    }
    @Override
    public void drawButton(Minecraft mcc, int x, int y, float p) {//drawButon
      if (this.visible) {
        FontRenderer fontrenderer = mcc.fontRenderer;
        mcc.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
        int k = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        this.drawTexturedModalRect(this.x, this.y, 160 + 16 * k, 52, 16, 16);
        if (id == 3) {
          if (tile.isWhitelist())
            this.drawTexturedModalRect(this.x + 1, this.y + 3, 176, 83, 13, 10);
          else
            this.drawTexturedModalRect(this.x + 1, this.y + 3, 190, 83, 13, 10);
        }
        if (id == 4) {
          if (((TileCable) tile).isMode())
            this.displayString = ">";//   this.drawTexturedModalRect(this.x + 0, this.y + 0, 176, 94, 16, 15);
          else
            this.displayString = "<";//    this.drawTexturedModalRect(this.x + 0, this.y + 0, 176 + 16, 94, 16, 15);
        }
        if (id == 6) {
          this.drawTexturedModalRect(this.x + 2, this.y + 2, 176 + tile.getWay().ordinal() * 12, 114, 12, 12);
        }
        this.mouseDragged(mcc, x, y);
        int l = 14737632;
        if (packedFGColour != 0) {
          l = packedFGColour;
        }
        else if (!this.enabled) {
          l = 10526880;
        }
        else if (this.hovered) {
          l = 16777120;
        }
        this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
      }
    }
  }
}
