package minic;

import minic.semantic.SemanticVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import minic.ir.IRGenerator;
import minic.mips.MIPSGenerator;
import minic.semantic.*;
import minic.ir.IROptimizer;
import java.util.List;
import minic.ir.IRInstr;


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

        ScopedSymbolTable symtab = semantic.getSymbolTable();
        boolean optimize = false;
        boolean dumpIR = false;
        String inputFile = null;
        String outputFile = "output.s";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-O":
                    optimize = true;
                    break;
                case "--dump-ir":
                    dumpIR = true;
                    break;
                case "-o":
                    outputFile = args[++i];
                    break;
                default:
                    inputFile = args[i];
            }
        }

        System.out.println("\n=== IR (Three Address Code) ===");
        IRGenerator ir = new IRGenerator(symtab);
        ir.visit(tree);

        List<IRInstr> irCode = ir.getInstructions();

        if (optimize) {
            System.out.println("=== IR ANTES ===");
            irCode.forEach(System.out::println);

            if (optimize){
                irCode = IROptimizer.constantFold(irCode);
                irCode = IROptimizer.copyPropagation(irCode);
                irCode = IROptimizer.deadCodeElimination(irCode);
                //irCode = IROptimizer.removeConstantConditions(irCode);
            }

            System.out.println("=== IR DESPUÉS ===");
            irCode.forEach(System.out::println);
        }

        System.out.println("\n=== GENERANDO MIPS ===");
        MIPSGenerator mips = new MIPSGenerator(irCode, symtab);
        mips.generate(outputFile);
        System.out.println("✔ Archivo output.s generado");

    }
}
