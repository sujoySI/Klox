package com.craftinginterpreters.lox

sealed class Expr {
	interface Visitor<R> {
		fun visitAssignExpr(expr:Assign):R
		fun visitBinaryExpr(expr:Binary):R
		fun visitCallExpr(expr:Call):R
		fun visitGroupingExpr(expr:Grouping):R
		fun visitLiteralExpr(expr:Literal):R
		fun visitLogicalExpr(expr:Logical):R
		fun visitUnaryExpr(expr:Unary):R
		fun visitVariableExpr(expr:Variable):R
		fun visitTernaryExpr(expr:Ternary):R
	}

	data class Assign( var name:Token, var value:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitAssignExpr(this)
		}
	}
	data class Binary( var left:Expr, var operator:Token, var right:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitBinaryExpr(this)
		}
	}
	data class Call( var callee:Expr, var paren:Token, var arguments:MutableList<Expr> ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitCallExpr(this)
		}
	}
	data class Grouping( var expression:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitGroupingExpr(this)
		}
	}
	data class Literal( var value:Any? ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitLiteralExpr(this)
		}
	}
	data class Logical( var left:Expr, var operator:Token, var right:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitLogicalExpr(this)
		}
	}
	data class Unary( var operator:Token, var right:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitUnaryExpr(this)
		}
	}
	data class Variable( var name:Token ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitVariableExpr(this)
		}
	}
	data class Ternary( var condition:Expr, var thenBranch:Expr, var elseBranch:Expr ):Expr() {
		override fun <R> accept(visitor:Visitor<R>):R{
			return visitor.visitTernaryExpr(this)
		}
	}

	abstract fun <R> accept(visitor:Visitor<R>):R
}
