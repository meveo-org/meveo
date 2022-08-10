# User Hierarchies or User Groups

User hierarchy is used to restrict the access of a user to a repository. As the name implies it is a hierarchy and if a user is a member of a parent hierarchy that means that account has access to all the repositories associated with the children of that hierarchy.

For example, we have the following User hierarchy:

```
- Marketing
-- France
-- Philippines
```

Let's assume that we have a userA who is associated with the group Marketing and a repository associated with France. This means that our userA should be able to access that repository as well. Or in this case, any repositories associated with either Marketing, France, and Philipines user hierarchy.

The user hierarchy security is implemented in the CEI creation page and when using the CrossStorageService to create CEI as well.

The API for user hierarchy is available at:

> /hierarchy/userGroupLevel

