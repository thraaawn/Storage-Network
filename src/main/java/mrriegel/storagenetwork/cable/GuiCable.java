package mrriegel.storagenetwork.cable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.RigelNetworkGuiContainer;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.cable.TileCable.Kind;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.network.ButtonMessage;
import mrriegel.storagenetwork.network.FilterMessage;
import mrriegel.storagenetwork.network.LimitMessage;
import mrriegel.storagenetwork.network.PacketHandler;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class GuiCable extends RigelNetworkGuiContainer {
  private ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID + ":textures/gui/cable.png");
  Kind kind;
  Button btnPlus, btnMinus, btnWhite, btnActi, btnImport, btnWay;
  AbstractFilterTile tile;
  private GuiTextField searchBar;
  List<ItemSlot> list;
  ItemSlot operation;
  public GuiCable(Container inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 171;
    this.tile = ((ContainerCable) inventorySlots).tile;
    if (tile instanceof TileCable) {
      this.kind = ((TileCable) tile).getKind();
    }
    list = Lists.newArrayList();
  }
  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int i = (this.width - this.xSize) / 2;
    int j = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    for (int ii = 0; ii < 9; ii++) {
      for (int jj = 0; jj < 2; jj++)
        this.drawTexturedModalRect(i + 7 + ii * 18, j + 25 + 18 * jj, 176, 34, 18, 18);
    }
    if (tile instanceof TileCable) {
      if (((TileCable) tile).isUpgradeable()) {
        for (int ii = 0; ii < 4; ii++) {
          this.drawTexturedModalRect(i + 97 + ii * 18, j + 5, 176, 34, 18, 18);
        }
      }
      if (((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1) {
        btnActi.enabled = true;
        btnActi.visible = true;
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(i + 7, j + 65, 176, 34, 18, 18);
        this.drawTexturedModalRect(i + 30, j + 67, 0, 171, 90, 12);
        searchBar.drawTextBox();
      }
      else {
        btnActi.enabled = false;
        btnActi.visible = false;
      }
    }
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
    for (ItemSlot s : list)
      s.drawSlot(mouseX, mouseY);
    if (tile instanceof TileCable && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1) {
      operation.drawSlot(mouseX, mouseY);
    }
    fontRendererObj.drawString(String.valueOf(tile.getPriority()), guiLeft + 30 - fontRendererObj.getStringWidth(String.valueOf(tile.getPriority())) / 2, guiTop + 10, 4210752);
  }
  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    for (int i = 0; i < list.size(); i++) {
      ItemSlot e = list.get(i);
      ContainerCable con = (ContainerCable) inventorySlots;
      if (e.stack != null && !e.stack.isEmpty()) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        if (con.tile.getOres().get(i) != null && con.tile.getOres().get(i))
          mc.fontRendererObj.drawStringWithShadow("O", e.x + 10, e.y, 0x4f94cd);
        if (con.tile.getMetas().get(i) == null || !con.tile.getMetas().get(i))
          mc.fontRendererObj.drawStringWithShadow("M", e.x + 1, e.y, 0xff4040);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
    }
    for (ItemSlot s : list)
      s.drawTooltip(mouseX, mouseY);
    if (tile instanceof TileCable && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1)
      operation.drawTooltip(mouseX, mouseY);
    if (btnImport != null && btnImport.isMouseOver())
      drawHoveringText(Lists.newArrayList("Import Filter"), mouseX - guiLeft, mouseY - guiTop);
    if (btnWay != null && btnWay.isMouseOver())
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.fil.tooltip_" + tile.getWay().toString())), mouseX - guiLeft, mouseY - guiTop);
    if (mouseX > guiLeft + 29 && mouseX < guiLeft + 37 && mouseY > guiTop + 10 && mouseY < guiTop + 20)
      this.drawHoveringText(Lists.newArrayList("Priority"), mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
    if (btnWhite != null && btnWhite.isMouseOver())
      this.drawHoveringText(Lists.newArrayList(tile.isWhite() ? "Whitelist" : "Blacklist"), mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
  }
  @Override
  public void initGui() {
    super.initGui();
    btnMinus = new Button(0, guiLeft + 6, guiTop + 5, "-");
    buttonList.add(btnMinus);
    btnPlus = new Button(1, guiLeft + 37, guiTop + 5, "+");
    buttonList.add(btnPlus);
    btnWhite = new Button(3, guiLeft + 58, guiTop + 5, "");
    buttonList.add(btnWhite);
    //if (tile.isStorage()) {
      btnImport = new Button(5, guiLeft + 78, guiTop + 5, "I");
      buttonList.add(btnImport);
    if (tile.isStorage()) {
      btnWay = new Button(6, guiLeft + 115, guiTop + 5, "");
      buttonList.add(btnWay);
    }
    if (tile instanceof TileCable) {
      Keyboard.enableRepeatEvents(true);
      searchBar = new GuiTextField(0, fontRendererObj, guiLeft + 34, guiTop + 69, 85, fontRendererObj.FONT_HEIGHT);
      searchBar.setMaxStringLength(30);
      searchBar.setEnableBackgroundDrawing(false);
      searchBar.setVisible(true);
      searchBar.setTextColor(16777215);
      searchBar.setCanLoseFocus(false);
      searchBar.setFocused(true);
      searchBar.setText(((TileCable) tile).getLimit() + "");
      btnActi = new Button(4, guiLeft + 127, guiTop + 65, "");
      buttonList.add(btnActi);
      operation = new ItemSlot(((TileCable) tile).getStack(), guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false, true, false, true);
    }
  }
  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (operation != null && operation.isMouseOverSlot(mouseX, mouseY) && ((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1) {
      ((TileCable) tile).setStack(mc.player.inventory.getItemStack());
      operation.stack = mc.player.inventory.getItemStack();
      int num = searchBar.getText().isEmpty() ? 0 : Integer.valueOf(searchBar.getText());
      PacketHandler.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), mc.player.inventory.getItemStack()));
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
            con.tile.getOres().put(i, false);
            con.tile.getMetas().put(i, true);
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
              con.tile.getOres().put(i, false);
              con.tile.getMetas().put(i, true);
            }
            if (x != null && x.getSize() <= 0) {
              con.tile.getFilter().put(i, null);
              con.tile.getOres().put(i, false);
              con.tile.getMetas().put(i, true);
            }
          }
        }
        con.slotChanged();
        PacketHandler.INSTANCE.sendToServer(new FilterMessage(i, tile.getFilter().get(i), tile.getOre(i), tile.getMeta(i)));
        break;
      }
    }
  }
  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    PacketHandler.INSTANCE.sendToServer(new ButtonMessage(button.id, tile.getPos()));
    switch (button.id) {
      case 0:
        tile.setPriority(tile.getPriority() - 1);
      break;
      case 1:
        tile.setPriority(tile.getPriority() + 1);
      break;
      case 3:
        tile.setWhite(!tile.isWhite());
      break;
      case 4:
        if (tile instanceof TileCable)
          ((TileCable) tile).setMode(!((TileCable) tile).isMode());
      break;
    }
  }
  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (typedChar == 'o' || typedChar == 'm') {
      for (int i = 0; i < list.size(); i++) {
        ItemSlot e = list.get(i);
        int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        ContainerCable con = (ContainerCable) inventorySlots;
        if (e.isMouseOverSlot(mouseX, mouseY) && e.stack != null) {
          if (typedChar == 'o' && OreDictionary.getOreIDs(e.stack).length > 0)
            con.tile.getOres().put(i, !con.tile.getOres().get(i));
          else if (typedChar == 'm')
            con.tile.getMetas().put(i, !con.tile.getMetas().get(i));
          con.slotChanged();
          PacketHandler.INSTANCE.sendToServer(new FilterMessage(i, tile.getFilter().get(i), tile.getOre(i), tile.getMeta(i)));
          break;
        }
      }
    }
    if (!(tile instanceof TileCable)) {
      super.keyTyped(typedChar, keyCode);
      return;
    }
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      String s = "";
      if (((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1) {
        s = searchBar.getText();
      }
      if ((((TileCable) tile).getUpgradesOfType(ItemUpgrade.OP) >= 1) && this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
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
        PacketHandler.INSTANCE.sendToServer(new LimitMessage(num, tile.getPos(), operation.stack));
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
    public Button(int p_i1021_1_, int p_i1021_2_, int p_i1021_3_, String p_i1021_6_) {
      super(p_i1021_1_, p_i1021_2_, p_i1021_3_, 16, 16, p_i1021_6_);
    }
    @Override
    public void func_191745_a(Minecraft mcc, int x, int y, float p) {//drawButon
      if (this.visible) {
        FontRenderer fontrenderer = mcc.fontRendererObj;
        mcc.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
        int k = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 160 + 16 * k, 52, 16, 16);
        if (id == 3) {
          if (tile.isWhite())
            this.drawTexturedModalRect(this.xPosition + 1, this.yPosition + 3, 176, 83, 13, 10);
          else
            this.drawTexturedModalRect(this.xPosition + 1, this.yPosition + 3, 190, 83, 13, 10);
        }
        if (id == 4) {
          if (((TileCable) tile).isMode())
            this.drawTexturedModalRect(this.xPosition + 0, this.yPosition + 0, 176, 94, 16, 15);
          else
            this.drawTexturedModalRect(this.xPosition + 0, this.yPosition + 0, 176 + 16, 94, 16, 15);
        }
        if (id == 6) {
          this.drawTexturedModalRect(this.xPosition + 2, this.yPosition + 2, 176 + tile.getWay().ordinal() * 12, 114, 12, 12);
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
        if (tile instanceof TileCable) {
          List<String> lis = new ArrayList<String>();
          String s = I18n.format("gui.storagenetwork.operate.tooltip", mc.world.getBlockState(tile.getPos()).getBlock().getLocalizedName(), I18n.format("gui.storagenetwork.operate.tooltip." + (((TileCable) tile).isMode() ? "more" : "less")), ((TileCable) tile).getLimit(), ((TileCable) tile).getStack() != null ? ((TileCable) tile).getStack().getDisplayName() : "Items");
          List<String> matchList = new ArrayList<String>();
          Pattern regex = Pattern.compile(".{1,25}(?:\\s|$)", Pattern.DOTALL);
          Matcher regexMatcher = regex.matcher(s);
          while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
          }
          lis = new ArrayList<String>(matchList);
          if (this.hovered && id == 4 && ((TileCable) tile).getStack() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            drawHoveringText(lis, x, y, fontRendererObj);
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
          }
        }
        this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
      }
    }
  }
}
