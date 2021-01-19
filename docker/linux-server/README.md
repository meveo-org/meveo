# Installation on a linux server

This procedure is to install a meveo server with keycloack security server embedded and nginx as a reverse proxy using letsencrypt ssl certificates (generated automatically during installation)

Make sur you have a linux server (tested with debian and ubuntu) with at least 2Go of Ram

Then create a domain and DNS A record that mapp your (sub)domain name to the IP of your server

decide of a name of a server, let say `myserver`

create on the server a `/home/myserver` directory and copy all the files of the directory containing this file

the following commands are executed as root


## Nginx, postgres and Meveo Installation

go to the root dir of the server

```sh
    cd /home/myserver
```

edit the .env file and set the server name, the keycloak server name, email to be used with lets encrypt, ...

start the server

```sh
    chmod +x install.sh
    ./install.sh
```

Enter the meveo container to execute some bash command by running 

```sh
    docker exec -it meveo bash
```

## change admin password

go to `https://<mydomain.org>/auth` login to the administration console using the default `meveo.admin/meveo` credentials, go to users, display all, select `meveo.admin` and in credential page change the password to the one set in .env file

meveo admin console should be accessible at `https://<mydomain.org>/meveo`

