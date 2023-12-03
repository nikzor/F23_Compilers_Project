import java.util.*;

// A class that represents the semantics analyzer for the AST of a program.
class SemanticAnalyzer {
    final List<ASTNode> astNodeList;

    public SemanticAnalyzer(List<ASTNode> astNodeList) {
        this.astNodeList = astNodeList;
    }

    private final Map<String, ASTNode> symbolTable = new HashMap<>();

    // Begins semantic analysis on the AST node list.
    public List<ASTNode> startAnalyze() {
        return analyze(astNodeList);
    }

    // Processes the analysis of the AST node list.
    private List<ASTNode> analyze(List<ASTNode> astNodeList) {
        for (int i = 0; i < astNodeList.size(); i++) {
            // Analyzes variable declarations.
            if (astNodeList.get(i) instanceof VarDeclaration varDeclaration) {
                varDeclaration.expression = checkExpression(varDeclaration.expression, i, "var");
                if (!symbolTable.containsKey(varDeclaration.variableName)) {
                    symbolTable.put(varDeclaration.variableName, varDeclaration);
                } else {
                    throw new RuntimeException("Variable already declared");
                }
                ASTNode tableNode = symbolTable.get(((VarDeclaration) astNodeList.get(i)).variableName);
                String typeName = ((TypeNode) ((VarDeclaration) tableNode).variableType).typeName;
                if ((((VarDeclaration) tableNode).expression) != null && (((VarDeclaration) tableNode).expression) instanceof LiteralNode) {
                    String expressionString = String.valueOf(((LiteralNode) (((VarDeclaration) tableNode).expression)).value);
                    if (typeName.equals("integer")) {
                        VarDeclaration node;
                        if (expressionString.contains(".")) {
                            expressionString = expressionString.substring(0, expressionString.indexOf('.'));
                            node = new VarDeclaration(((VarDeclaration) tableNode).variableName, ((VarDeclaration) tableNode).variableType, new LiteralNode(expressionString));
                            symbolTable.put(((VarDeclaration) astNodeList.get(i)).variableName, node);
                            varDeclaration.expression = node.expression;
                        }
                    }
                }

            }
            // Analyzes assignment nodes.
            else if (astNodeList.get(i) instanceof AssignmentNode assignmentNode) {
                if (assignmentNode.expression instanceof BinaryOpNode) {
                    assignmentNode.expression = checkExpression(assignmentNode.expression, i, "assignment");
                } else {
                    for (ASTNode astNode : astNodeList) {
                        if (astNode instanceof VarDeclaration && ((VarDeclaration) astNode).variableName.equals(((AssignmentNode) astNodeList.get(i)).variableName)) {
                            VarDeclaration node = new VarDeclaration(((VarDeclaration) astNode).variableName, ((VarDeclaration) astNode).variableType, new LiteralNode(String.valueOf(((LiteralNode) assignmentNode.expression).value)));
                            symbolTable.put(((AssignmentNode) astNodeList.get(i)).variableName, node);
                            break;
                        }
                    }
                }
                ASTNode tableNode = symbolTable.get(((AssignmentNode) astNodeList.get(i)).variableName);
                String typeName = ((TypeNode) ((VarDeclaration) tableNode).variableType).typeName;
                String expressionString = String.valueOf(((LiteralNode) (((VarDeclaration) tableNode).expression)).value);
                if (typeName.equals("integer")) {
                    VarDeclaration node;
                    if (expressionString.contains(".")) {
                        expressionString = expressionString.substring(0, expressionString.indexOf('.'));
                        node = new VarDeclaration(((VarDeclaration) tableNode).variableName, ((VarDeclaration) tableNode).variableType, new LiteralNode(expressionString));
                        symbolTable.put(((AssignmentNode) astNodeList.get(i)).variableName, node);
                        assignmentNode.expression = node.expression;
                    }
                }

                if (!symbolTable.containsKey(assignmentNode.variableName)) {
                    throw new RuntimeException("Variable not declared before assignment");
                }
            }
            // Analyzes routine declarations.
            else if (astNodeList.get(i) instanceof RoutineDeclarationNode routineDeclarationNode) {
                if (!symbolTable.containsKey(routineDeclarationNode.routineName)) {
                    symbolTable.put(routineDeclarationNode.routineName, routineDeclarationNode);
                }
            }
            // Analyzes for loops.
            else if (astNodeList.get(i) instanceof ForLoop forLoop) {
                String[] rangeVariables = forLoop.range.split("\\.\\.");
                if (!isNumeric(rangeVariables[0]) && !symbolTable.containsKey(rangeVariables[0])) {
                    throw new RuntimeException("Variable not declared before use");
                }
                if (!isNumeric(rangeVariables[1]) && !symbolTable.containsKey(rangeVariables[1])) {
                    throw new RuntimeException("Variable not declared before use");
                }
                if (isNumeric(rangeVariables[0]) && isDouble(rangeVariables[0]) || isNumeric(rangeVariables[0]) && isDouble(rangeVariables[0])) {
                    throw new RuntimeException("Double value cannot be used in ForLoop range");
                }
                //BlockNode loop = (BlockNode) forLoop.loopBody;
                //analyze(loop.statements);
            }
            // Analyzes while loops.
            else if (astNodeList.get(i) instanceof WhileLoop) {
                BlockNode loop = (BlockNode) ((WhileLoop) astNodeList.get(i)).loopBody;
                //analyze(loop.statements);
            }
            // Analyzes if statements.
            else if (astNodeList.get(i) instanceof IfStatementNode ifStatementNode) {
                ifStatementNode.condition = checkExpression(ifStatementNode.condition, i, "var");
                BlockNode thenBlock = (BlockNode) ((IfStatementNode) astNodeList.get(i)).thenBlock;
                BlockNode elseBlock = (BlockNode) ((IfStatementNode) astNodeList.get(i)).elseBlock;
                analyze(thenBlock.statements);
                if (elseBlock != null) {
                    analyze(elseBlock.statements);
                }
            }
        }
        return astNodeList;
    }

