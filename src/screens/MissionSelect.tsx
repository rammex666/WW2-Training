import React from 'react';
import { Mission } from '../models/types';
import { ChevronLeft, User, Shield, Target } from 'lucide-react';

interface MissionSelectProps {
  missions: Mission[];
  onSelect: (mission: Mission) => void;
  onBack: () => void;
  playerName: string;
  playerRank: string;
  setPlayerName: (name: string) => void;
  setPlayerRank: (rank: string) => void;
}

const MissionSelect: React.FC<MissionSelectProps> = ({ 
  missions, onSelect, onBack, playerName, playerRank, setPlayerName, setPlayerRank 
}) => {
  return (
    <div className="mission-select">
      <header className="header">
        <button className="btn-outline back-btn" onClick={onBack}>
          <ChevronLeft size={18} /> RETOUR
        </button>
        <h1 className="title-gold">SÉLECTION DE LA MISSION</h1>
      </header>

      <div className="content">
        <aside className="player-setup">
          <h2 className="section-title"><User size={18} /> PROFIL DU JOUEUR</h2>
          <div className="input-group">
            <label>Nom du soldat</label>
            <input 
              type="text" 
              value={playerName} 
              onChange={e => setPlayerName(e.target.value)} 
              placeholder="Ex: John Doe"
            />
          </div>
          <div className="input-group">
            <label>Grade</label>
            <select value={playerRank} onChange={e => setPlayerRank(e.target.value)}>
              <option value="Soldat">Soldat</option>
              <option value="Sous-Officier">Sous-Officier</option>
              <option value="Officier">Officier</option>
            </select>
          </div>
          <div className="rank-badge">
            <Shield size={32} color="var(--gold)" />
            <span>{playerRank.toUpperCase()}</span>
          </div>
        </aside>

        <main className="mission-grid">
          {missions.length === 0 ? (
            <div className="empty-state">
              <p>Aucune mission disponible dans les archives.</p>
              <small>Si vous êtes admin, créez une mission dans l'espace administration.</small>
            </div>
          ) : (
            missions.map(m => (
              <div key={m.id} className="mission-card" onClick={() => onSelect(m)}>
                <div className="card-image">
                  <Target size={48} color="rgba(255,255,255,0.2)" />
                  <span className={`difficulty ${m.difficulty.toLowerCase()}`}>{m.difficulty}</span>
                </div>
                <div className="card-info">
                  <h3>{m.title}</h3>
                  <p>{m.description}</p>
                  <div className="card-footer">
                    <span className="terrain-tag">{m.terrain}</span>
                    <button className="btn-gold" onClick={(e) => { e.stopPropagation(); onSelect(m); }}>LANCER</button>
                  </div>
                </div>
              </div>
            ))
          )}
        </main>
      </div>

      <style>{`
        .mission-select {
          height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--bg-dark);
          color: var(--text-primary);
        }
        .header {
          padding: 1.5rem 2rem;
          display: flex;
          align-items: center;
          border-bottom: 2px solid var(--gold);
          background-color: var(--bg-panel);
        }
        .back-btn {
          margin-right: 2rem;
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }
        .content {
          flex: 1;
          display: grid;
          grid-template-columns: 300px 1fr;
          overflow: hidden;
        }
        .player-setup {
          background-color: var(--bg-panel);
          padding: 2rem;
          border-right: 1px solid var(--border);
          display: flex;
          flex-direction: column;
          gap: 1.5rem;
        }
        .section-title {
          font-size: 1.2rem;
          color: var(--gold);
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }
        .input-group {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }
        .input-group label {
          font-size: 0.9rem;
          color: var(--text-secondary);
        }
        .input-group input, .input-group select {
          background-color: var(--bg-card);
          border: 1px solid var(--border);
          color: var(--text-primary);
          padding: 10px;
          border-radius: 4px;
        }
        .rank-badge {
          margin-top: 2rem;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 0.5rem;
          background: rgba(0,0,0,0.3);
          padding: 1.5rem;
          border: 1px dashed var(--gold);
        }
        .mission-grid {
          padding: 2rem;
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
          gap: 2rem;
          overflow-y: auto;
        }
        .mission-card {
          background-color: var(--bg-card);
          border: 1px solid var(--border);
          transition: all 0.3s;
          cursor: pointer;
          display: flex;
          flex-direction: column;
        }
        .mission-card:hover {
          border-color: var(--gold);
          transform: translateY(-5px);
        }
        .card-image {
          height: 120px;
          background-color: #1a2010;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;
        }
        .difficulty {
          position: absolute;
          top: 10px;
          right: 10px;
          padding: 4px 8px;
          font-size: 0.7rem;
          font-weight: bold;
          background: rgba(0,0,0,0.7);
        }
        .difficulty.facile { color: #4CAF50; }
        .difficulty.moyen { color: #FFC107; }
        .difficulty.difficile { color: #F44336; }
        .difficulty.élite { color: #9C27B0; }
        .card-info {
          padding: 1.2rem;
          flex: 1;
          display: flex;
          flex-direction: column;
        }
        .card-info h3 {
          margin-bottom: 0.5rem;
          color: var(--gold-light);
          font-size: 1.1rem;
        }
        .card-info p {
          font-size: 0.85rem;
          color: var(--text-secondary);
          flex: 1;
          margin-bottom: 1rem;
        }
        .card-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .terrain-tag {
          font-size: 0.7rem;
          font-weight: bold;
          text-transform: uppercase;
          background: rgba(255,255,255,0.05);
          padding: 2px 6px;
        }
        .empty-state {
          grid-column: 1 / -1;
          text-align: center;
          padding: 4rem;
          color: var(--text-secondary);
        }
      `}</style>
    </div>
  );
};

export default MissionSelect;
