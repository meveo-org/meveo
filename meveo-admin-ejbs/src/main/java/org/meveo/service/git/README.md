# Git repositories

Meveo is a git server and client.

Each meveo module has a dedicated repository.

## Cloning in meveo a remote git repository

You typically install an existing meveo module by cloning its git repository in meveo.
In the admin console
* go to "Configuration > Storages > Git repositories" and click the button `New`
* input a code for the repo (that must be unique in your meveo instance)
* input the branch you will uses (for instance on github master might not exist, type main)
* Select if the dev mode is active (see section below)
* input the `https` url of the remote origin, for instance `https://github.com/meveo-org/mv-elastic`
* optionally enter a path in the drive where to store the repository (relative to root path of meveodata where all meveo files are stored and accessible via the menu `Execution > Filexplorer`), by default it is store in the `git` directory
* If your repository requires authentication, input the username and password (for github it is a personal access token)
* If you want to automatically pull from the origin at a predefined interval, you can select a timer in the autopull combo. You create create your own timers in `Services > Jobs > Timers` 
* Click `Save` button

![image](https://user-images.githubusercontent.com/16659140/228106851-e1c7d98a-0421-487c-b264-e8ece831dd14.png)

Meveo will ask again for the username and password, it is usefull if you dont want to store the credentials in meveo). You can leave it blank if your repo is public

![image](https://user-images.githubusercontent.com/16659140/228107079-ce533502-536b-4274-bb08-afafd73fe1f8.png)

You should now see the repository in the list

![image](https://user-images.githubusercontent.com/16659140/228107243-b9200639-7f44-46bb-a2da-3cfd5fdf5eb7.png)

and see its files in the file explorer (menu `Execution > Filexplorer`)

![image](https://user-images.githubusercontent.com/16659140/228107585-ce59b795-8ded-405d-97e4-9964b4e7dd17.png)

### Lock

When the `lock` flag is set the Rest endpoint for checking out a repo will deny the checkout of the repository's default branch

### Dev mode

When a repository has the flag `devMode` set, meveo will detect whenever a file in the repository is overriden (before even commited)
and will emit a `org.meveo.model.dev.FileChangedEvent` that your code might want to observe


## Git API

You can use the git service in 
* meveo web interface, under the menu "Configuration > Storages > Git repositories"
* as a Rest APS, see [these postman examples](https://github.com/meveo-org/meveo/tree/develop/src/test/apiTests/postman/tests/Git)
* directly from your functions, see the usage of `org.meveo.service.git.GitRepositoryService` in this [module function](https://github.com/meveo-org/module-webapprouter/blob/master/facets/java/org/manaty/webapp/WebApp.java)

## Cloning locally a meveo git repository 

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

# Publish to Github
With no pre-existing repo:
1. Create an empty repository on github
2. Add remote origin
```
git remote add origin <remote repository URL>
```
3. Add and commit your changes
```
git add .
```
```
git commit -m "<commit message">
```
4. Push your changes
```
git push origin <branch name>
```
To a pre-existing repository:
Are you the repo owner?
If no:
1.) Navigate to repo in question.
2.) Fork the repo to your own account.
3.) Clone the forked repo to your local environment.
4.) Make your changes and push them to GitHub (to the forked repo).
5.) In the browser, go to your version and verify your updates.
6.) Create a pull request from your fork to the original.
7.) The repo owner will be notified and will review your request.

else if you are the repo owner:

1. Git clone from the git repository you need to push to. Just make sure you create a new directory for the cloned code.
2. Copy the contents of the cloned repository into the local directory that has your current code. Make sure to copy the .git (hidden) file.
3. cd into your local directory and run git remote -v. You should see the remote repository git address.
4. git add -A to add whatever change you require and commit it.
5. Finally git push
