import React from 'react';
import { Mission } from '../models/types';
import { EvaluationResult } from '../services/ScoringEngine';
import { Trophy, RotateCcw, Home, Check, X, AlertTriangle } from 'lucide-react';

interface ResultScreenProps {
  mission: Mission;
  result: EvaluationResult;
  onRetry: () => void;
  onMenu: () => void;
}

const ResultScreen: React.FC<ResultScreenProps> = ({ mission, result, onRetry, onMenu }) => {
  return (
    <div className="result-screen">
      <div className="result-card">
        <header className="result-header">
          <Trophy size={48} color="var(--gold)" />
          <h1 className="title-gold">RÉSULTATS DE LA MISSION</h1>
          <p className="mission-title">{mission.title}</p>
        </header>

        <div className="score-section">
          <div className="grade">{result.grade}</div>
          <div className="score-bar-container">
            <div className="score-bar" style={{width: `${result.percentage}%`}}></div>
          </div>
          <div className="score-text">{result.totalScore} / {result.maxScore} PTS ({Math.round(result.percentage)}%)</div>
        </div>

        <div className="details-section">
          <div className="feedback-list">
            <h3>ANALYSE TACTIQUE</h3>
            {result.feedbackLines.map((line, i) => (
              <div key={i} className="feedback-line">{line}</div>
            ))}
          </div>

          <div className="criteria-results">
            <h3>OBJECTIFS</h3>
            {result.criteriaResults.map((cr, i) => (
              <div key={i} className="criteria-row">
                {cr.satisfied ? <Check size={16} color="var(--green)" /> : (cr.earned > 0 ? <AlertTriangle size={16} color="orange" /> : <X size={16} color="var(--red)" />)}
                <span className="name">{cr.name}</span>
                <span className="pts">{cr.earned}/{cr.max}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="advice-section">
          <h3>CONSEIL DE L'ÉTAT-MAJOR</h3>
          <p>{result.tacticalAdvice}</p>
        </div>

        <footer className="result-footer">
          <button className="btn-outline" onClick={onRetry}><RotateCcw size={18} /> RECOMMENCER</button>
          <button className="btn-gold" onClick={onMenu}><Home size={18} /> RETOUR AU MENU</button>
        </footer>
      </div>

      <style>{`
        .result-screen {
          height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(rgba(0,0,0,0.8), rgba(0,0,0,0.8)), url('./maps/berlin.png');
          background-size: cover;
        }
        .result-card {
          background-color: var(--bg-panel);
          border: 2px solid var(--gold);
          width: 90%;
          max-width: 800px;
          max-height: 90vh;
          overflow-y: auto;
          padding: 2.5rem;
          box-shadow: 0 0 40px rgba(0,0,0,0.9);
        }
        .result-header {
          text-align: center;
          margin-bottom: 2rem;
        }
        .mission-title {
          color: var(--text-secondary);
          font-style: italic;
          margin-top: 0.5rem;
        }
        .score-section {
          text-align: center;
          margin-bottom: 2.5rem;
        }
        .grade {
          font-size: 3rem;
          font-weight: 900;
          color: var(--gold);
          margin-bottom: 1rem;
          letter-spacing: 5px;
        }
        .score-bar-container {
          height: 12px;
          background-color: var(--bg-card);
          border: 1px solid var(--border);
          border-radius: 6px;
          margin-bottom: 0.5rem;
          overflow: hidden;
        }
        .score-bar {
          height: 100%;
          background: linear-gradient(to right, var(--green), #8cbf4f);
          transition: width 1s ease-out;
        }
        .score-text {
          font-weight: bold;
          letter-spacing: 1px;
        }
        .details-section {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 2rem;
          margin-bottom: 2rem;
        }
        h3 {
          font-size: 0.9rem;
          color: var(--gold);
          border-bottom: 1px solid var(--border);
          padding-bottom: 0.5rem;
          margin-bottom: 1rem;
        }
        .feedback-line {
          font-size: 0.85rem;
          margin-bottom: 0.5rem;
          color: var(--text-secondary);
        }
        .criteria-row {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 8px;
          font-size: 0.85rem;
        }
        .criteria-row .name { flex: 1; }
        .criteria-row .pts { font-weight: bold; }
        .advice-section {
          background-color: var(--bg-card);
          padding: 1.5rem;
          border-left: 4px solid var(--gold);
          margin-bottom: 2.5rem;
        }
        .advice-section p {
          font-size: 0.9rem;
          font-style: italic;
          color: var(--text-secondary);
        }
        .result-footer {
          display: flex;
          justify-content: center;
          gap: 1.5rem;
        }
        .result-footer button {
          display: flex;
          align-items: center;
          gap: 10px;
          min-width: 200px;
          justify-content: center;
        }
      `}</style>
    </div>
  );
};

export default ResultScreen;
