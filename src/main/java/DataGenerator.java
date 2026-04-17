import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataGenerator {

    private static final String FILE_PATH =
            "C:\\Users\\hp\\OneDrive\\Desktop\\AI\\Coding-project\\Data.xlsx";

    public static List<Plant> loadFromExcel() {
        List<Plant> plants = new ArrayList<>();
        Random rand = new Random();
        int id = 0;

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // skip header
                    continue;
                }

                if (row == null) continue;

                Cell moistureCell = row.getCell(0);
                Cell hoursCell = row.getCell(1);
                Cell typeCell = row.getCell(2);
                Cell labelCell = row.getCell(3);

                if (moistureCell == null || hoursCell == null || typeCell == null || labelCell == null) {
                    continue;
                }

                double moisture = moistureCell.getNumericCellValue();
                double hoursSinceWater = hoursCell.getNumericCellValue();
                int plantType = (int) typeCell.getNumericCellValue();
                int needsWater = (int) labelCell.getNumericCellValue();

                double x = rand.nextInt(500);
                double y = rand.nextInt(500);

                plants.add(new Plant(id++, moisture, hoursSinceWater,
                        plantType, needsWater, x, y));
            }

        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }

        System.out.println("Loaded " + plants.size() + " plants from Excel file.");
        return plants;
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
        int split = (plants.size() * 80) / 100;
        return new ArrayList<>(plants.subList(0, split));
    }

    public static List<Plant> getValidationSet(List<Plant> plants) {
        int split = (plants.size() * 80) / 100;
        return new ArrayList<>(plants.subList(split, plants.size()));
    }

    public static double validate(Perceptron perceptron, List<Plant> validationSet) {
        int correct = 0;

        for (Plant plant : validationSet) {
            int predicted = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(predicted);

            if (plant.getExpectedOutput() != null &&
                    predicted == plant.getExpectedOutput()) {
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

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            workbook.write(fos);
            workbook.close();
            System.out.println("Saved " + plants.size() + " plants to Excel file.");
        } catch (IOException e) {
            System.err.println("Error saving Excel file: " + e.getMessage());
        }
    }
}