package com.craftinginterpreters.lox

class KloxFunction :KloxCallable {
    private var declaration: Stmt.Function
    private var isInitializer:Boolean
    private var closure: Environment

    constructor(declaration: Stmt.Function, closure: Environment, isInitializer:Boolean) {
        this.isInitializer = isInitializer
        this.closure = closure
        this.declaration = declaration
    }

    fun bind(instance:KloxInstance):KloxFunction{
        val environment:Environment = Environment(closure)
        environment.define("this", instance)
        return KloxFunction(declaration, environment, isInitializer)
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
        if(isInitializer) return closure.getAt(0, "this")
        return null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}