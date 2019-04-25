package ro.kreator

import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.apache.commons.lang3.RandomStringUtils
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.io.File
import java.lang.reflect.Array.*
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.set
import kotlin.math.absoluteValue
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.internal.ReflectProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName


typealias Token = Long

internal object CreationLogic : Reify() {
    @JvmStatic
    internal val ObjectFactory : LinkedHashMap<Class<*>, (KType, KProperty<*>?, Token) -> Any?> = LinkedHashMap()

    init {
        Seed.seed
        fun list(type: KType, kProperty: KProperty<*>?, token: Token): List<*> {
            val type1 = type.arguments.first().type!!
            return aList(type1, token, kProperty)
        }

        fun <T : Any> list(klass: KClass<T>, kProperty: KProperty<*>?, token: Token): List<T> = aList(klass.createType(), token, kProperty) as List<T>
        fun map(type: KType, kProperty: KProperty<*>?, token: Token) = list(type, kProperty, token)
                .map { Pair(it, instantiateRandomClass(type.arguments[1].type!!, kProperty, token)) }.toMap()

        val o = ObjectFactory

        o[kotlin.String::class.java] = { _, kproperty, token -> aString(token) }
        o[kotlin.Byte::class.java] = { _, kproperty, token -> aByte(token) }
        o[kotlin.Int::class.java] = { _, kproperty, token -> anInt(token) }
        o[0.toUInt().let { it }::class.java] = { _, kproperty, token -> anUInt(token) }
        o[kotlin.Long::class.java] = { _, kproperty, token -> aLong(token) }
        o[0.toULong().let { it }::class.java] = { _, kproperty, token -> aULong(token) }
        o[kotlin.Short::class.java] = { _, kproperty, token -> aShort(token) }
        o[0.toUShort().let { it }::class.java] = { _, kproperty, token -> aUShort(token) }
        o[kotlin.Double::class.java] = { _, kproperty, token -> aDouble(token) }
        o[kotlin.Float::class.java] = { _, kproperty, token -> aFloat(token) }
        o[kotlin.Boolean::class.java] = { _, kproperty, token -> aBoolean(token) }
        o[kotlin.Char::class.java] = { _, kproperty, token -> aChar(token) }
        o[List::class.java] = { type, kproperty, token -> list(type, kproperty, token) }
        o[Set::class.java] = { type, kproperty, token -> list(type, kproperty, token).toSet() }
        o[File::class.java] = { _, kproperty, token -> File(aString(token)) }
        o[Date::class.java] = { _, kproperty, token -> Date(aLong(token)) }
        o[kotlin.collections.Map::class.java] = { type, kproperty, token -> map(type, kproperty, token) }


        val g = GenericObjectFactory
        g[IntArray::class(Int::class()).type] = { _, kproperty, token -> list(Int::class, kproperty, token).toIntArray() }
        g[ShortArray::class(Short::class()).type] = { _, kproperty, token -> list(Short::class, kproperty, token).toShortArray() }
        g[LongArray::class(Long::class()).type] = { _, kproperty, token -> list(Long::class, kproperty, token).toLongArray() }
        g[FloatArray::class(Float::class()).type] = { _, kproperty, token -> list(Float::class, kproperty, token).toFloatArray() }
        g[DoubleArray::class(Double::class()).type] = { _, kproperty, token -> list(Double::class, kproperty, token).toDoubleArray() }
        g[BooleanArray::class(Boolean::class()).type] = { _, kproperty, token -> list(Boolean::class, kproperty, token).toBooleanArray() }
        g[ByteArray::class(Byte::class()).type] = { _, kproperty, token -> list(Byte::class, kproperty, token).toByteArray() }
        g[CharArray::class(Char::class()).type] = { _, kproperty, token -> list(Char::class, kproperty, token).toCharArray() }
        g[Array<Int>::class(Int::class()).type] = { _, kproperty, token -> list(Int::class, kproperty, token).toTypedArray() }
        g[Array<Short>::class(Short::class()).type] = { _, kproperty, token -> list(Short::class, kproperty, token).toTypedArray() }
        g[Array<Long>::class(Long::class()).type] = { _, kproperty, token -> list(Long::class, kproperty, token).toTypedArray() }
        g[Array<Float>::class(Float::class()).type] = { _, kproperty, token -> list(Float::class, kproperty, token).toTypedArray() }
        g[Array<Double>::class(Double::class()).type] = { _, kproperty, token -> list(Double::class, kproperty, token).toTypedArray() }
        g[Array<Boolean>::class(Boolean::class()).type] = { _, kproperty, token -> list(Boolean::class, kproperty, token).toTypedArray() }
        g[Array<Byte>::class(Byte::class()).type] = { _, kproperty, token -> list(Byte::class, kproperty, token).toTypedArray() }
        g[Array<Char>::class.invoke(Char::class()).type] = { _, kproperty, token -> list(Char::class, kproperty, token).toTypedArray() }
    }


