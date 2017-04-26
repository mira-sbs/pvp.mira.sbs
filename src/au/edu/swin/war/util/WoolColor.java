package au.edu.swin.war.util;

import org.bukkit.ChatColor;

/**
 * An enumerated type to store wool color data.
 * For each possible wool coolor, this enum stores:
 * The byte associated with the wool and,
 * the chatcolor associated with the wool.
 */
public enum WoolColor {
    WHITE((byte) 0, ChatColor.WHITE),
    ORANGE((byte) 1, ChatColor.GOLD),
    MAGENTA((byte) 2, ChatColor.LIGHT_PURPLE),
    LIGHT_BLUE((byte) 3, ChatColor.BLUE),
    YELLOW((byte) 4, ChatColor.YELLOW),
    LIME((byte) 5, ChatColor.GREEN),
    PINK((byte) 6, ChatColor.LIGHT_PURPLE),
    GREY((byte) 7, ChatColor.DARK_GRAY),
    LIGHT_GREY((byte) 8, ChatColor.GRAY),
    CYAN((byte) 9, ChatColor.BLUE),
    PURPLE((byte) 10, ChatColor.DARK_PURPLE),
    BLUE((byte) 11, ChatColor.DARK_BLUE),
    BROWN((byte) 12, ChatColor.GOLD),
    GREEN((byte) 13, ChatColor.DARK_GREEN),
    RED((byte) 14, ChatColor.RED),
    BLACK((byte) 15, ChatColor.BLACK);

    private final byte color;
    private final ChatColor chatColor;

    WoolColor(byte color, ChatColor chatColor) {
        this.color = color;
        this.chatColor = chatColor;
    }

    /**
     * Gets the associated WoolColor from a ChatColor.
     * This should be used to color stuff in team color.
     *
     * @param color The associated color.
     * @return The resulting WoolColor.
     */
    public static WoolColor fromChatColor(ChatColor color) {
        for (WoolColor value : values())
            if (value.getChatColor().equals(color))
                return value;
        return WHITE;
    }

    public byte getColor() {
        return color;
    }

    private ChatColor getChatColor() {
        return chatColor;
    }
}
