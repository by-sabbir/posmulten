# Posmulten

[![Build Status](https://travis-ci.org/starnowski/posmulten.svg?branch=master)](https://travis-ci.org/starnowski/posmulten)
[![Download](https://api.bintray.com/packages/starnowski/posmulten/posmulten/images/download.svg) ](https://bintray.com/starnowski/posmulten/posmulten/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.starnowski.posmulten/postgresql-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.starnowski.posmulten%22%20AND%20a:%22postgresql-core%22)


* [Introduction](#introduction)
* [Multi-tenancy](#multi-tenancy)
    * [Separate database strategy](#separate-database)
    * [Separate schema strategy](#separate-schema)
    * [Shared schema strategy](#shared-schema)


# Introduction
Posmulten library is an open-source project for the generation of SQL DDL statements that make it easy for implementation of [Shared Schema Multi-tenancy strategy](#shared-schema) via the [Row Security Policies](https://www.postgresql.org/docs/9.6/ddl-rowsecurity.html) in the Postgres database.
Project is tested for compatibility with postgres database in versions 9.6, 10.14, 11.9, 12.4 and 13.0.

# Multi-tenancy

Based on [hibernate documentation](https://docs.jboss.org/hibernate/orm/4.3/devguide/en-US/html/ch16.html) 
_`the term multi-tenancy in general is applied to software development to indicate an architecture in which a single running instance of an application simultaneously serves multiple clients (tenants).`_
There are three main strategies [separate database](#separate-database), [separate schema](#separate-schema) and [shared schema](#shared-schema)
Below you can find short description of those strategies. Of course we will focus more on [shared schema](#shared-schema).
For more information about what pros and cons of each approach please check below links:

* https://docs.jboss.org/hibernate/orm/4.3/devguide/en-US/html/ch16.html
* https://medium.com/@MentorMate/increase-efficiency-with-multi-tenant-cloud-software-architecture-4261fca6025e

## Separate database
In this strategy each tenant's data is stored in separate database. 
For this obvious reason this approach gives the highest isolation level.

<p align="center">
  <img src="https://raw.githubusercontent.com/starnowski/posmulten/master/doc/Separate_database.png">
</p>


## Separate schema
Strategy assumes that each tenant's data is kept in his own schema but all those schemas exists in single database instance.
Obviously this approach offers lower isolation level than [separate database](#separate-database) but allows to potentially save costs for infrastructure by not having multiple database instances.

<p align="center">
  <img src="https://raw.githubusercontent.com/starnowski/posmulten/master/doc/Separate_schema.png">
</p>

## Shared schema
TODO
In this strategies data all tenants are kept in single database and same schema.
Although there is no limitation that there has to be only one schema in database but all tenants should have same access to them.
The strategy assumes that all tables in a database (with an exception for tables that stores vocabulary data or data available for all tenants) have a column that stores tenant identifier.
Of course, based on this value column, we know which tenant is the owner of the table raw.
Obviously, this approach offers a lower isolation level than both previous strategies but, just like [shared schema](#shared-schema) strategy, allows to save costs for infrastructure potentially


<p align="center">
  <img src="https://raw.githubusercontent.com/starnowski/posmulten/master/doc/Shared_schema.png">
</p>

# TODO Concept What is shared schema strategy
# TODO How sql query looks like for shared schema
# TODO How Posmulten is doing this?
# TODO  - RLS policy
# TODO  - Constraints
# TODO  - Connection settings 

# TODO How to start using posmulten


