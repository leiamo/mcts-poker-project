package bots.smarterbot;

public enum Action {
    PREMIUM(5),
    RERAISE(4),
    RAISE(3),
    CALL(2),
    LIMP(1),
    FOLD(0),
    INVALID(-1);

    private final int value;

    Action(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Action oneUp() {
        switch (this.value) {
            case 0:
                return Action.LIMP;
            case 1:
                return Action.CALL;
            case 2:
                return Action.RAISE;
            case 3:
                return Action.RERAISE;
            case 4:
                return Action.PREMIUM;
            default:
                return Action.INVALID;
        }
    }

    public Action oneDown() {
        switch (this.value) {
            case 1:
                return Action.FOLD;
            case 2:
                return Action.LIMP;
            case 3:
                return Action.CALL;
            case 4:
                return Action.RAISE;
            case 5:
                return Action.RERAISE;
            default:
                return Action.INVALID;
        }
    }

    public static Action convert(int number) {
        switch (number) {
            case 0:
                return Action.FOLD;
            case 1:
                return Action.LIMP;
            case 2:
                return Action.CALL;
            case 3:
                return Action.RAISE;
            case 4:
                return Action.RERAISE;
            case 5:
                return Action.PREMIUM;
            default:
                return Action.INVALID;
        }
    }
}