    // Evaluates expressions and ensures they are semantically correct.
    private ASTNode checkExpression(ASTNode expression, int i, String originalDeclaration) {
        if (expression instanceof BinaryOpNode binaryOpNode) {
            ASTNode left = checkExpression(binaryOpNode.left, i, originalDeclaration);
            ASTNode right = checkExpression(binaryOpNode.right, i, originalDeclaration);
            if (left instanceof LiteralNode && right instanceof LiteralNode) {
                if (!areTypesCompatible((LiteralNode) left, (LiteralNode) right, binaryOpNode.operator)) {
                    throw new RuntimeException("Type mismatch in binary expression");
                }
                ASTNode var = symbolTable.get(((LiteralNode) left).value);
                if (var instanceof VarDeclaration && ((VarDeclaration) var).expression instanceof LiteralNode) {
                    left = ((VarDeclaration) var).expression;
                }
                ASTNode var2 = symbolTable.get(((LiteralNode) right).value);
                if (var2 instanceof VarDeclaration && ((VarDeclaration) var2).expression instanceof LiteralNode) {
                    right = ((VarDeclaration) var2).expression;
                }
                if (isBoolean(((LiteralNode) left).value) && isBoolean(((LiteralNode) right).value)) {
                    boolean result = calculateBooleanOperation(Boolean.parseBoolean(((LiteralNode) left).value), Boolean.parseBoolean(((LiteralNode) right).value), binaryOpNode.operator);
                    return new LiteralNode(String.valueOf(result));
                }
                if (isNumeric(((LiteralNode) left).value) && isNumeric(((LiteralNode) right).value) && (binaryOpNode.operator == Token.TokenType.PLUS || binaryOpNode.operator == Token.TokenType.MINUS || binaryOpNode.operator == Token.TokenType.MULTIPLY || binaryOpNode.operator == Token.TokenType.DIVIDE)) {
                    double result = calculateBinaryOperation(Double.parseDouble(((LiteralNode) left).value), Double.parseDouble(((LiteralNode) right).value), binaryOpNode.operator);
                    if (isDouble(String.valueOf(result)) && originalDeclaration.equals("var")) {
                        symbolTable.put(((VarDeclaration) astNodeList.get(i)).variableName, (astNodeList.get(i)));
                    } else if (isDouble(String.valueOf(result)) && (originalDeclaration.equals("assignment"))) {
                        for (ASTNode astNode : astNodeList) {
                            if (astNode instanceof VarDeclaration && ((VarDeclaration) astNode).variableName.equals(((AssignmentNode) astNodeList.get(i)).variableName)) {
                                VarDeclaration node = new VarDeclaration(((VarDeclaration) astNode).variableName, ((VarDeclaration) astNode).variableType, new LiteralNode(String.valueOf(result)));
                                symbolTable.put(((AssignmentNode) astNodeList.get(i)).variableName, node);
                                break;
                            }
                        }
                    }
                    return new LiteralNode(String.valueOf(result));
                } else if (isNumeric(((LiteralNode) left).value) && isNumeric(((LiteralNode) right).value) && (binaryOpNode.operator == Token.TokenType.GREATER_THAN || binaryOpNode.operator == Token.TokenType.GREATER_THAN_OR_EQUAL || binaryOpNode.operator == Token.TokenType.LESS_THAN || binaryOpNode.operator == Token.TokenType.LESS_THAN_OR_EQUAL || binaryOpNode.operator == Token.TokenType.EQUALS)) {
                    boolean result = calculateComparisonOperation(Double.parseDouble(((LiteralNode) left).value), Double.parseDouble(((LiteralNode) right).value), binaryOpNode.operator);
                    if (isDouble(String.valueOf(result)) && (originalDeclaration.equals("assignment"))) {
                        for (ASTNode astNode : astNodeList) {
                            if (astNode instanceof VarDeclaration && ((VarDeclaration) astNode).variableName.equals(((AssignmentNode) astNodeList.get(i)).variableName)) {
                                VarDeclaration node = new VarDeclaration(((VarDeclaration) astNode).variableName, ((VarDeclaration) astNode).variableType, new LiteralNode(String.valueOf(result)));
                                symbolTable.put(((AssignmentNode) astNodeList.get(i)).variableName, node);
                                break;
                            }
                        }
                    }
                    return new LiteralNode(String.valueOf(result));
                } else if (!isNumeric(((LiteralNode) left).value) && !symbolTable.containsKey(((LiteralNode) left).value)) {
                    throw new RuntimeException("Variable not declared before use");
                } else if (!isNumeric(((LiteralNode) right).value) && !symbolTable.containsKey(((LiteralNode) right).value)) {
                    throw new RuntimeException("Variable not declared before use");
                }
            }
            return new BinaryOpNode(binaryOpNode.operator, left, right);
        }
        if (expression instanceof FunctionCallNode) {
            RoutineDeclarationNode routine = (RoutineDeclarationNode) symbolTable.get(((FunctionCallNode) expression).functionName);
            if (routine == null) {
                throw new RuntimeException("Function not declared before call");
            }
            if (routine.parameters.size() != ((FunctionCallNode) expression).arguments.size()) {
                throw new RuntimeException("The number of arguments in the function call does not match the required number");
            }
            if (astNodeList.get(i) instanceof VarDeclaration declaration) {
                if (declaration.variableType.toString().equals("boolean") && routine.returnType.toString().equals("real")) {
                    throw new RuntimeException("Cannot convert real to boolean");
                }
            }
        }


        return expression;
    }


