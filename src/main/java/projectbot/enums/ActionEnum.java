package projectbot.enums;

import com.biotools.meerkat.Action;

public enum ActionEnum {
    RAISE(4),
    BET(3),
    CALL(2),
    CHECK(1),
    FOLD(0),
    INVALID(-1);


    private final int value;

    ActionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String convertToMLString() {
        if (this == BET || this == RAISE) {
            return "br";
        }
        return "ck";
    }

    public static ActionEnum convertToAction(String action) {
        switch (action)
        {
            case "k":
                return ActionEnum.CHECK;
            case "r":
                return ActionEnum.RAISE;
            case "b":
                return ActionEnum.BET;
            case "c":
                return ActionEnum.CALL;
            case "f":
                return ActionEnum.FOLD;
        }
        return ActionEnum.INVALID;
    }

    public static ActionEnum convertMeerkatStringToAction(String action) {
        String act = action.split(" ")[0];
        switch (act)
        {
            case "Check":
                return ActionEnum.CHECK;
            case "Raise":
                return ActionEnum.RAISE;
            case "Bet":
                return ActionEnum.BET;
            case "Call":
                return ActionEnum.CALL;
            case "Fold":
                return ActionEnum.FOLD;
        }
        return ActionEnum.INVALID;
    }

    public String convertToString() {
        switch (this) {
            case CHECK:
                return "CHECK";
            case RAISE:
                return "RAISE";
            case BET:
                return "BET";
            case CALL:
                return "CALL";
            case FOLD:
                return "FOLD";
        }
        return "INVALID";
    }


    public static Action convertAction(int number, double betLimit, boolean isFirstMove) {
        double callAmount = betLimit;
        if (isFirstMove) callAmount = betLimit / 2;
        switch (number) {
            case 0:
                return Action.foldAction(betLimit);
            case 1:
                return Action.checkAction();
            case 2:
                return Action.callAction(callAmount);
            case 3:
                return Action.betAction(betLimit);
            case 4:
                return Action.raiseAction(callAmount, betLimit);
        }
        return Action.foldAction(betLimit);
    }
}
