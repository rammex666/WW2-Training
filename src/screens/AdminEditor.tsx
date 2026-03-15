import React, { useState } from 'react';
import { Mission } from '../models/types';
import { ChevronLeft, Plus, Save, Trash2, FileText } from 'lucide-react';

interface AdminEditorProps {
  onBack: () => void;
  onSave: (mission: Mission) => void;
  missions: Mission[];
  onDelete: (id: string) => void;
}

const AdminEditor: React.FC<AdminEditorProps> = ({ onBack, onSave, missions, onDelete }) => {
  const [editingMission, setEditingMission] = useState<Partial<Mission> | null>(null);

  const createNew = () => {
    setEditingMission({
      id: "mission_" + Date.now(),
      title: "Nouvelle Mission",
      description: "Description de la mission",
      difficulty: "MOYEN",
      mapName: "berlin",
      objective: "Objectif principal",
      briefing: "Briefing officier",
      timeLimit: 300,
      terrain: "URBAIN",
      availableTroops: [],
      criteria: []
    });
  };

  const save = () => {
    if (editingMission && editingMission.id) {
      onSave(editingMission as Mission);
      setEditingMission(null);
    }
  };

  return (
    <div className="admin-editor">
      <header className="header">
        <button className="btn-outline" onClick={onBack}><ChevronLeft size={18} /> QUITTER ADMIN</button>
        <h1 className="title-gold">GESTION DES MISSIONS</h1>
        <button className="btn-gold" onClick={createNew}><Plus size={18} /> NOUVELLE MISSION</button>
      </header>

      <div className="content">
        <aside className="mission-list">
          {missions.map(m => (
            <div key={m.id} className="mission-item">
              <div className="info" onClick={() => setEditingMission(m)}>
                <FileText size={16} />
                <span>{m.title}</span>
              </div>
              <button className="delete-btn" onClick={() => onDelete(m.id)}><Trash2 size={14} /></button>
            </div>
          ))}
        </aside>

        <main className="editor-form">
          {editingMission ? (
            <div className="form-container">
              <div className="form-grid">
                <div className="field">
                  <label>Titre de la mission</label>
                  <input type="text" value={editingMission.title} onChange={e => setEditingMission({...editingMission, title: e.target.value})} />
                </div>
                <div className="field">
                  <label>Difficulté</label>
                  <select value={editingMission.difficulty} onChange={e => setEditingMission({...editingMission, difficulty: e.target.value})}>
                    <option value="FACILE">FACILE</option>
                    <option value="MOYEN">MOYEN</option>
                    <option value="DIFFICILE">DIFFICILE</option>
                    <option value="ÉLITE">ÉLITE</option>
                  </select>
                </div>
                <div className="field full">
                  <label>Description courte</label>
                  <input type="text" value={editingMission.description} onChange={e => setEditingMission({...editingMission, description: e.target.value})} />
                </div>
                <div className="field full">
                  <label>Briefing complet</label>
                  <textarea rows={5} value={editingMission.briefing} onChange={e => setEditingMission({...editingMission, briefing: e.target.value})} />
                </div>
              </div>
              <button className="btn-gold save-btn" onClick={save}><Save size={18} /> ENREGISTRER LA MISSION</button>
            </div>
          ) : (
            <div className="empty-editor">
              <FileText size={48} color="rgba(255,255,255,0.1)" />
              <p>Sélectionnez une mission à modifier ou créez-en une nouvelle.</p>
              <small>Note: Les missions créées sont sauvegardées localement.</small>
            </div>
          )}
        </main>
      </div>

      <style>{`
        .admin-editor {
          height: 100vh;
          display: flex;
          flex-direction: column;
          background: var(--bg-dark);
        }
        .header {
          padding: 1.5rem 2rem;
          background: var(--bg-panel);
          border-bottom: 2px solid var(--gold);
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .content {
          flex: 1;
          display: grid;
          grid-template-columns: 300px 1fr;
          overflow: hidden;
        }
        .mission-list {
          background: var(--bg-panel);
          border-right: 1px solid var(--border);
          padding: 1rem;
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          overflow-y: auto;
        }
        .mission-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: var(--bg-card);
          padding: 10px;
          border: 1px solid var(--border);
          cursor: pointer;
        }
        .mission-item:hover { border-color: var(--gold); }
        .mission-item .info { display: flex; align-items: center; gap: 10px; flex: 1; }
        .delete-btn { color: var(--red); background: none; padding: 5px; }
        .editor-form { padding: 2.5rem; overflow-y: auto; }
        .form-container { max-width: 800px; margin: 0 auto; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-bottom: 2rem; }
        .field { display: flex; flex-direction: column; gap: 0.5rem; }
        .field.full { grid-column: 1 / -1; }
        label { color: var(--gold); font-size: 0.8rem; font-weight: bold; }
        input, select, textarea {
          background: var(--bg-card);
          border: 1px solid var(--border);
          color: white;
          padding: 10px;
          border-radius: 4px;
        }
        .save-btn { width: 100%; padding: 1rem; margin-top: 1rem; }
        .empty-editor {
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: var(--text-secondary);
          gap: 1rem;
        }
      `}</style>
    </div>
  );
};

export default AdminEditor;
