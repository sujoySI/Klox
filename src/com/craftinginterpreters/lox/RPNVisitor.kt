package com.craftinginterpreters.lox

class RPNVisitor: Expr.Visitor<String> {
    private fun print(expr:Expr): String {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return ""
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return "$left $right ${expr.operator.lexeme}"
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        return " "
    }

    override fun visitGetExpr(expr: Expr.Get): String {
        return " "
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return expr.expression.accept(this)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return ""
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        return " "
    }

    override fun visitThisExpr(expr: Expr.This): String {
        return " "
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        val right = expr.right.accept( this)
        return "$right ${expr.operator.lexeme}"
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return ""
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): String {
        return "${expr.condition} ${expr.thenBranch} ${expr.elseBranch}"
    }

//    public fun main(args:Array<String>) {
//        val rpnVisitor = RPNVisitor()
//        val expression = Expr.Binary(
//            Expr.Grouping(
//                Expr.Binary(
//                    Expr.Literal(1),
//                    Token(TokenType.PLUS, "+", null, 1),
//                    Expr.Literal(2)
//                )
//            ),
//            Token(TokenType.STAR,"*", null, 1),
//            Expr.Grouping(
//                Expr.Binary(
//                    Expr.Literal(3),
//                    Token(TokenType.MINUS, "-", null, 1),
//                    Expr.Literal(4)
//                )
//            )
//        )
//        println(rpnVisitor.print(expression))
//    }
}
//
//fun main(args: Array<String>){
//    val objec = RPNVisitor()
//    objec.main(args)
//}