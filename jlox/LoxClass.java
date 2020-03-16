package jlox;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

class LoxClass extends LoxInstance implements LoxCallable {
	final String name;
	final LoxClass superclass;
	private final Map<String, LoxFunction> methods;

	LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
		super(null);
		this.name = name;
		this.superclass = superclass;
		this.methods = methods;
	}

	LoxClass(String name, LoxClass superclass,
		  Map<String, LoxFunction> methods, Map<String, Object> fields) {
		super(null, fields);
		this.name = name;
		this.superclass = superclass;
		this.methods = methods;
	}

	LoxFunction findMethod(LoxInstance instance, String name) {
		if (methods.containsKey(name))
			return methods.get(name).bind(instance);
		
		LoxFunction func = null;
		
		if (superclass != null) {
			func = superclass.findMethod(instance, name);
			if (func != null) return func;
		}

		try {
			func = (LoxFunction)get(new Token(TokenType.IDENTIFIER, name, null, 0));
		} catch(RuntimeError err) {
			return null;
		}
		return func;
	}

	@Override
	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		throw new RuntimeError(name, "Undefined static property '" + name.lexeme + "'.");
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		LoxFunction initializer = methods.get("__init__");
		if (initializer != null)
			initializer.bind(instance).call(interpreter, arguments);
		return instance;
	}

	@Override
	public int arity() {
		LoxFunction initializer = methods.get("__init__");
		if (initializer == null) return 0;
		return initializer.arity();
	}

	@Override
	public String toString() {
		return "<class " + this.name + ">";
	}
}