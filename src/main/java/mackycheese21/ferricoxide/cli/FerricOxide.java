package mackycheese21.ferricoxide.cli;

import org.apache.commons.cli.*;

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
                .argName("files")
                .desc("input source")
                .hasArg()
                .required()
                .build();
        Option out = Option.builder()
                .option("o")
                .argName("dir")
                .desc("output binary")
                .hasArg()
                .required()
                .build();
//        Option llvm = Option.builder()
//                .option("l")
//                .longOpt("llvm")
//                .argName("dir")
//                .desc("output llvm")
//                .hasArg()
//                .build();
        Option help = Option.builder()
                .option("h")
                .longOpt("help")
                .desc("show help")
                .hasArg(false)
                .build();

        options.addOption(in);
        options.addOption(out);
//        options.addOption(llvm);
        options.addOption(help);

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
//        if (!cmd.hasOption(out) && !cmd.hasOption(llvm)) {
//            System.out.println("at least one output required");
//            help();
//        }
//        if (cmd.hasOption(llvm)) {
//            System.out.println("WARNING: llvm output not implemented, ignoring option");
//        }
//        if (cmd.hasOption(out)) {
//            System.out.println("WARNING: binary output not implemented, ignoring option");
//        }
        System.out.println("Input files:");
        for(String s : cmd.getOptionValues(in)) {
            System.out.println(s);
        }
    }

}
