package com.craftinginterpreters.lox

import java.lang.classfile.Superclass

class KloxClass:KloxCallable {

    var name: String
    var superclass: KloxClass
    private var methods:MutableMap<String, KloxFunction>

    constructor(name: String, superclass: KloxClass, methods:MutableMap<String, KloxFunction>) {
        this.name = name
        this.superclass = superclass
        this.methods = methods
    }

    fun findMethod(name: String):KloxFunction? {
        if (methods.containsKey(name)) {
            return methods.get(name)
        }
        if (superclass != null) {
            return superclass.findMethod(name)
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