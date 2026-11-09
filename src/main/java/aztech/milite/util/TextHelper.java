package aztech.milite.util;

import aztech.milite.MIText;
import aztech.milite.MITooltips;
import aztech.milite.machines.multiblocks.HatchFlags;
import aztech.milite.machines.multiblocks.SimpleMember;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TextHelper {
    public static final Style GRAY_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);
    public static final Style WATER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0x3264ff));
    public static final Style WARNING_TEXT = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style MAX_TEMP_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xd94a1e));
    public static final Style HEAT_CONDUCTION = Style.EMPTY.withColor(TextColor.fromRgb(0x0073ba));
    public static final Style NEUTRONS = Style.EMPTY.withColor(TextColor.fromRgb(0x29a329));
    public static final Style GOLD_BOLD = Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true);
    public static final Style GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);

    public static final Style RED = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);

    public record Amount(String digit, String unit) {}

    public record MaxedAmount(String digit, String maxDigit, String unit) {}

    public static final String[] units = new String[] { "k", "M", "G", "T", "P", "E" };
    public static final long[] nums = new long[] { 1000L, 1000_000L, 1000_000_000L, 1000_000_000_000L, 1000_000_000_000_000L,
            1000_000_000_000_000_000L };

    public static String getAmount(double amount, long num) {
        double fract = amount / num;
        if (fract < 10) {
            return String.format("%.3f", fract);
        } else if (fract < 100) {
            return String.format("%.2f", fract);
        } else {
            return String.format("%.1f", fract);
        }
    }

    public static Amount getAmountGeneric(Number number) {
        if (number instanceof Long l) {
            return getAmount(l);
        } else if (number instanceof Integer i) {
            return getAmount(i);
        } else if (number instanceof Double d) {
            return getAmount(d);
        } else if (number instanceof Float f) {
            return getAmount(f);
        }
        throw new IllegalArgumentException("Number " + number + " is neither long, int, double or float");
    }

    public static Amount getAmount(double amount) {
        if (amount < 100000) {
            return new Amount(getAmount(amount, 1), "");
        } else {
            int i = 0;
            while (amount / nums[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 1000) {
            return new MaxedAmount(getAmount(amount, 1), getAmount(max, 1), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmountGeneric(Number amount, Number max) {
        if (amount instanceof Long l && max instanceof Long m) {
            return getMaxedAmount(l, m);
        } else if (amount instanceof Integer i && max instanceof Integer m) {
            return getMaxedAmount(i, m);
        } else if (amount instanceof Double d && max instanceof Double m) {
            return getMaxedAmount(d, m);
        } else if (amount instanceof Float f && max instanceof Float m) {
            return getMaxedAmount(f, m);
        }
        // Mixed number types (for example an int amount with a long capacity). Widen both sides to double
        // instead of crashing on a tooltip render.
        return getMaxedAmount(amount.doubleValue(), max.doubleValue());
    }

    public static Amount getAmount(long amount) {
        if (amount < 100000) {
            return new Amount(String.valueOf(amount), "");
        } else {
            int i = 0;
            while (amount / nums[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, nums[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(long amount, long max) {
        if (max < 100000) {
            return new MaxedAmount(String.valueOf(amount), String.valueOf(max), "");
        } else {
            int i = 0;
            while (max / nums[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, nums[i]), getAmount(max, nums[i]), units[i]);
        }
    }

    public static MutableComponent getFeTextMaxed(Number fe, Number max) {
        var amount = getMaxedAmountGeneric(fe, max);
        return MIText.FeMaxed.text(amount.digit(), amount.maxDigit(), amount.unit());
    }

    public static MutableComponent getFeText(double fe) {
        var amount = getAmount(fe);
        return MIText.Fe.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getFeTextTick(double fe) {
        var amount = getAmount(fe);
        return MIText.FeT.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getFeText(long fe) {
        var amount = getAmount(fe);
        return MIText.Fe.text(amount.digit(),
                amount.unit());
    }

    public static MutableComponent getFeTextTick(long fe) {
        var amount = getAmount(fe);
        return MIText.FeT.text(amount.digit(),
                amount.unit());
    }

    public static Component getFeTextTick(double fe, boolean style) {
        MutableComponent text = getFeTextTick(fe);
        if (style) {
            text.setStyle(TextHelper.NUMBER_TEXT);
        }
        return text;
    }

    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("#.#");

    public static MutableComponent getNotConsumedTooltip() {
        return MIText.NotConsumed.text().withStyle(GOLD);
    }

    @Nullable
    public static Component getProbabilityTooltip(float probability, boolean input) {
        if (probability == 1) {
            return null;
        } else {
            MutableComponent text;
            if (probability == 0) {
                return getNotConsumedTooltip();
            } else {
                if (input) {
                    text = MIText.ChanceConsumption.text(PROBABILITY_FORMAT.format(probability * 100));
                } else {
                    text = MIText.ChanceProduction.text(PROBABILITY_FORMAT.format(probability * 100));
                }

            }
            text.setStyle(GOLD);
            return text;
        }
    }

    public static List<Component> getMultiblockMemberTooltip(RegistryAccess registries, SimpleMember member, @Nullable HatchFlags hatch) {
        List<Component> lines = new ArrayList<>(member.getTooltipPreview(registries));
        if (hatch != null && !hatch.values().isEmpty()) {
            lines.add(MIText.AcceptsHatches.text().withStyle(MITooltips.DEFAULT_STYLE));
            for (var type : hatch.values()) {
                lines.add(Component.literal("- ").withStyle(MITooltips.DEFAULT_STYLE)
                        .append(type.description().withStyle(MITooltips.HIGHLIGHT_STYLE)));
            }
        }
        return lines;
    }
}
