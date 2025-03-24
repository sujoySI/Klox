package com.craftinginterpreters.lox


class Resolver: Expr.Visitor<Unit?> , Stmt.Visitor<Unit?> {
    private val interpreter:Interpreter
    // Basically Stack of map from string to boolean so instead of using push(), pop() or peek() use addLast(), removeLastOrNull() or last()
    private val scopes: ArrayDeque<MutableMap<String, Boolean>> = ArrayDeque()

    constructor(interpreter: Interpreter) {
        this.interpreter = interpreter
    }

    fun resolve(statements:MutableList<Stmt?>) {
        for(statement in statements){
            if (statement != null) {
                resolve(statement)
            }
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Unit? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Unit? {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Unit? {
        resolve(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Unit? {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Unit? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if(stmt.elseBranch != null ) resolve(stmt.elseBranch)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Unit? {
        resolve(stmt.expression)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Unit? {
        if (stmt.value != null) resolve(stmt.value)
        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Unit? {
        declare(stmt.name)
        if(stmt.initializer != null){
            resolve(stmt.initializer!!)
        }
        define(stmt.name)
        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Unit? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitAssignExpr(expr: Expr.Assign): Unit? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Unit? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Unit? {
        resolve(expr.callee)
        for(argument:Expr in expr.arguments) {
            resolve(argument)
        }
        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Unit? {
        resolve(expr.expression)
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Unit? {
        return null
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Unit? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Unit? {
        resolve(expr.right)
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Unit? {
        if ((!scopes.isEmpty()) && (scopes.last().get(expr.name.lexeme) == "false".toBoolean())) {
            Klox.error(expr.name, "Can't read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Unit? {
        return null
    }

    private fun resolve(stmt:Stmt?) {
        stmt?.accept(this)
    }

    private fun resolve(expr: Expr?) {
        expr?.accept(this)
    }

    private fun resolveFunction(function: Stmt.Function) {
        beginScope()
        for(param:Token in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
    }

    private fun beginScope() {
        scopes.addLast(HashMap<String, Boolean>())
    }

    private fun endScope() {
        scopes.removeLastOrNull()
    }

    private fun declare(name:Token) {
        if(scopes.isEmpty()) return

        var scope:MutableMap<String, Boolean> = scopes.last()
        scope.put(name.lexeme, false)
    }

    private fun define(name: Token) {
        if(scopes.isEmpty()) return

        scopes.last().put(name.lexeme, true)
    }

    private fun resolveLocal(expr:Expr, name:Token){
        var i:Int = scopes.size - 1
        while(i >= 0) {
            if(scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
            i--
        }
    }
}