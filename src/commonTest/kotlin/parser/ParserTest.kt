package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

import lexer.Lexer
import kotlin.ast.Identifier
import kotlin.ast.LetStatement
import kotlin.ast.Statement

class ParserTest {

  @Test
  fun `let statements`() {
    val input = """
      let x = 5;
      let y = 10;
      let z = 838383;
    """.trimIndent()

    val lexer = Lexer(input)
    val parser = Parser(lexer)

    val program = parser.parseProgram()
    checkParseErrors(parser)

    assertEquals(3, program.statements.size)

    val expectedIdentifiers = listOf("x", "y", "z")

    program.statements.zip(expectedIdentifiers).forEach { (statement, identifier) ->
      testLetStatement(statement, identifier)
    }
  }

  fun testLetStatement(statement: Statement, identifier: String) {
    when(statement) {
      is LetStatement -> assertEquals(identifier, statement.name.value)
      else -> fail("Expected let statement, found ${statement::class.simpleName}")
    }
  }

  fun checkParseErrors(parser: Parser) {
    if (parser.errors.isEmpty()) return

    val errorsAsString = parser.errors.joinToString("\n  ", "  ")

    fail("Parser has ${parser.errors.size} errors\n$errorsAsString")
  }
}
