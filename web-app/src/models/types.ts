export interface TroopUnit {
  id: string;
  name: string;
  type: string;
  icon: string;
  strength: number;
  count: number;
  description: string;
}

export interface EnemyUnit {
  id: string;
  name: string;
  type: string;
  relX: number;
  relY: number;
}

export type CriteriaType = 'POSITION' | 'COHESION' | 'AVOIDANCE' | 'DIVERSITY' | 'CONSOLIDATION';

export interface EvaluationCriteria {
  name: string;
  description: string;
  maxPoints: number;
  type: CriteriaType;
  zoneId?: string;
  requiredTroopType?: string;
  secondTroopType?: string;
  minDistance?: number;
  requiredCount?: number;
}

export interface MapZone {
  id: string;
  name: string;
  description: string;
  x: number;
  y: number;
  width: number;
  height: number;
  strategicValue: 'LOW' | 'MEDIUM' | 'HIGH';
  type: string;
  rotation?: number;
}

export interface Mission {
  id: string;
  title: string;
  description: string;
  difficulty: string;
  mapName: string;
  objective: string;
  briefing: string;
  timeLimit: number;
  terrain: string;
  availableTroops: TroopUnit[];
  criteria: EvaluationCriteria[];
}

export interface SessionResult {
  playerName: string;
  playerRank: string;
  missionTitle: string;
  score: number;
  maxScore: number;
  feedback: string;
  date: string;
}
