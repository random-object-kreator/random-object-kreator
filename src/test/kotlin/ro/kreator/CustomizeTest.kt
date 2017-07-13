package ro.kreator


import com.memoizr.assertk.expect
import com.memoizr.assertk.notNull
import com.memoizr.assertk.of
import org.junit.Test
import java.security.MessageDigest

object CustomizationForTest : Customizer {
    val a by customize<Pair<Int, Int>>().using<Int, Int>(::Pair) { it[3, 4] }
    val b by customize<Pair<String, List<Int>>>().using<String, List<Int>>(::Pair) { it["hey", listOf(5)] }
    val p0 by customize<Param0>().using({ Param0 }) { Param0 }
    val p1 by customize<Param1<Int>>().using<Int>(::Param1) { it[any()] }
    val p2 by customize<Param2<Int, Int>>().using<Int, Int>(::Param2) { it[any(), 2] }
    val p3 by customize<Param3<Int, Int, Int>>().using<Int, Int, Int>(::Param3) { it[any(), 2, 3] }
    val p4 by customize<Param4<Int, Int, Int, Int>>().using<Int, Int, Int, Int>(::Param4) { it[any(), 2, 3, 4] }
    val p5 by customize<Param5<Int, Int, Int, Int, Int>>().using<Int, Int, Int, Int, Int>(::Param5) { it[any(), 2, 3, 4, 5] }
    val p6 by customize<Param6<Int, Int, Int, Int, Int, Int>>().using<Int, Int, Int, Int, Int, Int>(::Param6) { it[any(), 2, 3, 4, 5, 6] }

    val inverted by customize<InvertedGenerics<String, Int, Long>>().using<Long, Int, String>(::InvertedGenerics) {
        val aLong = a<List<String>>(String::class())
        val aLong2 = a<List<String>>(String::class())
        it[any(), 0, any()]
    }
}

data class InvertedGenerics<out A, out B, in C>(private val x: C, val b: B, val a: A)

class CustomizeTest {

    val pairIntInt by aRandom<Pair<Int, Int>>()
    val pairStringListInt by aRandom<Pair<String, List<Int>>>()
    val inverted by aRandom<InvertedGenerics<String, Int, Long>>()

    init {
        CustomizationForTest.register()
    }

    @Test
    fun `works with generics`() {
        expect that pairIntInt isEqualTo Pair(3, 4)
        expect that pairStringListInt isEqualTo Pair("hey", listOf(5))
        expect that inverted _is notNull
    }

    val p0 by aRandom<Param0>()
    val p1 by aRandom<Param1<Int>>()
    val p2 by aRandom<Param2<Int, Int>>()
    val p3 by aRandom<Param3<Int, Int, Int>>()
    val p4 by aRandom<Param4<Int, Int, Int, Int>>()
    val p5 by aRandom<Param5<Int, Int, Int, Int, Int>>()

    @Test
    fun `works with different arities`() {
        expect that p0 _is notNull
        expect that p1.t1 isInstance of<Int>()
        expect that p2.t2 isEqualTo 2
        expect that p3.t3 isEqualTo 3
        expect that p4.t4 isEqualTo 4
        expect that p5.t5 isEqualTo 5
    }
}
