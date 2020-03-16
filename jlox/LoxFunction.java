package jlox;

import java.util.List;

class LoxFunction implements LoxCallable {
	private final Stmt.Function decleration;
	private final Environment closure;
	private final boolean isInitializer;

	LoxFunction(Stmt.Function decleration, Environment closure,
			boolean isInitializer) {
		this.decleration = decleration;
		this.closure = closure;
		this.isInitializer = isInitializer;
	}

	LoxFunction(Stmt.Function decleration, Environment closure) {
		this.decleration = decleration;
		this.closure = closure;
		this.isInitializer = false;
	}

	LoxFunction bind(LoxInstance instance) {
		Environment env = new Environment(this.closure);
		env.define("this", instance);
		return new LoxFunction(decleration, env, isInitializer);
	}

	@Override
	public int arity() {
		return decleration.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment env = new Environment(closure);
		for (int i = 0; i < arguments.size(); ++i) {
			env.define(decleration.parameters.get(i).lexeme, arguments.get(i));
		}
		
		try {
			interpreter.executeBlock(decleration.body, env);
		} catch (Interpreter.Return r) {
			return r.value;
		}

		if (isInitializer) return closure.getAt(0, "this");
		return null;
	}

	public String toString() {
		if (decleration.name != null)
			return "<function " + decleration.name.lexeme + ">";
		return "<function>";
	}
}