    internal object GenericObjectFactory {
        private val objectFactories = mutableMapOf<KType, (KType, KProperty<*>?, Token) -> Any?>()

        operator fun set(type: KType, factory: (KType, KProperty<*>?, Token) -> Any?): GenericObjectFactory {
            objectFactories.put(type, factory)
            return this
        }

        fun putIfAbsent(type: KType, factory: (KType, KProperty<*>?, Token) -> Any?) = objectFactories.putIfAbsent(type, factory)

        operator fun get(type: KType): ((KType, KProperty<*>?, Token) -> Any?)? {
            return objectFactories[type]
                    ?: if (type.arguments.isNotEmpty()) objectFactories[type.jvmErasure.starProjectedType] else null
        }

        operator fun contains(type: KType): Boolean {
            return get(type)?.let { true } ?: false
        }
    }

    private val maxChar = 59319
    private val maxStringLength = 5

    private fun aChar(token: Long): Char = pseudoRandom(token).nextInt(maxChar).toChar()
    private fun anInt(token: Long, max: Int = Int.MAX_VALUE): Int = seededToken(token).toInt() % max
    private fun anUInt(token: Long, max: Int = Int.MAX_VALUE): UInt = (seededToken(token) % max).toUInt()

    private fun aLong(token: Long): Long = pseudoRandom(token).nextLong()
    private fun aULong(token: Long): ULong = pseudoRandom(token).nextLong().toULong()
    private fun aDouble(token: Long): Double = pseudoRandom(token).nextDouble()
    private fun aShort(token: Long): Short = anUInt(token, UShort.MAX_VALUE.toInt()).toShort()
    private fun aUShort(token: Long): UShort = anUInt(token, UShort.MAX_VALUE.toInt()).toUShort()
    private fun aFloat(token: Long): Float = pseudoRandom(token).nextFloat()
    private fun aByte(token: Long): Byte = pseudoRandom(token).nextInt(255).toByte()
    private fun aBoolean(token: Long): Boolean = pseudoRandom(token).nextBoolean()

    private inline fun aString(token: Long): String {
        val seededToken = seededToken(token)
        val size = (seededToken.toInt().absoluteValue % 3) + 1
        return RandomStringUtils.random(size, 0, 5000, true, true, null, Random(seededToken))
    }

    internal inline infix fun Long.with(other: Long): Long = this * 31 + other
    internal inline infix fun Long.with(other: Int): Long = this * 31 + other
    internal inline infix fun Int.with(other: Long): Long = this * 31 + other
    internal inline infix fun Int.with(other: Int): Long = (this * 31 + other).toLong()

    internal fun aList(type: KType,
                       token: Long,
                       kProperty: KProperty<*>?,
                       size: Int? = null,
                       minSize: Int = 1,
                       maxSize: Int = 5): List<*> {
        val klass = type.jvmErasure

        val items = 0..(size ?: (pseudoRandom(token).nextInt(maxSize - minSize) + minSize))

        return items.map {
            if (klass == List::class) {
                val type1 = type.arguments.first().type!!
                aList(type1, token.hashCode() with it.hashCode(), kProperty)
            } else instantiateRandomClass(type, type.jvmErasure.java, token.hashCode() with it.hashCode(), kProperty)
        }
    }

    fun KClass<out Any>.isAnInterfaceOrSealed() = this.java.isInterface || this.isSealed || this.isAbstract
    fun KClass<out Any>.isAnArray() = this.java.isArray
    fun KClass<out Any>.isAnEnum() = this.java.isEnum
    fun KClass<out Any>.isAnObject() = this.objectInstance != null

