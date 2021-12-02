package mackycheese21.ferricoxide.cli;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import mackycheese21.ferricoxide.compile.CompileModuleVisitor;
import mackycheese21.ferricoxide.parser.ModuleParser;
import mackycheese21.ferricoxide.parser.token.Token;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.Tokenizer;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FerricOxide {

    private static Options options;

    private static void help() {
        new HelpFormatter().printHelp("mackycheese21/ferricoxide", options);
        System.exit(0);
    }

    public static void main(String[] args) {
        options = new Options();

        Option in = Option.builder()
                .option("i")
                .longOpt("in")
                .argName("file")
                .desc("input source")
                .hasArg()
                .numberOfArgs(1)
                .required()
                .build();
        Option out = Option.builder()
                .option("o")
                .longOpt("out")
                .argName("file")
                .desc("output binary")
                .hasArg()
                .numberOfArgs(1)
                .required()
                .build();
//        Option llvm = Option.builder()
//                .option("l")
//                .longOpt("llvm")
//                .argName("file")
//                .desc("output llvm")
//                .hasArg()
//                .numberOfArgs(1)
//                .build();
        Option help = Option.builder()
                .option("h")
                .longOpt("help")
                .desc("show help")
                .hasArg(false)
                .build();
        Option riscv = Option.builder()
                .option("r")
                .longOpt("riscv")
                .argName("file")
                .desc("output riscv")
                .hasArg()
                .numberOfArgs(1)
                .build();
        Option x86 = Option.builder()
                .option("x")
                .longOpt("x86")
                .argName("file")
                .desc("output x86")
                .hasArg()
                .numberOfArgs(1)
                .build();

        options.addOption(in);
        options.addOption(out);
//        options.addOption(llvm);
        options.addOption(help);
        options.addOption(riscv);
        options.addOption(x86);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            help();
            return;
        }
        if (cmd.hasOption(help)) {
            help();
        }

        if (!cmd.hasOption(in)) {
            System.out.println("in parameter required");
            help();
        }
        String data = null;

        try {
            data = Files.readString(Path.of(cmd.getOptionValues(in)[0]));
            System.out.println("File read...");
            List<Token> tokens = Tokenizer.tokenize(data);
            System.out.println("Tokenized...");
            FOModule module = ModuleParser.parse(new TokenScanner(tokens));
            module.resolve();
            System.out.println("FO parsed...");
            String x86_assembly = cmd.getOptionValue(x86, null);
            String x86_binary = cmd.getOptionValue(out);
            String riscv_assembly = cmd.getOptionValue(riscv, null);
            System.out.println("CLI parsed...");
            new TypeValidatorVisitor().visit(module);
            System.out.println("Validated...");
            CompiledModule compiledModule = new CompileModuleVisitor().visit(module);
            System.out.println("CompiledModule...");
            if (riscv_assembly != null) compiledModule.outputRISCV(riscv_assembly);
            compiledModule.outputX86(x86_assembly, x86_binary);
            System.out.println("Dumped");
        } catch (SourceCodeException e) {
            int line = 1;
            for (int i = 0; i <= e.span.start; i++) {
                if (data.charAt(i) == '\n') line++;
            }
            System.out.println(line + ":" + (e.span.end - e.span.start));
            System.out.println(e.span);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}