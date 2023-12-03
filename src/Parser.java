import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


// Represents the base class for all AST nodes
abstract class ASTNode {
}

// Represents a variable declaration
class VarDeclaration extends ASTNode {
    String variableName;
    ASTNode variableType;
    ASTNode expression;

    public VarDeclaration(String variableName, ASTNode variableType, ASTNode expression) {
        this.variableName = variableName;
        this.variableType = variableType;
        this.expression = expression;
    }
}

// Represents a type declaration
class TypeNode extends ASTNode {
    String typeName;

    public TypeNode(String typeName) {
        this.typeName = typeName;
    }
}

// Represents an if statement
class IfStatementNode extends ASTNode {
    ASTNode condition;
    ASTNode thenBlock;
    ASTNode elseBlock;

    public IfStatementNode(ASTNode condition, ASTNode thenBlock, ASTNode elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
}

// Represents a for loop
class ForLoop extends ASTNode {
    String loopVariable;
    boolean isReverse;
    String range;
    ASTNode loopBody;

    public ForLoop(String loopVariable, boolean isReverse, String range, ASTNode loopBody) {
        this.loopVariable = loopVariable;
        this.isReverse = isReverse;
        this.range = range;
        this.loopBody = loopBody;
    }
}

// Represents a while loop
class WhileLoop extends ASTNode {
    ASTNode condition;
    ASTNode loopBody;

    public WhileLoop(ASTNode condition, ASTNode loopBody) {
        this.condition = condition;
        this.loopBody = loopBody;
    }
}
// Represents a block for conditional operator
class BlockNode extends ASTNode {
    List<ASTNode> statements;

    public BlockNode(List<ASTNode> statements) {
        this.statements = statements;
    }
}

// Represents a node for binary operations (e.g., +, -, *, /, %)
class BinaryOpNode extends ASTNode {
    Token.TokenType operator;
    ASTNode left;
    ASTNode right;

    public BinaryOpNode(Token.TokenType operator, ASTNode left, ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }
}

// Represents a node for unary operations (e.g., unary +, unary -)
class UnaryOpNode extends ASTNode {
    Token.TokenType operator;
    ASTNode operand;

    public UnaryOpNode(Token.TokenType operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }
}

// Represents a node for literal values and identifiers
class LiteralNode extends ASTNode {
    String value;

    public LiteralNode(String value) {
        this.value = value;
    }
}

// Represents an assignment statement
class AssignmentNode extends ASTNode {
    String variableName;
    ASTNode expression;

    public AssignmentNode(String variableName, ASTNode expression) {
        this.variableName = variableName;
        this.expression = expression;
    }
}

// Represents a parameter in a routine
class ParameterNode extends ASTNode {
    String paramName;
    ASTNode paramType;

    public ParameterNode(String paramName, ASTNode paramType) {
        this.paramName = paramName;
        this.paramType = paramType;
    }
}

// Represents the body of a routine
class RoutineBodyNode extends ASTNode {
    List<ASTNode> statements;

    public RoutineBodyNode(List<ASTNode> statements) {
        this.statements = statements;
    }
}

// Represents a return statement
class ReturnStatementNode extends ASTNode {
    ASTNode returnValue;

    public ReturnStatementNode(ASTNode returnValue) {
        this.returnValue = returnValue;
    }
}

// Represents a routine declaration
class RoutineDeclarationNode extends ASTNode {
    String routineName;
    List<ParameterNode> parameters;
    ASTNode returnType;
    RoutineBodyNode routineBody;

    public RoutineDeclarationNode(String routineName, List<ParameterNode> parameters, ASTNode returnType, RoutineBodyNode routineBody) {
        this.routineName = routineName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.routineBody = routineBody;
    }
}

// Represents a function call
class FunctionCallNode extends ASTNode {
    String functionName;
    List<ASTNode> arguments;

    public FunctionCallNode(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }
}

// Represents a type declaration
class TypeDeclarationNode extends ASTNode {
    String typeName;
    ASTNode typeDefinition;

    public TypeDeclarationNode(String typeName, ASTNode typeDefinition) {
        this.typeName = typeName;
        this.typeDefinition = typeDefinition;
    }
}

// Represents an array type
class ArrayTypeNode extends ASTNode {
    ASTNode size;
    ASTNode elementType;

