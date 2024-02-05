package com.jelly.farmhelperv2;

import baritone.api.BaritoneAPI;
import cc.polyfrost.oneconfig.utils.Notifications;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.farmhelperv2.command.FarmHelperMainCommand;
import com.jelly.farmhelperv2.command.RewarpCommand;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.MillisecondEvent;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.impl.MovRecPlayer;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.pathfinder.WorldCache;
import com.jelly.farmhelperv2.remote.DiscordBotHandler;
import com.jelly.farmhelperv2.remote.WebsocketHandler;
import com.jelly.farmhelperv2.util.FailsafeUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.ReflectionUtils;
import com.jelly.farmhelperv2.util.TickRate;
import com.jelly.farmhelperv2.util.helper.AudioManager;
import com.jelly.farmhelperv2.util.helper.BaritoneEventListener;
import com.jelly.farmhelperv2.util.helper.FlyPathfinder;
import com.jelly.farmhelperv2.util.helper.TickTask;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = "farmhelperv2", useMetadata = true)
public class FarmHelper {
    public static final String VERSION = "%%VERSION%%";
    public static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    public static FarmHelperConfig config;
    public static boolean sentInfoAboutShittyClient = false;
    public static boolean isDebug = false;
    private final Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        initializeFields();
        initializeListeners();
        initializeCommands();
        FeatureManager.getInstance().fillFeatures().forEach(MinecraftForge.EVENT_BUS::register);

        mc.gameSettings.pauseOnLostFocus = false;
        mc.gameSettings.gammaSetting = 1000;
        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
        Display.setTitle("Farm Helper 〔v" + VERSION + "〕 " + (!isDebug ? "Bing Chilling" : "wazzup dev?") + " ☛ " + Minecraft.getMinecraft().getSession().getUsername());
        FailsafeUtils.getInstance();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> MinecraftForge.EVENT_BUS.post(new MillisecondEvent()), 0, 1, TimeUnit.MILLISECONDS);
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(new BaritoneEventListener());
    }

    @SubscribeEvent
    public void onTickSendInfoAboutShittyClient(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (sentInfoAboutShittyClient) return;

        if (ReflectionUtils.hasPackageInstalled("feather")) {
            Notifications.INSTANCE.send("FarmHelper", "You've got Feather Client installed! Be aware, you might have a lot of bugs because of this shitty client!", 15000);
            LogUtils.sendError("You've got §6§lFeather Client §cinstalled! Be aware, you might have a lot of bugs because of this shitty client!");
        }
        if (ReflectionUtils.hasPackageInstalled("cc.woverflow.hytils.HytilsReborn")) {
            Notifications.INSTANCE.send("FarmHelper", "You've got Hytils installed in your mods folder! This will cause many issues with rewarping as it sends tons of commands every minute.", 15000);
            LogUtils.sendError("You've got §6§lHytils §cinstalled in your mods folder! This will cause many issues with rewarping as it sends tons of commands every minute.");
        }
        if (Minecraft.isRunningOnMac && FarmHelperConfig.autoUngrabMouse) {
            FarmHelperConfig.autoUngrabMouse = false;
            Notifications.INSTANCE.send("FarmHelper", "Auto Ungrab Mouse feature doesn't work properly on Mac OS. It has been disabled automatically.", 15000);
            LogUtils.sendError("Auto Ungrab Mouse feature doesn't work properly on Mac OS. It has been disabled automatically.");
        }
        if (FarmHelperConfig.configVersion == 1 && FarmHelperConfig.pestsKillerTicksOfNotSeeingPestWhileAttacking < 80) {
            FarmHelperConfig.pestsKillerTicksOfNotSeeingPestWhileAttacking = 100;
            Notifications.INSTANCE.send("FarmHelper", "Pests Killer Ticks Of Not Seeing Pest While Attacking has been set to 100 ticks because of a bug in the previous version.", 15000);
            LogUtils.sendWarning("Pests Killer Ticks Of Not Seeing Pest While Attacking has been set to 100 ticks because of a bug in the previous version.");
        }
        if (FarmHelperConfig.configVersion == 1)
            FarmHelperConfig.configVersion = 2;
        sentInfoAboutShittyClient = true;
    }

    private void initializeListeners() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(TickRate.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FailsafeManager.getInstance());
        MinecraftForge.EVENT_BUS.register(GameStateHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(MacroHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(TickTask.getInstance());
        MinecraftForge.EVENT_BUS.register(MovRecPlayer.getInstance());
        MinecraftForge.EVENT_BUS.register(WebsocketHandler.getInstance());
        if (Loader.isModLoaded("farmhelperjdadependency"))
            MinecraftForge.EVENT_BUS.register(DiscordBotHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(AudioManager.getInstance());
        MinecraftForge.EVENT_BUS.register(RotationHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(FlyPathfinder.getInstance());
        MinecraftForge.EVENT_BUS.register(WorldCache.getInstance());
        MinecraftForge.EVENT_BUS.register(FlyPathFinderExecutor.getInstance());
    }

    private void initializeFields() {
        config = new FarmHelperConfig();
    }

    private void initializeCommands() {
        ClientCommandHandler.instance.registerCommand(new RewarpCommand());
        CommandManager.register(new FarmHelperMainCommand());
    }
}
