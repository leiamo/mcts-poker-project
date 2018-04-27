package ww10.gui;

import game.stats.BankrollObserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import util.Utils;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.GameObserver;

/**
 * This BankrollObserver opens a window while the game is running, showing the current
 * progress. A bankroll-chart is updated every 10 seconds.<br>
 * <br>
 * The window closes itself on the end of the game.<br>
 * 
 * Register this class on a GameRunner and call {@link #createGraph()} in the end
 * to open a window with the final result
 */
public class DataModel implements BankrollObserver, GameObserver {

	private final SwingGUI gui = new SwingGUI(this);

	private String[] playerNames;

	private volatile int currentGamesPlayed;

	private HashMap<String, String> chosenNames = new HashMap<String, String>();

	private final LinkedList<GameStats> bankrollWindow = new LinkedList<GameStats>();
	private final LinkedList<GameStats> plotTimes = new LinkedList<GameStats>();
	private final LinkedList<Long> markerTimes = new LinkedList<Long>();

	private int nbGamesInTotalProfit = 0;

	private final Map<String, Double> currentTotalProfit = new HashMap<String, Double>();

	private final long plotWindow = 60 * 1000;

	private final long minBankrollWindow = plotWindow / 3;

	private long lastSubmitTime = 0;

	private final HashMap<String, int[]> currentActionFreqs = new HashMap<String, int[]>();

	private GameInfo gameInfo;

	public synchronized String[] getPlayerNames() {
		return playerNames;
	}

	public synchronized int getCurrentGamesPlayed() {
		return currentGamesPlayed;
	}

	public synchronized String getChosenName(String fixedName) {
		String chosenName = chosenNames.get(fixedName);
		if (chosenName == null)
			return fixedName;
		else
			return chosenName;
	}

	public synchronized String getLongChosenName(String fixedName) {
		String chosenName = chosenNames.get(fixedName);
		if (chosenName == null)
			return fixedName;
		else
			return chosenName + " (" + fixedName + ")";
	}

