[![Gitpod - Code Now](https://img.shields.io/badge/Gitpod-code%20now-blue.svg?longCache=true)](https://gitpod.io#https://github.com/meveo-org/meveo)

# Meveo

[Meveo](https://meveo.org) is a platform that allow to develop and execute enterprise back and front applications. [Watch an introduction video](https://vimeo.com/661171888)

It allow you to create and deploy at runtime with fine grained security :
- Data model, putting part of your entities in RDBMS, Neo4j, Infinispan, Elastic search
- Business logic (functions in java, js, python with management of their dependencies)
- API (REST, websocket)
- Notifications (triggering emails, webhooks, custom function ..)
- Workflows
- Tests

Speeding up the conception of apps. 


It is intended to be run by Wildfly 18 with full Jakarta EE stack under licence : AGPLv3.0

Frontend framework  https://github.com/meveo-org/meveo-fronted.github.io

Fontend kitchensink demo https://frontend.meveo.org for a demo of all the web components.

## Installation

[Docker Installation](./docker/README.md)

[Installation on a wildfly instance](./documentation/installation/WILDFLY_INSTALL.md)

[Debugging](./documentation/DEBUGGING.md)

[Development Installation](./.devcontainer/README.md)

[Module development Installation](./documentation/MODULE_DEVELOPMENT.md))

## Using Meveo

### Configuration

- [Git repositories](./meveo-admin-ejbs/src/main/java/org/meveo/service/git)
- [System settings](./meveo-admin-ejbs/src/main/java/org/meveo/admin/configuration)
- [Modules](./meveo-api/src/main/java/org/meveo/api/module)
- [Users](.meveo-admin-ejbs/src/main/java/org/meveo/service/admin/impl/README.md)
- [User Hierarchy](./meveo-admin-ejbs/src/main/java/org/meveo/service/hierarchy)
- [Credentials](./meveo-admin-ejbs/src/main/java/org/meveo/service/admin/impl/credentials)

### Ontology

- [Base API](./meveo-admin-ejbs/src/main/java/org/meveo/api/base/sql)
- [Persistence](./meveo-admin-ejbs/src/main/java/org/meveo/api/persistence)
- [Custom Entity](./meveo-admin-ejbs/src/main/java/org/meveo/service/custom)
- [File system storage](./meveo-admin-ejbs/src/main/java/org/meveo/service/storage)

### Services

- [Functions](./meveo-admin-ejbs/src/main/java/org/meveo/service/script)
- [Rest Endpoints](./meveo-admin-ejbs/src/main/java/org/meveo/service/technicalservice/endpoint)
- [Websocket Endpoints](./meveo-admin-ejbs/src/main/java/org/meveo/service/technicalservice/wsendpoint)

### Workflows & Notifications
- [Lifecycle Workflows](./meveo-admin-ejbs/src/main/java/org/meveo/service/wf)
- [Notification](./meveo-admin-ejbs/src/main/java/org/meveo/service/notification)

### Frontend

 - [Static pages](./meveo-admin-web/src/main/java/org/meveo/admin/action/frontend)
 - [Module Webapp](https://github.com/meveo-org/module-webapprouter)

## Operating Meveo

### Security & Monitoring

- [Security](./meveo-model/src/main/java/org/meveo/security/README.md)
  [Monitoring](./meveo-api/src/main/java/org/meveo/api/rest/monitoring/README.md

### Clustering
Meveo can be deployed in a wildfly cluster to scale its capabilities.
Infinispan cache can be replicated/distributed/scaterred among nodes.
A JMS topic is used to [publish events](./meveo-admin-ejbs/src/main/java/org/meveo/event/monitoring) to the cluster nodes.

## REST API Reference

On a started instance, go to /meveo/api/rest/swagger.json or /meveo/api/rest/swagger.yaml

## Troubleshooting

When encountering persistence errors you might have to connect to the DB container then to postgres:
```
docker exec -it postgres bash
psql meveo meveo
meveo=#\d  //to list the tables
meveo=#drop table myentity;
meveo=#drop sequence myentity_seq;
meveo=#^Z  //ctrl+Z
```

## Migration from versions previous to 7.0.0

- The simplest solution is to uninstall / re-install all the modules
- If the uninstall is not possible without data loss, move your git/Meveo/src/main/java folder to git/Meveo/facets/java
