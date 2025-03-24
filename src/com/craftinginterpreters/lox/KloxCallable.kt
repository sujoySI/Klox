package com.craftinginterpreters.lox

interface KloxCallable {
    fun arity():Int
    fun call(interpreter: Interpreter?, arguments: MutableList<Any?>): Any?
}