package mackycheese21.ferricoxide.ast.nast;

import mackycheese21.ferricoxide.ast.module.CompiledModule;
import mackycheese21.ferricoxide.ast.module.FOModule;
import mackycheese21.ferricoxide.parser.ModuleParser;
import mackycheese21.ferricoxide.ast.visitor.TypeValidatorVisitor;
import mackycheese21.ferricoxide.compile.CompileModuleVisitor;
import mackycheese21.ferricoxide.parser.token.TokenScanner;
import mackycheese21.ferricoxide.parser.token.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestNASTCompile {

    public static void main(String[] args) throws IOException {
        FOModule module = ModuleParser.parse(
                new TokenScanner(
                        Tokenizer.tokenize(
                                Files.readString(Path.of("BIN/main.fo"))
                        )));
        new TypeValidatorVisitor().visit(module);
        CompiledModule compiledModule = new CompileModuleVisitor().visit(module);
        compiledModule.outputRISCV("BIN/build/riscv.txt");
        compiledModule.outputX86("BIN/build/x86.txt", "BIN/build/x86.o");
    }

}