    // Checks if the literal nodes' types are compatible based on an operator.
    private boolean areTypesCompatible(LiteralNode left, LiteralNode right, Token.TokenType operator) {
        String leftValue = left.value;
        String rightValue = right.value;
        if (symbolTable.containsKey(leftValue) || symbolTable.containsKey(rightValue)) {
            return true;
        }
        boolean isLeftNumeric = isNumeric(leftValue);
        boolean isRightNumeric = isNumeric(rightValue);
        boolean isLeftBoolean = isBoolean(leftValue);
        boolean isRightBoolean = isBoolean(rightValue);

        return switch (operator) {
            case PLUS, MINUS, MULTIPLY, DIVIDE -> isLeftNumeric && isRightNumeric;
            case EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL ->
                    (isLeftNumeric && isRightNumeric) || (leftValue.equals(rightValue));
            case AND, OR, XOR, NOT -> isLeftBoolean && isRightBoolean;
            default -> throw new RuntimeException("Unsupported operator in type compatibility check");
        };
    }

    // Performs calculations for binary operations.
    private double calculateBinaryOperation(double left, double right, Token.TokenType operator) {
        return switch (operator) {
            case PLUS -> left + right;
            case MINUS -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
            default -> throw new RuntimeException("Unsupported operator: " + operator);
        };
    }

    // Performs calculations for boolean operations.
    private boolean calculateBooleanOperation(boolean left, boolean right, Token.TokenType operator) {
        return switch (operator) {
            case AND -> left && right;
            case OR -> left || right;
            default -> throw new RuntimeException("Unsupported operator: " + operator);
        };
    }

    // Performs calculations for comparison operations.
    private boolean calculateComparisonOperation(double left, double right, Token.TokenType operator) {
        return switch (operator) {
            case EQUALS -> left == right;
            case GREATER_THAN -> left > right;
            case GREATER_THAN_OR_EQUAL -> left >= right;
            case LESS_THAN -> left < right;
            case LESS_THAN_OR_EQUAL -> left <= right;
            default -> throw new RuntimeException("Unsupported operator: " + operator);
        };
    }

    // Determines if a string is numeric.
    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Determines if a string represents a double value.
    private boolean isDouble(String num) {
        if (num == null) {
            return false;
        }
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return true;
        }
        return false;
    }

    // Determines if a string represents a boolean value.
    private boolean isBoolean(String strBool) {
        return Objects.equals(strBool, "true") || Objects.equals(strBool, "false");
    }
}
