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
L'application utilise Spring Security pour gérer l'authentification. Le processus est composé de trois parties principales :

1. **UserDetailsServiceImpl** : Service qui charge les informations de l'utilisateur
2. **SecurityConfig** : Configuration de la sécurité
3. **LoginController** : Gestion de l'interface de connexion

### Flux d'Authentification

1. **Affichage du Formulaire de Connexion**
   ```java
   // LoginController.java
   @GetMapping("/login")
   public String login(@RequestParam(value = "error", required = false) String error, Model model) {
       if (error != null) {
           logger.error("Échec de la tentative de connexion");
           model.addAttribute("error", true);
       }
       return "login";
   }
   ```

2. **Soumission du Formulaire**
   - Spring Security intercepte automatiquement la requête POST
   - Appelle `UserDetailsServiceImpl.loadUserByUsername()`
   - Vérifie les identifiants et les rôles
   - Note: Aucune méthode POST explicite n'est nécessaire dans le contrôleur car Spring Security gère automatiquement la soumission du formulaire

3. **Gestion des Rôles**
   ```java
   // UserDetailsServiceImpl.java
   String[] roles = user.isAdmin() ? new String[]{"ADMIN", "USER"} : new String[]{"USER"};
   ```

4. **Redirection**
   - Succès : Redirection vers la page d'accueil
   - Échec : Redirection vers `/login?error`

### Protection des Routes
La protection des routes est entièrement gérée par Spring Security via la configuration dans SecurityConfig. Spring Security intercepte toutes les requêtes HTTP et vérifie les autorisations avant d'y répondre.

```java
// A faire : Configuration des routes protégées dans SecurityConfig.java
```

Cette configuration permet à Spring Security de automatiquement:
1. Intercepter automatiquement toutes les requêtes
2. Vérifier les rôles et permissions
3. Rediriger vers la page de login si nécessaire
4. Protéger contre les accès non autorisés


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

