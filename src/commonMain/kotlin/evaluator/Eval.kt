package evaluator

import `object`.Bool
import `object`.Integer
import `object`.Null
import `object`.Object
import ast.*

fun eval(node: Node): Object {
  return when(node) {
    is Program -> evalStatements(node.statements)
    is ExpressionStatement -> eval(node.expression)
    is IntegerLiteral -> Integer(node.value)
    is BooleanLiteral -> Bool(node.value)
    else -> Null
  }
}

fun evalStatements(statements: List<Statement>): Object {
  return statements.fold(Null) { _: Object, statement -> eval(statement) }
}
