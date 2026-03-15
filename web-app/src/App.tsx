import React, { useState } from 'react';
import './styles/globals.css';
import { Mission, SessionResult } from './models/types';
import MainMenu from './screens/MainMenu';
import MissionSelect from './screens/MissionSelect';
import BattleScreen from './screens/BattleScreen';
import ResultScreen from './screens/ResultScreen';
import Leaderboard from './screens/Leaderboard';
import { EvaluationResult } from './services/ScoringEngine';

import { ApiService } from './services/ApiService';

type Screen = 'MAIN_MENU' | 'MISSION_SELECT' | 'BATTLE' | 'RESULTS' | 'LEADERBOARD';

const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<Screen>('MAIN_MENU');
  const [playerName, setPlayerName] = useState('Sous-Lieutenant');
  const [playerRank, setPlayerRank] = useState('Sous-Officier');
  const [selectedMission, setSelectedMission] = useState<Mission | null>(null);
  const [evaluationResult, setEvaluationResult] = useState<EvaluationResult | null>(null);

  const navigateTo = (screen: Screen) => setCurrentScreen(screen);

  const startMission = (mission: Mission) => {
    setSelectedMission(mission);
    navigateTo('BATTLE');
  };

  const showResults = (result: EvaluationResult) => {
    setEvaluationResult(result);
    // Sauvegarde MongoDB (avec fallback local automatique dans le service)
    const newResult: SessionResult = {
      playerName,
      playerRank,
      missionTitle: selectedMission?.title || 'Unknown',
      score: result.totalScore,
      maxScore: result.maxScore,
      feedback: result.tacticalAdvice,
      date: new Date().toLocaleDateString(),
    };
    ApiService.saveScore(newResult);
    
    navigateTo('RESULTS');
  };

  return (
    <div className="app-container">
      {currentScreen === 'MAIN_MENU' && (
        <MainMenu onStart={() => navigateTo('MISSION_SELECT')} onLeaderboard={() => navigateTo('LEADERBOARD')} />
      )}
      {currentScreen === 'MISSION_SELECT' && (
        <MissionSelect 
          onSelect={startMission} 
          onBack={() => navigateTo('MAIN_MENU')} 
          playerName={playerName}
          playerRank={playerRank}
          setPlayerName={setPlayerName}
          setPlayerRank={setPlayerRank}
        />
      )}
      {currentScreen === 'BATTLE' && selectedMission && (
        <BattleScreen 
          mission={selectedMission} 
          onEvaluate={showResults} 
          onBack={() => navigateTo('MISSION_SELECT')} 
        />
      )}
      {currentScreen === 'RESULTS' && selectedMission && evaluationResult && (
        <ResultScreen 
          mission={selectedMission} 
          result={evaluationResult} 
          onRetry={() => navigateTo('BATTLE')} 
          onMenu={() => navigateTo('MAIN_MENU')} 
        />
      )}
      {currentScreen === 'LEADERBOARD' && (
        <Leaderboard onBack={() => navigateTo('MAIN_MENU')} />
      )}
    </div>
  );
};

export default App;
