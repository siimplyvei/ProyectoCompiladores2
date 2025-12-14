package minic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println("Uso: java minic.Main <archivo.mc>");
            System.exit(1);
        }

        // Leer archivo Mini-C
        CharStream input = CharStreams.fromFileName(args[0]);

        // Lexer
        MiniCLexer lexer = new MiniCLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Parser
        MiniCParser parser = new MiniCParser(tokens);

        // Parsear
        ParseTree tree = parser.program();

        // Mostrar árbol sintáctico
        System.out.println("=== ARBOL SINTACTICO ===");
        System.out.println(tree.toStringTree(parser));
    }
}

