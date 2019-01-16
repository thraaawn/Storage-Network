package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import java.util.List;
import org.lwjgl.input.Mouse;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableFilterMessage;
import mrriegel.storagenetwork.network.CableLimitMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public abstract class GuiCableBase extends GuiContainer {

  public static final int SQ = 18;
  public static final int TEXTBOX_WIDTH = 26;
  public final static int FONTCOLOR = 4210752;
  protected List<ItemSlotNetwork> itemSlotsGhost;
  protected TileCable tile;
  protected GuiCheckBox checkboxNBT;
  protected ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  protected GuiCableButton btnPlus, btnMinus, btnWhite, btnOperationToggle, btnImport, btnInputOutputStorage;
  protected GuiTextField searchBar;
  protected ItemSlotNetwork operationItemSlot;
  protected GuiCheckBox checkOreBtn;
  protected GuiCheckBox checkMetaBtn;
  protected GuiCableButton pbtnReset;
  protected GuiCableButton pbtnBottomface;
  protected GuiCableButton pbtnTopface;

  public GuiCableBase(ContainerCable inventorySlotsIn) {
    super(inventorySlotsIn);
    this.xSize = 176;
    this.ySize = 171;
    this.tile = inventorySlotsIn.getTile();
    itemSlotsGhost = Lists.newArrayList();
  }

  public void drawString(String s, int x, int y) {
    int FONT = 14737632;
    this.drawString(this.fontRenderer, StorageNetwork.lang(s),
        x, y, FONT);
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
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(-1, null, checkOreBtn.isChecked(), checkMetaBtn.isChecked(), this.checkboxNBT.isChecked()));
    }
    else {
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, tile.getPos()));
    }
  }

  @Override
  public void drawBackground(int tint) {
    super.drawBackground(tint);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getDWheel();
    if (wheel == 0) {
      return;
    }
    boolean wheelUp = wheel > 0;
    int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
    int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    for (int i = 0; i < itemSlotsGhost.size(); i++) {
      ItemSlotNetwork itemSlot = itemSlotsGhost.get(i);
      if (itemSlot.isMouseOverSlot(mouseX, mouseY)
          && itemSlot.getStack().isEmpty() == false) {
        // slot 
        ContainerCable container = (ContainerCable) inventorySlots;
        StackWrapper stackWrapper = container.getTile().getFilter().get(i);
        boolean changed = false;
        if (wheelUp && stackWrapper.getSize() < 64) {
          stackWrapper.setSize(stackWrapper.getSize() + 1);
          changed = true;
        }
        else if (!wheelUp && stackWrapper.getSize() > 1) {
          stackWrapper.setSize(stackWrapper.getSize() - 1);
          changed = true;
        }
        if (changed) {
          PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(i, tile.getFilter().get(i), tile.getOre(), tile.getMeta(), checkboxNBT.isChecked()));
        }
        return;
      }
    }
  }

  public FontRenderer getFont() {
    return this.fontRenderer;
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
    //  boolean isMiddleClick = mouseButton == UtilTileEntity.MOUSE_BTN_MIDDLE_CLICK;
    for (int i = 0; i < itemSlotsGhost.size(); i++) {
      ItemSlotNetwork itemSlot = itemSlotsGhost.get(i);
      if (itemSlot.isMouseOverSlot(mouseX, mouseY)) {
        ContainerCable container = (ContainerCable) inventorySlots;
        StackWrapper stackWrapper = container.getTile().getFilter().get(i);
        boolean doesExistAlready = container.isInFilter(new StackWrapper(stackCarriedByMouse, 1));
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
          }
        }
        //        container.slotChanged();
        PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(i, tile.getFilter().get(i), tile.getOre(), tile.getMeta(), false));
        break;
      }
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    super.renderHoveredToolTip(mouseX, mouseY);
    drawTooltips(mouseX, mouseY);
  }

  protected void drawTooltips(int mouseX, int mouseY) {
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
    }
    if (pbtnBottomface != null && pbtnBottomface.isMouseOver()) {
      this.drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.processing.extract")), mouseX, mouseY, fontRenderer);
    }
    if (btnOperationToggle != null && btnOperationToggle.isMouseOver()) {
      String s = I18n.format("gui.storagenetwork.operate.tooltip", I18n.format("gui.storagenetwork.operate.tooltip." + (tile.isMode() ? "more" : "less")), tile.getLimit(), tile.getOperationStack() != null ? tile.getOperationStack().getDisplayName() : "Items");
      this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
    }
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
  }
}
