package com.craftinginterpreters.lox

class Environment {
    var enclosing:Environment?
    private var values:MutableMap<String, Any?> = HashMap()

    constructor(){
        enclosing = null
    }

    constructor(enclosing:Environment?):this() {
        this.enclosing = enclosing
    }

    fun get(name:Token):Any? {
        if(values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if(enclosing != null) return enclosing?.get(name)

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name:Token, value:Any?){
        if(values.containsKey(name.lexeme)){
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing!!.assign(name, value);
            return;
        }

        throw RuntimeError(name,"Undefined variable '${name.lexeme}'.")
    }

    fun define(name:String, value:Any?) {
        values[name] = value
    }

    fun getAt(distance:Int, name:String):Any? {
        return ancestor(distance).values.get(name)
    }

    fun assignAT(distance: Int, name: Token, value: Any?){
        ancestor(distance).values.put(name.lexeme, value)
    }

    fun ancestor(distance: Int):Environment {
        var environment:Environment = this
        var i = 0
        while (i<distance){
            environment = environment.enclosing!!
            i++
        }
        return environment
    }
}