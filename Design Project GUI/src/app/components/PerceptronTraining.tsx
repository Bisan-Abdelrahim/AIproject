import { useState } from 'react';

export function PerceptronTraining() {
  const [testInputs, setTestInputs] = useState({
    soilMoisture: '',
    lastWatered: '',
    plantType: '1',
  });

  const [predictionResult, setPredictionResult] = useState<{ needsWater: boolean; confidence: number } | null>(null);

  const handleTest = (e: React.FormEvent) => {
    e.preventDefault();
    const mock = Math.random() > 0.5;
    setPredictionResult({
      needsWater: mock,
      confidence: Math.random() * 0.3 + 0.7,
    });
  };

  const epochs = Array.from({ length: 50 }, (_, i) => i + 1);
  const lossData = epochs.map((e) => 1 / (e * 0.2 + 1) + Math.random() * 0.1);
  const accuracyData = epochs.map((e) => Math.min(0.95, e * 0.018 + 0.1 + Math.random() * 0.05));

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Training Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Training Accuracy</div>
          <div className="text-3xl font-bold mt-2">94.2%</div>
        </div>
        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Validation Accuracy</div>
          <div className="text-3xl font-bold mt-2">91.8%</div>
        </div>
        <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Final Loss</div>
          <div className="text-3xl font-bold mt-2">0.142</div>
        </div>
        <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Epochs Completed</div>
          <div className="text-3xl font-bold mt-2">50</div>
        </div>
      </div>

      {/* Learning Curves */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Loss Curve */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Training Loss Over Epochs</h2>
          <div className="relative" style={{ height: '300px' }}>
            <svg className="w-full h-full" viewBox="0 0 500 300">
              {/* Grid lines */}
              {[0, 1, 2, 3, 4, 5].map((i) => (
                <g key={i}>
                  <line
                    x1="50"
                    y1={50 + i * 40}
                    x2="480"
                    y2={50 + i * 40}
                    stroke="#e5e7eb"
                    strokeWidth="1"
                  />
                  <text x="40" y={50 + i * 40 + 5} fontSize="10" fill="#6b7280" textAnchor="end">
                    {(1 - i * 0.2).toFixed(1)}
                  </text>
                </g>
              ))}

              {/* X-axis labels */}
              {[0, 10, 20, 30, 40, 50].map((epoch) => (
                <text
                  key={epoch}
                  x={50 + (epoch / 50) * 430}
                  y="280"
                  fontSize="10"
                  fill="#6b7280"
                  textAnchor="middle"
                >
                  {epoch}
                </text>
              ))}

              {/* Loss curve */}
              <polyline
                points={lossData
                  .map((loss, i) => `${50 + (i / 49) * 430},${250 - loss * 200}`)
                  .join(' ')}
                fill="none"
                stroke="#8b5cf6"
                strokeWidth="3"
              />

              {/* Points */}
              {lossData.map((loss, i) => (
                <circle
                  key={i}
                  cx={50 + (i / 49) * 430}
                  cy={250 - loss * 200}
                  r="3"
                  fill="#8b5cf6"
                />
              ))}
            </svg>
            <div className="absolute bottom-0 left-0 right-0 text-center text-sm text-gray-600 mt-2">
              Epochs
            </div>
            <div className="absolute left-0 top-0 bottom-0 flex items-center">
              <div className="transform -rotate-90 text-sm text-gray-600 whitespace-nowrap">
                Loss
              </div>
            </div>
          </div>
        </div>

        {/* Accuracy Curve */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Training Accuracy Over Epochs</h2>
          <div className="relative" style={{ height: '300px' }}>
            <svg className="w-full h-full" viewBox="0 0 500 300">
              {/* Grid lines */}
              {[0, 1, 2, 3, 4, 5].map((i) => (
                <g key={i}>
                  <line
                    x1="50"
                    y1={50 + i * 40}
                    x2="480"
                    y2={50 + i * 40}
                    stroke="#e5e7eb"
                    strokeWidth="1"
                  />
                  <text x="40" y={50 + i * 40 + 5} fontSize="10" fill="#6b7280" textAnchor="end">
                    {(100 - i * 20).toFixed(0)}%
                  </text>
                </g>
              ))}

              {/* X-axis labels */}
              {[0, 10, 20, 30, 40, 50].map((epoch) => (
                <text
                  key={epoch}
                  x={50 + (epoch / 50) * 430}
                  y="280"
                  fontSize="10"
                  fill="#6b7280"
                  textAnchor="middle"
                >
                  {epoch}
                </text>
              ))}

              {/* Accuracy curve */}
              <polyline
                points={accuracyData
                  .map((acc, i) => `${50 + (i / 49) * 430},${250 - acc * 200}`)
                  .join(' ')}
                fill="none"
                stroke="#10b981"
                strokeWidth="3"
              />

              {/* Points */}
              {accuracyData.map((acc, i) => (
                <circle
                  key={i}
                  cx={50 + (i / 49) * 430}
                  cy={250 - acc * 200}
                  r="3"
                  fill="#10b981"
                />
              ))}
            </svg>
            <div className="absolute bottom-0 left-0 right-0 text-center text-sm text-gray-600 mt-2">
              Epochs
            </div>
            <div className="absolute left-0 top-0 bottom-0 flex items-center">
              <div className="transform -rotate-90 text-sm text-gray-600 whitespace-nowrap">
                Accuracy (%)
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Model Weights and Parameters */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Model Weights</h2>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">w₁ (Soil Moisture)</span>
              <span className="text-blue-600 font-mono">-0.4823</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">w₂ (Last Watered)</span>
              <span className="text-blue-600 font-mono">0.6147</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">w₃ (Plant Type)</span>
              <span className="text-blue-600 font-mono">0.2891</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-blue-100 rounded">
              <span className="text-gray-700 font-medium">Bias (b)</span>
              <span className="text-blue-700 font-mono">0.1523</span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Training Parameters</h2>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">Learning Rate</span>
              <span className="text-purple-600 font-mono">0.01</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">Batch Size</span>
              <span className="text-purple-600 font-mono">32</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">Training Samples</span>
              <span className="text-purple-600 font-mono">800</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span className="text-gray-700 font-medium">Validation Samples</span>
              <span className="text-purple-600 font-mono">200</span>
            </div>
          </div>
        </div>
      </div>

      {/* Test Perceptron */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Test Perceptron</h2>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <form onSubmit={handleTest} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Soil Moisture (0-100)
              </label>
              <input
                type="number"
                min="0"
                max="100"
                value={testInputs.soilMoisture}
                onChange={(e) => setTestInputs({ ...testInputs, soilMoisture: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 35"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Last Watered (hours ago, 0-48)
              </label>
              <input
                type="number"
                min="0"
                max="48"
                value={testInputs.lastWatered}
                onChange={(e) => setTestInputs({ ...testInputs, lastWatered: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., 24"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Plant Type
              </label>
              <select
                value={testInputs.plantType}
                onChange={(e) => setTestInputs({ ...testInputs, plantType: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="0">🌵 Cactus (0)</option>
                <option value="1">🌸 Flower (1)</option>
                <option value="2">🌿 Herb (2)</option>
              </select>
            </div>

            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
            >
              Predict Watering Need
            </button>
          </form>

          {/* Prediction Result */}
          <div className="flex items-center justify-center">
            {predictionResult ? (
              <div className="text-center">
                <div
                  className={`inline-flex items-center justify-center w-32 h-32 rounded-full ${
                    predictionResult.needsWater ? 'bg-red-100' : 'bg-green-100'
                  }`}
                >
                  <div className="text-center">
                    <div className="text-5xl mb-2">
                      {predictionResult.needsWater ? '💧' : '✅'}
                    </div>
                  </div>
                </div>
                <div className="mt-4">
                  <div className="text-2xl font-bold text-gray-800">
                    {predictionResult.needsWater ? 'Needs Water' : 'No Water Needed'}
                  </div>
                  <div className="text-sm text-gray-600 mt-2">
                    Confidence: {(predictionResult.confidence * 100).toFixed(1)}%
                  </div>
                  <div className="mt-4">
                    <div className="w-64 bg-gray-200 rounded-full h-3 mx-auto">
                      <div
                        className={`h-3 rounded-full ${
                          predictionResult.needsWater ? 'bg-red-500' : 'bg-green-500'
                        }`}
                        style={{ width: `${predictionResult.confidence * 100}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center text-gray-400">
                <div className="text-6xl mb-4">🧪</div>
                <div className="text-lg">Enter plant data and click predict</div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Confusion Matrix */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Confusion Matrix (Validation Set)</h2>
        <div className="flex justify-center">
          <div className="inline-block">
            <div className="grid grid-cols-3 gap-2 text-center">
              <div></div>
              <div className="font-medium text-sm text-gray-700 p-2">Predicted: No Water</div>
              <div className="font-medium text-sm text-gray-700 p-2">Predicted: Needs Water</div>

              <div className="font-medium text-sm text-gray-700 p-2 flex items-center">Actual: No Water</div>
              <div className="bg-green-100 border-2 border-green-500 p-6 rounded-lg">
                <div className="text-3xl font-bold text-green-700">87</div>
                <div className="text-xs text-gray-600">True Negative</div>
              </div>
              <div className="bg-red-100 border-2 border-red-300 p-6 rounded-lg">
                <div className="text-3xl font-bold text-red-700">8</div>
                <div className="text-xs text-gray-600">False Positive</div>
              </div>

              <div className="font-medium text-sm text-gray-700 p-2 flex items-center">Actual: Needs Water</div>
              <div className="bg-red-100 border-2 border-red-300 p-6 rounded-lg">
                <div className="text-3xl font-bold text-red-700">9</div>
                <div className="text-xs text-gray-600">False Negative</div>
              </div>
              <div className="bg-green-100 border-2 border-green-500 p-6 rounded-lg">
                <div className="text-3xl font-bold text-green-700">96</div>
                <div className="text-xs text-gray-600">True Positive</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
