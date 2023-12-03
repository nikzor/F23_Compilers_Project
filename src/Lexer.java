import java.util.*;

class Token {
    public enum TokenType {
        IDENTIFIER, INTEGER_LITERAL, REAL_LITERAL, BOOLEAN_LITERAL, RECORD, PRIMITIVE_TYPE,
        VAR, TYPE, ROUTINE, IS, END, WHILE, LOOP, FOR, IN, REVERSE, IF, THEN, ELSE, TRUE, FALSE,
        NOT, AND, OR, XOR, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,
        ASSIGN, EQUALS, RANGE,
        PLUS, MINUS, MULTIPLY, DIVIDE, REMAINDER,
        LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, COMMA, SEMICOLON, COLON,
        RETURN, PRINTLN, ARRAY
    }

    public TokenType type;
    public String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}

class Lexer {
    private final String input;
    private int position;
    private final List<Token> tokens;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (Character.isWhitespace(currentChar)) {
                // Skip whitespace
                position++;
            } else if (Character.isDigit(currentChar)) {
                tokenizeNumber();
            } else if (Character.isLetter(currentChar) || currentChar == '_') {
                tokenizeIdentifierOrKeyword();
            } else {
                tokenizeOperatorOrSymbol();
            }
        }

        return tokens;
    }

    private void tokenizeNumber() {
        int start = position;
        while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
            position++;
        }
        String numberStr = input.substring(start, position);

        // Determine if it's an integer or real literal
        if (numberStr.contains("..")) {
            tokens.add(new Token(Token.TokenType.RANGE, numberStr));
        } else if (numberStr.contains(".")) {
            tokens.add(new Token(Token.TokenType.REAL_LITERAL, numberStr));
        } else {
            tokens.add(new Token(Token.TokenType.INTEGER_LITERAL, numberStr));
        }
    }

    private void tokenizeIdentifierOrKeyword() {
        int start = position;
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            position++;
        }
        String identifier = input.substring(start, position);

        // Check if it's a keyword
        switch (identifier) {
            case "var" -> tokens.add(new Token(Token.TokenType.VAR, identifier));
            case "type" -> tokens.add(new Token(Token.TokenType.TYPE, identifier));
            case "routine" -> tokens.add(new Token(Token.TokenType.ROUTINE, identifier));
            case "is" -> tokens.add(new Token(Token.TokenType.IS, identifier));
            case "end" -> tokens.add(new Token(Token.TokenType.END, identifier));
            case "while" -> tokens.add(new Token(Token.TokenType.WHILE, identifier));
            case "loop" -> tokens.add(new Token(Token.TokenType.LOOP, identifier));
            case "array" -> tokens.add(new Token(Token.TokenType.ARRAY, identifier));
            case "for" -> tokens.add(new Token(Token.TokenType.FOR, identifier));
            case "in" -> tokens.add(new Token(Token.TokenType.IN, identifier));
            case "reverse" -> tokens.add(new Token(Token.TokenType.REVERSE, identifier));
            case "if" -> tokens.add(new Token(Token.TokenType.IF, identifier));
            case "then" -> tokens.add(new Token(Token.TokenType.THEN, identifier));
            case "else" -> tokens.add(new Token(Token.TokenType.ELSE, identifier));
            case "record" -> tokens.add(new Token(Token.TokenType.RECORD, identifier));
            case "true" -> tokens.add(new Token(Token.TokenType.TRUE, "true"));
            case "false" -> tokens.add(new Token(Token.TokenType.FALSE, "false"));
            case "not" -> tokens.add(new Token(Token.TokenType.NOT, "not"));
            case "and" -> tokens.add(new Token(Token.TokenType.AND, "and"));
            case "or" -> tokens.add(new Token(Token.TokenType.OR, "or"));
            case "xor" -> tokens.add(new Token(Token.TokenType.XOR, "xor"));
            case "return" -> tokens.add(new Token(Token.TokenType.RETURN, "return"));
            case "println" -> tokens.add(new Token(Token.TokenType.PRINTLN, "println"));
            case "integer" -> tokens.add(new Token(Token.TokenType.PRIMITIVE_TYPE, "integer"));
            case "real" -> tokens.add(new Token(Token.TokenType.PRIMITIVE_TYPE, "real"));
            case "boolean" -> tokens.add(new Token(Token.TokenType.PRIMITIVE_TYPE, "boolean"));
            default -> {
                String check = input.substring(position, position+2);
                if (check.equals("..")){
                    while (position < input.length() && !(input.charAt(position)==' ')) {
                        position++;
                    }
                    tokens.add(new Token(Token.TokenType.RANGE, input.substring(start, position)));
                }
                else {
                    tokens.add(new Token(Token.TokenType.IDENTIFIER, identifier));
                }
            }
        }
    }

    private void tokenizeOperatorOrSymbol() {
        char currentChar = input.charAt(position);
        switch (currentChar) {
            case '+' -> tokens.add(new Token(Token.TokenType.PLUS, "+"));
            case '-' -> tokens.add(new Token(Token.TokenType.MINUS, "-"));
            case '*' -> tokens.add(new Token(Token.TokenType.MULTIPLY, "*"));
            case '/' -> tokens.add(new Token(Token.TokenType.DIVIDE, "/"));
            case '%' -> tokens.add(new Token(Token.TokenType.REMAINDER, "%"));
            case '=' -> {
                // Check for "==" as the equals operator
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(Token.TokenType.EQUALS, "=="));
                    position++; // Consume the second '='
                } else {
                    // Treat '=' as an error
                    System.err.println("Error: Unrecognized character '=' at position " + position);
                }
            }
            case '!' -> {
                // Check for "!=" as not equals
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(Token.TokenType.NOT_EQUALS, "!="));
                    position++; // Consume the second '='
                } else {
                    // Treat '!' as an error
                    System.err.println("Error: Unrecognized character '!' at position " + position);
                }
            }
            case '<' -> {
                // Check for "<=" as less than or equal
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(Token.TokenType.LESS_THAN_OR_EQUAL, "<="));
                    position++; // Consume the '='
                } else {
                    tokens.add(new Token(Token.TokenType.LESS_THAN, "<"));
                }
            }
            case '>' -> {
                // Check for ">=" as greater than or equal
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(Token.TokenType.GREATER_THAN_OR_EQUAL, ">="));
                    position++; // Consume the '='
                } else {
                    tokens.add(new Token(Token.TokenType.GREATER_THAN, ">"));
                }
            }
            // Implement other operators and symbols
            case '(' -> tokens.add(new Token(Token.TokenType.LPAREN, "("));
            case ')' -> tokens.add(new Token(Token.TokenType.RPAREN, ")"));
            case '[' -> tokens.add(new Token(Token.TokenType.LBRACKET, "["));
            case ']' -> tokens.add(new Token(Token.TokenType.RBRACKET, "]"));
            case '{' -> tokens.add(new Token(Token.TokenType.LBRACE, "{"));
            case '}' -> tokens.add(new Token(Token.TokenType.RBRACE, "}"));
            case ',' -> tokens.add(new Token(Token.TokenType.COMMA, ","));
            case ';' -> tokens.add(new Token(Token.TokenType.SEMICOLON, ";"));
            case ':' -> {
                // Check for ":=" as assign
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(Token.TokenType.ASSIGN, ":="));
                    position++; // Consume the '='
                } else {
                    tokens.add(new Token(Token.TokenType.COLON, ":"));
                }
            }
            case '.' -> {
                // Check for '..' as the interval operator in a for loop
                if (position + 1 < input.length() && input.charAt(position + 1) == '.') {
                    tokens.add(new Token(Token.TokenType.RANGE, ".."));
                    position++; // Consume the second '.'
                } else {
                    // Treat a single '.' as an error
                    System.err.println("Error: Unrecognized character '.' at position " + position);
                }
            }      // Handle unrecognized characters as an error
            default -> {
                // Handle unrecognized characters as an error
                System.err.println("Error: Unrecognized character '" + currentChar + "' at position " + position);
                position++;
            }
        }
        position++;
    }
}