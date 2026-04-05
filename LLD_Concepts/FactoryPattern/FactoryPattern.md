# Factory Pattern

## What is Factory Pattern?

Factory Pattern is a **creational design pattern** used to create objects **without exposing the object creation logic to the client**.

Instead of doing this directly in client code:

```java
Pizza pizza = new NYStyleCheesePizza();
```

we delegate the responsibility of object creation to a **factory**.

So the client says:

> "I want a pizza"

and the factory decides:

> "Which exact pizza object should be created?"

---

## Why do we need it?

When object creation becomes complex, or when the exact type of object depends on some condition, creating objects directly using `new` makes code tightly coupled.

### Problem without factory

```java
if (location.equals("NY")) {
    pizza = new NYCheesePizza();
} else if (location.equals("Italian")) {
    pizza = new ItalianCheesePizza();
}
```

Issues:
- client knows too much about concrete classes
- adding new pizza types requires changing client code
- violates maintainability
- harder to test and extend

Factory Pattern solves this by moving creation logic to a separate place.

---

## Pizza Factory Example

Suppose we have:
- `PizzaFactory` → parent/abstract factory
- `NYPizzaFactory` → creates New York style pizzas
- `ItalianPizzaFactory` → creates Italian style pizzas

The client works with the abstraction `PizzaFactory`, not with concrete factories directly.

---

## Small Diagram

```text
                   +----------------------+
                   |     PizzaFactory     |
                   |----------------------|
                   | + createPizza()      |
                   +----------+-----------+
                              |
              -----------------------------------------
              |                                       |
              |                                       |
  +--------------------------+          +-----------------------------+
  |     NYPizzaFactory       |          |    ItalianPizzaFactory      |
  |--------------------------|          |-----------------------------|
  | + createPizza()          |          | + createPizza()             |
  +------------+-------------+          +-------------+---------------+
               |                                          |
               | creates                                  | creates
               v                                          v
   +------------------------+                 +------------------------+
   |    NYStylePizza        |                 |   ItalianStylePizza    |
   +------------------------+                 +------------------------+
```

---

## How to think about it

- `PizzaFactory` defines a contract for creating pizzas.
- `NYPizzaFactory` knows how to create New York style pizzas.
- `ItalianPizzaFactory` knows how to create Italian style pizzas.
- client code only asks the factory for pizza.
- client does **not** need to know the exact implementation class.

---

## Example Structure in Java

### 1. Product

```java
interface Pizza {
    void prepare();
}
```

### 2. Concrete Products

```java
class NYStylePizza implements Pizza {
    public void prepare() {
        System.out.println("Preparing New York Style Pizza");
    }
}

class ItalianStylePizza implements Pizza {
    public void prepare() {
        System.out.println("Preparing Italian Style Pizza");
    }
}
```

### 3. Factory Parent

```java
abstract class PizzaFactory {
    abstract Pizza createPizza();
}
```

### 4. Concrete Factories

```java
class NYPizzaFactory extends PizzaFactory {
    Pizza createPizza() {
        return new NYStylePizza();
    }
}

class ItalianPizzaFactory extends PizzaFactory {
    Pizza createPizza() {
        return new ItalianStylePizza();
    }
}
```

### 5. Client Code

```java
public class Main {
    public static void main(String[] args) {
        PizzaFactory factory = new NYPizzaFactory();
        Pizza pizza = factory.createPizza();
        pizza.prepare();
    }
}
```

---

## Flow of execution

If client does:

```java
PizzaFactory factory = new NYPizzaFactory();
Pizza pizza = factory.createPizza();
```

Then:
1. client holds reference of type `PizzaFactory`
2. actual object is `NYPizzaFactory`
3. `createPizza()` of `NYPizzaFactory` is called
4. it returns `NYStylePizza`
5. client uses pizza through `Pizza` interface

This is useful because the client depends on **abstractions**, not concrete classes.

---

## Design Principles Behind Factory Pattern

Factory Pattern is closely related to important object-oriented design principles.

### 1. Encapsulation of Object Creation
Object creation logic is kept inside factory classes.

Instead of scattering `new NYStylePizza()` everywhere, we centralize creation logic.

**Benefit:**
- cleaner client code
- easier maintenance
- single place to update creation logic

---

### 2. Open/Closed Principle (OCP)

> Software entities should be open for extension but closed for modification.

If tomorrow you want to add:
- `ChicagoPizzaFactory`
- `ChicagoStylePizza`

you can extend the system by creating new classes, without changing much of the existing client code.

**Good design:**
- add new factory
- add new product
- reuse same abstraction

---

### 3. Dependency Inversion Principle (DIP)

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

Client depends on:
- `PizzaFactory`
- `Pizza`

Client does **not** depend directly on:
- `NYPizzaFactory`
- `ItalianStylePizza`

This reduces coupling.

---

### 4. Program to an Interface, not an Implementation

The client uses:

```java
PizzaFactory factory;
Pizza pizza;
```

instead of tightly coupling with concrete classes.

This gives flexibility to swap implementations easily.

---

## Real-world intuition

Think of a pizza ordering app.

User selects:
- New York style
- Italian style

The app should not manually create all pizza objects with lots of `if-else` everywhere.

Instead:
- choose correct factory
- factory creates the correct pizza
- rest of the system just works with `Pizza`

---

## Benefits of Factory Pattern

- hides object creation logic
- reduces tight coupling
- improves readability
- supports extension
- aligns with SOLID principles
- makes testing easier

---

## When to use it

Use Factory Pattern when:
- object creation logic is complex
- exact object type is decided at runtime
- you want to remove `new` from client code
- multiple related concrete classes exist
- you want cleaner and extensible design

---

## Important Clarification

If your design is:

- `PizzaFactory` is the base factory
- `NYPizzaFactory` and `ItalianPizzaFactory` are subclasses

then this is closer to the **Factory Method Pattern** style, where subclasses decide which product to create.

So in your pizza example:
- `PizzaFactory` = creator
- `NYPizzaFactory`, `ItalianPizzaFactory` = concrete creators
- `Pizza` = product
- `NYStylePizza`, `ItalianStylePizza` = concrete products

---

## Quick Summary

Factory Pattern helps you:
- move object creation out of client code
- depend on abstraction
- support extension easily

In your example:
- `PizzaFactory` provides the creation contract
- `NYPizzaFactory` creates New York pizza
- `ItalianPizzaFactory` creates Italian pizza
- client only interacts with factory abstraction and pizza abstraction

---

## One-line Interview Definition

> Factory Pattern is a creational design pattern that provides an interface for creating objects, while letting subclasses decide which concrete object to instantiate.
