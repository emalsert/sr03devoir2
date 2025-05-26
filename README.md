# Devoir 2

## Notes

- 50h par personne dont 25 en TD
- BDD postgrest SQL
- Springboot pour communiquer avec la BDD
- Meaven, pom.xml, controller, restcontroller, websocket dans springboot (on l'aura déjà)
- Vue en java avec Thymleaf qui communique avec Springboot (pour la partie Admin)
- Reste de l'interface client en React avec Typescript
- Commit sur git et donner les accès aux prof de TD

Pas de clé primaire sur 2 colonnes -> AI

## Base de données

### Channel

```
channel_id (int, PK, AI)
user_id (int) # Owner
title (string)
description (text)
date (datetime)
duration (time/duration)
```

### User

```
user_id (int, PK, AI)
last_name (varchar)
first_name (varchar)
email (varchar)
password (varchar)
avatar (binary)
is_admin (boolean)
is_connected (boolean)
```

### User_Channel (Junction table)

```
user_channel_id (int)
user_id (int)
channel_id (int)
```

### Invitation
Note : Lorsqu'elle est acceptée ou refusée, l'invitation est supprimée de cette table et le User_Channel est mis à jour (inséré en cas d'acceptation).

```
invitation_id (int, PK, AI)
user_id (int)
channel_id (int)
```

## Interfaces

### Administrateur

#### Fonctionnalités

Via l'interface d'administration :
- Ajout (route : "/admin/create_user")
- Suppression (route : "/admin/remove_user")
- Désactivation (route : "/admin/disable_user")
- Réactivation (route : "/admin/enable_user")

Menu avec les liens suivants :
- Accueil (liste des utilisateurs avec moyen de recherche, raccourcis édition, suppression, désactivation, réactivation)
- Nouveau utilisateur : affiche un formulaire pour ajouter un nouveau
  utilisateur
- Filtres : Utilisateurs désactivés

### Utilisateur

Liste des salons de discussion (invité ou propriétaire)
- Ouvrir le salon (route : "/channel/channel_id")
- Supprimer le salon (route : "/channel/remove_channel")
- Liste des utilisateurs (connectés ou non) (route : "/channel/channel_id/users")

Filtre : Mes chats (propriétaire)
Liste des invitations
Possibilité de créer un salon de discussion : (route : "/user/create_channel")
- Titre
- Description
- Durée de validité
- Invitation de nouveaux utilisateurs

Structuration de l'application en architecture single page (Composants React + APIs REST)

### Chat

Lorsque l'utilisateur clique sur un lien chat une nouvelle fenêtre est ouverte. Elle est composé d'un fil de discussion (TEXTAREA, ….) :
- Liste de messages par ordre d'envoi
- Formulaire pour modifier / créer un message (textarea)
- Liste des utilisateurs connectés (sur la droite de la page)

## Configuration de la Base de Données

### Variables d'environnement requises
- `DB_HOST`: Adresse IP de la base de données (par défaut: 167.86.109.247)
- `DB_PORT`: Port de la base de données (par défaut: 5434)
- `DB_NAME`: Nom de la base de données (par défaut: chatdb)
- `DB_USER`: Nom d'utilisateur PostgreSQL (par défaut: postgres)
- `DB_PASSWORD`: Mot de passe PostgreSQL (par défaut: your_secure_password)
- `APP_PORT`: Port de l'application (par défaut: 8080)

## Gestion des Sessions

### Stockage des Sessions dans la Base de Données
L'application utilise Spring Session JDBC pour stocker les sessions utilisateur dans la base de données PostgreSQL distante. Cette configuration offre plusieurs avantages :

1. **Persistance des sessions** : Les sessions survivent aux redémarrages de l'application
2. **Scalabilité** : Possibilité d'avoir plusieurs instances de l'application
3. **Sécurité** : Meilleure gestion des sessions expirées

### Tables de Session
Deux tables sont automatiquement créées dans la base de données :
- `SPRING_SESSION` : Stocke les informations de base des sessions
- `SPRING_SESSION_ATTRIBUTES` : Stocke les attributs supplémentaires des sessions

### Configuration
La configuration des sessions est définie dans `application.properties` :
```properties
# Session Configuration
spring.session.store-type=jdbc
spring.session.jdbc.initialize-schema=always
spring.session.timeout=30m
```

### Vérification des Sessions
Pour vérifier les sessions actives, connectez-vous à la base de données PostgreSQL via adminer :
`http://167.86.109.247:8080/?pgsql=167.86.109.247%3A5434&username=postgres&db=chatdb`
Ajouter le mdp de la db

### Logging des Sessions
Pour activer le logging des opérations sur les sessions, ajoutez dans `application.properties` :
```properties
logging.level.org.springframework.jdbc.core=DEBUG
```

## Processus d'Authentification

### Architecture de l'Authentification
L'application utilise JWT (JSON Web Tokens) pour l'authentification, offrant plusieurs avantages :

1. **Stateless** : Pas besoin de stocker les sessions côté serveur
2. **Sécurité** : Les tokens sont signés et peuvent contenir des informations chiffrées
3. **Scalabilité** : Facile à distribuer sur plusieurs serveurs

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
     "sub": "user@example.com",
     "roles": ["USER"],
     "exp": 1516239022
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
   - Il extrait le token du header `Authorization: Bearer <token>`
   - Vérifie la signature avec la clé secrète
   - Valide la date d'expiration
   - Extrait les informations utilisateur
   - Crée un `Authentication` object pour Spring Security

3. **Sécurité** :
   - Les tokens sont signés avec une clé secrète (`jwt.secret`)
   - Les tokens expirent après 24h (`jwt.expiration`)
   - Les tokens sont transmis via HTTPS
   - Les cookies sont en HTTP-only pour prévenir le vol

### Configuration JWT

La configuration JWT est définie dans `application.properties` :

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=86400000
```

### Authentification Admin

L'interface d'administration utilise une authentification JWT via cookie :
- Le token est stocké dans un cookie HTTP-only
- Les routes admin sont protégées par le rôle ADMIN
- La déconnexion invalide le token

### Authentification API

L'API REST utilise JWT via le header Authorization :
- Format : `Bearer <token>`
- Les tokens expirent après 24h
- Les routes publiques sont accessibles sans token



### Vérification de l'Authentification
Pour vérifier si un utilisateur est connecté dans une vue Thymeleaf :
```html
<div th:if="${#authentication.principal}">
    Utilisateur connecté: <span th:text="${#authentication.name}"></span>
</div>
```

## Intégration Thymeleaf-Spring Security

Spring Security fournit des expressions spéciales pour Thymeleaf qui permettent d'accéder aux informations d'authentification dans les templates. Pour utiliser ces expressions, il faut ajouter la dépendance `thymeleaf-extras-springsecurity6`.

Les expressions disponibles sont :
1. `${#authentication.name}` : récupère l'email de l'utilisateur connecté
2. `${#authentication.principal}` : accède à l'objet UserDetails complet
3. `${#authentication.isAuthenticated()}` : vérifie si l'utilisateur est authentifié
4. `${#authorization.expression('hasRole(''ADMIN'')')}` : vérifie si l'utilisateur a le rôle ADMIN
5. `${#authentication.principal.fullName}` : affiche le nom complet de l'utilisateur si disponible

Exemple d'utilisation dans un template :
```html
<div th:if="${#authentication.isAuthenticated()}">
    Connecté en tant que: <span th:text="${#authentication.name}"></span>
</div>
<div th:if="${#authorization.expression('hasRole(''ADMIN'')')}">
    <!-- Contenu réservé aux administrateurs -->
</div>
```

Avantages de cette intégration :
- Pas besoin de passer l'utilisateur dans le modèle du contrôleur
- La sécurité est gérée par Spring Security
- Facile à utiliser dans les templates
- Permet d'afficher conditionnellement des éléments selon les rôles

### Gestion de la Déconnexion

La déconnexion est implémentée en utilisant le formulaire de déconnexion de Spring Security. Voici comment cela fonctionne :

1. **Implémentation dans les templates** :
```html
<form th:action="@{/logout}" method="post">
    <button type="submit" class="btn btn-danger">Déconnexion</button>
</form>
```

2. **Sécurité** :
- Utilise la méthode POST (requise par Spring Security)
- Inclut automatiquement le token CSRF
- Invalide la session
- Supprime les cookies d'authentification

3. **Comportement** :
- Après la déconnexion, l'utilisateur est redirigé vers la page de connexion
- Toutes les sessions sont invalidées
- Les tokens d'authentification sont supprimés

4. **Personnalisation** (dans `SecurityConfig`) :
```java
.logout()
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout")
```

#Lancer le projet 
./gradlew bootRun

//npm install avant
npm run start

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

### Backend Implementation

#### 1. Configuration WebSocket
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");  // Pour envoyer
        config.enableSimpleBroker("/topic");              // Pour recevoir
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
}
```

#### 2. Gestion des Connexions
```java
@Component
public class WebSocketEventListener {
    private final ChatWebSocketService chatWebSocketService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Gestion des connexions
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // Gestion des déconnexions
    }
}
```

#### 3. Stockage des Utilisateurs Connectés
```java
@Service
public class ChatWebSocketService {
    // Stockage en mémoire des utilisateurs connectés
    private final Map<Long, Map<String, String>> channelSubscriptions = new ConcurrentHashMap<>();
    // Structure: channelId -> { sessionId -> username }
}
```

### Frontend Implementation

#### 1. Service WebSocket
```javascript
class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
    }

    connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        this.client = new Client({
            webSocketFactory: () => socket,
            // Configuration STOMP
        });
    }

    subscribeToChannel(channelId, onMessage) {
        this.client.subscribe(
            `/topic/chat/${channelId}`,
            (message) => { /* ... */ }
        );
    }
}
```

#### 2. Composant Chat
```jsx
function ChatRoom() {
    useEffect(() => {
        connectWebSocket();
        return () => {
            websocketService.unsubscribeFromChannel(channelId);
            websocketService.disconnect();
        };
    }, [channelId]);
}
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

