package CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static CODE.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("AND",    AND);
        keywords.put("ELSE",   ELSE);
        keywords.put("FALSE",  FALSE);
        keywords.put("IF",     IF);
        keywords.put("nil",    NIL);
        keywords.put("OR",     OR);
        keywords.put("DISPLAY", DISPLAY);
        keywords.put("TRUE",   TRUE);
        keywords.put("WHILE",  WHILE);
        keywords.put("INT",  INT);
        keywords.put("FLOAT",  FLOAT);
        keywords.put("CHAR",  CHAR);
        keywords.put("BOOL",  BOOL);
        keywords.put("NOT",  NOT);
        keywords.put("BEGIN",  BEGIN);
        keywords.put("END",  END);
        keywords.put("CODE",  CODE);
        keywords.put("SCAN",  SCAN);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case '*': addToken(STAR); break;
            case '&': addToken(AMPERSAND); break;
            case '#': while (!isAtEnd()) advance(); break;
            case '$': addToken(DOLLAR); break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else if (match('>')) {
                    addToken(NOT_EQUAL);
                } else {
                    addToken(LESS);
                }
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                addToken(SLASH);
                break;
            case '%':
                addToken(PERCENT);
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                addToken(NEWLINE);
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    CODE_LANG.error(line, "Unexpected character.");
                }
                break;
        }
    }
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private void number() {
        boolean isFLOAT = false;
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            isFLOAT = true;
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }
        if (isFLOAT) {
            addToken(REAL,
                    Double.parseDouble(source.substring(start, current)));
        } else {
            addToken(INTEGER,
                    Integer.parseInt(source.substring(start, current)));
        }

    }
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            CODE_LANG.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }
    private boolean isAtEnd() {
        return current >= source.length();
    }
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
