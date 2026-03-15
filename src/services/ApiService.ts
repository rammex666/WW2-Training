import { Mission, SessionResult } from '../models/types';

export const ApiService = {
  // Charger les missions depuis le fichier JSON statique
  getMissions: async (): Promise<Mission[]> => {
    try {
      const response = await fetch('./data/missions.json');
      if (!response.ok) throw new Error('Erreur chargement missions.json');
      return await response.json();
    } catch (err) {
      console.error("Erreur lors de la lecture du fichier JSON des missions.", err);
      return [];
    }
  },

  // Les scores sont gérés localement sur GitHub Pages (Hébergement statique)
  getScores: async (): Promise<SessionResult[]> => {
    const data = JSON.parse(localStorage.getItem('ww2_history') || '[]');
    return data.reverse(); // Plus récent en premier
  },

  saveScore: async (score: SessionResult): Promise<void> => {
    const history = JSON.parse(localStorage.getItem('ww2_history') || '[]');
    history.push(score);
    localStorage.setItem('ww2_history', JSON.stringify(history));
  }
};
