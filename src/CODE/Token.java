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
    COMMA, DOT, MINUS, PLUS, SLASH, STAR, SEMICOLON, AMPERSAND,

    // One or two character tokens.
    NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER, INTEGER, REAL,

    // Keywords.
    AND, ELSE, FALSE, IF, NIL, OR, SCAN,
    DISPLAY, TRUE, WHILE, INT, BOOL, CHAR, FLOAT, NOT, BEGIN_CODE,
    END_CODE, BEGIN_IF, END_IF, BEGIN_WHILE, END_WHILE,
    EOF
}