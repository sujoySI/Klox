package com.craftinginterpreters.lox

class KloxInstance {
    private var klass:KloxClass
    private val fields:MutableMap<String, Any?> = HashMap()

    constructor(klass:KloxClass) {
        this.klass = klass
    }

    fun get(name:Token):Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }
        val method:KloxFunction? = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)
        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value:Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}