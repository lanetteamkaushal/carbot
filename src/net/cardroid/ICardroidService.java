package net.cardroid;

/**
 * Date: Oct 10, 2010
 * Time: 8:57:18 PM
 *
 * @author Lex Nikitin
 */
public interface ICardroidService {
    boolean isFake();

    void connectTo(String deviceAddress);

    void setIsFake(boolean isFake);

    String getDefaultAdapter();
}
