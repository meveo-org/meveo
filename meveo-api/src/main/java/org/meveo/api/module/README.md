# Modules

A Module is an entity containing CET, CRT, CFT, CEI, Functions, endpoints, notification, files.

It can be exported / imported as a [json file](./../../../../../../../../meveo-api-dto/src/main/java/org/meveo/api/dto/module/MeveoModuleDto.java)

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

### Using notifications

// TODO