
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Perceptron {
    private final double[] weights;
    private final double learningRate;
    private final double threshold;
    private final List<Double> accuracyHistory = new ArrayList<>();

    public Perceptron(int inputSize, double learningRate, double threshold) {
        this.weights = new double[inputSize];
        this.learningRate = learningRate;
        this.threshold = threshold;
    }

    private int stepFunction(double value) {
        return value >= 0 ? 1 : 0;
    }

public void initializeWeights() {
    Random rand = new Random();
    for (int i = 0; i < weights.length; i++) {
        weights[i] = rand.nextDouble() - 0.5; // range -0.5 to 0.5
    }
}

    public int predict(double[] inputs) {
    double sum = 0.0;
    for (int i = 0; i < weights.length; i++) {
        sum += weights[i] * inputs[i];
    }
    return stepFunction(sum - threshold); // subtract threshold here
}

    

   public void updateWeights(double[] inputs, int error) {
    for (int i = 0; i < weights.length; i++) {
        weights[i] += learningRate * inputs[i] * error;
    }
}

public void train(List<Plant> plants, int maxEpochs) {
    accuracyHistory.clear();

    for (int epoch = 0; epoch < maxEpochs; epoch++) {
        int totalError = 0;

        for (Plant plant : plants) {
            double[] inputs = plant.getInputs();
            int expected = plant.getExpectedOutput();

            // Step 2: Activation
            int predicted = predict(inputs);
            plant.setPredictedOutput(predicted);

            // Step 3: Weight Training
            int error = expected - predicted;
            totalError += Math.abs(error);
            updateWeights(inputs, error);
        }

        // calculate and save accuracy for this epoch
        double accuracy = (plants.size() - totalError) / (double) plants.size() * 100;
        accuracyHistory.add(accuracy);
        System.out.println("Epoch " + epoch + " | Accuracy: " + accuracy + "%");

        // Step 4: Iteration - stop if converged
        if (totalError == 0) {
            System.out.println("Converged at epoch: " + epoch);
            break;
        }
    }
}

public List<Double> getAccuracyHistory() {
    return accuracyHistory;
}

public double[] getWeights() {
    return weights;
}

public double getThreshold() {
    return threshold;
}



    
}