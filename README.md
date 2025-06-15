# Devoir 2

## Technologies

- **Java Spring** : Backend principal, gestion des APIs REST, WebSocket, sécurité, logique métier et accès base de données.
- **React** : Frontend SPA, gestion de l'interface utilisateur, navigation, WebSocket et interactions temps réel.
- **Thymeleaf** : Utilisé pour certaines vues serveur (ex : administration), intégration avec Spring Security pour le rendu conditionnel.
- **Bootstrap** : Framework CSS pour le design responsive et les composants UI (utilisé côté React et Thymeleaf).
- **PostgreSQL** : Base de données relationnelle, stockage des utilisateurs, canaux, invitations, sessions, etc.
- **Spring Mail** : Envoi d'e-mails (ex : notifications, invitations) via le backend Spring.
- **JWT Token + Chaine de filtrage** : Authentification sécurisée via tokens JWT, filtrage des requêtes HTTP côté backend.
- **STOMP + Blob URL** : Utilisation de STOMP/WebSocket pour le chat temps réel et le partage de fichiers/images (Blob URL côté frontend pour l'affichage immédiat sans stockage permanent).
- **Cloudinary** : Stockage et gestion des images d'avatar utilisateurs, intégration côté frontend et backend.

## Démarrage et Test

1. **Backend** :
```bash
./gradlew bootRun
```

2. **Frontend** :
```bash
cd frontend
npm install
npm start
```

3. **Test du Chat** :
   - Ouvrir `http://localhost:3000` dans plusieurs fenêtres
   - Se connecter avec différents comptes
   - Rejoindre un salon
   - Vérifier la communication en temps réel
   - Observer la console pour les logs WebSocket

## Base de données

### USER

```
userId (Long, PK)
lastName (String)
firstName (String)
email (String)
password (String)
avatar (String)
isAdmin (boolean)
```

### CHANNEL

```
title (String)
description (String)
date (LocalDateTime)
durationMinutes (Integer)
channelId (Long, PK)
owner (Long, FK vers USER)
```

### USERCHANNEL (Table de jointure)

```
userChannelId (Long, PK)
user (Long, FK vers USER)
channel (Long, FK vers CHANNEL)
```

### INVITATION

```
status (String)
invitationId (Long, PK)
user (Long, FK vers USER)
channel (Long, FK vers CHANNEL)
```


## Configuration de la Base de Données

### Variables d'environnement requises
- `DB_HOST`: Adresse IP de la base de données (par défaut: 167.86.109.247)
- `DB_PORT`: Port de la base de données (par défaut: 5434)
- `DB_NAME`: Nom de la base de données (par défaut: chatdb)
- `DB_USER`: Nom d'utilisateur PostgreSQL (par défaut: postgres)
- `DB_PASSWORD`: Mot de passe PostgreSQL (par défaut: your_secure_password)
- `APP_PORT`: Port de l'application (par défaut: 8080)
- `JWT_SECRET`: Clé secrète utilisée pour signer les tokens JWT (par défaut: changeme)
- `SPRING_PROFILES_ACTIVE`: Profil Spring actif (par défaut: prod)
- `MAIL_USERNAME`: Identifiant pour le service d'envoi d'e-mails
- `MAIL_PASSWORD`: Mot de passe pour le service d'envoi d'e-mails
- `REACT_APP_CLOUDINARY_CLOUD_NAME`: Nom du compte Cloudinary utilisé par le frontend
- `REACT_APP_API_URL`: URL de l'API utilisée par le frontend (par défaut: http://167.86.109.247:8081/)
- `URL_FRONTEND`: URL du frontend (par défaut: http://localhost:3000/)


## Processus d'Authentification

### Architecture de l'Authentification
L'application utilise JWT (JSON Web Tokens) pour l'authentification, offrant plusieurs avantages :

1. **Stateless** : Pas besoin de stocker les sessions côté serveur
2. **Sécurité** : Les tokens sont signés et peuvent contenir des informations chiffrées
3. **Scalabilité** : Facile à distribuer sur plusieurs serveurs et plusieurs services différents

### Fonctionnement détaillé de JWT

#### Structure d'un JWT
Un JWT est composé de trois parties séparées par des points :
1. **Header** : Contient le type de token et l'algorithme de signature
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```
2. **Payload** : Contient les claims (informations) du token
   ```json
   {
    "roles": [
        "ROLE_USER"
    ],
        "sub": "admin@example.com",
        "iat": 1749822166,
        "exp": 1749908566
    }
   ```
3. **Signature** : Signature HMAC du token
   ```
   HMACSHA256(
     base64UrlEncode(header) + "." +
     base64UrlEncode(payload),
     secret
   )
   ```

#### Processus d'authentification

1. **Login** :
   - L'utilisateur envoie ses identifiants à `/api/auth/login`
   - Le serveur vérifie les identifiants via `UserDetailsService`
   - Si valides, un JWT est généré avec :
     - L'email de l'utilisateur comme subject
     - Ses rôles
     - Une date d'expiration
   - Le token est renvoyé au client

2. **Validation du token** :
   - Le `JwtAuthenticationFilter` intercepte chaque requête
   - Il extrait le token du header `Authorization: Bearer <token>` ou du cookie
   - Vérifie la signature avec la clé secrète
   - Valide la date d'expiration
   - Extrait les informations utilisateur
   - Crée un `Authentication` object pour Spring Security (on l'utilise presque pas)

3. **Sécurité** :
   - Les tokens sont signés avec une clé secrète (`jwt.secret`)
   - Les tokens expirent après 24h (`jwt.expiration`)
   - Les tokens sont transmis via HTTPS
   - Les cookies sont en HTTP-only pour prévenir le vol



## Implémentation du Chat WebSocket

### Architecture WebSocket

L'application utilise une architecture WebSocket avec STOMP (Simple Text Oriented Messaging Protocol) pour le chat en temps réel. Cette approche offre plusieurs avantages :

1. **Single WebSocket Connection** : 
   - Une seule connexion WebSocket par client
   - Utilisation de STOMP pour le routage des messages
   - Plus efficace qu'une connexion WebSocket par salon

2. **Structure des connexions** :
```
[Client] --ws://localhost:8080/ws--> [Serveur WebSocket Unique]
                                    |
                                    |-- /topic/chat/1 (Salon 1)
                                    |-- /topic/chat/2 (Salon 2)
                                    |-- /topic/chat/3 (Salon 3)
```


### Flux de Communication

1. **Connexion** :
   - Client établit une connexion WebSocket via SockJS
   - STOMP est initialisé sur cette connexion
   - Client s'abonne aux topics des salons

2. **Envoi de Message** :
   ```
   [Client] --/app/chat/123/send--> [Serveur] --/topic/chat/123--> [Autres Clients]
   ```

3. **Gestion des Utilisateurs** :
   - Base de données : `user_channel` (membres permanents)
   - Mémoire : `channelSubscriptions` (utilisateurs connectés)


### Gestion des Fichiers dans le Chat

#### Architecture de Gestion des Fichiers

L'application implémente un système de partage de fichiers en temps réel via WebSocket avec les caractéristiques suivantes :

1. **Types de Fichiers Supportés** :
   - Images : JPEG, PNG, GIF
   - Documents : PDF
   - Taille maximale : 5MB par fichier

2. **Flux de Traitement** :
```
[Client] --HTTP POST /api/chat/{channelId}/file--> [Serveur]
         <--WebSocket /topic/chat/{channelId}---- [Serveur]
```


### Gestion des Avatars avec Cloudinary

#### Architecture Cloudinary

L'application utilise Cloudinary pour la gestion et l'optimisation des avatars utilisateurs, offrant une solution robuste et scalable :

1. **Configuration Cloudinary** :
   - Stockage cloud sécurisé des images
   - Transformation automatique des images (redimensionnement, compression)
   - CDN global pour une distribution rapide
   - Variables d'environnement requises :
     - `REACT_APP_CLOUDINARY_CLOUD_NAME` : Nom du compte Cloudinary
     - `CLOUDINARY_API_KEY` : Clé API Cloudinary (backend)
     - `CLOUDINARY_API_SECRET` : Clé secrète Cloudinary (backend)

2. **Flux de Gestion des Avatars** :
   Le processus commence par l'upload d'une image sélectionnée par l'utilisateur via l'interface React. L'image est ensuite envoyée au backend de CLoudinary (pas le notre) via une requête multipart où Cloudinary effectue l'optimisation automatique (redimensionnement et compression). L'URL optimisée est stockée en base de données dans le champ `avatar` de l'utilisateur, puis utilisée par le frontend avec le composant `AdvancedImage` pour l'affichage.

3. **Avantages de Cloudinary** :
   Cloudinary offre des performances optimales avec des images optimisées servies rapidemment, une gestion responsive avec transformation automatique selon la taille d'affichage.






