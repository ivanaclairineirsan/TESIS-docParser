/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ivana Clairine
 */
public class PerformanceCalculator {

    Map<Integer, ArrayList<String>> predicted;
    Map<Integer, ArrayList<String>> target;
    int dataTestSize;

    public PerformanceCalculator() {
        predicted = new HashMap<>();
        target = new HashMap<>();
    }

    public void readResultFile(String filePath) {
        int counter = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            
            while (line != null) {
                System.out.println("line: " + line);
                String[] splited = line.split(" ");
                if (splited[0].equals("=")) {
                    ArrayList<String> targetClass = new ArrayList<>();
                    for (int i = 1; i < splited.length; i++) {
                        targetClass.add(splited[i]);
                    }
                    System.out.println("put target, counter: " + counter);
                    target.put(counter, targetClass);
                }
                if (splited[0].equals("<")) {
                    ArrayList<String> predictedClass = new ArrayList<>();
                    for (int i = 1; i < splited.length; i++) {
                        predictedClass.add(splited[i]);
                    }
                    System.out.println("put predicted, counter: " + counter);
                    predicted.put(counter, predictedClass);
                    counter++;
                }
                line = br.readLine();
            }
            dataTestSize = counter;
        } 
            catch (Exception e) {
                System.out.println(e);
        }
    }
    
    public double countIrisan(int i) {
        ArrayList<String> predictedClass = predicted.get(i);
        ArrayList<String> targetClass = target.get(i);
        double counter = 0.0;
        for(int x=0; x<predictedClass.size(); x++) {
            for(int y=0; y<targetClass.size(); y++) {
                if(predictedClass.get(x).equals(targetClass.get(y))) {
                    counter += 1.0;
                    break;
                }
            }
        }
        return counter;
    }
    
    public double countPrecision() {
        double totalIrisan = 0.0;
        double totalPredicted = 0.0;
        for(int i=0; i<dataTestSize; i++) {
            totalIrisan += countIrisan(i);
            totalPredicted += predicted.get(i).size();
        }
        System.out.println("totalIrisan: " + totalIrisan);
        System.out.println("totalpredicted: " + totalPredicted);
        return totalIrisan/totalPredicted;
    }
    
    public double countRecall() {
         double totalIrisan = 0.0;
        double totalTarget = 0.0;
        for(int i=0; i<dataTestSize; i++) {
            totalIrisan += countIrisan(i);
            totalTarget += target.get(i).size();
        }
        return totalIrisan/totalTarget;
    }
    
    public double countF1Macro() {
        double precision = countPrecision();
        double recall = countRecall();
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        return (2 * precision * recall) / (precision + recall);
    }
    
    public double countF1Micro() {
        double totalF1 = 0.0;
        for(int i=0; i<dataTestSize; i++) {
            double precision = countIrisan(i)/predicted.get(i).size();
            double recall = countIrisan(i)/target.get(i).size();
//            System.out.println("precisionMicro: " + precision);
//            System.out.println("recallMicro: " + recall);
            if(precision+recall == 0) totalF1 += 0.0;
            else totalF1 += ((2*precision*recall)/(precision+recall));
        }
//        System.out.println("totalF1: " + totalF1);
        return totalF1/dataTestSize;
    }

    public static void main(String[] args) {
        PerformanceCalculator pc = new PerformanceCalculator();
        pc.readResultFile("C:/Users/Ivana Clairine/Dropbox/S2/TESIS/Hasil Eksperimen/Multilabel-5000 Iter-677Train-TestTrain.txt");
        System.out.println("F1-Measure Macro: " + pc.countF1Macro());
        System.out.println("F1-Measure Micro: " + pc.countF1Micro());
    }
}
