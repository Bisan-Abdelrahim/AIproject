package functional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataGenerator {

    private static final Path FILE_PATH = Paths.get("Data.xlsx").toAbsolutePath();
    
    // Normalization stats
    private static double minMoisture = Double.MAX_VALUE;
    private static double maxMoisture = Double.MIN_VALUE;
    private static double minHours = Double.MAX_VALUE;
    private static double maxHours = Double.MIN_VALUE;
    private static boolean normalizationCalculated = false;
    
    // Trained model state
    private static double[] trainedWeights = null;
    private static boolean isModelTrained = false;

    public static List<Plant> loadFromExcel() {
        List<Plant> plants = new ArrayList<>();
        Random rand = new Random();
        int id = 0;

        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(FILE_PATH.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                if (row == null) {
                    continue;
                }

                Cell moistureCell = row.getCell(0);
                Cell hoursCell = row.getCell(1);
                Cell typeCell = row.getCell(2);
                Cell labelCell = row.getCell(3);

                if (moistureCell == null || hoursCell == null || typeCell == null || labelCell == null) {
                    continue;
                }

                double moisture = parseNumericCell(moistureCell, formatter);
                double hoursSinceWater = parseNumericCell(hoursCell, formatter);
                int plantType = (int) parseNumericCell(typeCell, formatter);
                int needsWater = (int) parseNumericCell(labelCell, formatter);

                double x = rand.nextInt(500);
                double y = rand.nextInt(500);

                plants.add(new Plant(id++, moisture, hoursSinceWater,
                        plantType, needsWater, x, y));
            }

        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
            System.err.println("Expected Excel path: " + FILE_PATH);
        }

        System.out.println("Loaded " + plants.size() + " plants from Excel file.");
        return plants;
    }

    public static void shuffleDataset(List<Plant> plants) {
        Collections.shuffle(plants);
    }

    private static double parseNumericCell(Cell cell, DataFormatter formatter) {
        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            String text = formatter.formatCellValue(cell);
            return Double.parseDouble(text.trim());
        }
    }

    public static Plant addPlant(List<Plant> plants,
                                 double moisture,
                                 double hoursSinceWatering,
                                 int plantType,
                                 double x,
                                 double y) {
        int newId = plants.size();
        Plant newPlant = new Plant(newId, moisture, hoursSinceWatering,
                plantType, null, x, y);
        plants.add(newPlant);
        System.out.println("Added new plant: " + newPlant);
        return newPlant;
    }

    public static List<Plant> getTrainingSet(List<Plant> plants) {
        int split = (plants.size() * 70) / 100;
        return new ArrayList<>(plants.subList(0, split));
    }

    public static List<Plant> getValidationSet(List<Plant> plants) {
        int trainingSize = (plants.size() * 70) / 100;
        int validationEnd = trainingSize + (plants.size() * 15) / 100;
        return new ArrayList<>(plants.subList(trainingSize, validationEnd));
    }

    public static List<Plant> getTestSet(List<Plant> plants) {
        int trainingSize = (plants.size() * 70) / 100;
        int validationEnd = trainingSize + (plants.size() * 15) / 100;
        return new ArrayList<>(plants.subList(validationEnd, plants.size()));
    }

    public static void printDatasetSizes(List<Plant> allPlants, List<Plant> trainingSet, 
                                          List<Plant> validationSet, List<Plant> testSet) {
        System.out.println("\n=== Dataset Sizes ===");
        System.out.println("Total plants: " + allPlants.size());
        System.out.println("Training set: " + trainingSet.size() + " (" + 
                           (trainingSet.size() * 100 / allPlants.size()) + "%)");
        System.out.println("Validation set: " + validationSet.size() + " (" + 
                           (validationSet.size() * 100 / allPlants.size()) + "%)");
        System.out.println("Test set: " + testSet.size() + " (" + 
                           (testSet.size() * 100 / allPlants.size()) + "%)");
        System.out.println("====================\n");
    }

    public static double calculateAccuracy(Perceptron perceptron, List<Plant> plants) {
        int correct = 0;

        for (Plant plant : plants) {
            int predicted = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            if (plant.getExpectedOutput() != null && predicted == plant.getExpectedOutput()) {
                correct++;
            }
        }

        double accuracy = (correct / (double) plants.size()) * 100;
        return accuracy;
    }

    public static void printConfusionMatrix(Perceptron perceptron, List<Plant> testSet) {
        int tp = 0, tn = 0, fp = 0, fn = 0;

        for (Plant plant : testSet) {
            int predicted = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            int expected = (plant.getExpectedOutput() != null) ? plant.getExpectedOutput() : -1;

            if (predicted == 1 && expected == 1) {
                tp++;
            } else if (predicted == 0 && expected == 0) {
                tn++;
            } else if (predicted == 1 && expected == 0) {
                fp++;
            } else if (predicted == 0 && expected == 1) {
                fn++;
            }
        }

        System.out.println("\n=== Test Set Confusion Matrix ===");
        System.out.println("True Positives (TP):  " + tp);
        System.out.println("True Negatives (TN):  " + tn);
        System.out.println("False Positives (FP): " + fp);
        System.out.println("False Negatives (FN): " + fn);
        System.out.println("=================================\n");
    }

    public static int[] getConfusionMatrix(Perceptron perceptron, List<Plant> testSet) {
        int tp = 0, tn = 0, fp = 0, fn = 0;

        for (Plant plant : testSet) {
            int predicted = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            int expected = (plant.getExpectedOutput() != null) ? plant.getExpectedOutput() : -1;

            if (predicted == 1 && expected == 1) {
                tp++;
            } else if (predicted == 0 && expected == 0) {
                tn++;
            } else if (predicted == 1 && expected == 0) {
                fp++;
            } else if (predicted == 0 && expected == 1) {
                fn++;
            }
        }

        return new int[]{tp, tn, fp, fn};
    }

    public static void calculateNormalizationStats(List<Plant> trainingSet) {
        minMoisture = Double.MAX_VALUE;
        maxMoisture = Double.MIN_VALUE;
        minHours = Double.MAX_VALUE;
        maxHours = Double.MIN_VALUE;

        for (Plant plant : trainingSet) {
            double moisture = plant.getMoisture();
            double hours = plant.getHoursSinceWatering();

            if (moisture < minMoisture) minMoisture = moisture;
            if (moisture > maxMoisture) maxMoisture = moisture;
            if (hours < minHours) minHours = hours;
            if (hours > maxHours) maxHours = hours;
        }

        normalizationCalculated = true;
        System.out.println("\n=== Normalization Stats Calculated ===");
        System.out.println("Moisture range: [" + String.format("%.2f", minMoisture) + ", " + 
                           String.format("%.2f", maxMoisture) + "]");
        System.out.println("Hours range: [" + String.format("%.2f", minHours) + ", " + 
                           String.format("%.2f", maxHours) + "]");
        System.out.println("========================================\n");
    }

    public static void normalizeDataset(List<Plant> plants) {
        if (!normalizationCalculated) {
            System.err.println("Warning: Normalization stats not calculated. Call calculateNormalizationStats first.");
            return;
        }

        for (Plant plant : plants) {
            normalizePlant(plant);
        }
    }

    private static void normalizePlant(Plant plant) {
        double normalizedMoisture = normalizeValue(plant.getMoisture(), minMoisture, maxMoisture);
        double normalizedHours = normalizeValue(plant.getHoursSinceWatering(), minHours, maxHours);
        
        plant.setNormalizedMoisture(normalizedMoisture);
        plant.setNormalizedHours(normalizedHours);
    }

    private static double normalizeValue(double value, double min, double max) {
        if (max == min) {
            return 0.0;  // Avoid division by zero
        }
        return (value - min) / (max - min);
    }

    public static double getMinMoisture() {
        return minMoisture;
    }

    public static double getMaxMoisture() {
        return maxMoisture;
    }

    public static double getMinHours() {
        return minHours;
    }

    public static double getMaxHours() {
        return maxHours;
    }

    public static double validate(Perceptron perceptron, List<Plant> validationSet) {
        int correct = 0;

        for (Plant plant : validationSet) {
            int predicted = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            if (plant.getExpectedOutput() != null && predicted == plant.getExpectedOutput()) {
                correct++;
            }
        }

        double accuracy = (correct / (double) validationSet.size()) * 100;
        System.out.println("Validation Accuracy: " + accuracy + "%");
        return accuracy;
    }

    public static void saveToExcel(List<Plant> plants) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Plants");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("soil_moisture");
        header.createCell(1).setCellValue("last_watered");
        header.createCell(2).setCellValue("plant_type");
        header.createCell(3).setCellValue("needs_water");

        int rowIndex = 1;
        for (Plant p : plants) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(p.getMoisture());
            row.createCell(1).setCellValue(p.getHoursSinceWatering());
            row.createCell(2).setCellValue(p.getType());

            int label;
            if (p.getExpectedOutput() != null) {
                label = p.getExpectedOutput();
            } else if (p.getPredictedOutput() != null) {
                label = p.getPredictedOutput();
            } else {
                label = -1;
            }

            row.createCell(3).setCellValue(label);
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH.toFile())) {
            workbook.write(fos);
            workbook.close();
            System.out.println("Saved " + plants.size() + " plants to Excel file.");
        } catch (IOException e) {
            System.err.println("Error saving Excel file: " + e.getMessage());
        }
    }

    // Model state management
    public static void saveTrainedModel(double[] weights) {
        trainedWeights = weights.clone();
        isModelTrained = true;
        System.out.println("Model saved with " + weights.length + " weights.");
    }

    public static double[] getTrainedWeights() {
        return trainedWeights != null ? trainedWeights.clone() : null;
    }

    public static boolean isModelTrained() {
        return isModelTrained;
    }

    public static void resetModel() {
        trainedWeights = null;
        isModelTrained = false;
        normalizationCalculated = false;
        System.out.println("Model reset.");
    }

    public static void printModelStatus() {
        if (isModelTrained) {
            System.out.println("\n=== Model Status ===");
            System.out.println("Status: TRAINED");
            System.out.println("Weights: " + (trainedWeights != null ? trainedWeights.length + " weights" : "None"));
            System.out.println("Normalization: " + (normalizationCalculated ? "Calculated" : "Not calculated"));
            System.out.println("====================\n");
        } else {
            System.out.println("\nModel Status: NOT TRAINED YET\n");
        }
    }
}

