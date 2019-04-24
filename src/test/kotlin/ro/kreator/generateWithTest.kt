package ro.kreator
import org.junit.Test

class generateWithTest {
    val customizeListString by customize { -> listOf("hello")}
    val customizePairStringInt by customize { -> Pair("hello", 33)}
//    val customizePairStringListInt by customize { a: List<Map<String, SimpleClass>>, _: Any -> Pair("string", a)}

    val aList by aRandom<List<String>>()
    val pairStringInt by aRandom<Pair<String, Int>>()
    val pairStringListInt by aRandom<Pair<String, List<Map<String, SimpleClass>>>>()

    init {
        registerCustomizations(customizeListString, customizePairStringInt)
    }

    @Test
    fun `works with generics`() {
//        expect that aList isEqualTo listOf("hello")
//        expect that pairStringInt isEqualTo Pair("hello", 33)
//        expect that pairStringListInt.first isEqualTo  "string"
    }
}
