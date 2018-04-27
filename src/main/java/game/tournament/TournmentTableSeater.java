package game.tournament;

import bots.BotRepository;
import game.AbstractGameDescription;
import game.PublicGameInfo;
import game.PublicPlayerInfo;
import game.TableSeater;
import org.apache.log4j.Logger;

import java.util.*;

public class TournmentTableSeater extends TableSeater {
    private static final Logger log = Logger.getLogger(TournmentTableSeater.class);

    public TournmentTableSeater(BotRepository botRepository) {
        super(botRepository);
    }

    @Override
    public PublicGameInfo[] createTables(AbstractGameDescription gameDescription) {
        int numTotalSeats = gameDescription.getNumSeats();

        PublicGameInfo[] gameInfos = initTables(numTotalSeats);

        List<Integer> players = randomizePlayers(numTotalSeats);

        int playerIndex = 0;
        for (PublicGameInfo gameInfo : gameInfos) {
            for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
                seatPlayer(gameDescription, gameInfo, seat, players.get(playerIndex++));
                numTotalSeats--;
            }
        }

        return gameInfos;
    }

    private List<Integer> randomizePlayers(int numTotalSeats) {
        List<Integer> players = new ArrayList<Integer>(numTotalSeats);
        for (int i = 0; i < numTotalSeats; i++) {
            players.add(i);
        }
        Collections.shuffle(players);
        return players;
    }

    private PublicGameInfo[] initTables(int numTotalSeats) {
        int[] tableLengths = calcTableLengths(numTotalSeats);
        int neededTables = tableLengths.length;
        PublicGameInfo[] gameInfos = new PublicGameInfo[neededTables];

        for (int i = 0; i < neededTables; i++) {
            gameInfos[i] = new PublicGameInfo();
            gameInfos[i].setNumSeats(tableLengths[i]);
        }

        return gameInfos;
    }

    private int[] calcTableLengths(int numTotalSeats) {
        int neededTables = Math.max(1, (int) Math.ceil(numTotalSeats / 10.0));
        int numSeatsPerTable = (int) Math.floor((double) numTotalSeats / neededTables);
        int exceding = numTotalSeats - (numSeatsPerTable * neededTables);

        int[] tableLengths = new int[neededTables];

        for (int i = 0; i < neededTables; i++) {
            int numSeats = numSeatsPerTable;
            if (exceding > 0) {
                numSeats++;
                exceding--;
            }
            tableLengths[i] = numSeats;
        }

        return tableLengths;
    }

    public PublicGameInfo[] rearrangeTables(PublicGameInfo[] gameInfos) {
        if (gameInfos.length <= 1) {
            return gameInfos;
        }

        int remainingPlayers = countRemainingPlayers(gameInfos);

        int neededTables = Math.max(1, (int) Math.ceil(remainingPlayers / 10.0));
        if (neededTables == gameInfos.length) {
            return gameInfos;
        }

        log.debug("Rearranging " + gameInfos.length + " tables in " + neededTables);
        Set<PublicGameInfo> tablesToBeRemoved = findTablesToBeRemoved(gameInfos, neededTables);
        Deque<PublicPlayerInfo> movingPlayers = getMovingPlayers(tablesToBeRemoved);

        PublicGameInfo[] newGameInfos = new PublicGameInfo[neededTables];
        int newGameIndex = 0;
        for (PublicGameInfo gameInfo : gameInfos) {
            if (!tablesToBeRemoved.contains(gameInfo)) {
                newGameInfos[newGameIndex++] = gameInfo;
                int availableSeats = getNumAvailableSeats(gameInfo);
                for (int i = 0; i < availableSeats; i++) {
                    if (movingPlayers.isEmpty()) {
                        break;
                    }
                    try {
                        PublicPlayerInfo player = movingPlayers.pop();
                        PublicGameInfo originTable = (PublicGameInfo) player.getGameInfo();
                        player.setSittingOut(true);
                        originTable.removePlayer(player);
                        gameInfo.seatPlayer(player);
                    } catch (NoSuchElementException e) {
                        log.error("There are no unseated players left!");
                        break;
                    }
                }
            }
        }

        return newGameInfos;
    }

    private int getNumAvailableSeats(PublicGameInfo table) {
        int activePlayers = 0;
        for (int seat = 0; seat < table.getNumSeats(); seat++) {
            PublicPlayerInfo player = table.getPlayer(seat);
            if (player != null && !player.isSittingOut()) {
                activePlayers++;
            }
        }
        return 10 - activePlayers;
    }

    private Deque<PublicPlayerInfo> getMovingPlayers(Set<PublicGameInfo> tablesToBeRemoved) {
        Deque<PublicPlayerInfo> result = new LinkedList<PublicPlayerInfo>();
        for (PublicGameInfo table : tablesToBeRemoved) {
            for (int seat = 0; seat < table.getNumSeats(); seat++) {
                PublicPlayerInfo player = table.getPlayer(seat);
                if (player != null && !player.isSittingOut()) {
                    result.add(player);
                }
            }
        }
        return result;
    }

    private Set<PublicGameInfo> findTablesToBeRemoved(PublicGameInfo[] gameInfos, int neededTables) {
        List<PublicGameInfo> games = new ArrayList<PublicGameInfo>(Arrays.asList(gameInfos));
        Collections.sort(games, new Comparator<PublicGameInfo>() {
            @Override
            public int compare(PublicGameInfo table1, PublicGameInfo table2) {
                return getNumAvailableSeats(table1) - getNumAvailableSeats(table2);
            }
        });

        Set<PublicGameInfo> result = new HashSet<PublicGameInfo>();
        for (int i = 0; i < games.size() - neededTables; i++) {
            result.add(games.get(i));
        }

        return result;
    }

    private int countRemainingPlayers(PublicGameInfo[] gameInfos) {
        int remainingPlayers = 0;
        for (PublicGameInfo gameInfo : gameInfos) {
            for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
                PublicPlayerInfo player = gameInfo.getPlayer(seat);
                if (player != null && !player.isSittingOut()) {
                    remainingPlayers++;
                }
            }
        }
        return remainingPlayers;
    }
}
