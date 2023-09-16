package com.jelly.farmhelper.remote.command.commands;

import com.google.gson.JsonObject;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.remote.struct.BaseCommand;
import com.jelly.farmhelper.remote.struct.Command;
import com.jelly.farmhelper.remote.struct.RemoteMessage;
import com.jelly.farmhelper.utils.PlayerUtils;
import com.jelly.farmhelper.utils.Utils;
import com.jelly.farmhelper.player.Rotation;
import com.jelly.farmhelper.utils.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;

@Command(label = "setspeed")
public class SetSpeedCommand extends BaseCommand {
    private static int speed = -1;
    private static JsonObject data;

    @Override
    public void execute(RemoteMessage message) {
        JsonObject args = message.args;
        speed = args.get("speed").getAsInt();
        data = new JsonObject();
        data.addProperty("username", mc.getSession().getUsername());
        data.addProperty("uuid", mc.getSession().getPlayerID());
        data.addProperty("speed", speed);

        if (PlayerUtils.getRancherBootSpeed() == -1) {
            data.addProperty("error", "You need to have Rancher's boots equipped for this command to work.");
        } else if (PlayerUtils.getRancherBootSpeed() == speed) {
            data.addProperty("error", "Your Rancher's boots are already at " + speed + " speed.");
        } else {
            try {
                enabled = true;
                currentState = State.START;
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RemoteMessage response = new RemoteMessage(label, data);
        send(response);
    }

    private static boolean enabled = false;
    private static State currentState = State.NONE;
    private static final Clock clock = new Clock();
    private static final Rotation rotation = new Rotation();
    private static Pair<Float, Float> lastRotation = new MutablePair<>(0f, 0f);
    int freeSlot = -1;
    int freeHotbarSlot = -1;
    boolean wasMacroing = false;
    public enum State {
        NONE,
        START,
        OPEN_INVENTORY,
        TAKE_OFF_BOOTS,
        CLOSE_INVENTORY,
        LOOK_IN_THE_AIR,
        HOLD_RANCHER_BOOTS,
        CLICK_RANCHER_BOOTS,
        TYPE_IN_SPEED,
        LOOK_BACK,
        OPEN_INVENTORY_AGAIN,
        PUT_ON_BOOTS,
        CLOSE_INVENTORY_AGAIN,
        END
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (BaseCommand.mc.thePlayer == null || BaseCommand.mc.theWorld == null) return;
        if (clock.isScheduled() && !clock.passed()) return;

        switch (currentState) {
            case NONE:
                clock.reset();
                enabled = false;
                break;
            case START:
                if (BaseCommand.mc.thePlayer.inventoryContainer.getSlot(8) == null || !BaseCommand.mc.thePlayer.inventoryContainer.getSlot(8).getStack().getDisplayName().contains("Rancher's Boots")) {
                    disableWithError("You don't wear Rancher's Boots! Disabling...");
                    break;
                }
                wasMacroing = MacroHandler.isMacroing;
                if (wasMacroing) {
                    MacroHandler.disableCurrentMacro();
                    MacroHandler.isMacroing = false;
                }
                currentState = State.OPEN_INVENTORY;
                clock.schedule(500);
                break;
            case OPEN_INVENTORY:
                PlayerUtils.openInventory();
                freeSlot = -1;
                currentState = State.TAKE_OFF_BOOTS;
                clock.schedule(500);
                break;
            case TAKE_OFF_BOOTS:
                if (BaseCommand.mc.thePlayer.openContainer == null) {
                    currentState = State.OPEN_INVENTORY;
                    clock.schedule(500);
                    break;
                }
                for (int i = 36; i < 44; i++) {
                    if (BaseCommand.mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                        LogUtils.sendDebug("Found free slot: " + i);
                        freeSlot = i;
                        freeHotbarSlot = i - 36;
                        break;
                    }
                }
                if (freeSlot == -1) {
                    disableWithError("You don't have any free slot in your hotbar! Disabling...");
                    break;
                }
                if (freeHotbarSlot < 0 || freeHotbarSlot > 7) {
                    disableWithError("Free hotbar slot is invalid! Disabling...");
                    break;
                }
                PlayerUtils.clickOpenContainerSlot(8);
                PlayerUtils.clickOpenContainerSlot(freeSlot);
                currentState = State.CLOSE_INVENTORY;
                clock.schedule(500);
                break;
            case CLOSE_INVENTORY:
                BaseCommand.mc.currentScreen = null;
                lastRotation = Pair.of(AngleUtils.get360RotationYaw(), BaseCommand.mc.thePlayer.rotationPitch);
                currentState = State.LOOK_IN_THE_AIR;
                clock.schedule(500);
                break;
            case LOOK_IN_THE_AIR:
                lookInTheAir();
                currentState = State.HOLD_RANCHER_BOOTS;
                clock.schedule(500);
                break;
            case HOLD_RANCHER_BOOTS:
                if (BaseCommand.mc.currentScreen != null) {
                    currentState = State.CLOSE_INVENTORY;
                    clock.schedule(500);
                    break;
                }
                BaseCommand.mc.thePlayer.inventory.currentItem = freeHotbarSlot;
                currentState = State.CLICK_RANCHER_BOOTS;
                clock.schedule(500);
                break;
            case CLICK_RANCHER_BOOTS:
                if (BaseCommand.mc.thePlayer.inventoryContainer.getSlot(freeSlot).getStack() == null || BaseCommand.mc.thePlayer.inventory.currentItem != freeHotbarSlot) {
                    currentState = State.HOLD_RANCHER_BOOTS;
                    clock.schedule(500);
                    break;
                }
                Utils.signText = String.valueOf(speed);
                KeyBindUtils.leftClick();
                currentState = State.TYPE_IN_SPEED;
                clock.schedule(500);
                break;
            case TYPE_IN_SPEED:
                if (BaseCommand.mc.currentScreen == null) {
                    currentState = State.CLICK_RANCHER_BOOTS;
                    clock.schedule(500);
                    break;
                }
                BaseCommand.mc.currentScreen = null;
                currentState = State.LOOK_BACK;
                clock.schedule(500);
                break;
            case LOOK_BACK:
                rotation.reset();
                rotation.easeTo(lastRotation.getLeft(), lastRotation.getRight(), 300);
                currentState = State.OPEN_INVENTORY_AGAIN;
                clock.schedule(500);
                break;
            case OPEN_INVENTORY_AGAIN:
                PlayerUtils.openInventory();
                currentState = State.PUT_ON_BOOTS;
                clock.schedule(500);
                break;
            case PUT_ON_BOOTS:
                if (BaseCommand.mc.thePlayer.openContainer == null) {
                    currentState = State.OPEN_INVENTORY_AGAIN;
                    clock.schedule(500);
                    break;
                }
                PlayerUtils.clickOpenContainerSlot(freeSlot);
                PlayerUtils.clickOpenContainerSlot(8);
                currentState = State.CLOSE_INVENTORY_AGAIN;
                clock.schedule(500);
                break;
            case CLOSE_INVENTORY_AGAIN:
                BaseCommand.mc.currentScreen = null;
                currentState = State.END;
                clock.schedule(500);
                break;
            case END:
                PlayerUtils.getFarmingTool(MacroHandler.crop, false, false);
                if (wasMacroing) {
                    MacroHandler.isMacroing = true;
                    MacroHandler.enableCurrentMacro();
                }
                LogUtils.sendSuccess("Rancher's Boots speed has been set to " + speed + ".");
                currentState = State.NONE;
                clock.reset();
                enabled = false;
                RemoteMessage response = new RemoteMessage(label, data);
                send(response);
                break;
        }
    }

    private void disableWithError(String message) {
        currentState = State.NONE;
        clock.reset();
        enabled = false;
        LogUtils.sendError(message);
        data.addProperty("error", message);
        RemoteMessage response = new RemoteMessage(label, data);
        send(response);
    }

    private final float[][] vectors = {
            {0.5F, 3.5F, 3.5F},
            {-6.5F, 1.5F, 0F},
            {6.5F, 1.5F, 0F},
            {0.5F, 7.5F, 0F}
    };
    private void lookInTheAir() {
        LogUtils.sendDebug("Looking in the air...");
        for (float[] vector : vectors) {
            Vec3 target = new Vec3(BlockUtils.getRelativeBlockPos(vector[0], vector[1], vector[2]));
            MovingObjectPosition rayTraceResult = BaseCommand.mc.theWorld.rayTraceBlocks(BaseCommand.mc.thePlayer.getPositionEyes(1), target, false, false, true);

            if (rayTraceResult != null) {
                if (rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                    BlockPos blockPos = rayTraceResult.getBlockPos();
                    if (BaseCommand.mc.theWorld.getBlockState(blockPos).getBlock() == null || BaseCommand.mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air) {
                        LogUtils.sendDebug("Looking at " + Arrays.toString(vector));
                        rotation.reset();
                        rotation.easeTo(AngleUtils.getRotation(target, true).getLeft(), AngleUtils.getRotation(target, true).getRight(), 300);
                        return;
                    }
                }
            }
            LogUtils.sendDebug("Couldn't find an empty space to look at! Trying next vector...");
        }
        disableWithError("Couldn't find an empty space to look at! Disabling...");
    }
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if (rotation.rotating) {
            rotation.update();
        }
    }
    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (enabled && event.type == 0 && event.message != null && event.message.getFormattedText().contains("§eClick in the air!")) {
            lookInTheAir();
            currentState = State.HOLD_RANCHER_BOOTS;
            clock.schedule(500);
        }
    }
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (enabled) {
            disableWithError("World change detected! Disabling...");
        }
    }
}
