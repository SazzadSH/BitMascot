package jlox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visit(Block stmt);
		R visit(Expression stmt);
		R visit(Var stmt);
		R visit(Function stmt);
		R visit(Method stmt);
		R visit(Class stmt);
		R visit(Break stmt);
		R visit(Continue stmt);
		R visit(Return stmt);
		R visit(If stmt);
		R visit(While stmt);
		R visit(For stmt);
	}
	static class Block extends Stmt {
		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final List<Stmt> statements;
	}
	static class Expression extends Stmt {
		Expression(Expr expression) {
			this.expression = expression;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Expr expression;
	}
	static class Var extends Stmt {
		Var(List<Token> names, List<Expr> initializers) {
			this.names = names;
			this.initializers = initializers;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final List<Token> names;
		final List<Expr> initializers;
	}
	static class Function extends Stmt {
		Function(Token name, List<Token> parameters, List<Stmt> body) {
			this.name = name;
			this.parameters = parameters;
			this.body = body;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Token name;
		final List<Token> parameters;
		final List<Stmt> body;
	}
	static class Method extends Stmt {
		Method(Stmt.Function function, Boolean isStatic) {
			this.function = function;
			this.isStatic = isStatic;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Stmt.Function function;
		final Boolean isStatic;
	}
	static class Class extends Stmt {
		Class(Token name, Expr.Variable superclass, List<Stmt.Method> methods) {
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Token name;
		final Expr.Variable superclass;
		final List<Stmt.Method> methods;
	}
	static class Break extends Stmt {
		Break(Token keyword) {
			this.keyword = keyword;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Token keyword;
	}
	static class Continue extends Stmt {
		Continue(Token keyword) {
			this.keyword = keyword;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Token keyword;
	}
	static class Return extends Stmt {
		Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Token keyword;
		final Expr value;
	}
	static class If extends Stmt {
		If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;
	}
	static class While extends Stmt {
		While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Expr condition;
		final Stmt body;
	}
	static class For extends Stmt {
		For(Stmt initializer, Expr condition, Expr increment, Stmt body) {
			this.initializer = initializer;
			this.condition = condition;
			this.increment = increment;
			this.body = body;
		}

		<R> R accept(Visitor<R> visitor) {
			return visitor.visit(this);
		}

		final Stmt initializer;
		final Expr condition;
		final Expr increment;
		final Stmt body;
	}

	abstract <R> R accept(Visitor<R> visitor);
}
