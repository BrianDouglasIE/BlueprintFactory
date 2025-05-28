![Maven Central Version](https://img.shields.io/maven-central/v/ie.briandouglas/BlueprintFactory)
[![javadoc](https://javadoc.io/badge2/ie.briandouglas/BlueprintFactory/javadoc.svg)](https://javadoc.io/doc/ie.briandouglas/BlueprintFactory)

# Blueprint Factory

Create objects from reusable blueprints with flexible overrides.

## Overview

Blueprint Factory simplifies creating instances of your classes by providing a base template (the *blueprint*) and allowing you to customize each created object with variations or related objects. 
It's designed to be used along with [Faker](https://github.com/DiUS/java-faker) to easily generate seed data.

## Features

- Define a reusable blueprint for your object.
- Override properties via instances or key-value maps.
- Create single or multiple instances at once.
- Easily set related entities with custom setters.
- Clean, type-safe API with just two public methods: `with` and `create`.

## Installation

### Maven

```
<dependency>
    <groupId>ie.briandouglas</groupId>
    <artifactId>BlueprintFactory</artifactId>
    <version>1.0.2</version>
</dependency>
```

### Gradle

```
implementation group: 'ie.briandouglas', name: 'BlueprintFactory', version: '1.0.2'
```

## Usage

---

### Define your Factory

Extend the abstract `BlueprintFactory<T>` and implement the `blueprint()` method to provide a default instance:

```java
public class UserFactory extends BlueprintFactory<User> {
    private final Faker faker = new Faker();

    @Override
    protected User blueprint () {
        return new User(faker.name().firstName(), faker.internet().emailAddress());
    }
}
```

When an object is created by a `BlueprintFactory` the `blueprint` method will be called. Once created
the object returned from the `blueprint` method will have any overrides applied. Because `blueprint` is
called for each `create`, it is a good idea to use a library like `Faker` to generate your blueprint's
data.

---

### Creating Instances

**Basic instance:**

```java
User user = userFactory.create();
```

**Override fields with an instance:**

```java
User override = new User("alice", "alice@example.com");
User user = userFactory.create(override);
```

**Override fields with a map:**

```java
User user = userFactory.create(Map.of("name", "bob", "email", "bob@example.com"));
```

**Create multiple instances:**

```java
List<User> users = userFactory.create(5);
```

**Create from a list of variants:**

```java
List<User> variants = List.of(
    new User("charlie", "charlie@example.com"),
    new User("dave", "dave@example.com")
);
List<User> users = userFactory.create(new VariantList<>(variants));
```

**Create from a list of map variants:**

```java
List<VariantMap> variantMaps = List.of(
    new VariantMap(Map.of("name", "eve")),
    new VariantMap(Map.of("name", "frank"))
);
List<User> users = userFactory.create(new VariantMapList(variantMaps));
```

---

### Setting Related Entities

**With a single related object:**

```java
User user = userFactory
    .with(roleFactory::create, User::setRole)
    .create();
```

**With multiple related objects:**

```java
User user = userFactory
    .with(roleFactory::create, 3, User::setRoles)
    .create();
```

**With a variant passed to related factory:**

```java
Role adminRole = new Role("admin");
User user = userFactory
    .with(roleFactory::create, adminRole, User::setRole)
    .create();
```

**With a list of variants:**

```java
List<Role> roles = List.of(new Role("editor"), new Role("viewer"));
User user = userFactory
    .with(roleFactory::create, new VariantList<>(roles), User::setRoles)
    .create();
```

**With a list of map variants:**

```java
List<Map<String, ?>> maps = List.of(
        Map.of("name", "contributor"),
        Map.of("name", "maintainer")
);
User user = userFactory
        .with(roleFactory::create, new VariantMapList<>(maps), User::setRoles)
        .create();
```

---

## LICENSE

Copyright (c) 2025 Brian Douglas.  
See the LICENSE file for license rights and limitations.