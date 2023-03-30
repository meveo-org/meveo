# Modules

A Module is an entity containing CET, CRT, CFT, CEI, Functions, endpoints, notification, files.

It is essentially a git repository with json files containg the definition of the ontology (data model), endpoints, functions, jobs, notifications, credentials, ...

It also contains facets that allow to use different views of the module in a specific computer language / framework / context
For instance 
* a function written in java will have its code in `/facets/java`
* an entity will have its json schema in `/facets/json` and its java class in `facet/java`
* an endpoint will have a javascript class allowing to easily call the endpoint in a frontend or node program in `/facets/javascript/endpoint/`

For legacy purpose it can also be exported / imported as a [json file](./../../../../../../../../meveo-api-dto/src/main/java/org/meveo/api/dto/module/MeveoModuleDto.java)

## Create a new module

You can create a module in the web admin, under the menu `Configuration > Modules` then click the `New` button

* chose a unique code (this is tyically the name of the git repository you will use, make it long enough to be unique to your purpose)
* write a description that will allow others to understand the purpose of the module
* input the current release using [semver](https://semver.org/)
* optionally input the min and max version of meveo you know your module works with
* click autocommit if you want meveo to create a commit on the git repository associated to the module each time you modify something in the module (add or modify an entity, create or modify a script, an endpoint, etc)... this can create a lot of commits. If you dont set autocommit you can go to the git repository screen of the associated git repository to manually commit files.
* click `Save` button

![image](https://user-images.githubusercontent.com/16659140/228124613-16a097c0-39f1-4216-95f5-88fd4da54d1c.png)

## Associate items to a module

If you previously created items (entites, endpoints,...) that where in the default Meveo module, you can move them to your module by using the `Add Entity` button

![image](https://user-images.githubusercontent.com/16659140/228125761-a89e1ea9-6f5a-4028-a4df-515c68b8f51f.png)

Note that this is not the recommended way though.

It is better to start by creating the module before creating its items. Once a module is created you can select it as the  `current module` in the top right of the screen `Ontology > Entity Customization`, the screen will only display the entities that belong to the current module.
You can display all the entities of all modules by selecting in the filter `belongs to module: Meveo`
![image](https://user-images.githubusercontent.com/16659140/228126493-1f14b586-3d43-43fc-831f-02f08f02455f.png)


## Import existing module

A meveo module is stored in a git repository, you install a meveo module by [cloning its git repository in meveo](https://github.com/meveo-org/meveo/blob/develop/meveo-admin-ejbs/src/main/java/org/meveo/service/git/README.md#cloning-in-meveo-a-remote-git-repository) then [install the module](https://github.com/meveo-org/meveo/blob/develop/meveo-admin-ejbs/src/main/java/org/meveo/service/git/README.md#module-installation).

There is also a legacy way of installing a module by importing a zip file. You import the zip file by clicking the `import from file` button on the module list screen
![image](https://user-images.githubusercontent.com/16659140/228126995-d751c3f0-cb81-46ce-9510-505a2ad2e612.png)

## Clone the module locally

In order to use your IDE to work on your module (typically coding the functions) you first have to [clone its associated git repository](https://github.com/meveo-org/meveo/blob/develop/meveo-admin-ejbs/src/main/java/org/meveo/service/git/README.md#cloning-in-meveo-a-remote-git-repository) (as served by meveo)

### open the module as a maven project

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


## Module lifecycle


### Draft

When a module is created inside a Meveo instance (and not imported), it has a "draft" status which means it is under development.

### Download

A module has the downloaded state if its source file has been imported to the Meveo instance (either by uploading it or downloading it from a remote instance).

### Installation

When a module has the downloaded state, it is possible to install it. That means that all the items contained in the module will be created inside the current Meveo instance.

When installing a module, it exists thee modes: 

- `OVERWRITE` : if an entity on the module to install is in conflict with an existing entity, the existing entity will be updated
- `SKIP` : if an entity on the module to install is in conflict with an existing entity, the existing entity will be not be updated
- `SKIP` : if an entity on the module to install is in conflict with an existing entity, the module installaton will fail and will be rollbacked.

Once the module in installed, it has the state "installed" and should not be modified. It is possible to "fork" an installed module so its statues passes to "draft".

### Publication

It is possible to upload a module directly to another Meveo instance, so it will have the "downloaded" status on the remote instance.

### Release

When working on a draft module, we have the possibility to create a realse of a module. A release is a snapshot of the module at the current state so that it is stored serialized in a separated table along with the corresponding version number.

### Enable / disable 

When enabling / disabling a module, all of its items will be enabled / disabled. For disabling, it will only occurs if they don't belongs to another module

## Customizing the lifecycle

### Using the module script

A module can be linked to a script instance through the `script` property. This script should implement [ModuleScriptInterface](./../../../../../../../../meveo-admin/ejbs/src/main/java/org/meveo/service/script/module/ModuleScriptInterface.java) or extends [ModuleScript](../../../../../../../../meveo-admin/ejbs/src/main/java/org/meveo/service/script/module/ModuleScript.java)

The `ModuleScriptInterface` provides several method to react to the module lifecycle : 

- `preInstallModule`: called before the module installation
- `postInstallModule`: called after the module installation
- `preUninstallModule`: called before the module uninstallation
- `postUninstallModule`: called after the module uninstallation
- `preEnableModule`: called before the module is enabled
- `postEnableModule`: called after the module is enabled
- `preDisableModule`: called before the module is deactivated
- `postDisableModule`: called after the module is deactivated
- `prePull`,`postPull`: called before and after the [git repository linked to the module is pulled](https://github.com/meveo-org/meveo/blob/develop/meveo-admin-ejbs/src/main/java/org/meveo/service/git/README.md#pull)

### Using notifications

// TODO
