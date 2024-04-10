package CODE;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, DOT, MINUS, PLUS, SLASH, STAR, PERCENT, AMPERSAND, NEWLINE, DOLLAR,

    // One or two character tokens.
    NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER, INTEGER, REAL, CHARACTER,

    // Keywords.
    AND, ELSE, FALSE, IF, NIL, OR, SCAN, BEGIN, END, CODE,
    DISPLAY, TRUE, WHILE, INT, BOOL, CHAR, FLOAT, NOT,
    EOF
}
