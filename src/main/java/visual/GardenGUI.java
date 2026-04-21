package visual;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import functional.DataGenerator;
import functional.Perceptron;
import functional.Plant;
import functional.SA;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GardenGUI extends JFrame {

    private static final Color BG = new Color(245, 248, 246);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_DARK = new Color(21, 128, 61);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color PURPLE = new Color(126, 34, 206);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color BORDER = new Color(221, 226, 234);
    private static final Color GRAY_TEXT = new Color(55, 65, 81);

    private List<Plant> trainingDataPlants = new ArrayList<>();  // Excel training data
    private List<Plant> gardenPlants = new ArrayList<>();        // User-added plants only
    private Perceptron perceptron;
    
    private boolean modelTrained = false;
    private JLabel modelStatusLabel;
    
    // State tracking
    private List<Plant> currentSARoute = null;  // Stores optimized watering order from SA
    
    private DefaultTableModel tableModel;
    private JTable plantTable;
    private GardenMapPanel gardenMapPanel;
    private final JTextArea outputArea;

    private JSlider moistureSlider;
    private JSlider hoursSlider;
    private JComboBox<String> typeCombo;
    private JLabel moistureValueLabel;
    private JLabel hoursValueLabel;
    private JTextField xField;
    private JTextField yField;
    private JTextField numberToSelectField;

    private JLabel saSelectedPlantsValue;
    private JLabel saTemperatureValue;
    private JLabel saIterationValue;
    private JLabel saCurrentCostValue;
    private JLabel saBestCostValue;
    private JLabel saAcceptanceRateValue;
    private JLabel saDistanceValue;
    private JLabel saMissedValue;
    private JLabel saExtraValue;
    private JLabel saStatusValue;
    private JLabel saCostHistoryInfoValue;
    private JLabel saTemperatureHistoryInfoValue;
    private JTextArea saOrderArea;
    private JTextArea saLogArea;
    private SimpleHistoryChartPanel costChartPanel;
    private SimpleHistoryChartPanel temperatureChartPanel;

    // Perceptron dashboard fields
    private JLabel trainTotalPlantsValue;
    private JLabel trainSplitValue;
    private JLabel trainNormalizationValue;
    private JLabel trainModelStatusValue;

    private JLabel trainCurrentEpochValue;
    private JLabel trainCurrentTrainAccValue;
    private JLabel trainCurrentValAccValue;
    private JLabel trainCurrentLossValue;
    private JLabel trainBestValAccValue;

    private JLabel trainFinalTrainAccValue;
    private JLabel trainFinalValAccValue;
    private JLabel trainFinalTestAccValue;
    private JLabel trainFinalWeightsValue;
    private JLabel trainThresholdValue;
    private JLabel trainEpochsCompletedValue;
    private JLabel trainConvergenceValue;

    private JTextArea trainingLogArea;
    private SimpleHistoryChartPanel trainingAccuracyChartPanel;
    private SimpleHistoryChartPanel validationAccuracyChartPanel;
    private SimpleHistoryChartPanel trainingLossChartPanel;

    private List<Plant> latestTrainingSet = new ArrayList<>();
    private List<Plant> latestValidationSet = new ArrayList<>();
    private List<Plant> latestTestSet = new ArrayList<>();
    private boolean trainingNormalizationApplied = false;
    private double latestValidationAccuracy = 0.0;
    private double latestTestAccuracy = 0.0;

    private final JLabel accuracyLabel;
    private final JLabel plantsCardValue;
    private final JLabel modelCardValue;
    private final JLabel saCardValue;

    public GardenGUI() {
        setTitle("Smart Plant Watering Scheduler");
        setSize(1320, 840);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        cards.setPreferredSize(new Dimension(0, 120));
        plantsCardValue = createMetricCard(cards, "Training Plants", "0", GREEN);
        modelStatusLabel = createMetricCard(cards, "Model Status", "NOT TRAINED", ORANGE);
        modelCardValue = createMetricCard(cards, "Test Accuracy", "0.00%", BLUE);
        saCardValue = createMetricCard(cards, "Last SA Cost", "-", PURPLE);

        root.add(cards, BorderLayout.CENTER);

        JTabbedPane leftTabs = new JTabbedPane();
        leftTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftTabs.setBackground(CARD);
        leftTabs.addTab("Garden & Plants", buildGardenTab());
        leftTabs.addTab("Perceptron Training", buildTrainingTab());
        leftTabs.addTab("SA Optimization", buildSATab());

        JPanel outputPanel = new JPanel(new BorderLayout(0, 10));
        outputPanel.setBackground(CARD);
        outputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        accuracyLabel = new JLabel("Validation Accuracy: Not calculated yet");
        accuracyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        accuracyLabel.setForeground(GREEN_DARK);

        JLabel outputTitle = new JLabel("Live Output");
        outputTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        outputTitle.setForeground(new Color(31, 41, 55));

        JPanel outputHead = new JPanel(new BorderLayout());
        outputHead.setOpaque(false);
        outputHead.add(outputTitle, BorderLayout.NORTH);
        outputHead.add(accuracyLabel, BorderLayout.SOUTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(new Color(250, 252, 255));
        outputArea.setForeground(GRAY_TEXT);
        outputArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        outputPanel.add(outputHead, BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftTabs, outputPanel);
        split.setDividerLocation(820);
        split.setResizeWeight(0.68);
        split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setBackground(BG);

        root.add(split, BorderLayout.SOUTH);
        ((BorderLayout) root.getLayout()).addLayoutComponent(split, BorderLayout.SOUTH);
        split.setPreferredSize(new Dimension(1240, 560));

        // Load initial data from project Excel file as soon as UI starts.
        refreshTable();

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, GREEN, getWidth(), getHeight(), GREEN_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(14, 12, 260, 28, 14, 14);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Smart Plant Watering Scheduler");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("AI-Based Garden Management System");
        subtitle.setForeground(new Color(220, 252, 231));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JLabel createMetricCard(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD);
        card.setPreferredSize(new Dimension(0, 110));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(75, 85, 99));
        t.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 28));
        v.setForeground(color);
        v.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        parent.add(card);
        return v;
    }

    private JPanel buildGardenTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBackground(BG);

        gardenMapPanel = new GardenMapPanel();
        gardenMapPanel.setPreferredSize(new Dimension(760, 250));

        JPanel mapCard = new JPanel(new BorderLayout());
        mapCard.setBackground(CARD);
        mapCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel mapTitle = new JLabel("Garden Layout");
        mapTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mapTitle.setForeground(new Color(31, 41, 55));
        mapTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        mapCard.add(mapTitle, BorderLayout.NORTH);
        mapCard.add(gardenMapPanel, BorderLayout.CENTER);

        String[] columns = {"ID", "Moisture", "Hours", "Type", "Predicted", "X", "Y"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        plantTable = new JTable(tableModel);
        plantTable.setRowHeight(28);
        plantTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        plantTable.setGridColor(new Color(229, 231, 235));
        plantTable.setSelectionBackground(new Color(219, 234, 254));

        JTableHeader header = plantTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(240, 253, 244));
        header.setForeground(new Color(22, 101, 52));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < plantTable.getColumnCount(); i++) {
            plantTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel tableTitle = new JLabel("Plants Data Table");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(new Color(31, 41, 55));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(plantTable), BorderLayout.CENTER);

        JPanel mainColumn = new JPanel(new BorderLayout(0, 10));
        mainColumn.setOpaque(false);
        mainColumn.add(mapCard, BorderLayout.NORTH);
        mainColumn.add(tableCard, BorderLayout.CENTER);

        tab.add(mainColumn, BorderLayout.CENTER);
        tab.add(buildInputsPanel(), BorderLayout.EAST);

        return tab;
    }

    private JPanel buildInputsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD);
        panel.setPreferredSize(new Dimension(320, 100));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel inputTitle = new JLabel("Controls");
        inputTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputTitle.setForeground(new Color(31, 41, 55));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 8));
        form.setOpaque(false);

        moistureSlider = new JSlider(0, 100, 50);
        moistureValueLabel = new JLabel("50", SwingConstants.RIGHT);
        moistureValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        moistureSlider.addChangeListener(e -> moistureValueLabel.setText(String.valueOf(moistureSlider.getValue())));

        hoursSlider = new JSlider(0, 48, 12);
        hoursValueLabel = new JLabel("12", SwingConstants.RIGHT);
        hoursValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hoursSlider.addChangeListener(e -> hoursValueLabel.setText(String.valueOf(hoursSlider.getValue())));

        styleSlider(moistureSlider);
        styleSlider(hoursSlider);

        typeCombo = new JComboBox<>(new String[]{"Cactus", "Flower", "Herb"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeCombo.setBackground(new Color(248, 250, 252));
        typeCombo.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        xField = createInputField("");
        yField = createInputField("");
        numberToSelectField = createInputField("5");

        form.add(fieldLabel("Soil Moisture"));
        form.add(createSliderInputPanel(moistureSlider, moistureValueLabel));
        form.add(fieldLabel("Last Watered (hours)"));
        form.add(createSliderInputPanel(hoursSlider, hoursValueLabel));
        form.add(fieldLabel("Plant Type"));
        form.add(typeCombo);
        form.add(fieldLabel("X Position"));
        form.add(xField);
        form.add(fieldLabel("Y Position"));
        form.add(yField);
        form.add(fieldLabel("Plants to Select in SA"));
        form.add(numberToSelectField);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 0, 6));
        buttons.setOpaque(false);

        JButton loadButton = createButton("Load Data", GREEN);
        JButton trainButton = createButton("Train Perceptron", BLUE);
        JButton predictButton = createButton("Predict All", PURPLE);
        JButton runSAButton = createButton("Run SA", ORANGE);
        JButton addPlantButton = createButton("Add Plant", new Color(14, 116, 144));

        loadButton.addActionListener(e -> loadData());
        trainButton.addActionListener(e -> trainPerceptron());
        predictButton.addActionListener(e -> predictAllPlants());
        runSAButton.addActionListener(e -> runSA());
        addPlantButton.addActionListener(e -> addPlant());

        buttons.add(loadButton);
        buttons.add(trainButton);
        buttons.add(predictButton);
        buttons.add(runSAButton);
        buttons.add(addPlantButton);

        panel.add(inputTitle, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTrainingTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel trainingContent = new JPanel(new BorderLayout(12, 12));
        trainingContent.setBackground(BG);
        // Keep dashboard sections readable and allow scrolling when needed.
        trainingContent.setPreferredSize(new Dimension(820, 1150));

        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 12));
        topRow.setOpaque(false);

        JPanel controlsCard = createSectionCard("Training Controls");
        JPanel controls = new JPanel(new GridLayout(1, 3, 10, 10));
        controls.setOpaque(false);
        JButton loadTrainingButton = createButton("Load Training Data", GREEN);
        JButton normalizeButton = createButton("Normalize Data", new Color(2, 132, 199));
        JButton trainButton = createButton("Train Perceptron", BLUE);

        loadTrainingButton.addActionListener(e -> loadData());
        normalizeButton.addActionListener(e -> normalizeLoadedTrainingData());
        trainButton.addActionListener(e -> trainPerceptron());

        controls.add(loadTrainingButton);
        controls.add(normalizeButton);
        controls.add(trainButton);
        controlsCard.add(controls, BorderLayout.CENTER);

        JPanel datasetCard = createSectionCard("Dataset Information");
        JPanel datasetGrid = new JPanel(new GridLayout(4, 2, 8, 8));
        datasetGrid.setOpaque(false);

        trainTotalPlantsValue = new JLabel("0");
        trainSplitValue = new JLabel("-");
        trainNormalizationValue = new JLabel("Not applied");
        trainModelStatusValue = new JLabel("Not trained");

        addMetricRow(datasetGrid, "Total Training Plants", trainTotalPlantsValue);
        addMetricRow(datasetGrid, "Train/Val/Test Split", trainSplitValue);
        addMetricRow(datasetGrid, "Normalization", trainNormalizationValue);
        addMetricRow(datasetGrid, "Model Status", trainModelStatusValue);
        datasetCard.add(datasetGrid, BorderLayout.CENTER);

        topRow.add(controlsCard);
        topRow.add(datasetCard);

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 12, 12));
        middleRow.setOpaque(false);

        JPanel currentStatusCard = createSectionCard("Current Training Status");
        JPanel statusGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        statusGrid.setOpaque(false);

        trainCurrentEpochValue = new JLabel("-");
        trainCurrentTrainAccValue = new JLabel("-");
        trainCurrentValAccValue = new JLabel("-");
        trainCurrentLossValue = new JLabel("-");
        trainBestValAccValue = new JLabel("-");

        addMetricRow(statusGrid, "Current Epoch", trainCurrentEpochValue);
        addMetricRow(statusGrid, "Training Accuracy", trainCurrentTrainAccValue);
        addMetricRow(statusGrid, "Validation Accuracy", trainCurrentValAccValue);
        addMetricRow(statusGrid, "Training Loss/Error", trainCurrentLossValue);
        addMetricRow(statusGrid, "Best Validation Accuracy", trainBestValAccValue);
        currentStatusCard.add(statusGrid, BorderLayout.CENTER);

        JPanel finalResultsCard = createSectionCard("Final Model Results");
        JPanel resultsGrid = new JPanel(new GridLayout(7, 2, 8, 8));
        resultsGrid.setOpaque(false);

        trainFinalTrainAccValue = new JLabel("-");
        trainFinalValAccValue = new JLabel("-");
        trainFinalTestAccValue = new JLabel("-");
        trainFinalWeightsValue = new JLabel("-");
        trainThresholdValue = new JLabel("-");
        trainEpochsCompletedValue = new JLabel("-");
        trainConvergenceValue = new JLabel("-");

        addMetricRow(resultsGrid, "Final Training Accuracy", trainFinalTrainAccValue);
        addMetricRow(resultsGrid, "Final Validation Accuracy", trainFinalValAccValue);
        addMetricRow(resultsGrid, "Final Test Accuracy", trainFinalTestAccValue);
        addMetricRow(resultsGrid, "Final Weights", trainFinalWeightsValue);
        addMetricRow(resultsGrid, "Threshold", trainThresholdValue);
        addMetricRow(resultsGrid, "Epochs Completed", trainEpochsCompletedValue);
        addMetricRow(resultsGrid, "Converged", trainConvergenceValue);
        finalResultsCard.add(resultsGrid, BorderLayout.CENTER);

        middleRow.add(currentStatusCard);
        middleRow.add(finalResultsCard);

        JPanel chartsRow = new JPanel(new GridLayout(1, 3, 12, 12));
        chartsRow.setOpaque(false);
        trainingAccuracyChartPanel = new SimpleHistoryChartPanel("Training Accuracy %", GREEN_DARK);
        validationAccuracyChartPanel = new SimpleHistoryChartPanel("Validation Accuracy %", BLUE);
        trainingLossChartPanel = new SimpleHistoryChartPanel("Training Loss/Error", ORANGE);

        JPanel chart1 = createSectionCard("Learning Curve: Training Accuracy");
        chart1.add(trainingAccuracyChartPanel, BorderLayout.CENTER);
        JPanel chart2 = createSectionCard("Learning Curve: Validation Accuracy");
        chart2.add(validationAccuracyChartPanel, BorderLayout.CENTER);
        JPanel chart3 = createSectionCard("Learning Curve: Loss/Error");
        chart3.add(trainingLossChartPanel, BorderLayout.CENTER);
        chartsRow.add(chart1);
        chartsRow.add(chart2);
        chartsRow.add(chart3);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 12));
        bottomRow.setOpaque(false);

        JPanel logCard = createSectionCard("Training Log / Epoch Log");
        trainingLogArea = new JTextArea();
        trainingLogArea.setEditable(false);
        trainingLogArea.setLineWrap(true);
        trainingLogArea.setWrapStyleWord(true);
        trainingLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        trainingLogArea.setBackground(new Color(250, 252, 255));
        logCard.add(new JScrollPane(trainingLogArea), BorderLayout.CENTER);

        JPanel explanationCard = createSectionCard("How Perceptron Training Works");
        JTextArea explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setBackground(new Color(250, 252, 255));
        explanationArea.setText("""
            - The perceptron predicts whether a plant needs water: 0 or 1.
            - Training updates model weights using the perceptron learning rule.
            - Accuracy shows how many predictions are correct.
            - Loss/Error shows how many mistakes remain in training.
            - Learning curves visualize model improvement over epochs.
            - Training data is used for train/validation/test only, not as garden plants.
            """);
        explanationCard.add(new JScrollPane(explanationArea), BorderLayout.CENTER);

        bottomRow.add(logCard);
        bottomRow.add(explanationCard);

        JPanel center = new JPanel(new GridLayout(3, 1, 12, 12));
        center.setOpaque(false);
        center.add(middleRow);
        center.add(chartsRow);
        center.add(bottomRow);

        trainingContent.add(topRow, BorderLayout.NORTH);
        trainingContent.add(center, BorderLayout.CENTER);

        JScrollPane trainingScrollPane = new JScrollPane(trainingContent);
        trainingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        trainingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainingScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        trainingScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        trainingScrollPane.getVerticalScrollBar().setBlockIncrement(72);

        panel.add(trainingScrollPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        resetTrainingDashboard();
        return panel;
    }

    private JPanel buildSATab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel saContent = new JPanel(new BorderLayout(12, 12));
        saContent.setBackground(BG);
        // Keep sections readable; allow scrolling instead of shrinking.
        saContent.setPreferredSize(new Dimension(820, 1100));

        JPanel costCards = new JPanel(new GridLayout(1, 3, 12, 12));
        costCards.setOpaque(false);
        createMiniCard(costCards, "Missed Plants Term", "count(missed)", new Color(220, 38, 38));
        createMiniCard(costCards, "Extra Watering Term", "count(extra)", ORANGE);
        createMiniCard(costCards, "Distance Term", "Euclidean", BLUE);

        JPanel statusCard = createSectionCard("Current Status");
        JPanel statusGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        statusGrid.setOpaque(false);

        saStatusValue = new JLabel("Waiting for prediction");
        saSelectedPlantsValue = new JLabel("-");
        saIterationValue = new JLabel("-");
        saTemperatureValue = new JLabel("-");
        saAcceptanceRateValue = new JLabel("-");

        addMetricRow(statusGrid, "Status", saStatusValue);
        addMetricRow(statusGrid, "Selected Plants", saSelectedPlantsValue);
        addMetricRow(statusGrid, "Current Iteration", saIterationValue);
        addMetricRow(statusGrid, "Current Temperature", saTemperatureValue);
        addMetricRow(statusGrid, "Acceptance Rate", saAcceptanceRateValue);
        statusCard.add(statusGrid, BorderLayout.CENTER);

        JPanel costBreakdownCard = createSectionCard("Cost Breakdown");
        JPanel costGrid = new JPanel(new GridLayout(7, 2, 8, 8));
        costGrid.setOpaque(false);

        saCurrentCostValue = new JLabel("-");
        saBestCostValue = new JLabel("-");
        saDistanceValue = new JLabel("-");
        saMissedValue = new JLabel("-");
        saExtraValue = new JLabel("-");
        saCostHistoryInfoValue = new JLabel("-");
        saTemperatureHistoryInfoValue = new JLabel("-");

        addMetricRow(costGrid, "Current Cost", saCurrentCostValue);
        addMetricRow(costGrid, "Best Cost", saBestCostValue);
        addMetricRow(costGrid, "Total Distance", saDistanceValue);
        addMetricRow(costGrid, "Missed Plants", saMissedValue);
        addMetricRow(costGrid, "Extra Watering", saExtraValue);
        addMetricRow(costGrid, "Cost History", saCostHistoryInfoValue);
        addMetricRow(costGrid, "Temp History", saTemperatureHistoryInfoValue);
        costBreakdownCard.add(costGrid, BorderLayout.CENTER);

        JPanel statusAndBreakdown = new JPanel(new GridLayout(1, 2, 12, 12));
        statusAndBreakdown.setOpaque(false);
        statusAndBreakdown.add(statusCard);
        statusAndBreakdown.add(costBreakdownCard);

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 12, 12));
        chartsRow.setOpaque(false);

        costChartPanel = new SimpleHistoryChartPanel("Cost History", new Color(220, 38, 38));
        temperatureChartPanel = new SimpleHistoryChartPanel("Temperature History", BLUE);

        JPanel costChartCard = createSectionCard("Cost History Graph");
        costChartCard.add(costChartPanel, BorderLayout.CENTER);
        JPanel tempChartCard = createSectionCard("Temperature History Graph");
        tempChartCard.add(temperatureChartPanel, BorderLayout.CENTER);
        chartsRow.add(costChartCard);
        chartsRow.add(tempChartCard);

        saOrderArea = new JTextArea();
        saOrderArea.setEditable(false);
        saOrderArea.setLineWrap(true);
        saOrderArea.setWrapStyleWord(true);
        saOrderArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        saOrderArea.setBackground(new Color(250, 252, 255));

        saLogArea = new JTextArea();
        saLogArea.setEditable(false);
        saLogArea.setLineWrap(true);
        saLogArea.setWrapStyleWord(true);
        saLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        saLogArea.setBackground(new Color(250, 252, 255));

        JPanel orderCard = new JPanel(new BorderLayout(0, 8));
        orderCard.setBackground(CARD);
        orderCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        orderCard.add(new JLabel("Final Optimized Watering Order"), BorderLayout.NORTH);
        orderCard.add(new JScrollPane(saOrderArea), BorderLayout.CENTER);

        JPanel logCard = new JPanel(new BorderLayout(0, 8));
        logCard.setBackground(CARD);
        logCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        logCard.add(new JLabel("Full SA Log"), BorderLayout.NORTH);
        logCard.add(new JScrollPane(saLogArea), BorderLayout.CENTER);

        JSplitPane saSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, orderCard, logCard);
        saSplit.setDividerLocation(320);
        saSplit.setResizeWeight(0.35);
        saSplit.setBorder(BorderFactory.createEmptyBorder());

        JTextArea explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setBackground(new Color(250, 252, 255));
        explanationArea.setText("""
            How Simulated Annealing works in this project:
            - Start from a random selected ordered list of garden plants.
            - Build a neighbor by swapping route positions, and sometimes replacing one plant.
            - Compute cost = missed plants + Euclidean route distance + extra watering.
            - Accept better solutions immediately.
            - Sometimes accept worse solutions based on temperature to avoid local minima.
            - Cool temperature every iteration until stopping.
            """);

        JPanel explanationCard = createSectionCard("SA Explanation");
        explanationCard.add(new JScrollPane(explanationArea), BorderLayout.CENTER);

        JSplitPane lowerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, saSplit, explanationCard);
        lowerSplit.setResizeWeight(0.75);
        lowerSplit.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new GridLayout(3, 1, 12, 12));
        center.setOpaque(false);
        center.add(statusAndBreakdown);
        center.add(chartsRow);
        center.add(lowerSplit);

        saContent.add(costCards, BorderLayout.NORTH);
        saContent.add(center, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(saContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setBlockIncrement(72);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
        return panel;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));
        field.setMargin(new Insets(9, 12, 9, 12));
        field.setPreferredSize(new Dimension(0, 40));
        field.setBackground(new Color(248, 250, 252));
        return field;
    }

    private void normalizeLoadedTrainingData() {
        if (trainingDataPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load training data first.");
            return;
        }

        if (latestTrainingSet.isEmpty() && !trainingDataPlants.isEmpty()) {
            latestTrainingSet = DataGenerator.getTrainingSet(trainingDataPlants);
            latestValidationSet = DataGenerator.getValidationSet(trainingDataPlants);
            latestTestSet = DataGenerator.getTestSet(trainingDataPlants);
        }

        DataGenerator.calculateNormalizationStats(latestTrainingSet);
        DataGenerator.normalizeDataset(latestTrainingSet);
        DataGenerator.normalizeDataset(latestValidationSet);
        DataGenerator.normalizeDataset(latestTestSet);
        trainingNormalizationApplied = true;
        updateTrainingDatasetInfo();

        outputArea.setText("Training data normalized for train/validation/test sets.\n");
    }

    private void updateTrainingDatasetInfo() {
        if (trainTotalPlantsValue == null) {
            return;
        }

        trainTotalPlantsValue.setText(String.valueOf(trainingDataPlants.size()));

        int trainSize = latestTrainingSet != null ? latestTrainingSet.size() : 0;
        int valSize = latestValidationSet != null ? latestValidationSet.size() : 0;
        int testSize = latestTestSet != null ? latestTestSet.size() : 0;
        trainSplitValue.setText(trainSize + " / " + valSize + " / " + testSize);

        trainNormalizationValue.setText(trainingNormalizationApplied ? "Applied" : "Not applied");
        trainModelStatusValue.setText(modelTrained ? "Trained" : "Not trained");
    }

    private void resetTrainingStatusAndResults() {
        if (trainCurrentEpochValue == null) {
            return;
        }

        trainCurrentEpochValue.setText("-");
        trainCurrentTrainAccValue.setText("-");
        trainCurrentValAccValue.setText("-");
        trainCurrentLossValue.setText("-");
        trainBestValAccValue.setText("-");

        trainFinalTrainAccValue.setText("-");
        trainFinalValAccValue.setText("-");
        trainFinalTestAccValue.setText("-");
        trainFinalWeightsValue.setText("-");
        trainThresholdValue.setText("-");
        trainEpochsCompletedValue.setText("-");
        trainConvergenceValue.setText("-");

        if (trainingAccuracyChartPanel != null) {
            trainingAccuracyChartPanel.setSeries(new ArrayList<>());
        }
        if (validationAccuracyChartPanel != null) {
            validationAccuracyChartPanel.setSeries(new ArrayList<>());
        }
        if (trainingLossChartPanel != null) {
            trainingLossChartPanel.setSeries(new ArrayList<>());
        }

        if (trainingLogArea != null) {
            trainingLogArea.setText("Training log will appear here after training starts.\n");
        }
    }

    private void resetTrainingDashboard() {
        updateTrainingDatasetInfo();
        resetTrainingStatusAndResults();
    }

    private double getBestValue(List<Double> values) {
        double best = Double.NEGATIVE_INFINITY;
        for (Double v : values) {
            if (v != null && v > best) {
                best = v;
            }
        }
        return best == Double.NEGATIVE_INFINITY ? 0.0 : best;
    }

    private String formatWeights(double[] weights) {
        if (weights == null || weights.length == 0) {
            return "-";
        }

        double[] rounded = Arrays.copyOf(weights, weights.length);
        for (int i = 0; i < rounded.length; i++) {
            rounded[i] = Math.round(rounded[i] * 10000.0) / 10000.0;
        }
        return Arrays.toString(rounded);
    }

            private void createMiniCard(JPanel parent, String title, String value, Color accent) {
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            t.setForeground(new Color(100, 116, 139));

            JLabel v = new JLabel(value);
            v.setFont(new Font("Segoe UI", Font.BOLD, 16));
            v.setForeground(accent);

            card.add(t, BorderLayout.NORTH);
            card.add(v, BorderLayout.CENTER);
            parent.add(card);
            }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(GRAY_TEXT);
        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(new Color(17, 24, 39));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setMargin(new Insets(8, 10, 8, 10));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Excel File");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        trainingDataPlants = DataGenerator.loadFromExcel(selectedFile.toPath());

        plantsCardValue.setText(String.valueOf(trainingDataPlants.size()));

        latestTrainingSet = DataGenerator.getTrainingSet(trainingDataPlants);
        latestValidationSet = DataGenerator.getValidationSet(trainingDataPlants);
        latestTestSet = DataGenerator.getTestSet(trainingDataPlants);
        trainingNormalizationApplied = false;
        modelTrained = false;
        latestValidationAccuracy = 0.0;
        latestTestAccuracy = 0.0;

        modelStatusLabel.setText("NOT TRAINED");
        modelStatusLabel.setForeground(ORANGE);
        updateTrainingDatasetInfo();
        resetTrainingStatusAndResults();

        gardenPlants.clear();
        refreshTable();

        outputArea.setText("Training data loaded.\n");
        outputArea.append("Selected file: " + selectedFile.getName() + "\n");
        outputArea.append("Training plants: " + trainingDataPlants.size() + "\n");
        outputArea.append("Garden plants: 0\n");
        outputArea.append("Model status: NOT TRAINED\n\n");
        outputArea.append("Click 'Train Perceptron' to start training.");

        if (saStatusValue != null) {
            saStatusValue.setText("Waiting for prediction");
        }

        DataGenerator.printModelStatus();
    }

    private void trainPerceptron() {
        if (trainingDataPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load training data first.");
            return;
        }

        trainModelStatusValue.setText("Training...");

        // Shuffle training data before splitting (for variety in training)
        DataGenerator.shuffleDataset(trainingDataPlants);
        
        List<Plant> trainingSet = DataGenerator.getTrainingSet(trainingDataPlants);
        List<Plant> validationSet = DataGenerator.getValidationSet(trainingDataPlants);
        List<Plant> testSet = DataGenerator.getTestSet(trainingDataPlants);

        latestTrainingSet = trainingSet;
        latestValidationSet = validationSet;
        latestTestSet = testSet;

        // Calculate normalization stats from training set
        DataGenerator.calculateNormalizationStats(trainingSet);
        
        // Normalize all three sets
        DataGenerator.normalizeDataset(trainingSet);
        DataGenerator.normalizeDataset(validationSet);
        DataGenerator.normalizeDataset(testSet);
        trainingNormalizationApplied = true;
        updateTrainingDatasetInfo();

        // Create fresh perceptron instance
        perceptron = new Perceptron(3, 0.01, 0.5);
        perceptron.initializeWeights();
        
        System.out.println("\n========== TRAINING RUN ==========");
        perceptron.train(trainingSet, validationSet, 100);

        double validationAccuracy = DataGenerator.validate(perceptron, validationSet);
        double testAccuracy = perceptron.testModel(testSet);
        latestValidationAccuracy = validationAccuracy;
        latestTestAccuracy = testAccuracy;
        
        // Update model status
        modelTrained = true;
        modelStatusLabel.setText("TRAINED");
        modelStatusLabel.setForeground(GREEN);
        trainModelStatusValue.setText("Trained");
        
        accuracyLabel.setText("Validation Accuracy: " + String.format("%.2f", validationAccuracy) + "%");
        modelCardValue.setText(String.format("%.2f%%", testAccuracy));

        outputArea.setText("Perceptron trained successfully.\n\n");
        
        // Print dataset sizes
        outputArea.append("=== Training Dataset Split ===\n");
        outputArea.append("Total training data: " + trainingDataPlants.size() + "\n");
        outputArea.append("Training set: " + trainingSet.size() + " (70%)\n");
        outputArea.append("Validation set: " + validationSet.size() + " (15%)\n");
        outputArea.append("Test set: " + testSet.size() + " (15%)\n");
        outputArea.append("===============================\n\n");
        
        outputArea.append("Validation Accuracy: " + String.format("%.2f", validationAccuracy) + "%\n");
        outputArea.append("Test Accuracy: " + String.format("%.2f", testAccuracy) + "%\n\n");
        outputArea.append("Training History (per Epoch):\n");

        List<Double> trainingHistory = perceptron.getTrainingAccuracyHistory();
        List<Double> validationHistory = perceptron.getValidationAccuracyHistory();
        List<Double> errorHistory = perceptron.getTrainingErrorHistory();
        
        for (int i = 0; i < trainingHistory.size(); i++) {
            outputArea.append(String.format("Epoch %3d: Train=%6.2f%% Val=%6.2f%% Error=%.4f\n", 
                i, trainingHistory.get(i), validationHistory.get(i), errorHistory.get(i)));
        }

        // Update Perceptron dashboard status, curves, and logs
        int lastIndex = trainingHistory.isEmpty() ? -1 : trainingHistory.size() - 1;
        if (lastIndex >= 0) {
            trainCurrentEpochValue.setText(String.valueOf(lastIndex));
            trainCurrentTrainAccValue.setText(String.format("%.2f%%", trainingHistory.get(lastIndex)));
            trainCurrentValAccValue.setText(String.format("%.2f%%", validationHistory.get(lastIndex)));
            trainCurrentLossValue.setText(String.format("%.4f", errorHistory.get(lastIndex)));
        }
        trainBestValAccValue.setText(String.format("%.2f%%", getBestValue(validationHistory)));

        trainFinalTrainAccValue.setText(lastIndex >= 0 ? String.format("%.2f%%", trainingHistory.get(lastIndex)) : "-");
        trainFinalValAccValue.setText(String.format("%.2f%%", validationAccuracy));
        trainFinalTestAccValue.setText(String.format("%.2f%%", testAccuracy));
        trainFinalWeightsValue.setText(formatWeights(perceptron.getWeights()));
        trainThresholdValue.setText(String.format("%.2f", perceptron.getThreshold()));
        trainEpochsCompletedValue.setText(String.valueOf(perceptron.getCompletedEpochs()));
        trainConvergenceValue.setText(perceptron.isConverged() ? "Yes" : "No");

        trainingAccuracyChartPanel.setSeries(trainingHistory);
        validationAccuracyChartPanel.setSeries(validationHistory);
        trainingLossChartPanel.setSeries(errorHistory);

        trainingLogArea.setText("=== Perceptron Training Log ===\n");
        for (int i = 0; i < trainingHistory.size(); i++) {
            trainingLogArea.append(String.format(
                    "Epoch %3d | Train=%6.2f%% | Val=%6.2f%% | Loss=%.4f\n",
                    i,
                    trainingHistory.get(i),
                    validationHistory.get(i),
                    errorHistory.get(i)
            ));
        }
        if (perceptron.isConverged()) {
            trainingLogArea.append("Convergence reached.\n");
        } else {
            trainingLogArea.append("Training stopped at max epochs without full convergence.\n");
        }
        
        // Show confusion matrix
        int[] cm = DataGenerator.getConfusionMatrix(perceptron, testSet);
        outputArea.append("\n=== Test Set Confusion Matrix ===\n");
        outputArea.append("True Positives (TP):  " + cm[0] + "\n");
        outputArea.append("True Negatives (TN):  " + cm[1] + "\n");
        outputArea.append("False Positives (FP): " + cm[2] + "\n");
        outputArea.append("False Negatives (FN): " + cm[3] + "\n");
        outputArea.append("=================================\n");

        refreshTable();
    }

    private void predictAllPlants() {
        if (!modelTrained || perceptron == null) {
            JOptionPane.showMessageDialog(this, "Please train the perceptron first.");
            return;
        }

        if (gardenPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add plants to the garden first.");
            return;
        }

        outputArea.setText("Predicting for garden plants:\n\n");
        for (Plant plant : gardenPlants) {
            int prediction = perceptron.predict(plant.getInputs());
            plant.setPredictedOutput(prediction);
            outputArea.append("Plant ID: " + plant.getPlantId() + " | Predicted Needs Water: " + prediction + "\n");
        }

        // Predict All should only predict. It also clears any old SA route.
        currentSARoute = null;
        if (gardenMapPanel != null) {
            gardenMapPanel.setRoute(null);
        }

        if (saStatusValue != null) {
            saStatusValue.setText("Ready to run SA");
        }

        refreshTable();
    }

    private void runSA() {
        if (gardenPlants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add plants to the garden first.");
            return;
        }

        if (!modelTrained || perceptron == null) {
            JOptionPane.showMessageDialog(this, "Please train the perceptron first.");
            return;
        }

        // SA must run only after Predict All has filled outputs.
        for (Plant plant : gardenPlants) {
            if (plant.getPredictedOutput() == null) {
                JOptionPane.showMessageDialog(this, "Please click Predict All first before running SA.");
                if (saStatusValue != null) {
                    saStatusValue.setText("Prediction missing");
                }
                return;
            }
        }

        int numberToSelect;
        try {
            numberToSelect = Integer.parseInt(numberToSelectField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number of plants to select.");
            return;
        }

        if (numberToSelect <= 0) {
            JOptionPane.showMessageDialog(this, "Number of plants to select must be greater than 0.");
            return;
        }

        if (saStatusValue != null) {
            saStatusValue.setText("Running...");
        }

        // Clear old route before drawing the new optimized path.
        currentSARoute = null;
        if (gardenMapPanel != null) {
            gardenMapPanel.setRoute(null);
        }

        SA sa = new SA(1000, 0.95, 500, 1, 1);
        SA.Result result = sa.optimize(gardenPlants, numberToSelect);
        saCardValue.setText(String.format("%.2f", result.getBestCost()));

        // Store the optimized route for drawing on garden map
        currentSARoute = new ArrayList<>(result.getBestOrder());
        gardenMapPanel.setRoute(currentSARoute);

        // Update SA tab metrics.
        saSelectedPlantsValue.setText(String.valueOf(result.getSelectedPlants()));
        saTemperatureValue.setText(String.format("%.4f", result.getFinalTemperature()));
        saIterationValue.setText(String.valueOf(result.getCompletedIterations()));
        saAcceptanceRateValue.setText(String.format("%.2f%%", result.getAcceptanceRate()));
        saCurrentCostValue.setText(String.format("%.4f", result.getCurrentCost()));
        saBestCostValue.setText(String.format("%.4f", result.getBestCost()));
        saDistanceValue.setText(String.format("%.2f", result.getTotalDistance()));
        saMissedValue.setText(result.getMissedPlants() + " (cost " + String.format("%.2f", result.getMissedCostComponent()) + ")");
        saExtraValue.setText(result.getExtraWatering() + " (cost " + String.format("%.2f", result.getExtraCostComponent()) + ")");
        saCostHistoryInfoValue.setText(result.getCostHistory().size() + " points");
        saTemperatureHistoryInfoValue.setText(result.getTemperatureHistory().size() + " points");

        if (costChartPanel != null) {
            costChartPanel.setSeries(result.getCostHistory());
        }
        if (temperatureChartPanel != null) {
            temperatureChartPanel.setSeries(result.getTemperatureHistory());
        }
        if (saStatusValue != null) {
            saStatusValue.setText("Completed");
        }

        outputArea.setText("=== Simulated Annealing Results ===\n");
        outputArea.append("Selected Plants: " + result.getSelectedPlants() + "\n");
        outputArea.append("Current Iteration: " + result.getCompletedIterations() + "\n");
        outputArea.append("Acceptance Rate: " + String.format("%.2f%%", result.getAcceptanceRate()) + "\n");
        outputArea.append("Current Temperature: " + String.format("%.4f", result.getFinalTemperature()) + "\n");
        outputArea.append("Current Cost: " + String.format("%.4f", result.getCurrentCost()) + "\n");
        outputArea.append("Best Cost: " + result.getBestCost() + "\n");
        outputArea.append("Total Distance: " + result.getTotalDistance() + "\n");
        outputArea.append("Missed Plants: " + result.getMissedPlants() + " (cost " + String.format("%.2f", result.getMissedCostComponent()) + ")\n");
        outputArea.append("Extra Watering: " + result.getExtraWatering() + " (cost " + String.format("%.2f", result.getExtraCostComponent()) + ")\n");
        outputArea.append("Distance Cost: " + String.format("%.2f", result.getDistanceCostComponent()) + "\n\n");

        outputArea.append("Optimized Watering Order:\n");
        int step = 1;

        saOrderArea.setText("");
        for (Plant plant : result.getBestOrder()) {
            String line = "Step " + step++
                    + " -> Plant ID: " + plant.getPlantId()
                    + " (x=" + plant.getX() + ", y=" + plant.getY() + ")"
                    + " | Predicted Needs Water: " + plant.getPredictedOutput() + "\n";
            outputArea.append(line);
            saOrderArea.append(line);
        }

        outputArea.append("\n--- SA Steps Log ---\n");
        saLogArea.setText("");
        for (String log : result.getStepsLog()) {
            outputArea.append(log + "\n");
            saLogArea.append(log + "\n");
        }

        refreshTable();
    }

    private void addPlant() {
        try {
            double moisture = moistureSlider.getValue();
            double hours = hoursSlider.getValue();
            int type = typeCombo.getSelectedIndex();
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());

            // Add plant to garden only, not to training data
            int newId = gardenPlants.size();
            Plant newPlant = new Plant(newId, moisture, hours, type, null, x, y);
            gardenPlants.add(newPlant);

            // Do NOT predict automatically - user must click "Predict All"
            // Do NOT set ExpectedOutput - it stays null for user-added plants

            // Add Plant should only add. Keep route hidden until SA is run again.
            currentSARoute = null;
            if (gardenMapPanel != null) {
                gardenMapPanel.setRoute(null);
            }

            if (saStatusValue != null) {
                saStatusValue.setText("Prediction required");
            }

            refreshTable();
            outputArea.setText("New plant added to garden successfully:\n");
            outputArea.append("ID: " + newId + " | Position: (" + x + ", " + y + ")\n");
            outputArea.append("Inputs -> Moisture: " + (int) moisture + ", Hours: " + (int) hours + ", Type: " + typeCombo.getSelectedItem() + "\n");
            outputArea.append("Predicted status: NOT YET - click 'Predict All' button\n");

            moistureSlider.setValue(50);
            hoursSlider.setValue(12);
            typeCombo.setSelectedIndex(0);
            xField.setText("");
            yField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid values.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Plant plant : gardenPlants) {
            tableModel.addRow(new Object[]{
                    plant.getPlantId(),
                    plant.getMoisture(),
                    plant.getHoursSinceWatering(),
                    plant.getType(),
                    plant.getPredictedOutput() == null ? "" : plant.getPredictedOutput(),
                    plant.getX(),
                    plant.getY()
            });
        }

        if (gardenMapPanel != null) {
            gardenMapPanel.setPlants(gardenPlants);
        }
    }

    private class GardenMapPanel extends JPanel {
        private List<Plant> plants = new ArrayList<>();
        private List<Plant> route = null;  // SA optimized route (if any)

        GardenMapPanel() {
            setBackground(new Color(240, 253, 244));
            setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208)));
        }

        void setPlants(List<Plant> plants) {
            this.plants = new ArrayList<>(plants);
            repaint();
        }

        void setRoute(List<Plant> route) {
            this.route = route != null ? new ArrayList<>(route) : null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bgPaint = new GradientPaint(0, 0, new Color(236, 253, 245), getWidth(), getHeight(), new Color(220, 252, 231));
            g2.setPaint(bgPaint);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(167, 243, 208));
            for (int i = 1; i < 10; i++) {
                int x = i * getWidth() / 10;
                int y = i * getHeight() / 10;
                g2.drawLine(x, 0, x, getHeight());
                g2.drawLine(0, y, getWidth(), y);
            }

            int pad = 18;
            int drawW = Math.max(1, getWidth() - (pad * 2));
            int drawH = Math.max(1, getHeight() - (pad * 2));

            // Draw SA route if it exists
            if (route != null && route.size() > 1) {
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(239, 68, 68, 180));  // Red for SA route
                
                for (int i = 0; i < route.size() - 1; i++) {
                    Plant p1 = route.get(i);
                    Plant p2 = route.get(i + 1);
                    
                    int px1 = pad + (int) Math.round((Math.max(0, Math.min(500, p1.getX())) / 500.0) * drawW);
                    int py1 = pad + (int) Math.round((Math.max(0, Math.min(500, p1.getY())) / 500.0) * drawH);
                    int px2 = pad + (int) Math.round((Math.max(0, Math.min(500, p2.getX())) / 500.0) * drawW);
                    int py2 = pad + (int) Math.round((Math.max(0, Math.min(500, p2.getY())) / 500.0) * drawH);
                    
                    g2.drawLine(px1, py1, px2, py2);
                }
            }

            // Draw plants as circles
            for (Plant p : plants) {
                int px = pad + (int) Math.round((Math.max(0, Math.min(500, p.getX())) / 500.0) * drawW);
                int py = pad + (int) Math.round((Math.max(0, Math.min(500, p.getY())) / 500.0) * drawH);

                Color typeColor = switch (p.getType()) {
                    case 0 -> new Color(245, 158, 11);
                    case 1 -> new Color(236, 72, 153);
                    default -> new Color(22, 163, 74);
                };

                g2.setColor(typeColor);
                g2.fillOval(px - 7, py - 7, 14, 14);
                g2.setColor(Color.WHITE);
                g2.drawOval(px - 7, py - 7, 14, 14);

                g2.setColor(new Color(31, 41, 55));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("P" + p.getPlantId(), px + 8, py - 8);
            }

            g2.setColor(new Color(31, 41, 55));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString("Grid: 500 x 500", getWidth() - 110, getHeight() - 8);
            g2.dispose();
        }
    }

    private void addMetricRow(JPanel parent, String title, JLabel valueLabel) {
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(75, 85, 99));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(new Color(31, 41, 55));

        parent.add(titleLabel);
        parent.add(valueLabel);
    }

    private JPanel createSliderInputPanel(JSlider slider, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    private void styleSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setPaintTicks(true);
        slider.setPaintLabels(false);
        slider.setMajorTickSpacing(Math.max(1, (slider.getMaximum() - slider.getMinimum()) / 4));
        slider.setMinorTickSpacing(Math.max(1, (slider.getMaximum() - slider.getMinimum()) / 12));
    }

    private JPanel createSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(new Color(31, 41, 55));
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private class SimpleHistoryChartPanel extends JPanel {
        private final String title;
        private final Color lineColor;
        private List<Double> series = new ArrayList<>();

        SimpleHistoryChartPanel(String title, Color lineColor) {
            this.title = title;
            this.lineColor = lineColor;
            setPreferredSize(new Dimension(300, 150));
            setBackground(new Color(250, 252, 255));
            setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        }

        void setSeries(List<Double> values) {
            this.series = values == null ? new ArrayList<>() : new ArrayList<>(values);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 28;
            int w = Math.max(1, getWidth() - (pad * 2));
            int h = Math.max(1, getHeight() - (pad * 2));

            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(pad, pad, pad, pad + h);
            g2.drawLine(pad, pad + h, pad + w, pad + h);

            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(title, pad, 16);

            if (series.size() < 2) {
                g2.drawString("Run SA to populate graph", pad + 6, pad + (h / 2));
                g2.dispose();
                return;
            }

            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double v : series) {
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            }

            if (Math.abs(max - min) < 1e-9) {
                max = min + 1.0;
            }

            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2f));
            int prevX = pad;
            int prevY = pad + h - (int) (((series.get(0) - min) / (max - min)) * h);

            for (int i = 1; i < series.size(); i++) {
                int x = pad + (int) ((i * 1.0 / (series.size() - 1)) * w);
                int y = pad + h - (int) (((series.get(i) - min) / (max - min)) * h);
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }

            g2.setColor(new Color(71, 85, 105));
            g2.drawString(String.format("min=%.2f", min), pad + 6, pad + h - 6);
            g2.drawString(String.format("max=%.2f", max), getWidth() - 92, pad + 14);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException ignored) {
        }
        SwingUtilities.invokeLater(GardenGUI::new);
    }
}
