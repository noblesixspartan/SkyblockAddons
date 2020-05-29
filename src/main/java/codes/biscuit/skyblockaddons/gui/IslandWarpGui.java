package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonToggleNew;
import codes.biscuit.skyblockaddons.gui.buttons.IslandButton;
import codes.biscuit.skyblockaddons.gui.buttons.IslandMarkerButton;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.Feature;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

public class IslandWarpGui extends GuiScreen {

    @Getter @Setter private static Marker doubleWarpMarker;

    private static int TOTAL_WIDTH;
    private static int TOTAL_HEIGHT;

    public static float SHIFT_LEFT;
    public static float SHIFT_TOP;

    @Getter private Map<IslandWarpGui.Marker, IslandWarpGui.UnlockedStatus> markers;

    private Marker selectedMarker;
    private boolean guiIsActualWarpMenu = false;
    private boolean foundAdvancedWarpToggle = false;

    public IslandWarpGui() {
        super();

        Map<Marker, UnlockedStatus> markers = new EnumMap<>(Marker.class);
        for (Marker marker : Marker.values()) {
            markers.put(marker, UnlockedStatus.UNLOCKED);
        }
        this.markers = markers;
    }

    public IslandWarpGui(Map<Marker, UnlockedStatus> markers) {
        super();

        this.markers = markers;
        this.guiIsActualWarpMenu = true;
    }

    @Override
    public void initGui() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        Map<Island, UnlockedStatus> islands = new EnumMap<>(Island.class);

        for (Map.Entry<Marker, UnlockedStatus> marker : markers.entrySet()) {
            Island island = marker.getKey().getIsland();
            UnlockedStatus currentStatus = islands.get(island);
            UnlockedStatus newStatus = marker.getValue();

            if (currentStatus == null || newStatus.ordinal() > currentStatus.ordinal()) {
                islands.put(island, newStatus);
            }
        }

        for (Map.Entry<Island, UnlockedStatus> island : islands.entrySet()) {
            this.buttonList.add(new IslandButton(island.getKey(), island.getValue(), markers));
        }

