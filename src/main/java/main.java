
import java.util.List;

public class main {
    public static void main(String[] args) {

        // Step 1: Load plants from Excel
        List<Plant> allPlants = DataGenerator.loadFromExcel();

        if (allPlants.isEmpty()) {
            System.out.println("No plants were loaded. Check the Excel file path.");
            return;
        }

        // Step 2: Split data into training and validation
        List<Plant> trainingSet = DataGenerator.getTrainingSet(allPlants);
        List<Plant> validationSet = DataGenerator.getValidationSet(allPlants);

        System.out.println("Total plants loaded: " + allPlants.size());
        System.out.println("Training data size: " + trainingSet.size());
        System.out.println("Validation data size: " + validationSet.size());

        // Step 3: Create and train perceptron
        Perceptron perceptron = new Perceptron(3, 0.01, 0.5);
        perceptron.initializeWeights();

        System.out.println("\n--- Training Perceptron ---");
        perceptron.train(trainingSet, 100);

        // Step 4: Validate perceptron
        System.out.println("\n--- Validating Perceptron ---");
        double validationAccuracy = DataGenerator.validate(perceptron, validationSet);
        System.out.println("Final Validation Accuracy = " + validationAccuracy + "%");

        // Step 5: Predict all plants before SA
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

        // Step 6: Run Simulated Annealing
        int numberToSelect = 5; 

        SA sa = new SA(1000, 0.95, 500, 100, 50);

        System.out.println("\n--- Running Simulated Annealing ---");
        SA.Result result = sa.optimize(allPlants, numberToSelect);

        // Step 7: Print SA results
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

        // Step 8: Print SA steps log
        System.out.println("\n--- SA Steps Log ---");
        for (String log : result.getStepsLog()) {
            System.out.println(log);
        }

    }
}