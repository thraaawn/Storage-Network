package mrriegel.storagenetwork.block.cable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.HashMap;
import java.util.Map;

public class UnlistedPropertyBlockNeighbors implements IUnlistedProperty<UnlistedPropertyBlockNeighbors.BlockNeighbors> {
    @Override
    public String getName() {
        return "hillNeighbors";
    }

    @Override
    public boolean isValid(BlockNeighbors value) {
        return true;
    }

    @Override
    public Class<BlockNeighbors> getType() {
        return BlockNeighbors.class;
    }

    @Override
    public String valueToString(BlockNeighbors value) {
        return value.toString();
    }


    public enum EnumNeighborType {
        NONE,
        CABLE,
        SPECIAL
    }

    public static class BlockNeighbors {
        public Map<EnumFacing, EnumNeighborType> neighborTypes = new HashMap<>();

        public void setNeighborType(EnumFacing facing, EnumNeighborType type) {
            neighborTypes.put(facing, type);
        }

        private static String getFacingShortName(EnumFacing facing) {
            return facing.getName().substring(0, 1).toLowerCase();
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("NeighborTypes[");
            for(Map.Entry<EnumFacing, EnumNeighborType> entry : neighborTypes.entrySet()) {
                if(entry.getValue() == EnumNeighborType.NONE) {
                    continue;
                }

                if(entry.getValue() == EnumNeighborType.CABLE) {
                    builder.append(getFacingShortName(entry.getKey()).toLowerCase());
                } else {
                    builder.append(getFacingShortName(entry.getKey()).toUpperCase());
                }
            }
            builder.append(']');

            return builder.toString();
        }

        public boolean requiresCube() {
            // Only Y-Axis -> no cube
            boolean hasNorth = north() != EnumNeighborType.NONE;
            boolean hasSouth = south() != EnumNeighborType.NONE;
            boolean hasWest = west() != EnumNeighborType.NONE;
            boolean hasEast = east() != EnumNeighborType.NONE;
            boolean hasUp = up() != EnumNeighborType.NONE;
            boolean hasDown = down() != EnumNeighborType.NONE;

            boolean a = hasNorth && hasSouth && !hasWest && !hasEast && !hasUp && !hasDown;
            boolean b = !hasNorth && !hasSouth && hasWest && hasEast && !hasUp && !hasDown;
            boolean c = !hasNorth && !hasSouth && !hasWest && !hasEast && hasUp && hasDown;
            return !(a ^ b ^ c);
        }

        public EnumNeighborType north() {
            return neighborTypes.getOrDefault(EnumFacing.NORTH, EnumNeighborType.NONE);
        }

        public EnumNeighborType east() {
            return neighborTypes.getOrDefault(EnumFacing.EAST, EnumNeighborType.NONE);
        }

        public EnumNeighborType south() {
            return neighborTypes.getOrDefault(EnumFacing.SOUTH, EnumNeighborType.NONE);
        }

        public EnumNeighborType west() {
            return neighborTypes.getOrDefault(EnumFacing.WEST, EnumNeighborType.NONE);
        }

        public EnumNeighborType up() {
            return neighborTypes.getOrDefault(EnumFacing.UP, EnumNeighborType.NONE);
        }

        public EnumNeighborType down() {
            return neighborTypes.getOrDefault(EnumFacing.DOWN, EnumNeighborType.NONE);
        }
    }
}
