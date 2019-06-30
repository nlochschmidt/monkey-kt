import repl.REPL

fun main() {
  println("Welcome to the Monkey programming language!")
  println("Feel free to type in commands")
  REPL().start(::readLine, ::print)
}
