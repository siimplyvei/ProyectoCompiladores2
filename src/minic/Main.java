package minic;

import minic.semantic.SemanticVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("Uso: java minic.Main <archivo.mc>");
            System.exit(1);
        }

        CharStream input = CharStreams.fromFileName(args[0]);
        MiniCLexer lexer = new MiniCLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCParser parser = new MiniCParser(tokens);

        ParseTree tree = parser.program();

        System.out.println("=== ARBOL SINTACTICO ===");
        System.out.println(tree.toStringTree(parser));

        System.out.println("\n=== ANALISIS SEMANTICO ===");
        SemanticVisitor semantic = new SemanticVisitor();
        semantic.visit(tree);

        System.out.println("✔ Analisis semantico finalizado sin errores");
    }
}
