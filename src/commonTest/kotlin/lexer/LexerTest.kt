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
      assertEquals(it, lexer.nextToken())
    }
  }

  @Test
  fun `nextToken on simplified monkey program`() {
    val input = """
      let five = 5;
      let ten = 10;

      let add = fn(x, y) {
        x + y;
      };

      let result = add(five, ten);
      """.trimIndent()

    val expectedTokens = listOf(
      Token(LET, "let"),
      Token(IDENT, "five"),
      Token(ASSIGN, "="),
      Token(INT, "5"),
      Token(SEMICOLON, ";"),
      Token(LET, "let"),
      Token(IDENT, "ten"),
      Token(ASSIGN, "="),
      Token(INT, "10"),
      Token(SEMICOLON, ";"),
      Token(LET, "let"),
      Token(IDENT, "add"),
      Token(ASSIGN, "="),
      Token(FUNCTION, "fn"),
      Token(LPAREN, "("),
      Token(IDENT, "x"),
      Token(COMMA, ","),
      Token(IDENT, "y"),
      Token(RPAREN, ")"),
      Token(LBRACE, "{"),
      Token(IDENT, "x"),
      Token(PLUS, "+"),
      Token(IDENT, "y"),
      Token(SEMICOLON, ";"),
      Token(RBRACE, "}"),
      Token(SEMICOLON, ";"),
      Token(LET, "let"),
      Token(IDENT, "result"),
      Token(ASSIGN, "="),
      Token(IDENT, "add"),
      Token(LPAREN, "("),
      Token(IDENT, "five"),
      Token(COMMA, ","),
      Token(IDENT, "ten"),
      Token(RPAREN, ")"),
      Token(SEMICOLON, ";"),
      Token(EOF, "")
    )

    val lexer = Lexer(input)

    expectedTokens.forEach {
      assertEquals(it, lexer.nextToken())
    }
  }

    @Test
  fun `nextToken support for more single character operators`() {
    val input = """
      !-/*5;
      5 < 10 > 5;
      """.trimIndent()

    val expectedTokens = listOf(
      Token(BANG, "!"),
      Token(MINUS, "-"),
      Token(SLASH, "/"),
      Token(ASTERISK, "*"),
      Token(INT, "5"),
      Token(SEMICOLON, ";"),
      Token(INT, "5"),
      Token(LT, "<"),
      Token(INT, "10"),
      Token(GT, ">"),
      Token(INT, "5"),
      Token(SEMICOLON, ";"),
      Token(EOF, "")
    )

    val lexer = Lexer(input)

    expectedTokens.forEach {
      assertEquals(it, lexer.nextToken())
    }
  }
}