    internal fun instantiateRandomClass(type: KType, java: Class<*>, token: Token = 0, kProperty: KProperty<*>?): Any? {

        val markedNullable = type.isMarkedNullable
        when {
            markedNullable && (token with Seed.seed) % 2 == 0L -> return null
        }
        val get = ObjectFactory.get(if (markedNullable) type.withNullability(false).jvmErasure.java else java)
        when {
            get != null -> return get.invoke(type, kProperty, token)
            type in GenericObjectFactory -> return GenericObjectFactory[type]?.invoke(type, kProperty, token)
        }
        val klass = type.jvmErasure
        when {
            klass.isAnEnum() -> return java.enumConstants[Random(seededToken(token)).nextInt(java.enumConstants.size)]
            klass.isAnArray() -> return instantiateArray(type, token, kProperty)
            klass.isAnInterfaceOrSealed() -> return instantiateAbstract(type, token, kProperty)
            klass.isAnObject() -> return klass.objectInstance
            else -> return instantiateArbitraryClass(klass, java, token, type, kProperty)
        }
    }

    private fun instantiateArray(type: KType, token: Token, kProperty: KProperty<*>?): Array<Any?> {
        val genericType = type.arguments.first().type!!
        val list = aList(genericType, token, kProperty)
        val array = newInstance(genericType.jvmErasure.java, list.size) as Array<Any?>
        return array.apply { list.forEachIndexed { index, any -> array[index] = any } }
    }

    private fun instantiateAbstract(type: KType, token: Token, kProperty: KProperty<*>?): Any {
        val allClassesInModule = if (classes.isEmpty()) {
            getAndCacheClasses(type)
        } else classes

        val klass = type.jvmErasure

        val allImplementationsInModule = classesMap[klass] ?: allClassesInModule
                .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                .let {
                    if (it.isEmpty()) getAndCacheClasses(type)
                            .filter { klass.java != it && klass.java.isAssignableFrom(it) }
                    else it
                }
                .apply { classesMap.put(klass, this) }

        return allImplementationsInModule.getOrNull(pseudoRandom(token).int(allImplementationsInModule.size))
                ?.let {
                    val params = it.kotlin.typeParameters.map { KTypeProjection(it.variance, it.starProjectedType) }
                    instantiateRandomClass(it.kotlin.createType(params), kProperty, token.hashCode() with it.name.hashCode())
                }
                ?: instantiateNewInterface(type, token, kProperty)
    }

