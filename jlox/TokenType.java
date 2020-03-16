package jlox;

enum TokenType {
	// (){}[]
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
	// ,.;:?
	COMMA, DOT, SEMICOLON, COLON, QUESTION_MARK,
	// arithmetic operators + - / *
	MINUS, PLUS, SLASH, STAR,
	// relational operators ! != == > >= < <=
	BANG, BANG_EQUAL, EQUAL_EQUAL,
	GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
	// assignment and augmented assignment operators = += -= /= *= =>
	EQUAL, PLUS_EQUAL, MINUS_EQUAL, SLASH_EQUAL, STAR_EQUAL, EQUAL_GREATER,
	// literals
	IDENTIFIER, STRING, NUMBER, INTEGER,
	// keywords
	AND, BREAK, CLASS, CONST, CONTINUE, ELSE, FALSE, FUN, FOR, IF, IN, INHERITS, NIL, OR, RETURN,
	SUPER, STATIC, THIS, TRUE, VAR, WHILE,
	// end of file
	EOF
}