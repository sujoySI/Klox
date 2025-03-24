package com.craftinginterpreters.lox

sealed class Stmt {
	interface Visitor<R> {
		fun visitBlockStmt(stmt:Block):R
		fun visitClassStmt(stmt:Class):R
		fun visitExpressionStmt(stmt:Expression):R
		fun visitFunctionStmt(stmt:Function):R
		fun visitIfStmt(stmt:If):R
		fun visitPrintStmt(stmt:Print):R
		fun visitReturnStmt(stmt:Return):R
		fun visitVarStmt(stmt:Var):R
		fun visitWhileStmt(stmt:While):R
	}

	data class Block( var statements:MutableList<Stmt?> ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitBlockStmt(this)
		}
	}
	data class Class( var name:Token, var methods:MutableList<Stmt.Function?> ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitClassStmt(this)
		}
	}
	data class Expression( var expression:Expr ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitExpressionStmt(this)
		}
	}
	data class Function( var name:Token, var params:MutableList<Token>, var body:MutableList<Stmt?> ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitFunctionStmt(this)
		}
	}
	data class If( var condition:Expr, var thenBranch:Stmt?, var elseBranch:Stmt? ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitIfStmt(this)
		}
	}
	data class Print( var expression:Expr ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitPrintStmt(this)
		}
	}
	data class Return( var keyword:Token, var value:Expr? ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitReturnStmt(this)
		}
	}
	data class Var( var name:Token, var initializer:Expr? ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitVarStmt(this)
		}
	}
	data class While( var condition:Expr, var body:Stmt? ):Stmt() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitWhileStmt(this)
		}
	}

	abstract fun <R> accept(visitor:Visitor<R>):R
}
