# Crosstorage Persistence

[I. Persistence using rest API](I-persistence-using-rest-api)
- [I.1. Persisting an entity](#i1-persisting-an-entity)
- [I.2. Find an entity](#i2-find-an-entity)
	- [I.2.1. By UUID](#i21-by-uuid)
	- [I.2.2. By values](#i22-by-values)
- [I.3. List entities](#i3-list-entities)
- [I.4. Remove an entity](#i4-remove-an-entity)

[II. Persistence in Scripts](#ii-persistence-in-scripts)
- [II.1. Persisting an entity](#ii1-persisting-an-entity)
- [II.2. Find an entity](#ii2-find-an-entity)
	- [II.2.1. By UUID](#ii21-by-uuid)
	- [II.2.2. By values](#ii22-by-values)
- [II.3. List entities](#ii3-list-entities)
- [II.4. Remove an entity](#ii4-remove-an-entity)
- [II.5. CREATE, UPDATE, DELETE events](#i5-create-update-delete-events)

# I. Persistence using rest API

The crosstorage persistence API is accessible at `{{protocol}}://{{hostname}}:{{port}}/{{webContext}}/api/rest/:project/persistence` 

with typically 
```
{{protocol}}=https
{{hostname}}=mydomain.com
{{port}}=443
{{webContext}}=meveo
:project = default  (the repository to use for persistence)
```
As the services are secrured, you must either use basic authentication (if allowed on your instance) or bearer token.

## I.1. Persisting an entity


## I.2. Find an entity

### I.2.1. By UUID

### I.2.2. By values

## I.3. List entities

### Request
```
POST {{protocol}}://{{hostname}}:{{port}}/{{webContext}}/api/rest/:project/persistence/:cet/list
```

Headers
```
Base64-Encode : boolean
```

Path parameters
```
:cet : the code of the Custom entity template
```

Query parameters
```
withCount : boolean
```

### Response 
body : either an array of serialized entities of type `:cet` if `withCount==false` or an  object with properties `count`, that givens the total number of entities  and `result` the list of entities

## I.4. Remove an entity

# II. Persistence in Scripts

## II.1. Persisting an entity

To persist an entity, use `CrossStorageApi#createOrUpdate` with these parameters :

- *repository* : `Repository` - repository where to save the entity
- *value* : `Object` - the object to persist. The simple class name of the object should correspond to an existing `CustomEntityTemplate`.

*Note: you can set the UUID of the entity instance, this can have two effects*

- *force the entity to have a certain UUID if the entity does not exists*
- *speed-up the update process by enabling the persistence engine to retrieve the entity to update by UUID if the entity already exists*

Imagine you created a CET (Custom Entity Template), then a java class MyCet has been created with package org.meveo.model.customEntities

**Example**:

```java

import javax.inject.Inject;
import org.meveo.model.customEntities.MyCet;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.api.persistence.CrossStorageApi;

... in your class definition use script getCDIBean to get instance of services

	@Inject
	private CrossStorageApi crossStorageApi;
	
        @Inject
	private RepositoryService repositoryService;
  
...

MyCet cei = new MyCet();
cei.setUuid("2434e3a3-3c32-4b18-869d-ea5ff1aeafbb") // Optionally set UUID
// Set cei properties ...

Repository defaultRepo = repositoryService.findDefaultRepository();
try{
   String uuid = crossStorageApi.createOrUpdate(defaultRepo, cei);
   System.out.println("MyCet instance " + uuid + " created / updated");
} catch(Exception ex){
   throw new BusinessException(ex);
}
```

## II.2. Find an entity

### II.2.1. By UUID

To retrieve an entity by its uuid, use `CrossStorageApi#find` with these parameters :

1. *repository* : `Repository` - repository where to retrieve the entity
2. *uuid* : `String` - UUID of the entity
3. *cetCode* or *cetClass* : `CustomEntityTemplate` information
   1. *cetCode* : `String` - the code of the template (retrieves a `CustomEntityInstance`)
   2. *cetClass* : `Class<?>` - the generated java class of the template (returns an instance of the class)

**Example** :

```java
String uuid = "2434e3a3-3c32-4b18-869d-ea5ff1aeafbb";
Repository defaultRepo = repositoryService.findDefaultRepository();

MyCet cei = crossStorageApi.find(defaultRepo, uuid, MyCet.class);
// or CustomEntityInstance cei = crossStorageApi.find(defaultRepo, uuid, "MyCet");

System.out.println("Found MyCet instance: " + cei);
```

### II.2.2. By values

To retrieve an entity by its uuid, use `CrossStorageApi#find` with these parameters :

- *repository* : `Repository` - repository where to retrieve the entity
- *cetClass* : `Class<?>` - the generated java class of the template

The method will return a [CrossStorageRequest](./CrossStorageRequest.java) initialized for the given entity and repository.

This object has two methods `by` and `fetch`:

- `by(String field, Object value) : CrossStorageRequest` - adds a filter to the request
  - *field*: name of the field to filter on
  - *value*: value of the filter
- `fetch(String field)  : CrossStorageRequest` - fetch a relation of the entity
  - *field*: relation to fetch (corresponding `CustomFieldTemplate` should be an entity reference)

To retrieve the entity, call `CrossStorageRequest#getResult` to retrieve a single result from query. If there is no result, an `EntityDoesNotExistsException` is raised.

**Example** :

```java
Repository defaultRepo = repositoryService.findDefaultRepository();
MyCet cei = crossStorageApi.find(defaultRepo, MyCet.class)
    .by("valueOne", "test")
    .fetch("relationshipOne") // Optional
    .getResult();

System.out.println("Found MyCet instance: " + cei);
```

## II.3. List entities

To retrieve a list of entities, follow the steps above but at the end, call `CrossStorageRequest#getResults` instead.

**Example** :

```java
Repository defaultRepo = repositoryService.findDefaultRepository();
List<MyCet> ceis = crossStorageApi.find(defaultRepo, MyCet.class)
    .by("valueOne", "test") // Optional
    .fetch("relationshipOne") // Optional
    .getResults();

System.out.println("Found MyCet instances: " + ceis);
```

## II.4. Remove an entity

To remove an entity, either call  `CrossStorageApi#remove(Repository repository, String uuid, String cetCode)` or `CrossStorageApi#remove(Repository repository, String uuid, Class<?> cetClass)`

**Example** :

```java
String uuid = "2434e3a3-3c32-4b18-869d-ea5ff1aeafbb";
Repository defaultRepo = repositoryService.findDefaultRepository();

crossStorageApi.remove(defaultRepo, uuid, MyCet.class);
// or crossStorageApi.remove(defaultRepo, uuid, "MyCet");

System.out.println("Removed MyCet instance: " + uuid);
```

## II.5. CREATE, UPDATE, DELETE events

Despite it is possible to observe CREATE, UPDATE, DELETE events on custom entities instances using the meveo notifications, it is also possible to link a script to the custom entity template. This script will contain several methods (desribed below) called at the right moment during the persistence process.

The attribute of `CustomEntityTemplate` that represents this link is `crudEventListenerScript`. The listener script must implement the `CrudEventListenerScript` interface. It can be instantiated by calling  `CustomEntityTemplateService#loadCrudEventListener` which sets the `CustomEntityTemplate#crudEventListener` transient property.

The `CrudEventListenerScript` interface contains the following method defintions:

- `getEntityClass() : Class<T>` - should return the class of the custom entity being listened
- `prePersist(T entity)` - called just before entity persistence
- `preUpdate(T entity)` - called just before entity update
- `preRemove(T entity)` - called just before entity removal
- `postUpdate(T entity)` - called just after entity persistence but before transaction commit
- `prePersist(T entity)` - called just after entity update but before transaction commit
- `postRemove(T entity)` - called just after entity removal but before transaction commit

*Note: In any of these methods, if an unhandled exception is raised, the transaction will be rollbacked.*

**Example:**

```java
package org.meveo.script;

import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import java.util.List;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.CrudCetTest;
import org.meveo.model.customEntities.CrudEventListenerScript;

public class ListenerScript extends Script implements CrudEventListenerScript<CrudCetTest> {

	private CrossStorageApi crossStorageApi;
	private RepositoryService rService;

	public ListenerScript() {
		crossStorageApi = getCDIBean(CrossStorageApi.class);
		rService = getCDIBean(RepositoryService.class);
	}

	public Class<CrudCetTest> getEntityClass() {
		return CrudCetTest.class;
	}

	/**
	 * Called just before entity persistence
	 * 
	 * @param entity entity being persisted
	 */
	public void prePersist(CrudCetTest entity) {
		entity.setComputedValue("computed" + entity.getValue());
	}

	/**
	 * Called just before entity update
	 * 
	 * @param entity entity being updated
	 */
	public void preUpdate(CrudCetTest entity) {
		entity.setComputedValue("computedUpdated" + entity.getValue());
	}

	/**
	 * Called just before entity removal
	 * 
	 * @param entity entity being removed
	 */
	public void preRemove(CrudCetTest entity) {
		if (entity.getValue().contains("generated")) {
			CrudCetTest mainEntity = crossStorageApi.find(rService.findDefaultRepository(), CrudCetTest.class)
					.by("value", "main")
					.getResult();

			if (mainEntity != null) {
				throw new IllegalArgumentException("Can't remove the generated data if main data exists !");
			}
		}
	}

	/**
	 * Called just after entity persistence
	 * 
	 * @param entity persisted entity
	 */
	public void postPersist(CrudCetTest entity) {
		if(!entity.getValue().contains("generated")) {
			CrudCetTest generatedEntity = new CrudCetTest();
			generatedEntity.setValue("generated");

			try {
				crossStorageApi.createOrUpdate(rService.findDefaultRepository(), generatedEntity);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Called just after entity update
	 * 
	 * @param entity updated entity
	 */
	public void postUpdate(CrudCetTest entity) {
		if(!entity.getValue().contains("generated")) {
			CrudCetTest generatedEntity = crossStorageApi.find(rService.findDefaultRepository(), CrudCetTest.class)
				.by("value", "generated")
				.getResult();

			generatedEntity.setValue("updatedgenerated");

			try {
				crossStorageApi.createOrUpdate(rService.findDefaultRepository(), generatedEntity);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Called just after entity removal
	 * 
	 * @param entity removed entity
	 */
	public void postRemove(CrudCetTest entity) {
		if(!entity.getValue().contains("generated")) {
			List<CrudCetTest> generatedEntities = crossStorageApi.find(rService.findDefaultRepository(), CrudCetTest.class)
				.getResults();

			try {
				
				for(var generatedEntity : generatedEntities) {
					crossStorageApi.remove(rService.findDefaultRepository(), generatedEntity.getUuid(), CrudCetTest.class);
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
```
