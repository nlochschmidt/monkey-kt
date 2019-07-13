package ast

import token.Token
import token.TokenType.IDENT
import token.TokenType.LET
import kotlin.test.Test
import kotlin.test.assertEquals

class ASTTest {

  @Test
  fun `AST to string`() {
    val program = Program(
      statements = listOf(
        LetStatement(
          token = Token(type = LET, literal = "let"),
          name = Identifier(
            token = Token(type = IDENT, literal = "myVar"),
            value = "myVar"
          ),
          value = Identifier(
            token = Token(type = IDENT, literal = "anotherVar"),
            value = "anotherVar"
          )
        )
      )
    )

    assertEquals("let myVar = anotherVar;", program.toString())
  }
}
