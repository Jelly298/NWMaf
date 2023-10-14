package com.github.may2beez.farmhelperv2.util;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.github.may2beez.farmhelperv2.config.FarmHelperConfig;
import com.github.may2beez.farmhelperv2.config.struct.DiscordWebhook;
import com.github.may2beez.farmhelperv2.feature.impl.ProfitCalculator;
import com.github.may2beez.farmhelperv2.handler.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LogUtils {
    private static String lastDebugMessage;
    private static String lastWebhook;
    private static long logMsgTime = 1000;
    private static long statusMsgTime = -1;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public synchronized static void sendLog(ChatComponentText chat) {
        if (mc.thePlayer != null && !FarmHelperConfig.hideLogs)
            mc.thePlayer.addChatMessage(chat);
        else if (mc.thePlayer == null)
            System.out.println("[Farm Helper] " + chat.getUnformattedText());
    }

    public static void sendSuccess(String message) {
        sendLog(new ChatComponentText("§2§lFarm Helper §8» §a" + message));
    }

    public static void sendWarning(String message) {
        sendLog(new ChatComponentText("§6§lFarm Helper §8» §e" + message));
    }

    public static void sendError(String message) {
        sendLog(new ChatComponentText("§4§lFarm Helper §8» §c" + message));
    }

    public static void sendDebug(String message) {
        if (lastDebugMessage != null && lastDebugMessage.equals(message)) return;
        if (FarmHelperConfig.debugMode && mc.thePlayer != null)
            sendLog(new ChatComponentText("§3§lFarm Helper §8» §7" + message));
        else
            System.out.println("[Farm Helper] " + message);
        lastDebugMessage = message;
    }

    public static void sendFailsafeMessage(String message) {
        sendLog(new ChatComponentText("§5§lFarm Helper §8» §d" + message));
        webhookLog(StringUtils.stripControlCodes(message));
    }

    public static String getRuntimeFormat() {
        if (!MacroHandler.getInstance().getMacroingTimer().isScheduled())
            return "0h 0m 0s";
        long millis = MacroHandler.getInstance().getMacroingTimer().getElapsedTime();
        return formatTime(millis) + (MacroHandler.getInstance().getMacroingTimer().paused ? " (Paused)" : "");
    }

    public static String formatTime(long millis) {

        if (TimeUnit.MILLISECONDS.toHours(millis) > 0) {
            return String.format("%dh %dm %ds",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        } else if (TimeUnit.MILLISECONDS.toMinutes(millis) > 0) {
            return String.format("%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        } else {
            return TimeUnit.MILLISECONDS.toSeconds(millis) + "." + (TimeUnit.MILLISECONDS.toMillis(millis) -
                    TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis))) / 100 + "s";
        }
    }

    public static String capitalize(String message) {
        String[] words = message.split("_|\\s");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    public static void webhookStatus() {
        if (!FarmHelperConfig.sendStatusUpdates) return;

        if (statusMsgTime == -1) {
            statusMsgTime = System.currentTimeMillis();
        }
        long timeDiff = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - statusMsgTime);
        if (timeDiff > FarmHelperConfig.statusUpdateInterval && FarmHelperConfig.sendStatusUpdates) {
            DiscordWebhook webhook = new DiscordWebhook(FarmHelperConfig.webHookURL.replace(" ", "").replace("\n", "").trim());
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Farm Helper")
                    .setDescription("```" + "I'm still alive!" + "```")
                    .setColor(Color.decode("#ff3b3b"))
                    .setFooter("Jelly", "")
                    .setThumbnail("https://crafatar.com/renders/head/" + mc.thePlayer.getUniqueID())
                    .addField("Username", mc.thePlayer.getName(), true)
                    .addField("Runtime", getRuntimeFormat(), true)
                    .addField("Total Profit", ProfitCalculator.getInstance().getRealProfitString(), false)
                    .addField("Profit / hr", ProfitCalculator.getInstance().getProfitPerHourString(), false)
                    .addField("Crop Type", capitalize(String.valueOf(MacroHandler.getInstance().getCrop())), true)
            );
            Multithreading.schedule(() -> {
                try {
                    webhook.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 0, TimeUnit.MILLISECONDS);
            statusMsgTime = System.currentTimeMillis();
        }
    }

    public static void webhookLog(String message) {
        if (!FarmHelperConfig.sendStatusUpdates) return;

        long timeDiff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - logMsgTime);
        sendDebug("Last webhook message: " + timeDiff);
        if (FarmHelperConfig.sendLogs && (timeDiff > 20 || !Objects.equals(lastWebhook, message))) {
            DiscordWebhook webhook = new DiscordWebhook(FarmHelperConfig.webHookURL.replace(" ", "").replace("\n", "").trim());
            webhook.setUsername("Jelly - Farm Helper");
            webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/1152966451406327858/1160577992876109884/icon.png");
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setDescription("**Farm Helper Log** ```" + message + "```")
                    .setColor(Color.decode("#741010"))
                    .setFooter(mc.thePlayer.getName(), "")
            );
            Multithreading.schedule(() -> {
                try {
                    webhook.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 0, TimeUnit.MILLISECONDS);
            logMsgTime = System.currentTimeMillis();
        }
        lastWebhook = message;
    }
}
