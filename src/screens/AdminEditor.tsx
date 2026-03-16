import React, { useState, useRef } from 'react';
import { Mission, MapZone } from '../models/types';
import { ChevronLeft, Plus, Save, Trash2, FileText, Users, Map as MapIcon, Target, MousePointer2, Square } from 'lucide-react';

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
  
  // Interactive Map Editor State
  const [isDrawing, setIsDrawing] = useState(false);
  const [startPos, setStartPos] = useState({ x: 0, y: 0 });
  const [currentPos, setCurrentPos] = useState({ x: 0, y: 0 });
  const [selectedZoneIndex, setSelectedZoneIndex] = useState<number | null>(null);
  const mapRef = useRef<HTMLDivElement>(null);

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
    if (listName === 'zones') setSelectedZoneIndex(list.length - 1);
  };

  const removeItem = (listName: 'availableTroops' | 'zones' | 'criteria', index: number) => {
    if (!editingMission) return;
    const list = [...(editingMission[listName] || []) as any[]];
    list.splice(index, 1);
    setEditingMission({ ...editingMission, [listName]: list });
    if (listName === 'zones' && selectedZoneIndex === index) setSelectedZoneIndex(null);
  };

  const updateItem = (listName: 'availableTroops' | 'zones' | 'criteria', index: number, field: string, value: any) => {
    if (!editingMission) return;
    const list = [...(editingMission[listName] || []) as any[]];
    list[index] = { ...list[index], [field]: value };
    setEditingMission({ ...editingMission, [listName]: list });
  };

  // -- Interactive Map Handlers
  const handleMapMouseDown = (e: React.MouseEvent) => {
    if (activeTab !== 'ZONES' || !mapRef.current) return;
    
    const rect = mapRef.current.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width;
    const y = (e.clientY - rect.top) / rect.height;
    
    setIsDrawing(true);
    setStartPos({ x, y });
    setCurrentPos({ x, y });
  };

  const handleMapMouseMove = (e: React.MouseEvent) => {
    if (!isDrawing || !mapRef.current) return;
    
    const rect = mapRef.current.getBoundingClientRect();
    const x = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const y = Math.max(0, Math.min(1, (e.clientY - rect.top) / rect.height));
    
    setCurrentPos({ x, y });
  };

  const handleMapMouseUp = () => {
    if (!isDrawing || !editingMission) return;
    setIsDrawing(false);
    
    const x = Math.min(startPos.x, currentPos.x);
    const y = Math.min(startPos.y, currentPos.y);
    const width = Math.abs(currentPos.x - startPos.x);
    const height = Math.abs(currentPos.y - startPos.y);
    
    // Minimum size check
    if (width < 0.01 || height < 0.01) return;

    const newZone: MapZone = {
      id: 'z_' + Date.now(),
      name: 'Zone ' + ((editingMission.zones?.length || 0) + 1),
      description: '',
      x, y, width, height,
      strategicValue: 'MEDIUM',
      type: 'DEFENSIF'
    };

    addItem('zones', newZone);
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
              <div className="info" onClick={() => { setEditingMission(m); setActiveTab('GENERAL'); setSelectedZoneIndex(null); }}>
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
                  <div className="zones-editor-layout">
                    <div className="map-visual-container">
                      <div className="map-toolbar">
                        <span><MousePointer2 size={14}/> Cliquez et glissez pour dessiner une zone</span>
                      </div>
                      <div 
                        className="interactive-map" 
                        ref={mapRef}
                        onMouseDown={handleMapMouseDown}
                        onMouseMove={handleMapMouseMove}
                        onMouseUp={handleMapMouseUp}
                        style={{ backgroundImage: `url(/maps/${editingMission.mapName}.png)` }}
                      >
                        {/* Drawn Zones */}
                        {(editingMission.zones || []).map((z, i) => (
                          <div 
                            key={z.id}
                            className={`map-zone-overlay ${selectedZoneIndex === i ? 'selected' : ''}`}
                            style={{
                              left: `${z.x * 100}%`,
                              top: `${z.y * 100}%`,
                              width: `${z.width * 100}%`,
                              height: `${z.height * 100}%`,
                            }}
                            onClick={(e) => { e.stopPropagation(); setSelectedZoneIndex(i); }}
                          >
                            <span className="zone-label">{z.name}</span>
                          </div>
                        ))}

                        {/* Current Drawing feedback */}
                        {isDrawing && (
                          <div 
                            className="map-zone-overlay drawing"
                            style={{
                              left: `${Math.min(startPos.x, currentPos.x) * 100}%`,
                              top: `${Math.min(startPos.y, currentPos.y) * 100}%`,
                              width: `${Math.abs(currentPos.x - startPos.x) * 100}%`,
                              height: `${Math.abs(currentPos.y - startPos.y) * 100}%`,
                            }}
                          />
                        )}
                      </div>
                    </div>

                    <div className="zone-details-panel">
                      <h3>{selectedZoneIndex !== null ? 'MODIFIER ZONE' : 'SÉLECTIONNEZ UNE ZONE'}</h3>
                      {selectedZoneIndex !== null && editingMission.zones && editingMission.zones[selectedZoneIndex] ? (
                        <div className="zone-form">
                          <div className="field">
                            <label>Nom</label>
                            <input value={editingMission.zones[selectedZoneIndex].name} onChange={e => updateItem('zones', selectedZoneIndex, 'name', e.target.value)} />
                          </div>
                          <div className="field">
                            <label>Type</label>
                            <select value={editingMission.zones[selectedZoneIndex].type} onChange={e => updateItem('zones', selectedZoneIndex, 'type', e.target.value)}>
                              <option value="OFFENSIF">OFFENSIF</option>
                              <option value="DEFENSIF">DEFENSIF</option>
                              <option value="COLLINE">COLLINE</option>
                              <option value="PONT">PONT</option>
                              <option value="VILLE">VILLE</option>
                            </select>
                          </div>
                          <div className="field">
                            <label>Valeur Stratégique</label>
                            <select value={editingMission.zones[selectedZoneIndex].strategicValue} onChange={e => updateItem('zones', selectedZoneIndex, 'strategicValue', e.target.value)}>
                              <option value="LOW">LOW Value</option>
                              <option value="MEDIUM">MEDIUM Value</option>
                              <option value="HIGH">HIGH Value</option>
                            </select>
                          </div>
                          <div className="coords-readout">
                            <span>X: {(editingMission.zones[selectedZoneIndex].x * 100).toFixed(1)}%</span>
                            <span>Y: {(editingMission.zones[selectedZoneIndex].y * 100).toFixed(1)}%</span>
                            <span>W: {(editingMission.zones[selectedZoneIndex].width * 100).toFixed(1)}%</span>
                            <span>H: {(editingMission.zones[selectedZoneIndex].height * 100).toFixed(1)}%</span>
                          </div>
                          <button className="btn-outline danger" onClick={() => removeItem('zones', selectedZoneIndex)}>
                            <Trash2 size={16} /> SUPPRIMER CETTE ZONE
                          </button>
                        </div>
                      ) : (
                        <div className="empty-state">
                          <Square size={32} opacity={0.3} />
                          <p>Cliquez sur une zone existante sur la carte pour la modifier, ou dessinez-en une nouvelle.</p>
                          
                          <div className="zones-list-mini">
                            <label>Zones existantes :</label>
                            {(editingMission.zones || []).map((z, i) => (
                              <div key={z.id} className="zone-chip" onClick={() => setSelectedZoneIndex(i)}>
                                {z.name}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
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
                            <select 
                              value={c.zoneId} 
                              onChange={e => updateItem('criteria', i, 'zoneId', e.target.value)}
                              className="zone-select"
                            >
                              <option value="">-- Sélectionner Zone --</option>
                              {(editingMission.zones || []).map(z => (
                                <option key={z.id} value={z.id}>{z.name}</option>
                              ))}
                            </select>
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
        .delete-btn { color: var(--red); background: none; padding: 5px; cursor: pointer; }
        .editor-area { flex: 1; overflow-y: auto; padding: 2rem; }
        .form-container { max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 2rem; }
        .editor-tabs { display: flex; gap: 5px; border-bottom: 1px solid var(--border); }
        .editor-tabs button { background: var(--bg-card); color: var(--text-secondary); padding: 10px 20px; border: 1px solid var(--border); border-bottom: none; display: flex; align-items: center; gap: 8px; border-radius: 4px 4px 0 0; cursor: pointer; }
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
        
        /* Zones Editor Styles */
        .zones-editor-layout { display: grid; grid-template-columns: 1fr 300px; gap: 2rem; height: 600px; }
        .map-visual-container { display: flex; flex-direction: column; gap: 10px; }
        .map-toolbar { background: rgba(0,0,0,0.5); padding: 8px 15px; font-size: 0.8rem; color: var(--gold); border-left: 3px solid var(--gold); }
        .interactive-map { 
          flex: 1; 
          background-size: contain; 
          background-repeat: no-repeat; 
          background-position: center; 
          position: relative; 
          background-color: #111;
          border: 1px solid var(--border);
          cursor: crosshair;
          user-select: none;
        }
        .map-zone-overlay { 
          position: absolute; 
          border: 2px solid rgba(197, 160, 89, 0.6); 
          background: rgba(197, 160, 89, 0.2);
          pointer-events: auto;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: 0.2s;
        }
        .map-zone-overlay:hover { border-color: var(--gold); background: rgba(197, 160, 89, 0.3); }
        .map-zone-overlay.selected { border-color: #fff; background: rgba(255, 255, 255, 0.2); box-shadow: 0 0 10px rgba(255,255,255,0.5); z-index: 10; }
        .map-zone-overlay.drawing { border: 2px dashed #fff; background: rgba(255,255,255,0.1); }
        .zone-label { color: white; text-shadow: 1px 1px 2px black; font-size: 0.7rem; font-weight: bold; pointer-events: none; text-align: center; padding: 2px; }
        
        .zone-details-panel { background: var(--bg-panel); border: 1px solid var(--border); padding: 1.5rem; display: flex; flex-direction: column; gap: 1.5rem; }
        .zone-form { display: flex; flex-direction: column; gap: 1rem; }
        .coords-readout { display: grid; grid-template-columns: 1fr 1fr; gap: 5px; font-size: 0.7rem; color: var(--text-secondary); margin-bottom: 1rem; }
        .danger { border-color: var(--red); color: var(--red); margin-top: 1rem; }
        .danger:hover { background: rgba(255, 0, 0, 0.1); }
        .empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; gap: 1rem; color: var(--text-secondary); font-size: 0.9rem; height: 100%; }
        .zones-list-mini { width: 100%; margin-top: 2rem; display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
        .zone-chip { background: var(--bg-card); border: 1px solid var(--border); padding: 5px 10px; font-size: 0.8rem; cursor: pointer; border-radius: 20px; width: 100%; text-align: left; }
        .zone-chip:hover { border-color: var(--gold); color: var(--gold); }

        .save-btn { width: 100%; padding: 1.2rem; font-size: 1.1rem; margin-top: 2rem; box-shadow: 0 4px 15px rgba(0,0,0,0.4); cursor: pointer; }
        .empty-editor { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-secondary); gap: 1rem; }
        .zone-select { width: 100%; }
      `}</style>
    </div>
  );
};

export default AdminEditor;
