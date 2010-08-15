package net.cardroid.can;

import junit.framework.TestCase;

import java.text.ParseException;

/**
 * Date: Apr 10, 2010
 * Time: 7:45:17 PM
 *
 * @author Lex Nikitin
 */
public class CanMessageParserTest extends TestCase {
    public void testParse() throws ParseException {
        assertSuccesful("t10021133", "100", "1133");
        assertSuccesful("t1EE4404040FF", "1EE", "404040FF");
        assertSuccesful("t1EE0", "1EE", "");
        assertSuccesful("t1EEB1122334455667788990011", "1EE", "1122334455667788990011");
    }

    private void assertSuccesful(String message, String destination, String data) throws ParseException {
        CanMessage canMessage = CanMessageParser.parseMesage(message, 0);
        assertEquals(Integer.parseInt(destination, 16), canMessage.getDestination());
        assertEquals(data, canMessage.getData());
        assertEquals(message, canMessage.toString());
    }
}
