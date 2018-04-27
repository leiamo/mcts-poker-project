package projectbot.enums;

public enum RoundEnum {
    RIVER(4),
    TURN(3),
    FLOP(2),
    PREFLOP(1),
    INVALID(-1);

    private final int value;

    RoundEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String convertToMLString() {
        switch (this){
            case FLOP: return "Flop";
            case TURN: return "Turn";
            case RIVER: return "River";
            default: return "Preflop";
        }
    }

    public String convertToString() {
        switch (this) {
            case PREFLOP:
                return "PREFLOP";
            case FLOP:
                return "FLOP";
            case TURN:
                return "TURN";
            case RIVER:
                return "RIVER";
        }
        return "INVALID";
    }

    public static RoundEnum convert(int round) {
        switch (round) {
            case 1:
                return PREFLOP;
            case 2:
                return FLOP;
            case 3:
                return TURN;
            case 4:
                return RIVER;
            default:
                return INVALID;
        }
    }

}