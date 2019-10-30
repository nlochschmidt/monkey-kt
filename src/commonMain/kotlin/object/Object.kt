package `object`

import `object`.ObjectType.*

interface Object {
  val type: ObjectType
}

// FIXME: I might be able to get away without the ObjectType. Let's see

// FIXME: The wrapper classes might be convertible to inline classes
data class Integer(val value: Int) : Object {
  override val type: ObjectType = INTEGER

  override fun toString(): String = value.toString()
}

class Bool private constructor (val value: Boolean): Object {
  override val type: ObjectType = BOOLEAN

  override fun toString(): String = value.toString()

  companion object {
    val TRUE = Bool(true)
    val FALSE = Bool(false)

    operator fun invoke(value: Boolean): Bool {
      return if (value) TRUE else FALSE
    }
  }
}

object Null : Object {
  override val type: ObjectType = NULL

  override fun toString(): String = "null"
}
