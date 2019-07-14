package repl

import lexer.Lexer
import parser.Parser
import token.TokenType.EOF

class REPL {

  fun start(readLine: () -> String?, write: (String) -> Unit) {
    while (true) {
      write(PROMPT)
      val line = readLine().takeIf { it != "" } ?: return
      val lexer = Lexer(line)
      val parser = Parser(lexer)
      val program = parser.parseProgram()
      if (parser.errors.isNotEmpty()) {
        printParseErrors(write, parser.errors)
        continue
      }
      write(program.toString())
      write("\n")
    }
  }

  private fun printParseErrors(write: (String) -> Unit, errors: List<String>) {
    write(MONKEY_FACE)
    write("Woops! We ran into some monkey business here!\n")
    write(" parser errors:\n")
    errors.forEach { error ->
      write("\t$error\n")
    }
  }

  companion object {
    const val PROMPT = ">> "

    const val MONKEY_FACE = """            __,__
   .--.  .-"     "-.  .--.
  / .. \/  .-. .-.  \/ .. \
 | |  '|  /   Y   \  |'  | |
 | \   \  \ 0 | 0 /  /   / |
  \ '- ,\.-""${'"'}${'"'}${'"'}${'"'}${'"'}-./, -' /
   ''-' /_   ^ ^   _\ '-''
       |  \._   _./  |
       \   \ '~' /   /
        '._ '-=-' _.'
           '-----'
    """
  }
}
