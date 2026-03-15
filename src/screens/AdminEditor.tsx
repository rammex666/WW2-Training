import React, { useState } from 'react';
import { Mission, TroopUnit, MapZone, EvaluationCriteria } from '../models/types';
import { ChevronLeft, Plus, Save, Trash2, FileText, Users, Map as MapIcon, Target } from 'lucide-react';

interface AdminEditorProps {
  onBack: () => void;
  onSave: (mission: Mission) => void;
  missions: Mission[];
  onDelete: (id: string) => void;
}

type Tab = 'GENERAL' | 'TROOPS' | 'ZONES' | 'CRITERIA';

const AdminEditor: React.FC<AdminEditorProps> = ({ onBack, onSave, missions, onDelete }) => {
  const [editingMission, setEditingMission] = useState<Partial<Mission> | null>(null);
  const [activeTab, setActiveTab] = useState<Tab>('GENERAL');

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
      criteria: [],
      zones: []
    });
    setActiveTab('GENERAL');
  };

  const save = () => {
    if (editingMission && editingMission.id) {
      onSave(editingMission as Mission);
      setEditingMission(null);
    }
  };

  // -- Helpers Gestion de Listes
  const addItem = (listName: 'availableTroops' | 'zones' | 'criteria', defaultItem: any) => {
    if (!editingMission) return;
    const list = [...(editingMission[listName] || []) as any[], defaultItem];
    setEditingMission({ ...editingMission, [listName]: list });
  };

  const removeItem = (listName: 'availableTroops' | 'zones' | 'criteria', index: number) => {
    if (!editingMission) return;
    const list = [...(editingMission[listName] || []) as any[]];
    list.splice(index, 1);
    setEditingMission({ ...editingMission, [listName]: list });
  };

  const updateItem = (listName: 'availableTroops' | 'zones' | 'criteria', index: number, field: string, value: any) => {
    if (!editingMission) return;
    const list = [...(editingMission[listName] || []) as any[]];
    list[index] = { ...list[index], [field]: value };
    setEditingMission({ ...editingMission, [listName]: list });
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
            <div key={m.id} className={`mission-item ${editingMission?.id === m.id ? 'active' : ''}`}>
              <div className="info" onClick={() => { setEditingMission(m); setActiveTab('GENERAL'); }}>
                <FileText size={16} />
                <span>{m.title}</span>
              </div>
              <button className="delete-btn" onClick={() => onDelete(m.id)}><Trash2 size={14} /></button>
            </div>
          ))}
        </aside>

        <main className="editor-area">
          {editingMission ? (
            <div className="form-container">
              <nav className="editor-tabs">
                <button className={activeTab === 'GENERAL' ? 'active' : ''} onClick={() => setActiveTab('GENERAL')}><FileText size={16}/> Infos</button>
                <button className={activeTab === 'TROOPS' ? 'active' : ''} onClick={() => setActiveTab('TROOPS')}><Users size={16}/> Unités</button>
                <button className={activeTab === 'ZONES' ? 'active' : ''} onClick={() => setActiveTab('ZONES')}><MapIcon size={16}/> Zones (Emplacements)</button>
                <button className={activeTab === 'CRITERIA' ? 'active' : ''} onClick={() => setActiveTab('CRITERIA')}><Target size={16}/> Objectifs</button>
              </nav>

              <div className="tab-content">
                {activeTab === 'GENERAL' && (
                  <div className="form-grid">
                    <div className="field">
                      <label>Titre</label>
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
                    <div className="field">
                      <label>Terrain</label>
                      <input type="text" value={editingMission.terrain} onChange={e => setEditingMission({...editingMission, terrain: e.target.value})} />
                    </div>
                    <div className="field">
                      <label>Map (Fichier PNG)</label>
                      <select value={editingMission.mapName} onChange={e => setEditingMission({...editingMission, mapName: e.target.value})}>
                        <option value="berlin">Berlin</option>
                        <option value="falaise">Falaise</option>
                      </select>
                    </div>
                    <div className="field full">
                      <label>Briefing complet</label>
                      <textarea rows={5} value={editingMission.briefing} onChange={e => setEditingMission({...editingMission, briefing: e.target.value})} />
                    </div>
                  </div>
                )}

                {activeTab === 'TROOPS' && (
                  <div className="list-editor">
                    <div className="list-header">
                      <h3>UNITÉS DISPONIBLES</h3>
                      <button className="btn-outline mini" onClick={() => addItem('availableTroops', { id: 't_'+Date.now(), name: 'Nouvelle Unité', type: 'INFANTRY', icon: '⚔', strength: 50, count: 1, description: '' })}>+ Ajouter Unité</button>
                    </div>
                    <div className="items">
                      {(editingMission.availableTroops || []).map((t, i) => (
                        <div key={i} className="list-item">
                          <input className="mini" value={t.icon} onChange={e => updateItem('availableTroops', i, 'icon', e.target.value)} title="Icone" />
                          <input value={t.name} onChange={e => updateItem('availableTroops', i, 'name', e.target.value)} placeholder="Nom" />
                          <select value={t.type} onChange={e => updateItem('availableTroops', i, 'type', e.target.value)}>
                            <option value="INFANTRY">INFANTRY</option>
                            <option value="TANK">TANK</option>
                            <option value="SNIPER">SNIPER</option>
                            <option value="ARTILLERY">ARTILLERY</option>
                            <option value="MEDIC">MEDIC</option>
                            <option value="ENGINEER">ENGINEER</option>
                          </select>
                          <input type="number" value={t.strength} onChange={e => updateItem('availableTroops', i, 'strength', parseInt(e.target.value))} title="Force" />
                          <button className="delete-btn" onClick={() => removeItem('availableTroops', i)}><Trash2 size={14}/></button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {activeTab === 'ZONES' && (
                  <div className="list-editor">
                    <div className="list-header">
                      <h3>ZONES STRATÉGIQUES (EMPLACEMENTS)</h3>
                      <button className="btn-outline mini" onClick={() => addItem('zones', { id: 'z_'+Date.now(), name: 'Nouvelle Zone', x: 0.1, y: 0.1, width: 0.2, height: 0.2, strategicValue: 'MEDIUM', type: 'DEFENSIF', description: '' })}>+ Ajouter Zone</button>
                    </div>
                    <p className="hint">Les coordonnées (X, Y) et tailles sont en pourcentage (ex: 0.5 = 50%).</p>
                    <div className="items">
                      {(editingMission.zones || []).map((z, i) => (
                        <div key={i} className="list-item complex">
                          <div className="row">
                            <input value={z.name} onChange={e => updateItem('zones', i, 'name', e.target.value)} placeholder="Nom de la zone" />
                            <select value={z.type} onChange={e => updateItem('zones', i, 'type', e.target.value)}>
                              <option value="OFFENSIF">OFFENSIF</option>
                              <option value="DEFENSIF">DEFENSIF</option>
                              <option value="COLLINE">COLLINE</option>
                              <option value="PONT">PONT</option>
                              <option value="VILLE">VILLE</option>
                            </select>
                            <select value={z.strategicValue} onChange={e => updateItem('zones', i, 'strategicValue', e.target.value)}>
                              <option value="LOW">LOW Value</option>
                              <option value="MEDIUM">MEDIUM Value</option>
                              <option value="HIGH">HIGH Value</option>
                            </select>
                            <button className="delete-btn" onClick={() => removeItem('zones', i)}><Trash2 size={14}/></button>
                          </div>
                          <div className="row coords">
                            <label>X: <input type="number" step="0.01" value={z.x} onChange={e => updateItem('zones', i, 'x', parseFloat(e.target.value))} /></label>
                            <label>Y: <input type="number" step="0.01" value={z.y} onChange={e => updateItem('zones', i, 'y', parseFloat(e.target.value))} /></label>
                            <label>Largeur: <input type="number" step="0.01" value={z.width} onChange={e => updateItem('zones', i, 'width', parseFloat(e.target.value))} /></label>
                            <label>Hauteur: <input type="number" step="0.01" value={z.height} onChange={e => updateItem('zones', i, 'height', parseFloat(e.target.value))} /></label>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {activeTab === 'CRITERIA' && (
                  <div className="list-editor">
                    <div className="list-header">
                      <h3>CRITÈRES DE SCORE</h3>
                      <button className="btn-outline mini" onClick={() => addItem('criteria', { name: 'Objectif', description: '', maxPoints: 50, type: 'POSITION', zoneId: '', requiredTroopType: 'INFANTRY' })}>+ Ajouter Critère</button>
                    </div>
                    <div className="items">
                      {(editingMission.criteria || []).map((c, i) => (
                        <div key={i} className="list-item complex">
                          <div className="row">
                            <input value={c.name} onChange={e => updateItem('criteria', i, 'name', e.target.value)} placeholder="Nom de l'objectif" />
                            <select value={c.type} onChange={e => updateItem('criteria', i, 'type', e.target.value)}>
                              <option value="POSITION">POSITION (Unité dans zone)</option>
                              <option value="COHESION">COHÉSION (Proximité)</option>
                              <option value="AVOIDANCE">ÉVITEMENT (Zone vide)</option>
                              <option value="CONSOLIDATION">CONSOLIDATION (Nb unités)</option>
                              <option value="DIVERSITY">DIVERSITÉ (Types différents)</option>
                            </select>
                            <input type="number" value={c.maxPoints} onChange={e => updateItem('criteria', i, 'maxPoints', parseInt(e.target.value))} title="Points" />
                            <button className="delete-btn" onClick={() => removeItem('criteria', i)}><Trash2 size={14}/></button>
                          </div>
                          <div className="row">
                            <input value={c.description} onChange={e => updateItem('criteria', i, 'description', e.target.value)} placeholder="Description pour le joueur" style={{flex: 1}}/>
                            <input value={c.zoneId} onChange={e => updateItem('criteria', i, 'zoneId', e.target.value)} placeholder="ID Zone cible" />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <button className="btn-gold save-btn" onClick={save}><Save size={18} /> ENREGISTRER TOUTE LA MISSION</button>
            </div>
          ) : (
            <div className="empty-editor">
              <FileText size={48} color="rgba(255,255,255,0.1)" />
              <p>Sélectionnez une mission à gauche pour la modifier ou cliquez sur "Nouvelle Mission".</p>
            </div>
          )}
        </main>
      </div>

      <style>{`
        .admin-editor { height: 100vh; display: flex; flex-direction: column; background: var(--bg-dark); color: white; }
        .header { padding: 1rem 2rem; background: var(--bg-panel); border-bottom: 2px solid var(--gold); display: flex; justify-content: space-between; align-items: center; }
        .content { flex: 1; display: grid; grid-template-columns: 280px 1fr; overflow: hidden; }
        .mission-list { background: var(--bg-panel); border-right: 1px solid var(--border); padding: 1rem; display: flex; flex-direction: column; gap: 0.5rem; overflow-y: auto; }
        .mission-item { display: flex; align-items: center; justify-content: space-between; background: var(--bg-card); padding: 10px; border: 1px solid var(--border); cursor: pointer; transition: 0.2s; }
        .mission-item:hover, .mission-item.active { border-color: var(--gold); background: rgba(197, 160, 89, 0.1); }
        .mission-item .info { display: flex; align-items: center; gap: 10px; flex: 1; }
        .delete-btn { color: var(--red); background: none; padding: 5px; }
        .editor-area { flex: 1; overflow-y: auto; padding: 2rem; }
        .form-container { max-width: 900px; margin: 0 auto; display: flex; flex-direction: column; gap: 2rem; }
        .editor-tabs { display: flex; gap: 5px; border-bottom: 1px solid var(--border); }
        .editor-tabs button { background: var(--bg-card); color: var(--text-secondary); padding: 10px 20px; border: 1px solid var(--border); border-bottom: none; display: flex; align-items: center; gap: 8px; border-radius: 4px 4px 0 0; }
        .editor-tabs button.active { background: var(--bg-panel); color: var(--gold); border-color: var(--gold); }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
        .field { display: flex; flex-direction: column; gap: 0.5rem; }
        .field.full { grid-column: 1 / -1; }
        label { color: var(--gold); font-size: 0.75rem; font-weight: bold; text-transform: uppercase; }
        input, select, textarea { background: var(--bg-card); border: 1px solid var(--border); color: white; padding: 10px; border-radius: 4px; }
        input:focus { border-color: var(--gold); outline: none; }
        .list-editor { display: flex; flex-direction: column; gap: 1rem; }
        .list-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem; }
        .mini { width: 50px !important; text-align: center; }
        .items { display: flex; flex-direction: column; gap: 0.8rem; }
        .list-item { display: flex; gap: 10px; align-items: center; background: rgba(255,255,255,0.02); padding: 8px; border: 1px solid var(--border); }
        .list-item.complex { flex-direction: column; align-items: stretch; gap: 10px; }
        .row { display: flex; gap: 10px; align-items: center; }
        .coords input { width: 80px; }
        .hint { font-size: 0.8rem; color: var(--text-secondary); font-style: italic; }
        .save-btn { width: 100%; padding: 1.2rem; font-size: 1.1rem; margin-top: 2rem; box-shadow: 0 4px 15px rgba(0,0,0,0.4); }
        .empty-editor { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-secondary); gap: 1rem; }
      `}</style>
    </div>
  );
};

export default AdminEditor;
