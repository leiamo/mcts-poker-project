package projectbot.dataHandler;

import projectbot.opponentModelling.GameDataRow;
import projectbot.opponentModelling.TrainingDataRow;
import java.io.*;
import java.util.LinkedList;

/* NOTES:
    This code has not been optimised with threading, also it is highly suggested to add such features.
    Otherwise, limit the number of entries from the data-set as done for this project.
    Converting 60,000 entries takes up to 24 hours (depending on hardware being used).
    The reason for this slow speed is due to the timely process of calculating winning probability calculations
    for each entry.
*/

public class DataHandler {

    // Set the following for your system:
    static int numEntries = 50000;

    // TODO Initialise home directory with local file location
    static String homeDir = "C:\\Users\\leila\\Desktop\\opentestbed-project\\opentestbed-master-1\\opentestbed-master" +
            "\\src\\main\\java\\projectbot\\data\\";

    // This is the file produced by the C# parser
    static String readFileLocation = homeDir + "parsedData.csv";

    // This is the new file location used by opponent models for training
    static String writeFileLocation = homeDir + "trainingData45k.csv";


    // The data handler is responsible for a one-time purpose to convert to parsed game data into useful training data
    // This will be used for initially training the opponent models with offline information before gameplay
    public static void main(String[] args) throws IOException {
        // Read and generate game data
        LinkedList<GameDataRow> gameData = readGameData();

        // Convert game data into training data
        DataConverter dataConverter = new DataConverter(homeDir);
        LinkedList<TrainingDataRow> trainingData = dataConverter.convertToTrainingData(gameData);

        // Write training data to file
        writeTrainingData(trainingData);
    }

    // Method that reads parsed data and converts into game data
    private static LinkedList<GameDataRow> readGameData() {
        LinkedList<GameDataRow> data = new LinkedList<>();
        String line, board;
        String[] row;
        int count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(readFileLocation));
            while ((line = br.readLine()) != null && count <= numEntries) {
                // Ignore headings
                if (count != 0) {
                    row = line.split(",");

                    // Initialise board settings (row[7] does not exist when empty board)
                    if (row.length < 8) board = "";
                    else board = row[7].trim();

                    // Normalises pot size amount from 10-20 limit hold'em to 2-4 limit hold'em.
                    // This only applies for data parsed from the IRC hand histories.
                    // Changing the limit bet amounts will impact this attribute in learning.
                    row[4] = Integer.toString(Integer.parseInt(row[4].trim()) / 5);

                    // Add new data row to list
                    data.add(new GameDataRow(row[0], row[1], row[2], row[3], row[4], row[5], row[6], board));
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Method that writes training data to file
    private static void writeTrainingData(LinkedList<TrainingDataRow> output) {
        String[] headings = new String[]{"playerName", "round", "position", "action", "potSize", "winProb", "aggression"};

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(writeFileLocation));
            bw.write(String.join(",", headings));
            for (TrainingDataRow entry : output) {
                // Ignores data rows without a calculated aggression value (default setting is 0.5)
                if (!entry.aggression.equals("0.5")) {
                    bw.newLine();
                    bw.write(entry.printString());
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
