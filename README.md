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
