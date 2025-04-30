package com.craftinginterpreters.tool

import java.io.PrintWriter
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.system.exitProcess

class GenerateAst {
    @Throws
    public fun main(args: Array<String>) {
        if( args.size != 1) {
            error("Usage: generate_ast <output directory>")
            @Suppress("UNREACHABLE_CODE")
            exitProcess(64)
        }
        val path = Path(args[0])
        println(path)
        val outputDir:String = path.absolute().parent.toString()
        defineAst(outputDir, "Expr", mutableListOf(
            /*  Splitting happens in "#",   "* ",   " " and ":" */
            "Assign    # var name:Token,* var value:Expr ",
            "Binary    # var left:Expr,* var operator:Token,* var right:Expr ",
            "Call      # var callee:Expr,* var paren:Token,* var arguments:MutableList<Expr> ",
            "Get       # var objec:Expr,* var name:Token ",
            "Grouping  # var expression:Expr ",
            "Literal   # var value:Any? ",
            "Logical   # var left:Expr,* var operator:Token,* var right:Expr ",
            "Set       # var objec:Expr,* var name:Token,* var value:Expr ",
            "Super     # var keyword:Token,* var method:Token ",
            "This      # var keyword:Token ",
            "Unary     # var operator:Token,* var right:Expr ",
            "Variable  # var name:Token ",
            "Ternary   # var condition:Expr,* var thenBranch:Expr,* var elseBranch:Expr "
        ))
        defineAst(outputDir, "Stmt", mutableListOf(
            /*  Splitting happens in "#",   "* ",   " " and ":" */
            "Block      # var statements:MutableList<Stmt?> ",
            "Class      # var name:Token,* var superclass:Expr.Variable?,* var methods:MutableList<Stmt.Function?> ",
            "Expression # var expression:Expr ",
            "Function   # var name:Token,* var params:MutableList<Token>,* var body:MutableList<Stmt?> ",
            "If         # var condition:Expr,* var thenBranch:Stmt?,* var elseBranch:Stmt? ",
            "Print      # var expression:Expr ",
            "Return     # var keyword:Token,* var value:Expr? ",
            "Var        # var name:Token,* var initializer:Expr? ",
            "While      # var condition:Expr,* var body:Stmt? "
        ))

    }

    @Throws
    private fun defineAst(outputDir:String, baseName:String, types:MutableList<String>) {
        val path:String = "$outputDir/$baseName.kt"
        val writer:PrintWriter = PrintWriter(path, "UTF-8") // java.io

        writer.println("package com.craftinginterpreters.lox")
        writer.println()
        writer.println("sealed class $baseName {")

        //Visitor Functions and Interface declaration
        defineVisitor(writer, baseName, types)

        //Ast classes
        for(type:String in types){
            val className:String = type.split("#")[0].trim()
            val fields:String = type.split("#")[1].trim()
            defineType(writer, baseName, className, fields)
        }

        //The base accept() function
        writer.println()
        writer.println("\tabstract fun <R> accept(visitor:Visitor<R>):R")
        writer.println("}")
        writer.close()
    }

    private fun defineVisitor(writer:PrintWriter, baseName: String, types:MutableList<String>) {
        writer.println("\tinterface Visitor<R> {")
        for (type:String in types ){
            val typeName:String =  type.split("#")[0].trim()
            writer.println("\t\tfun visit$typeName$baseName(${baseName.lowercase()}:$typeName):R")
        }
        writer.println("\t}")
        writer.println()
    }

    @Throws
    private fun defineType(writer: PrintWriter, baseName:String, className: String, fieldList:String) {
        //Store parameters in fields also split in "* "
        val fields:List<String> = fieldList.split("* ")

        //Primary constructor. Class, Variable Creation and initiation
        writer.print("\tdata class $className(")
        for (field in fields) {
            val nameType:String = field.split(" ")[1]
            writer.print(" var $nameType")
        }
        writer.print(" ):$baseName() {\n")

        //Visitor Pattern
        writer.println("\t\toverride fun <R> accept(visitor:Visitor<R>):R{")
        writer.println("\t\t\treturn visitor.visit$className$baseName(this)")
        writer.println("\t\t}")
        writer.println("\t}")
    }
}

fun main(args: Array<String>){
    val objec = GenerateAst()
    objec.main(args)
}