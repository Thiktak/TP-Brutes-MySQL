# TP-Brutes MySQL & SQLite

TP "Brutes" - Réseau (Groupe 8)

Authors:
* Woditsch Karl (Client Java/JavaFX)
* Olivarès Georges (Serveur Java)

Le TP consistait à la création d'un client-serveur permettant la gestion et l'utilisation de Brutes : un système de combat interactif faisant évoluer son personnage (brute) avec expérience et bonus

Ce dépot est un fork du [repo maitre](https://github.com/Rauks/TP-Brutes/). Il comprend la gestion multi base de donnée.

## Exemple

Un aperçu du server/client est disponible sur un site internet : http://brutes.ensisa.olivares-georges.fr

## Les base de données
* JDBC SQLite : base de donnée interne, stockée dans un fichier dans le dossier du projet
* JDBC MySQL : base de donnée localhost ou externalisée.

Un fichier de configuration `res/options.xml` est a modifier en fonction de votre utilisation.

**res/options.xml** pour **SQLite**
```
<?xml version="1.0" encoding="UTF-8"?>
<root>
    <server>
        <type>sqlite</type>
        <host>~$bdd.db</host> <!-- Le nom de votre fichier (les fichiers en ~* sont ignorés par git (.gitignore)) -->
    </server>
</root>
```

**res/options.xml** pour **MySQL**
```
<?xml version="1.0" encoding="UTF-8"?>
<root>
    <server>
        <type>mysql</type>
        <host>127.0.0.1/db_name</host>
        <login>root</login>
        <password>root_password</password>
    </server>
</root>
```

*Vous pouvez combiner les deux, mais seul la dernière déclaration du `<server />` sera prise en compte*

Pour travailler plus vite, il existe un fichier `res/options.default.xml`


## Lancer le programme

Simplement exécuter le jar.
Il script comporte un client et un serveur (création automatique d'une base de donnée) permettant de se connecter en **localhost**

Pour lancer les tests associés au programme, faites un `Clean & Build` puis `Run File` sur `test/BrutesTestSequence`


A l'exécution du JAR, un mode console s'ouvrira :
```
TP-Brutes CONSOLE
    [options]    return informations about res/options.xml
    [start]      start the server
    [kill]       kill the server
    [server]     return informations about the server
    [populate]   populate the server with datas into res/*.xml
    [unpopulate] unpopulate the server
    [exit]       kill the server and exit
``` 
N'oubliez pas de **start** le serveur !

## Le protocole

Port **42666**

Pour plus d'informations, consultez le [document relatif au protocol (.docx)](https://github.com/Rauks/TP-Brutes/blob/master/Structure%20du%20protocole%20-%20Groupe%208.docx)
