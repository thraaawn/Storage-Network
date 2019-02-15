package mrriegel.storagenetwork.block.cable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TesrCable extends TileEntitySpecialRenderer<TileCable> {

  private ModelCable model;

  private static Map<Block, ResourceLocation> renderMaps = new HashMap<>();

  public static void addCableRender(Block block, ResourceLocation image) {
    renderMaps.put(block, image);
  }
  // TODO: Use baked models instead of tesrs
  public TesrCable() {
    model = new ModelCable();
  }

  @Override
  public void render(TileCable te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
    if (te == null) {
      return;
    }
    IBlockState blockstate = te.getWorld().getBlockState(te.getPos());
    if (!(blockstate.getBlock() instanceof BlockCable)) {
      return;
    }


    blockstate = blockstate.getActualState(te.getWorld(), te.getPos());
    IExtendedBlockState extendedBlockState = (IExtendedBlockState)blockstate.getBlock().getExtendedState(blockstate, te.getWorld(), te.getPos());
    UnlistedPropertyBlockNeighbors.BlockNeighbors neighbors = extendedBlockState.getValue(BlockCable.BLOCK_NEIGHBORS);
    if (neighbors == null) {
      return;
    }

    GlStateManager.pushMatrix();
    GlStateManager.enableRescaleNormal();
    GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
    Block kind = te.getBlockType();
    if (renderMaps.containsKey(kind)) {
      Minecraft.getMinecraft().renderEngine.bindTexture(renderMaps.get(kind));
    }

    GlStateManager.pushMatrix();
    GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.pushAttrib();
    RenderHelper.disableStandardItemLighting();
    model.render(neighbors, kind);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.popAttrib();
    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  }
}
