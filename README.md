# FerricOxide

Like rust but more pedantic

### With respect to

- Processing (as a library, FO on its own is a generic lang)
  - Visual expressiveness
  - OpenGL renderer
- Java
  - Syntactical clarity (no autos in prototypes, blazing-fast IDEs)
    - Autocomplete is love, autocomplete is life
- C
  - Direct relationship with (unoptimized) bytecode
- C++
  - mostly as examples of what not to do: templates, auto, standard
  library, cmake, and more
  - Templates should only exist to specialize containers and
  to make functional programming easier
  - auto is legitimate for filling in generics but the base type
  should always be specified, and function prototypes should always
  have to be verbose about the relevant types
  - Standard library is the disaster child of 50 years of technical
  debt and a few well-meaning engineers who don't write code for their jobs anymore
  - CMake is just another badly documented DSL, and gradle for C++ is dumb
- Rust
  - Macro system is absolutely perfect, procedural macros except
  for formatting shouldn't exist. I controversially don't think
  `ferricoxide build MyProject` should be quite as non-obvious of
  an RCE vulnerability
  - Trait system / inheritance is awesome
    - Traits instead of class inheritance is good
    - Tagged unions, raw unions, and raw enums are good
    - `match` and stuff is a bit janky at times but overall good
  - Error handling: `?` operator, `Result`, and `Option`
    - Maybe too many ways to do error handling and maybe `Option<T>`
    should have just been typedefed to `Result<T, None>`, but still
    way better than anything else
  - I could do without the whole cargo TOML thing, instead just keep
  `build.rs`
  
  
  
      - One of my biggest problems with Rust is that no IDE can keep up
      with the maze of generics, macros, and native code in the standard
      library let alone WebAssembly or OpenGL


### Review / thoughts

- [x] Every expression and statement is pretty straightforward 
to translate to LLVM by hand if necessary: no hidden surprises,
everything does exactly what it says. 
- [x] First-class support for the LLVM module import / exports, with
exporting and importing symbols of arbitrary names 
  - With this comes first-class support for anything like OpenGL or
     even LLVM bindings themself. If you wanted to you could load in
     stb_image because main.c is entirely under your control - FerricOxide
     is just another stage in a pipeline. Demos exist for OpenGL and c file API.
- [ ] Context-free code, where a code string has an unambiguous meaning
in the grammar regardless of its context  
  - This turns out to not be possible in a C-like language for
  a variety of very good reasons
    - `x = 5;` vs `f(x)`: the expectation is that referencing `x` is
    the samme in both cases, but for the first statement `x` actually
    parses to `RefAccessVar("x")` vs the second `AccessVar("x")`.
- [ ] Easy-to-understand code, with every unnecessary token either
optional or not allowed
  - I chose to keep semicolons and some parantheses / commas that
  aren't strictly necessary, although I think they help more than they hurt
    - FWIW if I built a syntax highlighter it would put unnecessary tokens
    in the theme's comment color and comments in a slightly bolder
    standard text color, but I never got around to that
- [ ] Decent module system: got halfway there, ran into issues with
function references and identifiers that were easily resolved but
took so much time I couldn't fix the actual issue: the function pointer
`f(x)` calls and the pointer that `&f` returns should be the same.
- [ ] Error handling: never got around to implementing rusty
error handling so this is definitely not done at all, segfaults FTW
- [ ] Syntax and validation errors: the way I structured both the
parser and AST classes it took forever to add basic code span metadata
and even then it doesn't work well or provide a "stack trace" for the
syntax error


**_E_**

https://xkcd.com/2555/#

https://www.smbc-comics.com/comic/econs

https://questionablecontent.net/view.php?comic=4679#