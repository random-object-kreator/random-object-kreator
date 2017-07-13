# Random Object Kreator
Creates any arbitrary random object for your test data. The name is annoying as it uses a k instead of a c because it'a Kotlin library.

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
