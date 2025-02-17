package com.jelly.farmhelperv2.handler;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.config.struct.Rewarp;
import com.jelly.farmhelperv2.event.ReceivePacketEvent;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.failsafe.impl.WorldChangeFailsafe;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.impl.*;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.macro.impl.*;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.RenderUtils;
import com.jelly.farmhelperv2.util.helper.AudioManager;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MacroHandler {
    private static MacroHandler instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final Timer macroingTimer = new Timer();
    @Getter
    private final Timer analyticsTimer = new Timer();
    @Getter
    @Setter
    private Optional<AbstractMacro> currentMacro = Optional.empty();
    @Getter
    @Setter
    private boolean isMacroToggled = false;
    @Getter
    @Setter
    private boolean startingUp = false;
    Runnable startCurrent = () -> {
        KeyBindUtils.stopMovement();
        if (isMacroToggled()) {
            currentMacro.ifPresent(AbstractMacro::onEnable);
        }
        startingUp = false;
    };
    @Getter
    @Setter
    private FarmHelperConfig.CropEnum crop = FarmHelperConfig.CropEnum.NONE;

    public static MacroHandler getInstance() {
        if (instance == null) {
            instance = new MacroHandler();
        }
        return instance;
    }

    public boolean isCurrentMacroEnabled() {
        return currentMacro.isPresent() && currentMacro.get().isEnabledAndNoFeature();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCurrentMacroPaused() {
        return isMacroToggled() && currentMacro.isPresent() && currentMacro.get().isPaused();
    }

    public boolean isTeleporting() {
        return currentMacro.isPresent() && (currentMacro.get().getRewarpState() != AbstractMacro.RewarpState.NONE || !afterRewarpDelay.passed());
    }

    public <T extends AbstractMacro> T getMacro() {
        switch (FarmHelperConfig.getMacro()) {
            case S_V_NORMAL_TYPE:
            case S_PUMPKIN_MELON:
            case S_PUMPKIN_MELON_MELONGKINGDE:
            case S_CACTUS:
            case S_CACTUS_SUNTZU:
            case S_COCOA_BEANS_LEFT_RIGHT:
                return Macros.S_SHAPE_VERTICAL_CROP_MACRO.getMacro();
            case S_PUMPKIN_MELON_DEFAULT_PLOT:
                return Macros.S_SHAPE_MELON_PUMPKIN_DEFAULT_MACRO.getMacro();
            case S_SUGAR_CANE:
                return Macros.S_SHAPE_SUGARCANE_MACRO.getMacro();
            case S_SUGAR_CANE_MELONKINGDE:
                return Macros.S_SHAPE_SUGARCANE_MELONKINGDE_MACRO.getMacro();
            case S_COCOA_BEANS:
            case S_COCOA_BEANS_TRAPDOORS:
                return Macros.S_SHAPE_COCOA_BEAN_MACRO.getMacro();
            case S_MUSHROOM:
                return Macros.S_SHAPE_MUSHROOM_MACRO.getMacro();
            case S_MUSHROOM_ROTATE:
                return Macros.S_SHAPE_MUSHROOM_ROTATE_MACRO.getMacro();
            case S_MUSHROOM_SDS:
                return Macros.S_SHAPE_MUSHROOM_SDS.getMacro();
            case C_NORMAL_TYPE:
                return Macros.CIRCLE_CROP_MACRO.getMacro();
            default:
                throw new IllegalArgumentException("Invalid crop type: " + FarmHelperConfig.macroType);
        }
    }

    public void setMacro(AbstractMacro macro) {
        setCurrentMacro(Optional.ofNullable(macro));
    }

    public void toggleMacro() {
        if (isMacroToggled()) {
            this.disableMacro();
        } else {
            if (FlyPathFinderExecutor.getInstance().isRunning())
                FlyPathFinderExecutor.getInstance().stop();
            if (VisitorsMacro.getInstance().isRunning() || FarmHelperConfig.visitorsMacroAfkInfiniteMode) {
                if (FarmHelperConfig.visitorsMacroAfkInfiniteMode) {
                    LogUtils.sendWarning("[Visitors Macro] Disabling AFK Mode!");
                    FarmHelperConfig.visitorsMacroAfkInfiniteMode = false;
                }
                VisitorsMacro.getInstance().stop();
                return;
            }
            if (PestsDestroyer.getInstance().isRunning() || FarmHelperConfig.pestsDestroyerAfkInfiniteMode) {
                if (FarmHelperConfig.pestsDestroyerAfkInfiniteMode) {
                    LogUtils.sendWarning("[Pests Destroyer] Disabling AFK Mode!");
                    FarmHelperConfig.pestsDestroyerAfkInfiniteMode = false;
                }
                PestsDestroyer.getInstance().stop();
                return;
            }
            if (AutoComposter.getInstance().isRunning()) {
                AutoComposter.getInstance().stop();
                return;
            }
            if (AutoPestExchange.getInstance().isRunning()) {
                AutoPestExchange.getInstance().stop();
                return;
            }
            if (PlayerUtils.isInBarn()) {
                if (VisitorsMacro.getInstance().isToggled()) {
                    VisitorsMacro.getInstance().setManuallyStarted(true);
                    VisitorsMacro.getInstance().start();
                } else {
                    LogUtils.sendError("You are in the barn and have Visitors Macro disabled!");
                }
                return;
            }
            if (FailsafeManager.getInstance().isHadEmergency()) {
                if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
                    FailsafeManager.getInstance().stopFailsafes();
                }
                FailsafeManager.getInstance().setHadEmergency(false);
                FailsafeManager.getInstance().getRestartMacroAfterFailsafeDelay().reset();
                LogUtils.sendWarning("Farm manually and DO NOT restart the macro too soon! The staff might still be spectating you for a while!");
                return;
            }
            this.enableMacro();
        }
    }

    public void enableMacro() {
        if (!GameStateHandler.getInstance().inGarden()) {
            LogUtils.sendError("You must be in the garden to start the macro!");
            return;
        }

        setMacro(getMacro());
        if (!currentMacro.isPresent()) {
            LogUtils.sendError("Invalid macro type: " + FarmHelperConfig.macroType);
            return;
        }
        LogUtils.sendDebug("Selected macro: " + LogUtils.capitalize(currentMacro.get().getClass().getSimpleName()));
        PlayerUtils.closeScreen();
        LogUtils.sendSuccess("Macro enabled!");
        if (FarmHelperConfig.sendMacroEnableDisableLogs) {
            LogUtils.webhookLog("Macro enabled!");
        }

        analyticsTimer.reset();
        Multithreading.schedule(() -> {
            if (!macroingTimer.isScheduled() || FarmHelperConfig.resetStatsBetweenDisabling) {
                macroingTimer.schedule();
                macroingTimer.pause();
            }
        }, 300, TimeUnit.MILLISECONDS);

        if (mc.currentScreen != null) {
            PlayerUtils.closeScreen();
        }
        AudioManager.getInstance().setSoundBeforeChange(mc.gameSettings.getSoundLevel(SoundCategory.MASTER));

        FeatureManager.getInstance().enableAll();

        setMacroToggled(true);
        enableCurrentMacro();
        getCurrentMacro().ifPresent(cm -> cm.getCheckOnSpawnClock().reset());
        analyticsTimer.schedule();
    }

    public void disableMacro() {
        setMacroToggled(false);
        LogUtils.sendSuccess("Macro disabled!");
        if (FarmHelperConfig.sendMacroEnableDisableLogs) {
            LogUtils.webhookLog("Macro disabled!");
        }
        currentMacro.ifPresent(m -> {
            m.setSavedState(Optional.empty());
            m.getRotation().reset();
        });

        macroingTimer.pause();

        BanInfoWS.getInstance().saveStats();
        analyticsTimer.pause();

        setCrop(FarmHelperConfig.CropEnum.NONE);
        FeatureManager.getInstance().disableAll();
        FeatureManager.getInstance().resetAllStates();
        FailsafeManager.getInstance().resetAfterMacroDisable();
        if (UngrabMouse.getInstance().isToggled())
            UngrabMouse.getInstance().stop();
        disableCurrentMacro();
        setCurrentMacro(Optional.empty());
    }

    public void pauseMacro(boolean scheduler) {
        currentMacro.ifPresent(cm -> {
            KeyBindUtils.stopMovement();
            if (cm.isPaused()) return;
            cm.saveState();
            cm.onDisable();
            beforeTeleportationPos = Optional.empty();
            macroingTimer.pause();
            analyticsTimer.pause();
            if (scheduler && Freelook.getInstance().isRunning()) {
                Freelook.getInstance().stop();
            }
            if (Scheduler.getInstance().isFarming())
                Scheduler.getInstance().pause();
            if (!BPSTracker.getInstance().isPaused)
                BPSTracker.getInstance().pause();
        });
    }

    public void pauseMacro() {
        pauseMacro(false);
    }

    @Getter
    private boolean resume = false;

    public void resumeMacro() {
        currentMacro.ifPresent(cm -> {
            if (!cm.isPaused()) return;
            mc.inGameHasFocus = true;
            resume = true;
            cm.onEnable();
            resume = false;
            PlayerUtils.getTool();
            macroingTimer.resume();
            analyticsTimer.resume();
            afterRewarpDelay.reset();
//            Scheduler.getInstance().resume(); - gets enabled in featuremanager
            FeatureManager.getInstance().resume();
        });
    }

    public void enableCurrentMacro() {
        if (currentMacro.isPresent() && !currentMacro.get().isEnabledAndNoFeature() && !startingUp) {
            mc.thePlayer.closeScreen();
            // Fixed issue #180 for Mac users (mouse vanishing glitch)
            if (!System.getProperty("os.name").contains("Mac")) {
                mc.inGameHasFocus = true;
                mc.mouseHelper.grabMouseCursor();
            }
            startingUp = true;
            PlayerUtils.itemChangedByStaff = false;
            PlayerUtils.changeItemEveryClock.reset();
            KeyBindUtils.holdThese(mc.thePlayer.capabilities.isFlying ? mc.gameSettings.keyBindSneak : null);
            Multithreading.schedule(() -> {
                startCurrent.run();
                macroingTimer.resume();
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    public void disableCurrentMacro() {
        currentMacro.ifPresent(AbstractMacro::onDisable);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || !isMacroToggled()) {
            return;
        }
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) {
            KeyBindUtils.stopMovement();
            return;
        }

        if (!GameStateHandler.getInstance().inGarden() && !this.isTeleporting()) {
            if (!FeatureManager.getInstance().shouldIgnoreFalseCheck() && !FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
                FailsafeManager.getInstance().possibleDetection(WorldChangeFailsafe.getInstance());
            }
            return;
        }
        onTickCheckTeleport();
        currentMacro.ifPresent(cm -> {
            if (!cm.isEnabledAndNoFeature()) return;
            cm.onTick();
        });
    }

    @SubscribeEvent
    public void onChatMessageReceived(ClientChatReceivedEvent event) {
        if (event.type == 0) {
            String message = event.message.getUnformattedText();
            if (!message.contains(":") && GameStateHandler.getInstance().inGarden()) {
                if (message.equals("Your spawn location has been set!")) {
                    PlayerUtils.setSpawnLocation();
                    for (Rewarp rewarp : FarmHelperConfig.rewarpList) {
                        if (mc.thePlayer.getDistance(rewarp.x, rewarp.y, rewarp.z) < 2) {
                            LogUtils.sendWarning("Spawn location is close to the rewarp location! Removing it from the list...");
                            FarmHelperConfig.rewarpList.remove(rewarp);
                            break;
                        }
                    }
                }
            }
        }

        if (!isMacroToggled()) {
            return;
        }

        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onChatMessageReceived(event.message.getUnformattedText());
        });
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (FarmHelperConfig.streamerMode) return;

        if (FarmHelperConfig.highlightRewarp && FarmHelperConfig.rewarpList != null && GameStateHandler.getInstance().inGarden()) {
            for (Rewarp rewarp : FarmHelperConfig.rewarpList) {
                double distance = mc.thePlayer.getDistance(rewarp.x, rewarp.y, rewarp.z);
                Color rewarpColor = FarmHelperConfig.rewarpColor.toJavaColor();
                if (distance < 50) {
                    RenderUtils.drawBlockBox(new BlockPos(rewarp.x, rewarp.y, rewarp.z), rewarpColor);
                }
            }
        }

        if (FarmHelperConfig.drawSpawnLocation && PlayerUtils.isSpawnLocationSet() && GameStateHandler.getInstance().inGarden()) {
            BlockPos spawnLocation = new BlockPos(PlayerUtils.getSpawnLocation());
            double distance = mc.thePlayer.getDistance(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
            Color spawnColor = FarmHelperConfig.spawnColor.toJavaColor();
            if (distance < 50) {
                RenderUtils.drawBlockBox(spawnLocation, spawnColor);
            }
        }

        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onLastRender();
        });
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!isMacroToggled()) {
            return;
        }

        currentMacro.ifPresent(m -> {
            if (m.getRewarpState().equals(AbstractMacro.RewarpState.TELEPORTING)) {
                long timeLeft = MacroHandler.getInstance().getLastTpTry() + 5_000 - System.currentTimeMillis();
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                String formattedTime = LogUtils.formatTime(timeLeft);
                if (!FarmHelperConfig.streamerMode)
                    RenderUtils.drawCenterTopText("Retrying to teleport in: " + formattedTime, event, Color.orange, 1.5f);
            }
            if (!m.isEnabledAndNoFeature()) return;
            m.onOverlayRender(event);
        });
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onPacketReceived(event);
        });
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            m.setCurrentState(AbstractMacro.State.NONE);
            m.getSavedState().ifPresent(ss -> ss.setState(AbstractMacro.State.NONE));
        });
    }

    @Setter
    private Optional<BlockPos> beforeTeleportationPos = Optional.empty();
    @Getter
    private final Clock afterRewarpDelay = new Clock();
    @Setter
    @Getter
    private boolean rewarpTeleport = false;

    public void onTickCheckTeleport() {
        checkForTeleport();
    }

    private void checkForTeleport() {
        if (!beforeTeleportationPos.isPresent()) return;
        if (mc.thePlayer.getPosition().distanceSq(beforeTeleportationPos.get()) > 2 || PlayerUtils.isStandingOnSpawnPoint()) {
            if (PlayerUtils.isPlayerSuffocating()) {
                LogUtils.sendDebug("Player is suffocating. Waiting");
                return;
            }
            if (!mc.thePlayer.capabilities.isFlying && !mc.thePlayer.onGround) {
                LogUtils.sendDebug("Player is not on ground, but is not flying. Waiting");
                return;
            } else if (mc.thePlayer.capabilities.isFlying && !mc.thePlayer.onGround) {
                if (rewarpTeleport) {
                    LogUtils.sendDebug("Player is flying, but is not on ground. Waiting");
                    if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                        KeyBindUtils.holdThese(mc.gameSettings.keyBindSneak);
                        Multithreading.schedule(KeyBindUtils::stopMovement, (long) (350 + Math.random() * 300), TimeUnit.MILLISECONDS);
                    }
                    return;
                }
            }
            afterRewarpDelay.schedule(1_500);
            LogUtils.sendDebug("Teleported!");
            currentMacro.ifPresent(cm -> {
                if (cm.isPaused() && this.rewarpTeleport) {
                    resumeMacro();
                }
                cm.changeState(AbstractMacro.State.NONE);
                cm.setRotated(false);
                cm.getRewarpDelay().schedule(FarmHelperConfig.getRandomTimeBetweenChangingRows());
                if (rewarpTeleport) {
                    cm.setRewarpState(AbstractMacro.RewarpState.TELEPORTED);
                    cm.actionAfterTeleport();
                    LogUtils.sendDebug("Teleporting to spawn point. Doing actions");
                } else {
                    cm.setRewarpState(AbstractMacro.RewarpState.NONE);
                    LogUtils.sendDebug("Teleporting to spawn point. Not doing actions");
                }
            });
            beforeTeleportationPos = Optional.empty();
            rewarpTeleport = false;
        } else {
            if (System.currentTimeMillis() - MacroHandler.getInstance().getLastTpTry() > 5_000) {
                LogUtils.sendDebug("Teleporting again");
                this.triggerWarpGarden(true, this.rewarpTeleport);
            }
        }
    }

    @Getter
    private long lastTpTry = 0;

    public void triggerWarpGarden(boolean force, boolean rewarpTeleport) {
        triggerWarpGarden(force, rewarpTeleport, true);
    }

    public void triggerWarpGarden(boolean force, boolean rewarpTeleport, boolean sendErrors) {
        if (GameStateHandler.getInstance().notMoving()) {
            KeyBindUtils.stopMovement();
        }
        if (force || GameStateHandler.getInstance().canRewarp() && !beforeTeleportationPos.isPresent()) {
            if (canTriggerFeatureAfterWarp(sendErrors)) {
                LogUtils.sendDebug("Not warping because of feature");
                return;
            }
            lastTpTry = System.currentTimeMillis();
            setBeforeTeleportationPos(Optional.ofNullable(mc.thePlayer.getPosition()));
            LogUtils.sendDebug("Before tp location: " + beforeTeleportationPos);
            LogUtils.sendDebug("Warping to spawn point");
            mc.thePlayer.sendChatMessage("/warp garden");
            this.rewarpTeleport = rewarpTeleport;
            AntiStuck.getInstance().resetUnstuckTries();
            GameStateHandler.getInstance().scheduleRewarp();
            currentMacro.ifPresent(cm -> cm.setRewarpState(AbstractMacro.RewarpState.TELEPORTING));
        }
    }

    public boolean canTriggerFeatureAfterWarp(boolean sendErrors) {
        if (PestsDestroyer.getInstance().canEnableMacro()) {
            LogUtils.sendDebug("Activating Pests Destroyer");
            PestsDestroyer.getInstance().start();
            return true;
        } else if (VisitorsMacro.getInstance().canEnableMacro(false, sendErrors)) {
            LogUtils.sendDebug("Activating Visitors Macro");
            VisitorsMacro.getInstance().start();
            return true;
        } else if (AutoComposter.getInstance().canEnableMacro(false)) {
            LogUtils.sendDebug("Activating Auto Composter");
            AutoComposter.getInstance().start();
            return true;
        } else if (AutoPestExchange.getInstance().canEnableMacro(false)) {
            LogUtils.sendDebug("Activating Auto Pest Hunter");
            AutoPestExchange.getInstance().start();
            return true;
        }
        return false;
    }

    @AllArgsConstructor
    public enum Macros {
        S_SHAPE_MUSHROOM_ROTATE_MACRO(SShapeMushroomRotateMacro.class),
        S_SHAPE_MUSHROOM_MACRO(SShapeMushroomMacro.class),
        S_SHAPE_COCOA_BEAN_MACRO(SShapeCocoaBeanMacro.class),
        S_SHAPE_SUGARCANE_MACRO(SShapeSugarcaneMacro.class),
        S_SHAPE_SUGARCANE_MELONKINGDE_MACRO(SShapeSugarcaneMelonkingdeMacro.class),
        S_SHAPE_VERTICAL_CROP_MACRO(SShapeVerticalCropMacro.class),
        S_SHAPE_MELON_PUMPKIN_DEFAULT_MACRO(SShapeMelonPumpkinDefaultMacro.class),
        S_SHAPE_MUSHROOM_SDS(SShapeMushroomSDSMacro.class),
        CIRCLE_CROP_MACRO(CircularCropMacro.class);

        private static final Map<Macros, AbstractMacro> macros = new HashMap<>();
        private final Class<? extends AbstractMacro> macroClass;

        public <T extends AbstractMacro> T getMacro() {
            if (!macros.containsKey(this)) {
                try {
                    macros.put(this, macroClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    LogUtils.sendError("Failed to instantiate macro: " + this.name());
                    e.printStackTrace();
                }
            }
            return (T) macros.get(this);
        }
    }
}
