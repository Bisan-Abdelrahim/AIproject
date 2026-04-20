import { useState } from 'react';

interface SAStep {
  iteration: number;
  currentCost: number;
  temperature: number;
  accepted: boolean;
  sequence: number[];
}

export function SimulatedAnnealingView() {
  const [numPlantsToWater, setNumPlantsToWater] = useState('3');
  const [isRunning, setIsRunning] = useState(false);

  const saSteps: SAStep[] = [
    { iteration: 0, currentCost: 45.8, temperature: 100.0, accepted: true, sequence: [2, 5, 1] },
    { iteration: 1, currentCost: 42.3, temperature: 95.0, accepted: true, sequence: [2, 1, 5] },
    { iteration: 2, currentCost: 44.1, temperature: 90.3, accepted: false, sequence: [5, 2, 1] },
    { iteration: 3, currentCost: 38.7, temperature: 85.8, accepted: true, sequence: [2, 4, 1] },
    { iteration: 4, currentCost: 39.2, temperature: 81.5, accepted: false, sequence: [4, 2, 1] },
    { iteration: 5, currentCost: 35.4, temperature: 77.4, accepted: true, sequence: [2, 4, 5] },
    { iteration: 6, currentCost: 36.8, temperature: 73.5, accepted: false, sequence: [4, 5, 2] },
    { iteration: 7, currentCost: 33.2, temperature: 69.8, accepted: true, sequence: [2, 5, 4] },
    { iteration: 8, currentCost: 31.6, temperature: 66.3, accepted: true, sequence: [5, 2, 4] },
    { iteration: 9, currentCost: 29.8, temperature: 63.0, accepted: true, sequence: [5, 4, 2] },
  ];

  const costHistory = Array.from({ length: 100 }, (_, i) => {
    const base = 45 * Math.exp(-i / 30);
    return Math.max(25, base + Math.random() * 5);
  });

  const temperatureHistory = Array.from({ length: 100 }, (_, i) => 100 * Math.exp(-i / 25));

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Control Panel */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Optimization Settings</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Number of Plants to Water
            </label>
            <input
              type="number"
              min="1"
              max="10"
              value={numPlantsToWater}
              onChange={(e) => setNumPlantsToWater(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Initial Temperature
            </label>
            <input
              type="number"
              value="100"
              disabled
              className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Cooling Rate
            </label>
            <input
              type="number"
              value="0.95"
              step="0.01"
              disabled
              className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
            />
          </div>

          <div className="flex items-end">
            <button
              onClick={() => setIsRunning(!isRunning)}
              className={`w-full py-2 px-4 rounded-md font-medium text-white transition-colors ${
                isRunning
                  ? 'bg-red-600 hover:bg-red-700'
                  : 'bg-orange-600 hover:bg-orange-700'
              }`}
            >
              {isRunning ? '⏸ Pause' : '▶ Start Optimization'}
            </button>
          </div>
        </div>
      </div>

      {/* Current Status */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Current Cost</div>
          <div className="text-3xl font-bold mt-2">29.8</div>
        </div>
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Temperature</div>
          <div className="text-3xl font-bold mt-2">63.0</div>
        </div>
        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Iterations</div>
          <div className="text-3xl font-bold mt-2">100</div>
        </div>
        <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Acceptance Rate</div>
          <div className="text-3xl font-bold mt-2">68%</div>
        </div>
      </div>

      {/* Cost and Temperature Graphs */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Cost Over Iterations */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Cost Function Over Iterations</h2>
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
                    {(50 - i * 10).toFixed(0)}
                  </text>
                </g>
              ))}

              {/* X-axis labels */}
              {[0, 25, 50, 75, 100].map((iter) => (
                <text
                  key={iter}
                  x={50 + (iter / 100) * 430}
                  y="280"
                  fontSize="10"
                  fill="#6b7280"
                  textAnchor="middle"
                >
                  {iter}
                </text>
              ))}

              {/* Cost curve */}
              <polyline
                points={costHistory
                  .map((cost, i) => `${50 + (i / 99) * 430},${250 - (cost / 50) * 200}`)
                  .join(' ')}
                fill="none"
                stroke="#f97316"
                strokeWidth="3"
              />
            </svg>
            <div className="absolute bottom-0 left-0 right-0 text-center text-sm text-gray-600">
              Iterations
            </div>
            <div className="absolute left-0 top-0 bottom-0 flex items-center">
              <div className="transform -rotate-90 text-sm text-gray-600 whitespace-nowrap">
                Cost
              </div>
            </div>
          </div>
        </div>

        {/* Temperature Over Iterations */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">Temperature Over Iterations</h2>
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
                    {(100 - i * 20).toFixed(0)}
                  </text>
                </g>
              ))}

              {/* X-axis labels */}
              {[0, 25, 50, 75, 100].map((iter) => (
                <text
                  key={iter}
                  x={50 + (iter / 100) * 430}
                  y="280"
                  fontSize="10"
                  fill="#6b7280"
                  textAnchor="middle"
                >
                  {iter}
                </text>
              ))}

              {/* Temperature curve */}
              <polyline
                points={temperatureHistory
                  .map((temp, i) => `${50 + (i / 99) * 430},${250 - (temp / 100) * 200}`)
                  .join(' ')}
                fill="none"
                stroke="#3b82f6"
                strokeWidth="3"
              />
            </svg>
            <div className="absolute bottom-0 left-0 right-0 text-center text-sm text-gray-600">
              Iterations
            </div>
            <div className="absolute left-0 top-0 bottom-0 flex items-center">
              <div className="transform -rotate-90 text-sm text-gray-600 whitespace-nowrap">
                Temperature
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Cost Function Breakdown */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Cost Function Components</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="border-2 border-red-200 rounded-lg p-4 bg-red-50">
            <div className="text-sm text-gray-600 mb-1">Plants Missed</div>
            <div className="text-3xl font-bold text-red-600">2</div>
            <div className="text-xs text-gray-500 mt-2">Plants needing water not in sequence</div>
          </div>
          <div className="border-2 border-blue-200 rounded-lg p-4 bg-blue-50">
            <div className="text-sm text-gray-600 mb-1">Total Distance</div>
            <div className="text-3xl font-bold text-blue-600">18.4</div>
            <div className="text-xs text-gray-500 mt-2">Sum of Euclidean distances</div>
          </div>
          <div className="border-2 border-orange-200 rounded-lg p-4 bg-orange-50">
            <div className="text-sm text-gray-600 mb-1">Extra Watering</div>
            <div className="text-3xl font-bold text-orange-600">1</div>
            <div className="text-xs text-gray-500 mt-2">Plants watered unnecessarily</div>
          </div>
        </div>
        <div className="mt-4 p-4 bg-gray-100 rounded-lg">
          <div className="text-center">
            <span className="text-gray-700 font-medium">Total Cost = </span>
            <span className="text-red-600 font-bold">2</span>
            <span className="text-gray-700"> + </span>
            <span className="text-blue-600 font-bold">18.4</span>
            <span className="text-gray-700"> + </span>
            <span className="text-orange-600 font-bold">1</span>
            <span className="text-gray-700"> = </span>
            <span className="text-purple-600 font-bold text-xl">21.4</span>
          </div>
        </div>
      </div>

      {/* SA Iteration Steps */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Recent Optimization Steps</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 border-b-2 border-gray-200">
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Iteration</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Sequence</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Cost</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Temperature</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Status</th>
              </tr>
            </thead>
            <tbody>
              {saSteps.map((step, index) => (
                <tr
                  key={step.iteration}
                  className={`border-b border-gray-100 ${
                    index % 2 === 0 ? 'bg-white' : 'bg-gray-50'
                  }`}
                >
                  <td className="px-4 py-3 text-sm font-medium">{step.iteration}</td>
                  <td className="px-4 py-3 text-sm">
                    <div className="flex gap-2">
                      {step.sequence.map((plant) => (
                        <span
                          key={plant}
                          className="inline-flex items-center px-2 py-1 rounded-full bg-green-100 text-green-800 text-xs font-medium"
                        >
                          P{plant}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm font-mono">{step.currentCost.toFixed(1)}</td>
                  <td className="px-4 py-3 text-sm font-mono">{step.temperature.toFixed(1)}</td>
                  <td className="px-4 py-3 text-sm">
                    {step.accepted ? (
                      <span className="inline-flex items-center px-2 py-1 rounded-full bg-green-100 text-green-800 text-xs font-medium">
                        ✓ Accepted
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2 py-1 rounded-full bg-red-100 text-red-800 text-xs font-medium">
                        ✗ Rejected
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Algorithm Explanation */}
      <div className="bg-gradient-to-br from-purple-50 to-blue-50 rounded-lg shadow-md p-6 border border-purple-200">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">How Simulated Annealing Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 className="font-medium text-gray-800 mb-2">Algorithm Steps:</h3>
            <ol className="space-y-2 text-sm text-gray-700">
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  1
                </span>
                <span>Start with random watering sequence</span>
              </li>
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  2
                </span>
                <span>Calculate cost (missed plants + distance + extra watering)</span>
              </li>
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  3
                </span>
                <span>Swap two random plants in sequence</span>
              </li>
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  4
                </span>
                <span>Calculate new cost</span>
              </li>
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  5
                </span>
                <span>Accept if better, or accept with probability based on temperature</span>
              </li>
              <li className="flex items-start">
                <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-purple-500 text-white text-xs mr-2 flex-shrink-0">
                  6
                </span>
                <span>Decrease temperature and repeat</span>
              </li>
            </ol>
          </div>
          <div>
            <h3 className="font-medium text-gray-800 mb-2">Key Concepts:</h3>
            <div className="space-y-3 text-sm">
              <div className="bg-white p-3 rounded-lg border border-gray-200">
                <div className="font-medium text-purple-700">Temperature</div>
                <div className="text-gray-600">Controls exploration vs exploitation. High temp = more exploration</div>
              </div>
              <div className="bg-white p-3 rounded-lg border border-gray-200">
                <div className="font-medium text-blue-700">Acceptance Probability</div>
                <div className="text-gray-600">P(accept) = exp(-ΔCost / Temperature)</div>
              </div>
              <div className="bg-white p-3 rounded-lg border border-gray-200">
                <div className="font-medium text-green-700">Cooling Schedule</div>
                <div className="text-gray-600">Temperature gradually decreases to converge on solution</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
