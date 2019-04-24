package ro.kreator

import org.junit.Test
import kotlin.system.measureTimeMillis

class PerformanceTest {
    val a0 by aRandomListOf<ManyParams>(10)


    @Test
    fun isFast(){
        measureTimeMillis {
            println(a0)
        }.print()
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


data class Yo(val a: String, val b: Int, val c: Int)
data class I(val x: Yo, val b: Yo, val d: Yo)
data class ManyParams(val a: I, val b: I, val c: I, val d: I, val e: I, val f: I, val g: I, val h: I)

//fun getString() = Random(0).let { RandomStringUtils.random(Math.max(1, it.nextInt(9)), 0, 6000, true, true, null, it) }
//fun getInt() = Random(0).nextInt()
//fun getYo()= Yo::class.constructors.first().call(getString(), getInt(), getInt())
//fun getI()= I::class.constructors.first().call(getYo(), getYo(), getYo())
//fun getMP()= ManyParams::class.constructors.first().call(getI(), getI(), getI(), getI(), getI(), getI(), getI(), getI())
