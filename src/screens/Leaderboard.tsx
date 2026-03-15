import React, { useEffect, useState } from 'react';
import { SessionResult } from '../models/types';
import { ChevronLeft, Trophy, Calendar, User, Target } from 'lucide-react';

import { ApiService } from '../services/ApiService';

interface LeaderboardProps {
  onBack: () => void;
}

const Leaderboard: React.FC<LeaderboardProps> = ({ onBack }) => {
  const [history, setHistory] = useState<SessionResult[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    ApiService.getScores().then(data => {
      setHistory(data);
      setLoading(false);
    });
  }, []);

  return (
    <div className="leaderboard">
      <header className="header">
        <button className="btn-outline back-btn" onClick={onBack}>
          <ChevronLeft size={18} /> RETOUR
        </button>
        <h1 className="title-gold">TABLEAU D'HONNEUR</h1>
      </header>

      <div className="content">
        {loading ? (
          <div className="empty-state">Chargement des archives...</div>
        ) : history.length === 0 ? (
          <div className="empty-state">
            <Trophy size={64} color="var(--border)" />
            <p>Aucun état de service enregistré. Complétez votre première mission !</p>
          </div>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th><Calendar size={14} /> DATE</th>
                <th><User size={14} /> SOLDAT</th>
                <th><Target size={14} /> MISSION</th>
                <th>SCORE</th>
                <th>GRADE</th>
              </tr>
            </thead>
            <tbody>
              {history.map((res, i) => {
                const pct = (res.score / res.maxScore) * 100;
                return (
                  <tr key={i}>
                    <td>{res.date}</td>
                    <td><span className="rank">{res.playerRank}</span> {res.playerName}</td>
                    <td>{res.missionTitle}</td>
                    <td>
                      <div className="score-cell">
                        <span className="pts">{res.score}/{res.maxScore}</span>
                        <div className="mini-bar-bg"><div className="mini-bar-fill" style={{width: `${pct}%`}}></div></div>
                      </div>
                    </td>
                    <td className={`grade-cell ${getGradeClass(pct)}`}>{getGrade(pct)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      <style>{`
        .leaderboard {
          height: 100vh;
          display: flex;
          flex-direction: column;
          background-color: var(--bg-dark);
        }
        .header {
          padding: 1.5rem 2rem;
          display: flex;
          align-items: center;
          border-bottom: 2px solid var(--gold);
          background-color: var(--bg-panel);
        }
        .back-btn { margin-right: 2rem; display: flex; align-items: center; gap: 0.5rem; }
        .content {
          flex: 1;
          padding: 2rem;
          overflow-y: auto;
          display: flex;
          justify-content: center;
        }
        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          gap: 1rem;
          color: var(--text-secondary);
          margin-top: 100px;
        }
        .history-table {
          width: 100%;
          max-width: 1000px;
          border-collapse: collapse;
          background-color: var(--bg-panel);
          border: 1px solid var(--border);
        }
        .history-table th {
          background-color: var(--bg-card);
          color: var(--gold);
          text-align: left;
          padding: 15px;
          font-size: 0.8rem;
          letter-spacing: 1px;
          border-bottom: 2px solid var(--gold);
        }
        .history-table th svg { vertical-align: middle; margin-right: 5px; }
        .history-table td {
          padding: 15px;
          border-bottom: 1px solid var(--border);
          font-size: 0.9rem;
        }
        .rank { color: var(--gold-light); font-weight: bold; font-size: 0.75rem; margin-right: 5px; text-transform: uppercase; }
        .score-cell { display: flex; flex-direction: column; gap: 5px; width: 120px; }
        .pts { font-weight: bold; }
        .mini-bar-bg { height: 4px; background: rgba(0,0,0,0.3); border-radius: 2px; }
        .mini-bar-fill { height: 100%; background: var(--green); border-radius: 2px; }
        .grade-cell { font-weight: 900; letter-spacing: 1px; font-size: 0.8rem; }
        .grade-a { color: #d4af37; }
        .grade-b { color: #4CAF50; }
        .grade-c { color: #FFC107; }
        .grade-d { color: #F44336; }
      `}</style>
    </div>
  );
};

const getGrade = (pct: number) => {
  if (pct >= 90) return "EXCELLENCE";
  if (pct >= 75) return "TRÈS BIEN";
  if (pct >= 60) return "BIEN";
  if (pct >= 40) return "PASSABLE";
  return "ÉCHEC";
};

const getGradeClass = (pct: number) => {
  if (pct >= 90) return "grade-a";
  if (pct >= 75) return "grade-b";
  if (pct >= 60) return "grade-c";
  return "grade-d";
};

export default Leaderboard;
