package com.bonsai.pixelpets.util.describers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ObjectDescriber<T> {
    Component describe(T value);

    static String formatDuration(float ticks) {
        if (ticks < 0) {
            // Shouldn't reach here
            return ticks + " ticks";
        }

        String seconds = BigDecimal.valueOf(ticks)
                .divide(BigDecimal.valueOf(20), RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();

        return seconds + (ticks == 20 ? " second" : " seconds");
    }

    static String formatNumber(float value) {
        String text = Float.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    static String formatNumber(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    static String formatSignedNumber(double value) {
        String formatted = formatNumber(Math.abs(value));
        return value < 0 ? "-" + formatted : "+" + formatted;
    }

    static String intToRomanLookup(int num) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds  = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens      = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones      = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return  thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static MutableComponent capitalizeFirst(Component component) {
        if (!(component.getContents() instanceof PlainTextContents.LiteralContents(String text))) {
            return component.copy();
        }

        MutableComponent result = Component.literal(capitalize(text)).withStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            result.append(sibling.copy());
        }

        return result;
    }

    static MutableComponent joinClauses(List<Component> clauses) {
        MutableComponent result = Component.empty();
        int len = clauses.size();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                result.append(Component.literal(i == len - 1 ? (len > 2 ? ", and " : " and ") : ", "));
            }
            result.append(clauses.get(i));
        }
        return result;
    }

    static MutableComponent numberChange(String property, float previous, float current, Function<Float, String> formatter, String suffix) {
        String direction = current > previous ? "increases" : "decreases";
        return Component.literal(property + " " + direction + " to " + formatter.apply(current) + suffix);
    }
}