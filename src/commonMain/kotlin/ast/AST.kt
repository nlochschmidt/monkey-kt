package kotlin.ast

import token.Token

interface Node {
  val literal: String
}

interface Statement : Node

interface Expression : Node

data class Program(val statements: List<Statement>) : Node {
  override val literal: String
    get() = statements.firstOrNull()?.literal ?: ""
}

data class LetStatement(
  val token: Token,
  val name: Identifier,
  val value: Expression
) : Statement {
  override val literal: String
    get() = token.literal
}

data class Identifier(
  val token: Token,
  val value: String
): Expression {
  override val literal: String
    get() = token.literal
}

data class ReturnStatement(
  val token: Token,
  val returnValue: Expression
) : Statement {
  override val literal: String
    get() = token.literal
}
