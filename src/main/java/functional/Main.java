package functional;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<Plant> allPlants = DataGenerator.loadFromExcel();
        DataGenerator.shuffleDataset(allPlants);

        if (allPlants.isEmpty()) {
            System.out.println("No plants were loaded. Check the Excel file path.");
            return;
        }

        List<Plant> trainingSet = DataGenerator.getTrainingSet(allPlants);
        List<Plant> validationSet = DataGenerator.getValidationSet(allPlants);
        List<Plant> testSet = DataGenerator.getTestSet(allPlants);

        DataGenerator.printDatasetSizes(allPlants, trainingSet, validationSet, testSet);

        // Calculate normalization stats from training set
        DataGenerator.calculateNormalizationStats(trainingSet);
        
        // Normalize all three sets
        DataGenerator.normalizeDataset(trainingSet);
        DataGenerator.normalizeDataset(validationSet);
        DataGenerator.normalizeDataset(testSet);

        Perceptron perceptron = new Perceptron(3, 0.01, 0.5);
        perceptron.initializeWeights();

        System.out.println("\n--- Training Perceptron ---");
        perceptron.train(trainingSet, validationSet, 100);

        System.out.println("\n--- Testing Perceptron ---");
        double testAccuracy = perceptron.testModel(testSet);

        System.out.println("\n--- Confusion Matrix ---");
        DataGenerator.printConfusionMatrix(perceptron, testSet);

        System.out.println("\n--- Predicting all plants ---");
        for (Plant plant : allPlants) {
            int prediction = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(prediction);

            System.out.println("Plant ID: " + plant.getPlantId()
                    + " | Moisture: " + plant.getMoisture()
                    + " | Hours: " + plant.getHoursSinceWatering()
                    + " | Type: " + plant.getType()
                    + " | Predicted Needs Water: " + prediction);
        }

        int numberToSelect = 5;

        SA sa = new SA(1000, 0.95, 500, 100, 50);

        System.out.println("\n--- Running Simulated Annealing ---");
        SA.Result result = sa.optimize(allPlants, numberToSelect);

        System.out.println("\n=== SA RESULT ===");
        System.out.println("Best Cost: " + result.getBestCost());
        System.out.println("Total Distance: " + result.getTotalDistance());
        System.out.println("Missed Plants: " + result.getMissedPlants());
        System.out.println("Extra Watering: " + result.getExtraWatering());

        System.out.println("\nOptimized Watering Order:");
        int step = 1;
        for (Plant plant : result.getBestOrder()) {
            System.out.println("Step " + step++
                    + " -> Plant ID: " + plant.getPlantId()
                    + " (x=" + plant.getX() + ", y=" + plant.getY() + ")"
                    + " | Predicted Needs Water: " + plant.getPredictedOutput());
        }

        System.out.println("\n--- SA Steps Log ---");
        for (String log : result.getStepsLog()) {
            System.out.println(log);
        }
    }
}
