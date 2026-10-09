## Réponses - exo2

### Q1. Analyse des requêtes sur perdu.com

* **Requête 1 : `GET /\r\n`**
    * **Explication :** Envoi d'une requête minimaliste  sans préciser la version du protocole ni d'en-têtes.
    * **Réponse obtenue :** Le serveur renvoie directement le contenu brut de la page HTML (`Vous êtes perdu ?`) sans en-têtes de réponse HTTP complets, le serveur tolérant ce format simplifié.

* **Requête 2 : `GET / HTTP/1.1\r\n\r\n`**
    * **Explication :** Utilisation de la version HTTP/1.1, mais **sans l'en-tête `Host`** pourtant requis par cette norme.
    * **Réponse obtenue :** Le serveur renvoie une erreur **`400 Bad Request`** (Mauvaise requête) car la spécification HTTP/1.1 exige obligatoirement la présence du champ `Host`.

* **Requête 3 : `GET / HTTP/1.1\r\nHost:perdu.com\r\n\r\n`**
    * **Explication :** Envoi d'une requête HTTP/1.1 complète, conforme et valide, avec la version du protocole et l'en-tête `Host` correctement renseigné.
    * **Réponse obtenue :** Le serveur renvoie une réponse HTTP complète et valide : la ligne de statut (`HTTP/1.1 200 OK`), les métadonnées/en-têtes de réponse (type de contenu, date, serveur), une ligne vide, puis le code HTML de la page.

---

### À quoi sert le champ Host ?
L'en-tête `Host` sert à **indiquer le nom de domaine du serveur** que l'on souhaite joindre.
Il est indispensable avec le protocole HTTP/1.1 pour permettre l'**hébergement virtuel** : il permet à un même serveur physique (possédant une seule adresse IP) d'héberger simultanément plusieurs sites web différents. Grâce à cet en-tête, le serveur sait exactement quel site web il doit afficher en réponse.