package bots;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.Player;
import com.biotools.meerkat.util.Preferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TCP-IP Bridge for Meerkat API. Found on poker-ai and poker-bot.ru forums. Never tested myself.
 *
 * @author Unknown
 */
public class ComBot implements Player {
    private int ourSeat;       // our seat for the current hand
    private GameInfo gi;       // general game information
    private Preferences prefs; // the configuration options for this bot
    private Socket connection;

    public JPanel getSettingsPanel() {
        JPanel jp = new JPanel();
        SpinnerModel PortNumberModel = new SpinnerNumberModel(prefs.getIntPreference("PORT_NUMBER"),
                1024,       // min
                65535,      // max
                1);         // step
        final JSpinner PortNumber = new JSpinner(PortNumberModel);
        class PortNumberListener implements ChangeListener {
            public void stateChanged(ChangeEvent evt) {
                prefs.setPreference("PORT_NUMBER", ((Integer) ((JSpinner) evt.getSource()).getModel().getValue()).intValue());
            }
        }
        PortNumber.addChangeListener(new PortNumberListener());

        jp.add(new JLabel("Port Number"));
        jp.add(PortNumber);
        return jp;
    }

    public Preferences getPreferences() {
        return prefs;
    }

    public void comSend(String s) {
        try {
            connection.getOutputStream().write(s.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void init(Preferences playerPrefs) {
        this.prefs = playerPrefs;
    }

    public void gameStateChanged() {
    }

    public void gameStartEvent(GameInfo gInfo) {
        this.gi = gInfo;
        try {
            connection = new Socket("127.0.0.1", prefs.getIntPreference("PORT_NUMBER", 50000));
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String s = String.format("01;%4.2f;%d;%d;%d;%d\n",
                gi.getBigBlindSize(),
                gi.getNumPlayers(),
                gi.getButtonSeat(),
                gi.getGameID(),
                gi.isFixedLimit() ? 1 : 0);
        comSend(s);
        for (int seat = 0; seat < 10; seat++) {
            if (gi.inGame(seat)) {
                s = String.format("02;%d;%s;%4.2f;\n", seat, gi.getPlayer(seat).getName(), gi.getPlayer(seat).getBankRoll());
                comSend(s);
            }
        }
    }

    public void stageEvent(int stage) {
        String s = String.format("03;%d;%s;\n", stage, gi.getBoard().toString());
        comSend(s);
    }

    public void actionEvent(int pos, Action act) {
        double toCall = gi.getPlayer(pos).getAmountToCall();
        double toRaise = gi.isFixedLimit() ? gi.getPlayer(pos).getAmountRaiseable() : act.getAmount();
        String s = "";
        switch (act.getType()) {
            case Action.SMALL_BLIND:
            case Action.BIG_BLIND:
            case Action.POST_BLIND:
            case Action.POST_ANTE:
            case Action.POST_DEAD_BLIND:
            case Action.INVALID:
            case Action.FOLD:
            case Action.CHECK:
            case Action.ALLIN_PASS:
            case Action.MUCK:
            case Action.SIT_OUT:
            case Action.BET:
                s = String.format("04;%d;%d;%4.2f;\n", pos, act.getType(), act.getAmount());
                break;
            case Action.CALL:
                s = String.format("04;%d;%d;%4.2f;\n", pos, act.getType(), toCall);
                break;
            case Action.RAISE:
                s = String.format("04;%d;%d;%4.2f;\n", pos, act.getType(), toCall + toRaise);
        }
        comSend(s);
    }

    public void dealHoleCardsEvent() {
        comSend("05;\n");
    }

    public void holeCards(Card c1, Card c2, int seat) {
        String s = String.format("06;%d;%s %s;\n", seat, c1.toString(), c2.toString());
        comSend(s);
        this.ourSeat = seat;
    }

    public Action getAction() {
        double toCall = gi.getAmountToCall(ourSeat);
        double toRaise = gi.getPlayer(ourSeat).getAmountRaiseable();
        Action act;
        act = Action.foldAction(toCall);

        StringBuilder raiseString = new StringBuilder("");

        String s = String.format("07;%4.2f;%4.2f;\n", toCall, toRaise);
        comSend(s);

        int f = 0;
        int b = 0;
        while (true) {
            try {
                b = connection.getInputStream().read();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (b == 10) break;

            if (f != 0 && b != -1) {
                raiseString.append((char) b);
            } else {
                switch (b) {
                    case 48:
                        s = s.concat("0");
                        break;
                    case 49:
                        s = s.concat("1");
                        break;
                    case 50:
                        s = s.concat("2");
                        break;
                    case 59:
                        s = s.concat(";");
                        f = 1;
                }

                if (b == -1) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        String s2 = raiseString.toString();

        double stakes = 0;

        if (!s2.isEmpty())
            stakes = Double.valueOf(s2);

        if (s.compareTo("0;") == 0) act = Action.foldAction(gi);
        if (s.compareTo("1;") == 0) act = Action.callAction(gi);
        if (s.compareTo("2;") == 0) {
            if (s2.isEmpty() || stakes - toCall < gi.getMinRaise())
                act = Action.betAction(gi);
            else if (toCall == 0)
                act = Action.betAction(stakes);
            else
                act = Action.raiseAction(toCall, stakes - toCall);
        }
        return act;
    }

    public void showdownEvent(int seat, Card c1, Card c2) {
        String s = String.format("08;%d;%s %s;\n", seat, c1.toString(), c2.toString());
        comSend(s);
    }

    public void winEvent(int pos, double amount, String handName) {
        String s = String.format("09;%d;%4.2f;%s;\n", pos, amount, handName);
        comSend(s);
    }

    public void gameOverEvent() {
        comSend("10;\n");
        try {
            connection.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}