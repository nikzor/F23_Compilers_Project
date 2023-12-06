import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

class CodeGenerator {
    private final List<ASTNode> astNodes;

    public CodeGenerator(List<ASTNode> astNodes) {
        this.astNodes = astNodes;
    }

    Set<VarDeclaration> declaredVariables = new HashSet<>();

    public void generateCode(String outputFileName) {
        // Collect variable declarations
        for (ASTNode node : astNodes) {
            if (node instanceof VarDeclaration) {
                declaredVariables.add(((VarDeclaration) node));
            }
        }

        try (FileWriter writer = new FileWriter(outputFileName)) {
            // Generate Jasmin file header
            writer.write(".class public GeneratedClass\n");
            writer.write(".super java/lang/Object\n\n");

            // Generate fields for variable declarations
            for (VarDeclaration varDeclaration : declaredVariables) {
                writer.write(".field static " + varDeclaration.variableName + " " + getVarType((TypeNode) varDeclaration.variableType) + "\n");
            }

            // Generate main method
            writer.write(".method public static main([Ljava/lang/String;)V\n");
            writer.write(".limit stack 100\n");
            writer.write(".limit locals 100\n\n");

            // Generate code for each AST node
            for (ASTNode node : astNodes) {
                generateCodeForNode(node, writer);
            }

            // Generate Jasmin file footer
            writer.write("return\n");
            writer.write(".end method\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCodeForNode(ASTNode node, FileWriter writer) throws IOException {
        if (node instanceof AssignmentNode assignmentNode) {
            if (assignmentNode.expression instanceof LiteralNode value) {
                if (value.value == "true") {
                    writer.write("ldc 1 \n");
                } else if (value.value == "false") {
                    writer.write("ldc 0 \n");
                } else {
                    writer.write("ldc " + value.value + " \n");
                }
                writer.write("putstatic GeneratedClass/" + assignmentNode.variableName + " " + getVarTypeFromVarName(assignmentNode.variableName) + "\n");
            } else if (assignmentNode.expression instanceof BinaryOpNode binaryNode) {
                if (binaryNode.left instanceof LiteralNode left && isNumeric(left.value)) {
                    writer.write("ldc " + left.value + " \n");
                } else if (binaryNode.left instanceof LiteralNode left) {
                    writer.write("getstatic GeneratedClass/" + left.value + " " + getVarTypeFromVarName(left.value) + "\n");
                }
                if (binaryNode.right instanceof LiteralNode right && isNumeric(right.value)) {
                    writer.write("ldc " + right.value + " \n");
                } else if (binaryNode.right instanceof LiteralNode right) {
                    writer.write("getstatic GeneratedClass/" + right.value + " " + getVarTypeFromVarName(right.value) + "\n");
                }
                switch (binaryNode.operator) {
                    case PLUS:
                        writer.write("iadd\n"); // Integer addition
                        break;
                    case MINUS:
                        writer.write("isub\n"); // Integer subtraction
                        break;
                    case MULTIPLY:
                        writer.write("imul\n"); // Integer multiplication
                        break;
                    case DIVIDE:
                        writer.write("idiv\n"); // Integer division
                        break;
                    // Other operations as necessary, e.g., comparisons, logical and/or, etc.
                    default:
                        throw new UnsupportedOperationException("Unsupported binary operator: " + binaryNode.operator);
                }
                writer.write("putstatic GeneratedClass/" + assignmentNode.variableName + " " + getVarTypeFromVarName(assignmentNode.variableName) + "\n");
            }
        } else if (node instanceof VarDeclaration varNode && varNode.expression != null) {
            if (varNode.expression instanceof LiteralNode literalNode && ((TypeNode) varNode.variableType).typeName.equals("boolean")) {
                if (literalNode.value.equals("true")) {
                    writer.write("ldc 1 \n");
                    writer.write("putstatic GeneratedClass/" + varNode.variableName + " " + getVarType((TypeNode) varNode.variableType) + "\n");
                } else {
                    writer.write("ldc 0 \n");
                    writer.write("putstatic GeneratedClass/" + varNode.variableName + " " + getVarType((TypeNode) varNode.variableType) + "\n");
                }
            } else {
                writer.write("ldc " + ((LiteralNode) varNode.expression).value + " \n");
                writer.write("putstatic GeneratedClass/" + varNode.variableName + " " + getVarType((TypeNode) varNode.variableType) + "\n");
            }
        } else if (node instanceof IfStatementNode ifStatementNode) {
            String endLabel = "LabelEnd" + uniqueLabelIndex();
            String elseLabel = ifStatementNode.elseBlock != null ? "LabelElse" + uniqueLabelIndex() : endLabel;

            // First, evaluate the condition and branch to elseLabel if the condition is false
            generateCodeForIfCondition(ifStatementNode.condition, writer, elseLabel);

            // Generate code for the 'then' block
            if (ifStatementNode.thenBlock instanceof BlockNode thenBlock) {
                for (ASTNode statement : thenBlock.statements) {
                    generateCodeForNode(statement, writer);
                }
            }

            // If there is an else block, jump to the end label after the then block
            if (ifStatementNode.elseBlock != null) {
                writer.write("goto " + endLabel + "\n");  // Skip else block if 'then' block is executed
                writer.write(elseLabel + ":\n");  // Start of else block

                // Generate code for the else block
                if (ifStatementNode.elseBlock instanceof BlockNode elseBlock) {
                    for (ASTNode statement : elseBlock.statements) {
                        generateCodeForNode(statement, writer);
                    }
                }
            }

            // Mark the end of the if statement
            writer.write(endLabel + ":\n");
        } else if (node instanceof WhileLoop whileLoop) {
            String startLabel = "WhileStart" + uniqueLabelIndex();
            String endLabel = "WhileEnd" + uniqueLabelIndex();

            // Label for the start of the loop
            writer.write(startLabel + ":\n");

            // Generate code for evaluating the loop condition
            generateCodeForWhileCondition(whileLoop.condition, writer, endLabel); // This needs to handle jumping to endLabel if condition is false

            // Generate code for the loop body
            if (whileLoop.loopBody instanceof BlockNode loopBlock) {
                for (ASTNode statement : loopBlock.statements) {
                    generateCodeForNode(statement, writer);
                }
            }

            // Unconditional jump back to the beginning to reevaluate the condition
            writer.write("goto " + startLabel + "\n");

            // Label for the end of the loop
            writer.write(endLabel + ":\n");
        } else if (node instanceof ForLoop forLoopNode) {
            // Here you may want to add more complex logic to deal with variable names and indices.
            int loopVarIndex = 1; // Assuming `i` is at index 1 for the sake of this example.
            int startValue = Integer.parseInt(forLoopNode.range.split("\\.\\.")[0]);
            int endValue = Integer.parseInt(forLoopNode.range.split("\\.\\.")[1]);

            String loopStartLabel = "ForLoopStart" + uniqueLabelIndex();
            String loopEndLabel = "ForLoopEnd" + uniqueLabelIndex();

            // Initialize loop variable to start value
            writer.write("ldc " + startValue + "\n");
            writer.write("istore " + loopVarIndex + "\n");

            // Label for the start of the loop
            writer.write(loopStartLabel + ":\n");

            // Load loop variable and compare with end value
            writer.write("iload " + loopVarIndex + "\n");
            writer.write("ldc " + endValue + "\n");
            writer.write("if_icmpgt " + loopEndLabel + "\n");

//            // Generate code for the loop body
//            if (forLoopNode.loopBody instanceof BlockNode loopBlock) {
//                for (ASTNode statement : loopBlock.statements) {
//                    generateCodeForNode(statement, writer);
//                }
//            }

            // Loop body: Increment 'a' by the loop index 'i'
            writer.write("getstatic GeneratedClass/a I\n");
            writer.write("iload " + loopVarIndex + "\n");
            writer.write("iadd\n");
            writer.write("putstatic GeneratedClass/a I\n");

            // Increment loop variable
            writer.write("iinc " + loopVarIndex + " 1\n");

            // Jump back to the loop start
            writer.write("goto " + loopStartLabel + "\n");

            // Label for the end of the loop
            writer.write(loopEndLabel + ":\n");
        }
    }

    private String getVarType(TypeNode type) {
        if (Objects.equals(type.typeName, "integer")) {
            return "I";
        } else if (Objects.equals(type.typeName, "real")) {
            return "F";
        } else if (Objects.equals(type.typeName, "boolean")) {
            return "I";
        }
        return "I";
    }

    private String getVarTypeFromVarName(String variableName) {
        for (VarDeclaration varDeclaration : declaredVariables) {
            if (varDeclaration.variableName.equals(variableName)) {
                return getVarType((TypeNode) varDeclaration.variableType);
            }
        }
        return "I"; // Default to integer if not found, though an error might be more appropriate
    }

    private int labelCounter = 0;

    // Generates a unique label index to avoid collisions
    private int uniqueLabelIndex() {
        return labelCounter++;
    }

    // Somewhere in your class
    private final Map<String, Integer> variableIndices = new HashMap<>();
    private int nextLocalVariableIndex = 0; // Start at 0 for static methods, 1 for instance methods

    private int getLocalVariableIndex(String variableName) {
        if (!variableIndices.containsKey(variableName)) {
            variableIndices.put(variableName, nextLocalVariableIndex);
            nextLocalVariableIndex++; // Increment the index for the next variable
        }
        return variableIndices.get(variableName);
    }

    private void generateCodeForIfCondition(ASTNode condition, FileWriter writer, String endLabel) throws IOException {
        if (condition instanceof BinaryOpNode comparison) {

            // Assuming ComparisonNode has left and right operands and an operator
            if (comparison.left instanceof LiteralNode leftValue) {
                if (isNumeric(leftValue.value)) {
                    writer.write("ldc " + leftValue.value + "\n");
                } else if (Objects.equals(leftValue.value, "true")) {
                    writer.write("ldc 1\n");
                } else if (Objects.equals(leftValue.value, "false")) {
                    writer.write("ldc 0\n");
                } else {
                    writer.write("getstatic GeneratedClass/" + leftValue.value + " " + getVarTypeFromVarName(leftValue.value) + "\n");
                }
            }
            if (comparison.right instanceof LiteralNode rightValue) {
                if (isNumeric(rightValue.value)) {
                    writer.write("ldc " + rightValue.value + "\n");
                } else if (Objects.equals(rightValue.value, "true")) {
                    writer.write("ldc 1\n");
                } else if (Objects.equals(rightValue.value, "false")) {
                    writer.write("ldc 0\n");
                } else {
                    writer.write("getstatic GeneratedClass/" + rightValue.value + " " + getVarTypeFromVarName(rightValue.value) + "\n");
                }
            }
            Token.TokenType operator = comparison.operator;

            // Depending on the comparison operator, use the appropriate branching instruction
            switch (operator) {
                case GREATER_THAN:
                    writer.write("if_icmpgt " + endLabel + "\n");
                    break;
                case LESS_THAN:
                    writer.write("if_icmplt " + endLabel + "\n");
                    break;
                case EQUALS:
                    writer.write("if_icmpeq " + endLabel + "\n");
                    break;
                case NOT_EQUALS:
                    writer.write("if_icmpne " + endLabel + "\n");
                    break;
                case GREATER_THAN_OR_EQUAL:
                    writer.write("if_icmpge " + endLabel + "\n");
                    break;
                case LESS_THAN_OR_EQUAL:
                    writer.write("if_icmple " + endLabel + "\n");
                    break;
                default:
                    throw new UnsupportedOperationException("Operator " + operator + " is not supported");
            }
        }
    }

    private void generateCodeForWhileCondition(ASTNode condition, FileWriter writer, String endLabel) throws IOException {
        if (condition instanceof BinaryOpNode comparison) {

            // Assuming ComparisonNode has left and right operands and an operator
            if (comparison.left instanceof LiteralNode leftValue) {
                if (isNumeric(leftValue.value)) {
                    writer.write("ldc " + leftValue.value + "\n");
                } else if (Objects.equals(leftValue.value, "true")) {
                    writer.write("ldc 1\n");
                } else if (Objects.equals(leftValue.value, "false")) {
                    writer.write("ldc 0\n");
                } else {
                    writer.write("getstatic GeneratedClass/" + leftValue.value + " " + getVarTypeFromVarName(leftValue.value) + "\n");
                }
            }
            if (comparison.right instanceof LiteralNode rightValue) {
                if (isNumeric(rightValue.value)) {
                    writer.write("ldc " + rightValue.value + "\n");
                } else if (Objects.equals(rightValue.value, "true")) {
                    writer.write("ldc 1\n");
                } else if (Objects.equals(rightValue.value, "false")) {
                    writer.write("ldc 0\n");
                } else {
                    writer.write("getstatic GeneratedClass/" + rightValue.value + " " + getVarTypeFromVarName(rightValue.value) + "\n");
                }
            }
            Token.TokenType operator = comparison.operator;

            // Depending on the comparison operator, use the appropriate branching instruction
            switch (operator) {
                case GREATER_THAN:
                    writer.write("if_icmple " + endLabel + "\n");
                    break;
                case LESS_THAN:
                    writer.write("if_icmpge " + endLabel + "\n");
                    break;
                case EQUALS:
                    writer.write("if_icmpne " + endLabel + "\n");
                    break;
                case NOT_EQUALS:
                    writer.write("if_icmpeq " + endLabel + "\n");
                    break;
                case GREATER_THAN_OR_EQUAL:
                    writer.write("if_icmplt " + endLabel + "\n");
                    break;
                case LESS_THAN_OR_EQUAL:
                    writer.write("if_icmpgt " + endLabel + "\n");
                    break;
                default:
                    throw new UnsupportedOperationException("Operator " + operator + " is not supported");
            }
        }
    }


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
}
