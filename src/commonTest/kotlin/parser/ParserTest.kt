package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

import lexer.Lexer
import kotlin.ast.Expression
import kotlin.ast.ExpressionStatement
import kotlin.ast.Program
import kotlin.ast.Identifier
import kotlin.ast.IntegerLiteral
import kotlin.ast.LetStatement
import kotlin.ast.ReturnStatement
import kotlin.ast.Statement

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

    when (expression) {
      is IntegerLiteral -> assertEquals(5, expression.value)
      else -> fail("$expression is not an integer literal")
    }

  }

  private fun parseValidProgram(input: String): Program {
    val lexer = Lexer(input)
    val parser = Parser(lexer)

    val program = parser.parseProgram()
    checkParseErrors(parser)
    return program
  }

  fun testLetStatement(statement: Statement, identifier: String) {
    when(statement) {
      is LetStatement -> assertEquals(identifier, statement.name.value)
      else -> fail("Expected let statement, found ${statement::class.simpleName}")
    }
  }

  fun testReturnStatement(statement: Statement) {
    when(statement) {
      is ReturnStatement -> Unit
      else -> fail("Expected return statement, found ${statement::class.simpleName}")
    }
  }

  fun getExpression(statement: Statement): Expression {
    return when(statement) {
      is ExpressionStatement -> statement.expression
      else -> fail("$statement is not an expression statement")
    }
  }

  fun checkParseErrors(parser: Parser) {
    if (parser.errors.isEmpty()) return

    val errorsAsString = parser.errors.joinToString("\n  ", "  ")

    fail("Parser has ${parser.errors.size} errors\n$errorsAsString")
  }
}
