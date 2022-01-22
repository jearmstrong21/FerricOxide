package mackycheese21.ferricoxide.cli;

import mackycheese21.ferricoxide.AnalysisException;
import mackycheese21.ferricoxide.FOLLVM;
import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.ast.hl.compile.HLModule;
import mackycheese21.ferricoxide.ast.hl.compile.HLModuleCompiler;
import mackycheese21.ferricoxide.ast.hl.mod.ModItem;
import mackycheese21.ferricoxide.ast.ll.compile.LLModuleCompiler;
import mackycheese21.ferricoxide.ast.ll.compile.LLModuleWriter;
import mackycheese21.ferricoxide.ast.ll.mod.LLModule;
import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.format.HLModuleFormatter;
import mackycheese21.ferricoxide.parser.ModuleParser;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.TokenTree;
import mackycheese21.ferricoxide.pp.Preprocessor;
import org.apache.commons.cli.*;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FerricOxide {

    private static Options options;

    private static void help() {
        new HelpFormatter().printHelp("mackycheese21/ferricoxide", options);
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        try {
            options = new Options();

            Option include = Option.builder()
                    .option("I")
                    .hasArg()
                    .build();
            Option out = Option.builder()
                    .option("o")
                    .hasArg()
                    .numberOfArgs(1)
                    .required()
                    .build();
            Option main = Option.builder()
                    .option("i")
                    .hasArg()
                    .numberOfArgs(1)
                    .required()
                    .build();

            options.addOption(include);
            options.addOption(out);
            options.addOption(main);

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd;
            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                help();
                return;
            }

            String outFilename = cmd.getOptionValue(out);
            String mainFilename = cmd.getOptionValue(main);

            FOLLVM.initialize();

            List<String> includePaths = Arrays.asList(cmd.getOptionValues(include));
            Preprocessor pp = new Preprocessor(includePaths);
            List<TokenTree> tokens = pp.include(mainFilename);

            List<ModItem> items = ModuleParser.parse(new TokenScanner(tokens));

            System.out.println(HLModuleFormatter.format(items));

            HLModule hlModule = new HLModule(items);

            LLModule llModule = HLModuleCompiler.compile(hlModule);

            LLVMModuleRef moduleRef = LLModuleCompiler.compile(llModule);

            LLModuleWriter.write(llModule, moduleRef, outFilename);
        } catch (AnalysisException e) {
            System.out.println(e.span);
            System.out.println(e.span.file());
            e.printStackTrace();
        } catch (SourceCodeException e) {
            System.out.println(e.span);
            System.out.println(e.span.file());
            e.printStackTrace();
        }
    }

}
