import React, { useState, useRef, useEffect } from 'react';
import { Mission, EnemyUnit } from '../models/types';
import { MissionFactory } from '../services/MissionFactory';
import { EnemyPlacementAI } from '../services/EnemyPlacementAI';
import { ScoringEngine, EvaluationResult } from '../services/ScoringEngine';
import { ChevronLeft, CheckCircle, Info, RotateCcw, Eye, EyeOff } from 'lucide-react';

interface BattleScreenProps {
  mission: Mission;
  onEvaluate: (result: EvaluationResult) => void;
  onBack: () => void;
}

const BattleScreen: React.FC<BattleScreenProps> = ({ mission, onEvaluate, onBack }) => {
  const [placedTroops, setPlacedTroops] = useState<Record<string, {x: number, y: number}>>({});
  const [selectedTroopId, setSelectedTroopId] = useState<string | null>(null);
  const [showZones, setShowZones] = useState(true);
  const [enemies, setEnemies] = useState<EnemyUnit[]>([]);
  const zones = MissionFactory.getZonesForMission(mission.id);
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setEnemies(EnemyPlacementAI.generateEnemies(mission, zones));
  }, [mission.id, zones]);

  const handleMapClick = (e: React.MouseEvent) => {
    if (!selectedTroopId || !mapRef.current) return;

    const rect = mapRef.current.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;

    setPlacedTroops(prev => ({
      ...prev,
      [selectedTroopId]: { x, y }
    }));
    setSelectedTroopId(null);
  };

  const removeTroop = (tid: string) => {
    setPlacedTroops(prev => {
      const next = { ...prev };
      delete next[tid];
      return next;
    });
  };

  const handleEvaluate = () => {
    if (Object.keys(placedTroops).length === 0) {
      alert("Déployez au moins une unité !");
      return;
    }
    const result = ScoringEngine.evaluateByPosition(mission, placedTroops, zones);
    onEvaluate(result);
  };

  return (
    <div className="battle-screen">
      <header className="header">
        <div className="header-left">
          <button className="btn-outline" onClick={onBack}><ChevronLeft size={18} /></button>
          <h2 className="title-gold">{mission.title}</h2>
        </div>
        <div className="header-right">
          <button className="btn-outline" onClick={() => setShowZones(!showZones)}>
            {showZones ? <EyeOff size={18} /> : <Eye size={18} />} {showZones ? "MASQUER ZONES" : "AFFICHER ZONES"}
          </button>
          <button className="btn-gold" onClick={handleEvaluate}><CheckCircle size={18} /> ÉVALUER</button>
        </div>
      </header>

      <div className="main-content">
        <aside className="sidebar left-sidebar">
          <h3 className="sidebar-title">UNITÉS DISPONIBLES</h3>
          <div className="troop-list">
            {mission.availableTroops.map(t => {
              const isPlaced = !!placedTroops[t.id];
              const isSelected = selectedTroopId === t.id;
              return (
                <div 
                  key={t.id} 
                  className={`troop-card ${isPlaced ? 'placed' : ''} ${isSelected ? 'selected' : ''}`}
                  onClick={() => !isPlaced && setSelectedTroopId(isSelected ? null : t.id)}
                >
                  <div className="troop-icon" style={{color: getTroopColor(t.type)}}>{t.icon}</div>
                  <div className="troop-info">
                    <span className="name">{t.name}</span>
                    <span className="type">{t.type} • Force: {t.strength}</span>
                  </div>
                  {isPlaced && <button className="remove-btn" onClick={(e) => { e.stopPropagation(); removeTroop(t.id); }}>×</button>}
                </div>
              );
            })}
          </div>
          <button className="btn-outline reset-btn" onClick={() => setPlacedTroops({})}><RotateCcw size={16} /> RÉINITIALISER</button>
        </aside>

        <div className="map-area" ref={mapRef} onClick={handleMapClick}>
          <div className="map-container" style={{backgroundImage: `url('./maps/${mission.mapName}.png')`}}>
            {showZones && zones.map(z => (
              <div 
                key={z.id} 
                className="zone-overlay" 
                style={{
                  left: `${z.x * 100}%`,
                  top: `${z.y * 100}%`,
                  width: `${z.width * 100}%`,
                  height: `${z.height * 100}%`,
                  backgroundColor: `${getZoneColor(z.type)}44`,
                  borderColor: z.strategicValue === 'HIGH' ? 'var(--gold)' : 'var(--border)',
                  borderWidth: z.strategicValue === 'HIGH' ? '2px' : '1px'
                }}
              >
                <span className="zone-name">{z.name}</span>
              </div>
            ))}

            {enemies.map(e => (
              <div 
                key={e.id} 
                className="unit-token enemy"
                style={{left: `${e.relX * 100}%`, top: `${e.relY * 100}%`}}
                title={`ENNEMI: ${e.name} (${e.type})`}
              >
                💀
              </div>
            ))}

            {Object.entries(placedTroops).map(([tid, pos]) => {
              const troop = mission.availableTroops.find(t => t.id === tid);
              if (!troop) return null;
              return (
                <div 
                  key={tid} 
                  className="unit-token player"
                  style={{
                    left: `${pos.x * 100}%`, 
                    top: `${pos.y * 100}%`,
                    backgroundColor: getTroopColor(troop.type)
                  }}
                  onMouseDown={() => {
                    // Simple drag could be implemented here
                  }}
                  title={troop.name}
                >
                  {troop.icon}
                </div>
              );
            })}
          </div>
        </div>

        <aside className="sidebar right-sidebar">
          <h3 className="sidebar-title">CRITÈRES</h3>
          <div className="criteria-list">
            {mission.criteria.map((c, i) => (
              <div key={i} className="criteria-card">
                <div className="name">{c.name}</div>
                <div className="desc">{c.description}</div>
                <div className="points">+{c.maxPoints} pts</div>
              </div>
            ))}
          </div>
          <div className="mission-brief">
            <h4><Info size={16} /> BRIEFING</h4>
            <p>{mission.briefing}</p>
          </div>
        </aside>
      </div>

      <style>{`
        .battle-screen {
          height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--bg-dark);
        }
        .header {
          padding: 1rem 2rem;
          background-color: var(--bg-panel);
          border-bottom: 2px solid var(--gold);
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .header-left, .header-right {
          display: flex;
          align-items: center;
          gap: 1rem;
        }
        .main-content {
          flex: 1;
          display: grid;
          grid-template-columns: 280px 1fr 240px;
          overflow: hidden;
        }
        .sidebar {
          background-color: var(--bg-panel);
          padding: 1.5rem;
          display: flex;
          flex-direction: column;
          gap: 1.5rem;
          overflow-y: auto;
        }
        .left-sidebar { border-right: 1px solid var(--border); }
        .right-sidebar { border-left: 1px solid var(--border); }
        .sidebar-title {
          font-size: 1rem;
          color: var(--gold);
          letter-spacing: 1px;
          border-bottom: 1px solid var(--border);
          padding-bottom: 0.5rem;
        }
        .troop-list {
          display: flex;
          flex-direction: column;
          gap: 0.8rem;
          flex: 1;
        }
        .troop-card {
          background-color: var(--bg-card);
          border: 1px solid var(--border);
          padding: 10px;
          display: flex;
          align-items: center;
          gap: 10px;
          cursor: pointer;
          position: relative;
        }
        .troop-card:hover { border-color: var(--gold); }
        .troop-card.selected { border-color: var(--gold); background-color: rgba(197, 160, 89, 0.1); }
        .troop-card.placed { opacity: 0.6; cursor: default; border-color: var(--green); }
        .troop-icon { font-size: 1.5rem; width: 30px; text-align: center; }
        .troop-info .name { display: block; font-weight: bold; font-size: 0.9rem; }
        .troop-info .type { font-size: 0.75rem; color: var(--text-secondary); }
        .remove-btn {
          position: absolute;
          right: 5px;
          top: 5px;
          background: var(--red);
          color: white;
          border-radius: 50%;
          width: 18px;
          height: 18px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          cursor: pointer;
          border: none;
        }
        .map-area {
          flex: 1;
          background-color: #000;
          padding: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: auto;
        }
        .map-container {
          width: 100%;
          max-width: 1000px;
          aspect-ratio: 1.28; /* Adjust based on your maps */
          background-size: cover;
          background-position: center;
          position: relative;
          box-shadow: 0 0 50px rgba(0,0,0,0.5);
        }
        .zone-overlay {
          position: absolute;
          border-style: solid;
          display: flex;
          align-items: center;
          justify-content: center;
          pointer-events: none;
        }
        .zone-name {
          font-size: 10px;
          background: rgba(0,0,0,0.6);
          padding: 2px 4px;
          text-align: center;
        }
        .unit-token {
          position: absolute;
          width: 36px;
          height: 36px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          transform: translate(-50%, -50%);
          border: 2px solid white;
          box-shadow: 0 0 10px rgba(0,0,0,0.5);
          z-index: 10;
        }
        .unit-token.player { border-color: var(--gold); }
        .unit-token.enemy { background-color: #8b0000; border-color: #ff4444; font-size: 14px; }
        .criteria-card {
          background-color: var(--bg-card);
          padding: 10px;
          border: 1px solid var(--border);
          margin-bottom: 10px;
        }
        .criteria-card .name { font-weight: bold; font-size: 0.9rem; color: var(--gold-light); }
        .criteria-card .desc { font-size: 0.8rem; color: var(--text-secondary); margin: 5px 0; }
        .criteria-card .points { color: var(--green); font-weight: bold; font-size: 0.8rem; text-align: right; }
        .mission-brief h4 { font-size: 0.9rem; color: var(--gold); display: flex; align-items: center; gap: 5px; margin-bottom: 5px; }
        .mission-brief p { font-size: 0.8rem; color: var(--text-secondary); line-height: 1.4; }
        .reset-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; margin-top: 10px; }
      `}</style>
    </div>
  );
};

const getTroopColor = (type: string) => {
  switch (type) {
    case 'INFANTRY': return '#4a8c4a';
    case 'TANK': return '#4a6b8c';
    case 'SNIPER': return '#8c4a4a';
    case 'ARTILLERY': return '#8c8c4a';
    case 'MEDIC': return '#4a8c8c';
    case 'ENGINEER': return '#8c6b4a';
    default: return 'var(--gold)';
  }
};

const getZoneColor = (type: string) => {
  switch (type) {
    case 'OFFENSIF': return '#ff0000';
    case 'DEFENSIF': return '#0000ff';
    case 'COLLINE': return '#ffff00';
    case 'VILLE': return '#ff00ff';
    case 'PONT': return '#00ffff';
    case 'COUVERTURE': return '#00ff00';
    default: return '#cccccc';
  }
};

export default BattleScreen;
