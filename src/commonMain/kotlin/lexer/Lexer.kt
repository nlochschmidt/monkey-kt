package lexer

import token.Token
import token.TokenType
import token.TokenType.*

class Lexer(private val input: String) {

  private var position: Int = 0
  private var readPosition: Int = 0
  private var currentChar: Char = NULL

  init {
    readChar()
  }

  fun nextToken(): Token {
    skipWhitespace()
    val token = when (currentChar) {
      '=' -> {
        if (peekChar() == '=') {
          val saveCurrentChar = currentChar
          readChar()
          Token(EQ, "$saveCurrentChar$currentChar")
        } else {
          Token(ASSIGN, "$currentChar")
        }
      }
      '+' -> Token(PLUS, "$currentChar")
      '-' -> Token(MINUS, "$currentChar")
      '!' -> {
        if (peekChar() == '=') {
          val saveCurrentChar = currentChar
          readChar()
          Token(NOT_EQ, "$saveCurrentChar$currentChar")
        } else {
          Token(BANG, "$currentChar")
        }
      }
      '*' -> Token(ASTERISK, "$currentChar")
      '/' -> Token(SLASH, "$currentChar")
      '<' -> Token(LT, "$currentChar")
      '>' -> Token(GT, "$currentChar")
      ';' -> Token(SEMICOLON, "$currentChar")
      '(' -> Token(LPAREN, "$currentChar")
      ')' -> Token(RPAREN, "$currentChar")
      ',' -> Token(COMMA, "$currentChar")
      '{' -> Token(LBRACE, "$currentChar")
      '}' -> Token(RBRACE, "$currentChar")
      NULL -> Token(EOF, "")
      else -> {
        when {
          currentChar.isLetter() -> {
            val identifier = readIdentifier()
            return lookupIdentifierToken(identifier)
          }
          currentChar.isDigit() -> return Token(INT, readNumber())
          else -> Token(ILLEGAL, "$currentChar")
        }
      }
    }
    readChar()
    return token
  }

  private fun readIdentifier(): String {
    val startPosition = position
    while (currentChar.isLetter() || currentChar == '_') {
      readChar()
    }
    return input.substring(startPosition, position)
  }

  private fun lookupIdentifierToken(identifier: String): Token {
    return Token(
      type = KEYWORDS.getOrElse(identifier) { IDENT },
      literal = identifier
    )
  }

  private fun readNumber(): String {
    val startPosition = position
    while (currentChar.isDigit()) {
      readChar()
    }
    return input.substring(startPosition, position)
  }

  private fun readChar() {
    if (readPosition >= input.length) {
      currentChar = NULL
    } else {
      currentChar = input[readPosition]
    }
    position = readPosition
    readPosition += 1
  }

  private fun peekChar(): Char {
    return if (readPosition >= input.length) {
      NULL
    } else {
      input[readPosition]
    }
  }

  private fun skipWhitespace() {
    while (currentChar.isWhitespace() && currentChar != NULL) {
      readChar()
    }
  }

  companion object {
    const val NULL: Char = '\u0000'

    val KEYWORDS: Map<String, TokenType> = mapOf(
      "fn" to FUNCTION,
      "let" to LET,
      "true" to TRUE,
      "false" to FALSE,
      "if" to IF,
      "else" to ELSE,
      "return" to RETURN
    )
  }
}