    public ArrayTypeNode(ASTNode size, ASTNode elementType) {
        this.size = size;
        this.elementType = elementType;
    }
}

// Represents an array values
class ArrayValuesNode extends ASTNode {
    List<ASTNode> values;

    public ArrayValuesNode(List<ASTNode> values) {
        this.values = values;
    }
}


class Parser {
    final private List<Token> tokens;
    private int currentTokenIndex;

    // Constructor for Parser
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    // Parse the entire program
    // iteratively until the end of tokens is reached, building the AST nodes for the program
    public List<ASTNode> parse() {
        List<ASTNode> program = new ArrayList<>();
        while (currentTokenIndex < tokens.size()) {
            program.add(parseSimpleDeclaration());
        }
        return program;
    }

    // Helper method to parse simple declarations (variables, loops, etc.).
    private ASTNode parseSimpleDeclaration() {
        if (match(Token.TokenType.VAR)) {
            String variableName = consume(Token.TokenType.IDENTIFIER, "Expect variable name after 'var'");
            consume(Token.TokenType.COLON, "Expect ':' after variable name");
            ASTNode variableType = parseType();
            currentTokenIndex++;
            ASTNode expression = null;
            if (match(Token.TokenType.IS)) {
                // Check if it's a function call
                if (tokens.get(currentTokenIndex).type == Token.TokenType.IDENTIFIER &&
                        tokens.get(currentTokenIndex + 1).type == Token.TokenType.LPAREN) {
                    expression = parseFunctionCall();
                } else if (tokens.get(currentTokenIndex).type == Token.TokenType.LBRACKET) {
                    expression = parseArrayValues();
                } else {
                    expression = parseExpression();
                }
            }
            consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");
            return new VarDeclaration(variableName, variableType, expression);
        } else if (match(Token.TokenType.FOR)) {
            return parseForLoop();
        } else if (match(Token.TokenType.WHILE)) {
            return parseWhileLoop();
        } else if (match(Token.TokenType.IF)) {
            return parseIfStatement();
        } else if (match(Token.TokenType.ROUTINE)) {
            return parseRoutineDeclaration();
        } else if (match(Token.TokenType.TYPE)) {
            return parseTypeDeclaration();
        } else if (tokens.get(currentTokenIndex).type.equals(Token.TokenType.IDENTIFIER)) {
            return parseAssignment();
        } else if (match(Token.TokenType.RETURN)) {
            return parseReturnStatement();
        } else {
            throw new RuntimeException("Invalid statement");
        }
    }

    // Helper method to parse a type declaration
    private TypeDeclarationNode parseTypeDeclaration() {
        if (match(Token.TokenType.IDENTIFIER)) {
            String typeName = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.IS, "Expect 'is' after type name");

            // Parse the type definition
            ASTNode typeDefinition = parseType();

            currentTokenIndex++;

            consume(Token.TokenType.SEMICOLON, "Expect ';' after type declaration");

            return new TypeDeclarationNode(typeName, typeDefinition);
        } else {
            throw new RuntimeException("Expect type name after 'type'");
        }
    }

