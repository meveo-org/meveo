# SQL Persistence Service

## Custom table creation

When creating a [CustomEntityTemplate](../../../../../../../../../../meveo-model/src/main/java/org/meveo/model/customEntities/CustomEntityTemplate.java), if `availableStorage` contains the DBStorage `SQL` and that `sqlConfiguration#storeAsTable` is true, then a custom table will be created using the [CustomTableCreatorService](../../custom/CustomTableCreatorService.java).

The name of the table will be the code of the template in lower case.

The columns are created one by one after the creation of the table, they are described by the [custom fields](../../../../../../../../../../meveo-model/src/main/java/org/meveo/model/crm/CustomFieldTemplate.java) of the template.

## Custom table querying

For CRUD operations, see the class [CustomTableService](../../custom/CustomTableService.java)

## Inheritance

When a template has a super-template, we create an inheritance relationship between the parent table and the child table : the uuid of the child has a foreign key constraint to the uuid of the parent table.

### Create

When creating data in a child table, we separate the data stored in the parent table and the data stored in the child table. Then, we insert the data in the parent table first and finally in the child table, re-using the uuid of the parent row.

### Update

When updating data, we first update parent row, then we update the child row.

### Delete

When deleting data from a template:

- if the template has children then the corresponding row from each child is first removed
- row is removed from template's table
- if the template has a super-template, row is removed from parent table

### Retrieve

When we query a template that has a super-template, we complete the data retrieved from the template's table with data retrieved from parent table.

### *Notes*

*Instead of using multiple queries to synchronize operations between tables, maybe a better way would have been to modify the query themselves (ie: query both child and parent table at the same time using a joined query)*
