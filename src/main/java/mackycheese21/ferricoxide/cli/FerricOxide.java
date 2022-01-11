//package mackycheese21.ferricoxide.cli;
//
//import mackycheese21.ferricoxide.AnalysisException;
//import mackycheese21.ferricoxide.FOLLVM;
//import mackycheese21.ferricoxide.pp.Preprocessor;
//import mackycheese21.ferricoxide.SourceCodeException;
//import mackycheese21.ferricoxide.ast.module.CompiledModule;
//import mackycheese21.ferricoxide.compile.CompileModuleVisitor;
//import mackycheese21.ferricoxide.compile.TypeValidatorVisitor;
//import mackycheese21.ferricoxide.parser.ModuleParser;
//import mackycheese21.ferricoxide.parser.token.TokenScanner;
//import mackycheese21.ferricoxide.parser.token.TokenTree;
//import org.apache.commons.cli.*;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.List;
//
//public class FerricOxide {
//
//    private static Options options;
//
//    private static void help() {
//        new HelpFormatter().printHelp("mackycheese21/ferricoxide", options);
//        System.exit(0);
//    }
//
//    public static void main(String[] args) {
//        options = new Options();
//
//        Option include = Option.builder()
//                .option("I")
//                .hasArg()
//                .build();
//        Option out = Option.builder()
//                .option("o")
//                .hasArg()
//                .numberOfArgs(1)
//                .required()
//                .build();
//        Option main = Option.builder()
//                .option("i")
//                .hasArg()
//                .numberOfArgs(1)
//                .required()
//                .build();
//
//        options.addOption(include);
//        options.addOption(out);
//        options.addOption(main);
//
//        CommandLineParser parser = new DefaultParser();
//        CommandLine cmd;
//        try {
//            cmd = parser.parse(options, args);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//            help();
//            return;
//        }
//
////        Tokenizer.INCLUDE_SEARCH_PATHS.addAll(Arrays.asList(cmd.getOptionValues(include)));
//
//        String outFilename = cmd.getOptionValue(out);
//        String mainFilename = cmd.getOptionValue(main);
//
////        FOLLVM.initialize();
////        TypeRegistry.init();
////
////        List<String> includePaths = Arrays.asList(cmd.getOptionValues(include));
////
////        System.out.println("0 Starting...");
////        try {
////            Preprocessor preprocessor = new Preprocessor(includePaths);
////            List<TokenTree> tokens = preprocessor.include(mainFilename);
////            System.out.println("1 Tokenized / preprocessed...");
////
//////            System.out.println(tokens.stream().map(Object::toString).collect(Collectors.joining(" ")));
////
////            FOModule module = ModuleParser.parse(new TokenScanner(tokens));
////            System.out.println("2 FO parsed...");
////
////            new TypeValidatorVisitor().visit(module);
////            System.out.println("3 Validated/resolved...");
////
////            Files.writeString(Path.of("BIN/build/post_macro_format"), LLFo);
////
////            CompiledModule compiledModule = new CompileModuleVisitor().visit(module);
////            System.out.println("4 LLVM compiled, verified...");
////
////            compiledModule.outputX86(null, outFilename);
////            System.out.println("5 Dumped");
////        } catch (SourceCodeException e) {
////            System.out.println(e.span);
////            System.out.println(e.span.file());
////            e.printStackTrace();
////        } catch (AnalysisException e) {
////            System.out.println(e.span);
////            System.out.println(e.span.file());
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }
//
//}
