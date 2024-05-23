package CODE;

import java.util.ArrayList;
import java.util.List;
import static CODE.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
    private List<Stmt> statements = null;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        for (Token t : tokens) {
            //SET TO true TO PRINT TOKENS FOR DEBUGGING
            if (false) System.out.println(t);
            if (t.type.equals(DISPLAY)) CODE_LANG.noDisplay = false;
        }
    }
    List<Stmt> parse() {
        statements = new ArrayList<>();

        if (match(BEGIN) && match(CODE) && match(NEWLINE)) {
            while (!check(END) && !isAtEnd()) {
                statements.add(declaration());
            }
        }
        consume(END, "enclose with END CODE");
        consume(CODE, "enclose with END CODE");
        consume(NEWLINE, "enclose with END CODE");
        if (!isAtEnd()) {
            throw error(peek(), "Unexpected token after END CODE");
        }

        return statements;
    }
    private Expr expression() {
        return assignment();
    }
    private Stmt declaration() {
        try {
            if (match(INT, BOOL, FLOAT, CHAR)) {
                Token type = tokens.get(current - 1);
                return varDeclaration(type);
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(DISPLAY) && match(COLON)) return printStatement();
        if (match(SCAN) && match(COLON)) return scanStatement();
        if (match(WHILE)) return whileStatement();
        if(match(BEGIN) && (match(IF) || match(WHILE)) && match(NEWLINE)) return new Stmt.Block(block());

        return expressionStatement();
    }
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        consume(NEWLINE, "Expect NEWLINE after )");

        Stmt thenBranch = statement();

        Stmt elseBranch = null;

        if (match(ELSE)) {
            if (match(IF)) {
                // Handle "else if" condition
                consume(LEFT_PAREN, "Expect '(' after 'else if'.");
                Expr elseIfCondition = expression();
                consume(RIGHT_PAREN, "Expect ')' after 'else if' condition.");
                consume(NEWLINE, "enclose with BEGIN IF");

                Stmt elseIfBranch = statement();

                //elseBranch = new Stmt.If(elseIfCondition, elseIfBranch, null);
                //RECURSION
                //elseStatement(elseBranch);

                if (match(ELSE)) {
                    // Handle "if, else if, else" branch
                    consume(NEWLINE, "enclose with BEGIN IF");

                    //Stmt elseIfElseBranch = statement();
                }
                elseBranch = new Stmt.If(elseIfCondition, elseIfBranch, null);
            } else {
                // Handle "else" branch
                consume(NEWLINE, "enclose with BEGIN IF");

                elseBranch = statement();
            }
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    /*private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        consume(NEWLINE, "Expect NEWLINE after )");

        Stmt thenBranch = statement();

        Stmt elseBranch = null;

        elseStatement(elseBranch);

        return new Stmt.If(condition, thenBranch, elseBranch);
    }
    private void elseStatement(Stmt elseBranch) {
        if (match(ELSE)) {
            if (match(IF)) {
                // Handle "else if" condition
                consume(LEFT_PAREN, "Expect '(' after 'else if'.");
                Expr elseIfCondition = expression();
                consume(RIGHT_PAREN, "Expect ')' after 'else if' condition.");
                consume(NEWLINE, "enclose with BEGIN IF");

                Stmt elseIfBranch = statement();

                //elseBranch = new Stmt.If(elseIfCondition, elseIfBranch, null);
                //RECURSION
                //elseStatement(elseBranch);

                if (match(ELSE)) {
                    // Handle "if, else if, else" branch
                    consume(NEWLINE, "enclose with BEGIN IF");

                    //Stmt elseIfElseBranch = statement();
                }
                elseBranch = new Stmt.If(elseIfCondition, elseIfBranch, null);
            } else {
                // Handle "else" branch
                consume(NEWLINE, "enclose with BEGIN IF");

                elseBranch = statement();
            }
        }
    }*/
    private Stmt printStatement() {
        Expr value = expression();
        consume(NEWLINE, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    private  Stmt scanStatement() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(NEWLINE, "Expect ';' after value.");
        return new Stmt.Scan(name);
    }
    private Stmt varDeclaration(Token type) {
        do {
            Token name = consume(IDENTIFIER, "Expect variable name.");


            Expr initializer = null;
            if (match(EQUAL)) {
                initializer = expression();
            }
            statements.add(new Stmt.Var(name, initializer, type));
        } while (match(COMMA));
        consume(NEWLINE, "Incorrect variable declaration.");
        return statements.remove(statements.size() - 1);
    }
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");

        consume(NEWLINE, "enclose with BEGIN WHILE");

        Stmt body = statement();


        return new Stmt.While(condition, body);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(NEWLINE, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END) && !isAtEnd()) {
            statements.add(declaration());
        }
        if (match(END)) {
            if (!(match(IF) || match(WHILE))) throw error(peek(), "Expect either 'IF' or 'WHILE' after 'END");
        } else {
            throw error(peek(), "Block unterminated");
        }

        consume(NEWLINE, "Expect a newline after block.");

        return statements;
    }
    private Expr assignment() {
        //Expr expr = equality();
        Expr expr = or();

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
    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }
    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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
    private boolean checkAhead(TokenType type, int lookAhead) {
        if (isAtEnd()) return false;

        return tokens.get(current + lookAhead).type == type;
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
        if (match(PLUS, MINUS, NOT)) {
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

        if (match(INTEGER, REAL, STRING, CHARACTER)) {
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
