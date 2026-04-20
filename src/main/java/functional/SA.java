package functional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SA {

    private final double initialTemperature;
    private final double coolingRate;
    private final int maxIterations;
    private final double missedPenalty;
    private final double extraPenalty;

    private final Random random = new Random();
    private final List<String> stepsLog = new ArrayList<>();

    public SA(double initialTemperature, double coolingRate, int maxIterations,
              double missedPenalty, double extraPenalty) {
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;
        this.missedPenalty = missedPenalty;
        this.extraPenalty = extraPenalty;
    }

    public static class Result {
        private final List<Plant> bestOrder;
        private final int selectedPlants;
        private final int completedIterations;
        private final double finalTemperature;
        private final double currentCost;
        private final double bestCost;
        private final double acceptanceRate;
        private final double totalDistance;
        private final int missedPlants;
        private final int extraWatering;
        private final double missedCostComponent;
        private final double distanceCostComponent;
        private final double extraCostComponent;
        private final List<String> stepsLog;
        private final List<Double> costHistory;
        private final List<Double> temperatureHistory;

        public Result(List<Plant> bestOrder, int selectedPlants, int completedIterations,
                      double finalTemperature, double currentCost, double bestCost,
                      double acceptanceRate,
                      double totalDistance, int missedPlants, int extraWatering,
                      double missedCostComponent, double distanceCostComponent,
                      double extraCostComponent, List<String> stepsLog,
                      List<Double> costHistory, List<Double> temperatureHistory) {
            this.bestOrder = bestOrder;
            this.selectedPlants = selectedPlants;
            this.completedIterations = completedIterations;
            this.acceptanceRate = acceptanceRate;
            this.finalTemperature = finalTemperature;
            this.currentCost = currentCost;
            this.bestCost = bestCost;
            this.totalDistance = totalDistance;
            this.missedPlants = missedPlants;
            this.extraWatering = extraWatering;
            this.missedCostComponent = missedCostComponent;
            this.distanceCostComponent = distanceCostComponent;
            this.extraCostComponent = extraCostComponent;
            this.stepsLog = stepsLog;
            this.costHistory = costHistory;
            this.temperatureHistory = temperatureHistory;
        }

        public List<Plant> getBestOrder() {
            return bestOrder;
        }

        public int getSelectedPlants() {
            return selectedPlants;
        }

        public int getCompletedIterations() {
            return completedIterations;
        }

        public double getAcceptanceRate() {
            return acceptanceRate;
        }

        public double getFinalTemperature() {
            return finalTemperature;
        }

        public double getCurrentCost() {
            return currentCost;
        }

        public double getBestCost() {
            return bestCost;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public int getMissedPlants() {
            return missedPlants;
        }

        public int getExtraWatering() {
            return extraWatering;
        }

        public double getMissedCostComponent() {
            return missedCostComponent;
        }

        public double getDistanceCostComponent() {
            return distanceCostComponent;
        }

        public double getExtraCostComponent() {
            return extraCostComponent;
        }

        public List<String> getStepsLog() {
            return stepsLog;
        }

        public List<Double> getCostHistory() {
            return costHistory;
        }

        public List<Double> getTemperatureHistory() {
            return temperatureHistory;
        }
    }

    public Result optimize(List<Plant> allPlants, int numberToSelect) {
        stepsLog.clear();
        List<Double> costHistory = new ArrayList<>();
        List<Double> temperatureHistory = new ArrayList<>();

        if (allPlants == null || allPlants.isEmpty()) {
            return new Result(new ArrayList<>(), 0, 0, 0.0, initialTemperature, 0,
                    0, 0, 0, 0, 0, 0, 0, new ArrayList<>(),
                    costHistory, temperatureHistory);
        }

        if (numberToSelect <= 0) {
            numberToSelect = 1;
        }

        if (numberToSelect > allPlants.size()) {
            numberToSelect = allPlants.size();
        }

        List<Plant> currentSolution = generateRandomSolution(allPlants, numberToSelect);
        List<Plant> bestSolution = new ArrayList<>(currentSolution);

        double currentCost = calculateCost(currentSolution, allPlants);
        double bestCost = currentCost;
        int acceptedMoves = 0;

        double temperature = initialTemperature;
        int completedIterations = 0;

        stepsLog.add(String.format("Initial solution: selected=%d | cost=%.4f", currentSolution.size(), currentCost));
        stepsLog.add(formatCostBreakdown(calculateCostBreakdown(currentSolution, allPlants)));
        costHistory.add(currentCost);
        temperatureHistory.add(temperature);

        for (int i = 0; i < maxIterations; i++) {
            completedIterations = i + 1;
            List<Plant> nextSolution = generateNeighbor(currentSolution, allPlants);
            double nextCost = calculateCost(nextSolution, allPlants);

            double delta = nextCost - currentCost;

            if (delta < 0) {
                currentSolution = nextSolution;
                currentCost = nextCost;
                acceptedMoves++;
                stepsLog.add("Iteration " + (i + 1) + ": Better solution accepted. Cost = " + currentCost);
            } else {
                double probability = Math.exp(-delta / temperature);
                double randValue = random.nextDouble();

                if (randValue < probability) {
                    currentSolution = nextSolution;
                    currentCost = nextCost;
                    acceptedMoves++;
                    stepsLog.add("Iteration " + (i + 1) + ": Worse solution accepted by probability. Cost = " + currentCost);
                } else {
                    stepsLog.add("Iteration " + (i + 1) + ": Solution rejected.");
                }
            }

            if (currentCost < bestCost) {
                bestSolution = new ArrayList<>(currentSolution);
                bestCost = currentCost;
                stepsLog.add("Iteration " + (i + 1) + ": New best solution found. Best cost = " + bestCost);
            }

            CostBreakdown currentBreakdown = calculateCostBreakdown(currentSolution, allPlants);
            stepsLog.add(String.format(
                    "Iteration %d | Temp=%.4f | CurrentCost=%.4f | BestCost=%.4f | Missed=%d | Distance=%.2f | Extra=%d",
                    (i + 1), temperature, currentCost, bestCost,
                    currentBreakdown.missedPlants, currentBreakdown.totalDistance, currentBreakdown.extraWatering));

                costHistory.add(currentCost);
                temperatureHistory.add(temperature);

            temperature *= coolingRate;

            if (temperature < 0.0001) {
                stepsLog.add("Stopped early because temperature became very low.");
                break;
            }
        }

        CostBreakdown bestBreakdown = calculateCostBreakdown(bestSolution, allPlants);
    double acceptanceRate = completedIterations == 0 ? 0.0 :
        (acceptedMoves * 100.0) / completedIterations;

        return new Result(
                bestSolution,
                bestSolution.size(),
                completedIterations,
        acceptanceRate,
                temperature,
                currentCost,
                bestCost,
                bestBreakdown.totalDistance,
                bestBreakdown.missedPlants,
                bestBreakdown.extraWatering,
                bestBreakdown.missedCost,
                bestBreakdown.distanceCost,
                bestBreakdown.extraCost,
                new ArrayList<>(stepsLog),
                new ArrayList<>(costHistory),
                new ArrayList<>(temperatureHistory)
        );
    }

    private List<Plant> generateRandomSolution(List<Plant> allPlants, int numberToSelect) {
        List<Plant> shuffled = new ArrayList<>(allPlants);
        Collections.shuffle(shuffled, random);
        return new ArrayList<>(shuffled.subList(0, numberToSelect));
    }

    private List<Plant> generateNeighbor(List<Plant> currentSolution, List<Plant> allPlants) {
        List<Plant> neighbor = new ArrayList<>(currentSolution);

        if (neighbor.size() <= 1) {
            return neighbor;
        }

        // Always perform a swap so the ordered route changes.
        int i = random.nextInt(neighbor.size());
        int j = random.nextInt(neighbor.size());
        while (j == i) {
            j = random.nextInt(neighbor.size());
        }
        Collections.swap(neighbor, i, j);

        // Optionally replace one selected plant with another garden plant.
        if (random.nextDouble() < 0.35) {
            List<Plant> outsidePlants = new ArrayList<>();
            for (Plant p : allPlants) {
                if (!containsPlant(neighbor, p)) {
                    outsidePlants.add(p);
                }
            }

            if (!outsidePlants.isEmpty()) {
                int indexToReplace = random.nextInt(neighbor.size());
                Plant replacement = outsidePlants.get(random.nextInt(outsidePlants.size()));
                neighbor.set(indexToReplace, replacement);
            }
        }

        return neighbor;
    }

    private boolean containsPlant(List<Plant> plants, Plant target) {
        for (Plant p : plants) {
            if (p.getPlantId() == target.getPlantId()) {
                return true;
            }
        }
        return false;
    }

    private double calculateCost(List<Plant> solution, List<Plant> allPlants) {
        CostBreakdown breakdown = calculateCostBreakdown(solution, allPlants);
        return breakdown.totalCost;
    }

    private CostBreakdown calculateCostBreakdown(List<Plant> solution, List<Plant> allPlants) {
        int missed = countMissedPlants(solution, allPlants);
        int extra = countExtraWatering(solution);
        double distance = calculateTotalDistance(solution);

        // Requested objective: cost = missed plants + total distance + extra watering.
        double missedCost = missed;
        double distanceCost = distance;
        double extraCost = extra;
        double totalCost = missedCost + distanceCost + extraCost;

        return new CostBreakdown(missed, extra, distance, missedCost, distanceCost, extraCost, totalCost);
    }

    private String formatCostBreakdown(CostBreakdown breakdown) {
        return String.format(
                "Cost breakdown -> missed=%d (%.2f) + distance=%.2f + extra=%d (%.2f) = %.4f",
                breakdown.missedPlants,
                breakdown.missedCost,
                breakdown.distanceCost,
                breakdown.extraWatering,
                breakdown.extraCost,
                breakdown.totalCost
        );
    }

    private int countMissedPlants(List<Plant> solution, List<Plant> allPlants) {
        int missed = 0;

        for (Plant plant : allPlants) {
            int decision = getWaterDecision(plant);
            if (decision == 1 && !containsPlant(solution, plant)) {
                missed++;
            }
        }

        return missed;
    }

    private int countExtraWatering(List<Plant> solution) {
        int extra = 0;
        for (Plant plant : solution) {
            int decision = getWaterDecision(plant);
            if (decision == 0) {
                extra++;
            }
        }
        return extra;
    }

    private int getWaterDecision(Plant plant) {
        if (plant.getPredictedOutput() != null) {
            return plant.getPredictedOutput();
        }
        if (plant.getExpectedOutput() != null) {
            return plant.getExpectedOutput();
        }
        return 0;
    }

    private double calculateTotalDistance(List<Plant> solution) {
        if (solution.size() < 2) {
            return 0;
        }

        double totalDistance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            Plant p1 = solution.get(i);
            Plant p2 = solution.get(i + 1);
            totalDistance += distance(p1, p2);
        }
        return totalDistance;
    }

    private double distance(Plant p1, Plant p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public List<String> getStepsLog() {
        return stepsLog;
    }

    private static class CostBreakdown {
        private final int missedPlants;
        private final int extraWatering;
        private final double totalDistance;
        private final double missedCost;
        private final double distanceCost;
        private final double extraCost;
        private final double totalCost;

        private CostBreakdown(int missedPlants, int extraWatering, double totalDistance,
                              double missedCost, double distanceCost, double extraCost, double totalCost) {
            this.missedPlants = missedPlants;
            this.extraWatering = extraWatering;
            this.totalDistance = totalDistance;
            this.missedCost = missedCost;
            this.distanceCost = distanceCost;
            this.extraCost = extraCost;
            this.totalCost = totalCost;
        }
    }
}
