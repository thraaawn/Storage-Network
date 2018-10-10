package mrriegel.storagenetwork.block.control;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableDataMessage.CableMessageType;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiControlButton extends GuiButtonExt {

  private static final ResourceLocation widgets = new ResourceLocation(StorageNetwork.MODID, "textures/gui/enderio-publicdomain-widgetsv2.png");
  public ProcessWrapper cable;
  CableMessageType messageType;
  public int textureX, textureY;
  private List<String> tooltips = new ArrayList<>();

  public GuiControlButton(int id, CableMessageType type, int x, int y, int w, int h, String z) {
    super(id, x, y, w, h, z);
    messageType = type;
  }

  public void addTooltip(String s) {
    tooltips.add(s);
  }

  public List<String> getTooltips() {
    return tooltips;
  }
  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {//drawButon
    super.drawButton(mc, mouseX, mouseY, partial);
    mc.getTextureManager().bindTexture(widgets);
    //green check 
    if (messageType == CableMessageType.P_ONOFF && this.visible) {
      this.drawTexturedModalRect(x, y, textureX, textureY, 16, 16);
    }
  }

  public void actionPerformed() {
    int value = 0;
    if (this.messageType == CableMessageType.P_ONOFF) {
      cable.alwaysOn = !cable.alwaysOn;
      value = cable.alwaysOn ? 1 : 0;
    }
    else if (this.messageType == CableMessageType.P_CTRL_LESS) {
      value = cable.count--;
    }
    else if (this.messageType == CableMessageType.P_CTRL_MORE) {
      value = cable.count++; // 
    }
    StorageNetwork.log(messageType + "click " + value + " at +" + cable.pos + cable.output);
    //    this.messageType
    //    int newval = cable.alwaysOn ? 1 : 0;//inverted because we flippy
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(messageType.ordinal(), cable.pos, value));
  }
}
