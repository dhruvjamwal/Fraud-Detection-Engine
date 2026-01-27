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

@Service
public class FraudMLService {

    private RandomForest model;
    private ArrayList<Attribute> attributes;
    private Instances dataStructure;

    @PostConstruct
    public void init() {
        try {
            // 1. DEFINE FEATURES
            Attribute amountAttr = new Attribute("amount");
            Attribute hourAttr = new Attribute("hour");
            Attribute fraudClass = new Attribute("isFraud", Arrays.asList("safe", "fraud"));

            attributes = new ArrayList<>();
            attributes.add(amountAttr);
            attributes.add(hourAttr);
            attributes.add(fraudClass);

            dataStructure = new Instances("TransactionData", attributes, 100);
            dataStructure.setClassIndex(attributes.size() - 1);

            for (int i = 0; i < 25; i++) {
                addTrainingData(75000.0, 2, "fraud");
            }
            // Low value + Daytime = High Safety
            for (int i = 0; i < 60; i++) {
                addTrainingData(150.0 + (Math.random() * 1000), 14, "safe");
            }

            // 3. TRAIN MODEL
            model = new RandomForest();
            model.setNumIterations(100);
            model.buildClassifier(dataStructure);
            System.out.println("Neural Engine Synced: RandomForest ready for inference.");

        } catch (Exception e) {
            System.err.println("ML Init Failure: " + e.getMessage());
        }
    }

    private void addTrainingData(double amount, int hour, String status) {
        DenseInstance instance = new DenseInstance(3);
        instance.setDataset(dataStructure);
        instance.setValue(0, amount);
        instance.setValue(1, hour);
        instance.setValue(2, status);
        dataStructure.add(instance);
    }

    public double predictRisk(Transaction t) {
        try {
            DenseInstance newTxn = new DenseInstance(3);
            newTxn.setDataset(dataStructure);
            newTxn.setValue(0, t.getAmount());
            // Use the timestamp hour to drive the "Neural" logic
            newTxn.setValue(1, t.getTimestamp() != null ? t.getTimestamp().getHour() : 12);

            double[] probabilities = model.distributionForInstance(newTxn);

            return probabilities[1];

        } catch (Exception e) {
            return 0.15; 
        }
    }
}
