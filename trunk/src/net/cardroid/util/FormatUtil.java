package net.cardroid.util;

import java.math.BigInteger;

/**
 * Date: Apr 22, 2010
 * Time: 1:52:21 AM
 *
 * @author Lex Nikitin
 */
public class FormatUtil {
    public static String formatAsCanData(String value, int nDigits) {
        return "000000000000000000000000000000".substring(0, nDigits - value.length()) + value;
    }

    public static String formatAsCanData(BigInteger data, int length) {
        String value = data.toString(16).toUpperCase();
        value = formatAsCanData(value, length);
        return value;
    }

    public static String formatAsCanData(int data, int length) {
        String value = Integer.toHexString(data).toUpperCase();
        value = formatAsCanData(value, length);
        return value;
    }
}
