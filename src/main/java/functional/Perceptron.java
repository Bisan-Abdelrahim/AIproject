package functional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Perceptron {
    private final double[] weights;
    private final double learningRate;
    private final double threshold;
    private final List<Double> trainingAccuracyHistory = new ArrayList<>();
    private final List<Double> validationAccuracyHistory = new ArrayList<>();
    private final List<Double> trainingErrorHistory = new ArrayList<>();
    private int completedEpochs = 0;
    private boolean converged = false;

    public Perceptron(int inputSize, double learningRate, double threshold) {
        this.weights = new double[inputSize];
        this.learningRate = learningRate;
        this.threshold = threshold;
    }

    private int stepFunction(double value) {
        return value >= 0 ? 1 : 0;
    }

    public void initializeWeights() {
        Random rand = new Random(System.nanoTime());
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rand.nextDouble() - 0.5;
        }
        printWeights("Initial Weights");
    }

    private void printWeights(String label) {
        System.out.print(label + ": [");
        for (int i = 0; i < weights.length; i++) {
            System.out.print(String.format("%.6f", weights[i]));
            if (i < weights.length - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    public int predict(double[] inputs) {
        double sum = 0.0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * inputs[i];
        }
        return stepFunction(sum - threshold);
    }

    public void updateWeights(double[] inputs, int error) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] += learningRate * inputs[i] * error;
        }
    }

    public void train(List<Plant> trainingSet, List<Plant> validationSet, int maxEpochs) {
        trainingAccuracyHistory.clear();
        validationAccuracyHistory.clear();
        trainingErrorHistory.clear();
        completedEpochs = 0;
        converged = false;
        
        List<Plant> shuffledTraining = new ArrayList<>(trainingSet);

        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            completedEpochs = epoch + 1;
            // Shuffle training set each epoch
            Collections.shuffle(shuffledTraining);
            
            int totalError = 0;
            int correct = 0;

            // Train on shuffled training set
            for (Plant plant : shuffledTraining) {
                double[] inputs = plant.getInputs();
                int expected = plant.getExpectedOutput();

                int predicted = predict(inputs);
                plant.setPredictedOutput(predicted);

                int error = expected - predicted;
                totalError += Math.abs(error);
                
                if (error == 0) {
                    correct++;
                }

                updateWeights(inputs, error);
            }

            // Calculate training metrics
            double trainingAccuracy = (correct / (double) trainingSet.size()) * 100;
            double trainingError = totalError / (double) trainingSet.size();
            
            trainingAccuracyHistory.add(trainingAccuracy);
            trainingErrorHistory.add(trainingError);

            // Calculate validation accuracy
            double validationAccuracy = calculateValidationAccuracy(validationSet);
            validationAccuracyHistory.add(validationAccuracy);

            System.out.println("Epoch " + epoch + 
                             " | Train Accuracy: " + String.format("%.2f", trainingAccuracy) + "%" +
                             " | Val Accuracy: " + String.format("%.2f", validationAccuracy) + "%" +
                             " | Train Error: " + String.format("%.4f", trainingError));

            if (totalError == 0) {
                System.out.println("Converged at epoch: " + epoch);
                converged = true;
                break;
            }
        }
        
        printWeights("Final Weights");
        
        // Save the trained model
        DataGenerator.saveTrainedModel(weights);
    }

    private double calculateValidationAccuracy(List<Plant> validationSet) {
        int correct = 0;
        
        for (Plant plant : validationSet) {
            int predicted = predict(plant.getInputs());
            plant.setPredictedOutput(predicted);
            
            if (plant.getExpectedOutput() != null && predicted == plant.getExpectedOutput()) {
                correct++;
            }
        }
        
        return (correct / (double) validationSet.size()) * 100;
    }

    public double testModel(List<Plant> testSet) {
        int correct = 0;

        for (Plant plant : testSet) {
            int predicted = predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            if (plant.getExpectedOutput() != null && predicted == plant.getExpectedOutput()) {
                correct++;
            }
        }

        double accuracy = (correct / (double) testSet.size()) * 100;
        System.out.println("\n=== Test Results ===");
        System.out.println("Final Test Accuracy: " + String.format("%.2f", accuracy) + "%");
        System.out.println("Correct predictions: " + correct + " / " + testSet.size());
        System.out.println("====================\n");
        return accuracy;
    }

    public List<Double> getTrainingAccuracyHistory() {
        return trainingAccuracyHistory;
    }

    public List<Double> getValidationAccuracyHistory() {
        return validationAccuracyHistory;
    }

    public List<Double> getTrainingErrorHistory() {
        return trainingErrorHistory;
    }

    public List<Double> getLossHistory() {
        return trainingErrorHistory;
    }

    public List<Double> getAccuracyHistory() {
        return trainingAccuracyHistory;  // For backward compatibility
    }

    public double[] getWeights() {
        return weights;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getCompletedEpochs() {
        return completedEpochs;
    }

    public boolean isConverged() {
        return converged;
    }
}
