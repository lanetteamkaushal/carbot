package net.cardroid.car;

import junit.framework.TestCase;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class PatternMatcherTest extends TestCase {
    public void testValuePattern() {
        PatternMatcher.ValuePattern pattern = new PatternMatcher.ValuePattern(5, "AB", "CD", 1, 11);
        assertEquals(1, pattern.getMatch("AB1CD").getValue());
        assertEquals(11, pattern.getMatch("ABBCD").getValue());
        assertEquals(6, pattern.getMatch("AB6CD").getValue());

        assertEquals(0, pattern.getMatch("AB1CD").getPrecent());
        assertEquals(100, pattern.getMatch("ABBCD").getPrecent());
        assertEquals(50, pattern.getMatch("AB6CD").getPrecent());

        assertEquals("AB1CD", pattern.getData(1));
        assertEquals("ABBCD", pattern.getData(11));
        assertEquals("AB6CD", pattern.getData(6));

        assertEquals("AB1CD", pattern.getDataForPercent(0));
        assertEquals("ABBCD", pattern.getDataForPercent(100));
        assertEquals("AB6CD", pattern.getDataForPercent(50));
    }

    public void testSingleValuePattern() {
        PatternMatcher.ValuePattern pattern = new PatternMatcher.ValuePattern(5, "ABCDE", "", 5, 5);
        assertEquals(5, pattern.getMatch("ABCDE").getValue());
    }


    public void testFindPattern() {
        PatternMatcher<Integer> patternMatcher = new PatternMatcher<Integer>();
        patternMatcher.addPatern(new PatternMatcher.ValuePattern<Integer>(5, "AB", "CD", 1, 10, 1));
        patternMatcher.addPatern(new PatternMatcher.ValuePattern<Integer>(5, "EF", "", 1, 10, 2));
        patternMatcher.addPatern(new PatternMatcher.ValuePattern<Integer>(5, "", "DD", 1, 10, 3));

        assertEquals(1, (int)patternMatcher.findPattern("AB1CD").getTag());
        assertEquals(2, (int)patternMatcher.findPattern("EF001").getTag());
        assertEquals(3, (int)patternMatcher.findPattern("005DD").getTag());
        assertNull(patternMatcher.findPattern("123"));
    }
}