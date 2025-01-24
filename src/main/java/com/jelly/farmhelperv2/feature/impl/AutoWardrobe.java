package com.jelly.farmhelperv2.feature.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.InventoryUtils.ClickMode;
import com.jelly.farmhelperv2.util.InventoryUtils.ClickType;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoWardrobe implements IFeature {

    public static AutoWardrobe instance = new AutoWardrobe();
    public static int activeSlot = -1;
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean enabled = false;
    private int swapTo = -1;
    private State state = State.STARTING;
    private Clock timer = new Clock();

    @Override
    public String getName() {
        return "AutoWardrobe";
    }

    @Override
    public boolean isRunning() {
        return enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return false;
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        activeSlot = -1;
    }

    @Override
    public boolean isToggled() {
        return true;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    public void swapTo(int slot) {
        if (slot < 1 || slot > 18) {
            return;
        }

        swapTo = slot;
        enabled = true;
        LogUtils.sendSuccess("[AutoWardrobe] Starting. Swapping to slot " + slot);
    }

    @Override
    public void stop() {
        if (!enabled) {
            return;
        }
        enabled = false;
        swapTo = -1;
        state = State.STARTING;
        timer.reset();

        LogUtils.sendSuccess("[AutoWardrobe] Stopping.");
    }

    public void setState(State state, long time) {
        this.state = state;
        timer.schedule(time);
        if (time == 0) {
            timer.reset();
        }
    }

    public boolean isTimerRunning() {
        return timer.isScheduled() && !timer.passed();
    }

    public boolean hasTimerEnded() {
        return !timer.isScheduled() || timer.passed();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!enabled) {
            return;
        }

        switch (state) {
            case STARTING:
                setState(State.OPENING_WD, FarmHelperConfig.getRandomGUIMacroDelay());
                break;
            case OPENING_WD:
                if (isTimerRunning()) {
                    return;
                }

                mc.thePlayer.sendChatMessage("/wd");
                setState(State.WD_VERIFY, 2000);
                break;
            case WD_VERIFY:
                if (hasTimerEnded()) {
                    LogUtils.sendError("Could not open wardrobe in under 2 seconds. Stopping");
                    setState(State.WAITING, 0);
                    return;
                }

                if (inventoryName().startsWith("Wardrobe")) {
                    setState(State.NAVIGATING, FarmHelperConfig.getRandomGUIMacroDelay());
                }
                break;
            case NAVIGATING:
                if (isTimerRunning()) {
                    return;
                }
                if (swapTo < 9) {
                    setState(State.CLICKING_SLOT, 0);
                    return;
                }
                InventoryUtils.clickContainerSlot(InventoryUtils.getSlotIdOfItemInContainer("Next Page"), ClickType.LEFT, ClickMode.PICKUP);
                setState(State.NAVIGATION_VERIFY, 2000);
                break;
            case NAVIGATION_VERIFY:
                if (hasTimerEnded()) {
                    LogUtils.sendError("Could not switch to next page in under 2 seconds. Stopping");
                    setState(State.WAITING, 0);
                    return;
                }

                if (inventoryName().endsWith("2)")) {
                    setState(State.CLICKING_SLOT, FarmHelperConfig.getRandomGUIMacroDelay());
                }
                break;
            case CLICKING_SLOT:
                if (isTimerRunning()) {
                    return;
                }
                int slotId = 35 + (swapTo - 1) % 9 + 1;
                Slot slot = InventoryUtils.getSlotOfIdInContainer(slotId);
                if (slot != null && slot.getHasStack()) {
                    ItemStack stack = slot.getStack();
                    // remove this to make it unequip armor (click the same slot)
                    if (stack.hasDisplayName() && !stack.getDisplayName().contains("Equipped") && !stack.getDisplayName().contains("Locked")) {
                        InventoryUtils.clickContainerSlot(35 + (swapTo - 1) % 9 + 1, ClickType.LEFT, ClickMode.PICKUP);
                    }
                }
                activeSlot = swapTo;
                setState(State.WAITING, FarmHelperConfig.getRandomGUIMacroDelay());
                break;
            case WAITING:
                if (isTimerRunning()) {
                    return;
                }
                PlayerUtils.closeScreen();
                setState(State.ENDING, FarmHelperConfig.getRandomGUIMacroDelay());
                break;
            // this is just here to give a bit extra pause before it stops
            case ENDING:
              if (isTimerRunning()) {
                return;
              }
                stop();
                break;
        }
    }

    private String inventoryName() {
        try {
            if (mc.currentScreen instanceof GuiChest) {
                final ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                if (chest == null) {
                    return "";
                }
                final IInventory inv = chest.getLowerChestInventory();
                return inv.hasCustomName() ? inv.getName() : "";
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }


    enum State {
        STARTING, OPENING_WD, WD_VERIFY, NAVIGATING, NAVIGATION_VERIFY, CLICKING_SLOT, WAITING, ENDING
    }
}
