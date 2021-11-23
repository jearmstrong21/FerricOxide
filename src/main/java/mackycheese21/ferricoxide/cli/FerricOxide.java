package mackycheese21.ferricoxide.cli;

import mackycheese21.ferricoxide.Module;
import mackycheese21.ferricoxide.ast.AstParser;
import mackycheese21.ferricoxide.ast.SourceCodeException;
import mackycheese21.ferricoxide.ast.token.Token;
import mackycheese21.ferricoxide.ast.token.Tokenizer;
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


        try {
            String data = Files.readString(Path.of(cmd.getOptionValues(in)[0]));
            List<Token> tokens = Tokenizer.tokenize(data);
            Module module = AstParser.parse(tokens);
            String x86_assembly = cmd.getOptionValue(x86, null);
            String x86_binary = cmd.getOptionValue(out);
            String riscv_assembly = cmd.getOptionValue(riscv, null);
            module.codegen(x86_assembly, x86_binary, riscv_assembly);
        } catch (SourceCodeException | IOException e) {
            e.printStackTrace();
        }
    }

}
