import { useState } from 'react';

interface Plant {
  id: number;
  x: number;
  y: number;
  soilMoisture: number;
  lastWatered: number;
  type: 'cactus' | 'flower' | 'herb';
}

export function GardenView() {
  const [plants, setPlants] = useState<Plant[]>([
    { id: 1, x: 100, y: 150, soilMoisture: 25, lastWatered: 36, type: 'cactus' },
    { id: 2, x: 250, y: 100, soilMoisture: 15, lastWatered: 42, type: 'flower' },
    { id: 3, x: 400, y: 200, soilMoisture: 60, lastWatered: 8, type: 'herb' },
    { id: 4, x: 550, y: 120, soilMoisture: 30, lastWatered: 28, type: 'flower' },
    { id: 5, x: 180, y: 320, soilMoisture: 45, lastWatered: 15, type: 'herb' },
  ]);

  const [formData, setFormData] = useState({
    x: '',
    y: '',
    soilMoisture: '',
    lastWatered: '',
    type: 'flower' as 'cactus' | 'flower' | 'herb',
  });

  const [selectedPlant, setSelectedPlant] = useState<number | null>(null);

  const handleAddPlant = (e: React.FormEvent) => {
    e.preventDefault();
    const newPlant: Plant = {
      id: plants.length + 1,
      x: Number(formData.x),
      y: Number(formData.y),
      soilMoisture: Number(formData.soilMoisture),
      lastWatered: Number(formData.lastWatered),
      type: formData.type,
    };
    setPlants([...plants, newPlant]);
    setFormData({ x: '', y: '', soilMoisture: '', lastWatered: '', type: 'flower' });
  };

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

  const getPlantColor = (type: string) => {
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

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Garden Visualization */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Garden Layout</h2>
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

              {/* Plants */}
              {plants.map((plant) => (
                <div
                  key={plant.id}
                  onClick={() => setSelectedPlant(plant.id)}
                  className={`absolute transform -translate-x-1/2 -translate-y-1/2 cursor-pointer transition-all ${
                    selectedPlant === plant.id ? 'scale-125 z-10' : 'hover:scale-110'
                  }`}
                  style={{ left: `${plant.x}px`, top: `${plant.y}px` }}
                >
                  <div className={`w-12 h-12 ${getPlantColor(plant.type)} rounded-full flex items-center justify-center text-2xl shadow-lg border-2 border-white`}>
                    {getPlantIcon(plant.type)}
                  </div>
                  <div className="absolute -bottom-6 left-1/2 transform -translate-x-1/2 whitespace-nowrap text-xs font-medium text-gray-700">
                    P{plant.id}
                  </div>
                </div>
              ))}

              {/* Coordinates helper */}
              <div className="absolute bottom-2 right-2 bg-white/80 px-2 py-1 rounded text-xs text-gray-600">
                600 × 500 px
              </div>
            </div>
          </div>
        </div>

        {/* Plant Input Form */}
        <div className="space-y-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Add New Plant</h2>
            <form onSubmit={handleAddPlant} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    X Position
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="600"
                    value={formData.x}
                    onChange={(e) => setFormData({ ...formData, x: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Y Position
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="500"
                    value={formData.y}
                    onChange={(e) => setFormData({ ...formData, y: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Soil Moisture (0-100)
                </label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  value={formData.soilMoisture}
                  onChange={(e) => setFormData({ ...formData, soilMoisture: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Last Watered (hours ago)
                </label>
                <input
                  type="number"
                  min="0"
                  max="48"
                  value={formData.lastWatered}
                  onChange={(e) => setFormData({ ...formData, lastWatered: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Plant Type
                </label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value as 'cactus' | 'flower' | 'herb' })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="cactus">🌵 Cactus (0)</option>
                  <option value="flower">🌸 Flower (1)</option>
                  <option value="herb">🌿 Herb (2)</option>
                </select>
              </div>

              <button
                type="submit"
                className="w-full bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition-colors font-medium"
              >
                Add Plant
              </button>
            </form>
          </div>

          {/* Selected Plant Details */}
          {selectedPlant && (
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-semibold mb-4 text-gray-800">Plant Details</h2>
              {plants
                .filter((p) => p.id === selectedPlant)
                .map((plant) => (
                  <div key={plant.id} className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">Plant ID:</span>
                      <span className="font-medium">P{plant.id}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">Type:</span>
                      <span className="font-medium">
                        {getPlantIcon(plant.type)} {plant.type.charAt(0).toUpperCase() + plant.type.slice(1)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">Position:</span>
                      <span className="font-medium">({plant.x}, {plant.y})</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">Soil Moisture:</span>
                      <span className="font-medium">{plant.soilMoisture}%</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-600">Last Watered:</span>
                      <span className="font-medium">{plant.lastWatered}h ago</span>
                    </div>
                  </div>
                ))}
            </div>
          )}
        </div>
      </div>

      {/* Plants List */}
      <div className="mt-6 bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">All Plants ({plants.length})</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200">
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Position (X, Y)</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Soil Moisture</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Last Watered</th>
              </tr>
            </thead>
            <tbody>
              {plants.map((plant, index) => (
                <tr
                  key={plant.id}
                  onClick={() => setSelectedPlant(plant.id)}
                  className={`border-b border-gray-100 cursor-pointer hover:bg-green-50 ${
                    selectedPlant === plant.id ? 'bg-green-50' : index % 2 === 0 ? 'bg-white' : 'bg-gray-50'
                  }`}
                >
                  <td className="px-4 py-3 text-sm">P{plant.id}</td>
                  <td className="px-4 py-3 text-sm">
                    {getPlantIcon(plant.type)} {plant.type.charAt(0).toUpperCase() + plant.type.slice(1)}
                  </td>
                  <td className="px-4 py-3 text-sm">({plant.x}, {plant.y})</td>
                  <td className="px-4 py-3 text-sm">{plant.soilMoisture}%</td>
                  <td className="px-4 py-3 text-sm">{plant.lastWatered}h ago</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
