package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import kotlin.system.exitProcess

class Interpreter: Expr.Visitor<Any?>, Stmt.Visitor<Unit?> {

    val globals:Environment = Environment()
    private var environment:Environment = globals
    private var locals:MutableMap<Expr, Int> = HashMap()

    constructor() {
        globals.define("clock", object: KloxCallable{
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter?, arguments: MutableList<Any?>): Any {
                return System.currentTimeMillis()/1000.0
            }

            override fun toString(): String {
                return "<native fn>"
            }
        })
    }

    private fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        exitProcess(84)
    }

    fun interpret(statements:MutableList<Stmt?>) {
        try {
            for (statement in statements){
                if (statement != null) {
                    execute(statement)
                }
            }
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    //Expr Visitors

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value:Any? = evaluate(expr.value)
        val distance:Int? = locals.get(expr)
        if(distance!= null) {
            environment.assignAT(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left:Any? = evaluate(expr.left)
        val right:Any? = evaluate(expr.right)

        when(expr.operator.type){
            BANG_EQUAL -> return !isEqual(left, right)
            EQUAL_EQUAL -> return isEqual(left, right)
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) > (right as Double)
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) >= (right as Double)
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) <= (right as Double)
            }
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) - (right as Double)
            }
            PLUS -> {
                if(left is Double && right is Double){
                    return (left as Double) + (right as Double)
                }
                if(left is String && right is String){
                    return (left as String) + (right as String)
                }
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) / (right as Double)
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) * (right as Double)
            }
            else -> {}
        }

        //Unreachable
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee:Any? = evaluate(expr.callee)

        val arguments:MutableList<Any?> = ArrayList()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is KloxCallable){
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        val function:KloxCallable = callee as KloxCallable
        if(arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val objec:Any? = evaluate(expr.objec)
        if (objec is KloxInstance)
        {
            return objec.get(expr.name)
        }
        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left:Any? =evaluate(expr.left)

        if ( expr.operator.type == OR ) {
            if (isTruthy(left)) return left
        }
        else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val objec:Any? = evaluate(expr.objec)

        if (objec !is KloxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value:Any? = evaluate(expr.value)
        (objec as KloxInstance).set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance: Int? = locals.get(expr)
        val superclass: KloxClass = environment.getAt(distance, "super") as KloxClass
        val objec: KloxInstance = environment.getAt(distance?.minus(1), "this") as KloxInstance
        val method: KloxFunction? = superclass.findMethod(expr.method.lexeme)

        if (method == null) {
            throw RuntimeError(expr.method, "Undefined property ${expr.method.lexeme}.")
        }

        return method.bind(objec)
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right:Any? = evaluate(expr.right)
        when(expr.operator.type)
        {
            BANG -> return !isTruthy(right)
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
            else -> {}
        }

        //Unreachable
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        return null
    }

    private fun lookUpVariable(name:Token, expr: Expr):Any?  {
        val distance: Int? = locals.get(expr)
        if(distance != null)
        {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }

    private fun checkNumberOperand(operator:Token, operand:Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left:Any?, right:Any?) {
        if(left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isEqual(a:Any?, b:Any?):Boolean {
        if(a == null && b == null) return true
        if(a == null) return false

        return a.equals(b)
    }

    private fun isTruthy(objec:Any?):Boolean{
        if(objec == null) return false
        if(objec is Boolean) return (objec as Boolean)
        return true
    }

    private fun stringify(objec: Any?): String {
        if (objec == null) return "nil"

        if (objec is Double) {
            var text = objec.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return objec.toString()
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt:Stmt?){
        stmt?.accept(this)
    }

    fun resolve(expr:Expr, depth:Int){
        locals.put(expr,depth)
    }

    fun executeBlock(statements: MutableList<Stmt?>, environment: Environment){
        val previous:Environment = this.environment
        try {
            this.environment = environment

            for(statement in statements){
                if (statement != null) {
                    execute(statement)
                }
            }
        }
        finally {
            this.environment = previous
        }
    }

    //Stmt Visitors

    override fun visitBlockStmt(stmt: Stmt.Block): Unit? {
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Unit? {
        var superclass: Any? = null
        if(stmt.superclass != null) {
            superclass = evaluate(stmt.superclass!!)
            if(superclass !is KloxClass) {
                throw RuntimeError(stmt.superclass!!.name, "Superclass must be a class.")
            }
        }
        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods:MutableMap<String, KloxFunction> = HashMap()
        for (method in stmt.methods) {
            val function:KloxFunction = KloxFunction(method!!, environment, method.name.lexeme.equals("init"))
            methods.put(method.name.lexeme, function)
        }
        val klass:KloxClass = KloxClass(stmt.name.lexeme, superclass as KloxClass, methods)

        if  (superclass != null) {
            environment = environment.enclosing!!
        }

        environment.assign(stmt.name, klass)
        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Unit? {
        evaluate(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Unit? {
        val function:KloxFunction = KloxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Unit? {
        if(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null){
            execute(stmt.elseBranch)
        }
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Unit? {
        val value:Any? = evaluate(stmt.expression)
        println(stringify(value))
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Unit? {
        var value:Any? = null
        if(stmt.value != null) value = evaluate(stmt.value!!)

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var): Unit? {
        var value:Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer!!)
        }
        environment.define(stmt.name.lexeme, value)
        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Unit? {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
        return null
    }
}