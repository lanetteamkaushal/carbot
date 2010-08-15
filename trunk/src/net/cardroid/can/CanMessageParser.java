package net.cardroid.can;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * Date: Apr 10, 2010
 * Time: 7:18:13 PM
 *
 * @author Lex Nikitin
 */
public class CanMessageParser {
    public static CanMessage parseMesage (String message, long timestampMillis) throws java.text.ParseException {
        return parseMesage(message.getBytes(), 0, message.length(), timestampMillis);
    }

    static CanMessage parseMesage(byte [] readBuf, int offset, int len, long timestampMillis) throws java.text.ParseException {
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(readBuf, offset, len));

			byte ch = dis.readByte();

			if (ch != 't') {
				throw new java.text.ParseException("Invalid CAN message " + new String(readBuf), 1);
			}

			byte[] destination = new byte[3];
			dis.read(destination);

			int dataLengthChar = dis.readByte();
            int dataLength;

            if (dataLengthChar >= '0' && dataLengthChar <= '9') {
                dataLength = dataLengthChar - '0';
            } else if (dataLengthChar >= 'A' && dataLengthChar <= 'F') {
                dataLength = dataLengthChar - 'A' + 10;
            } else {
				throw new ParseException("Invalid CAN message " + new String(readBuf), 4);
			}

			byte [] data = new byte[dataLength * 2];
			dis.read(data);

			if (dis.read() != -1) {
				throw new ParseException("Invalid CAN message " + new String(readBuf), 0);
			}

			return new CanMessage(CanMessage.Type.CAN11, Integer.parseInt(new String(destination), 16), new String(data), timestampMillis);
		} catch (IOException e) {
			throw new ParseException("Invalid CAN message " + new String(readBuf), 0);
		}
	}
}
