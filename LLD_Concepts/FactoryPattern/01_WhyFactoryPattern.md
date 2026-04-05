# 01 - Why Factory Pattern?

## Problem Statement

Suppose `PizzaStore` has the responsibility to prepare different types of pizzas like:

- New York Pizza
- Italian Pizza
- Indian Pizza

A common beginner approach is to create objects directly inside `PizzaStore`.

```java
public class PizzaStore {

    public Pizza orderPizza(String type) {
        Pizza pizza = null;

        if (type.equalsIgnoreCase("ny")) {
            pizza = new NYPizza();
        } else if (type.equalsIgnoreCase("italian")) {
            pizza = new ItalianPizza();
        } else if (type.equalsIgnoreCase("indian")) {
            pizza = new IndianPizza();
        }

        return pizza;
    }
}
```

At first this looks simple, but the design starts getting worse as new pizza types are added.

---

## What is the actual problem?

`PizzaStore` is doing **two jobs**:

1. **Business workflow**
   - taking the order
   - preparing pizza
   - baking
   - packing
   - delivering

2. **Object creation**
   - deciding whether to create `NYPizza`, `ItalianPizza`, or `IndianPizza`

This mixes **usage logic** with **creation logic**.

---

## Why is this bad?

### 1. Tight coupling

`PizzaStore` directly depends on concrete classes:

- `NYPizza`
- `ItalianPizza`
- `IndianPizza`

If a new class is added, `PizzaStore` must change.

---

### 2. Too many `if-else` or `switch` blocks

As the number of pizza types grows, creation logic becomes messy.

```java
if (type.equalsIgnoreCase("ny")) {
    pizza = new NYPizza();
} else if (type.equalsIgnoreCase("italian")) {
    pizza = new ItalianPizza();
} else if (type.equalsIgnoreCase("indian")) {
    pizza = new IndianPizza();
} else if (type.equalsIgnoreCase("farmhouse")) {
    pizza = new FarmHousePizza();
}
```

This becomes harder to read and maintain.

---

### 3. Violation of Open/Closed Principle

Whenever a new pizza type is introduced, we modify existing code in `PizzaStore`.

That means the class is **not closed for modification**.

---

### 4. Harder to test and extend

If creation logic is scattered inside store classes, reusing or testing creation becomes difficult.

---

## What does Factory Pattern solve?

Factory Pattern says:

> Move the object creation responsibility into a separate factory.

So instead of `PizzaStore` deciding which pizza to instantiate, it delegates that responsibility to a factory.

---

## New Responsibility Split

### PizzaStore
Responsible for:
- receiving the order
- calling the factory
- preparing / baking / packing pizza

### PizzaFactory
Responsible for:
- creating the correct pizza object

---

## Small Diagram

```text
Customer
   |
   v
+------------+
| PizzaStore |
+------------+
      |
      | asks for pizza
      v
+--------------+
| PizzaFactory |
+--------------+
      |
      | creates
      v
+-------------------+
| NY / Italian /    |
| Indian Pizza      |
+-------------------+
```

---

## Flow with Factory Pattern

```java
public class PizzaStore {

    private PizzaFactory pizzaFactory;

    public PizzaStore(PizzaFactory pizzaFactory) {
        this.pizzaFactory = pizzaFactory;
    }

    public Pizza orderPizza(String type) {
        Pizza pizza = pizzaFactory.createPizza(type);

        // common workflow
        // pizza.prepare();
        // pizza.bake();
        // pizza.pack();

        return pizza;
    }
}
```

Now `PizzaStore` no longer does direct object creation.

---

## Why this is better

### Separation of concerns
- `PizzaStore` handles order flow
- `PizzaFactory` handles creation

### Reduced coupling
`PizzaStore` depends on `Pizza` / `PizzaFactory`, not on concrete pizza classes.

### Easier to extend
We can add new pizza types with minimal impact.

### Cleaner code
Creation logic is centralized in one place.

---

## In simple words

Earlier:

> PizzaStore both **used** pizza objects and **created** pizza objects.

With factory pattern:

> PizzaStore **uses** pizza objects, factory **creates** pizza objects.

This is the main reason factory pattern exists.

---

## Does this match our pizza discussion?

Yes.

When we say:

> "Currently PizzaStore has the task to create different types of pizza"

that is exactly the smell factory pattern tries to fix.

Factory Pattern addresses the question:

> "Who should be responsible for creating the correct object?"

Answer:

> A dedicated factory, not the store/client itself.

---

## Important Note

There are 3 common ways people talk about this:

### 1. Simple Factory
One class handles all creation.

```java
Pizza createPizza(String type)
```

### 2. Factory Method
A parent factory defines a creation method, and subclasses decide the concrete object.

Example:
- `PizzaFactory`
- `NYPizzaFactory`
- `ItalianPizzaFactory`

### 3. Abstract Factory
Creates families of related objects.

For your current discussion with:
- `PizzaFactory`
- `NYPizzaFactory`
- `ItalianPizzaFactory`

this is closer to **Factory Method Pattern**.

---

## Quick Summary

Factory Pattern is useful because:

- `PizzaStore` should not know every concrete pizza class
- object creation should be separated from business flow
- factories reduce tight coupling
- the design becomes easier to extend and maintain

---

## One-Line Takeaway

> Factory Pattern exists to remove object creation logic from client classes like `PizzaStore` and delegate it to a dedicated factory.
