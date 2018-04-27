package projectbot.opponentModelling;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import java.io.File;
import java.io.IOException;

public class MLFileHandler {

    public Instances dataSet;
    public String fileName;

    public MLFileHandler(String fileLocation) throws IOException {
        // Initialise local data set for this file handler
        fileName = fileLocation;
        dataSet = readCSV();

        // Make backup copy for training data
        writeCSV(fileName.replace(".csv", "-copy.csv"));
    }

    public Instance convertToInstance(TrainingDataRow newRow) {
        Instance newInstance = new Instance(7);
        newInstance.setDataset(dataSet);
        newInstance.setValue(0, newRow.playerName);
        newInstance.setValue(1, newRow.round);
        newInstance.setValue(2, Integer.parseInt(newRow.position));
        newInstance.setValue(3, newRow.action);
        newInstance.setValue(4, Double.parseDouble(newRow.potSize));
        newInstance.setValue(5, Double.parseDouble(newRow.winProb));
        newInstance.setValue(6, Double.parseDouble(newRow.aggression));
        return newInstance;
    }

    public void updateDataAndClassifiers(TrainingDataRow newRow, ClassifierHandler classifierHandler) throws Exception {
        Instance instance = convertToInstance(newRow);
        updateDataSet(instance);
        classifierHandler.updateClassifiers(instance);
    }

    public void updateDataSet(Instance newInstance) {
        dataSet.add(newInstance);
    }

    private Instances readCSV() throws IOException {
        Instances data;
        File dataFile = new File(fileName);
        if (dataFile.exists() && dataFile.length() > 0) {
            // Read and load existing training data file
            CSVLoader loader = new CSVLoader();
            loader.setFile(dataFile);
            data = loader.getDataSet();
        } else {
            // If file does not exist, this initialises the data set
            new File(fileName);
            data = initDataSet();
        }
        return data;
    }

    public Instances initDataSet() {
        FastVector attributes = new FastVector();

        FastVector rounds = new FastVector();
        rounds.addElement("Preflop");
        rounds.addElement("Flop");
        rounds.addElement("Turn");
        rounds.addElement("River");

        FastVector actions = new FastVector();
        actions.addElement("ck");
        actions.addElement("br");

        attributes.addElement(new Attribute("playerName", (FastVector) null));
        attributes.addElement(new Attribute("round", rounds));
        attributes.addElement(new Attribute("position"));
        attributes.addElement(new Attribute("action", actions));
        attributes.addElement(new Attribute("potSize"));
        attributes.addElement(new Attribute("winProb"));
        attributes.addElement(new Attribute("aggression"));

        return new Instances("", attributes, 0);
    }

    private void writeCSV(String fileLocation) throws IOException {
        if (dataSet == null) {
            new File(fileLocation);
        } else {
            CSVSaver writer = new CSVSaver();
            writer.setInstances(dataSet);
            writer.setFile(new File(fileLocation));
            writer.writeBatch();
        }
    }

}