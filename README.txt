CAR TP1 : Serveur FTP

Tristan Camus
Sarah Wissocq

Pour lancer le serveur FTP : java -jar m1_s2_car_tp1_camus_wissocq.jar

Port sur lequel se lance le serveur : 1234

Login : azerty
Mot de passe : azerty

Le répertoire courant du serveur est le dossier contenant le .jar.

Notre serveur ftp fonctionne avec la commande unix ftp.

Il permet de se connecter avec ftp via la commande : open adresse_IP 1234
Dans le cas du serveur ouvert en local adresse_IP = localhost
il permet d’utiliser les commandes user, ls, get, put, cd et cdup

pour tester get, un fichier « fichier.txt » est déjà présent sur le serveur.

Il fonctionne en mode passif et en mode actif.
Il est compatible IPV4 et IPV6.

Nous avons aussi implémenté des tests.
