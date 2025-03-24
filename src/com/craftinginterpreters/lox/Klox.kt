package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.system.exitProcess

class Klox {
    public lateinit var argsure:Array<String>
    companion object {
        private var interpreter:Interpreter = Interpreter()
        private var hadError: Boolean = false
        private var hadRuntimeError:Boolean = false

        @Throws
        fun main(args : Array<String>)
        {
//            Klox = args
            if (args.size > 1) {
                println("Usage: klox [script]")
                exitProcess(64)
            }
            else if (args.size == 1) {
                println("Running file ${args[0]}")
                runFile(args[0])
            }
            else {
                println("Asking user for command")
                runPrompt()
            }
        }

        @Throws
        private fun runFile(path: String) {
            val bytes : ByteArray = Files.readAllBytes(Paths.get((path)))
            run(String(bytes))  // Kotlin uses platform default charset automatically

            // Indicate an error in the exit code.
            if(hadError) { exitProcess(65) }
            if(hadRuntimeError) { exitProcess(70) }
        }

        @Throws
        private fun runPrompt() {
            val input = InputStreamReader(System.`in`)
            val reader = BufferedReader(input)
            var line: String?

            while(true){
                print("> ")
                line = reader.readLine()
                if(line == null) { break }
                run(line)
                hadError = false
            }
        }

        private fun run(source:String) {
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

            // Stop if there was a syntax error.
            if (hadError) return

            val resolver:Resolver = Resolver(interpreter)
            resolver.resolve(statements)

            interpreter.interpret(statements)
        }
        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        private fun report(line: Int, wheere: String, message: String) {
            System.err.println("Error$wheere[Line $line]:$message")

            hadError = true
        }

        fun error(token: Token, message: String ) {
            if(token.type == TokenType.EOF) {
                report(token.line, " at end", message)
            }
            else {
                report(token.line, " at ", message)
            }
        }

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            hadRuntimeError = true
        }
        private fun dumpTokens(tokens: List<Token>){
            for ((index, v) in tokens.withIndex()){
                println("${v.type}:${index}:${v.line}")
            }
        }
    }
}

fun main(args: Array<String>) {
    Klox.main(args)
}
