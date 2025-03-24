package com.craftinginterpreters.lox

class KloxFunction :KloxCallable {
    private var declaration: Stmt.Function
    private var closure: Environment

    constructor(declaration: Stmt.Function, closure: Environment) {
        this.closure = closure
        this.declaration = declaration
    }

    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter?, arguments: MutableList<Any?>): Any? {
        val environment:Environment = Environment(closure)
        var i = 0
        while(i < declaration.params.size) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i))
            i++
        }
        i = 0
        try {
            interpreter?.executeBlock(declaration.body, environment)
        } catch (returnValue:Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}