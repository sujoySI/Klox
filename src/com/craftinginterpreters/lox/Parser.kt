package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import kotlin.system.exitProcess

class Parser {
    companion object{
        private class ParseError:RuntimeException()
    }

    private var tokens:MutableList<Token>
    private var current:Int = 0

    constructor(tokens:MutableList<Token>) {
        this.tokens = tokens
    }

    fun parse(): MutableList<Stmt?> {
       val statements:MutableList<Stmt?> = ArrayList()
       while (!isAtEnd()) {
            statements.add(declaration())
       }
       return statements
    }

    private fun expression():Expr {
        println("expression")
        return assignment()
//        return comma()
    }

    private fun declaration(): Stmt? {
        try {
            if (match(CLASS)) return classDeclaration()
            if (match(FUN)) return function("function")
            if (match(VAR)) return varDeclaration()

            return statement()
        }
        catch (error:ParseError) {
            synchronize()
            return null
        }
    }

    private fun classDeclaration():Stmt {
        val name:Token = consume(IDENTIFIER, "Expect Class name.")
        var superclass:Expr.Variable? = null
        if(match(LESS)){
            consume(IDENTIFIER, "Expect superclass name")
            superclass = Expr.Variable(previous())
        }
        consume(LEFT_BRACE, "Expect '{' before class body.")

        val methods:MutableList<Stmt.Function?> = ArrayList()
        while((!check(RIGHT_BRACE)) && (!isAtEnd())) {
            methods.add(function("method"))
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.")
        return Stmt.Class(name, superclass, methods)
    }

    private fun statement():Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(RETURN)) return returnStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun forStatement():Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        var initializer:Stmt?
        if (match(SEMICOLON)) {
            initializer = null
        }
        else if (match(VAR)) {
            initializer = varDeclaration()
        }
        else {
            initializer = expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment:Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expect '}' after for clauses.")
        var body:Stmt = statement()

        if (increment != null) {
            body = Stmt.Block(mutableListOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(mutableListOf(initializer, body));
        }

        return body
    }

    private fun ifStatement():Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition:Expr = expression()
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.")

        var thenBranch:Stmt? = statement()
        var elseBranch:Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement():Stmt {
        val value:Expr = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement():Stmt {
        val keyword:Token = previous()
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = expression()
        }

        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun varDeclaration():Stmt {
        val name:Token = consume(IDENTIFIER, "Expect variable name.")

        var initializer:Expr? = null
        if(match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun whileStatement():Stmt{
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition:Expr = expression()
        consume(RIGHT_PAREN,"Expect ')' after condition")
        val body:Stmt = statement()

        return Stmt.While(condition, body)
    }

    private fun expressionStatement():Stmt {
        val expr:Expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun function(kind:String):Stmt.Function {
        val name:Token = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters:MutableList<Token> = ArrayList()
        if(!check(RIGHT_PAREN)){
            do {
                if(parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body: MutableList<Stmt?> = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun block(): MutableList<Stmt?> {
        val statements:MutableList<Stmt?> = ArrayList()

        while (!check(RIGHT_BRACE) && !isAtEnd())
        {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

//    /*  Comma and Ternary do nothing right now so might implement it later  */
//    private fun comma():Expr{
//        println("comma")
//        var expr:Expr = assignment()
//        //println("comma")
//
//        while(match(COMMA)) {
//            val operator:Token = previous()
//            val right:Expr = ternary()
//            expr = Expr.Binary(expr, operator,right)
//        }
//
//        return expr
//    }

    private fun assignment():Expr{
        println("assignment")
        val expr:Expr = ternary()

        if(match(EQUAL)) {
            val equals:Token = previous()
            val value:Expr = assignment()

            if(expr is Expr.Variable){
                val name:Token = expr.name
                return Expr.Assign(name, value)
            } else if (expr is Expr.Get) {
                val get:Expr.Get = expr
                return Expr.Set(get.objec, get.name, value)
            }

            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    /*  Comma and Ternary do nothing right now so might implement it later  */
    private fun ternary():Expr {
        println("ternary")
        var expr:Expr = or()
        //println("ternary")

        if(match(QUESTION)){ // If '?' is found, it is ternary operator
            val thenExpr:Expr = expression()// Parse 'then' expression
            consume(COLON, "Expect ':' after the expression")
            val elseExpr:Expr = ternary() // Recursively parse 'else' expression
            expr = Expr.Ternary(expr, thenExpr, elseExpr)
        }

        return expr
    }

    private fun or():Expr {
        println("or")
        var expr = and()

        while ( match(OR) ) {
            val operator:Token = previous()
            val right:Expr = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and():Expr {
        println("and")
        var expr = equality()

        while ( match(AND) ) {
            val operator:Token = previous()
            val right:Expr = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun equality():Expr {
        println("equality")
        var expr:Expr = comparison()
        //println("equality")

        while(match(BANG_EQUAL,EQUAL_EQUAL)){
            val operator:Token = previous()
            val right:Expr = comparison()
            expr = Expr.Binary(expr,operator, right)
        }

        return expr
    }

    private fun comparison():Expr {
        println("comparison")
        var expr:Expr = term()
        //println("comparison")

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator:Token = previous()
            val right:Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term():Expr {
        println("term")
        var expr:Expr = factor()
        //println("term")

        while (match(MINUS, PLUS)) {
            val operator:Token = previous()
            val right:Expr = factor()
            expr = Expr.Binary(expr,operator,right)
        }

        return expr
    }

    private fun factor(): Expr {
        println("factor")
        var expr: Expr = unary()
        //println("factor")

        while (match(SLASH, STAR)) {
            val operator:Token = previous()
            val right:Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        println("unary")
        if (match(BANG, MINUS)) {
            val operator:Token = previous()
            val right:Expr = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun finishCall(callee:Expr):Expr {
        val arguments:MutableList<Expr> = ArrayList()
        if(!check(RIGHT_PAREN)) {
            do{
                if(arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren:Token = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

     private fun call(): Expr{
        var expr:Expr = primary()

        while(true){
            if(match(LEFT_PAREN)){
                expr = finishCall(expr)
            } else if (match(DOT)) {
                val name:Token = consume(IDENTIFIER, "Expect property name after '.'.")
                expr =Expr.Get(expr, name)
            } else break
        }

        return expr
    }

    private fun primary(): Expr {
        println("primary")
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(SUPER)) {
            var keyword:Token = previous()
            consume(DOT, "Expect '.' after 'super'.")
            var method = consume(IDENTIFIER, "Expect superclass method name.")
            return Expr.Super(keyword, method)
        }
        if (match(THIS)) return Expr.This(previous())
        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }
        if (match(LEFT_PAREN)) {
            val expr:Expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expecting expression.")
    }

    private fun match(vararg types:TokenType):Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type:TokenType, message:String):Token {
        if(check(type)) return advance()
        throw error(peek(),message)
    }

    private fun check(type:TokenType):Boolean {
        if(isAtEnd()) return false
        return peek().type == type
    }

    private fun advance():Token {
        if(!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd():Boolean {
        return peek().type == EOF
    }

    private fun peek():Token {
        return tokens[current]
    }

    private fun previous():Token {
        return tokens[current - 1]
    }

    private fun report(line: Int, wheere: String, message: String) {
        System.err.println("Parser: Error$wheere[Line $line]:$message")
        exitProcess(78)
    }


    private fun error(token:Token, message: String):ParseError {
        if(token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        }
        else {
            report(token.line, " at ", message)
        }
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS -> {}
                FUN -> {}
                VAR -> {}
                FOR -> {}
                IF -> {}
                WHILE -> {}
                PRINT -> {}
                RETURN -> {return}
                else -> {}
            }

            advance()
        }
    }
}