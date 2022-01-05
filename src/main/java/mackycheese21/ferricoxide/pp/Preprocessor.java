package mackycheese21.ferricoxide.pp;

import mackycheese21.ferricoxide.SourceCodeException;
import mackycheese21.ferricoxide.parser.token.*;
import mackycheese21.ferricoxide.pp.macros.ConcatIdentMacro;
import mackycheese21.ferricoxide.pp.macros.IncludeMacro;
import mackycheese21.ferricoxide.pp.macros.StringifyMacro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Preprocessor {

    private final List<String> includeSearchPaths;

    public Preprocessor(List<String> includeSearchPaths) {
        this.includeSearchPaths = includeSearchPaths;
        this.includedPaths = new ArrayList<>();

        macros.put("concat_ident", new ConcatIdentMacro());
        macros.put("include", new IncludeMacro());
        macros.put("stringify", new StringifyMacro());
    }

    public Path resolveInclude(Span span, String include) {
        for (String searchPath : includeSearchPaths) {
            if (Path.of(searchPath, include).toFile().exists()) {
                return Path.of(searchPath, include);
            }
        }
        for (String searchPath : includeSearchPaths) {
            if (Path.of(searchPath, include + ".fo").toFile().exists()) {
                return Path.of(searchPath, include + ".fo");
            }
        }
        throw new SourceCodeException(span, "cannot resolve file " + include);
    }

    private final List<Path> includedPaths;

    public final Map<String, Macro> macros = new HashMap<>();

    private List<TokenTree> evalMacro(TokenScanner scanner) {
        String name = scanner.next().requireIdent().value;
        name = name.substring(0, name.length() - 1);
        GroupToken group = scanner.next().requireGroup(null);
        if (macros.containsKey(name)) {
            return macros.get(name).eval(this, group.span(), new TokenScanner(group.value));
        }
        throw new SourceCodeException(scanner.lastConsumedSpan(), "no macro found %s".formatted(name));
    }

    // TODO define/ifdef/elif
    private List<TokenTree> apply(TokenScanner scanner) {
        List<TokenTree> out = new ArrayList<>();
        while (scanner.remaining() > 0) {
            if (scanner.peek() instanceof GroupToken group) {
                scanner.next();
                out.add(new GroupToken(group.span(), group.type, apply(new TokenScanner(group.value))));
            } else if (scanner.peek() instanceof IdentToken ident && ident.value.endsWith("!")) {
                out.addAll(evalMacro(scanner));
            } else {
                out.add(scanner.next());
            }
        }
        return out;
    }

    public List<TokenTree> include(String mainFilename) throws IOException {
        return include(new Span(new Span.Loc(0, 0), new Span.Loc(0, 0), null), mainFilename);
    }

    public List<TokenTree> include(Span span, String mainFilename) throws IOException {
        Path path = resolveInclude(span, mainFilename);
        if (includedPaths.contains(path)) return new ArrayList<>();
        includedPaths.add(path);
        return apply(new TokenScanner(Tokenizer.tokenize(path, Files.readString(path))));
    }
}
