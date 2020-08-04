# Custom Entities

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