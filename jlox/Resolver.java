package jlox;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private enum FunctionType {
		NONE, FUNCTION, METHOD, STATIC_METHOD, INITIALIZER
	}

	private enum ClassType {
		NONE, CLASS, SUBCLASS
	}

	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;
	private ClassType currentClass = ClassType.NONE;
	private boolean isInLoop = false;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	void resolve(List<Stmt> stmts) {
		for (Stmt stmt : stmts) {
			resolve(stmt);
		}
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}

	public void resolve(Expr expr) {
		expr.accept(this);
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; --i) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}
	}

	private void resolveFunction(
		  List<Token> parameters, List<Stmt> body, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;
		beginScope();
		for (Token parameter : parameters) {
			declare(parameter);
			define(parameter);
		}
		resolve(body);
		endScope();
		currentFunction = enclosingFunction;
	}

	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme))
			Lox.error(name,
				"Variable with this name already declared in this scope.");

		scope.put(name.lexeme, false);
	}

	private void define(Token name) {
		if (scopes.isEmpty()) return;
		scopes.peek().put(name.lexeme, true);
	}

	@Override
	public Void visit(Stmt.Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}

	@Override
	public Void visit(Stmt.Var stmt) {
		int len = stmt.names.size();
		for (int i = 0; i < len; ++i) {
			Token name = stmt.names.get(i);
			Expr initializer = stmt.initializers.get(i);
			declare(name);
			if (initializer != null) {
				resolve(initializer);
			}
			define(name);
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Function stmt) {
		declare(stmt.name);
		define(stmt.name);
		resolveFunction(stmt.parameters, stmt.body, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visit(Stmt.Method stmt) {
		throw new RuntimeException(
			"Compiler Error: Unexpected method statement outside of class definition.");
	}

	@Override
	public Void visit(Stmt.Class stmt) {
		declare(stmt.name);
		define(stmt.name);

		ClassType enclosingClass = currentClass;
		currentClass = ClassType.CLASS;

		if (stmt.superclass != null) {
			resolve(stmt.superclass);
			beginScope();
			scopes.peek().put("super", true);
			currentClass = ClassType.SUBCLASS;
		}

		beginScope();
		scopes.peek().put("this", true);
		for (Stmt.Method method : stmt.methods) {
			FunctionType declaration;
			if (method.function.name.lexeme.equals("__init__")) {
				if (method.isStatic == true)
					Lox.error(method.function.name,
						"Constructor cannot be a static method.");
				declaration = FunctionType.INITIALIZER;
			} else if (method.isStatic) {
				declaration = FunctionType.STATIC_METHOD;
			} else {
				declaration = FunctionType.METHOD;
			}
			resolveFunction(method.function.parameters,
				method.function.body, declaration);
		}
		endScope();
		if (stmt.superclass != null) endScope();

		currentClass = enclosingClass;
		return null;
	}

	@Override
	public Void visit(Expr.Function expr) {
		resolveFunction(expr.parameters, expr.body, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visit(Expr.Variable expr) {
		if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE)
			Lox.error(expr.name, "Cannot read local variable in it's own initializer.");
		
		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visit(Expr.Assign expr) {
		resolve(expr.value);
		resolveLocal(expr, expr.name);
		return null;
	}

	// other visit methods
	@Override
	public Void visit(Stmt.Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visit(Stmt.If stmt) {
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null)
			resolve(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visit(Stmt.While stmt) {
		resolve(stmt.condition);
		boolean isInEnclosingLoop = isInLoop;
		isInLoop = true;
		resolve(stmt.body);
		isInLoop = isInEnclosingLoop;
		return null;
	}

	@Override
	public Void visit(Stmt.For stmt) {
		beginScope();
		boolean isInEnclosingLoop = isInLoop;
		isInLoop = true;
		if (stmt.initializer != null)
			resolve(stmt.initializer);
		resolve(stmt.condition);
		if (stmt.increment != null)
			resolve(stmt.increment);
		resolve(stmt.body);
		isInLoop = isInEnclosingLoop;
		endScope();
		return null;
	}

	@Override
	public Void visit(Stmt.Break stmt) {
		if (!isInLoop)
			Lox.error(stmt.keyword,
				"Break statement cannot appear outside a loop.");
		return null;
	}

	@Override
	public Void visit(Stmt.Continue stmt) {
		if (!isInLoop)
			Lox.error(stmt.keyword,
				"Continue statement cannot appear outside a loop.");
		return null;
	}

	@Override
	public Void visit(Stmt.Return stmt) {
		if (currentFunction == FunctionType.NONE)
			Lox.error(stmt.keyword,
				"Cannot return from top-level code.");
		if (currentFunction == FunctionType.INITIALIZER)
			Lox.error(stmt.keyword,
				"Cannot return a value from an initializer.");
		if (stmt.value != null)
			resolve(stmt.value);
		return null;
	}

	@Override
	public Void visit(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Unary expr) {
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Ternary expr) {
		resolve(expr.condition);
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visit(Expr.Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visit(Expr.Literal expr) {
		return null;
	}

	@Override
	public Void visit(Expr.Call expr) {
		resolve(expr.callee);
		for (Expr arg : expr.arguments)
			resolve(arg);
		return null;
	}

	@Override
	public Void visit(Expr.Get expr) {
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visit(Expr.Set expr) {
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visit(Expr.This expr) {
		if (currentClass == ClassType.NONE) {
			Lox.error(expr.keyword,
				"Cannot use 'this' outside of a class.");
			return null;
		} else if (currentFunction == FunctionType.STATIC_METHOD) {
			Lox.error(expr.keyword,
				"Cannot use 'this' inside a static method.");
			return null;
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visit(Expr.Super expr) {
		if (currentClass == ClassType.NONE)
			Lox.error(expr.keyword,
				"Cannot use 'super' outside of a class.");
		else if (currentClass == ClassType.CLASS)
			Lox.error(expr.keyword,
				"Cannot use 'super' in a class with no superclass.");
		resolveLocal(expr, expr.keyword);
		return null;
	}
}