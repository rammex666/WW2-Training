import React, { useState, useEffect } from 'react';
import './styles/globals.css';
import { Mission, SessionResult } from './models/types';
import MainMenu from './screens/MainMenu';
import MissionSelect from './screens/MissionSelect';
import BattleScreen from './screens/BattleScreen';
import ResultScreen from './screens/ResultScreen';
import Leaderboard from './screens/Leaderboard';
import AdminLogin from './screens/AdminLogin';
import AdminEditor from './screens/AdminEditor';
import { EvaluationResult } from './services/ScoringEngine';
import { ApiService } from './services/ApiService';
import { MissionFactory } from './services/MissionFactory';

type Screen = 'MAIN_MENU' | 'MISSION_SELECT' | 'BATTLE' | 'RESULTS' | 'LEADERBOARD' | 'ADMIN_LOGIN' | 'ADMIN_EDITOR';

const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<Screen>('MAIN_MENU');
  const [playerName, setPlayerName] = useState('Sous-Lieutenant');
  const [playerRank, setPlayerRank] = useState('Sous-Officier');
  const [selectedMission, setSelectedMission] = useState<Mission | null>(null);
  const [evaluationResult, setEvaluationResult] = useState<EvaluationResult | null>(null);
  const [allMissions, setAllMissions] = useState<Mission[]>([]);

  // Chargement des missions (JSON + LocalStorage pour les créations admin)
  useEffect(() => {
    const loadMissions = async () => {
      const baseMissions = await ApiService.getMissions();
      const customMissions = JSON.parse(localStorage.getItem('custom_missions') || '[]');
      
      // Si aucune mission par défaut (ex: erreur fetch), utiliser MissionFactory
      const finalBase = baseMissions.length > 0 ? baseMissions : MissionFactory.createAllMissions();
      setAllMissions([...finalBase, ...customMissions]);
    };
    loadMissions();
  }, []);

  const navigateTo = (screen: Screen) => setCurrentScreen(screen);

  const startMission = (mission: Mission) => {
    setSelectedMission(mission);
    navigateTo('BATTLE');
  };

  const showResults = (result: EvaluationResult) => {
    setEvaluationResult(result);
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

  const saveAdminMission = (mission: Mission) => {
    const custom = JSON.parse(localStorage.getItem('custom_missions') || '[]');
    const index = custom.findIndex((m: Mission) => m.id === mission.id);
    if (index >= 0) custom[index] = mission;
    else custom.push(mission);
    
    localStorage.setItem('custom_missions', JSON.stringify(custom));
    // Recharger la liste
    const baseMissions = allMissions.filter(m => !custom.find((c: any) => c.id === m.id));
    setAllMissions([...baseMissions, ...custom]);
    alert("Mission enregistrée avec succès !");
  };

  const deleteAdminMission = (id: string) => {
    let custom = JSON.parse(localStorage.getItem('custom_missions') || '[]');
    custom = custom.filter((m: Mission) => m.id !== id);
    localStorage.setItem('custom_missions', JSON.stringify(custom));
    window.location.reload(); // Simple reload pour rafraîchir
  };

  return (
    <div className="app-container">
      {currentScreen === 'MAIN_MENU' && (
        <MainMenu 
          onStart={() => navigateTo('MISSION_SELECT')} 
          onLeaderboard={() => navigateTo('LEADERBOARD')}
          onAdmin={() => navigateTo('ADMIN_LOGIN')}
        />
      )}
      {currentScreen === 'ADMIN_LOGIN' && (
        <AdminLogin 
          onLogin={() => navigateTo('ADMIN_EDITOR')}
          onClose={() => navigateTo('MAIN_MENU')}
        />
      )}
      {currentScreen === 'ADMIN_EDITOR' && (
        <AdminEditor 
          missions={allMissions}
          onSave={saveAdminMission}
          onDelete={deleteAdminMission}
          onBack={() => navigateTo('MAIN_MENU')}
        />
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
