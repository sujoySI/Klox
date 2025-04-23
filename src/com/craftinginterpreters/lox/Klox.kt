package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

import kotlin.system.exitProcess

private fun dumpTokens(tokens: List<Token>){
    for ((index, v) in tokens.withIndex()){
        println("${v.type}:${index}:${v.line}")
    }
}
@Throws
private fun runFile(path: String, interpreter: Interpreter) {
    val bytes : ByteArray = Files.readAllBytes(Paths.get((path)))
    run(String(bytes), interpreter)  // Kotlin uses platform default charset automatically
}
@Throws
private fun runPrompt(interpreter: Interpreter) {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    var line: String?

    while(true){
        print("> ")
        line = reader.readLine()
        if(line == null) { break }
        run(line, interpreter)
    }
}


private fun run(source:String, interpreter: Interpreter) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val path = "src/com/craftinginterpreters/lox/GeneratedTokens.txt"
    val writer:PrintWriter = PrintWriter(path, "UTF-8")
    for((i, token) in tokens.withIndex()) {
//                println("$i:(\t'${token.line}', '${token.lexeme}', '${token.type}'\t)")
        writer.println("$i:(\t'${token.line}', '${token.lexeme}', '${token.type}'\t)")
    }
    writer.close()
    val parser = Parser(tokens)
    val statements: MutableList<Stmt?> = parser.parse()

    val resolver:Resolver = Resolver(interpreter)
    resolver.resolve(statements)

    interpreter.interpret(statements)
}



fun main(args: Array<String>) {
    val interpreter:Interpreter = Interpreter()

    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    }
    else if (args.size == 1) {
        println("Running file ${args[0]}")
        runFile(args[0], interpreter)
    }
    else {
        println("Asking user for command")
        runPrompt(interpreter)
    }
}
