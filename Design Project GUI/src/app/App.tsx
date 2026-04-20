import { useState } from 'react';
import { GardenView } from './components/GardenView';
import { PerceptronTraining } from './components/PerceptronTraining';
import { SimulatedAnnealingView } from './components/SimulatedAnnealingView';
import { WateringResults } from './components/WateringResults';

export default function App() {
  const [activeTab, setActiveTab] = useState('garden');

  const tabs = [
    { id: 'garden', label: 'Garden & Plants' },
    { id: 'perceptron', label: 'Perceptron Training' },
    { id: 'optimization', label: 'SA Optimization' },
    { id: 'results', label: 'Watering Schedule' },
  ];

  return (
    <div className="size-full bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-gradient-to-r from-green-600 to-green-700 text-white px-6 py-4 shadow-lg">
        <h1 className="text-2xl font-bold">Smart Plant Watering Scheduler</h1>
        <p className="text-green-100 text-sm mt-1">AI-Based Garden Management System</p>
      </header>

      {/* Tab Navigation */}
      <div className="bg-white border-b border-gray-200 shadow-sm">
        <div className="flex gap-1 px-6">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-6 py-3 font-medium transition-colors border-b-2 ${
                activeTab === tab.id
                  ? 'border-green-600 text-green-600'
                  : 'border-transparent text-gray-600 hover:text-gray-800 hover:border-gray-300'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Content Area */}
      <div className="flex-1 overflow-auto">
        {activeTab === 'garden' && <GardenView />}
        {activeTab === 'perceptron' && <PerceptronTraining />}
        {activeTab === 'optimization' && <SimulatedAnnealingView />}
        {activeTab === 'results' && <WateringResults />}
      </div>
    </div>
  );
}