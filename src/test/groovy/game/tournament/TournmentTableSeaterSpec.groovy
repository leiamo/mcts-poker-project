package game.tournament

import bots.BotMetaData
import bots.BotRepository
import bots.demobots.SimpleBot
import spock.lang.Specification

class TournmentTableSeaterSpec extends Specification {

    TournmentTableSeater seater
    BotRepository botRepository

    void setup() {
        botRepository = new BotRepository();
        botRepository.addBot(new BotMetaData('Simple', SimpleBot.class.canonicalName, true, null))

        seater = new TournmentTableSeater(botRepository)
    }

    def "should distribute the players evenly in the tables"() {
        when:
        def games = seater.createTables(createGameWithPlayers(numPlayers))

        then:
        games.length == numTables
        numTables.times { tableNum ->
            def tableLength = tableLengths[tableNum]
            assert tableLengths == games.collect { it.numPlayers }
            tableLength.times {
                assert games[tableNum].getPlayer(it).name.startsWith("Simply #")
            }
        }

        where:
        numPlayers | numTables | tableLengths
        9          | 1         | [9]
        10         | 1         | [10]
        11         | 2         | [6, 5]
        12         | 2         | [6, 6]
        13         | 2         | [7, 6]
        21         | 3         | [7, 7, 7]
        22         | 3         | [8, 7, 7]
        29         | 3         | [10, 10, 9]
    }

    def "should shrink number of tables if possible"() {
        given:
        def gameDescription = createGameWithPlayers(11)
        def games = seater.createTables(gameDescription)

        when:
        games[0].getPlayer(0).sittingOut = true
        games = seater.rearrangeTables(games)

        then:
        games.length == 1
        games[0].numActivePlayers == 10
    }

    def "should be able to delete more than one table when shrinking, if needed"() {
        given:
        def gameDescription = createGameWithPlayers(21)
        def games = seater.createTables(gameDescription)

        when:
        games[0].getPlayer(0).sittingOut = true
        5.times {
            games[1].getPlayer(it).sittingOut = true
            games[2].getPlayer(it).sittingOut = true
        }
        games = seater.rearrangeTables(games)

        then:
        games.length == 1
        games[0].numActivePlayers == 10
    }

    TournamentGameDescription createGameWithPlayers(int numPlayers) {
        def gameDescription = new TournamentGameDescription(200, 1000)
        gameDescription.botNames = (1..numPlayers).collect { "Simple" }
        gameDescription.inGameNames = (1..numPlayers).collect { "Simply #${it}" }

        gameDescription
    }
}
