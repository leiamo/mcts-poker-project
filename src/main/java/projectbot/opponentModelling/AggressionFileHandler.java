package projectbot.opponentModelling;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class AggressionFileHandler {

    public String file;
    public String[] headings;
    public HashMap<String, AggressionDataRow> aggressionData;

    public AggressionFileHandler(String fileLocation) throws IOException {
        this.file = fileLocation;
        headings = new String[]{"playerName", "bets", "calls", "checks", "folds", "raises", "aggression"};
        aggressionData = new HashMap<>();
        readFile();
        writeFile(file.replace(".csv","-copy.csv"));
    }

    public void updateFile(AggressionDataRow updatedRow) throws IOException {
        updatedRow.recalculateAggression();
        aggressionData.put(updatedRow.playerName, updatedRow);
        writeFile(file);
    }

    public AggressionDataRow getAggressionData(String playerName) {
        if (aggressionData.containsKey(playerName)) {
            return aggressionData.get(playerName);
        }
        else {
            return new AggressionDataRow(playerName, 0, 0, 0, 0, 0);
        }
    }

    private void writeFile(String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        String line = String.join(",", headings);
        bw.write(line);
        Iterator iterator = aggressionData.values().iterator();
        while (iterator.hasNext()) {
            line = String.join(",", unparseData((AggressionDataRow) iterator.next()));
            bw.newLine();
            bw.write(line);
        }
        bw.close();
    }

    private void readFile() throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            String line = "";
            int row[] = new int[5];
            while ((line = br.readLine()) != null) {
                String[] rowStrings = line.split(",");
                String playerName = rowStrings[0];
                for (int i = 0; i < 5; i++) {
                    row[i] = Integer.parseInt(rowStrings[i + 1]);
                    AggressionDataRow aggressionDataRow = parseData(playerName, row);
                    aggressionData.put(playerName, aggressionDataRow);
                }
            }
            br.close();
        }
        catch (FileNotFoundException e) {
            new File(file);
        }
    }

    private AggressionDataRow parseData(String playerName, int[] row) {
        return new AggressionDataRow(playerName, row[0], row[1], row[2], row[3], row[4]);
    }

    private String[] unparseData(AggressionDataRow aggressionDataRow) {
        return new String[]{
                aggressionDataRow.playerName,
                Integer.toString(aggressionDataRow.numBets),
                Integer.toString(aggressionDataRow.numCalls),
                Integer.toString(aggressionDataRow.numChecks),
                Integer.toString(aggressionDataRow.numFolds),
                Integer.toString(aggressionDataRow.numRaises),
                Double.toString(aggressionDataRow.aggression)
        };
    }
}
