import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Read the source code from a file
        String sourceCode = readFile();

        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token.type + ": " + token.value);
        }
        Parser parser = new Parser(tokens);
        List<ASTNode> result = parser.parse();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(result);
        result = semanticAnalyzer.startAnalyze();
        System.out.println(result);
        CodeGenerator generator = new CodeGenerator(result);
        generator.generateCode("codegen_result.j");
    }

    private static String readFile() {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("source.txt"));
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