package lexer

import kotlin.test.Test
import kotlin.test.assertEquals

import lexer.Lexer
import token.Token
import token.TokenType.*


class LexerTest {

  @Test
  fun `nextToken on single character tokens`() {
    val input = "=+(){},;"
    val expectedTokens = listOf(
      Token(ASSIGN, "="),
      Token(PLUS, "+"),
      Token(LPAREN, "("),
      Token(RPAREN, ")"),
      Token(LBRACE, "{"),
      Token(RBRACE, "}"),
      Token(COMMA, ","),
      Token(SEMICOLON, ";"),
      Token(EOF, "")
    )

    val lexer = Lexer(input)

    expectedTokens.forEach {
      assertEquals(lexer.nextToken(), it)
    }
  }
}
