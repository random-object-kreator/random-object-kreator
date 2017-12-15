//# Random Object Kreator

Creates any arbitrary random object for your test data. The name is annoying as it uses a k instead of a c because it'a Kotlin library.

In your tests you very often need to use your data objects as inputs to functions or as outputs of fake or mocked functions. Creating those objects by hand every time is a tedious task so often people resort to writing builders, this is still some boilerplate code which needs to be written and maintained. Some people even resort to mocking their data classes, which leads to a whole set to other problems.

This library allows to create instances of any arbitrary class or interface. It has full support for generics.

```kotlin
val aUser by aRandom<User>()
```

It works with any type that can be instantiated

### Usage

```kotlin
class MyTest {
  
  val aUser by aRandom<User>()
  val aUser1 by aRandom<User> { copy(name = "Bubba") }
  
  @Test
  fun `a simple test`() {
    // use it
    aUser
  }
}
```


### Features

#### Simple classes
```kotlin
data class Username(val name: String)

val aUser by aRandom<User>()
```

#### Generics support
```kotlin
val someUsers by aRandom<Set<User>>()
val aPairOfUserAndInt by aRandom<Pair<User, Int>>()

data class GenericClass<T>(val items: List<T>)
val aGenericClass by aRandom<GenericClass<String>>()
```

#### Customization of objects
```kotlin
val aUser by aRandom<User> { copy(name = "Forrest") }
```

#### Random selection of interface implementation
If you ask for an interface, and in your module you have classes that implement that interface, one of these implementations will be randomly picked and instantiated.

Example:

```kotlin
interface Foo
class Bar() : Foo
class Baz() : Foo

val aFoo1 by aRandom<Foo>()
val aFoo2 by aRandom<Foo>()
val aFoo3 by aRandom<Foo>()
val aFoo4 by aRandom<Foo>()

aFoo1::class.simpleName // Baz
aFoo2::class.simpleName // Bar
aFoo3::class.simpleName // Bar
aFoo4::class.simpleName // Baz
```


#### Random implementation of interfaces with no implementation in module
```kotlin
If you ask for an interface wich has no concrete implementation in the module, the library will create a random implementation.

interface Foo {
    fun doSomething:
}
```


#### Random implementation of interfaces with no implementation in module

#### Sealed class and enum support

#### Arrays support

#### Fast runtime

#### Randomized instances

#### Deterministic runs by setting the seed

#### Customization of creation logic, with full support for generics

#### Localized creation of objects, with generics support




