[![Gitpod - Code Now](https://img.shields.io/badge/Gitpod-code%20now-blue.svg?longCache=true)](https://gitpod.io#https://github.com/meveo-org/meveo)
[![Build Status](https://travis-ci.org/meveo-org/meveo.svg?branch=master)](https://travis-ci.org/meveo-org/meveo)

# Meveo

Meveo is a platform that allow to develop and execute enterprise back and front applications
It is intended to be run by Wildfly 15 with full Jakarta EE stack under licence : AGPLv3.0

Frontend framework  https://github.com/meveo-org/meveo-fronted.github.io

Fontend kitchensink demo https://frontend.meveo.org for a demo of all the web components.

## Installation

[Docker Installation](./docker/README.md)

[Installation on a wildfly instance](./WILDFLY_INSTALL.md)

[Debugging](https://github.com/meveo-org/meveo/blob/master/documentation/DEBUGGING.md)

## Technical documentation

### System

- [Security](./meveo-model/src/main/java/org/meveo/security/README.md)

### Configuration

- [System settings](./meveo-admin/ejbs/src/main/java/org/meveo/admin/configuration)
- [Module](./meveo-api/src/main/java/org/meveo/api/module)
- [User Hierarchy](./meveo-admin/ejbs/src/main/java/org/meveo/service/hierarchy)

### Execution

### Ontology

- [Base API](./meveo-admin/ejbs/src/main/java/org/meveo/api/base/sql)
- [Persistence](./meveo-api/src/main/java/org/meveo/api/persistence)
- [Custom Entity](./meveo-admin/ejbs/src/main/java/org/meveo/service/custom)

### Services

- [Function](./meveo-admin/ejbs/src/main/java/org/meveo/service/script)
- [Endpoint](./meveo-admin/ejbs/src/main/java/org/meveo/service/technicalservice/endpoint)
- [Notification](./meveo-admin/ejbs/src/main/java/org/meveo/service/notification)

### Reporting


## User Guide

- [Documentation](https://github.com/meveo-org/meveo/tree/master/documentation/userguide)
