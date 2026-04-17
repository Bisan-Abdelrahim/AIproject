public class Plant {
    private final int plantId;
    private final double moisture;
    private final double hoursSinceWatering;
    private final int type;   // 0 = cactus, 1 = flower, 2 = herb
    private final double x;
    private final double y;

    private Integer expectedOutput;   // the real label from training data
    private Integer predictedOutput;  // the output from the perceptron

    public Plant(int plantId, double moisture, double hoursSinceWatering, int type,
                 Integer expectedOutput, double x, double y) {
        this.plantId = plantId;
        this.moisture = moisture;
        this.hoursSinceWatering = hoursSinceWatering;
        this.type = type;
        this.expectedOutput = expectedOutput;
        this.predictedOutput = null;
        this.x = x;
        this.y = y;
    }

    public int getPlantId() {
        return plantId;
    }

    public double getMoisture() {
        return moisture;
    }

    public double getHoursSinceWatering() {
        return hoursSinceWatering;
    }

    public int getType() {
        return type;
    }

    public Integer getExpectedOutput() {
        return expectedOutput;
    }

    public Integer getPredictedOutput() {
        return predictedOutput;
    }

    public void setPredictedOutput(Integer predictedOutput) {
        this.predictedOutput = predictedOutput;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double[] getInputs() {
        return new double[]{moisture, hoursSinceWatering, type};
    }

    public void setExpectedOutput(Integer expectedOutput) {
    this.expectedOutput = expectedOutput;
}

    @Override
    public String toString() {
        return "Plant{" +
                "plantId=" + plantId +
                ", moisture=" + moisture +
                ", hoursSinceWatering=" + hoursSinceWatering +
                ", type=" + type +
                ", expectedOutput=" + expectedOutput +
                ", predictedOutput=" + predictedOutput +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}