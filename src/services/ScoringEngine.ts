import { Mission, MapZone, TroopUnit, EvaluationCriteria } from '../models/types';

export interface CriteriaResult {
  name: string;
  earned: number;
  max: number;
  satisfied: boolean;
}

export interface EvaluationResult {
  totalScore: number;
  maxScore: number;
  feedbackLines: string[];
  criteriaResults: CriteriaResult[];
  tacticalAdvice: string;
  percentage: number;
  grade: string;
}

export const ScoringEngine = {
  evaluate: (mission: Mission, placedTroopZones: Record<string, string>, zones: MapZone[]): EvaluationResult => {
    let totalScore = 0;
    const maxScore = mission.criteria.reduce((sum, c) => sum + c.maxPoints, 0);
    const feedbackLines: string[] = [];
    const criteriaResults: CriteriaResult[] = [];

    const troopMap = new Map<string, TroopUnit>();
    mission.availableTroops.forEach(t => troopMap.set(t.id, t));

    const zoneMap = new Map<string, MapZone>();
    zones.forEach(z => zoneMap.set(z.id, z));

    mission.criteria.forEach(criteria => {
      const evalResult = ScoringEngine.evaluateCriteria(criteria, placedTroopZones, troopMap, zoneMap, zones);
      totalScore += evalResult.points;
      
      const icon = evalResult.points >= criteria.maxPoints ? "✅" : (evalResult.points > 0 ? "⚠" : "❌");
      feedbackLines.push(`${icon} ${criteria.name} : +${evalResult.points} pts ${evalResult.feedbackDetail ? `(${evalResult.feedbackDetail})` : ""}`);
      
      criteriaResults.push({
        name: criteria.name,
        earned: evalResult.points,
        max: criteria.maxPoints,
        satisfied: evalResult.satisfied
      });
    });

    // Global Bonuses
    const bonus = ScoringEngine.evaluateGlobalFactors(mission, placedTroopZones, zoneMap, feedbackLines);
    totalScore += bonus;

    totalScore = Math.max(0, totalScore);
    const percentage = maxScore > 0 ? (totalScore / maxScore) * 100 : 0;

    return {
      totalScore,
      maxScore,
      feedbackLines,
      criteriaResults,
      tacticalAdvice: "Analysez le terrain et assurez-vous de la cohérence de vos unités.",
      percentage,
      grade: ScoringEngine.getGrade(percentage)
    };
  },

  evaluateCriteria: (c: EvaluationCriteria, placed: Record<string, string>, troops: Map<string, TroopUnit>, zoneMap: Map<string, MapZone>, allZones: MapZone[]) => {
    switch (c.type) {
      case 'POSITION': return ScoringEngine.evalPosition(c, placed, troops, allZones);
      case 'AVOIDANCE': return ScoringEngine.evalAvoidance(c, placed);
      case 'CONSOLIDATION': return ScoringEngine.evalConsolidation(c, placed, troops);
      case 'COHESION': return ScoringEngine.evalCohesion(c, placed, troops, zoneMap);
      case 'DIVERSITY': return ScoringEngine.evalDiversity(c, placed, troops);
      default: return { points: 0, satisfied: false, feedbackDetail: "Type inconnu" };
    }
  },

  evalPosition: (c: EvaluationCriteria, placed: Record<string, string>, troops: Map<string, TroopUnit>, allZones: MapZone[]) => {
    let inZone = false;
    let rightType = false;

    for (const [troopId, zoneId] of Object.entries(placed)) {
      if (zoneId === c.zoneId) {
        inZone = true;
        if (!c.requiredTroopType || troops.get(troopId)?.type === c.requiredTroopType) {
          rightType = true;
          break;
        }
      }
    }

    if (inZone && rightType) return { points: c.maxPoints, satisfied: true };
    if (inZone && !rightType) return { points: Math.floor(c.maxPoints / 2), satisfied: true, feedbackDetail: "mauvais type d'unité" };
    
    // Proximity check
    for (const zoneId of Object.values(placed)) {
      if (ScoringEngine.isNearOptimalZone(zoneId, c.zoneId!, allZones)) {
        return { points: Math.floor(c.maxPoints / 3), satisfied: false, feedbackDetail: "position sous-optimale" };
      }
    }

    return { points: 0, satisfied: false, feedbackDetail: "zone non occupée" };
  },

  evalAvoidance: (c: EvaluationCriteria, placed: Record<string, string>) => {
    const count = Object.values(placed).filter(zid => zid === c.zoneId).length;
    if (count === 0) return { points: c.maxPoints, satisfied: true };
    return { points: 0, satisfied: false, feedbackDetail: `${count} unité(s) exposée(s)` };
  },

  evalConsolidation: (c: EvaluationCriteria, placed: Record<string, string>, troops: Map<string, TroopUnit>) => {
    const count = Object.entries(placed).filter(([tid, zid]) => 
      zid === c.zoneId && (!c.requiredTroopType || troops.get(tid)?.type === c.requiredTroopType)
    ).length;

    if (count >= (c.requiredCount || 1)) return { points: c.maxPoints, satisfied: true };
    if (count > 0) return { points: Math.floor(c.maxPoints / 2), satisfied: false, feedbackDetail: `${count}/${c.requiredCount} unités` };
    return { points: 0, satisfied: false, feedbackDetail: "aucune unité" };
  },

  evalCohesion: (c: EvaluationCriteria, placed: Record<string, string>, troops: Map<string, TroopUnit>, zoneMap: Map<string, MapZone>) => {
    const list1 = Object.entries(placed).filter(([tid]) => troops.get(tid)?.type === c.requiredTroopType).map(([_, zid]) => zoneMap.get(zid)!);
    const list2 = Object.entries(placed).filter(([tid]) => troops.get(tid)?.type === c.secondTroopType).map(([_, zid]) => zoneMap.get(zid)!);

    if (list1.length === 0 || list2.length === 0) return { points: 0, satisfied: false, feedbackDetail: "unités manquantes" };

    let closeEnough = false;
    for (const z1 of list1) {
      for (const z2 of list2) {
        const d = Math.hypot(z1.x - z2.x, z1.y - z2.y);
        if (d <= (c.minDistance || 0.2)) {
          closeEnough = true;
          break;
        }
      }
      if (closeEnough) break;
    }

    if (closeEnough) return { points: c.maxPoints, satisfied: true };
    return { points: 0, satisfied: false, feedbackDetail: "trop éloignées" };
  },

  evalDiversity: (c: EvaluationCriteria, placed: Record<string, string>, troops: Map<string, TroopUnit>) => {
    const types = new Set(Object.entries(placed).filter(([_, zid]) => zid === c.zoneId).map(([tid]) => troops.get(tid)!.type));
    if (types.size >= (c.requiredCount || 1)) return { points: c.maxPoints, satisfied: true };
    return { points: 0, satisfied: false, feedbackDetail: `${types.size}/${c.requiredCount} types` };
  },

  evaluateGlobalFactors: (mission: Mission, placed: Record<string, string>, zoneMap: Map<string, MapZone>, feedback: string[]) => {
    let bonus = 0;
    if (Object.keys(placed).length === mission.availableTroops.length && mission.availableTroops.length > 0) {
      bonus += 20;
      feedback.push("🎖 Bonus : Déploiement complet (+20 pts)");
    }

    const highValueZones = Array.from(zoneMap.values()).filter(z => z.strategicValue === 'HIGH');
    const occupiedHighValue = new Set(Object.values(placed).filter(zid => zoneMap.get(zid)?.strategicValue === 'HIGH')).size;

    if (highValueZones.length > 0 && occupiedHighValue === 0) {
      bonus -= 30;
      feedback.push("⚠ Pénalité : Aucun point stratégique majeur occupé (-30 pts)");
    } else if (occupiedHighValue > 0) {
      const stratBonus = occupiedHighValue * 5;
      bonus += stratBonus;
      feedback.push(`⭐ Bonus : Occupation stratégique (+${stratBonus} pts)`);
    }

    return bonus;
  },

  isNearOptimalZone: (zoneId: string, targetZoneId: string, zones: MapZone[]) => {
    const p = zones.find(z => z.id === zoneId);
    const t = zones.find(z => z.id === targetZoneId);
    if (!p || !t) return false;
    return Math.hypot(p.x - t.x, p.y - t.y) < 0.20;
  },

  getGrade: (pct: number) => {
    if (pct >= 90) return "EXCELLENCE";
    if (pct >= 75) return "TRÈS BIEN";
    if (pct >= 60) return "BIEN";
    if (pct >= 40) return "PASSABLE";
    return "ÉCHEC";
  },

  evaluateByPosition: (mission: Mission, troopPositions: Record<string, {x: number, y: number}>, zones: MapZone[]): EvaluationResult => {
    const placedTroopZones: Record<string, string> = {};
    for (const [tid, pos] of Object.entries(troopPositions)) {
      const zoneId = ScoringEngine.findZoneAtPosition(pos.x, pos.y, zones);
      if (zoneId) placedTroopZones[tid] = zoneId;
    }
    return ScoringEngine.evaluate(mission, placedTroopZones, zones);
  },

  findZoneAtPosition: (relX: number, relY: number, zones: MapZone[]): string | null => {
    // 1. Precise containment
    for (const z of zones) {
      if (relX >= z.x && relX <= z.x + z.width && relY >= z.y && relY <= z.y + z.height) {
        return z.id;
      }
    }
    // 2. Nearest center
    let minD = Infinity;
    let nearest: string | null = null;
    for (const z of zones) {
      const cx = z.x + z.width / 2;
      const cy = z.y + z.height / 2;
      const d = Math.hypot(relX - cx, relY - cy);
      if (d < minD) { minD = d; nearest = z.id; }
    }
    return nearest;
  }
};
