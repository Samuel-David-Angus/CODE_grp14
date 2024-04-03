package CODE;

import java.util.ArrayList;
import java.util.List;
import static CODE.TokenType.*;
public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {

        this.tokens = tokens;
        /*for (Token t: tokens) {
            System.out.println(t);
        }*/

    }
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        if (match(BEGIN) && match(CODE) && match(NEWLINE)) {
            while (!check(END) && !isAtEnd()) {
                statements.add(declaration());
            }
        }
        consume(END, "enclose with END CODE");
        consume(CODE, "enclose with END CODE");
        consume(NEWLINE, "enclose with END CODE");

        return statements;
    }
    private Expr expression() {
        return assignment();
    }
    private Stmt declaration() {
        try {
            if (match(INT, BOOL, FLOAT, CHAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    private Stmt statement() {
        if (match(DISPLAY)) return printStatement();

        return expressionStatement();
    }
    private Stmt printStatement() {
        Expr value = expression();
        consume(NEWLINE, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Token type = tokens.get(current - 2);

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(NEWLINE, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer, type);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(NEWLINE, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    private Expr assignment() {
        Expr expr = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
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
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
    private ParseError error(Token token, String message) {
        CODE_LANG.error(token, message);
        return new ParseError();
    }
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == NEWLINE) return;

            switch (peek().type) {
                case IF:
                case WHILE:
                case DISPLAY:
                    return;
            }

            advance();
        }
    }
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS, AMPERSAND)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr unary() {
        if (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(DOLLAR)) return new Expr.Literal("\n");

        if (match(INTEGER, REAL, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");

    }
}
