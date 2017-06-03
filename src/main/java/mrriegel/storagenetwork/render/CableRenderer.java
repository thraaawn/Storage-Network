package mrriegel.storagenetwork.render;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.cable.BlockCable;
import mrriegel.storagenetwork.cable.TileCable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class CableRenderer extends TileEntitySpecialRenderer<TileCable> {
  ModelCable model;
  private final ResourceLocation link = new ResourceLocation(StorageNetwork.MODID + ":textures/tile/link.png");
  private final ResourceLocation ex = new ResourceLocation(StorageNetwork.MODID + ":textures/tile/ex.png");
  private final ResourceLocation im = new ResourceLocation(StorageNetwork.MODID + ":textures/tile/im.png");
  private final ResourceLocation storage = new ResourceLocation(StorageNetwork.MODID + ":textures/tile/storage.png");
  public CableRenderer() {
    model = new ModelCable();
  }
  @Override
  public void renderTileEntityAt(TileCable te, double x, double y, double z, float partialTicks, int destroyStage) {
    // if(true)return;
   // boolean show = Minecraft.getMinecraft().player.inventory.getCurrentItem() != null && Block.getBlockFromItem(Minecraft.getMinecraft().player.inventory.getCurrentItem().getItem()) instanceof BlockKabel;
    if (te == null || te.getKind() == null || !(te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockCable)) { return; }
    
//    if (te.getCover() != null && !show) {
//      
//      
//      if (te.getCover() == Blocks.GLASS)
//        return;
//      this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//      BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//      World world = te.getWorld();
//      BlockPos blockpos = te.getPos();
//      IBlockState iblockstate = te.getCover().getStateFromMeta(te.getCoverMeta());
//      GlStateManager.pushMatrix();
//      RenderHelper.disableStandardItemLighting();
//      GlStateManager.translate((float) x, (float) y, (float) z);
//      Tessellator tessellator = Tessellator.getInstance();
//      VertexBuffer worldrenderer = tessellator.getBuffer();
//      worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//      int i = blockpos.getX();
//      int j = blockpos.getY();
//      int k = blockpos.getZ();
//      worldrenderer.setTranslation(((-i)), (-j), ((-k)));
//      worldrenderer.color(1F, 1F, 1F, 1F);
//      IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate);
//      blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, true);
//      worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
//      tessellator.draw();
//      RenderHelper.enableStandardItemLighting();
//      GlStateManager.popMatrix();
//      return;
//    }
    GlStateManager.pushMatrix();
    GlStateManager.enableRescaleNormal();
    GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
    switch (te.getKind()) {
      case kabel:
        Minecraft.getMinecraft().renderEngine.bindTexture(link);
      break;
      case exKabel:
        Minecraft.getMinecraft().renderEngine.bindTexture(ex);
      break;
      case imKabel:
        Minecraft.getMinecraft().renderEngine.bindTexture(im);
      break;
      case storageKabel:
        Minecraft.getMinecraft().renderEngine.bindTexture(storage);
      break;
      default:
      break;
    }
    GlStateManager.pushMatrix();
    GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.pushAttrib();
    RenderHelper.disableStandardItemLighting();
    model.render(te);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.popAttrib();
    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  }
}
