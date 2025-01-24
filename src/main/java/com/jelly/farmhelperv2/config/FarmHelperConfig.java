package com.jelly.farmhelperv2.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.*;
import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.page.CustomFailsafeMessagesPage;
import com.jelly.farmhelperv2.config.page.FailsafeNotificationsPage;
import com.jelly.farmhelperv2.config.struct.Rewarp;
import com.jelly.farmhelperv2.failsafe.Failsafe;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.failsafe.impl.BedrockCageFailsafe;
import com.jelly.farmhelperv2.failsafe.impl.DirtFailsafe;
import com.jelly.farmhelperv2.feature.impl.*;
import com.jelly.farmhelperv2.feature.impl.Proxy.ProxyType;
import com.jelly.farmhelperv2.gui.AutoUpdaterGUI;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.GameStateHandler.BuffState;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.hud.DebugHUD;
import com.jelly.farmhelperv2.hud.ProfitCalculatorHUD;
import com.jelly.farmhelperv2.hud.StatusHUD;
import com.jelly.farmhelperv2.util.BlockUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.AudioManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// THIS IS RAT - CatalizCS
@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class FarmHelperConfig extends Config {

    private transient static final Minecraft mc = Minecraft.getMinecraft();
    private transient static final String GENERAL = "General";
    private transient static final String MISCELLANEOUS = "Miscellaneous";
    private transient static final String FAILSAFE = "Failsafe";
    private transient static final String SCHEDULER = "Scheduler";
    private transient static final String JACOBS_CONTEST = "Jacob's Contest";
    private transient static final String VISITORS_MACRO = "Visitors Macro";
    private transient static final String PESTS_DESTROYER = "Pests Destroyer";
    private transient static final String PEST_FARMER = "Pest Farmer";
    private transient static final String AUTO_PEST_EXCHANGE = "Auto Pest Exchange";
    private transient static final String AUTO_GOD_POT = "Auto God Pot";
    private transient static final String AUTO_SELL = "Auto Sell";
    private transient static final String AUTO_REPELLENT = "Auto Repellent";
    private transient static final String AUTO_SPRAYONATOR = "Auto Sprayonator";
    private transient static final String AUTO_COMPOSTER = "Auto Composter";
    private transient static final String DISCORD_INTEGRATION = "Discord Integration";
    private transient static final String DELAYS = "Delays";
    private transient static final String HUD = "HUD";
    private transient static final String DEBUG = "Debug";
    private transient static final String EXPERIMENTAL = "Experimental";

    private transient static final File configRewarpFile = new File("farmhelper_rewarp.json");


    public static List<Rewarp> rewarpList = new ArrayList<>();

    //<editor-fold desc="PROXY">
    public static boolean proxyEnabled = false;
    public static String proxyAddress = "";
    public static String proxyUsername = "";
    public static String proxyPassword = "";
    public static ProxyType proxyType = ProxyType.HTTP;
    //</editor-fold>

    //<editor-fold desc="GENERAL">
    @Info(
            text = "DO NOT lock slot 7 in the hotbar if you're using any gui related features, such as Auto God Pot, Auto Cookie, Auto Repellent",
            category = GENERAL,
            type = InfoType.WARNING,
            size = 2
    )
    public static boolean guiInfo;

    @Dropdown(
            name = "Macro Type", category = GENERAL,
            description = "Farm Types",
            options = {
                    "S Shape / Vertical - Crops (Wheat, Carrot, Potato, NW)", // 0
                    "S Shape - Pumpkin/Melon", // 1
                    "S Shape - Pumpkin/Melon Melongkingde", // 2
                    "S Shape - Pumpkin/Melon Default Plot", // 3
                    "S Shape - Sugar Cane", // 4
                    "S Shape - Cactus", // 5
                    "S Shape - Cactus SunTzu Black Cat", // 6
                    "S Shape - Cocoa Beans", // 7
                    "S Shape - Cocoa Beans (With Trapdoors)", // 8
                    "S Shape - Cocoa Beans (Left/Right)", // 9
                    "S Shape - Mushroom (45°)", // 10
                    "S Shape - Mushroom (30° with rotations)", // 11
                    "S Shape - Mushroom SDS", // 12
                    "Circle - Crops (Wheat, Carrot, Potato, NW)" // 13
            }, size = 2
    )
    public static int macroType = 0;

    @Switch(
            name = "Always hold W while farming", category = GENERAL,
            description = "Always hold W while farming"
    )
    public static boolean alwaysHoldW = false;

    //<editor-fold desc="Rotation">
    @Switch(
            name = "Rotate After Warped", category = GENERAL, subcategory = "Rotation",
            description = "Rotates the player after re-warping", size = 1
    )
    public static boolean rotateAfterWarped = false;
    @Switch(
            name = "Rotate After Drop", category = GENERAL, subcategory = "Rotation",
            description = "Rotates after the player falls down", size = 1
    )
    public static boolean rotateAfterDrop = false;
    @Switch(
            name = "Don't fix micro rotations after warp", category = GENERAL, subcategory = "Rotation",
            description = "The macro doesn't do micro-rotations after rewarp if the current yaw and target yaw are the same", size = 2
    )
    public static boolean dontFixAfterWarping = false;
    @Switch(
            name = "Custom Pitch", category = GENERAL, subcategory = "Rotation",
            description = "Set pitch to custom level after starting the macro"
    )
    public static boolean customPitch = false;
    @Number(
            name = "Custom Pitch Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom pitch level after starting the macro",
            min = -90.0F, max = 90.0F
    )
    public static float customPitchLevel = 0;

    @Switch(
            name = "Custom Yaw", category = GENERAL, subcategory = "Rotation",
            description = "Set yaw to custom level after starting the macro"
    )
    public static boolean customYaw = false;

    @Number(
            name = "Custom Yaw Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom yaw level after starting the macro",
            min = -180.0F, max = 180.0F
    )
    public static float customYawLevel = 0;
    //</editor-fold>

    //<editor-fold desc="Rewarp">
    @Switch(
            name = "Highlight rewarp points", category = GENERAL, subcategory = "Rewarp",
            description = "Highlights all rewarp points you have added",
            size = OptionSize.DUAL
    )
    public static boolean highlightRewarp = true;
    @Info(
            text = "Don't forget to add rewarp points!",
            type = InfoType.WARNING,
            category = GENERAL,
            subcategory = "Rewarp"
    )
    public static boolean rewarpWarning;

    @Button(
            name = "Add Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Adds a rewarp position",
            text = "Add Rewarp"
    )
    Runnable _addRewarp = FarmHelperConfig::addRewarp;
    @Button(
            name = "Remove Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Removes a rewarp position",
            text = "Remove Rewarp"
    )
    Runnable _removeRewarp = FarmHelperConfig::removeRewarp;
    @Button(
            name = "Remove All Rewarps", category = GENERAL, subcategory = "Rewarp",
            description = "Removes all rewarp positions",
            text = "Remove All Rewarps"
    )
    Runnable _removeAllRewarps = FarmHelperConfig::removeAllRewarps;
    //</editor-fold>

    //<editor-fold desc="Spawn">
    @Number(
            name = "SpawnPos X", category = GENERAL, subcategory = "Spawn Position",
            description = "The X coordinate of the spawn",
            min = -30000000, max = 30000000

    )
    public static int spawnPosX = 0;
    @Number(
            name = "SpawnPos Y", category = GENERAL, subcategory = "Spawn Position",
            description = "The Y coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosY = 0;
    @Number(
            name = "SpawnPos Z", category = GENERAL, subcategory = "Spawn Position",
            description = "The Z coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosZ = 0;

    @Button(
            name = "Set SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Sets the spawn position to your current position",
            text = "Set SpawnPos"
    )
    Runnable _setSpawnPos = PlayerUtils::setSpawnLocation;
    @Button(
            name = "Reset SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Resets the spawn position",
            text = "Reset SpawnPos"
    )
    Runnable _resetSpawnPos = () -> {
        spawnPosX = 0;
        spawnPosY = 0;
        spawnPosZ = 0;
        save();
        LogUtils.sendSuccess("Spawn position has been reset!");
    };

    @Switch(
            name = "Draw spawn location", category = GENERAL, subcategory = "Drawings",
            description = "Draws the spawn location"
    )
    public static boolean drawSpawnLocation = true;
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="MISC">
    //<editor-fold desc="Keybinds">
    @KeyBind(
            name = "Toggle Farm Helper", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Toggles the macro on/off", size = 2
    )
    public static OneKeyBind toggleMacro = new OneKeyBind(Keyboard.KEY_GRAVE);
    @KeyBind(
            name = "Open GUI", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Opens Farm Helper configuration menu", size = 2
    )

    public static OneKeyBind openGuiKeybind = new OneKeyBind(Keyboard.KEY_F);
    @KeyBind(
            name = "Freelook", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Locks rotation, lets you freely look", size = 2
    )
    public static OneKeyBind freelookKeybind = new OneKeyBind(Keyboard.KEY_L);
    @Info(
            text = "Freelook doesn't work properly with Oringo!", type = InfoType.WARNING,
            category = MISCELLANEOUS, subcategory = "Keybinds"
    )
    private int freelookWarning;
    @KeyBind(
            name = "Cancel failsafe", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Cancels failsafe and continues macroing", size = 2
    )
    public static OneKeyBind cancelFailsafeKeybind = new OneKeyBind(Keyboard.KEY_NONE);
    //</editor-fold>

    //<editor-fold desc="Plot Cleaning Helper">
    @KeyBind(
            name = "Plot Cleaning Helper", category = MISCELLANEOUS, subcategory = "Plot Cleaning Helper",
            description = "Toggles the plot cleaning helper on/off", size = 2
    )
    public static OneKeyBind plotCleaningHelperKeybind = new OneKeyBind(Keyboard.KEY_P);
    @Switch(
            name = "Automatically choose a tool to destroy the block", category = MISCELLANEOUS, subcategory = "Plot Cleaning Helper",
            description = "Automatically chooses the best tool to destroy the block"
    )
    public static boolean autoChooseTool = false;
    //</editor-fold>

    //<editor-fold desc="Miscellaneous">
    @DualOption(
            name = "AutoUpdater Version Type", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "The version type to use",
            left = "Release",
            right = "Pre-release"
    )
    public static boolean autoUpdaterDownloadBetaVersions = false;

    @Button(
            name = "Check for update", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Checks for updates",
            text = "Check for update"
    )
    Runnable _checkForUpdate = () -> {
        FarmHelperConfig.checkForUpdate();
    };

    @Switch(
            name = "Mute The Game", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Mutes the game while farming"
    )
    public static boolean muteTheGame = false;

    @Switch(
            name = "Change window's title", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Changes the window's title"
    )
    public static boolean changeWindowTitle = true;

    @Switch(
            name = "Auto Cookie", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Automatically purchases and consumes a booster cookie"
    )
    public static boolean autoCookie = false;
    @Switch(
            name = "Hold left click when changing row", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Hold left click when change row"
    )
    public static boolean holdLeftClickWhenChangingRow = true;

    @Switch(
            name = "Auto Ungrab Mouse", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Automatically ungrabs your mouse, so you can safely alt-tab"
    )
    public static boolean autoUngrabMouse = true;

    @Switch(
            name = "PiP Mode", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Enables Picture-in-Picture mode, hold middle mouse while macroing to move the game window"
    )
    public static boolean pipMode = false;
    @Switch(
            name = "Anti Stuck Enabled (Disabled by default for now)", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Enables the anti stuck feature"
    )
    public static boolean tmpAntiStuckEnabled = false;
    @Slider(
            name = "Anti Stuck Tries Until Rewarp", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "The number of tries until rewarp",
            min = 3, max = 10
    )
    public static int antiStuckTriesUntilRewarp = 5;
    //</editor-fold>

    //<editor-fold desc="Performance Mod">
    @Switch(
            name = "Performance Mode", category = MISCELLANEOUS, subcategory = "Performance Mode",
            description = "Set render distance to 2, set max fps to 15 and doesn't render crops"
    )
    public static boolean performanceMode = false;

    @Switch(name = "Fast Render", category = MISCELLANEOUS, subcategory = "Performance Mode",
            description = "Using new fast render method to increase performance"
    )
    public static boolean fastRender = true;

    @Number(
            name = "Max FPS", category = MISCELLANEOUS, subcategory = "Performance Mode",
            description = "The maximum FPS to set when performance mode is enabled",
            min = 10, max = 60
    )
    public static int performanceModeMaxFPS = 20;
    //</editor-fold>

    //<editor-fold desc="Crop Utils">
    @Switch(
            name = "Increase Cocoa Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cocoa beans more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedCocoaBeans = true;

    @Switch(
            name = "Increase Crop Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm crops more efficient by making the hitboxes bigger"
    )
    public static boolean increasedCrops = true;

    @Switch(
            name = "Increase Nether Wart Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm nether warts more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedNetherWarts = true;

    @Switch(
            name = "Increase Mushroom Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm mushrooms more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedMushrooms = true;

    @Switch(
            name = "Pingless Cactus", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cactus more efficiently at higher speeds by making the cactus pingless"
    )
    public static boolean pinglessCactus = true;
    //</editor-fold>

    //<editor-fold desc="Analytics">
    @Switch(
            name = "Send analytic data", category = MISCELLANEOUS, subcategory = "Analytics",
            description = "Sends analytic data to the server to improve the macro and learn how to detect staff checks"
    )
    public static boolean sendAnalyticData = true;
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="FAILSAFES">

    // General Settings
    @Switch(name = "Pop-up Notifications", category = FAILSAFE, subcategory = "General",
            description = "Enable on-screen failsafe notifications")
    public static boolean popUpNotifications = true;

    @Switch(name = "Auto Alt-Tab", category = FAILSAFE, subcategory = "General",
            description = "Switch to game window when failsafe triggers")
    public static boolean autoAltTab = false;

    @Slider(name = "Failsafe Stop Delay", category = FAILSAFE, subcategory = "General",
            description = "Delay before stopping macro after failsafe (ms)",
            min = 1000, max = 7500)
    public static int failsafeStopDelay = 2000;

    // Automatic Actions
    @Switch(name = "Auto Warp on World Change", category = FAILSAFE, subcategory = "Auto Actions",
            description = "Warp to garden after server reboot or update, disconnects if disabled")
    public static boolean autoWarpOnWorldChange = true;

    @Switch(name = "Auto Evacuate on Server Reboot", category = FAILSAFE, subcategory = "Auto Actions",
            description = "Leave island during server reboot or update")
    public static boolean autoEvacuateOnServerReboot = true;

    @Switch(name = "Auto Reconnect", category = FAILSAFE, subcategory = "Auto Actions",
            description = "Automatically reconnect after disconnect")
    public static boolean autoReconnect = true;

    @Switch(name = "Pause on Guest Arrival", category = FAILSAFE, subcategory = "Auto Actions",
            description = "Pause macro when a guest joins your island")
    public static boolean pauseOnGuestArrival = false;

    // Detection Sensitivity
    @Slider(name = "Teleport Lag Tolerance", category = FAILSAFE, subcategory = "Detection",
            description = "Variation in distance between expected and actual positions when lagging",
            min = 0, max = 2)
    public static float teleportLagTolerance = 0.5f;

    @Slider(name = "Detection Time Window", category = FAILSAFE, subcategory = "Detection",
            description = "Time frame for teleport/rotation checks (ms)",
            min = 50, max = 4000, step = 50)
    public static int detectionTimeWindow = 500;

    @Slider(name = "Pitch Sensitivity", category = FAILSAFE, subcategory = "Detection",
            description = "Pitch change sensitivity (lower = stricter)",
            min = 1, max = 30)
    public static float pitchSensitivity = 7;

    @Slider(name = "Yaw Sensitivity", category = FAILSAFE, subcategory = "Detection",
            description = "Yaw change sensitivity (lower = stricter)",
            min = 1, max = 30)
    public static float yawSensitivity = 5;

    @Slider(name = "Teleport Distance Threshold", category = FAILSAFE, subcategory = "Detection",
            description = "Minimum teleport distance to trigger failsafe (blocks)",
            min = 0.5f, max = 20f)
    public static float teleportDistanceThreshold = 4;

    @Slider(name = "Vertical Knockback Threshold", category = FAILSAFE, subcategory = "Detection",
            description = "Minimum vertical knockback to trigger failsafe",
            min = 2000, max = 10000, step = 1000)
    public static float verticalKnockbackThreshold = 4000;

    // BPS Check
    @Switch(name = "Enable BPS Check", category = FAILSAFE, subcategory = "BPS",
            description = "Monitor for drops in blocks per second")
    public static boolean enableBpsCheck = true;

    @Slider(name = "Minimum BPS", category = FAILSAFE, subcategory = "BPS",
            description = "Trigger failsafe if BPS falls below this value",
            min = 5, max = 15)
    public static float minBpsThreshold = 10f;

    // Failsafe Testing
    @Button(name = "Test Failsafe", category = FAILSAFE, subcategory = "Testing",
            description = "Simulate a failsafe trigger",
            text = "Run Test")
    Runnable _testFailsafe = () -> {
        if (!MacroHandler.getInstance().isMacroToggled()) {
            LogUtils.sendError("You need to start the macro first!");
            return;
        }
        LogUtils.sendWarning("Testing failsafe...");
        PlayerUtils.closeScreen();
        Failsafe testingFailsafe = FailsafeManager.getInstance().failsafes.get(testFailsafeType);
        if (testingFailsafe.equals(DirtFailsafe.getInstance()) || testingFailsafe.equals(BedrockCageFailsafe.getInstance())) {
            LogUtils.sendError("You can't test this failsafe because it requires specific conditions to trigger!");
            return;
        }
        FailsafeManager.getInstance().possibleDetection(testingFailsafe);
    };

    @Dropdown(name = "Test Failsafe Type", category = FAILSAFE, subcategory = "Testing",
            description = "Select failsafe scenario to test",
            options = {
                    "Bad Effects",
                    "Banwave",
                    "Bedrock Cage",
                    "Cobweb",
                    "Dirt",
                    "Disconnect",
                    "Evacuate",
                    "Full Inventory",
                    "Guest Visit",
                    "Item Change",
                    "Jacob",
                    "Knockback",
                    "Lower Average BPS",
                    "Rotation",
                    "Teleport",
                    "World Change"
            })
    public static int testFailsafeType = 0;

    //</editor-fold>

    //<editor-fold desc="Clip Capturing">

    @Switch(
            name = "Capture Clip After Failsafe", category = FAILSAFE, subcategory = "Clip Capturing",
            description = "Captures a clip after triggering failsafe by pressing a key combination"
    )
    public static boolean captureClipAfterFailsafe = false;
    @Switch(
            name = "Capture Clip After Getting Banned (Replay Buffer Only)", category = FAILSAFE, subcategory = "Clip Capturing",
            description = "Captures a clip after getting banned by pressing a key combination"
    )
    public static boolean captureClipAfterGettingBanned = false;
    @DualOption(
            name = "Clip Capturing Type", category = FAILSAFE, subcategory = "Clip Capturing",
            description = "The clip capturing type to use",
            left = "Replay Buffer",
            right = "Recording"
    )
    public static boolean clipCapturingType = false;
    @KeyBind(
            name = "Keybind",
            category = FAILSAFE, subcategory = "Clip Capturing",
            description = "Captures a clip after triggering failsafe"
    )
    public static OneKeyBind captureClipKeybind = new OneKeyBind(Keyboard.KEY_NONE);
    @Slider(
            name = "Clip Capturing Delay (in seconds)", category = FAILSAFE, subcategory = "Clip Capturing",
            description = "The delay to capture a clip after triggering failsafe (in seconds)",
            min = 10, max = 200
    )
    public static int captureClipDelay = 30;

    @Info(
            text = "You need to use either ShadowPlay, OBS, Medal.tv or any alternative with Replay Buffer, then configure it to capture clips!",
            type = InfoType.WARNING,
            category = FAILSAFE,
            subcategory = "Clip Capturing",
            size = 2
    )
    public static boolean captureClipWarning;
    @Info(
            text = "Remember to use key combinations instead of single keys!",
            type = InfoType.WARNING,
            category = FAILSAFE,
            subcategory = "Clip Capturing",
            size = 2
    )
    public static boolean captureClipWarning2;

    //</editor-fold>

    //<editor-fold desc="Failsafes conf page">
    @Page(
            name = "Failsafe Notifications", category = FAILSAFE, subcategory = "Failsafe Notifications", location = PageLocation.BOTTOM,
            description = "Click here to customize failsafe notifications"
    )
    public FailsafeNotificationsPage failsafeNotificationsPage = new FailsafeNotificationsPage();
    //</editor-fold>

    //<editor-fold desc="Desync">
    @Switch(
            name = "Check Desync", category = FAILSAFE, subcategory = "Desync",
            description = "If client desynchronization is detected, it activates a failsafe. Turn this off if the network is weak or if it happens frequently."
    )
    public static boolean checkDesync = true;
    @Slider(
            name = "Pause for X milliseconds after desync triggered", category = FAILSAFE, subcategory = "Desync",
            description = "The delay to pause after desync triggered (in milliseconds)",
            min = 3_000, max = 10_000
    )
    public static int desyncPauseDelay = 5_000;
    //</editor-fold>

    //<editor-fold desc="Failsafe Trigger Sound">
    @Switch(
            name = "Enable Failsafe Trigger Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound", size = OptionSize.DUAL,
            description = "Makes a sound when a failsafe has been triggered"
    )
    public static boolean enableFailsafeSound = true;
    @DualOption(
            name = "Failsafe Sound Type", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The failsafe sound type to play when a failsafe has been triggered",
            left = "Minecraft",
            right = "Custom",
            size = 2
    )
    public static boolean failsafeSoundType = false;
    @Dropdown(
            name = "Minecraft Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The Minecraft sound to play when a failsafe has been triggered",
            options = {
                    "Ping", // 0
                    "Anvil" // 1
            }
    )
    public static int failsafeMcSoundSelected = 1;

    @Dropdown(
            name = "Custom Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The custom sound to play when a failsafe has been triggered",
            options = {
                    "Custom", // 0
                    "Voice", // 1
                    "Metal Pipe", // 2
                    "AAAAAAAAAA", // 3
                    "Loud Buzz", // 4
            }
    )
    public static int failsafeSoundSelected = 1;
    @Number(
            name = "Number of times to play custom sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The number of times to play custom sound when a failsafe has been triggered",
            min = 1, max = 10
    )
    public static int failsafeSoundTimes = 13;
    @Info(
            text = "If you want to use your own WAV file, rename it to 'farmhelper_sound.wav' and put it in your Minecraft directory.",
            type = InfoType.WARNING,
            category = FAILSAFE,
            subcategory = "Failsafe Trigger Sound",
            size = 2
    )
    public static boolean customFailsafeSoundWarning;
    @Slider(
            name = "Failsafe Sound Volume (in %)", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The volume of the failsafe sound",
            min = 0, max = 100
    )
    public static float failsafeSoundVolume = 50.0f;
    @Switch(
            name = "Max out Master category sounds while pinging", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Maxes out the sounds while failsafe"
    )
    public static boolean maxOutMinecraftSounds = false;

    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Plays the selected sound",
            text = "Play"
    )
    Runnable _playFailsafeSoundButton = () -> AudioManager.getInstance().playSound();
    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Stops playing the selected sound",
            text = "Stop"
    )
    Runnable _stopFailsafeSoundButton = () -> AudioManager.getInstance().resetSound();

    //</editor-fold>

    //<editor-fold desc="Restart after failsafe">
    @Switch(
            name = "Enable Restart After FailSafe", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Restarts the macro after a while when a failsafe has been triggered"
    )
    public static boolean enableRestartAfterFailSafe = true;
    @Slider(
            name = "Restart Delay", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "The delay to restart after failsafe (in minutes)",
            min = 0, max = 20
    )
    public static int restartAfterFailSafeDelay = 0;
    @Info(
            text = "Setting this value to 0 will start the macro a few seconds later, after the failsafe is finished",
            category = FAILSAFE, subcategory = "Restart After FailSafe",
            type = InfoType.INFO, size = 2
    )
    public static boolean restartAfterFailSafeInfo;

    @Switch(
            name = "Always teleport to /warp garden after the failsafe",
            category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Always teleports to /warp garden after the failsafe"
    )
    public static boolean alwaysTeleportToGarden = false;

    //</editor-fold>

    //<editor-fold desc="Banwave">
    @Switch(
            name = "Enable Banwave Checker", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Checks for banwave and shows you the number of players banned in the last 15 minutes",
            size = 2
    )
    public static boolean banwaveCheckerEnabled = true;
    @Switch(
            name = "Leave/pause during banwave", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Automatically disconnects from the server or pauses the macro when a banwave is detected"
    )
    public static boolean enableLeavePauseOnBanwave = false;
    @DualOption(
            name = "Banwave Action", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The action taken when banwave detected",
            left = "Leave",
            right = "Pause"
    )
    public static boolean banwaveAction = false;
    @Dropdown(
            name = "Base Threshold on", category = FAILSAFE, subcategory = "Banwave Checker",
            options = {"Global bans", "FarmHelper bans", "Both"}, size = 2
    )
    public static int banwaveThresholdType = 0;
    @Slider(
            name = "Banwave Disconnect Threshold", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The threshold to disconnect from the server on banwave",
            min = 1, max = 100
    )
    public static int banwaveThreshold = 50;
    @Number(
            name = "Delay Before Reconnecting", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The delay before reconnecting after leaving on banwave (in seconds)",
            min = 1, max = 20, size = 2
    )
    public static int delayBeforeReconnecting = 5;
    @Switch(
            name = "Don't leave during Jacob's Contest", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Prevents the macro from leaving during Jacob's Contest even when banwave detected"
    )
    public static boolean banwaveDontLeaveDuringJacobsContest = true;
    //</editor-fold>

    //<editor-fold desc="Failsafe Messages">
    @Switch(
            name = "Send Chat Message During Failsafe", category = FAILSAFE, subcategory = "Failsafe Messages",
            description = "Sends a chat message when a failsafe has been triggered"
    )
    public static boolean sendFailsafeMessage = false;
    @Page(
            name = "Custom Failsafe Messages", category = FAILSAFE, subcategory = "Failsafe Messages", location = PageLocation.BOTTOM,
            description = "Click here to edit custom failsafe messages"
    )
    public static CustomFailsafeMessagesPage customFailsafeMessagesPage = new CustomFailsafeMessagesPage();
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="SCHEDULER">
    //<editor-fold desc="Scheduler">
    @Switch(
            name = "Enable Scheduler", category = SCHEDULER, subcategory = "Scheduler", size = OptionSize.DUAL,
            description = "Farms for X amount of minutes then takes a break for X amount of minutes"
    )
    public static boolean enableScheduler = true;
    @Slider(
            name = "Farming time (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How long to farm",
            min = 1, max = 300, step = 1
    )
    public static int schedulerFarmingTime = 60;
    @Slider(
            name = "Farming time randomness (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How much randomness to add to the farming time",
            min = 0, max = 15, step = 1
    )
    public static int schedulerFarmingTimeRandomness = 5;
    @Slider(
            name = "Break time (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How long to take a break",
            min = 1, max = 120, step = 1
    )
    public static int schedulerBreakTime = 5;
    @Slider(
            name = "Break time randomness (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How much randomness to add to the break time",
            min = 0, max = 15, step = 1
    )
    public static int schedulerBreakTimeRandomness = 5;
    @Switch(
            name = "Pause the scheduler during Jacob's Contest", category = SCHEDULER, subcategory = "Scheduler",
            description = "Pauses and delays the scheduler during Jacob's Contest"
    )
    public static boolean pauseSchedulerDuringJacobsContest = true;
    @Switch(
            name = "Open inventory on scheduler breaks", category = SCHEDULER, subcategory = "Scheduler",
            description = "Opens inventory on scheduler breaks"
    )
    public static boolean openInventoryOnSchedulerBreaks = true;
    @Switch(
            name = "Disconnect during break", category = SCHEDULER, subcategory = "Scheduler",
            description = "Logs out of game and logs back in after break ends"
    )
    public static boolean schedulerDisconnectDuringBreak = false;
    @Switch(
            name = "Wait Until Rewarp Point for break", category = SCHEDULER, subcategory = "Scheduler",
            description = "Waits until player is standing on rewarp point to take break"
    )
    public static boolean schedulerWaitUntilRewarp = false;
    @Switch(
            name = "Reset Scheduler on Macro Disabled", category = SCHEDULER, subcategory = "Scheduler",
            description = "Resets Scheduler When macro is disabled"
    )
    public static boolean schedulerResetOnDisable = true;
    @Button(
            name = "Reset Scheduler", category = SCHEDULER, subcategory = "Scheduler",
            text = "Reset Scheduler", description = "Resets Scheduler (Only works when macro is of)"
    )
    public Runnable schedulerReset = () -> {
        if (!MacroHandler.getInstance().isMacroToggled()) {
            boolean old = FarmHelperConfig.schedulerResetOnDisable;
            FarmHelperConfig.schedulerResetOnDisable = true;
            Scheduler.getInstance().stop();
            FarmHelperConfig.schedulerResetOnDisable = old;
        }
    };
    //</editor-fold>

    //<editor-fold desc="Leave timer">
    @Switch(
            name = "Enable leave timer", category = SCHEDULER, subcategory = "Leave Timer",
            description = "Leaves the server after the timer has ended"
    )
    public static boolean leaveTimer = false;
    @Slider(
            name = "Leave time", category = SCHEDULER, subcategory = "Leave Timer",
            description = "The time to leave the server (in minutes)",
            min = 15, max = 720, step = 5
    )
    public static int leaveTime = 60;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="JACOB'S CONTEST">

    //<editor-fold desc="Pet Swapper">
    @Switch(
            name = "Swap pet during Jacob's contest", category = JACOBS_CONTEST, subcategory = "Pet Swapper",
            description = "Swaps pet to the selected pet during Jacob's contest. Selects the first one from the pet list."
    )
    public static boolean enablePetSwapper = false;

    @Slider(
            name = "Pet Swap Delay", category = JACOBS_CONTEST, subcategory = "Pet Swapper",
            description = "The delay between clicking GUI during swapping the pet (in milliseconds)",
            min = 200, max = 3000
    )
    public static int petSwapperDelay = 1000;
    @Text(
            name = "Pet Name", placeholder = "Type your pet name here",
            category = JACOBS_CONTEST, subcategory = "Pet Swapper"
    )
    public static String petSwapperName = "";
    //</editor-fold>

    @Switch(
            name = "Enable Jacob Failsafes", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "Stops farming once a crop threshold has been met"
    )
    public static boolean enableJacobFailsafes = false;
    @DualOption(
            name = "Jacob Failsafe Action", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The action to take when a failsafe has been triggered",
            left = "Leave",
            right = "Pause"
    )
    public static boolean jacobFailsafeAction = true;
    @Slider(
            name = "Nether Wart Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The nether wart cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobNetherWartCap = 800000;
    @Slider(
            name = "Potato Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The potato cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobPotatoCap = 830000;
    @Slider(
            name = "Carrot Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The carrot cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCarrotCap = 860000;
    @Slider(
            name = "Wheat Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The wheat cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobWheatCap = 265000;
    @Slider(
            name = "Sugar Cane Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The sugar cane cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobSugarCaneCap = 575000;
    @Slider(
            name = "Mushroom Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The mushroom cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobMushroomCap = 250000;
    @Slider(
            name = "Melon Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The melon cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobMelonCap = 1234000;

    @Slider(
            name = "Pumpkin Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The pumpkin cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobPumpkinCap = 240000;

    @Slider(
            name = "Cocoa Beans Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The cocoa beans cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCocoaBeansCap = 725000;
    @Slider(
            name = "Cactus Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The cactus cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCactusCap = 470000;

    //</editor-fold>

    //<editor-fold desc="VISITORS">
    //<editor-fold desc="Visitors Main">
    @Info(
            text = "Visitors Macro tends to move your mouse because of opening GUIs frequently. Be aware of that.",
            type = InfoType.WARNING,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro",
            size = 2
    )
    public static boolean visitorsMacroWarning2;

    @Info(
            text = "Cookie buff is required!",
            type = InfoType.ERROR,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro"
    )
    public static boolean infoCookieBuffRequired;

    @Switch(
            name = "Enable visitors macro", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Enables visitors macro"
    )
    public static boolean visitorsMacro = false;
    @Slider(
            name = "Minimum Visitors to start the macro", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The minimum amount of visitors to start the macro",
            min = 1, max = 5
    )
    public static int visitorsMacroMinVisitors = 5;
    @Switch(
            name = "Autosell before serving visitors", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Automatically sells crops before serving visitors"
    )
    public static boolean visitorsMacroAutosellBeforeServing = false;
    @Switch(
            name = "Pause the visitors macro during Jacob's contests", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Pauses the visitors macro during Jacob's contests"
    )
    public static boolean pauseVisitorsMacroDuringJacobsContest = true;

    @Slider(
            name = "The minimum amount of coins to start the macro (in thousands)", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The minimum amount of coins you need to have in your purse to start the visitors macro",
            min = 1_000, max = 20_000
    )
    public static int visitorsMacroMinMoney = 2_000;
    @Info(
            text = "If you put your compactors in the hotbar, they will be temporarily disabled.",
            type = InfoType.WARNING,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro",
            size = 2
    )
    public static boolean infoCompactors;

    @Slider(
            name = "Max Spend Limit (in Millions Per Purchase)", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            min = 0.2f, max = 7.5f
    )
    public static float visitorsMacroMaxSpendLimit = 0.7f;

    @Switch(
            name = "Visitors Macro Afk Infinite mode",
            description = "Will turn on Visitors Macro automatically when you are not farming and in the barn. Click macro toggle button to disable this option",
            category = VISITORS_MACRO, subcategory = "Visitors Macro"
    )
    public static boolean visitorsMacroAfkInfiniteMode = false;

    @Info(
            text = "If you have any issues, try switching the travel method.",
            type = InfoType.INFO,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro",
            size = 2
    )
    public static boolean visitorsExchangeTravelMethodInfo;
    @DualOption(
            name = "Travel method", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The travel method to use to get to the visitor stand",
            left = "Fly",
            right = "Walk"
    )
    public static boolean visitorsExchangeTravelMethod = false;

    @Button(
            name = "Start the macro manually", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Triggers the visitors macro",
            text = "Trigger now"
    )
    public static Runnable triggerVisitorsMacro = () -> {
        if (!PlayerUtils.isInBarn()) {
            LogUtils.sendError("[Visitors Macro] You need to be in the barn to start the macro!");
            return;
        }
        VisitorsMacro.getInstance().setManuallyStarted(true);
        VisitorsMacro.getInstance().start();
    };

    @DualOption(
            name = "Visitors Filtering Method", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "",
            left = "By Rarity", right = "By Name"
    )
    @Deprecated // Just here to keep the old settings for automatic migration
    public static boolean visitorsFilteringMethod = false;

    @DualOption(
            name = "Full Inventory Action", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The action to take when the items don't fit in your inventory",
            left = "Reject", right = "Ignore"
    )
    public static boolean fullInventoryAction = true;

    //</editor-fold>

    //<editor-fold desc="Name Filtering">

    @Switch(
            name = "Filter by name", category = VISITORS_MACRO, subcategory = "Name Filtering",
            description = "Filters visitors by name", size = 2
    )
    public static boolean filterVisitorsByName = false;

    @DualOption(
            name = "Name Filtering Type", category = VISITORS_MACRO, subcategory = "Name Filtering",
            description = "The name filtering method to use",
            left = "Blacklist", right = "Whitelist"
    )
    public static boolean nameFilteringType = false;

    @DualOption(
            name = "Name Action Type", category = VISITORS_MACRO, subcategory = "Name Filtering",
            description = "The action to execute when a visitor's name does not match your set filter",
            left = "Reject", right = "Ignore"
    )
    public static boolean nameActionType = true;

    @Text(
            name = "Name Filter", category = VISITORS_MACRO, subcategory = "Name Filtering",
            description = "Visitor names to filter. Use | to split the messages.",
            placeholder = "Visitor names to filter. Use | to split the messages.",
            size = 2
    )
    public static String nameFilter = "Librarian|Maeve|Spaceman";

    //<editor-fold desc="Rarity">
    @Switch(
            name = "Filter by rarity", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "Filters visitors by rarity"
    )
    public static boolean filterVisitorsByRarity = true;

    @Info(
            text = "If both filters are enabled, the macro will filter visitors by name first, then by rarity.",
            type = InfoType.INFO,
            category = VISITORS_MACRO,
            subcategory = "Rarity Filtering",
            size = 2
    )
    public static boolean nameFilterInfo;

    @Dropdown(
            name = "Uncommon", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "The action taken when an uncommon visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline", "Ignore"},
            size = 2
    )
    public static int visitorsActionUncommon = 0;
    @Dropdown(
            name = "Rare", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "The action taken when a rare visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline", "Ignore"},
            size = 2
    )
    public static int visitorsActionRare = 0;
    @Dropdown(
            name = "Legendary", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "The action taken when a legendary visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline", "Ignore"},
            size = 2
    )
    public static int visitorsActionLegendary = 0;
    @Dropdown(
            name = "Mythic", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "The action taken when a mythic visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline", "Ignore"},
            size = 2
    )
    public static int visitorsActionMythic = 0;
    @Dropdown(
            name = "Special", category = VISITORS_MACRO, subcategory = "Rarity Filtering",
            description = "The action taken when a special visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline", "Ignore"},
            size = 2
    )
    public static int visitorsActionSpecial = 3;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="PESTS DESTROYER">
    //<editor-fold desc="Infos">
    @Info(
            text = "Make sure to disable SkyHanni Hide Particles, it is enabled by default!",
            type = InfoType.WARNING,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerWarning1;
    @Info(
            text = "Make sure to enable Hypixel's Particles (/pq low), low is the minimum to make it work",
            type = InfoType.WARNING,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerWarning2;
    @Info(
            text = "Make sure to disable Oringo AutoSprint. Keep in mind Oringo breaks Farm Helper a lot.",
            type = InfoType.WARNING,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerWarning3;

    @Info(
            text = "Pests Destroyer will trigger only at rewarp or spawn location after reaching the threshold!",
            type = InfoType.INFO,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerInfo;
    //</editor-fold>

    //<editor-fold desc="Pests Destroyer Main">
    @Switch(
            name = "Enable Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Destroys pests"
    )
    public static boolean enablePestsDestroyer = false;
    @Slider(
            name = "Start killing pests at X pests", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "The amount of pests to start killing pests",
            min = 1, max = 8
    )
    public static int startKillingPestsAt = 3;
    @Slider(
            name = "Additional GUI Delay (ms)", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Extra time to wait between clicks. By default it's 500-1000 ms.",
            min = 0, max = 5000
    )
    public static int pestAdditionalGUIDelay = 0;

    @Switch(
            name = "Force Enable Pest Destroyer at X Pests", category = PESTS_DESTROYER, subcategory = "Pests Destroyer"
    )
    public static boolean pestForceEnable = false;

    @Switch(
            name = "Sprint while flying", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Sprints while flying"
    )
    public static boolean sprintWhileFlying = false;

    @Switch(
            name = "Use AOTE/V in Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Uses AOTE/V in Pests Destroyer"
    )
    public static boolean useAoteVInPestsDestroyer = false;

    @Switch(
            name = "Don't teleport to plots when the spawn is not obstructed", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Prevents the macro from teleporting to plots"
    )
    public static boolean dontTeleportToPlots = false;

    @Switch(
            name = "Fly Pathfinder Oringo Compatible",
            description = "Makes the fly pathfinder compatible with Oringo, but worse, zzz...",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer"
    )
    public static boolean flyPathfinderOringoCompatible = false;

    @Switch(
            name = "Pause the Pests Destroyer during Jacob's contests", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Pauses the Pests Destroyer during Jacob's contests",
            size = 2
    )
    public static boolean pausePestsDestroyerDuringJacobsContest = true;

    @Button(
            name = "Trigger now Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Triggers the pests destroyer manually",
            text = "Trigger now"
    )
    public static void triggerManuallyPestsDestroyer() {
        if (PestsDestroyer.getInstance().canEnableMacro(true)) {
            PestsDestroyer.getInstance().start();
        }
    }

    @Switch(
            name = "Pests Destroyer Afk Infinite mode",
            description = "Will turn on Pests Destroyer automatically when you are not farming. Click macro toggle button to disable this option",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer"
    )
    public static boolean pestsDestroyerAfkInfiniteMode = false;

    @Switch(
            name = "Pests Destroyer on the track",
            description = "Will kill pests if they are in your range while farming",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer on the track"
    )
    public static boolean pestsDestroyerOnTheTrack = false;

    @Slider(
            name = "Pests Destroyer on the track FOV",
            description = "The field of view of the pests destroyer on the track",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer on the track",
            min = 1, max = 360
    )
    public static int pestsDestroyerOnTheTrackFOV = 360;

    @Slider(
            name = "Time for the pest to stay in range to activate (ms)",
            description = "The time for the pest to stay in range to activate the macro",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer on the track",
            min = 0, max = 2_000
    )
    public static int pestsDestroyerOnTheTrackTimeForPestToStayInRange = 750;

    @Slider(
            name = "Stuck timer (ms)",
            description = "The time after which macro will count as being stuck",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer on the track",
            min = 4_000, max = 25_000
    )
    public static int pestsDestroyerOnTheTrackStuckTimer = 5_000;

    @Switch(
            name = "Don't kill pests on track during Jacob's Contest",
            description = "Prevents the macro from killing pests on the track during Jacob's Contest",
            category = PESTS_DESTROYER, subcategory = "Pests Destroyer on the track"
    )
    public static boolean dontKillPestsOnTrackDuringJacobsContest = true;

    @KeyBind(
            name = "Enable Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Enables the pests destroyer",
            size = 2
    )
    public static OneKeyBind enablePestsDestroyerKeyBind = new OneKeyBind(Keyboard.KEY_NONE);
    //</editor-fold>

    //<editor-fold desc="Armor Swapper">
    @Switch(
            name = "Swap Armor Before Killing", category = PESTS_DESTROYER, subcategory = "Armor Swapper"
    )
    public static boolean pestSwapArmorBefore = false;

    @Slider(
            name = "Wardrobe Slot To Kill With", category = PESTS_DESTROYER, subcategory = "Armor Swapper",
            description = "Wardrobe slot to use before turning on pest destroyer",
            min = 1, max = 18
    )
    public static int pestArmorSlot0 = 1;

    @Switch(
            name = "Swap Armor After Killing", category = PESTS_DESTROYER, subcategory = "Armor Swapper"
    )
    public static boolean pestSwapArmorAfter = false;

    @Slider(
            name = "Wardrobe Slot To Farm With", category = PESTS_DESTROYER, subcategory = "Armor Swapper",
            description = "Wardrobe slot to equip after turning off pest destroyer",
            min = 1, max = 18
    )
    public static int pestArmorSlot1 = 1;
    //</editor-fold>

    //<editor-fold desc="Drawings">
    @Switch(
            name = "Pests ESP", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Draws a box around pests"
    )
    public static boolean pestsESP = true;
    @Color(
            name = "ESP Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the pests ESP"
    )
    public static OneColor pestsESPColor = new OneColor(0, 255, 217, 171);
    @Switch(
            name = "Tracers to Pests", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Draws a line to pests"
    )
    public static boolean pestsTracers = true;
    @Color(
            name = "Tracers Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the pests tracers"
    )
    public static OneColor pestsTracersColor = new OneColor(0, 255, 217, 171);
    @Switch(
            name = "Highlight borders of Plot with pests", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Highlights the borders of the plot with pests"
    )
    public static boolean highlightPlotWithPests = true;
    @Color(
            name = "Plot Highlight Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the plot highlight"
    )
    public static OneColor plotHighlightColor = new OneColor(0, 255, 217, 40);
    //</editor-fold>

    //<editor-fold desc="Logs">
    @Switch(
            name = "Send Webhook log if pests detection number has been exceeded", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Sends a webhook log if pests detection number has been exceeded"
    )
    public static boolean sendWebhookLogIfPestsDetectionNumberExceeded = true;
    @Switch(
            name = "Ping @everyone", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Pings @everyone on pests detection number exceeded"
    )
    public static boolean pingEveryoneOnPestsDetectionNumberExceeded = false;
    @Switch(
            name = "Send notification if pests detection number has been exceeded", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Sends a notification if pests detection number has been exceeded"
    )
    public static boolean sendNotificationIfPestsDetectionNumberExceeded = true;
    @Switch(name = "Send Webhook log when pest destroyer starts/stops", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Sends a webhook log when pest destroyer starts/stops"
    )
    public static boolean sendWebhookLogWhenPestDestroyerStartsStops = true;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="PEST FARMER">
    @Header(
            text = "Read the comments below this page before using pest farmer.",
            category = PEST_FARMER, size = 2
    )
    public static boolean ignored1;

    @Switch(
            name = "Enable Pest Farming", category = PEST_FARMER,
            description = "Enables Pest Farming"
    )
    public static boolean pestFarming = false;

    @Switch(
            name = "Hold Daedalus Axe", category = PEST_FARMER,
            description = "Farms with daedalus axe while waiting for pests to spawn"
    )
    public static boolean pestFarmingHoldDaedalus = false;

    @Slider(
            name = "Farming Armor Slot", category = PEST_FARMER,
            min = 1, max = 18
    )
    public static int pestFarmingSet0Slot = 1;

    @Slider(
            name = "Pest Chance Armor Slot", category = PEST_FARMER,
            min = 1, max = 18
    )
    public static int pestFarmingSet1Slot = 1;

    @Slider(
            name = "Pest Spawn Timer (In seconds)", category = PEST_FARMER,
            description = "The time it should wait after pests spawn to swap slots (should be your pest spawn time - 5/10 seconds)",
            min = 30, max = 300
    )
    public static int pestFarmingWaitTime = 255;

    @Info(
            text = "It's supposed to swap armor before pest spawns, swap back after pest spawns and kill with Pest Destryoer/the other one.",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored0;
    @Info(
            text = "Use this Auto-Pet Rule:",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored2;
    @Info(
            text = "    1. On Gain Crop Collection -> Farming Pet, Except if slug is Equipped",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored3;
    @Info(
            text = "    2. On Equip Farming Armor -> Farming Pet",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored4;
    @Info(
            text = "    3. On Equip Pest Chance Armor -> Slug Pet",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored5;
    @Info(
            text = "    4. On Enter Combat -> Hedgehog Pet",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored6;
    @Info(
            text = "Use Armor Swapper in Pest Destroyer. Swap to Farming Armor On Start Just in case Pest Farmer doesn't do that itself.",
            type = InfoType.INFO, category = PEST_FARMER, size = 2
    )
    public static boolean ignored7;

    //</editor-fold>
    //<editor-fold desc="DISCORD INTEGRATION">
    //<editor-fold desc="Webhook Discord">
    @Switch(
            name = "Enable Webhook Messages", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Allows to send messages via Discord webhooks"
    )
    public static boolean enableWebHook = false;
    @Switch(
            name = "Send Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends all messages about the macro, staff checks, etc"
    )
    public static boolean sendLogs = false;
    @Switch(
            name = "Send Status Updates", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends messages about the macro, such as profits, harvesting crops, etc"
    )
    public static boolean sendStatusUpdates = false;
    @Number(
            name = "Status Update Interval (in minutes)", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "The interval between sending messages about status updates",
            min = 1, max = 60
    )
    public static int statusUpdateInterval = 5;
    @Switch(
            name = "Send Visitors Macro Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends messages about the visitors macro, such as which visitor got rejected or accepted and with what items"
    )
    public static boolean sendVisitorsMacroLogs = true;
    @Switch(
            name = "Ping everyone on Visitors Macro Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Pings everyone on Visitors Macro Logs"
    )
    public static boolean pingEveryoneOnVisitorsMacroLogs = false;
    @Switch(
            name = "Send Macro Enable/Disable Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends messages when the macro has been enabled or disabled"
    )
    public static boolean sendMacroEnableDisableLogs = true;
    @Text(
            name = "WebHook URL", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "The URL to use for the webhook",
            placeholder = "https://discord.com/api/webhooks/...",
            secure = true
    )
    public static String webHookURL = "";
    //</editor-fold>

    //<editor-fold desc="Remote Control">
    @Switch(
            name = "Enable Remote Control", category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "Enables remote control via Discord messages"
    )
    public static boolean enableRemoteControl = false;
    @Text(
            name = "Discord Remote Control Bot Token",
            category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The bot token to use for remote control",
            secure = true
    )
    public static String discordRemoteControlToken;
    @Text(
            name = "Discord Remote Control Address",
            category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The address to use for remote control. If you are unsure what to put there, leave \"localhost\".",
            placeholder = "localhost"
    )
    public static String discordRemoteControlAddress = "localhost";

    @Number(
            name = "Remote Control Port", category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The port to use for remote control. Change this if you have port conflicts.",
            min = 1, max = 65535
    )
    public static int remoteControlPort = 21370;

    @Info(
            text = "If you want to use the remote control feature, you need to put Farm Helper JDA Dependency inside your mods folder.",
            type = InfoType.ERROR,
            category = DISCORD_INTEGRATION,
            subcategory = "Remote Control",
            size = 2
    )
    public static boolean infoRemoteControl;

    @Info(
            text = "Your Farm Helper JDA Dependency is outdated! You must update it to use the remote control feature.",
            type = InfoType.ERROR,
            category = DISCORD_INTEGRATION,
            subcategory = "Remote Control",
            size = 2
    )
    public static boolean info2RemoteControl;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="AUTO PEST EXCHANGE">

    @Info(
            text = "Once you collect enough pests, the macro will go to the Phillip NPC and exchange pests in your vacuum for Farming Fortune for 30 minutes.",
            type = InfoType.INFO,
            category = AUTO_PEST_EXCHANGE,
            subcategory = "Auto Pest Exchange",
            size = 2
    )
    public static boolean autoPestExchangeInfo1;

    @Switch(
            name = "Enable Auto Pest Exchange", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Automatically hunts pests"
    )
    public static boolean autoPestExchange = false;
    @Switch(
            name = "Pause the Auto Pest Exchange during Jacob's contests", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Pauses the Auto Pest Exchange during Jacob's contests"
    )
    public static boolean pauseAutoPestExchangeDuringJacobsContest = true;
    @Switch(
            name = "Ignore Jacob's Contest", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Start the Auto Pest Exchange regardless of the next Jacob's contests"
    )
    public static boolean autoPestExchangeIgnoreJacobsContest = false;
    @Switch(
            name = "Only start on relevant Jacob's Contests", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Only start the Auto Pest Exchange if the next Jacob's contest contains the current crop you are farming"
    )
    public static boolean autoPestExchangeOnlyStartRelevant = false;
    @DualOption(
            name = "Travel method", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "The travel method to use to get to the pest exchange desk",
            left = "Fly",
            right = "Walk"
    )
    public static boolean autoPestExchangeTravelMethod = false;
    @Info(
            text = "If you have any issues, try switching the travel method.",
            type = InfoType.INFO,
            category = AUTO_PEST_EXCHANGE,
            subcategory = "Auto Pest Exchange",
            size = 2
    )
    public static boolean autoPestExchangeTravelMethodInfo;
    @Slider(
            name = "Trigger before contest starts (in minutes)", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "The time before the contest starts to trigger the auto pest exchange",
            min = 1, max = 40
    )
    public static int autoPestExchangeTriggerBeforeContestStarts = 5;
    @Slider(
            name = "Pests amount required", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "The amount of pests in a vacuum required to start the auto pest exchange",
            min = 1, max = 40
    )
    public static int autoPestExchangeMinPests = 10;
    @Switch(
            name = "Send Webhook Log", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Logs all events related to the auto pest exchange"
    )
    public static boolean logAutoPestExchangeEvents = true;
    @Switch(
            name = "Highlight desk location", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Highlights the pest exchange desk location"
    )
    public static boolean highlightPestExchangeDeskLocation = true;
    @Info(
            text = "The auto pest exchange will start automatically once you rewarp!",
            type = InfoType.WARNING,
            category = AUTO_PEST_EXCHANGE,
            subcategory = "Auto Pest Exchange",
            size = 2
    )
    public static boolean autoPestExchangeInfo;

    @Button(
            name = "Trigger now Auto Pest Exchange", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Triggers the auto pest exchange manually",
            text = "Trigger now"
    )
    public static void triggerManuallyAutoPestExchange() {
        AutoPestExchange.getInstance().setManuallyStarted(true);
        AutoPestExchange.getInstance().start();
    }

    @Button(
            name = "Set the pest exchange location", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Sets the pest exchange location",
            text = "Set desk"
    )
    public static Runnable setPestExchangeLocation = () -> {
        if (!PlayerUtils.isInBarn()) {
            LogUtils.sendError("[Auto Pest Exchange] You need to be in the barn to set the pest exchange location!");
            return;
        }
        pestExchangeDeskX = mc.thePlayer.getPosition().getX();
        pestExchangeDeskY = mc.thePlayer.getPosition().getY();
        pestExchangeDeskZ = mc.thePlayer.getPosition().getZ();
        LogUtils.sendSuccess("[Auto Pest Exchange] Set the pest exchange location to "
                + FarmHelperConfig.pestExchangeDeskX + ", "
                + FarmHelperConfig.pestExchangeDeskY + ", "
                + FarmHelperConfig.pestExchangeDeskZ);
    };

    @Button(
            name = "Reset the pest exchange location", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            description = "Resets the pest exchange location",
            text = "Reset desk"
    )
    public static Runnable resetPestExchangeLocation = () -> {
        pestExchangeDeskX = 0;
        pestExchangeDeskY = 0;
        pestExchangeDeskZ = 0;
        LogUtils.sendSuccess("[Auto Pest Exchange] Reset the pest exchange location");
    };

    @Number(
            name = "Pest Exchange Desk X", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            min = -300, max = 300
    )
    public static int pestExchangeDeskX = 0;
    @Number(
            name = "Pest Exchange Desk Y", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            min = 50, max = 150
    )
    public static int pestExchangeDeskY = 0;
    @Number(
            name = "Pest Exchange Desk Z", category = AUTO_PEST_EXCHANGE, subcategory = "Auto Pest Exchange",
            min = -300, max = 300
    )
    public static int pestExchangeDeskZ = 0;
    @Info(
            text = "You don't have to set the pest exchange location, it will be set automatically. Check the guide for more info.",
            type = InfoType.INFO,
            category = AUTO_PEST_EXCHANGE,
            subcategory = "Auto Pest Exchange",
            size = 2
    )
    public static boolean autoPestExchangeInfo2;

    //</editor-fold>

    //<editor-fold desc="God Pot">
    @Switch(
            name = "Auto God Pot", category = AUTO_GOD_POT, subcategory = "God Pot",
            description = "Automatically purchases and consumes a God Pot", size = 2
    )
    public static boolean autoGodPot = false;

    @Switch(
            name = "Get God Pot from Backpack", category = AUTO_GOD_POT, subcategory = "God Pot", size = 2
    )
    public static boolean autoGodPotFromBackpack = true;

    @DualOption(
            name = "Storage Type", category = AUTO_GOD_POT, subcategory = "God Pot",
            description = "The storage type to get god pots from",
            left = "Backpack",
            right = "Ender Chest"
    )
    public static boolean autoGodPotStorageType = true;

    @Number(
            name = "Backpack Number", category = AUTO_GOD_POT, subcategory = "God Pot",
            description = "The backpack number, that contains god pots",
            min = 1, max = 18
    )
    public static int autoGodPotBackpackNumber = 1;

    @Switch(
            name = "Buy God Pot using Bits", category = AUTO_GOD_POT, subcategory = "God Pot"
    )
    public static boolean autoGodPotFromBits = false;

    @Switch(
            name = "Get God Pot from Auction House", category = AUTO_GOD_POT, subcategory = "God Pot",
            description = "If the user doesn't have a cookie, it will go to the hub and buy from AH"
    )
    public static boolean autoGodPotFromAH = false;

    @Info(
            text = "Priority getting God Pot is: Backpack -> Bits -> AH",
            type = InfoType.INFO, size = 2, category = AUTO_GOD_POT, subcategory = "God Pot"
    )
    private static int godPotInfo;

    //</editor-fold>

    //<editor-fold desc="Auto Sell">
    @Info(
            text = "Click ESC during Auto Sell, to stop it and pause for the next 15 minutes",
            category = AUTO_SELL, subcategory = "Auto Sell", type = InfoType.INFO, size = 2
    )
    public static boolean autoSellInfo;

    @Switch(
            name = "Enable Auto Sell", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "Enables auto sell"
    )
    public static boolean enableAutoSell = false;

    @DualOption(
            name = "Market type", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "The market type to sell crops to",
            left = "BZ",
            right = "NPC"
    )
    public static boolean autoSellMarketType = false;

    @Switch(
            name = "Sell Items In Sacks", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "Sells items in your sacks and inventory"
    )
    public static boolean autoSellSacks = false;

    @DualOption(
            name = "Sacks placement",
            category = AUTO_SELL, subcategory = "Auto Sell",
            description = "The sacks placement",
            left = "Inventory",
            right = "Sack of sacks"
    )
    public static boolean autoSellSacksPlacement = true;

    @Switch(
            name = "Pause Auto Sell during Jacob's contest", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "Pauses auto sell during Jacob's contest"
    )
    public static boolean pauseAutoSellDuringJacobsContest = false;

    @Number(
            name = "Inventory Full Time", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "The time to wait to test if inventory fullness ratio is still the same (or higher)",
            min = 1, max = 20
    )
    public static int inventoryFullTime = 6;

    @Number(
            name = "Inventory Full Ratio", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "After reaching this ratio, the macro will start counting from 0 to Inventory Full Time. If the fullness ratio is still the same (or higher) after the time has passed, it will start selling items.",
            min = 1, max = 100
    )
    public static int inventoryFullRatio = 65;
    @Button(
            name = "Sell Inventory Now", category = AUTO_SELL, subcategory = "Auto Sell",
            description = "Sells crops in your inventory",
            text = "Sell Inventory Now"
    )
    Runnable autoSellFunction = () -> {
        PlayerUtils.closeScreen();
        AutoSell.getInstance().enable(true);
    };

    @Switch(name = "Runes", category = AUTO_SELL, subcategory = "Customize items sold to NPC")
    public static boolean autoSellRunes = true;

    @Switch(name = "Dead Bush", category = AUTO_SELL, subcategory = "Customize items sold to NPC")
    public static boolean autoSellDeadBush = true;

    @Switch(name = "Iron Hoe", category = AUTO_SELL, subcategory = "Customize items sold to NPC")
    public static boolean autoSellIronHoe = true;

    @Switch(name = "Pest Vinyls", category = AUTO_SELL, subcategory = "Customize items sold to NPC")
    public static boolean autoSellPestVinyls = true;

    @Text(
            name = "Custom Items", category = AUTO_SELL, subcategory = "Customize items sold to NPC",
            description = "Add custom items to AutoSell here. Use | to split the messages.",
            placeholder = "Custom items to auto sell. Use | to split the messages.",
            size = 2
    )
    public static String autoSellCustomItems = "";
    //</editor-fold>

    //<editor-fold desc="Pest Repellent">
    @Switch(
            name = "Auto Pest Repellent", category = AUTO_REPELLENT, subcategory = "Pest Repellent",
            description = "Automatically uses pest repellent when it's not active"
    )
    public static boolean autoPestRepellent = false;

    @DualOption(
            name = "Pest Repellent Type", category = AUTO_REPELLENT, subcategory = "Pest Repellent",
            description = "The pest repellent type to use",
            left = "Pest Repellent",
            right = "Pest Repellent MAX"
    )
    public static boolean pestRepellentType = true;

    @Switch(
            name = "Pause Auto Pest Repellent during Jacob's contest", category = AUTO_REPELLENT, subcategory = "Pest Repellent",
            description = "Pauses auto pest repellent during Jacob's contest"
    )
    public static boolean pauseAutoPestRepellentDuringJacobsContest = false;

    @Button(
            name = "Reset Failsafe", category = AUTO_REPELLENT, subcategory = "Pest Repellent",
            text = "Click Here",
            description = "Resets the failsafe timer for repellent"
    )
    Runnable resetFailsafe = () -> {
        AutoRepellent.repellentFailsafeClock.schedule(0);
    };
    //</editor-fold>

    //<editor-fold desc="Auto Sprayonator">
    @Switch(
            name = "Auto Sprayonator", category = AUTO_SPRAYONATOR, subcategory = "Auto Sprayonator"
    )
    public static boolean autoSprayonator = false;

    @Dropdown(
            name = "Type", category = AUTO_SPRAYONATOR, subcategory = "Auto Sprayonator",
            description = "Item to spray plot with",
            options = {
                    "Fine Flour (+20 Farming Fortune)",
                    "Compost (Earthworm & Mosquito)",
                    "Honey Jar (Moth & Cricket)",
                    "Dung (Beetle & Fly)",
                    "Plant Matter (Locust & Slug)",
                    "Tasty Cheese (Rat & Mite)"
            }
    )
    public static int autoSprayonatorSprayMaterial = 0;

    @Slider(
            name = "Additional Delay", category = AUTO_SPRAYONATOR, subcategory = "Auto Sprayonator",
            description = "Additional delay between actions (in milliseconds)",
            min = 0, max = 5000, step = 1
    )
    public static int autoSprayonatorAdditionalDelay = 500;

    @Switch(
            name = "Auto Buy item from Bazaar", category = AUTO_SPRAYONATOR, subcategory = "Auto Sprayonator",
            description = "Auto buy necessary sprayonator item from bazaar if none is in the inventory"
    )
    public static boolean autoSprayonatorAutoBuyItem = false;

    @Number(
            name = "Buy Amount", category = AUTO_SPRAYONATOR, subcategory = "Auto Sprayonator",
            description = "Amount of item to buy from bazaar",
            min = 1, max = 64
    )
    public static int autoSprayonatorAutoBuyAmount = 1;
    //</editor-fold>

    //<editor-fold desc="Auto Composter">
    @Info(
            text = "Once the Composter timer is low enough, the macro will go to the Composter and supply crops and fuel to create compost.",
            type = InfoType.INFO,
            category = AUTO_COMPOSTER,
            subcategory = "Auto Composter",
            size = 2
    )
    public static boolean autoComposterInfo1;

    @Switch(
            name = "Enable Auto Composter", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Automatically fills the Composter"
    )
    public static boolean autoComposter = false;
    @Switch(
            name = "Pause the Auto Composter during Jacob's contests", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Pauses the Auto Composter during Jacob's contests"
    )
    public static boolean pauseAutoComposterDuringJacobsContest = true;
    @Slider(
            name = "The minimum amount of coins to start Auto Composter (in thousands)", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "The minimum amount of coins you need to have in your purse to start the Auto Composter",
            min = 500, max = 10_000
    )
    public static int autoComposterMinMoney = 2_000;
    @Slider(
            name = "Max Spend Limit (in Millions Per Purchase)", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = 0.2f, max = 7.5f
    )
    public static float autoComposterMaxSpendLimit = 1.5f;
    @Slider(
            name = "Organic Matter Left to Start the Auto Composter", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = 0, max = 540_000
    )
    public static int autoComposterOrganicMatterLeft = 30_000;
    @Slider(
            name = "Fuel Left to Start the Auto Composter", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = 0, max = 850_000
    )
    public static int autoComposterFuelLeft = 15_000;
    @Switch(
            name = "Autosell before filling Composter", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Automatically sells crops before filling the Composter"
    )
    public static boolean autoComposterAutosellBeforeFilling = false;
    @DualOption(
            name = "Travel method", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "The travel method to use to get to the Composter",
            left = "Fly",
            right = "Walk"
    )
    public static boolean autoComposterTravelMethod = false;
    @Info(
            text = "If you have any issues, try switching the travel method.",
            type = InfoType.INFO,
            category = AUTO_COMPOSTER,
            subcategory = "Auto Composter",
            size = 2
    )
    public static boolean autoComposterTravelMethodInfo;
    @Switch(
            name = "Send Webhook Log", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Logs all events related to the Auto Composter"
    )
    public static boolean logAutoComposterEvents = true;
    @Switch(
            name = "Highlight composter location", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Highlights the Composter location"
    )
    public static boolean highlightComposterLocation = true;
    @Info(
            text = "The Auto Composter will start automatically once you rewarp!",
            type = InfoType.WARNING,
            category = AUTO_COMPOSTER,
            subcategory = "Auto Composter",
            size = 2
    )
    public static boolean autoComposterInfo;

    @Button(
            name = "Trigger now Auto Composter", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Triggers the auto Composter manually",
            text = "Trigger now"
    )
    public static void triggerManuallyAutoComposter() {
        AutoComposter.getInstance().setManuallyStarted(true);
        AutoComposter.getInstance().start();
    }

    @Button(
            name = "Set the Composter location", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Sets the Composter location",
            text = "Set composter"
    )
    public static Runnable setComposterLocation = () -> {
        if (!PlayerUtils.isInBarn()) {
            LogUtils.sendError("[Auto Composter] You need to be in the barn to set the Composter location!");
            return;
        }
        composterX = mc.thePlayer.getPosition().getX();
        composterY = mc.thePlayer.getPosition().getY();
        composterZ = mc.thePlayer.getPosition().getZ();
        LogUtils.sendSuccess("[Auto Composter] Set the Composter location to "
                + FarmHelperConfig.composterX + ", "
                + FarmHelperConfig.composterY + ", "
                + FarmHelperConfig.composterZ);
    };

    @Button(
            name = "Reset the Composter location", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            description = "Resets the composter location",
            text = "Reset Composter"
    )
    public static Runnable resetComposterLocation = () -> {
        composterX = 0;
        composterY = 0;
        composterZ = 0;
        LogUtils.sendSuccess("[Auto Composter] Reset the Composter location");
    };

    @Number(
            name = "Composter X", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = -300, max = 300
    )
    public static int composterX = 0;
    @Number(
            name = "Composter Y", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = 50, max = 150
    )
    public static int composterY = 0;
    @Number(
            name = "Composter Z", category = AUTO_COMPOSTER, subcategory = "Auto Composter",
            min = -300, max = 300
    )
    public static int composterZ = 0;
    @Info(
            text = "You don't have to set the Composter location, it will be set automatically. Check the guide for more info.",
            type = InfoType.INFO,
            category = AUTO_COMPOSTER,
            subcategory = "Auto Composter",
            size = 2
    )
    public static boolean autoComposterInfo2;
    //</editor-fold>

    //<editor-fold desc="DELAYS">
    //<editor-fold desc="Changing Rows">
    @Slider(
            name = "Time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The minimum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float timeBetweenChangingRows = 400f;
    @Slider(
            name = "Additional random time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The maximum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float randomTimeBetweenChangingRows = 200f;
    @Switch(
            name = "Custom row change delays during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "Custom row change delays during Jacob's Contest"
    )
    public static boolean customRowChangeDelaysDuringJacob = false;
    @Slider(
            name = "Time between changing rows during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "The minimum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float timeBetweenChangingRowsDuringJacob = 400f;
    @Slider(
            name = "Additional random time between changing rows during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "The maximum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float randomTimeBetweenChangingRowsDuringJacob = 200f;
    //</editor-fold>

    //<editor-fold desc="Rotation Time">
    @Slider(
            name = "Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float rotationTime = 500f;
    @Slider(
            name = "Additional random Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rotationTimeRandomness = 300;
    @Switch(
            name = "Custom rotation delays during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "Custom rotation delays during Jacob's Contest"
    )
    public static boolean customRotationDelaysDuringJacob = false;
    @Slider(
            name = "Rotation Time during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float rotationTimeDuringJacob = 500f;
    @Slider(
            name = "Additional random Rotation Time during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rotationTimeRandomnessDuringJacob = 300;
    //</editor-fold>

    //<editor-fold desc="Fly Pathexecutioner Rotation Time">
    @Slider(
            name = "Fly PathExecutioner Rotation Time", category = DELAYS, subcategory = "Fly PathExecutioner",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float flyPathExecutionerRotationTime = 500f;
    @Slider(
            name = "Fly PathExecutioner Additional random Rotation Time", category = DELAYS, subcategory = "Fly PathExecutioner",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float flyPathExecutionerRotationTimeRandomness = 300;
    //</editor-fold>

    //<editor-fold desc="Pests Destroyer Time">
    @Slider(
            name = "Pests Destroyer Stuck Time (in minutes)", category = DELAYS, subcategory = "Pests Destroyer",
            description = "Pests Destroyer Stuck Time (in minutes) for single pest",
            min = 1, max = 7
    )
    public static float pestsKillerStuckTime = 3;
    @Slider(
            name = "Pests Destroyer Ticks of not seeing pest.", category = DELAYS, subcategory = "Pests Destroyer",
            description = "Pests Destroyer Ticks of not seeing pest while attacking (1 tick == 50ms) to trigger Escape to Hub. 0 to disable",
            min = 20, max = 200
    )
    public static int pestsKillerTicksOfNotSeeingPestWhileAttacking = 100;
    //</editor-fold>

    //<editor-fold desc="Gui Delay">
    @Slider(
            name = "GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The delay between clicking during GUI macros (in milliseconds)",
            min = 50f, max = 2000f
    )
    public static float macroGuiDelay = 400f;
    @Slider(
            name = "Additional random GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The maximum random time added to the delay time between clicking during GUI macros (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float macroGuiDelayRandomness = 350f;
    //</editor-fold>

    //<editor-fold desc="Plot Cleaning Time">
    @Slider(
            name = "Plot Cleaning Helper Rotation Time", category = DELAYS, subcategory = "Plot Cleaning Helper",
            description = "The time it takes to rotate the player",
            min = 20f, max = 500f
    )
    public static float plotCleaningHelperRotationTime = 50;
    @Slider(
            name = "Additional random Plot Cleaning Helper Rotation Time", category = DELAYS, subcategory = "Plot Cleaning Helper",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 500f
    )

    public static float plotCleaningHelperRotationTimeRandomness = 50;
    //</editor-fold>

    //<editor-fold desc="Rewarp Time">
    @Slider(
            name = "Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The delay between rewarping (in milliseconds)",
            min = 250f, max = 2000f
    )
    public static float rewarpDelay = 400f;
    @Slider(
            name = "Additional random Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The maximum random time added to the delay time between rewarping (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rewarpDelayRandomness = 350f;
    //</editor-fold>

    //<editor-fold desc="HUD">
    @Switch(
            name = "Streamer mode", category = HUD, subcategory = "Streamer mode",
            description = "Hides everything Farm Helper related from the screen."
    )
    public static boolean streamerMode = false;
    @Info(
            text = "Streamer mode does NOT disable failsafe notifications or sounds! It only hides visual elements.",
            type = InfoType.WARNING,
            category = HUD,
            subcategory = "Streamer mode",
            size = 2
    )
    public static boolean streamerModeInfo;
    @Info(
            text = "You must restart the game if you want to hide the window title after enabling the streamer mode.",
            type = InfoType.WARNING,
            category = HUD,
            subcategory = "Streamer mode"
    )
    public static boolean streamerModeInfo2;
    @HUD(
            name = "Status HUD - Visual Settings", category = HUD, subcategory = "Status"
    )
    public static StatusHUD statusHUD = new StatusHUD();
    @Switch(
            name = "Show Status HUD outside the garden", category = HUD, subcategory = "Status"
    )
    public static boolean showStatusHudOutsideGarden = false;

    @Switch(
            name = "Count RNG to $/Hr in Profit Calculator", category = HUD, subcategory = "Profit Calculator",
            description = "Count RNG to $/Hr"
    )
    public static boolean countRNGToProfitCalc = false;
    @Switch(
            name = "Reset stats between disabling", category = HUD, subcategory = "Profit Calculator"
    )
    public static boolean resetStatsBetweenDisabling = false;
    //    @Button(
//            name = "Reset Profit Calculator", category = HUD, subcategory = "Profit Calculator",
//            text = "Reset Now", size = 2
//    )
//    public void resetStats() {
//        ProfitCalculator.getInstance().resetProfits();
//    }
    @HUD(
            name = "Profit Calculator HUD - Visual Settings", category = HUD, subcategory = " "
    )
    public static ProfitCalculatorHUD profitHUD = new ProfitCalculatorHUD();
    //</editor-fold>

    //<editor-fold desc="DEBUG">
    //<editor-fold desc="Debug">

    @KeyBind(
            name = "Debug Keybind", category = DEBUG, subcategory = "Debug"
    )
    public static OneKeyBind debugKeybind = new OneKeyBind(Keyboard.KEY_NONE);
//    @KeyBind(
//            name = "Debug Keybind 2", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind2 = new OneKeyBind(Keyboard.KEY_H);
//    @KeyBind(
//            name = "Debug Keybind 3", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind3 = new OneKeyBind(Keyboard.KEY_J);

    @Switch(
            name = "Debug Mode", category = DEBUG, subcategory = "Debug",
            description = "Prints to chat what the bot is currently executing. Useful if you are having issues."
    )
    public static boolean debugMode = false;

    //</editor-fold>

    //<editor-fold desc="Debug Hud">
    @HUD(
            name = "Debug HUD", category = DEBUG, subcategory = " "
    )
    public static DebugHUD debugHUD = new DebugHUD();
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="EXPERIMENTAL">
    //<editor-fold desc="Fastbreak">
    @Switch(
            name = "Enable Fast Break (DANGEROUS)", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break is very risky and using it will most likely result in a ban. Proceed with caution."
    )
    public static boolean fastBreak = false;

    @Switch(
            name = "Enable Fast Break Randomization", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Randomizes the Fast Break chance"
    )
    public static boolean fastBreakRandomization = false;

    @Slider(
            name = "Fast Break Randomization Chance", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "The chance to break the block",
            min = 1, max = 100, step = 1
    )
    public static int fastBreakRandomizationChance = 5;

    @Info(
            text = "Fast Break will most likely ban you. Use at your own risk.",
            type = InfoType.ERROR,
            category = EXPERIMENTAL,
            subcategory = "Fast Break"
    )
    public static boolean fastBreakWarning;
    @Slider(
            name = "Fast Break Speed", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break speed",
            min = 1, max = 3
    )
    public static int fastBreakSpeed = 1;
    @Switch(
            name = "Disable Fast Break during banwave", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Disables Fast Break during banwave"
    )
    public static boolean disableFastBreakDuringBanWave = true;
    @Switch(
            name = "Disable Fast Break during Jacob's contest", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Disables Fast Break during Jacob's contest"
    )
    public static boolean disableFastBreakDuringJacobsContest = true;
    //</editor-fold>

    @Switch(
            name = "Automatically switch recognized crop", category = EXPERIMENTAL, subcategory = "Auto Switch",
            description = "Macro will be recognizing farming crop, which will lead to auto switching tool to the best one"
    )
    public static boolean autoSwitchTool = true;

    @Switch(
            name = "Count profit based on Cultivating enchant", category = EXPERIMENTAL, subcategory = "Profit Calculator",
            description = "Counts profit based on Cultivating enchant"
    )
    public static boolean profitCalculatorCultivatingEnchant = true;

    @Switch(
            name = "Count only current crops for Jacob's Contest excludes", category = EXPERIMENTAL, subcategory = "Jacob's Contest",
            description = "Counts only current crops for Jacob's Contest excludes"
    )
    public static boolean jacobContestCurrentCropsOnly = true;

    @Switch(
            name = "Show Debug logs about PD OTT", category = EXPERIMENTAL, subcategory = "Debug",
            description = "Shows debug logs about PD OTT"
    )
    public static boolean showDebugLogsAboutPDOTT = false;

    //</editor-fold>

    @Number(name = "Config Version", category = EXPERIMENTAL, subcategory = "Experimental", min = 0, max = 1337)
    public static int configVersion = 6;
    @Switch(
            name = "Shown Welcome GUI", category = EXPERIMENTAL, subcategory = "Experimental"
    )
    public static boolean shownWelcomeGUI = false;

    public FarmHelperConfig() {
        super(new Mod("Farm Helper", ModType.HYPIXEL, "/farmhelper/icon-mod/icon.png"), "/farmhelper/config.json");
        initialize();

        this.addDependency("macroType", "Macro Type", () -> !MacroHandler.getInstance().isMacroToggled());

        this.addDependency("customPitchLevel", "customPitch");
        this.addDependency("customYawLevel", "customYaw");

        this.addDependency("inventoryFullTime", "enableAutoSell");
        this.addDependency("autoSellMarketType", "enableAutoSell");
        this.addDependency("autoSellSacks", "enableAutoSell");
        this.addDependency("autoSellSacksPlacement", "enableAutoSell");
        this.addDependency("autoSellFunction", "enableAutoSell");

        this.addDependency("petSwapperDelay", "enablePetSwapper");
        this.addDependency("petSwapperName", "enablePetSwapper");

        this.addDependency("autoUngrabMouse", "This feature doesn't work properly on Mac OS!", () -> !Minecraft.isRunningOnMac);

        this.addDependency("desyncPauseDelay", "checkDesync");
        this.addDependency("failsafeSoundType", "Play Button", () -> enableFailsafeSound && !AudioManager.getInstance().isSoundPlaying());
        this.addDependency("_playFailsafeSoundButton", "enableFailsafeSound");
        this.addDependency("_stopFailsafeSoundButton", "enableFailsafeSound");
        this.hideIf("_playFailsafeSoundButton", () -> AudioManager.getInstance().isSoundPlaying());
        this.hideIf("_stopFailsafeSoundButton", () -> !AudioManager.getInstance().isSoundPlaying());
        this.addDependency("failsafeMcSoundSelected", "Minecraft Sound", () -> !failsafeSoundType && enableFailsafeSound);
        this.addDependency("failsafeSoundSelected", "Custom Sound", () -> failsafeSoundType && enableFailsafeSound);
        this.addDependency("failsafeSoundVolume", "Custom Sound", () -> failsafeSoundType && enableFailsafeSound);
        this.addDependency("maxOutMinecraftSounds", "Minecraft Sound", () -> !failsafeSoundType && enableFailsafeSound);
        this.hideIf("customFailsafeSoundWarning", () -> !failsafeSoundType || !enableFailsafeSound || failsafeSoundSelected != 0);
        this.addDependency("restartAfterFailSafeDelay", "enableRestartAfterFailSafe");
        this.addDependency("alwaysTeleportToGarden", "enableRestartAfterFailSafe");

        this.addDependency("schedulerFarmingTime", "enableScheduler");
        this.addDependency("schedulerFarmingTimeRandomness", "enableScheduler");
        this.addDependency("schedulerBreakTime", "enableScheduler");
        this.addDependency("schedulerBreakTimeRandomness", "enableScheduler");
        this.addDependency("pauseSchedulerDuringJacobsContest", "enableScheduler");

        this.addDependency("jacobNetherWartCap", "enableJacobFailsafes");
        this.addDependency("jacobPotatoCap", "enableJacobFailsafes");
        this.addDependency("jacobCarrotCap", "enableJacobFailsafes");
        this.addDependency("jacobWheatCap", "enableJacobFailsafes");
        this.addDependency("jacobSugarCaneCap", "enableJacobFailsafes");
        this.addDependency("jacobMushroomCap", "enableJacobFailsafes");
        this.addDependency("jacobMelonCap", "enableJacobFailsafes");
        this.addDependency("jacobPumpkinCap", "enableJacobFailsafes");
        this.addDependency("jacobCocoaBeansCap", "enableJacobFailsafes");
        this.addDependency("jacobCactusCap", "enableJacobFailsafes");
        this.addDependency("jacobFailsafeAction", "enableJacobFailsafes");

        this.addDependency("pauseVisitorsMacroDuringJacobsContest", "visitorsMacro");
        this.addDependency("triggerVisitorsMacro", "visitorsMacro");
        this.addDependency("visitorsMacroPriceManipulationMultiplier", "visitorsMacro");
        this.addDependency("visitorsMacroMinVisitors", "visitorsMacro");
        this.addDependency("visitorsMacroAutosellBeforeServing", "visitorsMacro");
        this.addDependency("visitorsMacroMinMoney", "visitorsMacro");
        this.addDependency("visitorsMacroMaxSpendLimit", "visitorsMacro");
        this.hideIf("visitorsFilteringMethod", () -> true);

        this.addDependency("visitorsActionUncommon", "filterVisitorsByRarity");
        this.addDependency("visitorsActionRare", "filterVisitorsByRarity");
        this.addDependency("visitorsActionLegendary", "filterVisitorsByRarity");
        this.addDependency("visitorsActionMythic", "filterVisitorsByRarity");
        this.addDependency("visitorsActionSpecial", "filterVisitorsByRarity");
        this.addDependency("nameFilteringType", "filterVisitorsByName");
        this.addDependency("nameActionType", "filterVisitorsByName");
        this.addDependency("nameActionType", "You can't reject a whitelisted visitor!", () -> !nameFilteringType);
        this.addDependency("nameFilter", "filterVisitorsByName");
        this.hideIf("nameFilterInfo", () -> !filterVisitorsByName || !filterVisitorsByRarity);

        this.addDependency("sendVisitorsMacroLogs", "visitorsMacro");
        this.addDependency("sendVisitorsMacroLogs", "enableWebHook");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "visitorsMacro");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "sendVisitorsMacroLogs");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "enableWebHook");

        this.addDependency("startKillingPestsAt", "enablePestsDestroyer");
        this.addDependency("pestAdditionalGUIDelay", "enablePestsDestroyer");
        this.addDependency("sprintWhileFlying", "enablePestsDestroyer");
        this.addDependency("pausePestsDestroyerDuringJacobsContest", "enablePestsDestroyer");

        this.hideIf("infoCookieBuffRequired",
                () -> GameStateHandler.getInstance().inGarden() || GameStateHandler.getInstance().getCookieBuffState() == BuffState.NOT_ACTIVE);

        this.addDependency("sendLogs", "enableWebHook");
        this.addDependency("sendStatusUpdates", "enableWebHook");
        this.addDependency("statusUpdateInterval", "enableWebHook");
        this.addDependency("webHookURL", "enableWebHook");
        this.addDependency("enableRemoteControl", "Enable Remote Control",
                () -> Loader.isModLoaded("farmhelperjdadependency") && FarmHelper.isJDAVersionCorrect);
        this.addDependency("discordRemoteControlAddress", "enableRemoteControl");
        this.addDependency("remoteControlPort", "enableRemoteControl");

        this.hideIf("infoRemoteControl", () -> Loader.isModLoaded("farmhelperjdadependency"));
        this.hideIf("info2RemoteControl", () -> !Loader.isModLoaded("farmhelperjdadependency") || (Loader.isModLoaded("farmhelperjdadependency")
                && FarmHelper.isJDAVersionCorrect));
        this.hideIf("failsafeSoundTimes", () -> true);

        this.addDependency("debugMode", "Streamer Mode", () -> !streamerMode);
        this.addDependency("streamerMode", "Debug Mode", () -> !debugMode);
        this.addDependency("streamerModeInfo", "debugMode");
        this.addDependency("streamerModeInfo2", "debugMode");

        this.addDependency("fastBreakSpeed", "fastBreak");
        this.addDependency("fastBreakRandomization", "fastBreak");
        this.addDependency("fastBreakRandomizationChance", "fastBreak");
        this.addDependency("disableFastBreakDuringBanWave", "fastBreak");
        this.addDependency("disableFastBreakDuringJacobsContest", "fastBreak");

        this.addDependency("autoGodPotFromBackpack", "autoGodPot");
        this.addDependency("autoGodPotFromBits", "autoGodPot");
        this.addDependency("autoGodPotFromAH", "autoGodPot");

        this.hideIf("autoGodPotBackpackNumber", () -> !autoGodPotFromBackpack);
        this.hideIf("autoGodPotStorageType", () -> !autoGodPotFromBackpack);

        this.addDependency("banwaveAction", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveThreshold", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveThresholdType", "enableLeavePauseOnBanwave");
        this.addDependency("delayBeforeReconnecting", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveDontLeaveDuringJacobsContest", "enableLeavePauseOnBanwave");

        this.addDependency("antiStuckTriesUntilRewarp", "enableAntiStuck");

        this.addDependency("sendWebhookLogIfPestsDetectionNumberExceeded", "enableWebHook");
        this.addDependency("pingEveryoneOnPestsDetectionNumberExceeded", "sendWebhookLogIfPestsDetectionNumberExceeded");
        this.addDependency("pingEveryoneOnPestsDetectionNumberExceeded", "enableWebHook");

        this.addDependency("pauseAutoPestExchangeDuringJacobsContest", "autoPestExchange");
        this.addDependency("autoPestExchangeIgnoreJacobsContest", "autoPestExchange");
        this.addDependency("autoPestExchangeTriggerBeforeContestStarts", "autoPestExchange");
        this.addDependency("autoPestExchangeTriggerBeforeContestStarts",
                "You can either wait until Jacob's Contest or run it regardless.", () -> !autoPestExchangeIgnoreJacobsContest);
        this.addDependency("autoPestExchangeMinPests", "autoPestExchange");
        this.addDependency("logAutoPestExchangeEvents", "autoPestExchange");
        this.hideIf("pestExchangeDeskX", () -> true);
        this.hideIf("pestExchangeDeskY", () -> true);
        this.hideIf("pestExchangeDeskZ", () -> true);

        this.addDependency("pestRepellentType", "autoPestRepellent");

        this.addDependency("averageBPSDrop", "enableBpsCheck");

        this.addDependency("captureClipKeybind", "", () -> captureClipAfterFailsafe || captureClipAfterGettingBanned);
        this.addDependency("clipCapturingType", "", () -> captureClipAfterFailsafe || captureClipAfterGettingBanned);
        this.addDependency("captureClipDelay", "", () -> captureClipAfterFailsafe || captureClipAfterGettingBanned);

        this.addDependency("pestsDestroyerOnTheTrackFOV", "pestsDestroyerOnTheTrack");
        this.addDependency("dontKillPestsOnTrackDuringJacobsContest", "pestsDestroyerOnTheTrack");

        this.addDependency("timeBetweenChangingRowsDuringJacob", "customRowChangeDelaysDuringJacob");
        this.addDependency("randomTimeBetweenChangingRowsDuringJacob", "customRowChangeDelaysDuringJacob");
        this.addDependency("rotationTimeDuringJacob", "customRotationDelaysDuringJacob");
        this.addDependency("rotationTimeRandomnessDuringJacob", "customRotationDelaysDuringJacob");

        this.addDependency("pestArmorSlot0", "pestSwapArmorBefore");
        this.addDependency("pestArmorSlot1", "pestSwapArmorAfter");

        this.addDependency("leaveTime", "leaveTimer");

        this.hideIf("shownWelcomeGUI", () -> true);

        this.hideIf("configVersion", () -> true);

        registerKeyBind(openGuiKeybind, this::openGui);
        registerKeyBind(toggleMacro, () -> MacroHandler.getInstance().toggleMacro());
        registerKeyBind(debugKeybind, () -> {
        });
        registerKeyBind(freelookKeybind, () -> Freelook.getInstance().toggle());
        registerKeyBind(plotCleaningHelperKeybind, () -> PlotCleaningHelper.getInstance().toggle());
        registerKeyBind(enablePestsDestroyerKeyBind, () -> {
            if (PestsDestroyer.getInstance().canEnableMacro(true)) {
                PestsDestroyer.getInstance().start();
            }
        });
        registerKeyBind(cancelFailsafeKeybind, () -> {
            if (FailsafeManager.getInstance().getChooseEmergencyDelay().isScheduled()) {
                FailsafeManager.getInstance().stopFailsafes();
                LogUtils.sendWarning("[Failsafe] Emergency has been cancelled!");
            }
        });
//        registerKeyBind(debugKeybind2, () -> {
//            MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
//            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                BlockPos blockPos = objectMouseOver.getBlockPos();
//                BlockPos oppositeSide = blockPos.offset(objectMouseOver.sideHit);
//                LogUtils.sendDebug("Block: " + oppositeSide);
//                FlyPathfinder.getInstance().setGoal(new GoalBlock(oppositeSide));
//            }
//        });
//        registerKeyBind(debugKeybind3, () -> {
//                    FlyPathfinder.getInstance().getPathTo(FlyPathfinder.getInstance().getGoal());
//                });
        save();
    }

    public static void addRewarp() {
        if (FarmHelperConfig.rewarpList.stream().anyMatch(rewarp -> rewarp.isTheSameAs(BlockUtils.getRelativeBlockPos(0, 0, 0)))) {
            LogUtils.sendError("Rewarp location has already been set!");
            return;
        }
        Rewarp newRewarp = new Rewarp(BlockUtils.getRelativeBlockPos(0, 0, 0));
        if (newRewarp.getDistance(new BlockPos(PlayerUtils.getSpawnLocation())) < 2) {
            LogUtils.sendError("Rewarp location is too close to the spawn location! You must put it AT THE END OF THE FARM!");
            return;
        }
        rewarpList.add(newRewarp);
        LogUtils.sendSuccess("Added rewarp: " + newRewarp);
        saveRewarpConfig();
    }

    public static void removeRewarp() {
        Rewarp closest = null;
        if (rewarpList.isEmpty()) {
            LogUtils.sendError("No rewarp locations set!");
            return;
        }
        double closestDistance = Double.MAX_VALUE;
        for (Rewarp rewarp : rewarpList) {
            double distance = rewarp.getDistance(BlockUtils.getRelativeBlockPos(0, 0, 0));
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest != null) {
            rewarpList.remove(closest);
            LogUtils.sendSuccess("Removed the closest rewarp: " + closest);
            saveRewarpConfig();
        }
    }

    public static void removeAllRewarps() {
        rewarpList.clear();
        LogUtils.sendSuccess("Removed all saved rewarp positions");
        saveRewarpConfig();
    }

    public static void saveRewarpConfig() {
        try {
            if (!configRewarpFile.exists()) {
                Files.createFile(configRewarpFile.toPath());
            }

            Files.write(configRewarpFile.toPath(), FarmHelper.gson.toJson(rewarpList).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MacroEnum getMacro() {
        return MacroEnum.values()[macroType];
    }

    public static long getRandomTimeBetweenChangingRows() {
        if (customRowChangeDelaysDuringJacob && GameStateHandler.getInstance().inJacobContest()) {
            return (long) (timeBetweenChangingRowsDuringJacob + (float) Math.random() * randomTimeBetweenChangingRowsDuringJacob);
        }
        return (long) (timeBetweenChangingRows + (float) Math.random() * randomTimeBetweenChangingRows);
    }

    public static long getMaxTimeBetweenChangingRows() {
        return (long) (timeBetweenChangingRows + randomTimeBetweenChangingRows);
    }

    public static long getRandomRotationTime() {
        if (customRotationDelaysDuringJacob && GameStateHandler.getInstance().inJacobContest()) {
            return (long) (rotationTimeDuringJacob + (float) Math.random() * rotationTimeRandomnessDuringJacob);
        }
        return (long) (rotationTime + (float) Math.random() * rotationTimeRandomness);
    }

    public static long getRandomFlyPathExecutionerRotationTime() {
        return (long) (flyPathExecutionerRotationTime + (float) Math.random() * flyPathExecutionerRotationTimeRandomness);
    }

    public static long getRandomGUIMacroDelay() {
        return (long) (macroGuiDelay + (float) Math.random() * macroGuiDelayRandomness);
    }

    public static long getRandomPlotCleaningHelperRotationTime() {
        return (long) (plotCleaningHelperRotationTime + (float) Math.random() * plotCleaningHelperRotationTimeRandomness);
    }

    public static long getRandomRewarpDelay() {
        return (long) (rewarpDelay + (float) Math.random() * rewarpDelayRandomness);
    }

    public String getJson() {
        String json = gson.toJson(this);
        if (json == null || json.equals("{}")) {
            json = nonProfileSpecificGson.toJson(this);
        }
        return json;
    }

    public static void checkForUpdate() {
        AutoUpdaterGUI.checkedForUpdates = true;
        AutoUpdaterGUI.shownGui = false;
        AutoUpdaterGUI.getLatestVersion();
        if (AutoUpdaterGUI.isOutdated) {
            LogUtils.sendWarning("You are using an outdated version! The latest version is " + AutoUpdaterGUI.latestVersion + "!");
            AutoUpdaterGUI.showGUI();
        } else {
            LogUtils.sendSuccess("You are using the latest version!");
        }
    }

    public enum MacroEnum {
        S_V_NORMAL_TYPE,
        S_PUMPKIN_MELON,
        S_PUMPKIN_MELON_MELONGKINGDE,
        S_PUMPKIN_MELON_DEFAULT_PLOT,
        S_SUGAR_CANE,
        S_CACTUS,
        S_CACTUS_SUNTZU,
        S_COCOA_BEANS,
        S_COCOA_BEANS_TRAPDOORS,
        S_COCOA_BEANS_LEFT_RIGHT,
        S_MUSHROOM,
        S_MUSHROOM_ROTATE,
        S_MUSHROOM_SDS,
        C_NORMAL_TYPE
    }

    @Getter
    public enum CropEnum {
        NONE("None"),
        CARROT("Carrot"),
        NETHER_WART("Nether Wart"),
        POTATO("Potato"),
        WHEAT("Wheat"),
        SUGAR_CANE("Sugar Cane"),
        MELON("Melon"),
        PUMPKIN("Pumpkin"),
        PUMPKIN_MELON_UNKNOWN("Pumpkin/Melon"),
        CACTUS("Cactus"),
        COCOA_BEANS("Cocoa Beans"),
        MUSHROOM("Mushroom"),
        MUSHROOM_ROTATE("Mushroom"),
        ;

        final String localizedName;

        CropEnum(String localizedName) {
            this.localizedName = localizedName;
        }
    }
}
