/*
Defines the global functions: clock, print, println and input
*/

package jlox;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Globals {
	public static void define(Environment env) {
		// functions
		env.define("clock", new LoxCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000.0;
			}

			public String toString() {
				return "<builtin fn clock>";
			}
		});

		env.define("input", new LoxCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				InputStreamReader input = new InputStreamReader(System.in);
				BufferedReader reader = new BufferedReader(input);

				try {
					return reader.readLine();
				} catch (IOException ioe) {
					Lox.error(null, "An input error occured.");
					return null;
				}
			}

			public String toString() {
				return "<builtin fn input>";
			}
		});

		env.define("println", new LoxCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				for (Object arg : arguments)
					System.out.println(Interpreter.stringify(arg));
				return null;
			}

			public String toString() {
				return "<builtin fn print>";
			}
		});

		env.define("print", new LoxCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				for (Object arg : arguments)
					System.out.print(Interpreter.stringify(arg));
				return null;
			}

			public String toString() {
				return "<builtin fn print>";
			}
		});
	}
}