package parser

import ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

import lexer.Lexer

class ParserTest {

  @Test
  fun `let statements`() {
    val input = """
      let x = 5;
      let y = 10;
      let z = 838383;
    """.trimIndent()

    val program = parseValidProgram(input)

    assertEquals(3, program.statements.size)

    val expectedIdentifiers = listOf("x", "y", "z")

    program.statements.zip(expectedIdentifiers).forEach { (statement, identifier) ->
      testLetStatement(statement, identifier)
    }
  }

  @Test
  fun `return statements`() {
    val input = """
      return 5;
      return 10;
      return 993322;
    """.trimIndent()

    val program = parseValidProgram(input)
    assertEquals(3, program.statements.size)

    program.statements.forEach { statement ->
      testReturnStatement(statement)
    }
  }

  @Test
  fun `identifier expression`() {
    val input = "someIdentifier;"

    val program = parseValidProgram(input)

    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())

    when(expression) {
      is Identifier -> assertEquals("someIdentifier", expression.value)
      else -> fail("$expression is not an identifier")
    }
  }

  @Test
  fun `integer expression`() {
    val input = "5;"

    val program = parseValidProgram(input)
    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    testIntegerLiteral(expression, 5)
  }

  private fun testIntegerLiteral(expression: Expression, value: Int) {
    when (expression) {
      is IntegerLiteral -> assertEquals(value, expression.value)
      else -> fail("$expression is not an integer literal")
      }
  }

  @Test
  fun `negate prefix expression`() {
    val program = parseValidProgram("!5;")

    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    val prefixExpression = testPrefixExpression(expression, "!")
    testIntegerLiteral(prefixExpression.right, 5)
  }

  @Test
  fun `minus prefix expression`() {
    val program = parseValidProgram("-15;")

    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    val prefixExpression = testPrefixExpression(expression, "-")
    testIntegerLiteral(prefixExpression.right, 15)
  }

  @Test
  fun `parsing infix expressions`() {
    data class InfixTestCase(
      val input: String,
      val leftValue: Int,
      val operator: String,
      val rightValue: Int
    )
    val testCases = listOf(
      InfixTestCase("5 + 5;", 5, "+", 5),
      InfixTestCase("5 - 5;", 5, "-", 5),
      InfixTestCase("5 * 5;", 5, "*", 5),
      InfixTestCase("5 / 5;", 5, "/", 5),
      InfixTestCase("5 < 5;", 5, "<", 5),
      InfixTestCase("5 > 5;", 5, ">", 5),
      InfixTestCase("5 == 5;", 5, "==", 5),
      InfixTestCase("5 != 5;", 5, "!=", 5)
    )

    testCases.forEach { (input, leftValue, operator, rightValue) ->
      val program = parseValidProgram(input)
      assertEquals(1, program.statements.size)

      val expression = getExpression(program.statements.first())
      val infixExpression = testInfixExpression(expression, operator)
      testIntegerLiteral(infixExpression.left, leftValue)
      testIntegerLiteral(infixExpression.right, rightValue)
    }
  }

  private fun testInfixExpression(expression: Expression, operator: String): InfixExpression {
    when (expression) {
      is InfixExpression -> {
        assertEquals(operator, expression.operator)
        return expression
      }
      else -> fail("$expression is not an infix expression")
    }
  }

  private fun parseValidProgram(input: String): Program {
    val lexer = Lexer(input)
    val parser = Parser(lexer)

    val program = parser.parseProgram()
    checkParseErrors(parser)
    return program
  }

  private fun testLetStatement(statement: Statement, identifier: String) {
    when(statement) {
      is LetStatement -> assertEquals(identifier, statement.name.value)
      else -> fail("Expected let statement, found ${statement::class.simpleName}")
    }
  }

  private fun testReturnStatement(statement: Statement) {
    when(statement) {
      is ReturnStatement -> Unit
      else -> fail("Expected return statement, found ${statement::class.simpleName}")
    }
  }

  private fun getExpression(statement: Statement): Expression {
    return when(statement) {
      is ExpressionStatement -> statement.expression
      else -> fail("$statement is not an expression statement")
    }
  }

  private fun testPrefixExpression(
    expression: Expression,
    operator: String
  ): PrefixExpression {
    return when (expression) {
      is PrefixExpression -> {
        assertEquals(operator, expression.operator)
        expression
      }
      else -> fail("$expression is not a prefix expression")
    }
  }


  private fun checkParseErrors(parser: Parser) {
    if (parser.errors.isEmpty()) return

    val errorsAsString = parser.errors.joinToString("\n  ", "  ")

    fail("Parser has ${parser.errors.size} errors\n$errorsAsString")
  }
}
