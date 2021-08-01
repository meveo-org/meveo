# Git

Meveo is a git server and client.

You can clone locally a git repository hosted in a meveo instance, let say the default `Meveo` repository from an instance deployed on `https://mydomain.com/meveo` 

buy using the command 
```
git clone https://mydomain.com/meveo/git/Meveo/
```
and with the credential of the admin user

You can then edit your script on your local IDE (for java, the repo contains a maven pom file)  then when you push it to the meveo instance the script is compiled.
