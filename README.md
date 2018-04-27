<<<<<<< HEAD
Open Meerkat Bot Simulation Testbed
-----------------------------------

This is a fork from https://code.google.com/p/opentestbed

I've ported it to a maven project, so it should be easier to setup the development environment (specially in an IDE other than Eclipse) and make it run. For more information on how to run it, see the [Wiki](https://github.com/corintio/opentestbed/wiki/How-To-Simulate-Cash-Games)


Dependencies
------------

This project requires the Meerkat API 2.5 available at http://www.poker-academy.com/community.php
The .jar is located in the /lib folder. Documentation can be found at the URL above.

To install the meerkat api into your maven repository:
````mvn install:install-file -Dfile=lib/meerkat-api.jar -DgroupId=com.biotools.meerkat -DartifactId=meerkat-api -Dversion=2.5 -Dpackaging=jar````
=======
# mcts-poker-project
Final Year CompSci Project Source Code

-----
SETUP
-----
- Run game simulations from "src/main/java/CashGameConsoleStarter.java"
- Set number of games
- Set bankroll
- Select opponent by uncommenting enemy name
- Set local home directory strings at:
	- "src/main/java/CashGameConsoleStarter.java"
	- "src/main/java/projectbot/Abot.java"

-----------
PROJECT BOT
-----------
- Located at "src/main/java/projectbot/Abot.java" with GPL licence
- Uses Monte Carlo Tree Search with opponent modelling
- You can initialise strategies for MCTS in "initStrategiesForMCTS()" function.
- Recommended to use the following settings:
	- Number of MCTS iterations = 100,000
	- Selection = SmoothUCTSelection(0.1, 0.9, 0.00005, 17)
	- Expansion = FullExpansion()
	- Simulation = PredictiveSimulation()
	- Back-propagation = AdvancedBackpropagation()

-------
TESTBED
-------
- The Open Poker Testbed is imported from https://github.com/corintio/opentestbed with GPL licence
- Requires Meerkat and Weka dependencies already provided in code package
- Changes made to this code include fixes for limit and heads-up poker

-------------
TRAINING DATA
-------------
- External data parsing software has been used to create "data/parsedData.csv" file.
  Extracted using hand histories from IRC Database (http://poker.cs.ualberta.ca/irc_poker_database.html)
- The Data Handler from "dataHandler/DataHandler.java" converts the parsed data into training data
- This only needs to be done once, unless more training data is required.
- Training data files available:
	- "data/trainingData1k.csv" contains 1,000 entries
	- "data/trainingData10k.csv" contains 10,000 entries *RECOMMENDED*
	- "data/trainingData20k.csv" contains 20,000 entries
	- "data/trainingData45k.csv" contains 45,000 entries
- There is also a file that stores associated aggression values for each opponent at "data/aggression.csv"
- These files are updated in-game with online training data that models opponent behaviour
- Variations of these files exist at "data/AggressiveBot", "data/PassiveBot", "data/SimpleBot".
  These are used in experiments to compare the different resulting systems from the "data/Orginal" initial state
>>>>>>> d8a07c6b370327aeecaec3fdc638739e2e3b6ef7
