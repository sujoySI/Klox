package com.craftinginterpreters.lox

import com.craftinginterpreters.tool.GenerateAst

class AstPrinter:Expr.Visitor<String> {

    fun print(expr:Expr):String{
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return ""
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        if(expr.operator.type == TokenType.COMMA){
            print(expr.left)
            return print(expr.right)
        }

        return  parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        return " "
    }

    override fun visitGetExpr(expr: Expr.Get): String {
        return " "
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return  parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return ""
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        return " "
    }

    override fun visitSuperExpr(expr: Expr.Super): String {
        return " "
    }

    override fun visitThisExpr(expr: Expr.This): String {
        return " "
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return ""
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): String {
        val condition = print(expr.condition)

        return if(isTruthy(condition)) {
            print(expr.thenBranch)
        } else {
            print(expr.elseBranch)
        }
    }

    private fun parenthesize(name:String, vararg exprs:Expr):String{
        val builder:StringBuilder = StringBuilder()

        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false  // 'nil' is false
        if (value is Boolean) return value  // 'false' is false, 'true' is true
        return true  // Everything else is true
    }

//    fun main(args:Array<String>) {
//        val expression: Expr = Expr.Binary(
//            Expr.Unary(
//                Token(TokenType.MINUS, "-", null, 1),
//                Expr.Literal(123)
//            ),
//            Token(TokenType.STAR, "*", null, 1),
//            Expr.Grouping(
//                Expr.Literal(45.67)
//            )
//        )
//
//        println(AstPrinter().print(expression))
//    }
//
}
//
//fun main(args: Array<String>){
//    val objec = AstPrinter()
//    objec.main(args)
//}