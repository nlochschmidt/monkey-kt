package repl

import lexer.Lexer
import token.TokenType.EOF

class REPL {

  fun start(readLine: () -> String?, write: (String) -> Unit) {
    while (true) {
      write(PROMPT)
      val line = readLine().takeIf { it != ""} ?: return
      val lexer = Lexer(line)
      var token = lexer.nextToken()
      while (token.type != EOF) {
        write(token.toString())
        write("\n")
        token = lexer.nextToken()
      }
    }
  }

  companion object {
    const val PROMPT = ">> "
  }
}
