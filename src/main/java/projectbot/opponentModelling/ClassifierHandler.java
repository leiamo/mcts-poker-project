package projectbot.opponentModelling;

import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

public class ClassifierHandler {

    public Classifier winModel;
    public NaiveBayesUpdateable actionModel;
    public Instances data;
    public MLFileHandler MLFileHandler;
    public String fileName;

    // Initialise opponent models
    public ClassifierHandler(String homeDir, String selectedModelForWinProb, String opponentName) throws Exception {
        fileName = homeDir + "\\data\\trainingData10k.csv";
        MLFileHandler = new MLFileHandler(fileName);
        data = MLFileHandler.dataSet;

        // Create model to predict perceived winning probabilities
        winModel = createModel(selectedModelForWinProb.toLowerCase());
        buildWinModel();

        // Create model to predict actions
        actionModel = buildClassifier();
    }

    // Create Linear Regression (LR), K-Nearest Neighbours (IBk), Multi-Layer Perceptron (MLP)
    public Classifier createModel(String chosenModel){
        switch (chosenModel) {
            case "lr":
                return new LinearRegression();
            case "ibk":
                return new IBk();
            case "mlp":
                return new MultilayerPerceptron();
        }
        return new LinearRegression();
    }

    // Only updates opponent model for classifying actions
    public void updateClassifiers(Instance instance) throws Exception {
        instance.setDataset(data);
        data.setClassIndex(3);
        actionModel.updateClassifier(instance);
    }


    // Updates offline file with online training data, and rebuilds win opponent model
    public void updateFileWithOnlineData(Instances onlineDataSet) throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

        // Combine with new online training data
        Enumeration enumeration = onlineDataSet.enumerateInstances();
        while (enumeration.hasMoreElements()) {
            Instance newInstance = (Instance) enumeration.nextElement();
            data.add(newInstance);

            // Write directly to end of file
            out.println(newInstance.toString());
        }
        out.close();

        // Rebuild opponent model for predicting win probabilities,
        // using combined instances from online data and old training data
        buildWinModel();
    }

    // Use opponent model to return predicted win probability
    public double getOpponentWinProb(String enemyName, RoundEnum roundEnum, int position, ActionEnum actionEnum,
                                     double potSize, double aggression) throws Exception {
        // Reformat training data attributes
        String round = roundEnum.convertToMLString();
        String action = actionEnum.convertToMLString();

        // Initialise instance using data input
        Instance instance = new Instance(7);
        instance.setDataset(data);
        instance.setValue(0, enemyName);
        instance.setValue(1, round);
        instance.setValue(2, position);
        instance.setValue(3, action);
        instance.setValue(4, potSize);
        instance.setValue(6, aggression);

        // Prediction for winning probability
        instance.setMissing(5);
        return winModel.classifyInstance(instance);
    }

    // Use opponent model to return predicted action
    public String getLikelyAction(String enemyName, RoundEnum roundEnum, int position, double potSize, double aggression, double winProb) throws Exception {
        // Reformat attribute
        String round = roundEnum.convertToMLString();

        // Initialise instance using data input
        Instance instance = new Instance(7);
        instance.setDataset(data);
        instance.setValue(0, enemyName);
        instance.setValue(1, round);
        instance.setValue(2, position);
        instance.setValue(4, potSize);
        instance.setValue(6, aggression);

        // Prediction for action
        instance.setMissing(3);
        double prediction = actionModel.classifyInstance(instance);

        // Return classified prediction
        if (prediction == 0.0) return "ck";
        else return "br";
    }


    // Build Naive Bayes classifier for predicting actions
    public NaiveBayesUpdateable buildClassifier() throws Exception {
        data.setClassIndex(3);
        NaiveBayesUpdateable classifier = new NaiveBayesUpdateable();
        classifier.buildClassifier(data);
        return classifier;
    }

    // Build model for predicting perceived winning probabilities
    public void buildWinModel() throws Exception {
        data.setClassIndex(5);
        winModel.buildClassifier(data);
    }

    // Build 5-NN opponent model
    public IBk buildIBk() throws Exception {
        data.setClassIndex(5);
        IBk model = new IBk();
        model.buildClassifier(data);
        model.setKNN(5);
        return model;
    }
}
