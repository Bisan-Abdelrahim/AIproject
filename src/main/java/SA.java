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
        private final double bestCost;
        private final double totalDistance;
        private final int missedPlants;
        private final int extraWatering;
        private final List<String> stepsLog;

        public Result(List<Plant> bestOrder, double bestCost, double totalDistance,
                      int missedPlants, int extraWatering, List<String> stepsLog) {
            this.bestOrder = bestOrder;
            this.bestCost = bestCost;
            this.totalDistance = totalDistance;
            this.missedPlants = missedPlants;
            this.extraWatering = extraWatering;
            this.stepsLog = stepsLog;
        }

        public List<Plant> getBestOrder() {
            return bestOrder;
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

        public List<String> getStepsLog() {
            return stepsLog;
        }
    }

    public Result optimize(List<Plant> allPlants, int numberToSelect) {
        stepsLog.clear();

        if (allPlants == null || allPlants.isEmpty()) {
            return new Result(new ArrayList<>(), 0, 0, 0, 0, new ArrayList<>());
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

        double temperature = initialTemperature;

        stepsLog.add("Initial solution cost = " + currentCost);

        for (int i = 0; i < maxIterations; i++) {
            List<Plant> nextSolution = generateNeighbor(currentSolution, allPlants);
            double nextCost = calculateCost(nextSolution, allPlants);

            double delta = nextCost - currentCost;

            if (delta < 0) {
                currentSolution = nextSolution;
                currentCost = nextCost;
                stepsLog.add("Iteration " + (i + 1) + ": Better solution accepted. Cost = " + currentCost);
            } else {
                double probability = Math.exp(-delta / temperature);
                double randValue = random.nextDouble();

                if (randValue < probability) {
                    currentSolution = nextSolution;
                    currentCost = nextCost;
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

            temperature *= coolingRate;

            if (temperature < 0.0001) {
                stepsLog.add("Stopped early because temperature became very low.");
                break;
            }
        }

        double totalDistance = calculateTotalDistance(bestSolution);
        int missedPlants = countMissedPlants(bestSolution, allPlants);
        int extraWatering = countExtraWatering(bestSolution);

        return new Result(
                bestSolution,
                bestCost,
                totalDistance,
                missedPlants,
                extraWatering,
                new ArrayList<>(stepsLog)
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

        // 50% swap, 50% replace
        if (random.nextBoolean()) {
            // Swap two plants in the current solution
            int i = random.nextInt(neighbor.size());
            int j = random.nextInt(neighbor.size());

            while (j == i) {
                j = random.nextInt(neighbor.size());
            }

            Collections.swap(neighbor, i, j);
        } else {
            // Replace one plant with another from outside the solution
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
        int missed = countMissedPlants(solution, allPlants);
        int extra = countExtraWatering(solution);
        double distance = calculateTotalDistance(solution);

        return (missedPenalty * missed) + distance + (extraPenalty * extra);
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
}