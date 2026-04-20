export function WateringResults() {
  const plants = [
    { id: 5, x: 180, y: 320, type: 'herb', needsWater: true, distance: 0 },
    { id: 2, x: 250, y: 100, type: 'flower', needsWater: true, distance: 234.5 },
    { id: 4, x: 550, y: 120, type: 'flower', needsWater: true, distance: 301.2 },
  ];

  const allPlants = [
    { id: 1, x: 100, y: 150, type: 'cactus', needsWater: false },
    { id: 2, x: 250, y: 100, type: 'flower', needsWater: true },
    { id: 3, x: 400, y: 200, type: 'herb', needsWater: false },
    { id: 4, x: 550, y: 120, type: 'flower', needsWater: true },
    { id: 5, x: 180, y: 320, type: 'herb', needsWater: true },
  ];

  const getPlantIcon = (type: string) => {
    switch (type) {
      case 'cactus':
        return '🌵';
      case 'flower':
        return '🌸';
      case 'herb':
        return '🌿';
      default:
        return '🌱';
    }
  };

  const getPlantColor = (type: string, inSequence: boolean) => {
    if (!inSequence) return 'bg-gray-400';
    switch (type) {
      case 'cactus':
        return 'bg-yellow-500';
      case 'flower':
        return 'bg-pink-500';
      case 'herb':
        return 'bg-green-500';
      default:
        return 'bg-gray-500';
    }
  };

  const totalDistance = plants.reduce((sum, p) => sum + p.distance, 0);

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Total Plants to Water</div>
          <div className="text-3xl font-bold mt-2">{plants.length}</div>
        </div>
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Total Distance</div>
          <div className="text-3xl font-bold mt-2">{totalDistance.toFixed(1)}</div>
          <div className="text-xs opacity-75 mt-1">pixels</div>
        </div>
        <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Estimated Time</div>
          <div className="text-3xl font-bold mt-2">12</div>
          <div className="text-xs opacity-75 mt-1">minutes</div>
        </div>
        <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg shadow-md p-6 text-white">
          <div className="text-sm opacity-90">Optimization Score</div>
          <div className="text-3xl font-bold mt-2">95%</div>
        </div>
      </div>

      {/* Optimal Watering Sequence */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Optimal Watering Sequence</h2>
        <div className="flex items-center justify-center gap-4 flex-wrap">
          <div className="flex items-center justify-center w-16 h-16 rounded-full bg-gray-200 text-2xl">
            🏁
          </div>
          {plants.map((plant, index) => (
            <div key={plant.id} className="flex items-center gap-4">
              <div className="flex flex-col items-center">
                <div className="text-xs text-gray-500 mb-1">Step {index + 1}</div>
                <div className={`flex items-center justify-center w-20 h-20 ${getPlantColor(plant.type, true)} rounded-full text-3xl shadow-lg border-4 border-white ring-2 ring-green-500`}>
                  {getPlantIcon(plant.type)}
                </div>
                <div className="text-sm font-medium text-gray-700 mt-2">Plant {plant.id}</div>
                {index > 0 && (
                  <div className="text-xs text-blue-600 mt-1">
                    +{plant.distance.toFixed(1)}px
                  </div>
                )}
              </div>
              {index < plants.length - 1 && (
                <div className="flex flex-col items-center">
                  <div className="text-2xl text-gray-400">→</div>
                </div>
              )}
            </div>
          ))}
          <div className="flex items-center justify-center w-16 h-16 rounded-full bg-green-500 text-2xl">
            ✓
          </div>
        </div>
      </div>

      {/* Visual Path on Garden */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Watering Path Visualization</h2>
        <div className="relative bg-gradient-to-br from-green-50 to-green-100 rounded-lg border-2 border-green-200" style={{ height: '500px' }}>
          {/* Grid lines */}
          <div className="absolute inset-0 opacity-20">
            {[...Array(10)].map((_, i) => (
              <div key={`h-${i}`} className="absolute w-full border-t border-green-300" style={{ top: `${i * 10}%` }} />
            ))}
            {[...Array(10)].map((_, i) => (
              <div key={`v-${i}`} className="absolute h-full border-l border-green-300" style={{ left: `${i * 10}%` }} />
            ))}
          </div>

          {/* Path lines */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none">
            {plants.map((plant, index) => {
              if (index === 0) return null;
              const prevPlant = plants[index - 1];
              return (
                <g key={`path-${plant.id}`}>
                  <line
                    x1={prevPlant.x}
                    y1={prevPlant.y}
                    x2={plant.x}
                    y2={plant.y}
                    stroke="#3b82f6"
                    strokeWidth="3"
                    strokeDasharray="8,4"
                    markerEnd="url(#arrowhead)"
                  />
                  <text
                    x={(prevPlant.x + plant.x) / 2}
                    y={(prevPlant.y + plant.y) / 2 - 10}
                    fill="#3b82f6"
                    fontSize="12"
                    fontWeight="bold"
                    textAnchor="middle"
                  >
                    {plant.distance.toFixed(0)}px
                  </text>
                </g>
              );
            })}
            <defs>
              <marker
                id="arrowhead"
                markerWidth="10"
                markerHeight="10"
                refX="5"
                refY="3"
                orient="auto"
              >
                <polygon points="0 0, 10 3, 0 6" fill="#3b82f6" />
              </marker>
            </defs>
          </svg>

          {/* All Plants */}
          {allPlants.map((plant) => {
            const inSequence = plants.some((p) => p.id === plant.id);
            const sequenceIndex = plants.findIndex((p) => p.id === plant.id);
            return (
              <div
                key={plant.id}
                className="absolute transform -translate-x-1/2 -translate-y-1/2"
                style={{ left: `${plant.x}px`, top: `${plant.y}px` }}
              >
                <div
                  className={`w-14 h-14 ${getPlantColor(plant.type, inSequence)} rounded-full flex items-center justify-center text-2xl shadow-lg border-2 ${
                    inSequence ? 'border-white ring-2 ring-blue-500' : 'border-gray-300 opacity-40'
                  }`}
                >
                  {getPlantIcon(plant.type)}
                </div>
                {inSequence && sequenceIndex >= 0 && (
                  <div className="absolute -top-2 -right-2 w-6 h-6 bg-blue-600 rounded-full flex items-center justify-center text-white text-xs font-bold border-2 border-white">
                    {sequenceIndex + 1}
                  </div>
                )}
                <div className="absolute -bottom-6 left-1/2 transform -translate-x-1/2 whitespace-nowrap text-xs font-medium text-gray-700">
                  P{plant.id}
                </div>
              </div>
            );
          })}

          {/* Legend */}
          <div className="absolute top-4 right-4 bg-white/90 rounded-lg p-3 shadow-md">
            <div className="text-xs font-medium text-gray-700 mb-2">Legend</div>
            <div className="space-y-1 text-xs">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-blue-500 rounded-full ring-2 ring-blue-300"></div>
                <span>In watering sequence</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-gray-400 rounded-full"></div>
                <span>Not selected</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-8 h-0.5 bg-blue-500" style={{ borderTop: '2px dashed' }}></div>
                <span>Watering path</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Detailed Watering Schedule Table */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Detailed Watering Schedule</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 border-b-2 border-gray-200">
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Order</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Plant ID</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Position</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Distance from Previous</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Cumulative Distance</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Status</th>
              </tr>
            </thead>
            <tbody>
              {plants.map((plant, index) => {
                const cumulativeDistance = plants
                  .slice(0, index + 1)
                  .reduce((sum, p) => sum + p.distance, 0);
                return (
                  <tr
                    key={plant.id}
                    className={`border-b border-gray-100 ${
                      index % 2 === 0 ? 'bg-white' : 'bg-gray-50'
                    }`}
                  >
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-blue-500 text-white font-bold">
                        {index + 1}
                      </span>
                    </td>
                    <td className="px-4 py-3 font-medium">P{plant.id}</td>
                    <td className="px-4 py-3">
                      {getPlantIcon(plant.type)} {plant.type.charAt(0).toUpperCase() + plant.type.slice(1)}
                    </td>
                    <td className="px-4 py-3 text-sm font-mono">({plant.x}, {plant.y})</td>
                    <td className="px-4 py-3 text-sm">
                      {index === 0 ? (
                        <span className="text-gray-400">Start</span>
                      ) : (
                        <span className="font-mono text-blue-600">{plant.distance.toFixed(1)}px</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-sm font-mono">{cumulativeDistance.toFixed(1)}px</td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center px-2 py-1 rounded-full bg-green-100 text-green-800 text-xs font-medium">
                        ✓ Needs Water
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
            <tfoot>
              <tr className="bg-blue-50 border-t-2 border-blue-200">
                <td colSpan={4} className="px-4 py-3 font-medium text-gray-800">
                  Total
                </td>
                <td className="px-4 py-3"></td>
                <td className="px-4 py-3 font-bold text-blue-600">{totalDistance.toFixed(1)}px</td>
                <td className="px-4 py-3"></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-4 justify-center">
        <button className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium shadow-md">
          💾 Save Schedule
        </button>
        <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-md">
          📧 Export to Email
        </button>
        <button className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium shadow-md">
          🖨️ Print Schedule
        </button>
        <button className="px-6 py-3 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors font-medium shadow-md">
          ▶️ Start Watering
        </button>
      </div>

      {/* Recommendations */}
      <div className="bg-gradient-to-br from-yellow-50 to-orange-50 rounded-lg shadow-md p-6 border border-yellow-200">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">💡 Recommendations</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="flex items-start gap-3">
            <div className="text-2xl">🌤️</div>
            <div>
              <div className="font-medium text-gray-800">Best Time to Water</div>
              <div className="text-sm text-gray-600">Early morning (6-8 AM) or evening (6-8 PM)</div>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="text-2xl">💧</div>
            <div>
              <div className="font-medium text-gray-800">Water Amount</div>
              <div className="text-sm text-gray-600">Adjust based on plant type and soil moisture level</div>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="text-2xl">📅</div>
            <div>
              <div className="font-medium text-gray-800">Next Watering</div>
              <div className="text-sm text-gray-600">Check again in 24-48 hours depending on weather</div>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="text-2xl">⚠️</div>
            <div>
              <div className="font-medium text-gray-800">Plants Skipped</div>
              <div className="text-sm text-gray-600">2 plants don't need water right now</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
