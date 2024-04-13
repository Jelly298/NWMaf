package com.jelly.farmhelperv2.feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.ClickedBlockEvent;
import com.jelly.farmhelperv2.event.MillisecondEvent;
import com.jelly.farmhelperv2.event.ReceivePacketEvent;
import com.jelly.farmhelperv2.event.UpdateScoreboardLineEvent;
import com.jelly.farmhelperv2.failsafe.impl.LowerAvgBpsFailsafe;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.hud.ProfitCalculatorHUD;
import com.jelly.farmhelperv2.util.APIUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfitCalculator implements IFeature {
    private static ProfitCalculator instance;
    public final List<BazaarItem> cropsToCount = new ArrayList<BazaarItem>() {{
        final int HAY_ENCHANTED_TIER_1 = 144;
        final int ENCHANTED_TIER_1 = 160;
        final int ENCHANTED_TIER_2 = 25600;

        add(new BazaarItem("Hay Bale", "ENCHANTED_HAY_BLOCK", HAY_ENCHANTED_TIER_1, 54).setImage());
        add(new BazaarItem("Seeds", "ENCHANTED_SEEDS", ENCHANTED_TIER_1, 3).setImage());
        add(new BazaarItem("Carrot", "ENCHANTED_CARROT", ENCHANTED_TIER_1, 3).setImage());
        add(new BazaarItem("Potato", "ENCHANTED_POTATO", ENCHANTED_TIER_1, 3).setImage());
        add(new BazaarItem("Melon", "ENCHANTED_MELON_BLOCK", ENCHANTED_TIER_2, 2).setImage());
        add(new BazaarItem("Pumpkin", "ENCHANTED_PUMPKIN", ENCHANTED_TIER_1, 10).setImage());
        add(new BazaarItem("Sugar Cane", "ENCHANTED_SUGAR_CANE", ENCHANTED_TIER_2, 4).setImage());
        add(new BazaarItem("Cocoa Beans", "ENCHANTED_COCOA", ENCHANTED_TIER_1, 3).setImage());
        add(new BazaarItem("Nether Wart", "MUTANT_NETHER_STALK", ENCHANTED_TIER_2, 4).setImage());
        add(new BazaarItem("Cactus Green", "ENCHANTED_CACTUS", ENCHANTED_TIER_2, 3).setImage());
        add(new BazaarItem("Red Mushroom", "ENCHANTED_RED_MUSHROOM", ENCHANTED_TIER_1, 10).setImage());
        add(new BazaarItem("Brown Mushroom", "ENCHANTED_BROWN_MUSHROOM", ENCHANTED_TIER_1, 10).setImage());
    }};
    public final List<BazaarItem> rngDropToCount = new ArrayList<BazaarItem>() {{
        add(new BazaarItem("Cropie", "CROPIE", 1, 25_000).setImage());
        add(new BazaarItem("Squash", "SQUASH", 1, 75_000).setImage());
        add(new BazaarItem("Fermento", "FERMENTO", 1, 250_000).setImage());
        add(new BazaarItem("Burrowing Spores", "BURROWING_SPORES", 1, 1).setImage());
    }};
    public final List<String> cropsToCountList = Arrays.asList("Hay Bale", "Seeds", "Carrot", "Potato", "Melon", "Pumpkin", "Sugar Cane", "Cocoa Beans", "Nether Wart", "Cactus Green", "Red Mushroom", "Brown Mushroom");
    public final List<String> rngToCountList = Arrays.asList("Cropie", "Squash", "Fermento", "Burrowing Spores");
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    private final NumberFormat oneDecimalDigitFormatter = NumberFormat.getNumberInstance(Locale.US);
    @Getter
    private final Clock updateClock = new Clock();
    @Getter
    private final Clock updateBazaarClock = new Clock();
    private final Pattern regex = Pattern.compile("Dicer dropped (\\d+)x ([\\w\\s]+)!");
    public double realProfit = 0;
    public double realHourlyProfit = 0;
    @Getter
    private double bountifulProfit = 0;
    public long blocksBroken = 0;
    private final Queue<Long> bpsQueue = new LinkedList<>();
    private final Clock bpsClock = new Clock();
    private long bps = 0;

    public HashMap<String, APICrop> bazaarPrices = new HashMap<>();
    private boolean cantConnectToApi = false;

    {
        formatter.setMaximumFractionDigits(0);
    }

    {
        oneDecimalDigitFormatter.setMaximumFractionDigits(1);
    }

    public static ProfitCalculator getInstance() {
        if (instance == null) {
            instance = new ProfitCalculator();
        }
        return instance;
    }

    public static String getImageName(String name) {
        switch (name) {
            case "Hay Bale":
                return "ehaybale.png";
            case "Seeds":
                return "eseeds.png";
            case "Carrot":
                return "ecarrot.png";
            case "Potato":
                return "epotato.png";
            case "Melon":
                return "emelon.png";
            case "Pumpkin":
                return "epumpkin.png";
            case "Sugar Cane":
                return "ecane.png";
            case "Cocoa Beans":
                return "ecocoabeans.png";
            case "Nether Wart":
                return "mnw.png";
            case "Cactus Green":
                return "ecactus.png";
            case "Red Mushroom":
                return "eredmushroom.png";
            case "Brown Mushroom":
                return "ebrownmushroom.png";
            case "Cropie":
                return "cropie.png";
            case "Squash":
                return "squash.png";
            case "Fermento":
                return "fermento.png";
            case "Burrowing Spores":
                return "burrowingspores.png";
            default:
                throw new IllegalArgumentException("No image for " + name);
        }
    }

    public String getRealProfitString() {
        return formatter.format(realProfit);
    }

    public String getProfitPerHourString() {
        return formatter.format(realHourlyProfit) + "/hr";
    }

    public String getBPS() {
        if (!MacroHandler.getInstance().getMacroingTimer().isScheduled()) return "0.0 BPS";
        return oneDecimalDigitFormatter.format(getBPSFloat()) + " BPS";
    }

    public float getBPSFloat() {
        if (!MacroHandler.getInstance().getMacroingTimer().isScheduled()) return 0;
        return ((int) ((double) this.bps / this.bpsQueue.size() * 10.0D)) / 10.0F;
    }

    @Override
    public String getName() {
        return "Profit Calculator";
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
    public void start() {
        if (ProfitCalculatorHUD.resetStatsBetweenDisabling) {
            resetProfits();
        }
    }

    @Override
    public void stop() {
        updateClock.reset();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        blocksBroken = 0;
        bpsClock.reset();
    }

    @Override
    public boolean isToggled() {
        return true;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    public void resetProfits() {
        realProfit = 0;
        realHourlyProfit = 0;
        bountifulProfit = 0;
        blocksBroken = 0;
        bpsQueue.clear();
        bpsClock.reset();
        bps = 0;
        previousCurrentPurse = 0;
        previousCultivating.clear();
        cropsToCount.forEach(crop -> crop.currentAmount = 0);
        rngDropToCount.forEach(drop -> drop.currentAmount = 0);
        LowerAvgBpsFailsafe.getInstance().resetStates();
    }

    private final HashMap<String, Long> previousCultivating = new HashMap<>();

    @SubscribeEvent
    public void onTickUpdateBPS(MillisecondEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;

        if (bpsClock.passed()) {
            bpsClock.schedule(1_000);
            bpsQueue.add(blocksBroken);
            LogUtils.sendDebug("Blocks Broken last second: " + blocksBroken);
            bps += blocksBroken;
            LogUtils.sendDebug("BPS: " + bps);
            blocksBroken = 0;
            if (bpsQueue.size() == 61) {
                bps -= bpsQueue.poll();
            }
        }
    }

    @SubscribeEvent
    public void onTickUpdateProfit(TickEvent.ClientTickEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled() || 
        !MacroHandler.getInstance().isCurrentMacroEnabled() || 
        !GameStateHandler.getInstance().inGarden()) 
        return;


        double profit = 0;
        double rngPrice = 0;

        ItemStack currentItem = mc.thePlayer.getHeldItem();
        if (currentItem != null && currentItem.getItem() != null && FarmHelperConfig.profitCalculatorCultivatingEnchant) {
            long cultivatingCounter = GameStateHandler.getInstance().getCurrentCultivating().getOrDefault(currentItem.getDisplayName(), 0L);
            long previousCultivatingCounter = previousCultivating.getOrDefault(currentItem.getDisplayName(), 0L);
            if (previousCultivatingCounter == 0) {
                previousCultivating.put(currentItem.getDisplayName(), cultivatingCounter);
                previousCultivatingCounter = cultivatingCounter;
            }
            if (cultivatingCounter > previousCultivatingCounter) {
                long diff = cultivatingCounter - previousCultivatingCounter;
                cropsToCount.stream().filter(crop -> crop.localizedName.equals(MacroHandler.getInstance().getCrop().getLocalizedName())).findFirst().ifPresent(item -> item.currentAmount += diff);
                previousCultivating.put(currentItem.getDisplayName(), cultivatingCounter);
            }
        }

        for (BazaarItem item : cropsToCount) {
            if (cantConnectToApi) {
                profit += item.currentAmount * item.npcPrice;
            } else {
                double price = bazaarPrices.containsKey(item.localizedName) ? bazaarPrices.get(item.localizedName).currentPrice : item.npcPrice;
                profit += (float) (item.currentAmount / item.amountToEnchanted * price);
            }
        }

        for (BazaarItem item : rngDropToCount) {
            if (cantConnectToApi) {
                rngPrice += item.currentAmount * item.npcPrice;
            } else {
                double price = bazaarPrices.containsKey(item.localizedName) ? bazaarPrices.get(item.localizedName).currentPrice : item.npcPrice;
                rngPrice += (float) (item.currentAmount * price);
            }
        }

        profit += bountifulProfit;

        realProfit = profit + rngPrice;

        float elapsedTime = MacroHandler.getInstance().getMacroingTimer().getElapsedTime() / 1000f / 60 / 60;
        realHourlyProfit = ProfitCalculatorHUD.countRNGToProfitCalc ? realProfit / elapsedTime : profit / elapsedTime;
    }

    private double previousCurrentPurse = 0;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onScoreboardUpdate(UpdateScoreboardLineEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!MacroHandler.getInstance().isCurrentMacroEnabled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;

        ItemStack currentItem = mc.thePlayer.getHeldItem();
        if (currentItem != null && StringUtils.stripControlCodes(currentItem.getDisplayName()).startsWith("Bountiful")) {
            if (GameStateHandler.getInstance().getCurrentPurse() == previousCurrentPurse) return;
            if (GameStateHandler.getInstance().getPreviousPurse() == 0) return;
            double value = GameStateHandler.getInstance().getCurrentPurse() - GameStateHandler.getInstance().getPreviousPurse();
            if (value > 0)
                bountifulProfit += value;
            previousCurrentPurse = GameStateHandler.getInstance().getCurrentPurse();
        }
    }

    @SubscribeEvent
    public void onBlockChange(ClickedBlockEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;

        switch (MacroHandler.getInstance().getCrop()) {
            case NETHER_WART:
            case CARROT:
            case POTATO:
            case WHEAT:
                if (event.getBlock() instanceof BlockCrops ||
                        event.getBlock() instanceof BlockNetherWart) {
                    blocksBroken++;
                }
                break;
            case SUGAR_CANE:
                if (event.getBlock() instanceof BlockReed) {
                    blocksBroken++;
                }
                break;
            case MELON:
                if (event.getBlock().equals(Blocks.melon_block)) {
                    blocksBroken++;
                }
                break;
            case PUMPKIN:
                if (event.getBlock().equals(Blocks.pumpkin)) {
                    blocksBroken++;
                }
                break;
            case CACTUS:
                if (event.getBlock().equals(Blocks.cactus)) {
                    blocksBroken++;
                }
                break;
            case COCOA_BEANS:
                if (event.getBlock().equals(Blocks.cocoa)) {
                    blocksBroken++;
                }
                break;
            case MUSHROOM:
                if (event.getBlock().equals(Blocks.red_mushroom) ||
                        event.getBlock().equals(Blocks.brown_mushroom)) {
                    blocksBroken++;
                }
                break;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onReceivedPacket(ReceivePacketEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!MacroHandler.getInstance().isCurrentMacroEnabled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (mc.currentScreen != null) return;

        if (event.packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;

            int slotNumber = packet.func_149173_d();
            if (slotNumber < 0 || slotNumber > 44) return;
            Slot currentSlot = mc.thePlayer.inventoryContainer.getSlot(slotNumber);
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            ItemStack newItem = packet.func_149174_e();
            if (FarmHelperConfig.profitCalculatorCultivatingEnchant && newItem != null && heldItem != null && StringUtils.stripControlCodes(newItem.getDisplayName()).equals(MacroHandler.getInstance().getCrop().getLocalizedName()) && GameStateHandler.getInstance().getCurrentCultivating().getOrDefault(heldItem.getDisplayName(), 0L) > 0) {
                return;
            }
            ItemStack oldItem = currentSlot.getStack();
            if (newItem == null || newItem.getItem() instanceof ItemTool || newItem.getItem() instanceof ItemArmor || newItem.getItem() instanceof ItemHoe)
                return;

            if (oldItem == null || !oldItem.getItem().equals(newItem.getItem())) {
                int newStackSize = newItem.stackSize;
                String name = StringUtils.stripControlCodes(newItem.getDisplayName());
                addDroppedItem(name, newStackSize);
            } else if (oldItem.getItem().equals(newItem.getItem())) {
                int newStackSize = newItem.stackSize;
                int oldStackSize = oldItem.stackSize;
                String name = StringUtils.stripControlCodes(newItem.getDisplayName());
                int amount = Math.max((newStackSize - oldStackSize), 0);
                addDroppedItem(name, amount);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onReceivedChat(ClientChatReceivedEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (event.type != 0) return;

        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("Sold")) return;
        if (message.contains(":")) return;
        if (message.contains("[Bazaar]")) return;
        if (message.contains("[Auction]")) return;
        if (message.contains("coins")) return;

        Optional<String> optional = rngToCountList.stream().filter(message::contains).findFirst();
        if (optional.isPresent()) {
            String name = optional.get();
            addRngDrop(name);
            return;
        }

        if (message.contains("Dicer dropped")) {
            String itemDropped;
            int amountDropped;
            Matcher matcher = regex.matcher(message);
            if (matcher.find()) {
                amountDropped = Integer.parseInt(matcher.group(1));
                if (matcher.group(2).contains("Melon")) {
                    itemDropped = "Melon";
                } else if (matcher.group(2).contains("Pumpkin")) {
                    itemDropped = "Pumpkin";
                } else {
                    return;
                }
            } else {
                return;
            }
            amountDropped *= 160;
            if (matcher.group(2).contains("Block") || matcher.group(2).contains("Polished")) {
                amountDropped *= 160;
            }
            addDroppedItem(itemDropped, amountDropped);
        }
    }

    private void addDroppedItem(String name, int amount) {
        if (cropsToCountList.contains(name)) {
            cropsToCount.stream().filter(crop -> crop.localizedName.equals(name)).forEach(crop -> crop.currentAmount += amount);
        }
    }

    private void addRngDrop(String name) {
        rngDropToCount.stream().filter(drop -> drop.localizedName.equals(name)).forEach(drop -> drop.currentAmount += 1);
    }

    @SubscribeEvent
    public void onTickUpdateBazaarPrices(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (updateBazaarClock.passed()) {
            updateBazaarClock.schedule(1000 * 60 * 5);
            LogUtils.sendDebug("Updating bazaar prices...");
            Multithreading.schedule(this::fetchBazaarPrices, 0, TimeUnit.MILLISECONDS);
        }
    }

    public void fetchBazaarPrices() {
        try {
            String url = "https://api.hypixel.net/skyblock/bazaar";
            String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36";
            JsonObject request = APIUtils.readJsonFromUrl(url, "User-Agent", userAgent);
            if (request == null) {
                LogUtils.sendDebug("Failed to update bazaar prices!");
                cantConnectToApi = true;
                return;
            }
            JsonObject json = request.getAsJsonObject();
            JsonObject json1 = json.getAsJsonObject("products");

            getPrices(json1, cropsToCount);

            getPrices(json1, rngDropToCount);

            LogUtils.sendDebug("Bazaar prices updated.");
            cantConnectToApi = false;

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.sendDebug("Failed to update bazaar prices!");
            cantConnectToApi = true;
        }
    }

    private void getPrices(JsonObject json1, List<BazaarItem> cropsToCount) {
        getPricesPerList(json1, cropsToCount);
    }

    private void getPricesPerList(JsonObject bazaarData, List<BazaarItem> itemList) {
        for (BazaarItem item : itemList) {
            JsonObject itemData = bazaarData.getAsJsonObject(item.bazaarId);
            JsonArray sellSummary = itemData.getAsJsonArray("sell_summary");

            JsonObject summaryData = (sellSummary.size() > 1) ? sellSummary.get(1).getAsJsonObject() :
                    (sellSummary.size() > 0) ? sellSummary.get(0).getAsJsonObject() : null;

            if (summaryData != null) {
                double buyPrice = summaryData.get("pricePerUnit").getAsDouble();
                bazaarPrices.computeIfPresent(item.localizedName, (name, apiCrop) -> {
                    apiCrop.currentPrice = buyPrice;
                    return apiCrop;
                });
                bazaarPrices.putIfAbsent(item.localizedName, new APICrop(item.localizedName, buyPrice));
            }
        }
    }

    public static class BazaarItem {
        public String localizedName;
        public String bazaarId;
        public int amountToEnchanted;
        public float currentAmount;
        public String imageURL;
        public int npcPrice = 0;
        public boolean dontCount = false;

        public BazaarItem(String localizedName, String bazaarId, int amountToEnchanted, int npcPrice) {
            this.localizedName = localizedName;
            this.bazaarId = bazaarId;
            this.amountToEnchanted = amountToEnchanted;
            this.npcPrice = npcPrice;
            this.currentAmount = 0;
        }

        public BazaarItem(String localizedName, String bazaarId, int npcPrice) {
            this.localizedName = localizedName;
            this.bazaarId = bazaarId;
            this.dontCount = true;
            this.npcPrice = npcPrice;
        }

        public BazaarItem setImage() {
            this.imageURL = "/farmhelper/textures/gui/" + getImageName(localizedName);
            return this;
        }
    }

    public static class APICrop {
        public double currentPrice = 0;
        public String name;

        public APICrop(String name, double currentPrice) {
            this.name = name;
            this.currentPrice = currentPrice;
        }
    }
}