### Points Importants

1. **Single vs Multiple WebSocket** :
   - Approche initiale (non utilisée) : Un serveur WebSocket par salon
   - Approche actuelle : Un serveur WebSocket avec routage STOMP
   - Avantages : Plus efficace, plus facile à maintenir, meilleure scalabilité

2. **Stockage des Données** :
   - Membres du canal : Table `user_channel` (BDD)
   - Utilisateurs connectés : `ConcurrentHashMap` (mémoire)
   - Messages : Non persistés (en temps réel uniquement)

3. **Sécurité** :
   - CORS configuré pour le frontend
   - Authentification via JWT
   - Validation des accès aux canaux

### Bonnes Pratiques

1. **Organisation du Code** :
   - `config/` : Configuration WebSocket
   - `event/` : Gestionnaires d'événements WebSocket
   - `service/` : Logique métier du chat
   - `controller/` : Points d'entrée des messages

2. **Gestion des Erreurs** :
   - Reconnexion automatique
   - Gestion des déconnexions
   - Validation des messages

3. **Performance** :
   - Une seule connexion WebSocket
   - Utilisation de `ConcurrentHashMap` pour la thread-safety
   - Heartbeats pour maintenir la connexion active

### Démarrage et Test

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

#### Backend Implementation

1. **Validation des Fichiers** :
```java
@Service
public class ChatWebSocketService {
    private static final List<String> ALLOWED_IMAGE_TYPES = 
        List.of("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = 
        List.of("application/pdf");
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public void sendFileToChannel(Long channelId, MultipartFile file, String username) {
        // Validation du type et de la taille
        // Conversion en base64
        // Envoi via WebSocket
    }
}
```

