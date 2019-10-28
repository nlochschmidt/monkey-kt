package evaluator

import `object`.Bool
import `object`.Integer
import `object`.Null
import `object`.Object
import ast.*

val TRUE = Bool(true)
val FALSE = Bool(false)

fun eval(node: Node): Object {
  return when(node) {
    is Program -> evalStatements(node.statements)
    is ExpressionStatement -> eval(node.expression)
    is IntegerLiteral -> Integer(node.value)
    is BooleanLiteral -> node.nativeBooleanToBoolObject()
    else -> Null
  }
}

fun evalStatements(statements: List<Statement>): Object {
  return statements.fold(Null) { _: Object, statement -> eval(statement) }
}

private fun BooleanLiteral.nativeBooleanToBoolObject() =
  if (value) TRUE else FALSE
