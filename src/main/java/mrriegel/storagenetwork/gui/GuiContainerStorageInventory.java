package mrriegel.storagenetwork.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.request.TileRequest.EnumSortType;
import mrriegel.storagenetwork.jei.JeiHooks;
import mrriegel.storagenetwork.jei.JeiSettings;
import mrriegel.storagenetwork.network.ClearMessage;
import mrriegel.storagenetwork.network.InsertMessage;
import mrriegel.storagenetwork.network.RequestMessage;
import mrriegel.storagenetwork.network.SortMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Base class for Request table inventory and Remote inventory
 * 
 *
 */
public abstract class GuiContainerStorageInventory extends GuiContainerBase {

  private static final int MOUSE_BTN_RIGHT = 1;
  private static final int MOUSE_BTN_LEFT = 0;
  protected ResourceLocation texture;
  protected int page = 1, maxPage = 1;
  public List<StackWrapper> stacks, craftableStacks;
  protected ItemStack over = ItemStack.EMPTY;
  protected GuiTextField searchBar;
  protected Button directionBtn, sortBtn, jeiBtn;
  protected List<ItemSlotNetwork> slots;
  protected long lastClick;
  private Button clearTextBtn;
  private boolean forceFocus;

  public GuiContainerStorageInventory(ContainerNetworkBase inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 256;
    this.stacks = Lists.newArrayList();
    this.craftableStacks = Lists.newArrayList();
    PacketRegistry.INSTANCE.sendToServer(new RequestMessage());
    lastClick = System.currentTimeMillis();
  }

  protected boolean canClick() {
    return System.currentTimeMillis() > lastClick + 100L;
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    searchBar = new GuiTextField(0, fontRenderer, guiLeft + 81, guiTop + 96, 85, fontRenderer.FONT_HEIGHT);
    searchBar.setMaxStringLength(30);
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused(true);
    if (JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearch()) {
      searchBar.setText(JeiHooks.getFilterText());
    }
    directionBtn = new Button(0, guiLeft + 7, guiTop + 93, "");
    this.addButton(directionBtn);
    sortBtn = new Button(1, guiLeft + 21, guiTop + 93, "");
    this.addButton(sortBtn);
    jeiBtn = new Button(4, guiLeft + 35, guiTop + 93, "");
    if (JeiSettings.isJeiLoaded()) {
      this.addButton(jeiBtn);
    }
    clearTextBtn = new Button(5, guiLeft + 64, guiTop + 93, "X");
    this.addButton(clearTextBtn);
  }

  public abstract int getLines();

  public abstract int getColumns();

  public abstract boolean getDownwards();

  public abstract void setDownwards(boolean d);

  public abstract EnumSortType getSort();

  public abstract void setSort(EnumSortType s);

  public abstract BlockPos getPos();

  protected abstract int getDim();

  protected abstract boolean inField(int mouseX, int mouseY);

  protected abstract boolean inSearchbar(int mouseX, int mouseY);

  protected abstract boolean inX(int mouseX, int mouseY);

  protected abstract boolean isScreenValid();

