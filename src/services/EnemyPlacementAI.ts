import { MapZone, EnemyUnit, Mission } from '../models/types';

export const EnemyPlacementAI = {
  generateEnemies: (_mission: Mission, zones: MapZone[]): EnemyUnit[] => {
    const enemies: EnemyUnit[] = [];
    const enemyZones = zones.filter(z => z.type === 'DEFENSIF' || z.type === 'PONT' || z.strategicValue === 'HIGH');
    
    // Ensure some enemies are on high value points
    enemyZones.forEach((zone, index) => {
      enemies.push({
        id: `enemy_${index}`,
        name: `Garnison ${zone.name}`,
        type: index % 2 === 0 ? "Infanterie" : "Blindé",
        relX: zone.x + zone.width / 2,
        relY: zone.y + zone.height / 2
      });
    });

    return enemies;
  }
};
