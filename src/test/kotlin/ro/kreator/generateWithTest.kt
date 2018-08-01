package ro.kreator
import com.memoizr.assertk.expect
import org.junit.Test

class generateWithTest {
    val customizeListString by custom { -> listOf("hello")}
    val customizePairStringInt by custom { -> Pair("hello", 33)}
    val customizePairStringListInt by custom { a: List<Map<String, SimpleClass>>, _: Any -> Pair("string", a)}

    val aList by aRandom<List<String>>()
    val pairStringInt by aRandom<Pair<String, Int>>()
    val pairStringListInt by aRandom<Pair<String, List<Map<String, SimpleClass>>>>()

    @Test
    fun `works with generics`() {
        expect that aList isEqualTo listOf("hello")
        expect that pairStringInt isEqualTo Pair("hello", 33)
        expect that pairStringListInt.first isEqualTo  "string"
    }
}