import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Read the source code from a file
        String sourceCode = readFile();

        // Start tokenization of source code
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token.type + ": " + token.value);
        }

        // Start parsing tokens into nodes and building AST
        Parser parser = new Parser(tokens);
        List<ASTNode> result = parser.parse();

        // Start of the optimizations in semantic analyzer
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(result);
        result = semanticAnalyzer.startAnalyze();

        // Generation of Jasmin code
        CodeGenerator generator = new CodeGenerator(result);
        generator.generateCode("jasmin-2.4/codegen_result.j");
    }

    private static String readFile() {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("to_check.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}