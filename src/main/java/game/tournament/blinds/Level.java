package game.tournament.blinds;

public class Level {

    /**
     * Small blind amount, in number of chips, not cents.
     */
    private double smallBlindAmount;

    /**
     * Big blind amount, in number of chips, not cents.
     */
    private double bigBlindAmount;

    public Level() {
    }

    public Level(double smallBlindAmount, double bigBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
    }

    public double getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public double getBigBlindAmount() {
        return bigBlindAmount;
    }
}
