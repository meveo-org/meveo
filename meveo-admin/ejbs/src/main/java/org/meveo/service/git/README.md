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
