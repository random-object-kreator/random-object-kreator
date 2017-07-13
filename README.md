# random-object-kreator
Creates any arbitrary random object for your test data

###Usage

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
