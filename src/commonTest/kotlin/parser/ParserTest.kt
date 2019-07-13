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

    testIdentifier(expression, "someIdentifier")
  }

  private fun testIdentifier(expression: Expression, identifier: String) {
    when (expression) {
      is Identifier -> assertEquals(identifier, expression.value)
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
  fun `boolean expression`() {
    val input = "true;"

    val program = parseValidProgram(input)
    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    testBooleanLiteral(expression, true)
  }

  private fun testBooleanLiteral(expression: Expression, value: Boolean) {
    when (expression) {
      is BooleanLiteral -> assertEquals(value, expression.value)
      else -> fail("$expression is not an integer literal")
    }
  }

  @Test
  fun `parsing prefix expression`() {

    data class PrefixTestCase<T : Any>(val input: String, val operator: String, val value: T)

    val testCases = listOf(
      PrefixTestCase("!5;", "!", 5),
      PrefixTestCase("-15;", "-", 15),
      PrefixTestCase("!true;", "!", true),
      PrefixTestCase("!false;", "!", false)
    )

    testCases.forEach { (input, operator, value) ->
      val program = parseValidProgram(input)
      val expression = getExpression(program.statements.first())
      testPrefixExpression(expression, operator, value)
    }
  }

  @Test
  fun `parsing infix expressions`() {
    data class InfixTestCase<T : Any, U : Any>(
      val input: String,
      val leftValue: T,
      val operator: String,
      val rightValue: U
    )

    val testCases = listOf(
      InfixTestCase("5 + 5;", 5, "+", 5),
      InfixTestCase("5 - 5;", 5, "-", 5),
      InfixTestCase("5 * 5;", 5, "*", 5),
      InfixTestCase("5 / 5;", 5, "/", 5),
      InfixTestCase("5 < 5;", 5, "<", 5),
      InfixTestCase("5 > 5;", 5, ">", 5),
      InfixTestCase("5 == 5;", 5, "==", 5),
      InfixTestCase("5 != 5;", 5, "!=", 5),
      InfixTestCase("true == true", true, "==", true),
      InfixTestCase("true != false", true, "!=", false),
      InfixTestCase("false == false", false, "==", false)
    )

    testCases.forEach { (input, leftValue, operator, rightValue) ->
      val program = parseValidProgram(input)
      assertEquals(1, program.statements.size)

      val expression = getExpression(program.statements.first())
      testInfixExpression(expression, leftValue, operator, rightValue)
    }
  }

  @Test
  fun `operator precedence parsing`() {
    val testCases = mapOf(
      "-a * b" to "((-a) * b)",
      "!-a" to "(!(-a))",
      "a + b + c" to "((a + b) + c)",
      "a + b - c" to "((a + b) - c)",
      "a * b * c" to "((a * b) * c)",
      "a * b / c" to "((a * b) / c)",
      "a + b / c" to "(a + (b / c))",
      "a + b * c + d / e - f" to "(((a + (b * c)) + (d / e)) - f)",
      "3 + 4; -5 * 5" to "(3 + 4)\n((-5) * 5)",
      "5 > 4 == 3 < 4" to "((5 > 4) == (3 < 4))",
      "5 < 4 != 3 > 4" to "((5 < 4) != (3 > 4))",
      "3 + 4 * 5 == 3 * 1 + 4 * 5" to "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
      "3 > 5 == false" to "((3 > 5) == false)",
      "3 < 5 == true" to "((3 < 5) == true)",
      "1 + (2 + 3) + 4" to "((1 + (2 + 3)) + 4)",
      "(5 + 5) * 2" to "((5 + 5) * 2)",
      "2 / (5 + 5)" to "(2 / (5 + 5))",
      "-(5 + 5)" to "(-(5 + 5))",
      "!(true == true)" to "(!(true == true))"
    )

    testCases.forEach { (input, expected) ->
      val programAsString = parseValidProgram(input).toString()
      assertEquals(expected, programAsString)
    }
  }

  @Test
  fun `parse If expressions`() {
    val input = "if (x < y) { x }"

    val program = parseValidProgram(input)

    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    when (expression) {
      is IfExpression -> {
        testInfixExpression(expression.condition, "x", "<", "y")
        assertEquals(1, expression.consequence.statements.size)
        val consequenceExpression = getExpression(expression.consequence.statements.first())
        testIdentifier(consequenceExpression, "x")

        if (expression.alternative != null) {
          fail("alternative was not null")
        }
      }
    }
  }

  @Test
  fun `parse If Else expressions`() {
    val input = "if (x < y) { x } else { y }"

    val program = parseValidProgram(input)

    assertEquals(1, program.statements.size)

    val expression = getExpression(program.statements.first())
    when (expression) {
      is IfExpression -> {
        testInfixExpression(expression.condition, "x", "<", "y")
        assertEquals(1, expression.consequence.statements.size)
        val consequenceExpression = getExpression(expression.consequence.statements.first())
        testIdentifier(consequenceExpression, "x")
        assertEquals(1, expression.alternative?.statements?.size)
        val alternativeExpression = getExpression(expression.alternative!!.statements.first())
        testIdentifier(alternativeExpression, "y")
      }
    }
  }

  private fun <T : Any> testLiteralExpression(expression: Expression, value: T) {
    return when (value) {
      is Int -> testIntegerLiteral(expression, value)
      is Boolean -> testBooleanLiteral(expression, value)
      is String -> testIdentifier(expression, value)
      else -> fail("Unknown type ${value::class.simpleName}")
    }
  }

  private fun <T : Any, U : Any> testInfixExpression(
    expression: Expression,
    left: T,
    operator: String,
    right: U
  ): InfixExpression {
    when (expression) {
      is InfixExpression -> {
        testLiteralExpression(expression.left, left)
        assertEquals(operator, expression.operator)
        testLiteralExpression(expression.right, right)
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
    when (statement) {
      is LetStatement -> assertEquals(identifier, statement.name.value)
      else -> fail("Expected let statement, found ${statement::class.simpleName}")
    }
  }

  private fun testReturnStatement(statement: Statement) {
    when (statement) {
      is ReturnStatement -> Unit
      else -> fail("Expected return statement, found ${statement::class.simpleName}")
    }
  }

  private fun getExpression(statement: Statement): Expression {
    return when (statement) {
      is ExpressionStatement -> statement.expression
      else -> fail("$statement is not an expression statement")
    }
  }

  private fun <T : Any> testPrefixExpression(
    expression: Expression,
    operator: String,
    value: T
  ): PrefixExpression {
    return when (expression) {
      is PrefixExpression -> {
        assertEquals(operator, expression.operator)
        testLiteralExpression(expression.right, value)
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
