package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private static final Map<String, TokenType> keywords;

	private int start = 0;
	private int current = 0;
	private int line = 1;

	static {
		keywords = new HashMap<>();
		keywords.put("and",      TokenType.AND);
		keywords.put("break",    TokenType.BREAK);
		keywords.put("class",    TokenType.CLASS);
		keywords.put("const",    TokenType.CONST);
		keywords.put("continue", TokenType.CONTINUE);
		keywords.put("else",     TokenType.ELSE);
		keywords.put("false",    TokenType.FALSE);
		keywords.put("for",      TokenType.FOR);
		keywords.put("function", TokenType.FUN);
		keywords.put("if",       TokenType.IF);
		keywords.put("in",       TokenType.IN);
		keywords.put("inherits", TokenType.INHERITS);
		keywords.put("nil",      TokenType.NIL);
		keywords.put("or",       TokenType.OR);
		keywords.put("return",   TokenType.RETURN);
		keywords.put("super",    TokenType.SUPER);
		keywords.put("static",   TokenType.STATIC);
		keywords.put("this",     TokenType.THIS);
		keywords.put("true",     TokenType.TRUE);
		keywords.put("var",      TokenType.VAR);
		keywords.put("while",    TokenType.WHILE);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}

		tokens.add(new Token(TokenType.EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) { // (){}[]
		case '(': addToken(TokenType.LEFT_PAREN); break;
		case ')': addToken(TokenType.RIGHT_PAREN); break;
		case '{': addToken(TokenType.LEFT_BRACE); break;
		case '}': addToken(TokenType.RIGHT_BRACE); break;
		case '[': addToken(TokenType.LEFT_BRACKET); break;
		case ']': addToken(TokenType.RIGHT_BRACKET); break;
		case ',': addToken(TokenType.COMMA); break;
		case '.': addToken(TokenType.DOT); break;
		case '-':
			addToken(
				match('=') ?
					TokenType.MINUS_EQUAL : TokenType.MINUS);
			break;
		case '+':
			addToken(
				match('=') ?
					TokenType.PLUS_EQUAL : TokenType.PLUS);
			break;
		case ';': addToken(TokenType.SEMICOLON); break;
		case '*':
			addToken(
				match('=') ?
					TokenType.STAR_EQUAL : TokenType.STAR);
			break;
		case '?': addToken(TokenType.QUESTION_MARK); break;
		case ':': addToken(TokenType.COLON); break;
		case '/':
			if (match('/')) {
				while(peek() != '\n' && !isAtEnd()) advance();
			} else if (match('*')) {
				int comment_depth = 1;
				while (comment_depth > 0) {
					if (match('\n'))
						++line;
					else if (match('/')) {
						if (match('*')) {
							++comment_depth;
							continue;
						}
					}
					else if (match('*')) {
						if (match('/')) {
							--comment_depth;
							continue;
						}
					}
					else if (!isAtEnd())
						advance();
					else
						break;
				}
			} else if (match('=')) {
				addToken(TokenType.SLASH_EQUAL);
			} else {
				addToken(TokenType.SLASH);
			}
			break;
		case '!':
			addToken(
				match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
			break;
		case '=':
			addToken(
				match('=') ? TokenType.EQUAL_EQUAL :
							 match('>') ? TokenType.EQUAL_GREATER :
							 			  TokenType.EQUAL);
			break;
		case '>':
			addToken(
				match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
			break;
		case '<':
			addToken(
				match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
			break;

		case '\n':
			++line;
		case ' ':
		case '\r':
		case '\t':
			break;

		case '"': string(); break;

		default:
			if (isDigit(c)) number();
			else if (isAlpha(c)) identifier();
			else Lox.error(line, "Unexpected character.");
			break;
		}
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private char advance() {
		++current;
		return source.charAt(current - 1);
	}

	private void retreat() {
		--current;
	}

	private boolean match(char expected) {
		if (isAtEnd() || source.charAt(current) != expected) return false;

		++current;
		return true;
	}

	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String lexeme = source.substring(start, current);
		tokens.add(new Token(type, lexeme, literal, line));
	}

	private void string() {
		while(peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') ++line;
			advance();
		}

		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		advance();

		String literal = source.substring(start + 1, current - 1);
		addToken(TokenType.STRING, literal);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private void number() {
		while(isDigit(peek())) advance();

		if (peek() == '.' && isDigit(peekNext())) {
			advance();

			while(isDigit(peek())) advance();
		}

		if (isAlpha(peek()))
			Lox.error(line, "Unexpected character after number literal");

		addToken(TokenType.NUMBER,
			Double.parseDouble(source.substring(start, current)));
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z') || (c == '_');
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private void identifier() {
		while (isAlphaNumeric(peek())) advance();

		String id = source.substring(start, current);
		TokenType type = keywords.get(id);
		if (type == null) type = TokenType.IDENTIFIER;
		addToken(type);
	}
}