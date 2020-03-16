package jlox;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

class Parser {
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<Stmt>();
		while (!isAtEnd()) {
			statements.add(declaration());
		}

		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(TokenType.VAR))
				return varDecl();
			if (match(TokenType.FUN))
				if (check(TokenType.IDENTIFIER))
					return functionStmt();
				else
					retreat(); // is there a cleaner way to do this?
			if (match(TokenType.CLASS))
				return classDecl();
			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Boolean type() {
		if (match(TokenType.CONST)) return true;
		return false;
	}

	private Stmt varDecl() {
		List<Token> names = new ArrayList<>();
		List<Expr> initializers = new ArrayList<>();

		do {
			Token name = consume(TokenType.IDENTIFIER,
				"Expected a variable name.");
			Expr initializer = null;
			boolean isConstant;
			if (match(TokenType.COLON))
				isConstant = type();
			if (match(TokenType.EQUAL))
				initializer = expression();
			names.add(name);
			initializers.add(initializer);
		} while (match(TokenType.COMMA));

		consume(TokenType.SEMICOLON,
			"Expected ';' after variable declaration.");
		return new Stmt.Var(names, initializers);
	}

	private Stmt.Function functionStmt() {
		Token name = consume(TokenType.IDENTIFIER,
			"Expected a function name.");
		FunctionSignature func = function(true);
		return new Stmt.Function(name, func.parameters, func.body);
	}

	private Stmt.Method methodStmt() {
		boolean isStatic = match(TokenType.STATIC);
		consume(TokenType.FUN, "expected a method.");
		Token name = consume(TokenType.IDENTIFIER, "Expected a method name.");
		FunctionSignature func = function(true);
		return new Stmt.Method(
			new Stmt.Function(
				name, func.parameters, func.body
			), isStatic);
	}

	private Stmt classDecl() {
		Token name = consume(TokenType.IDENTIFIER, "Expected a class name.");
		Expr.Variable superclass = null;
		if (match(TokenType.INHERITS)) {
			consume(TokenType.IDENTIFIER,
				"Expected superclass name after 'inherits'.");
			superclass = new Expr.Variable(previous());
		}
		consume(TokenType.LEFT_BRACE, "Expected '{' before class body.");
		List<Stmt.Method> methods = new ArrayList<>();
		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			methods.add(methodStmt());
		}
		consume(TokenType.RIGHT_BRACE, "Expected '}' after class body.");
		
		return new Stmt.Class(name, superclass, methods);
	}

	private Stmt statement() {
		if (match(TokenType.IF)) return ifStmt();
		if (match(TokenType.RETURN)) return returnStmt();
		if (match(TokenType.WHILE)) return whileStmt();
		if (match(TokenType.FOR)) return forStmt();
		if (match(TokenType.BREAK)) return breakStmt();
		if (match(TokenType.CONTINUE)) return contStmt();
		if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
		return exprStmt();
	}

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}

		consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
		return statements;
	}

	private Stmt exprStmt() {
		Expr expr = expression();
		consume(TokenType.SEMICOLON, "Expected ';' after expression.");
		return new Stmt.Expression(expr);
	}

	private Stmt ifStmt() {
		consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.");

		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if (match(TokenType.ELSE))
			elseBranch = statement();
		
		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private Stmt returnStmt() {
		Token kw = previous();
		Expr value = null;
		if (!check(TokenType.SEMICOLON)) {
			value = expression();
		}
		consume(TokenType.SEMICOLON, "Expected ';' after return value");
		return new Stmt.Return(kw, value);
	}

	private Stmt breakStmt() {
		Token kw = previous();
		consume(TokenType.SEMICOLON, "Expected a ';' after 'break'.");
		return new Stmt.Break(kw);
	}

	private Stmt contStmt() {
		Token kw = previous();
		consume(TokenType.SEMICOLON, "Expected a ';' after 'continue'.");
		return new Stmt.Continue(kw);
	}
	
	private Stmt whileStmt() {
		consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition.");
		Stmt body = statement();
		return new Stmt.While(condition, body);
	}

	private Stmt forStmt() {
		consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'");
		
		Stmt initializer = null;
		if (match(TokenType.VAR))
			initializer = varDecl();
		else if (!match(TokenType.SEMICOLON))
			initializer = exprStmt();
		
		Expr condition = null;
		if (!check(TokenType.SEMICOLON))
			condition = expression();
		else
			condition = new Expr.Literal(true);
		consume(TokenType.SEMICOLON, "Expected a ';' after for condition.");

		Expr increment = null;
		if (!check(TokenType.RIGHT_PAREN))
			increment = expression();
		
		consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses.");

		Stmt body = statement();

		return new Stmt.For(
			initializer, condition, increment, body);
	}

	private Expr expression() {
		return assignment();
	}

	private Expr assignment() {
		Expr expr = ternary();

		if (match(TokenType.EQUAL) ||
			  match(TokenType.PLUS_EQUAL) ||
			  match(TokenType.MINUS_EQUAL) ||
			  match(TokenType.STAR_EQUAL) ||
			  match(TokenType.SLASH_EQUAL)) {
			Token equals = previous();
			Expr value = assignment();
			
			TokenType tok = null;
			switch (equals.type) {
			case PLUS_EQUAL:
				tok = TokenType.PLUS;
				break;
			case MINUS_EQUAL:
				tok = TokenType.MINUS;
				break;
			case STAR_EQUAL:
				tok = TokenType.STAR;
				break;
			case SLASH_EQUAL:
				tok = TokenType.SLASH;
				break;
			}

			if (equals.type != TokenType.EQUAL)
				value = new Expr.Binary(expr, new Token(
							tok, equals.lexeme, null, equals.line),
						value);
			
			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name, value);
			} else if (expr instanceof Expr.Get) {
				Expr.Get get = (Expr.Get)expr;
				return new Expr.Set(get.object, get.name, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expr;
	}

	private Expr comma() {
		Expr expr = ternary();

		while (match(TokenType.COMMA)) {
			Token operator = previous();
			Expr right = ternary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr ternary() {
		Expr expr = logic_or();

		while (match(TokenType.QUESTION_MARK)) {
			Token operator = previous();
			Expr left = ternary();
			consume(TokenType.COLON, "Expected ':' after expression.");
			Expr right = ternary();
			expr = new Expr.Ternary(operator, expr, left, right);
		}

		return expr;
	}

	private Expr logic_or() {
		Expr expr = logic_and();

		while(match(TokenType.OR)) {
			Token operator = previous();
			Expr right = logic_and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr logic_and() {
		Expr expr = equality();

		while(match(TokenType.AND)) {
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();

		while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = addition();

		while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
			  TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition() {
		Expr expr = multiplication();

		while (match(TokenType.PLUS, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication() {
		Expr expr = unary();

		while (match(TokenType.STAR, TokenType.SLASH)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr unary() {
		// error handling of binary operators without a left operand
		if (match(TokenType.PLUS, TokenType.STAR, TokenType.SLASH)) {
			Token operator = previous();
			
			if (operator.type == TokenType.PLUS)
				multiplication();
			else
				unary();
			
			throw error(operator, "Binary operator Left operand is missing.");
		}

		if (match(TokenType.MINUS, TokenType.BANG)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return call();
	}

	private Expr call() {
		Expr expr = primary();

		while (true) {
			if (match(TokenType.LEFT_PAREN)) {
				expr = finishCall(expr);
			} else if (match(TokenType.DOT)) {
				Token name = consume(TokenType.IDENTIFIER,
					"Expected property name after '.'");
				expr = new Expr.Get(expr, name);
			} else {
				return expr;
			}
		}
	}

	private Expr finishCall(Expr callee) {
		List<Expr> arguments = new ArrayList<>();
		if (!check(TokenType.RIGHT_PAREN))
			do {
				if (arguments.size() >= 8)
					error(peek(), "Cannot have more than 8 arguments");
				arguments.add(expression());
			} while (match(TokenType.COMMA));
		Token paren = consume(TokenType.RIGHT_PAREN,
			"Expected ')' after arguments.");
		return new Expr.Call(callee, paren, arguments);
	}

	private Expr primary() {
		if (match(TokenType.FALSE)) return new Expr.Literal(false);
		if (match(TokenType.TRUE)) return new Expr.Literal(true);
		if (match(TokenType.NIL)) return new Expr.Literal(null);

		if (match(TokenType.THIS)) return new Expr.This(previous());

		if (match(TokenType.SUPER)) {
			Token keyword = previous();
			consume(TokenType.DOT, "Expected a '.' after 'super'.");
			Token method = consume(TokenType.IDENTIFIER,
				"Expected superclass method name.");
			return new Expr.Super(keyword, method);
		}

		if (match(TokenType.NUMBER))
			return new Expr.Literal(previous().literal);
		
		if (match(TokenType.STRING))
			return makeString();

		if (match(TokenType.LEFT_PAREN)) {
			Expr expr = expression();
			consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
			return new Expr.Grouping(expr);
		}

		if (match(TokenType.IDENTIFIER)) {
			return new Expr.Variable(previous());
		}

		if (match(TokenType.FUN)) {
			return functionExpr();
		}

		if (match(TokenType.STATIC)) {
			throw error(previous(),
				"Keyword 'static' cannot appear outside a class.");
		}

		throw error(peek(), "Expected an expression.");
	}

	private Expr makeString() {
		String str = (String)previous().literal;
		while (peek().type == TokenType.STRING)
			str += (String)advance().literal;
		return new Expr.Literal(str);
	}

	private Expr functionExpr() {
		FunctionSignature func = function(false);
		return new Expr.Function(func.parameters, func.body);
	}

	private static class FunctionSignature {
		// Only used as a wrapper for the two return values of `function`
		// method.
		FunctionSignature(List<Token> parameters, List<Stmt> body) {
			this.parameters = parameters;
			this.body = body;
		}

		List<Token> parameters;
		List<Stmt> body;
	}

	private FunctionSignature function(boolean isStmt) {
		consume(TokenType.LEFT_PAREN, "Expected '('.");
		List<Token> parameters = new ArrayList<>();

		if (!check(TokenType.RIGHT_PAREN))
			do {
				if (parameters.size() >= 255) {
					error(peek(), "Cannot have more than 255 parameters");
				}
				parameters.add(consume(TokenType.IDENTIFIER,
					"Expected a parameter name."));
			} while (match(TokenType.COMMA));

		consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");
		
		List<Stmt> body;

		if (check(TokenType.EQUAL_GREATER)) {
			body = new ArrayList<Stmt>();
			body.add(new Stmt.Return(advance(), expression()));
			if (isStmt)
			consume(TokenType.SEMICOLON, "Expected a ';' after expression");
		} else {
			consume(TokenType.LEFT_BRACE, "Expected '{' before function body");
			body = block();
		}
		
		return new FunctionSignature(parameters, body);
	}

	private Token advance() {
		if (!isAtEnd()) ++current;
		return previous();
	}

	private Token retreat() {
		--current;
		return peek();
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean isAtEnd() {
		return peek().type == TokenType.EOF;
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();
		throw error(peek(), message);
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	private void synchronize() {
		advance();

		while(!isAtEnd()) {
			if (previous().type == TokenType.SEMICOLON) return;

			switch(peek().type) {
			case CLASS:
			case FUN:
			case VAR:
			case FOR:
			case IF:
			case WHILE:
			case RETURN:
				return;
			}

			advance();
		}
	}
}