package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.function.Supplier;

public enum DiscordStatus implements ButtonSelect.SelectItem {


    NONE(Message.DISCORD_STATUS_NONE_TITLE, Message.DISCORD_STATUS_NONE_DESCRIPTION, () -> null),
    LOCATION(Message.DISCORD_STATUS_LOCATION_TITLE, Message.DISCORD_STATUS_LOCATION_DESCRIPTION,
            () -> SkyblockAddons.getInstance().getUtils().getLocation().getScoreboardName()),

    PURSE(Message.DISCORD_STATUS_PURSE_TITLE, Message.DISCORD_STATUS_PURSE_DESCRIPTION,
            () -> String.format("%s Coins", TextUtils.formatDouble(SkyblockAddons.getInstance().getUtils().getPurse()))),

    STATS(Message.DISCORD_STATUS_STATS_TITLE, Message.DISCORD_STATUS_STATS_DESCRIPTION,
            () -> {
                int health = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.HEALTH).getValue();
                int defense = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.DEFENCE).getValue();
                int mana = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.MANA).getValue();
//                return String.format("%d\u2764 %d\u2748 %d\u270E", health, defense, mana);
                return String.format("%d H - %d D - %d M", health, defense, mana);
            }),

    ZEALOTS(Message.DISCORD_STATUS_ZEALOTS_TITLE, Message.DISCORD_STATUS_ZEALOTS_DESCRIPTION,
            () -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValues().getKills())),

    ITEM(Message.DISCORD_STATUS_ITEM_TITLE, Message.DISCORD_STATUS_ITEM_DESCRIPTION,
            () -> {
                final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if(player != null && player.getHeldItem() != null) {
                    return String.format("Holding %s", TextUtils.stripColor(player.getHeldItem().getDisplayName()));
                }
                return "No item in hand";
            }),

    TIME(Message.DISCORD_STATUS_TIME_TITLE, Message.DISCORD_STATUS_TIME_DESCRIPTION,
            () -> {
                final SkyblockDate date = SkyblockAddons.getInstance().getUtils().getCurrentDate();
                return date != null ? date.toString() : "";
            }),

    PROFILE(Message.DISCORD_STATUS_PROFILE_TITLE, Message.DISCORD_STATUS_PROFILE_DESCRIPTION,
            () -> {
                String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
                return String.format("Profile: %s", profile == null ? "None" : profile);
            }),

    CUSTOM(Message.DISCORD_STATUS_CUSTOM, Message.DISCORD_STATUS_CUSTOM_DESCRIPTION, () -> ""),

    AUTO_STATUS(Message.DISCORD_STATUS_AUTO, Message.DISCORD_STATUS_AUTO_DESCRIPTION, () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                Location location = main.getUtils().getLocation();

                if (location == Location.THE_END || location == Location.DRAGONS_NEST) {
                    return DiscordStatus.ZEALOTS.displayMessageSupplier.get();
                }

                if ("AUTO_STATUS".equals(main.getConfigValues().getDiscordAutoDefault().name())) { // Avoid self reference.
                    main.getConfigValues().setDiscordAutoDefault(DiscordStatus.NONE);
                }
                return main.getConfigValues().getDiscordAutoDefault().displayMessageSupplier.get();
            })
    ;

    private final Message title;
    private final Message description;
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(Message title, Message description, Supplier<String> displayMessageSupplier) {
        this.title = title;
        this.description = description;
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString() {
        return displayMessageSupplier.get();
    }

    @Override
    public String getName() {
        return title.getMessage();
    }

    @Override
    public String getDescription() {
        return description.getMessage();
    }
}