package com.craftinginterpreters.lox

class KloxClass:KloxCallable {

    var name: String
    private var methods:MutableMap<String, KloxFunction>

    constructor(name: String, methods:MutableMap<String, KloxFunction>) {
        this.name = name
        this.methods = methods
    }

    fun findMethod(name: String):KloxFunction? {
        if (methods.containsKey(name)) {
            return methods.get(name)
        }
        return null
    }

    override fun toString(): String {
        return name
    }

    override fun arity(): Int {
        val initializer:KloxFunction? = findMethod("init")
        if (initializer == null) return 0
        return initializer.arity()
    }

    override fun call(interpreter: Interpreter?, arguments: MutableList<Any?>): Any? {
        val instance:KloxInstance = KloxInstance(this)
        val initializer:KloxFunction? = findMethod("init")
        if(initializer != null) {
            initializer.bind(instance).call(interpreter, arguments)
        }
        return instance
    }
}