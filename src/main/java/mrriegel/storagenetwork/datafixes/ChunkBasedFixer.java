package mrriegel.storagenetwork.datafixes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class ChunkBasedFixer implements IFixableData {
  @Override
  public int getFixVersion() {
    return 0;
  }

  private void moveValue(NBTTagCompound sourceTag, NBTTagCompound targetTag, String key) {
    targetTag.setTag(key, sourceTag.getTag(key));
    sourceTag.removeTag(key);
  }

  private void moveValue(NBTTagCompound sourceTag, NBTTagCompound targetTag, String sourceKey, String targetKey) {
    targetTag.setTag(targetKey, sourceTag.getTag(sourceKey));
    sourceTag.removeTag(sourceKey);
  }

  @Override
  public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
    if(!compound.hasKey("Level")) {
      return compound;
    }

    NBTTagCompound levelData = compound.getCompoundTag("Level");
    if(!levelData.hasKey("TileEntities")) {
      return compound;
    }

    ChunkDataReader reader = new ChunkDataReader(compound.getCompoundTag("Level"));

    NBTTagList tileList = levelData.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
    for(NBTBase tileBase : tileList) {
      NBTTagCompound tileData = (NBTTagCompound) tileBase;
      boolean process = false;
      String id = tileData.getString("id");
      BlockPos pos = new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z"));
      if(id.equals("minecraft:tilekabel")) {
        IBlockState state = reader.getBlockState(pos);
        Block block = state.getBlock();

        if(block == ModBlocks.exKabel || block == ModBlocks.imKabel) {
          tileData.setString("id", StorageNetwork.MODID + ":tilekabelio");

          NBTTagCompound ioStorageTag = new NBTTagCompound();

          // Copy crunchTE + rule-tags to new filter compound
          NBTTagCompound filtersTag = new NBTTagCompound();
          filtersTag.setInteger("Size", 18);

          NBTTagCompound filterRulesTag = new NBTTagCompound();
          moveValue(tileData, filterRulesTag, "nbtFilter", "nbt");
          moveValue(tileData, filterRulesTag, "metas", "meta");
          moveValue(tileData, filterRulesTag, "white", "whitelist");
          moveValue(tileData, filterRulesTag, "ores");
          filtersTag.setTag("rules", filterRulesTag);

          NBTTagList filterStackList = tileData.getTagList("crunchTE", Constants.NBT.TAG_COMPOUND);
          for(NBTBase filterStackBase : filterStackList) {
            NBTTagCompound filterStack = (NBTTagCompound) filterStackBase;
            NBTTagCompound nestedStack = filterStack.getCompoundTag("stack");
            moveValue(nestedStack, filterStack, "id");
            moveValue(nestedStack, filterStack, "Count");
            moveValue(nestedStack, filterStack, "Damage");
            filterStack.removeTag("stack");
            filterStack.removeTag("size");
          }
          moveValue(tileData, filtersTag, "crunchTE", "Items");
          //StorageNetwork.instance.logger.info("Extracted tag: 'filters={}'", filtersTag);
          ioStorageTag.setTag("filters", filtersTag);

          // Move upgrades to upgrade tag
          NBTTagCompound upgradesTag = new NBTTagCompound();
          upgradesTag.setInteger("Size", 4);
          moveValue(tileData, upgradesTag, "Items");
          ioStorageTag.setTag("upgrades", upgradesTag);
          //StorageNetwork.instance.logger.info("Extracted tag: 'upgrades={}'", upgradesTag);

          // Move operation variables to operation tag
          NBTTagCompound operationTag = new NBTTagCompound();
          moveValue(tileData, operationTag, "mode", "mustBeSmaller");
          moveValue(tileData, operationTag, "limit");
          moveValue(tileData, operationTag, "stack");
          ioStorageTag.setTag("operation", operationTag);
          //StorageNetwork.instance.logger.info("Extracted tag: 'operation={}'", operationTag);

          moveValue(tileData, ioStorageTag, "prio");
          moveValue(tileData, ioStorageTag, "inventoryFace");

          tileData.setTag("ioStorage", ioStorageTag);
          /*
           // Original stack data
           {
            stack: {id:"minecraft:chest",Count:1b,Damage:0s},
            nbtFilter:0b,
            connectable:{master:{X:0,Y:0,Dim:0,Z:0}},
            processingBottom:0,
            mode:1b,
            white:0b,
            limit:2,
            Items:[
              {Slot:0b,id:"minecraft:air",Count:1b,Damage:0s},
              {Slot:1b,id:"minecraft:air",Count:1b,Damage:0s},
              {Slot:2b,id:"storagenetwork:upgrade",Count:1b,Damage:0s},
              {Slot:3b,id:"storagenetwork:upgrade",Count:1b,Damage:1s}
            ],
            id:"storagenetwork:tilekabelio",
            prio:-1,
            crunchTE:[
              {stack:{id:"minecraft:stone",Count:1b,Damage:0s},size:1,Slot:0b},
              {stack:{id:"minecraft:air",Count:1b,Damage:0s},size:0,Slot:5b},
              {stack:{id:"minecraft:air",Count:1b,Damage:0s},size:0,Slot:11b},
              {stack:{id:"minecraft:air",Count:1b,Damage:0s},size:0,Slot:12b},
              {stack:{id:"minecraft:air",Count:1b,Damage:0s},size:0,Slot:13b}
            ],
            metas:1b,ores:1b,
            sn_process_count:0,
            way:"BOTH",
            master:"{\"field_177962_a\":18,\"field_177960_b\":4,\"field_177961_c\":-770}",
            x:12,y:4,z:-771,
            sn_process_status:2,
            sn_process_always:1b,
            processingTop:1
           }
          */

          /*
          // New stack data
          {
            filters: {
              Size:18,
              Items: [
                {Slot:4,id:"minecraft:grass",Count:1b,Damage:0s}
              ],
              rules: {nbt:0b,ores:0b,meta:0b,whitelist:1b}
            },
            upgrades:{
              Size:4,
              Items:[
                {Slot:0,id:"storagenetwork:upgrade",Count:1b,Damage:1s},
                {Slot:1,id:"storagenetwork:upgrade",Count:1b,Damage:2s}
              ]
            },
            operation: {
              mustBeSmaller:1b,
              stack:{id:"minecraft:air",Count:1b,Damage:0s},
              limit:0
            }
          }
          */

          // ioStorage -> { upgrades -> itemhandler, filters -> itemhandler, operation -> { stack -> stack, mustBeSmaller -> boolean, limit -> integer }, inventoryFace -> string }
        }

        if(block == ModBlocks.storageKabel) {
          tileData.setString("id", StorageNetwork.MODID + ":tilekabellink");

          NBTTagCompound linkStorageTag = new NBTTagCompound();

          // Copy crunchTE + rule-tags to new filter compound
          NBTTagCompound filtersTag = new NBTTagCompound();
          filtersTag.setInteger("Size", 18);

          NBTTagCompound filterRulesTag = new NBTTagCompound();
          moveValue(tileData, filterRulesTag, "nbtFilter", "nbt");
          moveValue(tileData, filterRulesTag, "metas", "meta");
          moveValue(tileData, filterRulesTag, "white", "whitelist");
          moveValue(tileData, filterRulesTag, "ores");
          filtersTag.setTag("rules", filterRulesTag);

          NBTTagList filterStackList = tileData.getTagList("crunchTE", Constants.NBT.TAG_COMPOUND);
          for(NBTBase filterStackBase : filterStackList) {
            NBTTagCompound filterStack = (NBTTagCompound) filterStackBase;
            NBTTagCompound nestedStack = filterStack.getCompoundTag("stack");
            moveValue(nestedStack, filterStack, "id");
            moveValue(nestedStack, filterStack, "Count");
            moveValue(nestedStack, filterStack, "Damage");
            filterStack.removeTag("stack");
            filterStack.removeTag("size");
          }
          moveValue(tileData, filtersTag, "crunchTE", "Items");
          //StorageNetwork.instance.logger.info("Extracted tag: 'filters={}'", filtersTag);
          linkStorageTag.setTag("filters", filtersTag);

          // Move operation variables to operation tag
          NBTTagCompound operationTag = new NBTTagCompound();
          moveValue(tileData, operationTag, "mode", "mustBeSmaller");
          moveValue(tileData, operationTag, "limit");
          moveValue(tileData, operationTag, "stack");
          linkStorageTag.setTag("operation", operationTag);
          //StorageNetwork.instance.logger.info("Extracted tag: 'operation={}'", operationTag);

          moveValue(tileData, linkStorageTag, "way");
          moveValue(tileData, linkStorageTag, "prio");
          moveValue(tileData, linkStorageTag, "inventoryFace");

          tileData.setTag("itemStorage", linkStorageTag);
        }

        if(block == ModBlocks.processKabel) {
          tileData.setString("id", StorageNetwork.MODID + ":tilekabelprocess");

          // Copy crunchTE + rule-tags to new filter compound
          NBTTagCompound filtersTag = new NBTTagCompound();
          filtersTag.setInteger("Size", 18);

          NBTTagCompound filterRulesTag = new NBTTagCompound();
          moveValue(tileData, filterRulesTag, "nbtFilter", "nbt");
          moveValue(tileData, filterRulesTag, "metas", "meta");
          moveValue(tileData, filterRulesTag, "white", "whitelist");
          moveValue(tileData, filterRulesTag, "ores");
          filtersTag.setTag("rules", filterRulesTag);

          NBTTagList filterStackList = tileData.getTagList("crunchTE", Constants.NBT.TAG_COMPOUND);
          for(NBTBase filterStackBase : filterStackList) {
            NBTTagCompound filterStack = (NBTTagCompound) filterStackBase;
            NBTTagCompound nestedStack = filterStack.getCompoundTag("stack");
            moveValue(nestedStack, filterStack, "id");
            moveValue(nestedStack, filterStack, "Count");
            moveValue(nestedStack, filterStack, "Damage");
            filterStack.removeTag("stack");
            filterStack.removeTag("size");
          }
          moveValue(tileData, filtersTag, "crunchTE", "Items");
          //StorageNetwork.instance.logger.info("Extracted tag: 'filters={}'", filtersTag);
          tileData.setTag("filters", filtersTag);
        }

        process = true;
      }

      if(id.equals("minecraft:tilemaster")) {
        tileData.setString("id", StorageNetwork.MODID + ":tilemaster");
        process = true;
      }

      if(id.equals("minecraft:tilerequest")) {
        tileData.setString("id", StorageNetwork.MODID + ":tilerequest");
        process = true;
      }

      if(id.equals("minecraft:tilecontrol")) {
        tileData.setString("id", StorageNetwork.MODID + ":tilecontrol");
        process = true;
      }

      if(!process) {
        continue;
      }

      StorageNetwork.instance.logger.info("Renamed '{}' to '{}'", id, tileData.getString("id"));

      if(tileData.hasKey("master")) {
        NBTTagCompound connectableTag = new NBTTagCompound();
        BlockPos posMaster = new Gson().fromJson(tileData.getString("master"), new TypeToken<BlockPos>() {}.getType());
        DimPos newMasterPos = new DimPos(0, posMaster);
        connectableTag.setTag("master", newMasterPos.serializeNBT());

        tileData.setTag("connectable", connectableTag);
        StorageNetwork.instance.logger.info("Converted 'master={}' to 'connectable={}'", tileData.getString("master"), connectableTag);
        tileData.removeTag("master");
      }

      if(tileData.hasKey("inventoryFace")) {
        EnumFacing inventoryFace = EnumFacing.byName(tileData.getString("inventoryFace"));
        if (inventoryFace != null) {
          StorageNetwork.instance.logger.info("Converted 'inventoryFace={}' to 'direction={}'", tileData.getString("inventoryFace"), inventoryFace.getIndex());
          tileData.setInteger("direction", inventoryFace.getIndex());
          tileData.removeTag("inventoryFace");
        } else {
          StorageNetwork.instance.logger.info("Unable to convert inventoryFace={}", tileData.getString("inventoryFace"));
        }
      }

      tileData.removeTag("north");
      tileData.removeTag("south");
      tileData.removeTag("east");
      tileData.removeTag("west");
      tileData.removeTag("up");
      tileData.removeTag("down");
      tileData.removeTag("connectedInventory");

    }


    return compound;
  }
}