    // Helper method to parse a function call
    private FunctionCallNode parseFunctionCall() {
        if (match(Token.TokenType.IDENTIFIER)) {
            String functionName = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.LPAREN, "Expect '(' after function name");

            // Parse the arguments
            List<ASTNode> arguments = new ArrayList<>();
            arguments.add(parseExpression());

            while (match(Token.TokenType.COMMA)) {
                arguments.add(parseExpression());
            }

            consume(Token.TokenType.RPAREN, "Expect ')' after function arguments");

            return new FunctionCallNode(functionName, arguments);
        } else {
            throw new RuntimeException("Expect function name after '('");
        }
    }

    // Helper method to parse a type
    private ASTNode parseType() {
        if (tokens.get(currentTokenIndex).type.equals(Token.TokenType.PRIMITIVE_TYPE) || tokens.get(currentTokenIndex).type.equals(Token.TokenType.IDENTIFIER)) {
            return new TypeNode(tokens.get(currentTokenIndex).value);
        } else if (match(Token.TokenType.IDENTIFIER)) {
            currentTokenIndex++;
            return new TypeNode(tokens.get(currentTokenIndex).value);
        } else if (match(Token.TokenType.ARRAY)) {
            return parseArrayType();
        } else {
            throw new RuntimeException("Expect valid type");
        }
    }

    // Helper method to parse an array type
    private ArrayTypeNode parseArrayType() {
        consume(Token.TokenType.LBRACKET, "Expect '['");

        ASTNode size = parseExpression();

        consume(Token.TokenType.RBRACKET, "Expect ']'");

        ASTNode elementType = parseType();

        return new ArrayTypeNode(size, elementType);
    }

    // Helper method to parse an array of values
    private ASTNode parseArrayValues() {
        consume(Token.TokenType.LBRACKET, "Expect '[' for array values");

        List<ASTNode> values = new ArrayList<>();

        // Parse the first element
        values.add(parseExpression());

        while (match(Token.TokenType.COMMA)) {
            // Parse and add the next element
            values.add(parseExpression());
        }

        consume(Token.TokenType.RBRACKET, "Expect ']' for array values");

        return new ArrayValuesNode(values);
    }

    // Helper method to parse a routine declaration
    private ASTNode parseRoutineDeclaration() {
        if (match(Token.TokenType.IDENTIFIER)) {
            String routineName = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.LPAREN, "Expect '(' after routine name");

            // Parse parameters
            List<ParameterNode> parameters = parseParameters();

            consume(Token.TokenType.RPAREN, "Expect ')' after parameters");

            // Check for return type
            ASTNode returnType = null;
            if (match(Token.TokenType.COLON)) {
                // Parse the return type
                returnType = parseType();
                currentTokenIndex++;
            }

            consume(Token.TokenType.IS, "Expect 'is' after routine declaration");

            // Parse the routine body
            RoutineBodyNode routineBody = parseRoutineBody();

            consume(Token.TokenType.END, "Expect 'end' at the end of the routine declaration");
            consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");

            return new RoutineDeclarationNode(routineName, parameters, returnType, routineBody);
        } else {
            throw new RuntimeException("Expect routine name after 'routine'");
        }
    }

    // Helper method to parse a list of parameters
    private List<ParameterNode> parseParameters() {
        List<ParameterNode> parameters = new ArrayList<>();

        // Parse the first parameter
        parameters.add(parseParameterDeclaration());

        while (match(Token.TokenType.COMMA)) {
            // Parse subsequent parameters
            parameters.add(parseParameterDeclaration());
        }

        return parameters;
    }

    // Helper method to parse a parameter declaration
    private ParameterNode parseParameterDeclaration() {
        if (match(Token.TokenType.IDENTIFIER)) {
            String paramName = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.COLON, "Expect ':' after parameter name");

            // Parse the parameter type
            ASTNode paramType = parseType();
            currentTokenIndex++;
            // Return a ParameterNode representing the parameter declaration
            return new ParameterNode(paramName, paramType);
        } else {
            throw new RuntimeException("Expect parameter name");
        }
    }

    // Helper method to parse the body of a routine
    private RoutineBodyNode parseRoutineBody() {
        List<ASTNode> statements = new ArrayList<>();

        while (currentTokenIndex < tokens.size() && !(tokens.get(currentTokenIndex).type.equals(Token.TokenType.END))) {
            // Parse statements within the routine body and add them to the statements list
            statements.add(parseStatement());
        }
        return new RoutineBodyNode(statements);
    }

    // Helper method to parse a statement
    private ASTNode parseStatement() {
        if (match(Token.TokenType.RETURN)) {
            return parseReturnStatement();
        } else {
            return parseSimpleDeclaration();
        }
    }

    // Helper method to parse a return statement
    private ASTNode parseReturnStatement() {
        // Parse the expression that is being returned
        ASTNode returnValue = parseExpression();

        consume(Token.TokenType.SEMICOLON, "Expect ';' at the end of the return statement");

        return new ReturnStatementNode(returnValue);
    }

    // Helper method to parse a for loop
    private ASTNode parseForLoop() {
        if (match(Token.TokenType.IDENTIFIER)) {
            String loopVariable = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.IN, "Expect 'in' after loop variable");

            boolean isReverse = false;
            if (tokens.get(currentTokenIndex).type.equals(Token.TokenType.REVERSE)) {
                currentTokenIndex++;
                isReverse = true;
            }

            // Parse the start and end expressions
            String range = consume(Token.TokenType.RANGE, "Expect '..' in range specification");

            consume(Token.TokenType.LOOP, "Expect 'loop' after the range specification");

            // Parse the body of the for loop
            ASTNode loopBody = parseBody();

            consume(Token.TokenType.END, "Expect 'end' at the end of the for loop");
            consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");

            return new ForLoop(loopVariable, isReverse, range, loopBody);
        } else {
            throw new RuntimeException("Expect an identifier for the loop variable");
        }
    }

    // Helper method to parse the body of a loop
    private ASTNode parseBody() {
        List<ASTNode> statements = new ArrayList<>();

        while (currentTokenIndex < tokens.size() &&
                !(tokens.get(currentTokenIndex).type.equals(Token.TokenType.END)) &&
                !(tokens.get(currentTokenIndex).type.equals(Token.TokenType.ELSE))) {
            // Parse simple declarations within the block and add them to the statements list
            statements.add(parseSimpleDeclaration());
        }

        return new BlockNode(statements);
    }

    // Helper method to parse a while loop
    private ASTNode parseWhileLoop() {
        ASTNode condition = parseExpression();

        consume(Token.TokenType.LOOP, "Expect 'loop' after the loop condition");

        // Parse the body of the while loop
        ASTNode loopBody = parseBody();

        consume(Token.TokenType.END, "Expect 'end' at the end of the while loop");
        consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");
        return new WhileLoop(condition, loopBody);
    }

    // Helper method to parse an assignment statement
    private ASTNode parseAssignment() {
        // Ensure the current token is an identifier (the left-hand side of the assignment)
        if (match(Token.TokenType.IDENTIFIER)) {
            String variableName = tokens.get(currentTokenIndex - 1).value;

            consume(Token.TokenType.ASSIGN, "Expect ':=' for assignment");

            // Parse the right-hand side expression

            ASTNode expression;
            if (tokens.get(currentTokenIndex).type == Token.TokenType.IDENTIFIER &&
                    tokens.get(currentTokenIndex + 1).type == Token.TokenType.LPAREN) {
                // Parse and return a FunctionCallNode
                expression = parseFunctionCall();
            } else {
                expression = parseExpression();
            }
            consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");
            return new AssignmentNode(variableName, expression);
        } else {
            throw new RuntimeException("Expect an identifier for assignment");
        }
    }

    // Helper method to parse an if statement
    private ASTNode parseIfStatement() {
        // Parse the condition expression
        ASTNode condition = parseExpression();

        consume(Token.TokenType.THEN, "Expect 'then' after the condition");

        // Parse the "then" block
        ASTNode thenBlock = parseBody();

        ASTNode elseBlock = null;

        if (tokens.get(currentTokenIndex).type.equals(Token.TokenType.ELSE)) {
            currentTokenIndex++;
            // Parse the "else" block
            elseBlock = parseBody();
        }

        consume(Token.TokenType.END, "Expect 'end' at the end of the if statement");
        consume(Token.TokenType.SEMICOLON, "Expect ';' after declaration or assignment");
        return new IfStatementNode(condition, thenBlock, elseBlock);
    }

    // Helper method to parse an expression
    // Helper method to parse an expression using the Shunting Yard algorithm
    private ASTNode parseExpression() {
        Stack<Token.TokenType> operatorStack = new Stack<>();
        Stack<ASTNode> outputQueue = new Stack<>();

        while (!isEndOfExpression()) {
            if (match(Token.TokenType.INTEGER_LITERAL) ||
                    match(Token.TokenType.REAL_LITERAL) ||
                    match(Token.TokenType.TRUE) ||
                    match(Token.TokenType.FALSE) ||
                    match(Token.TokenType.IDENTIFIER)) {
                outputQueue.add(new LiteralNode(tokens.get(currentTokenIndex - 1).value));
            } else if (match(Token.TokenType.LPAREN)) {
                operatorStack.push(Token.TokenType.LPAREN);
            } else if (match(Token.TokenType.RPAREN)) {
                if (operatorStack.isEmpty()) {
                    currentTokenIndex--;
                    break;
                }
                while (!operatorStack.isEmpty() && operatorStack.peek() != Token.TokenType.LPAREN) {
                    outputQueue.push(createBinaryOpNode(operatorStack.pop(), outputQueue.pop(), outputQueue.pop()));
                }
                operatorStack.pop(); // Discard the left parenthesis
            } else if (isOperator()) {
                Token.TokenType currentOperator = tokens.get(currentTokenIndex - 1).type;
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), currentOperator)) {
                    pushOperator(outputQueue, operatorStack.pop());
                }
                operatorStack.push(currentOperator);
            } else {
                break;
            }
        }

        while (!operatorStack.isEmpty()) {
            outputQueue.push(createBinaryOpNode(operatorStack.pop(), outputQueue.pop(), outputQueue.pop()));
        }

        if (!outputQueue.isEmpty()) {
            return outputQueue.pop();
        }

        throw new RuntimeException("Invalid expression");
    }

    private void pushOperator(List<ASTNode> outputQueue, Token.TokenType operator) {
        if (outputQueue.size() < 2) {
            throw new RuntimeException("Invalid expression");
        }
        ASTNode right = outputQueue.remove(outputQueue.size() - 1);
        ASTNode left = outputQueue.remove(outputQueue.size() - 1);
        outputQueue.add(new BinaryOpNode(operator, left, right));}

    // Helper method to check if the current token is the end of the expression
    private boolean isEndOfExpression() {
        return currentTokenIndex >= tokens.size() ||
                tokens.get(currentTokenIndex).type == Token.TokenType.SEMICOLON ||
                tokens.get(currentTokenIndex).type == Token.TokenType.COMMA;
    }

    // Helper method to check if the current token is an operator
    private boolean isOperator() {
        return match(Token.TokenType.PLUS) ||
                match(Token.TokenType.MINUS) ||
                match(Token.TokenType.MULTIPLY) ||
                match(Token.TokenType.DIVIDE) ||
                match(Token.TokenType.REMAINDER) ||
                match(Token.TokenType.LESS_THAN) ||
                match(Token.TokenType.LESS_THAN_OR_EQUAL) ||
                match(Token.TokenType.GREATER_THAN) ||
                match(Token.TokenType.GREATER_THAN_OR_EQUAL) ||
                match(Token.TokenType.EQUALS) ||
                match(Token.TokenType.NOT_EQUALS) ||
                match(Token.TokenType.AND) ||
                match(Token.TokenType.OR) ||
                match(Token.TokenType.XOR);
    }

    // Helper method to check if the first operator has higher precedence than the second operator
    private boolean hasHigherPrecedence(Token.TokenType operator1, Token.TokenType operator2) {
        return getPrecedence(operator1) > getPrecedence(operator2);
    }

    // Helper method to get the precedence of an operator
    private int getPrecedence(Token.TokenType operator) {
        return switch (operator) {
            case PLUS, MINUS -> 1;
            case MULTIPLY, DIVIDE, REMAINDER -> 2;
            case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, EQUALS, NOT_EQUALS -> 3;
            case AND -> 4;
            case OR -> 5;
            case XOR -> 6;
            default -> 0;
        };
    }

    // Helper method to create a BinaryOpNode with the specified operator and operands
    private ASTNode createBinaryOpNode(Token.TokenType operator, ASTNode right, ASTNode left) {
        return new BinaryOpNode(operator, left, right);
    }

    // Helper method to check if the current token matches a given type
    private boolean match(Token.TokenType type) {
        // If there is a match, it advances the token index and returns true
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).type == type) {
            currentTokenIndex++;
            return true;
        }
        // If there is no match, it returns false without advancing the token index
        return false;
    }

    // Helper method to consume a token of a specific type or throw an exception
    private String consume(Token.TokenType token, String message) {
        // If there's a match, it returns the value of the consumed token
        if (match(token)) {
            return tokens.get(currentTokenIndex - 1).value;
        }
        // If there's no match, it throws a runtime exception with the error message
        System.out.println(message + " " + currentTokenIndex);
        throw new RuntimeException(message);
    }
}
