# Users

Meveo users can be persisted in DB as MeveoUser and stored in keycloak.
User are created in keycloak when they need their own credentials to use meveo services.

Using the [API](https://www.postman.com/interstellar-satellite-318365/workspace/meveo/collection/3389070-bca6f086-0937-4b14-b281-c23c8798d222?ctx=documentation) 
you can create a user in meveo only by using the `/user/createOrUpdate` with body
```
{
            "username": "testUser1",
            "email": "meveo@meveo.org",
            "roles":[],
            "externalRole": []
}
```

Note that the username is first converted to uppercase before being persisted in database.

You create a user in both meveo and keycloak by using the `/user/external` but to be allowed to do so the user 
whose credential are used to call the endpoint must have the `manage-users` role.

You add this role using the keycloak admin console by displaying the user detail, select tab `Role Mappings`  
select `realm-management' in `client-role` dropdown and add the `manage-users` role

![image](https://user-images.githubusercontent.com/16659140/230764412-44d528a0-6a08-44b0-a3c7-54b200d1e538.png)

## Roles

When a user logs in  meveo, its roles are populates by the roles stored in meveo DB and complemented by the role of the user in keycloak (in the case the user also exist in keycloak)
