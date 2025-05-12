package pt.ipbeja.app.model;

/**
 * The fifteen puzzle view
 *
 * @author João Paulo Barros
 * @version 2025/05/12
 */
public interface View {
    void notifyView(Move move, Boolean winning, int tValue);
}
