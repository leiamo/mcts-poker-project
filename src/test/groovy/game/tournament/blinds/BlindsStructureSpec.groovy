package game.tournament.blinds

import spock.lang.Specification

class BlindsStructureSpec extends Specification {

    def "should create a blinds structure like RVP"() {
        when:
        def blinds = new BlindsStructure(10, 20, 1000)

        then:
        blinds.numLevels == 8
        blinds.getBlindsLevel(level - 1).smallBlindAmount == small
        blinds.getBlindsLevel(level - 1).bigBlindAmount == big

        where:
        level | small | big
        1     | 10    | 20
        2     | 20    | 40
        3     | 40    | 80
        4     | 80    | 160
        5     | 160   | 320
        6     | 320   | 640
        7     | 640   | 1280
        8     | 1280  | 2560
    }

    def "should calculate the current level based on the current hand number"() {
        when:
        def blinds = new BlindsStructure(10, 10, 1000)

        then:
        blinds.getCurrentLevel(hand).smallBlindAmount == small

        where:
        hand | small
        1    | 10
        11   | 20
        21   | 40
        31   | 80
        41   | 160
        51   | 320
        61   | 640
        100  | 1280
    }

    def "should raise the blinds based on the number of players"() {
        when:
        def blinds = new BlindsStructure(1000, 10, 1000)

        then:
        blinds.getCurrentLevel(hand).smallBlindAmount == small

        where:
        hand | small
        1    | 10
        11   | 20
        21   | 40
        31   | 80
        41   | 160
        51   | 320
        61   | 640
        101  | 10240
        141  | 81920
    }
}
