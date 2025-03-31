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

Structuration de l’application en architecture single page (Composants React + APIs REST)

### Chat

Lorsque l’utilisateur clique sur un lien chat une nouvelle fenêtre est ouverte. Elle est composé d’un fil de discussion (TEXTAREA, ….) :
- Liste de messages par ordre d'envoi
- Formulaire pour modifier / créer un message (textarea)
- Liste des utilisateurs connectés (sur la droite de la page)