	public synchronized void onSubmit(final String fixedName, final String chosenName) {
		//TODO update GUI
		chosenNames.put(fixedName, chosenName);
		lastSubmitTime = System.currentTimeMillis();
		final int fCurrentGamesPlayed = currentGamesPlayed;
		markerTimes.add(lastSubmitTime);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.averageProfitPanel.addMarker(fCurrentGamesPlayed, fixedName + " submits " + chosenName);
			}
		});
	}

	@Override
	public synchronized void gameStarted(int numSeatPermutations, int numGames, Set<String> playerNames) {
		this.playerNames = playerNames.toArray(new String[playerNames.size()]);
		Arrays.sort(this.playerNames);

		for (String player : playerNames) {
			currentTotalProfit.put(player, 0.0);
			currentActionFreqs.put(player, new int[5]);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.initialize();
			}
		});
	}

	@Override
	public synchronized void updateBankroll(int seatpermutation, final Map<String, Double> playerDelta) {
		currentGamesPlayed++;

		//update stats

		currentGameStats.time = System.currentTimeMillis();
		currentGameStats.game = currentGamesPlayed;
		currentGameStats.bankrollDeltas = playerDelta;

		addGameStats(currentGameStats);
		bankrollWindow.add(currentGameStats);

		if (currentGamesPlayed % 1 == 0) {
			updatePlot();
		}
	}

	private void updatePlot() {

		//remove old stats

		// update bankroll
		removeFromWindow();

		// update plot

		// update data points
		final int fNbRemoved = removeDatapoints();
		final int fCurrentGamesPlayed = currentGamesPlayed;
		final ConcurrentHashMap<String, Double> fCurrentAvgProfit = getAvgProfit();

		//update status
		int currentGamesPlayed = getCurrentGamesPlayed();
		double gamesPerSecond = getGamesPerSecond();
		int window = getCurrentBankrollGameWindow();
		final String status = "Completed: " + currentGamesPlayed + " games, " + "Speed: " + Utils.roundToCents(gamesPerSecond) + " games/s, " + "Window: "
				+ window + " games";

		//update markers
		final int fNbRemovedMarkers = removeMarkers();

		//update action frequencies
		final ConcurrentHashMap<String, int[]> fActionFreqs = new ConcurrentHashMap<String, int[]>();
		for (Entry<String, int[]> entry : currentActionFreqs.entrySet()) {
			fActionFreqs.put(entry.getKey(), entry.getValue().clone());
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.averageProfitPanel.removeFirstDataPoints(fNbRemoved);
				gui.averageProfitPanel.addDataPoints(fCurrentGamesPlayed, fCurrentAvgProfit);
				gui.changeStatus(status);
				gui.averageProfitPanel.removeFirstMarkers(fNbRemovedMarkers);
				gui.actionPanel.updateActionFrequencies(fActionFreqs);
			}
		});
		plotTimes.add(currentGameStats);
	}

	private int removeFromWindow() {
		Iterator<GameStats> iter = bankrollWindow.iterator();
		boolean done = false;
		int nbRemoved = 0;
		long currentTime = System.currentTimeMillis();
		while (!done && iter.hasNext()) {
			GameStats first = iter.next();
			if (first.time < Math.min(lastSubmitTime, currentTime - minBankrollWindow)) {
				nbRemoved++;
				undoGameStats(first);
				iter.remove();
			} else {
				done = true;
			}
		}
		return nbRemoved;
	}

	private int removeDatapoints() {
		Iterator<GameStats> iter = plotTimes.iterator();
		boolean done = false;
		int nbRemoved = 0;
		long currentTime = System.currentTimeMillis();
		while (!done && iter.hasNext()) {
			GameStats first = iter.next();
			if (first.time < currentTime - plotWindow) {
				nbRemoved++;
				iter.remove();
			} else {
				done = true;
			}
		}
		return nbRemoved;
	}

	private int removeMarkers() {
		Iterator<Long> iter = markerTimes.iterator();
		boolean done = false;
		int nbRemoved = 0;
		long currentTime = System.currentTimeMillis();
		while (!done && iter.hasNext()) {
			long first = iter.next();
			if (first < currentTime - plotWindow) {
				nbRemoved++;
				iter.remove();
			} else {
				done = true;
			}
		}
		return nbRemoved;
	}

	private ConcurrentHashMap<String, Double> getAvgProfit() {
		ConcurrentHashMap<String, Double> avgProfit = new ConcurrentHashMap<String, Double>();
		for (String player : playerNames) {
			double currentProfit = currentTotalProfit.get(player);
			avgProfit.put(player, currentProfit / nbGamesInTotalProfit);
		}
		return avgProfit;
	}

	private void addGameStats(final GameStats stats) {
		nbGamesInTotalProfit++;
		for (String player : playerNames) {
			//profit
			double currentProfit = currentTotalProfit.get(player);
			currentTotalProfit.put(player, currentProfit + stats.bankrollDeltas.get(player));
			//actions
			int[] prevFreq = currentActionFreqs.get(player);
			int[] statFreq = stats.actionFreqs.get(player);
			for (int i = 0; i < statFreq.length; i++) {
				prevFreq[i] += statFreq[i];
			}
		}
	}

	private void undoGameStats(final GameStats stats) {
		nbGamesInTotalProfit--;
		for (String player : playerNames) {
			//profit
			double currentProfit = currentTotalProfit.get(player);
			currentTotalProfit.put(player, currentProfit - stats.bankrollDeltas.get(player));
			//actions
			int[] prevFreq = currentActionFreqs.get(player);
			int[] statFreq = stats.actionFreqs.get(player);
			for (int i = 0; i < statFreq.length; i++) {
				prevFreq[i] -= statFreq[i];
			}
		}
	}

	public synchronized float getGamesPerSecond() {
		int nbGames = getCurrentBankrollGameWindow();
		long time = getCurrentBankrollTimeWindow();
		return (float) (nbGames / (time / 1000.0));
	}

	public synchronized int getCurrentBankrollGameWindow() {
		return bankrollWindow.getLast().game - bankrollWindow.getFirst().game;
	}

	public synchronized long getCurrentBankrollTimeWindow() {
		return bankrollWindow.getLast().time - bankrollWindow.getFirst().time;
	}

	private GameStats currentGameStats;

	@Override
	public void actionEvent(int pos, Action act) {
		String player = gameInfo.getPlayerName(pos);
		int[] freq = currentGameStats.actionFreqs.get(player);
		switch (act.getType()) {
		case Action.SMALL_BLIND:
			break;
		case Action.BIG_BLIND:
			break;
		case Action.CALL:
			freq[3]++;
			break;
		case Action.RAISE:
			freq[4]++;
			break;
		case Action.BET:
			freq[1]++;
			break;
		case Action.CHECK:
			freq[0]++;
			break;
		case Action.FOLD:
			freq[2]++;
			break;
		case Action.MUCK:
			break;
		}

	}

	@Override
	public void dealHoleCardsEvent() {
	}

	@Override
	public void gameOverEvent() {
	}

	@Override
	public void gameStartEvent(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		currentGameStats = new GameStats();
		for (String player : playerNames) {
			currentGameStats.actionFreqs.put(player, new int[5]);
		}
	}

	@Override
	public void gameStateChanged() {
	}

	@Override
	public void showdownEvent(int arg0, Card arg1, Card arg2) {
	}

	@Override
	public void stageEvent(int arg0) {
	}

	@Override
	public void winEvent(int arg0, double arg1, String arg2) {
	}
}

final class GameStats {

	long time;
	int game;
	Map<String, Double> bankrollDeltas;

	final Map<String, int[]> actionFreqs = new HashMap<String, int[]>();

	public GameStats() {

	}

}