    private fun getAndCacheClasses(type: KType): Set<Class<out Any>> {
        val packages = (type.jvmErasure.allSuperclasses + type.jvmErasure).map { it.qualifiedName?.split(".")?.take(2)?.joinToString(".") }
        val reflections = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false))
                .setUrls(ClasspathHelper.forClassLoader())
                .filterInputsBy(FilterBuilder().includePackage(*packages.toTypedArray())))
        return reflections.getSubTypesOf(Any::class.java).apply { classes.addAll(this) }
    }

    private fun instantiateNewInterface(type: KType, token: Token, kProperty: KProperty<*>?): Any {

        val klass = type.jvmErasure
        val genericTypeNameToConcreteTypeMap = klass.typeParameters.map { it.name }.zip(type.arguments).toMap()

        fun degenerify(kType: KType): KType {
            return if (kType.arguments.isEmpty()) genericTypeNameToConcreteTypeMap[kType.javaType.typeName]?.type
                    ?: kType
            else {
                val argumentField = kType::class.java.declaredFields.find { it.name == "${kType::arguments.name}\$delegate" }!!
                argumentField.isAccessible = true
                val degenerifiedArguments = kType.arguments.map { KTypeProjection(it.variance, degenerify(it.type!!)) }
                val newFieldValue = ReflectProperties.lazySoft { degenerifiedArguments }
                argumentField.set(kType, newFieldValue)
                kType
            }
        }

        val javaMethods: Array<Method> = klass.java.methods + Any::class.java.methods

        val methodReturnTypes = javaMethods.map { method ->
            val returnType = klass.members.find { member ->
                fun hasNameName(): Boolean = (method.name == member.name || method.name == "get${member.name.capitalize()}")
                fun hasSameArguments() = method.parameterTypes.map { it.name } == member.valueParameters.map { it.type.jvmErasure.jvmName }
                hasNameName() && hasSameArguments()
            }?.returnType?.let { degenerify(it) }

            val type1 = genericTypeNameToConcreteTypeMap[returnType?.jvmErasure?.simpleName]?.type
            method to (type1 ?: returnType)
        }.toMap()

        val factory = ProxyFactory()
        if (klass.java.isInterface) {
            factory.interfaces = arrayOf(klass.java)
        } else {
            factory.superclass = klass.java
        }
        val proxy = factory.createClass().newInstance()

        (proxy as ProxyObject).setHandler({ proxy, method, proceed, obj ->
            when (method.name) {
                Any::hashCode.javaMethod?.name -> proxy.toString().hashCode()
                Any::equals.javaMethod?.name -> proxy.toString() == obj[0].toString()
                Any::toString.javaMethod?.name -> "\$RandomImplementation$${klass.simpleName}"
                else -> methodReturnTypes[method]?.let { instantiateRandomClass(it, it.jvmErasure.java, token, kProperty) }
                        ?: instantiateRandomClass(method.returnType.kotlin.createType(), method.returnType, token, kProperty)
            }
        })
        return proxy
    }

    private fun instantiateArbitraryClass(klass: KClass<out Any>, java: Class<*>, token: Token, type: KType, kProperty: KProperty<*>?): Any? {
        val constructors = klass.constructors.filter { !it.parameters.any { (it.type.jvmErasure == klass) } }.toList()
        if (constructors.isEmpty() && klass.constructors.any { it.parameters.any { (it.type.jvmErasure == klass) } }) throw CyclicException()
        val defaultConstructor = constructors[pseudoRandom(token).int(constructors.size)] as KFunction<*>
        if (!defaultConstructor.isAccessible) {
            defaultConstructor.isAccessible = true
        }
        val constructorTypeParameters by lazy { defaultConstructor.valueParameters.map { it.type.toString().replace("!", "").replace("?", "") }.toMutableList() }
        val typeMap by lazy { type.jvmErasure.typeParameters.map { it.name }.zip(type.arguments).toMap() }
        val pairedConstructor = defaultConstructor.parameters.map { if (it.type.javaType is TypeVariable<*>) constructorTypeParameters.get(it.index) to it else "" to it }


        val recipe = pairedConstructor.map { (first, second: KParameter) ->
            val tpe = if (second.type.javaType is TypeVariable<*>) typeMap[first]?.type ?: second.type else second.type
            val jvmErasure = tpe.jvmErasure
            ContructorParam(tpe, jvmErasure.java, jvmErasure.jvmName.hashCode() with second.name!!.hashCode())
        }

        val parameters by lazy {
            (pairedConstructor.map { (first, second: KParameter) ->
                fun isTypeVariable() = second.type.javaType is TypeVariable<*>
                val tpe = if (isTypeVariable()) typeMap[first]?.type ?: second.type else second.type
                instantiateRandomClass(tpe, tpe.jvmErasure.java, token.hashCode() with tpe.jvmErasure.jvmName.hashCode() with second.name!!.hashCode(), kProperty)
            }).toTypedArray()
        }
        val javaConstructor = defaultConstructor.javaConstructor!!

        try {
            val factory: (KType, KProperty<*>?, Token) -> Any? = { type, prop, token ->
                val array = arrayOfNulls<Any>(recipe.size)
                var count = 0

                for (it in recipe) {
                    array[count++] = instantiateRandomClass(it.type, it.java, token.hashCode() with it.parttoken, prop)
                }

                try {
                    javaConstructor.newInstance(*array)
                } catch (e: Throwable) {
                    defaultConstructor.call(*array)
                }
            }

            if (typeMap.isEmpty()) {
                ObjectFactory.putIfAbsent(klass.java, factory)
            } else {
                GenericObjectFactory.putIfAbsent(type, factory)
            }
            return instantiateRandomClass(type, java, token, kProperty)
        } catch (e: Throwable) {
            val namedParameters = parameters.zip(defaultConstructor.parameters.map { it.name }).map { "${it.second}=${it.first}" }
            throw CreationException("""Something went wrong when trying to instantiate class ${klass}
         using constructor: $defaultConstructor
         with values: $namedParameters""", e.cause)
        }
    }

    private fun Random.int(bound: Int) = if (bound == 0) 0 else nextInt(bound)
    private val classes: MutableSet<Class<out Any>> = mutableSetOf()
    private val classesMap: MutableMap<KClass<out Any>, List<Class<out Any>>> = mutableMapOf()
    private fun pseudoRandom(token: Long): Random = Random(seededToken(token))

    private inline fun seededToken(token: Long) = Seed.seed with token
    private val listClass = List::class
    private val mapClass = Map::class
    private val setClass = Set::class
}

data class ContructorParam(val type: KType, val java: Class<*>, val parttoken: Token)
