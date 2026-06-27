package bond.thematic.api.engine.molang;

import java.util.ArrayList;
import java.util.List;

public class MolangLexer {
    private final String source;
    private int pos;
    private int start;
    private final List<Token> tokens;

    public MolangLexer(String source) {
        this.source = source;
        this.pos = 0;
        this.start = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            start = pos;
            char c = advance();
            switch (c) {
                case ' ', '\t', '\r', '\n' -> {}
                case '/' -> {
                    if (match('/')) {
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else if (match('*')) {
                        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                            advance();
                        }
                        if (!isAtEnd()) { advance(); advance(); }
                    } else {
                        addToken(TokenType.SLASH);
                    }
                }
                case '(' -> addToken(TokenType.LPAREN);
                case ')' -> addToken(TokenType.RPAREN);
                case '{' -> addToken(TokenType.LBRACE);
                case '}' -> addToken(TokenType.RBRACE);
                case '[' -> addToken(TokenType.LBRACKET);
                case ']' -> addToken(TokenType.RBRACKET);
                case ',' -> addToken(TokenType.COMMA);
                case ';' -> addToken(TokenType.SEMICOLON);
                case '?' -> addToken(TokenType.QUESTION);
                case ':' -> addToken(TokenType.COLON);
                case '.' -> addToken(TokenType.DOT);
                case '-' -> addToken(TokenType.MINUS);
                case '+' -> addToken(TokenType.PLUS);
                case '*' -> addToken(TokenType.STAR);
                case '%' -> addToken(TokenType.PERCENT);
                case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                case '&' -> { if (match('&')) addToken(TokenType.AND); else throw error("Expected '&&'"); }
                case '|' -> { if (match('|')) addToken(TokenType.OR); else throw error("Expected '||'"); }
                case '\'' -> {
                    while (peek() != '\'' && !isAtEnd()) advance();
                    if (isAtEnd()) throw error("Unterminated string");
                    advance();
                    String value = source.substring(start + 1, pos - 1);
                    addToken(TokenType.STRING, value);
                }
                default -> {
                    if (isDigit(c)) {
                        while (isDigit(peek())) advance();
                        if (peek() == '.' && isDigit(peekNext())) {
                            advance();
                            while (isDigit(peek())) advance();
                        }
                        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, pos)));
                    } else if (isAlpha(c)) {
                        while (isAlphaNumeric(peek())) advance();
                        String text = source.substring(start, pos);
                        switch (text) {
                            case "true" -> addToken(TokenType.NUMBER, 1.0);
                            case "false" -> addToken(TokenType.NUMBER, 0.0);
                            case "pi" -> addToken(TokenType.NUMBER, Math.PI);
                            case "e" -> addToken(TokenType.NUMBER, Math.E);
                            default -> addToken(TokenType.IDENTIFIER, text);
                        }
                    } else {
                        throw error("Unexpected character: " + c);
                    }
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", pos, pos));
        return tokens;
    }

    private char advance() {
        return source.charAt(pos++);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(pos) != expected) return false;
        pos++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private char peekNext() {
        return pos + 1 >= source.length() ? '\0' : source.charAt(pos + 1);
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, null, start, pos));
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, literal, start, pos));
    }

    private RuntimeException error(String message) {
        throw new MolangParseException(message + " at position " + start);
    }

    public record Token(TokenType type, Object literal, int start, int end) {
        @Override
        public String toString() {
            return type + (literal != null ? "(" + literal + ")" : "");
        }
    }

    public enum TokenType {
        NUMBER, IDENTIFIER, STRING,
        LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
        COMMA, SEMICOLON, DOT,
        PLUS, MINUS, STAR, SLASH, PERCENT,
        EQUAL, EQUAL_EQUAL, BANG_EQUAL, BANG,
        LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
        AND, OR,
        QUESTION, COLON,
        EOF
    }

    public static class MolangParseException extends RuntimeException {
        public MolangParseException(String message) {
            super(message);
        }
    }
}
