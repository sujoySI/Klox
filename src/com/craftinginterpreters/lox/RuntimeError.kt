package com.craftinginterpreters.lox

class RuntimeError: RuntimeException {
    var token:Token

    constructor(token:Token, message:String):super(message){
        this.token = token
    }
}