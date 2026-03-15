import React from 'react';
import { Swords, Trophy, Settings } from 'lucide-react';

interface MainMenuProps {
  onStart: () => void;
  onLeaderboard: () => void;
}

const MainMenu: React.FC<MainMenuProps> = ({ onStart, onLeaderboard }) => {
  return (
    <div className="main-menu">
      <div className="menu-content">
        <h1 className="title-gold">⚔ WWII Tactical Training ⚔</h1>
        <p className="subtitle">Garry's Mod Tactical Simulator</p>
        
        <div className="button-group">
          <button className="btn-gold" onClick={onStart}>
            <Swords size={20} /> COMMENCER L'ENTRAÎNEMENT
          </button>
          <button className="btn-outline" onClick={onLeaderboard}>
            <Trophy size={20} /> TABLEAU D'HONNEUR
          </button>
          <button className="btn-outline">
            <Settings size={20} /> OPTIONS
          </button>
        </div>
      </div>
      
      <style>{`
        .main-menu {
          height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), url('./maps/berlin.png');
          background-size: cover;
          background-position: center;
        }
        .menu-content {
          background: rgba(26, 30, 18, 0.9);
          padding: 3rem;
          border: 2px solid var(--gold);
          text-align: center;
          max-width: 500px;
          box-shadow: 0 0 30px rgba(0,0,0,0.8);
        }
        .title-gold {
          font-size: 2.5rem;
          margin-bottom: 0.5rem;
        }
        .subtitle {
          color: var(--text-secondary);
          font-style: italic;
          margin-bottom: 2.5rem;
        }
        .button-group {
          display: flex;
          flex-direction: column;
          gap: 1rem;
        }
        .button-group button {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          font-size: 1.1rem;
        }
      `}</style>
    </div>
  );
};

export default MainMenu;
