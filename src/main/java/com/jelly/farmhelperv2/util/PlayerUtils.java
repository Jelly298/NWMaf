package com.jelly.farmhelperv2.util;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.config.struct.Rewarp;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class PlayerUtils {

    public static final Clock changeItemEveryClock = new Clock();
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean itemChangedByStaff = false;

    public static boolean isInventoryEmpty(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInventoryFull(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) == null) {
                return false;
            }
        }
        return true;
    }

    public static FarmHelperConfig.CropEnum getFarmingCrop() {
        Pair<Block, BlockPos> closestCrop = null;
        boolean foundCropUnderMouse = false;
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();
            if (mc.theWorld == null) return FarmHelperConfig.CropEnum.NONE;
            if (mc.theWorld.getBlockState(pos) == null) return FarmHelperConfig.CropEnum.NONE;
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block instanceof BlockCrops || block instanceof BlockReed || block instanceof BlockCocoa || block instanceof BlockNetherWart || block instanceof BlockMelon || block instanceof BlockPumpkin || block instanceof BlockMushroom || block instanceof BlockCactus) {
                closestCrop = Pair.of(block, pos);
                foundCropUnderMouse = true;
            }
        }

        if (!foundCropUnderMouse) {
            for (int x = -3; x < 3; x++) {
                for (int y = -1; y < 5; y++) {
                    for (int z = 0; z < 3; z++) {
                        float yaw;
                        if (MacroHandler.getInstance().getCurrentMacro().isPresent()) {
                            yaw = MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().orElse(AngleUtils.getClosest());
                        } else {
                            if (FarmHelperConfig.getMacro() == FarmHelperConfig.MacroEnum.S_MUSHROOM || FarmHelperConfig.getMacro() == FarmHelperConfig.MacroEnum.S_SUGAR_CANE || FarmHelperConfig.getMacro() == FarmHelperConfig.MacroEnum.S_SUGAR_CANE_MELONKINGDE) {
                                yaw = AngleUtils.getClosestDiagonal();
                            } else {
                                yaw = AngleUtils.getClosest();
                            }
                        }
                        BlockPos pos = BlockUtils.getRelativeBlockPos(x, y, z, yaw);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (!(block instanceof BlockCrops || block instanceof BlockReed || block instanceof BlockCocoa || block instanceof BlockNetherWart || block instanceof BlockMelon || block instanceof BlockPumpkin || block instanceof BlockMushroom || block instanceof BlockCactus))
                            continue;

                        if (closestCrop == null || mc.thePlayer.getPositionVector().distanceTo(new Vec3(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f)) < mc.thePlayer.getPositionVector().distanceTo(new Vec3(closestCrop.getRight().getX() + 0.5f, closestCrop.getRight().getY(), closestCrop.getRight().getZ() + 0.5f))) {
                            closestCrop = Pair.of(block, pos);
                        }
                    }
                }
            }
        }

        if (closestCrop != null) {
            Block left = closestCrop.getLeft();
            if (left.equals(Blocks.wheat)) {
                return FarmHelperConfig.CropEnum.WHEAT;
            } else if (left.equals(Blocks.carrots)) {
                return FarmHelperConfig.CropEnum.CARROT;
            } else if (left.equals(Blocks.potatoes)) {
                return FarmHelperConfig.CropEnum.POTATO;
            } else if (left.equals(Blocks.nether_wart)) {
                return FarmHelperConfig.CropEnum.NETHER_WART;
            } else if (left.equals(Blocks.reeds)) {
                return FarmHelperConfig.CropEnum.SUGAR_CANE;
            } else if (left.equals(Blocks.cocoa)) {
                return FarmHelperConfig.CropEnum.COCOA_BEANS;
            } else if (left.equals(Blocks.melon_block)) {
                return FarmHelperConfig.CropEnum.MELON;
            } else if (left.equals(Blocks.pumpkin)) {
                return FarmHelperConfig.CropEnum.PUMPKIN;
            } else if (left.equals(Blocks.red_mushroom)) {
                return FarmHelperConfig.CropEnum.MUSHROOM;
            } else if (left.equals(Blocks.brown_mushroom)) {
                return FarmHelperConfig.CropEnum.MUSHROOM;
            } else if (left.equals(Blocks.cactus)) {
                return FarmHelperConfig.CropEnum.CACTUS;
            }
        }
        LogUtils.sendError("Can't detect crop type! Lower average BPS failsafe will be disabled!");
        return FarmHelperConfig.CropEnum.NONE;
    }

    public static void getTool() {
        // Sometimes if staff changed your slot, you might not have the tool in your hand after the swap, so it won't be obvious that you're using a macro
        if (itemChangedByStaff) {
            LogUtils.sendDebug("Item changed by staff, not changing item");
            return;
        }

        if (changeItemEveryClock.isScheduled() && !changeItemEveryClock.passed()) {
            return;
        }

        changeItemEveryClock.schedule(1_500L);
        int id = PlayerUtils.getFarmingTool(MacroHandler.getInstance().getCrop(), true, false);
        if (id == -1) {
            LogUtils.sendDebug("No tool found! Trying to find any tool.");
            id = PlayerUtils.getFarmingTool(MacroHandler.getInstance().getCrop(), true, true);
        }
        if (id == -1) {
            LogUtils.sendError("No tool found!");
            return;
        }
        if (id == mc.thePlayer.inventory.currentItem) return;
        mc.thePlayer.inventory.currentItem = id;
    }

    public static FarmHelperConfig.CropEnum getCropBasedOnMouseOver() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return FarmHelperConfig.CropEnum.NONE;
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block.equals(Blocks.wheat)) {
            return FarmHelperConfig.CropEnum.WHEAT;
        } else if (block.equals(Blocks.carrots)) {
            return FarmHelperConfig.CropEnum.CARROT;
        } else if (block.equals(Blocks.potatoes)) {
            return FarmHelperConfig.CropEnum.POTATO;
        } else if (block.equals(Blocks.nether_wart)) {
            return FarmHelperConfig.CropEnum.NETHER_WART;
        } else if (block.equals(Blocks.reeds)) {
            return FarmHelperConfig.CropEnum.SUGAR_CANE;
        } else if (block.equals(Blocks.cocoa)) {
            return FarmHelperConfig.CropEnum.COCOA_BEANS;
        } else if (block.equals(Blocks.melon_block)) {
            return FarmHelperConfig.CropEnum.MELON;
        } else if (block.equals(Blocks.pumpkin)) {
            return FarmHelperConfig.CropEnum.PUMPKIN;
        } else if (block.equals(Blocks.red_mushroom)) {
            return FarmHelperConfig.CropEnum.MUSHROOM;
        } else if (block.equals(Blocks.brown_mushroom)) {
            return FarmHelperConfig.CropEnum.MUSHROOM;
        } else if (block.equals(Blocks.cactus)) {
            return FarmHelperConfig.CropEnum.CACTUS;
        }
        return FarmHelperConfig.CropEnum.NONE;
    }

    public static int getFarmingTool(FarmHelperConfig.CropEnum crop, boolean withError, boolean anyHoe) {
        if (crop == null) return withError ? -1 : 0;
        for (int i = 36; i < 44; i++) {
            if (mc.thePlayer == null || mc.thePlayer.inventoryContainer.inventorySlots == null || mc.thePlayer.inventoryContainer.inventorySlots.get(i).getStack() == null)
                continue;
            NBTTagCompound tag = mc.thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getTagCompound();
            if (tag != null && tag.hasKey("ExtraAttributes")) {
                NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("id")) {
                    String name = extraAttributes.getString("id");
                    if (name == null)
                        continue;
                    if (anyHoe) {
                        if (name.contains("HOE") || name.contains("DICER") || name.contains("CHOPPER") || name.contains("FUNGI") || name.contains("DAEDALUS_AXE") || name.contains("KNIFE")) {
                            return i - 36;
                        }
                        continue;
                    }
                    switch (crop) {
                        case NETHER_WART:
                            if (name.contains("HOE_WARTS_3") || name.contains("HOE_WARTS_2") || name.contains("HOE_WARTS")) {
                                return i - 36;
                            }
                            continue;
                        case CARROT:
                            if (name.contains("HOE_CARROT_3") || name.contains("HOE_CARROT_2") || name.contains("HOE_CARROT")) {
                                return i - 36;
                            }
                            continue;
                        case WHEAT:
                            if (name.contains("HOE_WHEAT_3") || name.contains("HOE_WHEAT_2") || name.contains("HOE_WHEAT")) {
                                return i - 36;
                            }
                            continue;
                        case POTATO:
                            if (name.contains("HOE_POTATO_3") || name.contains("HOE_POTATO_2") || name.contains("HOE_POTATO")) {
                                return i - 36;
                            }
                            continue;
                        case SUGAR_CANE:
                            if (name.contains("HOE_CANE_3") || name.contains("HOE_CANE_2") || name.contains("HOE_CANE")) {
                                return i - 36;
                            }
                            continue;
                        case CACTUS:
                            if (name.contains("CACTUS_KNIFE")) {
                                return i - 36;
                            }
                            continue;
                        case MUSHROOM:
                            if (name.contains("FUNGI_CUTTER") || name.contains("DAEDALUS_AXE")) {
                                return i - 36;
                            }
                            continue;
                        case PUMPKIN_MELON_UNKNOWN:
                            if (name.contains("_DICER")) {
                                return i - 36;
                            }
                            continue;
                        case MELON:
                            if (name.contains("MELON_DICER_3") || name.contains("MELON_DICER_2") || name.contains("MELON_DICER")) {
                                return i - 36;
                            }
                        case PUMPKIN:
                            if (name.contains("PUMPKIN_DICER_3") || name.contains("PUMPKIN_DICER_2") || name.contains("PUMPKIN_DICER")) {
                                return i - 36;
                            }
                            continue;
                        case COCOA_BEANS:
                            if (name.contains("COCO_CHOPPER")) {
                                return i - 36;
                            }
                    }
                }
            }
        }

        int gardeningHoe = InventoryUtils.getSlotIdOfItemInHotbar("Gardening Hoe", "Gardening Axe");
        if (gardeningHoe != -1) {
            return gardeningHoe;
        }

        return withError ? -1 : 0;
    }

    public static boolean isRewarpLocationSet() {
        return !FarmHelperConfig.rewarpList.isEmpty();
    }

    public static boolean isStandingOnRewarpLocation() {
        if (FarmHelperConfig.rewarpList.isEmpty()) return false;
        Rewarp closest = null;
        double closestDistance = Double.MAX_VALUE;
        BlockPos playerPos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        for (Rewarp rewarp : FarmHelperConfig.rewarpList) {
            double distance = rewarp.getDistance(playerPos);
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest == null) return false;
        return closest.isTheSameAs(playerPos);
    }

    public static boolean shouldPushBack() {
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return false;
        float angle = AngleUtils.getClosest();
        double x = mc.thePlayer.posX % 1;
        double z = mc.thePlayer.posZ % 1;
        Block blockBehind = BlockUtils.getRelativeBlock(0, 0, -1);
        if (!(blockBehind.getMaterial().isSolid() || (blockBehind instanceof BlockSlab) || blockBehind.equals(Blocks.carpet) || (blockBehind instanceof BlockDoor)) || blockBehind.getMaterial().isLiquid())
            return false;
        if (angle == 0) {
            return (z > -0.65 && z < -0.1) || (z < 0.9 && z > 0.35);
        } else if (angle == 90) {
            return (x > -0.9 && x < -0.35) || (x < 0.65 && x > 0.1);
        } else if (angle == 180) {
            return (z > -0.9 && z < -0.35) || (z < 0.65 && z > 0.1);
        } else if (angle == 270) {
            return (x > -0.65 && x < -0.1) || (x < 0.9 && x > 0.35);
        }
        return false;
    }

    public static boolean shouldWalkForwards() {
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return false;
        if (MacroHandler.getInstance().getCrop() == FarmHelperConfig.CropEnum.CACTUS ||
                (FarmHelperConfig.getMacro() != FarmHelperConfig.MacroEnum.S_PUMPKIN_MELON_MELONGKINGDE && (MacroHandler.getInstance().getCrop() == FarmHelperConfig.CropEnum.PUMPKIN || MacroHandler.getInstance().getCrop() == FarmHelperConfig.CropEnum.MELON)) ||
                (MacroHandler.getInstance().getCrop() == FarmHelperConfig.CropEnum.COCOA_BEANS && FarmHelperConfig.getMacro() == FarmHelperConfig.MacroEnum.S_COCOA_BEANS_LEFT_RIGHT))
            return false;

        float angle = AngleUtils.getClosest();
        double x = mc.thePlayer.posX % 1;
        double z = mc.thePlayer.posZ % 1;
        float yaw;
        if (MacroHandler.getInstance().getCurrentMacro().isPresent() && MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().isPresent()) {
            yaw = MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().get();
        } else {
            yaw = mc.thePlayer.rotationYaw;
        }
        if (BlockUtils.canWalkThrough(BlockUtils.getRelativeBlockPos(0, 0, 1, yaw))) {
            return false;
        }
        if (angle == 0) {
            return (z > -0.9 && z < -0.35) || (z < 0.65 && z > 0.1);
        } else if (angle == 90) {
            return (x > -0.65 && x < -0.1) || (x < 0.9 && x > 0.35);
        } else if (angle == 180) {
            return (z > -0.65 && z < -0.1) || (z < 0.9 && z > 0.35);
        } else if (angle == 270) {
            return (x > -0.9 && x < -0.35) || (x < 0.65 && x > 0.1);
        }
        return false;
    }

    public static boolean isSpawnLocationSet() {
        return FarmHelperConfig.spawnPosX != 0 || FarmHelperConfig.spawnPosY != 0 || FarmHelperConfig.spawnPosZ != 0;
    }

    public static boolean isStandingOnSpawnPoint() {
        BlockPos pos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        BlockPos spawnPoint = new BlockPos(FarmHelperConfig.spawnPosX + 0.5, FarmHelperConfig.spawnPosY + 0.5, FarmHelperConfig.spawnPosZ + 0.5);
        return pos.equals(spawnPoint);
    }

    public static Vec3 getSpawnLocation() {
        return new Vec3(FarmHelperConfig.spawnPosX + 0.5, FarmHelperConfig.spawnPosY + 0.5, FarmHelperConfig.spawnPosZ + 0.5);
    }

    public static void setSpawnLocation() {
        if (mc.thePlayer == null) return;
        BlockPos pos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        FarmHelperConfig.spawnPosX = pos.getX();
        FarmHelperConfig.spawnPosY = pos.getY();
        FarmHelperConfig.spawnPosZ = pos.getZ();
        FarmHelper.config.save();
    }

    public static Entity getEntityCuttingOtherEntity(Entity e) {
        return getEntityCuttingOtherEntity(e, entity -> true);
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Predicate<Entity> predicate) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
            boolean flag2 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag4 = predicate.test(a);
            return flag1 && flag2 && flag3 && flag4;
        });
        if (!possible.isEmpty())
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static boolean isPlayerSuffocating() {
        AxisAlignedBB playerBB = mc.thePlayer.getEntityBoundingBox().expand(-0.15, -0.15, -0.15);
        List<AxisAlignedBB> collidingBoxes = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, playerBB);
        return !collidingBoxes.isEmpty();
    }

    public static EnumFacing getHorizontalFacing(float yaw) {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5) & 3);
    }

    public static void closeScreen() {
        if (mc.currentScreen != null && mc.thePlayer != null) {
            mc.thePlayer.closeScreen();
        }
    }

    public static boolean isInBarn() {
        BlockPos barn1 = new BlockPos(-30, 65, -45);
        BlockPos barn2 = new BlockPos(36, 80, -2);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(barn1, barn2);
        return axisAlignedBB.isVecInside(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }

    public static Vec3 getClosestVecAround(Entity entity, double distance) {
        return getClosestVecAround(entity, distance, 20, 0);
    }

    public static Vec3 getClosestVecAround(Entity entity, double distance, int angleStep, int angleStart) {
        Vec3 closest = null;
        for (int i = angleStart; i <= 360; i += angleStep) {
            double x = entity.posX + distance * Math.cos(Math.toRadians(i));
            double z = entity.posZ + distance * Math.sin(Math.toRadians(i));
            Vec3 vec1 = new Vec3(x, entity.posY + 0.6, z);
            if ((closest == null || vec1.distanceTo(mc.thePlayer.getPositionVector()) < closest.distanceTo(mc.thePlayer.getPositionVector())) && !BlockUtils.hasCollision(new BlockPos(vec1))) {
                closest = vec1;
            }
        }
        return closest;
    }
}
