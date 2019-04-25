package ro.kreator

import org.junit.Test
import kotlin.system.measureTimeMillis

class PerformanceTest {
    val a0 by aRandomListOf<ManyParams>(100_000)

//    val t = this::class.createType()
    val t = this.javaClass
    val n = this.javaClass.name

    @Test
    fun isFast(){
//        val token = Random().nextInt().hashCode().absoluteValue.print()
//
//        val size = (token % 10) + 1
//        val charArray = CharArray(size)
//
//        for (i in 0 until size) {
//            charArray[i] = ((token shl i).absoluteValue % 0x00ff).toChar()
//        }
//
//        charArray.joinToString("")
//                .print()
//        val a = setOf<Int>(*(1..100).map { it }.toTypedArray())
        measureTimeMillis {
            a0
        }.print()

//        measureNanoTime {
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//            t.hashCode()
//        }.print()
    }

    val i by aRandom<I>()

    @Test
    fun `caches value`() {
        i.b
    }


//    private fun pseudoRandom(token: Long): Random = Random(Seed.seed with token)
//    internal infix fun Long.with(other: Long): Long = this * 31 + other
//    private fun Random.int(bound: Int) = if (bound == 0) 0 else nextInt(bound)
}


data class Yo(val a: String, val b: String, val c: String)
data class I(val x: Yo, val b: Yo, val d: Yo)
data class ManyParams(val a: I, val b: I, val c: I, val d: I, val e: I, val f: I, val g: I, val h: I)

//fun getString() = Random(0).let { RandomStringUtils.random(Math.max(1, it.nextInt(9)), 0, 6000, true, true, null, it) }
//fun getInt() = Random(0).nextInt()
//fun getYo()= Yo::class.constructors.first().call(getString(), getInt(), getInt())
//fun getI()= I::class.constructors.first().call(getYo(), getYo(), getYo())
//fun getMP()= ManyParams::class.constructors.first().call(getI(), getI(), getI(), getI(), getI(), getI(), getI(), getI())
