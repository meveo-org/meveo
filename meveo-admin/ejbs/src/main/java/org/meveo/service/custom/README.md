# Custom Entities

## Custom fields (CFT)
A Custom Field Template defines the fields of a Custom Entity.  It can be modified through the API or programmatically in a script.

### Searching CFTs
The CustomFieldTemplate entity has 2 natural ids: code and appliesTo.  When searching for a CFT, make sure to use use both.  This means, when searching programmatically, use [CustomFieldTemplateService](/meveo-admin/ejbs/src/main/java/org/meveo/service/crm/impl/CustomFieldTemplateService.java)#findByCodeAndAppliesTo and not findByCode

## Custom action
A custom action is script that can be called on an entity ether by API of from meveo admin.

you can use expression language (EL) to set the input parameters of the script call from the entity,
for instance this EL will retrieve the value of the field "name"
```
#{entity.cfValues.getCfValue("name")}
```

You can display the custom action conditionally by setting an EL in the "Show on condition" field.
For instance this EL allow to display the custom action button only if the field "name " of the entity is not null
```
#{entity.cfValues.getCfValue("name") ne null}
```


The entity instance on which the action is called is set in the context variable `CONTEXT_ENTITY`

The code of the custom action executed is in the variable `CONTEXT_ACTION`

A message can be displayed in the GUI by setting the variable `RESULT_GUI_MESSAGE` or `RESULT_GUI_MESSAGE_KEY` to use a message stored in the [message files](/meveo-admin/web/src/main/resources/messages_en.properties).

In order to redirect to another page once the action is executed the page name can be set in the variable `GUI_OUTCOME`

## CEI Auditing

A boolean option to make CET auditable is available in both GUI and API. Is this field is true, a new table must be created with the code of the cet prefix with audit_. For example, CET=computer, then our audit table must be audit_computer. This table is updated when an operation is executed on a particular CEI.

An icon field must be added in the CET computer for display.

The created table would have 5 columns : "id", "CEI_uuid", "user", "date", "action", "field", "oldValue", "newValue".

We will be auditing the changes in the CEI and we will have one audit record per CFT updates. So if two CFTs are changed, then we will see 2 audit records in the audit table.

Example :

A CET person P is auditable, with CFT field "name".
- User U create an instance of P : a row is inserted in the table "audit_person" with user = u, date = now(), CEI_uuid = uuid of the created CEI, action = "CREATED".
- User U update the CEI with name = newName : a row is inserted in the table "audit_person" with same info as previous, but action = "UPDATED", oldValue = "" and newValue = "newValue"
- User U remove the CEI with name = newName : a row is inserted in the table "audit_person" with same info as create, but action = "DELETE"
- User U updated both the name and age field, 2 new entries will be created similar with the create info. But with different oldValue and newValue respectively.

*The audit table should be kept even when the CET table is deleted.

A GUI and API is available to view the audit trail for a particular CEI sorted by new to old.

## CEI Workflow

Meveo workflow is used by applying it to the CET by choosing the CFT that will be the status.

A workflow is a set of transitions that allow to change the state of an entity in a controlled way.
The state is a CFT of type list of value of the CET. The workflow is made of a list of transitions.

A transition is a CFT that has:
- an origin state.
- a target state
- a evaluationEL
- a list of action

### How to apply a Workflow

A workflow is applied during CEI update.

If we update the value of a CFT of a CEI and the corresponding CET has one workflow:
- If there is no workflow the update is persisted.
- If the workflow exists, then we check if there are transition from the previous state to the new one (before persisting the update), otherwise the update is refused.
- If transition exists, then we check each of the CFT's applicationEL (order by priority) and if none match we refuse the update.
- If one match then we persist the update and perform the actions.

Currently, the way workflow are applied is different: transitions have names and we execute a service that will change the state by applying a named transition. But for this implementation the user do not choose the transition, rather he chose the target state.

### Additional Available Service
- A service that returns a list of states for a given CET.
- A service that returns the target states from a origin state of a given CEI where applicationEL evaluates to true.

## List of Services

### Get the Available States of a given CEI

API: /customEntityInstance/states/{customEntityTemplateCode}/{customFieldTemplateCode}/{uuid}

This API returns the list of states available for a given CEI. To build this list EL must be evaluated for all transitions with current status as origin.

It has 2 parameters : CEI's uuid and CFT code.
It loads the CEI, then evaluate all the transition rules whose origin is the state (the value of the CFT in the CEI) and return the list of target states for all those transition whose rule evaluated to true.


We need to evaluate every transition of the workflow base on the CEI.

For example, you have:
A CEI with code='xxx',

2 transitions:
NEW -> ACTIVE, with EL event.code='xxx',
NEW -> REJECTED, with EL event.code='yyy'

Then only the ACTIVE state should be returned.

The association should be inverted : it is not the CET that own a list of WF, it is a WF that is associated to a couple (CET,CFT) where the CFT is a list of String (that will play the roles of state).

Example :
A CET "project" is created with the following CFT

name, String
createDate, Date
description, String
status, List of String (NEW,ONGOING,CLOSED) with default value NEW

The a workflow is created with code "Projet_cycle", we need to choose a CET in a drop list (not a script). For example, we select "Person", then it list me in another dropdown all the CFT of Person that have type List of String and a default value, we choose "status".
Then we can create the transitions and the actions.

When we update a CEI of type "Person" then in the beforeUpdate method meveo will check if the "status" value has changed, if yes then it will check if a WF exist for this CET and CFT then apply it like written in the description of the ticket.
