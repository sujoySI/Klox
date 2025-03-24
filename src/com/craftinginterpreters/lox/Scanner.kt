package com.craftinginterpreters.lox

import  com.craftinginterpreters.lox.TokenType.*

class Scanner(private var source: String) {

    companion object {
        private val tokens: MutableList<Token> = ArrayList()
        private val keywords:HashMap<String, TokenType> = hashMapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE)
    }

    private var start:Int = 0
    private var current:Int = 0
    private var line:Int = 1

    fun scanTokens():MutableList<Token> {
        while(!isAtEnd())
        {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when(val c:Char = advance())
        {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            '[' -> addToken(LEFT_BRACKET)
            ']' -> addToken(RIGHT_BRACKET)
            '?' -> addToken(QUESTION)
            ':' -> addToken(COLON)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            '*' -> addToken(STAR)
            ';' -> addToken(SEMICOLON)
            '!' -> {
                if (match('=')) {
                    addToken(BANG_EQUAL)
                } else addToken(BANG)
            }
            '=' -> {
                if (match('=')) {
                    addToken(EQUAL_EQUAL)
                } else addToken(EQUAL)
            }
            '<' -> {
                if (match('=')) {
                    addToken(LESS_EQUAL)
                } else addToken(LESS)
            }
            '>' -> {
                if (match('=')) {
                    addToken(GREATER_EQUAL)
                } else addToken(GREATER)
            }
            '/' -> {
                if (match('/')) {
                    // A comment goes till the end of the line
                    while((peek() != '\n') && (!isAtEnd())) advance()
                }
                else if (match('*')) {
                    /* A multiline comment goes till the '*' and '/' */
                    while (true) {
                        if((peek() == '*') && (peekNext() == '/')) break;
                        if(advance() == '\n') line++
                        if(peek() == 0.toChar()){
                            Klox.error(line, "Missing the end of multiline comment '*/'")
                        }
                    }
                    advance()
                    advance()
                }
                else addToken(SLASH)
            }
            ' ' -> {}
            '\r' -> {}
            '\t' -> {/*Ignore white space*/}
            '\n' -> {
                line++
            }
            '"' -> { string() }

            else -> {
                if(isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                }
                else {
                    Klox.error(line, "Unexpected character. $c")
                }
            }
        }
    }

    private fun identifier() {
        while(isAlphaNumeric(peek())) { advance() }

        val text:String = source.substring(start, current)
        var type: TokenType? = keywords[text]
        if(type == null) { type = IDENTIFIER }

        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) { advance() }

        //Look for a fractional part
        if(peek() == '.' && isDigit(peekNext())) {
            //Consume the '.'
            advance()

            while (isDigit(peek())) { advance() }
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string(){
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') { line++ }
            advance()
        }

        if (isAtEnd()) {
            Klox.error(line, "Unterminated string.")
            return
        }

        //The Closing ".
        advance()

        //Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun match(expected:Char):Boolean {
        if(isAtEnd()) return false
        if(source.elementAt(current) != expected) return false

        current++
        return true
    }

    private fun peek():Char {
        if(isAtEnd()) return 0.toChar() //Code for NUll Character or Unicode '\u0000'
        return source.elementAt(current)
    }

    private fun peekNext():Char {
        if(current + 1 >= source.length) return 0.toChar() //Code for NUll Character or Unicode '\u0000'
        return source.elementAt(current + 1)
    }

    private fun isAlpha(c: Char):Boolean {
        return (c in ('a'..'z')) || (c in ('A'..'Z')) || (c == '_')
    }

    private fun isAlphaNumeric(c:Char):Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun isDigit(c:Char):Boolean {
        return ((c >= '0') && (c <= '9'))
    }

    private fun isAtEnd():Boolean {
        return current >= source.length
    }

    private fun advance():Char {
        return source.elementAt(current++)
    }

    private fun addToken(type:TokenType) {
        addToken(type, null)
    }

    private fun addToken(type:TokenType, literal:Any?) {
        val text:String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}