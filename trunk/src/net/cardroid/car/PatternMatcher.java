package net.cardroid.car;

import android.util.Log;
import com.google.common.collect.Lists;
import net.cardroid.util.FormatUtil;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class PatternMatcher<T>  {
    private static String TAG = "PatternMatcher";
    private final List<ValuePattern<T>> mPatterns = Lists.newArrayList();

    public interface ValuePatternProvider<T> {
        ValuePattern<T> get();
    }

    public static<T> PatternMatcher<T> fromProviders(ValuePatternProvider<T> [] providers) {
        PatternMatcher<T> patternMatcher = new PatternMatcher<T>();
        for (ValuePatternProvider<T> provider : providers) {
            patternMatcher.addPatern(provider.get());
        }
        return patternMatcher;
    }

    public void addPatern(ValuePattern<T> pattern) {
        mPatterns.add(pattern);
    }

    public Match<T> findPattern(String data) {
        for (ValuePattern<T> valuePattern : mPatterns) {
            Match<T> match = valuePattern.getMatch(data);
            if (match != null) return match;
        }

        return null;
    }

    public static interface Match<T> {
        public int getPrecent();
        public int getValue();
        public T getTag();
    }

    public static class ValuePattern<T> {
        private final int length;
        private final String prefix;
        private final String suffix;

        private final int from;
        private final int to;

        private T tag;

        public ValuePattern(int nChars, String prefix, String suffix, int from, int to) {
            this.length = nChars;
            this.prefix = prefix;
            this.suffix = suffix;
            this.from = from;
            this.to = to;
            checkArgument(nChars >= prefix.length() + suffix.length());
            checkArgument(from >= 0);
            checkArgument(to >= 0);
            if (nChars == prefix.length() + suffix.length()) {
                checkArgument(nChars != prefix.length() + suffix.length() || suffix.length() == 0 && from == to,
                    "for single value patterns use prefix only (no suffix) and same value for 'to' and 'from'");
            } else {
                checkArgument(from < to);
            }
        }

        public ValuePattern(int nChars, String prefix, String suffix, int from, int to, /*@Nullable*/ T tag) {
            this.length = nChars;
            this.prefix = prefix;
            this.suffix = suffix;
            this.from = from;
            this.to = to;
            this.tag = tag;
            checkArgument(nChars >= prefix.length() + suffix.length());
            checkArgument(from < to);
            checkArgument(from >= 0);
            checkArgument(to >= 0);
        }

        public T getTag() {
            return tag;
        }

        public void setTag(T tag) {
            this.tag = tag;
        }

        public Match<T> getMatch(String data) {
            if (data.length() == length && data.startsWith(prefix) && data.endsWith(suffix)) {
                final int value;
                if (prefix.length() + suffix.length() == length) {
                    value = from;
                } else {
                    value = Integer.parseInt(data.substring(prefix.length(), length - suffix.length()), 16);
                }
                if (from <= value && value <= to) {
                    Log.e(TAG, "Match out of range " + value + " from " + from + " to " + to);                    
                }

                return new Match<T>() {
                    @Override public int getPrecent() {
                        return (value - from) * 100 / (to - from);
                    }

                    @Override public int getValue() {
                        return value;
                    }

                    @Override public T getTag() {
                        return tag;
                    }
                };
            } else {
                return null;
            }
        }

        private int valueLength() {
            return length - prefix.length() - suffix.length();
        }

        public String getData(int value) {
            return prefix + FormatUtil.formatAsCanData(value, valueLength()) + suffix;
        }

        public String getDataForPercent(int percent) {
            int value = from + (to - from) * percent / 100;
            return prefix + FormatUtil.formatAsCanData(value, valueLength()) + suffix;
        }
    }
}