2. **Format des Messages de Fichier** :
```json
{
    "type": "FILE",
    "sender": "username",
    "fileName": "example.jpg",
    "fileType": "image/jpeg",
    "fileSize": 123456,
    "content": "base64_encoded_content",
    "timestamp": 1234567890
}
```

#### Frontend Implementation

1. **Service WebSocket** :
```javascript
class WebSocketService {
    validateFile(file) {
        // Validation du type et de la taille
    }

    async sendFile(channelId, file) {
        const formData = new FormData();
        formData.append('file', file);
        // Envoi via HTTP POST
    }

    displayFile(message) {
        // Conversion base64 -> Blob -> URL
        // Création de l'URL temporaire
    }
}
```

2. **Composant Chat** :
```jsx
function ChatRoom() {
    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        websocketService.validateFile(file);
        // Affichage de la prévisualisation
    };

    const renderFilePreview = (file) => {
        // Affichage différent selon le type
        // Image : prévisualisation
        // PDF : lien de téléchargement
    };
}
```

#### Points Importants

1. **Stockage Temporaire** :
   - Les fichiers ne sont pas persistés
   - Transmis uniquement aux utilisateurs connectés
   - URLs temporaires côté client
   - Perdus après déconnexion

2. **Sécurité** :
   - Validation des types de fichiers
   - Limite de taille
   - Authentification requise
   - Vérification des accès au canal

