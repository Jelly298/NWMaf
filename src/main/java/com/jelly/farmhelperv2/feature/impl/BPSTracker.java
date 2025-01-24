package com.jelly.farmhelperv2.feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.PlayerDestroyBlockEvent;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class BPSTracker implements IFeature {
    private static BPSTracker instance;
    public final ConcurrentLinkedDeque<Tuple<Long, Long>> bpsQueue = new ConcurrentLinkedDeque<>();
    public long blocksBroken = 0;
    public long totalBlocksBroken = 0;
    private final NumberFormat oneDecimalDigitFormatter = NumberFormat.getNumberInstance(Locale.US);

    private BPSTracker() {
        oneDecimalDigitFormatter.setMaximumFractionDigits(1);
    }

    public static BPSTracker getInstance() {
        if (instance == null) {
            instance = new BPSTracker();
        }
        return instance;
    }

    public boolean isPaused = false;
    public boolean isResumingScheduled = false;
    public long pauseStartTime = 0; // used for BPS adjustment after the break
    public float lastKnownBPS = 0;


    public void pause() {
        if (!isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
            lastKnownBPS = getBPSFloat();
        }
    }

    private void adjustQueueTimestamps(long pauseDuration) {
        LinkedList<Tuple<Long, Long>> adjustedQueue = new LinkedList<>();
        for (Tuple<Long, Long> element : bpsQueue) {
            adjustedQueue.add(new Tuple<>(element.getFirst(), element.getSecond() + pauseDuration));
        }
        bpsQueue.clear();
        bpsQueue.addAll(adjustedQueue);
    }

    @Override
    public void resume() {
        if (isPaused && !isResumingScheduled) {
            isResumingScheduled = true;
            Multithreading.schedule(() -> {
                isResumingScheduled = false;
                if (dontCheckForBPS()) {
                    return;
                }
                long pauseDuration = System.currentTimeMillis() - pauseStartTime;
                if (pauseDuration < 0 || pauseDuration > 3600000) {
                    LogUtils.sendDebug("BPSTracker: Invalid pause duration: " + pauseDuration + "ms. Ignoring.");
                } else {
                    adjustQueueTimestamps(pauseDuration);
                }
                isPaused = false;
                pauseStartTime = 0;
            }, 1000L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void start() {
        bpsQueue.clear();
        totalBlocksBroken = 0;
        IFeature.super.start();
    }

    private boolean checkForBPS(AbstractMacro.State currentState) {
        return currentState.ordinal() > 3 && Minecraft.getMinecraft().currentScreen == null;
    }

    public float elapsedTime = 0;

    @SubscribeEvent
    public void onTickCheckBPS(TickEvent.ClientTickEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (dontCheckForBPS())
            pause();
        else
            resume();
        if (isPaused) return;

        long currentTime = System.currentTimeMillis();
        bpsQueue.add(new Tuple<>(blocksBroken, currentTime));
        blocksBroken = 0;

        while (!bpsQueue.isEmpty() && bpsQueue.getFirst() == null) {
            bpsQueue.removeFirst();
        }

        if (bpsQueue.size() > 1) {
            // added small epsilon to prevent division by very small numbers and zero
            elapsedTime = Math.max((bpsQueue.getLast().getSecond() - bpsQueue.getFirst().getSecond()) / 1000f, 0.001f);
            while (elapsedTime > 10f && bpsQueue.size() > 1) {
                bpsQueue.pollFirst();
                elapsedTime = (currentTime - bpsQueue.getFirst().getSecond()) / 1000f;
            }

            totalBlocksBroken = 0;
            for (Tuple<Long, Long> element : bpsQueue) {
                totalBlocksBroken += element.getFirst();
            }
            totalBlocksBroken -= bpsQueue.getFirst().getFirst();
        }
    }

    public String getBPS() {
        if (isPaused)
            return oneDecimalDigitFormatter.format(getBPSFloat()) + " BPS (Paused)";
        return oneDecimalDigitFormatter.format(getBPSFloat()) + " BPS";
    }

    public boolean dontCheckForBPS() {
        return !MacroHandler.getInstance().getMacroingTimer().isScheduled()
                || MacroHandler.getInstance().isCurrentMacroPaused()
                || !MacroHandler.getInstance().isCurrentMacroEnabled()
                || MacroHandler.getInstance().isTeleporting()
                || MacroHandler.getInstance().isRewarpTeleport()
                || MacroHandler.getInstance().isStartingUp()
                || !checkForBPS(MacroHandler.getInstance().getMacro().getCurrentState())
                || MacroHandler.getInstance().getCrop() == FarmHelperConfig.CropEnum.NONE;
    }

    public float getBPSFloat() {
        if (!MacroHandler.getInstance().getMacroingTimer().isScheduled()) return 0;
        if (dontCheckForBPS() || isPaused || bpsQueue.size() < 2) {
            return lastKnownBPS;
        }

        float elapsedTime = (bpsQueue.getLast().getSecond() - bpsQueue.getFirst().getSecond()) / 1000f;
        lastKnownBPS = totalBlocksBroken == 0 ? 0.1f : Math.max(((int) ((double) this.totalBlocksBroken / elapsedTime * 10.0D)) / 10.0F, 0.1f);
        return lastKnownBPS;
    }

    @SubscribeEvent
    public void onBlockChange(PlayerDestroyBlockEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (dontCheckForBPS() || isPaused) return;

        switch (MacroHandler.getInstance().getCrop()) {
            case NETHER_WART:
            case CARROT:
            case POTATO:
            case WHEAT:
                if (event.block instanceof BlockCrops ||
                        event.block instanceof BlockNetherWart) {
                    blocksBroken++;
                }
                break;
            case SUGAR_CANE:
                if (event.block instanceof BlockReed) {
                    blocksBroken++;
                }
                break;
            case MELON:
                if (event.block.equals(Blocks.melon_block)) {
                    blocksBroken++;
                }
                break;
            case PUMPKIN:
                if (event.block.equals(Blocks.pumpkin)) {
                    blocksBroken++;
                }
                break;
            case CACTUS:
                if (event.block.equals(Blocks.cactus)) {
                    blocksBroken++;
                }
                break;
            case COCOA_BEANS:
                if (event.block.equals(Blocks.cocoa)) {
                    blocksBroken++;
                }
                break;
            case MUSHROOM:
                if (event.block.equals(Blocks.red_mushroom) ||
                        event.block.equals(Blocks.brown_mushroom)) {
                    blocksBroken++;
                }
                break;
        }
    }

    @Override
    public String getName() {
        return "BPSTracker";
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return false;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return true;
    }

    @Override
    public void stop() {
        IFeature.super.stop();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        bpsQueue.clear();
        totalBlocksBroken = 0;
        blocksBroken = 0;
        lastKnownBPS = 0;
        isPaused = false;
        isResumingScheduled = false;
        pauseStartTime = 0;
    }

    @Override
    public boolean isToggled() {
        return true;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }
}