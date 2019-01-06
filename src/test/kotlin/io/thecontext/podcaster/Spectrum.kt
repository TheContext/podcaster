package io.thecontext.podcaster

import com.greghaskins.spectrum.dsl.specification.Specification
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : Any> memoized(noinline initializer: () -> T) = SupplierProperty(initializer)

class SupplierProperty<out T : Any>(initializer: () -> T) : ReadOnlyProperty<Any?, T> {

    private val supplier = Specification.let(initializer)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = supplier.get()
}