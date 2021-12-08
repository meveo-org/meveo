# Git

Meveo is a git server and client.

Each meveo module has a dedicated repository

You can clone locally a module git repository hosted in a meveo instance, let say the default `myModule` module from an instance deployed on `https://mydomain.com/meveo` 

buy using the command 
```
git clone https://mydomain.com/meveo/git/myModule

```
and with the credential of the admin user

You can then edit your script on your local IDE (for java, the repo contains a maven pom file)  then when you push it to the meveo instance the script is compiled.

## Cloning a module

To be able to edit the scripts for a given module, you can clone it locally using the code 
of the module as the code of the git repository.

In order to have all the meveo dependencies available locally, you should add you personal 
github token to your maven settings.xml file.

- [Generate your token](https://github.com/settings/tokens/new) with the following permissions : `read:packages`
- Configure the github repository in your `~/.m2/settings.xml` file : 

```xml
<server>
    <id>github</id>
    <username>GITHUB_ACCOUNT_NAME</username>
    <password>GITHUB_TOKEN</password>
</server>
```
