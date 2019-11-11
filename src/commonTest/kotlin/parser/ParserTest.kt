package parser

import ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

import lexer.Lexer

class ParserTest {

  @Test
  fun `let statements`() {
    data class LetTestCase<T>(val input: String, val expectedIdentifier: String, val expectedValue: T)
    val testCases = listOf(
      LetTestCase("let x = 5;", "x", 5),
      LetTestCase("let y = true;", "y", true),
      LetTestCase("let z = y;", "z", "y")
    )

    testCases.forEach { (input, expectedIdentifier, expectedValue) ->
      val program = parseValidProgram(input)
      val letStatement = testLetStatement(program.statements.first(), expectedIdentifier)
      testLiteralExpression(letStatement.value, expectedValue)
    }
  }

  @Test
  fun `return statements`() {
    data class ReturnTestCase<T>(val input: String, val expectedValue: T)
    val testCases = listOf(
      ReturnTestCase("return 5;", 5),
      ReturnTestCase("return true;", true),
      ReturnTestCase("return x", "x")
    )

    testCases.forEach { (input, expectedValue) ->
      val program = parseValidProgram(input)
      testReturnStatement(program.statements.first(), expectedValue)
    }
  }

  @Test
  fun `identifier expression`() {
    val input = "someIdentifier;"

    val program = parseValidProgram(input)

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
  fun `string literal expression`() {
    val input = "\"hello world\";"

    val program = parseValidProgram(input)

    when (val expression = getExpression(program.statements.first())) {
      is StringLiteral -> assertEquals("hello world", expression.value)
      else -> fail("$expression is not a string literal")
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
      "!(true == true)" to "(!(true == true))",
      "a + add(b * c) + d" to "((a + add((b * c))) + d)",
      "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))" to "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))",
      "add(a + b + c * d / f + g)" to "add((((a + b) + ((c * d) / f)) + g))"
    )

    testCases.forEach { (input, expected) ->
      val programAsString = parseValidProgram(input, expectedStatements = null).toString()
      assertEquals(expected, programAsString)
    }
  }

  @Test
  fun `parse If expressions`() {
    val input = "if (x < y) { x }"

    val program = parseValidProgram(input)

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

  @Test
  fun `parse function literal`() {
    val input = "fn(x, y) { x + y; }"

    val program = parseValidProgram(input)

    val expression = getExpression(program.statements.first())

    when (expression) {
      is FunctionLiteral -> {
        assertEquals(2, expression.parameters.size)
        val (firstCondition, secondCondition) = expression.parameters
        testLiteralExpression(firstCondition, "x")
        testLiteralExpression(secondCondition, "y")

        assertEquals(1, expression.body.statements.size)
        val bodyExpression = getExpression(expression.body.statements.first())

        testInfixExpression(bodyExpression, "x", "+", "y")
      }
      else -> fail("$expression is not a function literal")
    }
  }

  @Test
  fun `parse function parameters`() {
    val testCases = mapOf(
      "fn() {};" to emptyList(),
      "fn(x) {};" to listOf("x"),
      "fn(x, y, z) {};" to listOf("x", "y", "z")
    )

    testCases.forEach { (input, expectedParameters) ->
      val program = parseValidProgram(input)
      val expression = getExpression(program.statements.first())
      when (expression) {
        is FunctionLiteral -> {
          assertEquals(expectedParameters, expression.parameters.map { it.value })
        }
        else -> fail("$expression is not a function literal")
      }
    }
  }

  @Test
  fun `parse call expression`() {
    val input = "add(1, 2 * 3, 4 + 5);"

    val program = parseValidProgram(input)
    val expression = getExpression(program.statements.first())

    when (expression) {
      is CallExpression -> {
        testIdentifier(expression.function, "add")
        assertEquals(3, expression.arguments.size)
        val (firstArgument, secondArgument, thirdArgument) = expression.arguments
        testLiteralExpression(firstArgument, 1)
        testInfixExpression(secondArgument, 2, "*", 3)
        testInfixExpression(thirdArgument, 4, "+", 5)
      }
      else -> fail("$expression is not a call expression")
    }
  }

  @Test
  fun `parse call arguments`() {
    val testCases = mapOf(
      "add();" to emptyList(),
      "add(x);" to listOf("x"),
      "add(x, y, z);" to listOf("x", "y", "z")
    )

    testCases.forEach { (input, expectedArguments) ->
      val program = parseValidProgram(input)
      val expression = getExpression(program.statements.first())
      when (expression) {
        is CallExpression -> {
          assertEquals(expectedArguments, expression.arguments.map { (it as Identifier).value })
        }
        else -> fail("$expression is not a call expression")
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

  private fun parseValidProgram(input: String, expectedStatements: Int? = 1): Program {
    val lexer = Lexer(input)
    val parser = Parser(lexer)

    val program = parser.parseProgram()
    checkParseErrors(parser)
    if (expectedStatements != null) {
      assertEquals(expectedStatements, program.statements.size)
    }
    return program
  }

  private fun testLetStatement(statement: Statement, identifier: String): LetStatement {
    return when (statement) {
      is LetStatement -> {
        assertEquals(identifier, statement.name.value)
        statement
      }
      else -> fail("Expected let statement, found ${statement::class.simpleName}")
    }
  }

  private fun <T: Any> testReturnStatement(statement: Statement, expectedValue: T) {
    when (statement) {
      is ReturnStatement -> testLiteralExpression(statement.returnValue, expectedValue)
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
