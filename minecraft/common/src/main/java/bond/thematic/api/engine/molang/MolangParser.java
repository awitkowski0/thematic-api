package bond.thematic.api.engine.molang;

import bond.thematic.api.engine.molang.MolangLexer.Token;
import bond.thematic.api.engine.molang.MolangLexer.TokenType;
import bond.thematic.api.engine.molang.MolangAst.*;

import java.util.ArrayList;
import java.util.List;

import static bond.thematic.api.engine.molang.MolangLexer.TokenType.*;

public class MolangParser {
    private final List<Token> tokens;
    private int pos;

    public MolangParser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public MolangAst parse() {
        return statementList();
    }

    private MolangAst statementList() {
        List<MolangAst> stmts = new ArrayList<>();
        stmts.add(assignment());
        while (match(SEMICOLON)) {
            if (check(EOF)) break;
            stmts.add(assignment());
        }
        if (stmts.size() == 1) return stmts.get(0);
        return new StatementList(stmts);
    }

    private MolangAst assignment() {
        MolangAst expr = ternary();
        if (match(EQUAL)) {
            if (expr instanceof VariableExpr v) {
                return new AssignmentExpr(v.name(), assignment(), false);
            } else if (expr instanceof TempExpr t) {
                return new AssignmentExpr(t.name(), assignment(), true);
            } else if (expr instanceof QueryExpr) {
                throw error("Cannot assign to a query");
            }
            throw error("Invalid assignment target");
        }
        return expr;
    }

    private MolangAst ternary() {
        MolangAst expr = logicalOr();
        if (match(QUESTION)) {
            MolangAst thenExpr = ternary();
            consume(COLON, "Expected ':' for ternary");
            MolangAst elseExpr = ternary();
            return new TernaryExpr(expr, thenExpr, elseExpr);
        }
        return expr;
    }

    private MolangAst logicalOr() {
        MolangAst expr = logicalAnd();
        while (match(OR)) {
            MolangAst right = logicalAnd();
            expr = new BinaryExpr(BinaryOp.OR, expr, right);
        }
        return expr;
    }

    private MolangAst logicalAnd() {
        MolangAst expr = comparison();
        while (match(AND)) {
            MolangAst right = comparison();
            expr = new BinaryExpr(BinaryOp.AND, expr, right);
        }
        return expr;
    }

    private MolangAst comparison() {
        MolangAst expr = addition();
        while (true) {
            if (match(EQUAL_EQUAL)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.EQUAL, expr, right);
            } else if (match(BANG_EQUAL)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.NOT_EQUAL, expr, right);
            } else if (match(LESS)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.LESS, expr, right);
            } else if (match(GREATER)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.GREATER, expr, right);
            } else if (match(LESS_EQUAL)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.LESS_EQUAL, expr, right);
            } else if (match(GREATER_EQUAL)) {
                MolangAst right = addition();
                expr = new BinaryExpr(BinaryOp.GREATER_EQUAL, expr, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private MolangAst addition() {
        MolangAst expr = multiplication();
        while (true) {
            if (match(PLUS)) {
                MolangAst right = multiplication();
                expr = new BinaryExpr(BinaryOp.ADD, expr, right);
            } else if (match(MINUS)) {
                MolangAst right = multiplication();
                expr = new BinaryExpr(BinaryOp.SUBTRACT, expr, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private MolangAst multiplication() {
        MolangAst expr = unary();
        while (true) {
            if (match(STAR)) {
                MolangAst right = unary();
                expr = new BinaryExpr(BinaryOp.MULTIPLY, expr, right);
            } else if (match(SLASH)) {
                MolangAst right = unary();
                expr = new BinaryExpr(BinaryOp.DIVIDE, expr, right);
            } else if (match(PERCENT)) {
                MolangAst right = unary();
                expr = new BinaryExpr(BinaryOp.MODULO, expr, right);
            } else {
                break;
            }
        }
        return expr;
    }

    private MolangAst unary() {
        if (match(MINUS)) {
            return new UnaryExpr(UnaryOp.NEGATE, unary());
        }
        if (match(BANG)) {
            return new UnaryExpr(UnaryOp.NOT, unary());
        }
        return primary();
    }

    private MolangAst primary() {
        if (match(NUMBER)) {
            return new NumberExpr((double) previous().literal());
        }
        if (match(STRING)) {
            return new StringExpr((String) previous().literal());
        }
        if (match(LPAREN)) {
            MolangAst expr = assignment();
            consume(RPAREN, "Expected ')' after expression");
            return expr;
        }

        if (match(IDENTIFIER)) {
            String name = (String) previous().literal();
            if (match(DOT)) {
                String property = consume(IDENTIFIER, "Expected identifier after '.'").literal().toString();
                if (match(LPAREN)) {
                    List<MolangAst> args = new ArrayList<>();
                    if (!check(RPAREN)) {
                        do {
                            args.add(assignment());
                        } while (match(COMMA));
                    }
                    consume(RPAREN, "Expected ')' after function arguments");
                    return new FunctionCallExpr(name + "." + property, args);
                }
                return switch (name) {
                    case "q", "query" -> new QueryExpr(property);
                    case "v", "variable" -> new VariableExpr(property);
                    case "t", "temp", "temporary" -> new TempExpr(property);
                    case "math" -> new FunctionCallExpr(property, List.of());
                    default -> throw error("Unknown namespace: " + name);
                };
            }
            if (match(LPAREN)) {
                List<MolangAst> args = new ArrayList<>();
                if (!check(RPAREN)) {
                    do {
                        args.add(assignment());
                    } while (match(COMMA));
                }
                consume(RPAREN, "Expected ')' after function arguments");
                return new FunctionCallExpr(name, args);
            }
            return new QueryExpr(name);
        }

        throw error("Expected expression but got " + peek().type());
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(message);
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type() == type;
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private RuntimeException error(String message) {
        Token t = peek();
        throw new MolangLexer.MolangParseException(
                message + " at position " + t.start() + " (token: " + t.type() + ")"
        );
    }
}
