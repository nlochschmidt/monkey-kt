package `object`

import `object`.ObjectType.*

interface Object {
  val type: ObjectType
}

// FIXME: I might be able to get away without the ObjectType. Let's see

data class Integer(val value: Long) : Object {
  override val type: ObjectType = INTEGER

  override fun toString(): String = value.toString()
}

data class Bool(val value: Boolean): Object {
  override val type: ObjectType = BOOLEAN

  override fun toString(): String = value.toString()
}

object Null : Object {
  override val type: ObjectType = NULL

  override fun toString(): String = "null"
}
