package jlox;

import java.util.List;

interface LoxCallable {
	public int arity();
	public Object call(Interpreter interpreter, List<Object> arguments);
}