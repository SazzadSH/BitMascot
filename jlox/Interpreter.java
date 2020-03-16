package jlox;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	final Environment globals = new Environment();
	private Environment environment = globals;
	private final Map<Expr, Integer> locals = new HashMap<>();

	class Break extends RuntimeException {
		Break() {
			super(null, null, false, false);
		}
	}

	class Continue extends RuntimeException {
		Continue() {
			super(null, null, false, false);
		}
	}

	class Return extends RuntimeException {
		final Object value;
		
		Return(Object value) {
			super(null, null, false, false);
			this.value = value;
		}
	}

	Interpreter() {
		Globals.define(globals);
	}

	void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}

	void execute(Stmt stmt) {
			stmt.accept(this);
	}

	void execute(List<Stmt> stmts) {
		for (Stmt stmt : stmts)
			execute(stmt);
	}
	
	void executeBlock(List<Stmt> statements, Environment env) {
		Environment previous = this.environment;
		try {
			this.environment = env;
			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	public void resolve(Expr expr, int depth) {
		locals.put(expr, depth);
	}

	@Override
	public Void visit(Stmt.Block block) {
		executeBlock(block.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visit(Stmt.Expression stmt) {
		Object value = evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visit(Stmt.Var stmt) {
		for (int i = 0; i < stmt.names.size(); ++i) {
			Expr initializer = stmt.initializers.get(i);
			if (initializer != null)
				environment.define(stmt.names.get(i).lexeme, evaluate(initializer));
			else
				environment.define(stmt.names.get(i).lexeme);
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Class stmt) {
		environment.define(stmt.name.lexeme);

		Object superClass = null;
		if (stmt.superclass != null) {
			superClass = evaluate(stmt.superclass);
			if (!(superClass instanceof LoxClass)) {
				throw new RuntimeError(stmt.superclass.name,
					"Superclass must be a class.");
			}
			environment = new Environment(environment);
			environment.define("super", superClass);
		}

		Map<String, LoxFunction> methods = new HashMap<>();
		Map<String, Object> fields = new HashMap<>(); 
		
		for (Stmt.Method method : stmt.methods) {
			LoxFunction function = new LoxFunction(method.function, environment,
				method.function.name.lexeme.equals("__init__"));
			if (method.isStatic) {
				fields.put(method.function.name.lexeme, function);
			} else {
				methods.put(method.function.name.lexeme, function);
			}
		}
		
		LoxClass cls = new LoxClass(stmt.name.lexeme,
			(LoxClass)superClass, methods, fields);

		if (superClass != null)
			environment = environment.enclosing;
		environment.assign(stmt.name, cls);
		
		return null;
	}

	@Override
	public Void visit(Stmt.If stmt) {
		if (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.thenBranch);
		} else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Break stmt) {
		throw new Break();
	}

	@Override
	public Void visit(Stmt.Continue stmt) {
		throw new Continue();
	}

	@Override
	public Void visit(Stmt.While stmt) {
		while (isTruthy(evaluate(stmt.condition))) {
			try {
				execute(stmt.body);
			} catch (Break e) {
				break;
			} catch (Continue e) {
				continue;
			}
		}
		return null;
	}

	@Override
	public Void visit(Stmt.For stmt) {
		Environment previous = this.environment;
		try {
			this.environment = new Environment(environment);
			if (stmt.initializer != null)
				execute(stmt.initializer);
			while (isTruthy(evaluate(stmt.condition))) {
				try {
					execute(stmt.body);
				} catch (Break e) {
					break;
				} catch (Continue e) {
					continue;
				} finally {
					if (stmt.increment != null)
						evaluate(stmt.increment);
				}
			}
		} finally {
			this.environment = previous;
		}
		
		return null;
	}

	@Override
	public Void visit(Stmt.Function stmt) {
		environment.define(stmt.name.lexeme,
			new LoxFunction(stmt, environment));
		return null;
	}

	@Override
	public Void visit(Stmt.Method stmt) {
		return null;
	}

	@Override
	public Void visit(Stmt.Return stmt) {
		Object value = null;
		if (stmt.value != null) {
			value = evaluate(stmt.value);
		}
		throw new Return(value);
	}

	@Override
	public Object visit(Expr.Function expr) {
		return new LoxFunction(
			new Stmt.Function(null, expr.parameters, expr.body),
			environment
		);
	}

	@Override
	public Object visit(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		Integer dist = locals.get(expr);
		if (dist != null) {
			environment.assignAt(dist, expr.name, value);
		} else {
			globals.assign(expr.name, value);
		}
		return value;
	}

	@Override
	public Object visit(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visit(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visit(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double)right;
		case BANG:
			return !isTruthy(right);
		}

		return null;
	}

	@Override
	public Object visit(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case PLUS:
			if (left instanceof Double && right instanceof Double)
				return (double)left + (double)right;
			if (left instanceof String && right instanceof String)
				return (String)left + (String)right;
			if (left instanceof String && right instanceof Double)
				return (String)left + Lox.reprNumber(right);
			if (left instanceof Double && right instanceof String)
				return Lox.reprNumber(left) + (String)right;
			if (left instanceof String || right instanceof String)
				return left.toString() + right.toString();
			throw new RuntimeError(expr.operator,
				"Operands must be two numbers or two strings");
		case MINUS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left - (double)right;
		case STAR:
			checkNumberOperands(expr.operator, left, right);
			return (double)left * (double)right;
		case SLASH:
			checkNumberOperands(expr.operator, left, right);
			return (double)left / (double)right;
		case GREATER:
			checkNumberOperands(expr.operator, left, right);
			return (double)left > (double)right;
		case GREATER_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left >= (double)right;
		case LESS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left < (double)right;
		case LESS_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left <= (double)right;
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case BANG_EQUAL:
			return !isEqual(left, right);
		}

		return null;
	}

	@Override
	public Object visit(Expr.Call expr) {
		Object callee = evaluate(expr.callee);

		List<Object> arguments = new ArrayList<>();
		for (Expr arg : expr.arguments) {
			arguments.add(evaluate(arg));
		}

		if (!(callee instanceof LoxCallable)) {
			throw new RuntimeError(expr.paren,
				"Object is not callable.");
		}

		LoxCallable function = (LoxCallable)callee;
		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.paren,
				"Expected " + function.arity() +
				(function.arity() == 1? " argument" : " arguments") +
				", got " + arguments.size() + ".");
		}
		return function.call(this, arguments);
	}

	@Override
	public Object visit(Expr.Get expr) {
		Object obj = evaluate(expr.object);
		if (obj instanceof LoxInstance) {
			return ((LoxInstance)obj).get(expr.name);
		}

		throw new RuntimeError(expr.name,
			"Only instances have properties.");
	}

	@Override
	public Object visit(Expr.Set expr) {
		Object obj = evaluate(expr.object);
		if (obj instanceof LoxInstance) {
			Object value = evaluate(expr.value);
			((LoxInstance)obj).set(expr.name, value);
			return value;
		}

		throw new RuntimeError(expr.name,
			"Only instances have properties.");
	}

	@Override
	public Object visit(Expr.Logical expr) {
		Object left = evaluate(expr.left);

		switch (expr.operator.type) {
		case OR:
			if (isTruthy(left)) return left;
		case AND:
			if (!isTruthy(left)) return left;
		}

		return evaluate(expr.right);
	}

	@Override
	public Object visit(Expr.Ternary expr) {
		Object condition = evaluate(expr.condition);

		return isTruthy(condition) ?
			evaluate(expr.left) : evaluate(expr.right);
	}

	@Override
	public Object visit(Expr.Variable expr) {
		return lookupVariable(expr.name, expr);
	}

	@Override
	public Object visit(Expr.This expr) {
		return lookupVariable(expr.keyword, expr);
	}

	@Override
	public Object visit(Expr.Super expr) {
		int dist = locals.get(expr);
		LoxClass superclass = (LoxClass)environment.getAt(
			dist, "super");
		LoxInstance obj = (LoxInstance)environment.getAt(
			dist - 1, "this");
		LoxFunction method = superclass.findMethod(obj, expr.method.lexeme);
		if (method == null)
			throw new RuntimeError(expr.method,
				"Undefined property '" + expr.method.lexeme + "'.");
		return method;
	}

	private Object lookupVariable(Token name, Expr expr) {
		Integer dist = locals.get(expr);
		if (dist != null) {
			return environment.getAt(dist, name.lexeme);
		}
		return globals.get(name);
	}

	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		return a.equals(b);
	}

	private boolean isTruthy(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Boolean) return (boolean)obj;
		return true;
	}

	public static String stringify(Object obj) {
		if (obj == null) return "nil";

		if (obj instanceof Double) {
			return Lox.reprNumber(obj);
		}

		return obj.toString();
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number");
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be a number");
	}
}