        int screenWidth = mc.displayWidth;
        int screenHeight = mc.displayHeight;
        float scale = 0.7F;
        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;
        SHIFT_LEFT = (screenWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (screenHeight/2F-totalHeight/2F)/scale;


        int x = Math.round(screenWidth/0.7F-SHIFT_LEFT-475);
        int y = Math.round(screenHeight/0.7F-SHIFT_TOP);

        if (guiIsActualWarpMenu) {
            this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60 * 3, 50,
                    () -> {
                        // Finds the advanced mode toggle button to see if it's enabled or not.
                        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
                        if (guiScreen instanceof GuiChest) {
                            GuiChest gui = (GuiChest)guiScreen;

                            Slot toggleAdvancedModeSlot = gui.inventorySlots.getSlot(51);
                            if (toggleAdvancedModeSlot != null && toggleAdvancedModeSlot.getHasStack()) {
                                ItemStack toggleAdvancedModeItem = toggleAdvancedModeSlot.getStack();

                                if (Items.dye.equals(toggleAdvancedModeItem.getItem())) {
                                    int damage = toggleAdvancedModeItem.getItemDamage();
                                    if (damage == 10) { // Lime Dye
                                        foundAdvancedWarpToggle = true;

                                        return true;
                                    } else if (damage == 8) { // Grey Dye
                                        foundAdvancedWarpToggle = true;

                                        return false;
                                    }
                                }
                            }
                        }
                        return false;
                    },
                    () -> {
                        if (!this.foundAdvancedWarpToggle) return;

                        // This will click the advanced mode button for you.
                        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
                        if (guiScreen instanceof GuiChest) {
                            GuiChest gui = (GuiChest) guiScreen;
                            try {
                                Method handleMouseClick = GuiContainer.class.getDeclaredMethod(SkyblockAddonsTransformer.isDeobfuscated() ? "handleMouseClick" :
                                                SkyblockAddonsTransformer.isUsingNotchMappings() ? "a" : "func_146984_a", Slot.class, int.class, int.class, int.class);
                                handleMouseClick.setAccessible(true);
                                handleMouseClick.invoke(gui, gui.inventorySlots.getSlot(51), 51, 0, 0);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  ex) {
                                ex.printStackTrace();
                            }
                        }
                    }));
            this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60 * 2, 50,
                    () -> main.getConfigValues().isEnabled(Feature.FANCY_WARP_MENU),
                    () -> {
                        if (main.getConfigValues().getDisabledFeatures().contains(Feature.FANCY_WARP_MENU)) {
                            main.getConfigValues().getDisabledFeatures().remove(Feature.FANCY_WARP_MENU);
                        } else {
                            main.getConfigValues().getDisabledFeatures().add(Feature.FANCY_WARP_MENU);
                        }
                    }));
        }
        this.buttonList.add(new ButtonToggleNew(x, y - 30 - 60, 50,
                () -> main.getConfigValues().isEnabled(Feature.DOUBLE_WARP),
                () -> {
                    if (main.getConfigValues().getDisabledFeatures().contains(Feature.DOUBLE_WARP)) {
                        main.getConfigValues().getDisabledFeatures().remove(Feature.DOUBLE_WARP);
                    } else {
                        main.getConfigValues().getDisabledFeatures().add(Feature.DOUBLE_WARP);
                    }
                }));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(mc.fontRendererObj, "Click a warp point to travel there!", width/2, 10, 0xFFFFFFFF);
        drawCenteredString(mc.fontRendererObj, "Must have the specific scroll unlocked.", width/2, 20, 0xFFFFFFFF);

        int guiScale = new ScaledResolution(mc).getScaleFactor();

        GlStateManager.pushMatrix();
        float scale = 0.7F;
        GlStateManager.scale(1F/guiScale, 1F/guiScale, 1);
        GlStateManager.scale(scale, scale, 1);

        float totalWidth = TOTAL_WIDTH*scale;
        float totalHeight = TOTAL_HEIGHT*scale;

        SHIFT_LEFT = (mc.displayWidth/2F-totalWidth/2F)/scale;
        SHIFT_TOP = (mc.displayHeight/2F-totalHeight/2F)/scale;
        GlStateManager.translate(SHIFT_LEFT, SHIFT_TOP, 0);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        IslandButton lastHoveredButton = null;

        for (GuiButton button : buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton)button;

                // Call this just so it calculates the hover, don't actually draw.
                islandButton.drawButton(mc, mouseX, mouseY, false);

                if (islandButton.isHovering()) {
                    if (lastHoveredButton != null) {
                        lastHoveredButton.setDisableHover(true);
                    }
                    lastHoveredButton = islandButton;
                }
            }
        }

        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY);
        }


        int x = Math.round(mc.displayWidth/0.7F-SHIFT_LEFT-500);
        int y = Math.round(mc.displayHeight/0.7F-SHIFT_TOP);
        GlStateManager.pushMatrix();
        float textScale = 3F;
        GlStateManager.scale(textScale, textScale, 1);
        if (guiIsActualWarpMenu) {
            mc.fontRendererObj.drawStringWithShadow(Feature.WARP_ADVANCED_MODE.getMessage(), x / textScale + 50, (y - 30 - 60 * 3) / textScale + 5, 0xFFFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(Feature.FANCY_WARP_MENU.getMessage(), x / textScale + 50, (y - 30 - 60 * 2) / textScale + 5, 0xFFFFFFFF);
        }
        mc.fontRendererObj.drawStringWithShadow(Feature.DOUBLE_WARP.getMessage(), x / textScale + 50, (y - 30 - 60) / textScale + 5, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        detectClosestMarker();
    }

    @Getter
    public enum Island {
        THE_END("The End", 100, 30, 573, 506),
        BLAZING_FORTRESS("Blazing Fortress", 809, 0, 644, 580),
        THE_PARK("The Park", 113, 380, 378, 409),
        SPIDERS_DEN("Spider's Den", 500, 361, 533, 412),
        DEEP_CAVERNS("Deep Caverns", 1406, 334, 323, 416),
        GOLD_MINE("Gold Mine", 1080, 606, 279, 297),
        THE_BARN("The Barn", 1223, 782, 274, 263),
        HUB("Hub", 215, 724, 937, 585),
        MUSHROOM_DESERT("Mushroom Desert", 1503, 778, 252, 205),
        PRIVATE_ISLAND("Private Island", 216, 1122, 105, 125)
        ;

        public static final float SCALE_FACTOR = 0.75F;

        private String label;
        private int x;
        private int y;
        private int w;
        private int h;

        private ResourceLocation resourceLocation;
        private BufferedImage bufferedImage;

        Island(String label, int x, int y, int w, int h) {
            this.label = label;
            this.x = x;
            this.y = y;
//            this.w = w;
//            this.h = h;

            this.resourceLocation = new ResourceLocation("skyblockaddons", "islands/"+this.name().toLowerCase().replace("_", "")+".png");
            try {
                bufferedImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(this.resourceLocation).getInputStream());
                this.w = bufferedImage.getWidth();
                this.h = bufferedImage.getHeight();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            this.w /= SCALE_FACTOR;
            this.h /= SCALE_FACTOR;

            if (y+h > TOTAL_HEIGHT) {
                TOTAL_HEIGHT = y+h;
            }
            if (x+w > TOTAL_WIDTH) {
                TOTAL_WIDTH = x+w;
            }
        }
    }


    public void detectClosestMarker() {
        int mouseX = Mouse.getX();
        int mouseY = Minecraft.getMinecraft().displayHeight-Mouse.getY();
        float islandGuiScale = 0.7F;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        IslandWarpGui.Marker hoveredMarker = null;
        double markerDistance = IslandMarkerButton.MAX_SELECT_RADIUS+1;

        for (GuiButton button : this.buttonList) {
            if (button instanceof IslandButton) {
                IslandButton islandButton = (IslandButton)button;

                for (IslandMarkerButton marker : islandButton.getMarkerButtons()) {
                    double distance = marker.getDistance(mouseX, mouseY);

                    if (distance != -1 && distance < markerDistance) {
                        hoveredMarker = marker.getMarker();
                        markerDistance = distance;
                    }
                }
            }
        }

        selectedMarker = hoveredMarker;

//        if (hoveredMarker != null) System.out.println(hoveredMarker.getLabel()+" "+markerDistance);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && selectedMarker != null) {
            Minecraft.getMinecraft().displayGuiScreen(null);

            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.DOUBLE_WARP)) {
                doubleWarpMarker = selectedMarker;
            }
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp "+selectedMarker.getWarpName());
        }

        int minecraftScale = Minecraft.getMinecraft().gameSettings.guiScale;
        float islandGuiScale = 0.7F;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Getter
    public enum Marker {
        PRIVATE_ISLAND("home", "Home", Island.PRIVATE_ISLAND, true, 72, 90),

        HUB("hub", "Spawn", Island.HUB, true, 653, 217),
        CASTLE("castle", "Castle", Island.HUB, 195, 153),
        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 452, 457),
        CRYPT("crypt", "Crypts", Island.HUB, 585, 84),

        SPIDERS_DEN("spider", "Spawn", Island.SPIDERS_DEN, true, 308, 284),
        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 291, 40),

        THE_PARK("park", "Spawn", Island.THE_PARK, true, 263, 308),
        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 254, 202),
        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 194, 82),

        THE_END("end", "Spawn", Island.THE_END, true, 440, 291),
        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 248),

        BLAZING_FORTRESS("nether", "Spawn", Island.BLAZING_FORTRESS, true, 115, 413),
        BLAZING_FORTRESS_MAGMA("magma", "Magma Cube Arena", Island.BLAZING_FORTRESS, 359, 284),

        THE_BARN("barn", "Spawn", Island.THE_BARN, true, 40, 163),
        MUSHROOM_DESERT("desert", "Spawn", Island.MUSHROOM_DESERT, true, 51, 73),

        GOLD_MINE("gold", "Spawn", Island.GOLD_MINE, true, 106, 159),
        DEEP_CAVERNS("deep", "Spawn", Island.DEEP_CAVERNS, true, 97, 213),
        ;

        private String warpName;
        private String label;
        private Island island;
        private boolean advanced;
        private int x;
        private int y;

        Marker(String warpName, String label, Island island, int x, int y) {
            this(warpName, label, island, false, x, y);
        }

        Marker(String warpName, String label, Island island, boolean advanced, int x, int y) {
            this.warpName = warpName;
            this.label = label;
            this.island = island;
            this.x = x;
            this.y = y;
            this.advanced = advanced;
        }

        public static Marker fromWarpName(String warpName) {
            for (Marker marker : values()) {
                if (marker.warpName.equals(warpName)) {
                    return marker;
                }
            }

            return null;
        }
    }

    public enum UnlockedStatus {
        UNKNOWN("Haven't Visited"),
        NOT_UNLOCKED("Not Unlocked"),
        IN_COMBAT("In Combat..."),
        UNLOCKED(null),
        ;

        private String message;

        UnlockedStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
