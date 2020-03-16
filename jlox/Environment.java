package jlox;

import java.util.Map;
import java.util.HashMap;

class Environment {
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();

	Environment() {
		enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	void define(String name, Object value) {
		values.put(name, value);
	}

	void define(String name) {
		values.put(name, null);
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			Object value = values.get(name.lexeme);
			return value;
		}
		if (enclosing != null)
			return enclosing.get(name);
		throw new RuntimeError(name,
			"Undefined variable '" + name.lexeme + "'.");
	}

	Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);
	}

	Environment ancestor(int distance) {
		Environment env = this;
		for (int i = 0; i < distance; ++i)
			env = env.enclosing;
		return env;
	}

	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme))
			values.put(name.lexeme, value);
		else if (enclosing != null)
			enclosing.assign(name, value);
		else
			throw new RuntimeError(name,
				"Undefined variable '" + name.lexeme + "'.");
	}

	void assignAt(int distance, Token name, Object value) {
		ancestor(distance).values.put(name.lexeme, value);
	}
}