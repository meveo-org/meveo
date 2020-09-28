# Cross storage API

- [1. Persisting an entity](#1-persisting-an-entity)
- [2. Find an entity](#2-find-an-entity)
  - [2.1. By UUID](#21-by-uuid)
  - [2.2. By values](#22-by-values)
- [3. List entities](#3-list-entities)
- [4. Remove an entity](#4-remove-an-entity)

## 1. Persisting an entity

To persist an entity, use `CrossStorageApi#createOrUpdate` with these parameters :

- *repository* : `Repository` - repository where to save the entity
- *value* : `Object` - the object to persist. The simple class name of the object should correspond to an existing `CustomEntityTemplate`.

*Note: you can set the UUID of the entity instance, this can have two effects*

- *force the entity to have a certain UUID if the entity does not exists*
- *speed-up the update process by enabling the persistence engine to retrieve the entity to update by UUID if the entity already exists*

**Example**:

```java
MyCet cei = new MyCet();
cei.setUuid("2434e3a3-3c32-4b18-869d-ea5ff1aeafbb") // Optionally set UUID
// Set cei properties ...

Repository defaultRepo = repositoryService.findDefaultRepository();
String uuid = crossStorageApi.createOrUpdate(defaultRepo, cei);

System.out.println("MyCet instance " + uuid + " created / updated");
```

## 2. Find an entity

### 2.1. By UUID

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

### 2.2. By values

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

## 3. List entities

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

## 4. Remove an entity

To remove an entity, either call  `CrossStorageApi#remove(Repository repository, String uuid, String cetCode)` or `CrossStorageApi#remove(Repository repository, String uuid, Class<?> cetClass)`

**Example** :

```java
String uuid = "2434e3a3-3c32-4b18-869d-ea5ff1aeafbb";
Repository defaultRepo = repositoryService.findDefaultRepository();

crossStorageApi.remove(defaultRepo, uuid, MyCet.class);
// or crossStorageApi.remove(defaultRepo, uuid, "MyCet");

System.out.println("Removed MyCet instance: " + uuid);
```
