import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GardenGUI extends JFrame {

    private List<Plant> allPlants = new ArrayList<>();
    private Perceptron perceptron;

    private final DefaultTableModel tableModel;
    private final JTable plantTable;
    private final JTextArea outputArea;

    private final JTextField moistureField;
    private final JTextField hoursField;
    private final JTextField typeField;
    private final JTextField xField;
    private final JTextField yField;
    private final JTextField numberToSelectField;

    private final JLabel accuracyLabel;

    public GardenGUI() {
        setTitle("Smart Plant Watering Scheduler");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("Load Data");
        JButton trainButton = new JButton("Train Perceptron");
        JButton predictButton = new JButton("Predict All");
        JButton runSAButton = new JButton("Run SA");
        JButton addPlantButton = new JButton("Add Plant");

        buttonPanel.add(loadButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(runSAButton);
        buttonPanel.add(addPlantButton);

        JPanel inputPanel = new JPanel();
        moistureField = new JTextField(5);
        hoursField = new JTextField(5);
        typeField = new JTextField(5);
        xField = new JTextField(5);
        yField = new JTextField(5);
        numberToSelectField = new JTextField("5", 5);

        inputPanel.add(new JLabel("Moisture:"));
        inputPanel.add(moistureField);
        inputPanel.add(new JLabel("Hours:"));
        inputPanel.add(hoursField);
        inputPanel.add(new JLabel("Type (0/1/2):"));
        inputPanel.add(typeField);
        inputPanel.add(new JLabel("X:"));
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Y:"));
        inputPanel.add(yField);
        inputPanel.add(new JLabel("Plants to Select:"));
        inputPanel.add(numberToSelectField);

        topPanel.add(buttonPanel);
        topPanel.add(inputPanel);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Moisture", "Hours", "Type", "Expected", "Predicted", "X", "Y"};
        tableModel = new DefaultTableModel(columns, 0);
        plantTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(plantTable);

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        accuracyLabel = new JLabel("Validation Accuracy: Not calculated yet");
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        rightPanel.add(accuracyLabel, BorderLayout.NORTH);
        rightPanel.add(outputScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, rightPanel);
        splitPane.setDividerLocation(650);

        add(splitPane, BorderLayout.CENTER);

        // Button actions
        loadButton.addActionListener(e -> loadData());
        trainButton.addActionListener(e -> trainPerceptron());
        predictButton.addActionListener(e -> predictAllPlants());
        runSAButton.addActionListener(e -> runSA());
        addPlantButton.addActionListener(e -> addPlant());

        setVisible(true);
    }

    private void loadData() {
        allPlants = DataGenerator.loadFromExcel();
        refreshTable();
        outputArea.setText("Data loaded successfully.\nTotal plants: " + allPlants.size());
    }

    private void trainPerceptron() {
        if (allPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load data first.");
            return;
        }

        List<Plant> trainingSet = DataGenerator.getTrainingSet(allPlants);
        List<Plant> validationSet = DataGenerator.getValidationSet(allPlants);

        perceptron = new Perceptron(3, 0.01, 0.5);
        perceptron.initializeWeights();
        perceptron.train(trainingSet, 100);

        double accuracy = DataGenerator.validate(perceptron, validationSet);
        accuracyLabel.setText("Validation Accuracy: " + accuracy + "%");

        outputArea.setText("Perceptron trained successfully.\n");
        outputArea.append("Validation Accuracy = " + accuracy + "%\n\n");
        outputArea.append("Accuracy History:\n");

        List<Double> history = perceptron.getAccuracyHistory();
        for (int i = 0; i < history.size(); i++) {
            outputArea.append("Epoch " + i + ": " + history.get(i) + "%\n");
        }

        refreshTable();
    }

    private void predictAllPlants() {
        if (perceptron == null) {
            JOptionPane.showMessageDialog(this, "Please train the perceptron first.");
            return;
        }

        outputArea.setText("Predictions for all plants:\n\n");

        for (Plant plant : allPlants) {
            int prediction = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(prediction);

            outputArea.append("Plant ID: " + plant.getPlantId()
                    + " | Predicted Needs Water: " + prediction + "\n");
        }

        refreshTable();
    }

    private void runSA() {
        if (allPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load data first.");
            return;
        }

        if (perceptron == null) {
            JOptionPane.showMessageDialog(this, "Please train the perceptron first.");
            return;
        }

        for (Plant plant : allPlants) {
            int prediction = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(prediction);
        }

        int numberToSelect;
        try {
            numberToSelect = Integer.parseInt(numberToSelectField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid number of plants to select.");
            return;
        }

        SA sa = new SA(1000, 0.95, 500, 100, 50);
        SA.Result result = sa.optimize(allPlants, numberToSelect);

        outputArea.setText("=== SA RESULT ===\n");
        outputArea.append("Best Cost: " + result.getBestCost() + "\n");
        outputArea.append("Total Distance: " + result.getTotalDistance() + "\n");
        outputArea.append("Missed Plants: " + result.getMissedPlants() + "\n");
        outputArea.append("Extra Watering: " + result.getExtraWatering() + "\n\n");

        outputArea.append("Optimized Watering Order:\n");
        int step = 1;
        for (Plant plant : result.getBestOrder()) {
            outputArea.append("Step " + step++
                    + " -> Plant ID: " + plant.getPlantId()
                    + " (x=" + plant.getX() + ", y=" + plant.getY() + ")"
                    + " | Predicted Needs Water: " + plant.getPredictedOutput() + "\n");
        }

        outputArea.append("\n--- SA Steps Log ---\n");
        for (String log : result.getStepsLog()) {
            outputArea.append(log + "\n");
        }

        refreshTable();
    }

    private void addPlant() {
        try {
            double moisture = Double.parseDouble(moistureField.getText().trim());
            double hours = Double.parseDouble(hoursField.getText().trim());
            int type = Integer.parseInt(typeField.getText().trim());
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());

            Plant newPlant = DataGenerator.addPlant(allPlants, moisture, hours, type, x, y);

            if (perceptron != null) {
                int prediction = perceptron.predict(newPlant.getInputs());
                newPlant.setPredictedOutput(prediction);
            }

            refreshTable();
            outputArea.setText("New plant added successfully:\n" + newPlant);

            moistureField.setText("");
            hoursField.setText("");
            typeField.setText("");
            xField.setText("");
            yField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid values.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Plant plant : allPlants) {
            tableModel.addRow(new Object[]{
                    plant.getPlantId(),
                    plant.getMoisture(),
                    plant.getHoursSinceWatering(),
                    plant.getType(),
                    plant.getExpectedOutput(),
                    plant.getPredictedOutput(),
                    plant.getX(),
                    plant.getY()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GardenGUI::new);
    }
}