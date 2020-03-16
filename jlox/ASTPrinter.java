package jlox;

import java.util.List;

class ASTPrinter implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private int depth = 0;

	void print(List<Stmt> stmts) {
		stmts.get(0).accept(this);
		for (int i = 1; i < stmts.size(); ++i) {
			System.out.println("");
			print(stmts.get(i));
		}
	}
	
	void print(Stmt stmt) {
		stmt.accept(this);
	}

	void print(Expr expr) {
		expr.accept(this);
	}

	void printFunction(Token name, List<Token> parameters, List<Stmt> body) {
		System.out.print("(fn " + (name == null ? "" : name.lexeme) + " (");
		if (parameters.size() > 0)
			System.out.print(parameters.get(0).lexeme);
		for (int i = 1; i < parameters.size(); ++i)
			System.out.print(" " + parameters.get(i).lexeme);
		System.out.print(") (\n");
		depth += 1;
		print(body);
		System.out.print(")");
		depth -= 1;
	}

	private void addDepth() {
		for (int i = 0; i < depth; ++i)
			System.out.print("\t");
	}

	@Override
	public Void visit(Expr.Assign expr) {
		parenthesize("= " + expr.name.lexeme, expr.value);
		return null;
	}

	@Override
	public Void visit(Expr.Ternary expr) {
		parenthesize(expr.operator.lexeme, expr.condition, expr.left, expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Binary expr) {
		parenthesize(expr.operator.lexeme, expr.left, expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Logical expr) {
		parenthesize(expr.operator.lexeme, expr.left, expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Unary expr) {
		parenthesize(expr.operator.lexeme, expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Grouping expr) {
		parenthesize("group", expr.expression);
		return null;
	}

	@Override
	public Void visit(Expr.Literal expr) {
		if (expr.value == null)
			System.out.print("nil");
		else
			System.out.print("\"" + expr.value.toString() + "\"");
		return null;
	}

	@Override
	public Void visit(Expr.Call expr) {
		System.out.print("(call ");
		print(expr.callee);
		for (Expr arg : expr.arguments) {
			System.out.print(" ");
			print(arg);
		}
		System.out.print(")");
		return null;
	}

	@Override
	public Void visit(Expr.Function expr) {
		printFunction(null, expr.parameters, expr.body);
		return null;
	}

	@Override
	public Void visit(Expr.Variable expr) {
		System.out.print(expr.name.lexeme);
		return null;
	}

	@Override
	public Void visit(Stmt.Block stmt) {
		addDepth();
		System.out.print("(block\n");
		depth += 1;
		print(stmt.statements);
		depth -= 1;
		addDepth();
		System.out.print(")");
		return null;
	}

	@Override
	public Void visit(Stmt.Expression stmt) {
		addDepth();
		print(stmt.expression);
		return null;
	}

	@Override
	public Void visit(Stmt.Function stmt) {
		addDepth();
		printFunction(stmt.name, stmt.parameters, stmt.body);
		return null;
	}

	@Override
	public Void visit(Stmt.Var stmt) {
		int len = stmt.names.size();
		for (int i = 0; i < len; ++i) {
			addDepth();
			System.out.print("(var " + stmt.names.get(i).lexeme);
			Expr initializer = stmt.initializers.get(i);
			if (initializer != null) {
				System.out.print(" ");
				print(initializer);
			}
			System.out.print(")");
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Class stmt) {
		addDepth();
		System.out.print("(cls " + stmt.name.lexeme + " (");
		depth += 1;
		for (Stmt.Function method : stmt.methods) {
			print(method);
		}
		depth -= 1;
		System.out.print(")");
		return null;
	}

	@Override
	public Void visit(Stmt.Break stmt) {
		addDepth();
		System.out.print("(" + stmt.keyword.lexeme + ")");
		return null;
	}

	@Override
	public Void visit(Stmt.Return stmt) {
		addDepth();
		System.out.print("(" + stmt.keyword.lexeme + " ");
		print(stmt.value);
		return null;
	}

	@Override
	public Void visit(Stmt.If stmt) {
		addDepth();
		System.out.print("(if ");
		print(stmt.condition);
		System.out.print(" (\n");
		depth += 1;
		print(stmt.thenBranch);
		depth -= 1;
		addDepth();
		System.out.print(")");
		if (stmt.elseBranch != null) {
			System.out.print(" (\n");
			depth += 1;
			print(stmt.elseBranch);
			depth -= 1;
			addDepth();
		}
		depth -= 1;
		return null;
	}

	@Override
	public Void visit(Stmt.While stmt) {
		addDepth();
		System.out.print("(while ");
		print(stmt.condition);
		System.out.print(" (\n");
		depth += 1;
		print(stmt.body);
		depth -= 1;
		addDepth();
		System.out.print(")");
		return null;
	}

	private void parenthesize(String name, Expr... exprs) {
		System.out.print("(" + name);
		for (Expr expr : exprs) {
			System.out.print(" ");
			print(expr);
		}
		System.out.print(")");
	}
}