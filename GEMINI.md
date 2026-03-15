# WWII Tactical Training - Version Web (Statique)

Ce projet est une version web (React/TypeScript) du simulateur d'entraînement tactique Java original, optimisée pour un hébergement 100% statique sur **GitHub Pages**.

## Architecture

- **Frontend**: React 18 avec Vite
- **Données Missions**: Fichier JSON statique (`public/data/missions.json`)
- **Persistence Scores**: Navigateur local (`localStorage`)
- **Style**: Vanilla CSS (thème militaire)
- **Icônes**: Lucide React

### Structure des dossiers

- `web-app/src/`: Code source de l'application.
- `web-app/public/data/missions.json`: Toutes les missions sont stockées ici. Vous pouvez modifier ce fichier pour ajouter de nouvelles missions sans toucher au code.
- `web-app/public/maps/`: Images des cartes.

## Pourquoi utiliser un fichier JSON ?

L'utilisation d'un fichier JSON permet de :
1.  **Garder tout sur GitHub** : Pas besoin de serveur externe ou de base de données payante.
2.  **Facilité de modification** : Vous pouvez éditer les missions directement via l'interface de GitHub en modifiant le fichier JSON.
3.  **Hébergement Gratuit** : Fonctionne parfaitement avec GitHub Pages.

---

## Comment héberger (Vercel - Recommandé)

Vercel est la solution la plus simple car elle automatise tout :

1.  Poussez votre code sur GitHub.
2.  Sur [Vercel](https://vercel.com), importez votre dépôt.
3.  Dans les paramètres, définissez le **Root Directory** sur **`web-app`**.
4.  Vercel s'occupe du reste (Build & Deploy).

## Comment héberger (GitHub Pages)

Si vous préférez GitHub Pages, vous devez utiliser une **GitHub Action** ou lancer ces commandes localement depuis le dossier `web-app` :

```bash
npm install
npm run build
npm run deploy
```

## Fonctionnalités

1.  **Chargement Dynamique** : Les missions sont lues au démarrage depuis le JSON.
2.  **Interface Tactique** : Placement interactif des unités par clic.
3.  **Évaluation** : Système de score identique à la version Java.
4.  **Tableau d'Honneur** : Vos scores sont sauvegardés localement dans votre navigateur.