  @Override
  public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    if (this.isScreenValid() == false) {
      return;
    }
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int i = (this.width - this.xSize) / 2;
    int j = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    String search = searchBar.getText();
    List<StackWrapper> tmp = search.equals("") ? Lists.newArrayList(stacks) : Lists.<StackWrapper> newArrayList();
    if (!search.equals("")) {
      for (StackWrapper s : stacks) {
        if (search.startsWith("@")) {
          String name = UtilTileEntity.getModNameForItem(s.getStack().getItem());
          if (name.toLowerCase().contains(search.toLowerCase().substring(1)))
            tmp.add(s);
        }
        else if (search.startsWith("#")) {
          String tooltipString;
          List<String> tooltip = s.getStack().getTooltip(mc.player, TooltipFlags.NORMAL);
          tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
          tooltipString = ChatFormatting.stripFormatting(tooltipString);
          if (tooltipString.toLowerCase().contains(search.toLowerCase().substring(1)))
            tmp.add(s);
        }
        else if (search.startsWith("$")) {
          StringBuilder oreDictStringBuilder = new StringBuilder();
          for (int oreId : OreDictionary.getOreIDs(s.getStack())) {
            String oreName = OreDictionary.getOreName(oreId);
            oreDictStringBuilder.append(oreName).append(' ');
          }
          if (oreDictStringBuilder.toString().toLowerCase().contains(search.toLowerCase().substring(1)))
            tmp.add(s);
        }
        else if (search.startsWith("%")) {
          StringBuilder creativeTabStringBuilder = new StringBuilder();
          for (CreativeTabs creativeTab : s.getStack().getItem().getCreativeTabs()) {
            if (creativeTab != null) {
              String creativeTabName = creativeTab.getTranslatedTabLabel();
              creativeTabStringBuilder.append(creativeTabName).append(' ');
            }
          }
          if (creativeTabStringBuilder.toString().toLowerCase().contains(search.toLowerCase().substring(1)))
            tmp.add(s);
        }
        else {
          if (s.getStack().getDisplayName().toLowerCase().contains(search.toLowerCase()))
            tmp.add(s);
        }
      }
    }
    // for (StackWrapper s : craftableStacks)
    // tmp.add(s);
    Collections.sort(tmp, new Comparator<StackWrapper>() {

      int mul = getDownwards() ? -1 : 1;

      @Override
      public int compare(StackWrapper o2, StackWrapper o1) {
        switch (getSort()) {
          case AMOUNT:
            return Integer.compare(o1.getSize(), o2.getSize()) * mul;
          case NAME:
            return o2.getStack().getDisplayName().compareToIgnoreCase(o1.getStack().getDisplayName()) * mul;
          case MOD:
            return UtilTileEntity.getModNameForItem(o2.getStack().getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getStack().getItem())) * mul;
        }
        return 0;
      }
    });
    maxPage = tmp.size() / (getColumns());
    if (tmp.size() % (getColumns()) != 0)
      maxPage++;
    maxPage -= (getLines() - 1);
    if (maxPage < 1)
      maxPage = 1;
    if (page < 1)
      page = 1;
    if (page > maxPage)
      page = maxPage;
    searchBar.drawTextBox();
    slots = Lists.newArrayList();
    int index = (page - 1) * (getColumns());
    for (int jj = 0; jj < getLines(); jj++) {
      for (int ii = 0; ii < getColumns(); ii++) {
        int in = index;
        if (in >= tmp.size())
          break;
        slots.add(new ItemSlotNetwork(this, tmp.get(in).getStack(), guiLeft + 8 + ii * 18, guiTop + 10 + jj * 18, tmp.get(in).getSize(), guiLeft, guiTop, true));
        index++;
      }
    }
    for (ItemSlotNetwork s : slots) {
      s.drawSlot(mouseX, mouseY);
    }
    for (ItemSlotNetwork s : slots) {
      if (s.isMouseOverSlot(mouseX, mouseY)) {
        over = s.getStack();
        break;
      }
      else
        over = ItemStack.EMPTY;
    }
    if (slots.isEmpty())
      over = ItemStack.EMPTY;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    super.renderHoveredToolTip(mouseX, mouseY);
    if (this.isScreenValid() == false) {
      mc.player.closeScreen();
      return;
    }
    drawTooltips(mouseX, mouseY);
  }

  @Override
  public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    if (this.isScreenValid() == false) {
      return;
    }
    if (forceFocus) {
      this.searchBar.setFocused(true);
      if (this.searchBar.isFocused()) {
        this.forceFocus = false;
      }
    }
  }

  public void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : slots) {
      if (s.isMouseOverSlot(mouseX, mouseY)) {
        s.drawTooltip(mouseX, mouseY);
      }
    }
    if (inSearchbar(mouseX, mouseY)) {
      List<String> lis = Lists.newArrayList();
      if (!isShiftKeyDown()) {
        lis.add(I18n.format("gui.storagenetwork.shift"));
      }
      else {
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_0"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_1"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_2"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_3"));
      }
      drawHoveringText(lis, mouseX, mouseY);
    }
    if (clearTextBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.tooltip_clear")), mouseX, mouseY);
    }
    if (sortBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.req.tooltip_" + getSort().toString())), mouseX, mouseY);
    }
    if (directionBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.sort")), mouseX, mouseY);
    }
    if (jeiBtn != null && jeiBtn.isMouseOver()) {
      String s = I18n.format(JeiSettings.isJeiSearch() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
      drawHoveringText(Lists.newArrayList(s), mouseX, mouseY);
    }
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    boolean doSort = true;
    if (button.id == 2 && page > 1) {
      page--;
    }
    else if (button.id == 3 && page < maxPage) {
      page++;
    }
    else if (button.id == 0) {
      setDownwards(!getDownwards());
    }
    else if (button.id == 1) {
      setSort(getSort().next());
    }
    else if (button.id == 4) {
      doSort = false;
      JeiSettings.setJeiSearch(!JeiSettings.isJeiSearch());
    }
    else if (button.id == this.clearTextBtn.id) {
      doSort = false;
      StorageNetwork.log("set text clear from clear button");
      this.searchBar.setText("");
      //      this.searchBar.setFocused(true);//doesnt work..somethings overriding it?
      this.forceFocus = true;//we have to force it to go next-tick
    }
    if (doSort) {
      PacketRegistry.INSTANCE.sendToServer(new SortMessage(getPos(), getDownwards(), getSort()));
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    searchBar.setFocused(false);
    if (inSearchbar(mouseX, mouseY)) {
      searchBar.setFocused(true);
      if (mouseButton == MOUSE_BTN_RIGHT) {

        searchBar.setText("");
      }
    }
    else if (inX(mouseX, mouseY)) {
      PacketRegistry.INSTANCE.sendToServer(new ClearMessage());
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
    }
    else if (over != null && !over.isEmpty() && (mouseButton == MOUSE_BTN_LEFT || mouseButton == MOUSE_BTN_RIGHT) && mc.player.inventory.getItemStack().isEmpty() && canClick()) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, over, isShiftKeyDown(), isCtrlKeyDown()));
      lastClick = System.currentTimeMillis();
    }
    else if (mc.player.inventory.getItemStack() != null && !mc.player.inventory.getItemStack().isEmpty() && inField(mouseX, mouseY) && canClick()) {
      PacketRegistry.INSTANCE.sendToServer(new InsertMessage(getDim(), mouseButton, mc.player.inventory.getItemStack()));
      lastClick = System.currentTimeMillis();
    }
  }

  @Override
  public void keyTyped(char typedChar, int keyCode) throws IOException {
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      if (this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
        PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
        if (searchBar.isFocused() && JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearch()) {

          JeiHooks.setFilterText(searchBar.getText()); //  searchBar.setText(JeiHooks.getFilterText());
        }
      }
      else {
        super.keyTyped(typedChar, keyCode);
      }
    }
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (searchBar != null) {
      searchBar.updateCursorCounter();
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int i = Mouse.getX() * this.width / this.mc.displayWidth;
    int j = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
    if (inField(i, j)) {
      int mouse = Mouse.getEventDWheel();
      if (mouse == 0)
        return;
      if (mouse > 0 && page > 1)
        page--;
      if (mouse < 0 && page < maxPage)
        page++;
    }
  }

  public class Button extends GuiButton {

    public Button(int id, int x, int y, String str) {
      super(id, x, y, 14, 14, str);
    }

    public Button(int id, int x, int y, int width, String str) {
      super(id, x, y, width, 14, str);
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y, float pticks) {// drawButton
      if (this.visible) {
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
        int k = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        this.drawTexturedModalRect(this.x, this.y, 162 + 14 * k, 0, width, height);
        if (id == 0) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, 176 + (getDownwards() ? 6 : 0), 14, 6, 8);
        }
        if (id == 1) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, 188 + (getSort() == EnumSortType.AMOUNT ? 6 : getSort() == EnumSortType.MOD ? 12 : 0), 14, 6, 8);
        }
        if (id == 4) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, 176 + (JeiSettings.isJeiSearch() ? 0 : 6), 22, 6, 8);
        }
        this.mouseDragged(mc, x, y);
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
