package org.example.service;

import org.example.model.Transaction;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FraudMLService {

    private RandomForest model;
    private ArrayList<Attribute> attributes;
    private Instances dataStructure;

    @PostConstruct
    public void init() {

        try {
            // 1. DEFINE FEATURES (Inputs for the AI)
            // We use: Amount, Hour of Day, and IsFraud (Target)
            Attribute amountAttr = new Attribute("amount");
            Attribute hourAttr = new Attribute("hour");
            Attribute fraudClass = new Attribute("isFraud", Arrays.asList("safe", "fraud"));

            attributes = new ArrayList<>();
            attributes.add(amountAttr);
            attributes.add(hourAttr);
            attributes.add(fraudClass);

            // 2. CREATE SYNTHETIC TRAINING DATA
            // (In a real company, you would load this from a CSV file)
            dataStructure = new Instances("TransactionData", attributes, 100);
            dataStructure.setClassIndex(2); // The last attribute is the target (fraud/safe)

            for (int i = 0; i < 15; i++) {
                addTrainingData(15.0, 4, "fraud");
            }

            for (int i = 0; i < 30; i++) {
                addTrainingData(5000.0, 14, "safe");
            }

            // Teach the AI: "Small amounts are usually safe"
            for (int i = 0; i < 50; i++) {
                addTrainingData(10.0 + (Math.random() * 500), 12, "safe");
            }
            // Teach the AI: "Large amounts at 3 AM are suspicious"
            for (int i = 0; i < 20; i++) {
                addTrainingData(20000.0 + (Math.random() * 50000), 3, "fraud");
            }

            // 3. TRAIN THE RANDOM FOREST MODEL
            model = new RandomForest();
            model.buildClassifier(dataStructure);
            System.out.println(" ML Model Trained Successfully (Random Forest)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTrainingData(double amount, int hour, String status) {
        DenseInstance instance = new DenseInstance(3);
        instance.setValue(attributes.get(0), amount);
        instance.setValue(attributes.get(1), hour);
        instance.setValue(attributes.get(2), status);
        instance.setDataset(dataStructure);
        dataStructure.add(instance);
    }

    // 4. PREDICT FUNCTION
    public double predictRisk(Transaction t) {
        try {
            DenseInstance newTxn = new DenseInstance(3);
            newTxn.setValue(attributes.get(0), t.getAmount());
            newTxn.setValue(attributes.get(1), t.getTimestamp().getHour());
            newTxn.setDataset(dataStructure); // Connect to structure

            // Returns [probability_safe, probability_fraud]
            double[] probabilities = model.distributionForInstance(newTxn);

            // Return the probability of fraud (0.0 to 1.0)
            return probabilities[1];

        } catch (Exception e) {
            System.err.println("ML Prediction Error: " + e.getMessage());
            return 0.0; // Default to safe if ML fails
        }
    }
}