# Workflow for entity lifecycle

A typical case of workflow is when the state of a workflow is the status of an entity and when action must be taken when the status is changing.

For instance when an order changed from the status IN_PREPARATION to SHIPPED, we would like to send an email to the client.

In meveo this kind of workflow is created (using menu Services/workflow in the admin console) by choosing an
custom entity on which it applies then the custom field that hold the status.
This custom field must be of type `Selection from a list` since it is this list of state that will be used to create the transitions in the workflow.

![image](https://user-images.githubusercontent.com/16659140/132503125-6282afe2-51df-4c98-8a5d-21473cc3475a.png)

you can then define a set of transition from an initial to a target state as being a list of action

![image](https://user-images.githubusercontent.com/16659140/132503372-8a3559be-9366-4196-85cb-8caa6d491558.png)

In a transition an action is the invocation of a script under some condition.
This mean that when undergoing a transition, an action is executed only if its condition expression evaluates to true.

the action execute a script whose input parameters can be a fixed value or an expression.

## Expressions

The EL that can be used in actions condition or input parameters can use the variable "entity" to access the 
custom entity on which the transition is applying.

for instance on an `Order` that have a `totalAmount` field of type `Double` if you want to execute the action
only if the amount of the order is greater than `100` then you can use the expression

```
#{entity.get('totalAmount')>100}
```