3. **Performance** :
   - Conversion en base64 pour le transport
   - Prévisualisation des images
   - Gestion optimisée de la mémoire

#### Bonnes Pratiques

1. **Validation** :
   - Vérification du type MIME
   - Contrôle de la taille
   - Nettoyage des noms de fichiers

2. **Gestion des Erreurs** :
   - Messages d'erreur explicites
   - Gestion des échecs d'envoi
   - Nettoyage des ressources

3. **Interface Utilisateur** :
   - Prévisualisation des images
   - Indicateur de progression
   - Messages d'erreur clairs
   - Boutons d'action contextuels

#### Utilisation

1. **Envoi de Fichier** :
   - Cliquer sur le bouton "Fichier"
   - Sélectionner un fichier
   - Valider l'envoi
   - Attendre la confirmation

2. **Réception de Fichier** :
   - Images : affichage direct
   - PDF : lien de téléchargement
   - Affichage du nom et de la taille
   - Horodatage de réception

3. **Limitations** :
   - Taille max : 5MB
   - Types : JPEG, PNG, GIF, PDF
   - Pas de stockage permanent
   - Disponible uniquement pour les utilisateurs connectés

#### Gestion des URLs Blob

1. **Qu'est-ce qu'une URL Blob ?**
   - Un Blob (Binary Large Object) est un objet représentant des données binaires
   - Une URL Blob est une URL temporaire qui pointe vers un Blob en mémoire
   - Format : `blob:http://localhost:3000/550e8400-e29b-41d4-a716-446655440000`

2. **Cycle de Vie** :
```
[Fichier] --> [Base64] --> [Blob] --> [URL Blob] --> [Affichage]
   ^                                                      |
   |                                                      v
   +------------------ [Nettoyage] <------------------ [Utilisation]
```

3. **Utilisation dans l'Application** :
```javascript
// Création d'une URL Blob
const blob = new Blob([byteArray], { type: fileType });
const blobUrl = URL.createObjectURL(blob);

// Nettoyage (important !)
URL.revokeObjectURL(blobUrl);
```

4. **Avantages** :
   - Pas de stockage sur le serveur
   - Affichage immédiat des fichiers
   - Gestion efficace de la mémoire
   - Sécurité (URLs uniques et temporaires)

5. **Points d'Attention** :
   - Les URLs Blob sont temporaires
   - Nécessitent un nettoyage explicite
   - Valides uniquement dans le navigateur qui les a créées
   - Perdues au rechargement de la page

6. **Gestion de la Mémoire** :
   - Création : `URL.createObjectURL(blob)`
   - Nettoyage : `URL.revokeObjectURL(blobUrl)`
   - Nettoyage automatique dans le composant React :
   ```jsx
   useEffect(() => {
       const blobUrl = URL.createObjectURL(blob);
       return () => URL.revokeObjectURL(blobUrl);
   }, [blob]);
   ```

7. **Sécurité** :
   - URLs uniques par session
   - Non accessibles depuis d'autres domaines
   - Expiration à la fermeture du navigateur
   - Pas de persistance sur le disque
