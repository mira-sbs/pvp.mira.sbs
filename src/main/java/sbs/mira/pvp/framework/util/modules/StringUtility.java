package sbs.mira.pvp.framework.util.modules;

import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.MiraModule;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class handles mildly helpful string functions
 * needed for user-friendliness.
 * <p>
 * Created by Josh on 18/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see ItemStack
 * @since 1.0
 */
public class StringUtility extends MiraModule {

    private Map<PotionEffectType, String> effects = new HashMap<>();

    /**
     * String utility constructor.
     * We need to link back to the manager and plugin.
     *
     * @param main The supercontroller.
     */
    public StringUtility(MiraPulse main) {
        super(main);
        effects.put(PotionEffectType.ABSORPTION, "Absorption");
        effects.put(PotionEffectType.BLINDNESS, "Blindness");
        effects.put(PotionEffectType.CONFUSION, "Nausea");
        effects.put(PotionEffectType.DAMAGE_RESISTANCE, "Resistance");
        effects.put(PotionEffectType.FAST_DIGGING, "Haste");
        effects.put(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance");
        effects.put(PotionEffectType.HARM, "Instant Damage");
        effects.put(PotionEffectType.HEAL, "Instant Health");
        effects.put(PotionEffectType.HEALTH_BOOST, "Health Boost");
        effects.put(PotionEffectType.HUNGER, "Hunger");
        effects.put(PotionEffectType.INCREASE_DAMAGE, "Strength");
        effects.put(PotionEffectType.INVISIBILITY, "Invisibility");
        effects.put(PotionEffectType.JUMP, "Jump Boost");
        effects.put(PotionEffectType.NIGHT_VISION, "Night Vision");
        effects.put(PotionEffectType.POISON, "Poison");
        effects.put(PotionEffectType.REGENERATION, "Regeneration");
        effects.put(PotionEffectType.SATURATION, "Saturation");
        effects.put(PotionEffectType.SLOW, "Slowness");
        effects.put(PotionEffectType.SLOW_DIGGING, "Mining Fatigue");
        effects.put(PotionEffectType.SPEED, "Speed");
        effects.put(PotionEffectType.WATER_BREATHING, "Water Breathing");
        effects.put(PotionEffectType.WEAKNESS, "Weakness");
        effects.put(PotionEffectType.WITHER, "Wither");
    }

    /**
     * Turns an array of Strings into a sentence.
     * <p>
     * i.e. ['1', '2', '3', '4']
     * -> "1, 2, 3 and 4"
     *
     * @param array An array of words.
     * @return A sentence.
     */
    public String sentenceFormat(List<?> array) {
        if (array.size() == 0) return "None";
        StringBuilder format = new StringBuilder();
        if (array.size() == 1) return array.get(0).toString();
        int i = 1;
        while (i <= array.size()) {
            if (i == array.size())
                format.append(ChatColor.WHITE).append(" and ").append(array.get(i - 1).toString());
            else if (i == 1)
                format = new StringBuilder(array.get(0).toString());
            else
                format.append(", ").append(array.get(i - 1).toString());
            i++;
        }
        return format.toString();
    }

    /**
     * Performs the same as above but outputs a TextComponent result.
     *
     * @param array Array of TextComponent.
     * @return The winner format.
     */
    public TextComponent winnerFormat(List<WarTeam> array) {
        if (array.size() == 0) return new TextComponent("No One");
        TextComponent result = new TextComponent();
        if (array.size() == 1) return array.get(0).getHoverInformation();
        int i = 1;
        while (i <= array.size()) {
            if (i == array.size()) {
                result.addExtra(ChatColor.WHITE + " and ");
                result.addExtra(array.get(i - 1).getHoverInformation());
            } else if (i == 1)
                result = new TextComponent(array.get(0).getHoverInformation());
            else {
                result.addExtra(ChatColor.WHITE + ", ");
                result.addExtra(array.get(i - 1).getHoverInformation());
            }
            i++;
        }
        return result;
    }

    /**
     * Generates a 5-digit number.
     * While not intrinsically a String, it is treated
     * as one when manipulating the map world.
     *
     * @return A 5-digit number.
     */
    public long generateID() {
        long generated = new Random().nextInt(90000) + 10000;
        // Number between 11111-99999;

        if (mira().match().getRawRoundID() == generated) return generateID();
        // Though a very small chance, regenerate if it is the same as the previous round.
        return generated;
    }

    /**
     * Converts an integer into a digital clock formatted string.
     *
     * @param i The integer to convert, in number of seconds.
     * @return The converted string in a MM:SS format.
     */
    public String getDigitalTime(int i) {
        if (i < 0) i = -i;
        int remainder = i % 3600, minutes = remainder / 60, seconds = remainder % 60;
        String time = "";
        if (minutes > 0) {
            if (minutes < 10) time += "0";
            time += minutes + ":";
        } else {
            time += "00:";
        }
        if (seconds > 0) {
            if (seconds < 10) time += "0";
            time += seconds;
        } else {
            time += "00";
        }
        return time;
    }

    /**
     * Is it second, or seconds?
     * Is it amount, or amounts?
     * <p>
     * i.e. 1 seconds -> 1 second,
     * 2 seconds -> 2 seconds.
     *
     * @param amount The amount.
     * @return The plural.
     */
    public String plural(int amount) {
        return amount == 1 ? " " : "s ";
    }

    /**
     * Turns ChatColor into dye Color.
     * This may not be 100% accurate but it was as close as I could get.
     *
     * @param color The ChatColor to convert.
     * @return The matching Color.
     */
    public Color convertChatToDye(ChatColor color) {
        switch (color) {
            case AQUA:
                return Color.AQUA;
            case BLACK:
                return Color.BLACK;
            case BLUE:
                return Color.BLUE;
            case DARK_AQUA:
                return Color.TEAL;
            case DARK_BLUE:
                return Color.NAVY;
            case DARK_GRAY:
                return Color.GRAY;
            case DARK_GREEN:
                return Color.GREEN;
            case DARK_PURPLE:
                return Color.PURPLE;
            case DARK_RED:
                return Color.MAROON;
            case GOLD:
                return Color.ORANGE;
            case GRAY:
                return Color.GRAY;
            case GREEN:
                return Color.LIME;
            case LIGHT_PURPLE:
                return Color.FUCHSIA;
            case RED:
                return Color.RED;
            case YELLOW:
                return Color.YELLOW;
            case WHITE:
                return Color.WHITE;
            default:
                return Color.WHITE;
        }
    }

    /**
     * Formats a potion effect into what it would normally be.
     *
     * @param effect Effect to pretty.
     * @return Normal-looking potion effect name.
     */
    String potionEffect(PotionEffect effect) {
        return effects.get(effect.getType()) + " " + quickNumerals(effect.getAmplifier() + 1);
    }

    /**
     * Returns the numerals of 1 to 10 quickly.
     *
     * @param number The number to convert.
     * @return The conversion.
     */
    private String quickNumerals(int number) {
        switch (number) {
            case 10:
                return "X";
            case 9:
                return "IX";
            case 8:
                return "VIII";
            case 7:
                return "VII";
            case 6:
                return "VI";
            case 5:
                return "V";
            case 4:
                return "IV";
            case 3:
                return "III";
            case 2:
                return "II";
            case 1:
                return "II";
            default:
                return "X+";
        }
    }
}
