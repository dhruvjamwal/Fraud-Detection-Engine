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

            Attribute amountAttr = new Attribute("amount");
            Attribute hourAttr = new Attribute("hour");
            Attribute fraudClass = new Attribute("isFraud", Arrays.asList("safe", "fraud"));

            attributes = new ArrayList<>();
            attributes.add(amountAttr);
            attributes.add(hourAttr);
            attributes.add(fraudClass);

            dataStructure = new Instances("TransactionData", attributes, 100);
            dataStructure.setClassIndex(2); 

            for (int i = 0; i < 15; i++) {
                addTrainingData(15.0, 4, "fraud");
            }

            for (int i = 0; i < 30; i++) {
                addTrainingData(5000.0, 14, "safe");
            }

            for (int i = 0; i < 50; i++) {
                addTrainingData(10.0 + (Math.random() * 500), 12, "safe");
            }
            for (int i = 0; i < 20; i++) {
                addTrainingData(20000.0 + (Math.random() * 50000), 3, "fraud");
            }

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

    public double predictRisk(Transaction t) {
        try {
            DenseInstance newTxn = new DenseInstance(3);
            newTxn.setValue(attributes.get(0), t.getAmount());
            newTxn.setValue(attributes.get(1), t.getTimestamp().getHour());
            newTxn.setDataset(dataStructure); // Connect to structure

            double[] probabilities = model.distributionForInstance(newTxn);

            return probabilities[1];

        } catch (Exception e) {
            System.err.println("ML Prediction Error: " + e.getMessage());
            return 0.0; 
        }
    }
}
