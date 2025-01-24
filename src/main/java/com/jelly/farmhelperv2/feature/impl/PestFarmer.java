package com.jelly.farmhelperv2.feature.impl;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class PestFarmer implements IFeature {

    public static PestFarmer instance = new PestFarmer();
    private boolean enabled = false;
    private long pestSpawnTime = 0L;
    private int swapTo = -1;
    private State state = State.SWAPPING;
    private boolean pestSpawned = false;
    private boolean holdDae = false;

    @Override
    public String getName() {
        return "PestFarmer";
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
        state = State.SWAPPING;
        pestSpawned = false;
        holdDae = false;
        swapTo = -1;
    }

    @Override
    public boolean isToggled() {
        return FarmHelperConfig.pestFarming;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    @Override
    public void start() {
        if (enabled) {
            return;
        }
        MacroHandler.getInstance().pauseMacro();
        enabled = true;
    }

    @Override
    public void stop() {
        if (!enabled) {
            return;
        }

        enabled = false;
        state = State.SWAPPING;
        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().resumeMacro();
        }
    }

    public boolean holdDaedalus() {
        return holdDae && FarmHelperConfig.pestFarming && FarmHelperConfig.pestFarmingHoldDaedalus
                && AutoWardrobe.activeSlot == FarmHelperConfig.pestFarmingSet1Slot && InventoryUtils.holdItem("Daedalus Axe");
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != Phase.START) {
            return;
        }
        if (!this.isToggled() || !MacroHandler.getInstance().isCurrentMacroEnabled()
                || MacroHandler.getInstance().getCurrentMacro().get().currentState.ordinal() < 4 || enabled) {
            return;
        }
        if (!GameStateHandler.getInstance().inGarden()) {
            return;
        }
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) {
            return;
        }
        if (!Scheduler.getInstance().isFarming()) {
            return;
        }
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            return;
        }
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) {
            return;
        }

        if (pestSpawned) {
            long timeDiff = System.currentTimeMillis() - pestSpawnTime;
            if (timeDiff >= FarmHelperConfig.pestFarmingWaitTime * 1000L && AutoWardrobe.activeSlot != FarmHelperConfig.pestFarmingSet1Slot) {
                LogUtils.sendDebug("Swapping to " + FarmHelperConfig.pestFarmingSet1Slot);
                swapTo = FarmHelperConfig.pestFarmingSet1Slot;
                pestSpawned = false;
                holdDae = true;
                start();
            } else if (AutoWardrobe.activeSlot != FarmHelperConfig.pestFarmingSet0Slot) {
                LogUtils.sendDebug("Swapping to " + FarmHelperConfig.pestFarmingSet0Slot);
                swapTo = FarmHelperConfig.pestFarmingSet0Slot;
                holdDae = false;
                start();
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0) {
            return;
        }
        String message = event.message.getUnformattedText();
        if (message.startsWith("§6§lYUCK!") || message.startsWith("§6§lEWW!") || message.startsWith("§6§lGROSS!")) {
            pestSpawnTime = System.currentTimeMillis();
            pestSpawned = true;
            LogUtils.sendDebug("[PestFarmer] Pest Spawned.");
        }
    }

    @SubscribeEvent
    public void onTickSwap(ClientTickEvent event) {
        if (!enabled) {
            return;
        }

        switch (state) {
            case SWAPPING:
                AutoWardrobe.instance.swapTo(swapTo);
                state = State.ENDING;
                break;
            case ENDING:
                if (AutoWardrobe.instance.isRunning()) {
                    return;
                }
                stop();
                break;
        }
    }

    // bleh, its only for the tracker basically
    enum State {
        SWAPPING,
        ENDING
    }
}
