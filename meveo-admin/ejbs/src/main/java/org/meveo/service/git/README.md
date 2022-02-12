# Git

Meveo is a git server and client.

Each meveo module has a dedicated repository



## Cloning a module

You can clone locally a module git repository hosted in a meveo instance, let say the default `myModule` module from an instance deployed on `https://mydomain.com/meveo` 

buy using the command 
```
git clone https://mydomain.com/meveo/git/myModule
```
and with the credential of the admin user

you can add the credential directly in the clone url:

```
git clone https://meveo.admin:adminpassword@mydomain.com/meveo/git/myModule
```

To be able to edit the scripts for a given module, you can clone it locally using the code 
of the module as the code of the git repository.

## open the module as a maven project

In order to have all the meveo dependencies available locally, you should add you personal 
github token to your maven settings.xml file.

- [Generate your token](https://github.com/settings/tokens/new) with the following permissions : `read:packages`
- Configure the github repository in your `~/.m2/settings.xml` file : 

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <server>
        <id>github</id>
        <username>GITHUB_ACCOUNT_NAME</username>
        <password>GITHUB_TOKEN</password>
    </server>
</settings>
```

you can now open the project in vscode

```
cd myModule/facets/maven
code .
```

