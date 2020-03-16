package jlox;

import java.util.Map;
import java.util.HashMap;

class LoxInstance {
	private LoxClass cls;
	protected final Map<String, Object> fields;

	LoxInstance(LoxClass cls) {
		this.cls = cls;
		this.fields = new HashMap<String, Object>();
	}

	LoxInstance(LoxClass cls, Map<String, Object> fields) {
		this.cls = cls;
		this.fields = fields;
	}

	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		LoxFunction method = cls.findMethod(this, name.lexeme);
		if (method != null) return method;

		throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
	}

	void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}

	@Override
	public String toString() {
		return "<" + cls.name + " " + " instance>";
	}
}