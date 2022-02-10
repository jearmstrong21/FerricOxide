import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestLogic {

    // Vec(u8) = Call("Vec", [Call("u8")])
    static record Call(String name, List<Call> args) {
        static Call of(String name) {
            return new Call(name, new ArrayList<>());
        }

        @Override
        public String toString() {
            if (args.size() == 0) return name;
            return name + "(" + args.stream().map(Call::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    // X: Clone = Bound("X", "Clone")
    static record Bound(String predicate, Call type) {
    }

    // <X: Clone> impl Clone for Vec<X> = Fact("tracestr", ["X"], [Bound("X", "Clone")], Call("Vec", [Call("X")])
    static record Fact(String trace, List<String> existentials, List<Bound> bounds, Bound truth) {
    }


    static Map<String, Integer> arities = new HashMap<>();
    static List<Fact> facts = new ArrayList<>();

    static {
        arities.put("Clone", 0);
        arities.put("u8", 0);
        arities.put("Vec", 1);
        facts.add(new Fact("trace1", List.of(), List.of(),
                new Bound("Clone", Call.of("u8"))
        ));
        facts.add(new Fact("trace2", List.of("X"), List.of(
                new Bound("Clone", Call.of("X")) // IF X: Clone
        ),
                new Bound("Clone",
                        new Call("Vec", List.of(Call.of("X"))) // THEN Vec(X): Clone
                )
        ));

        System.out.println(match(List.of("X"), new Call("Vec", List.of(Call.of("u8"))), new Call("Vec", List.of(Call.of("X")))));
        System.out.println(isSatisfied(new Bound("Clone", new Call("Vec", List.of(Call.of("u8"))))));
    }

    static Map<String, Call> match(List<String> existentials, Call query, Call truth) {
        String dbg = "{" + existentials + " -- " + query + " -- " + truth + "}";
        System.out.println("\tmatch " + dbg);
        if (!query.name.equals(truth.name)) {
            if (existentials.contains(truth.name)) {
                if (truth.args.size() > 0) {
                    System.out.println("\tmatch fail (non-zero arity to existential) " + dbg);
                    return null;
                }
                Map<String, Call> subs = new HashMap<>();
                System.out.println("\tmatch sub success " + dbg);
                subs.put(truth.name, query);
                return subs;
            }
            System.out.println("\tmatch fail (name) " + dbg);
            return null;
        }
        if (arities.get(query.name) != query.args.size()) {
            System.out.println("\tmatch fail (query validate) " + dbg);
            return null;
        }
        if (arities.get(truth.name) != truth.args.size()) {
            System.out.println("\tmatch fail (truth validate) " + dbg);
            return null;
        }
        Map<String, Call> substitutions = new HashMap<>();
        for (int i = 0; i < query.args.size(); i++) {
            Call qarg = query.args.get(i);
            Map<String, Call> nsubs = match(existentials, qarg, truth.args.get(i));
            if (nsubs == null) {
                System.out.println("\tmatch fail recursive (arg " + i + ") " + dbg);
                return null;
            }
            for (String key : nsubs.keySet()) {
                if (substitutions.containsKey(key)) {
                    System.out.println("\tmatch fail repeated sub " + key + " " + dbg);
                    return null;
                }
                substitutions.put(key, nsubs.get(key));
            }
        }
        System.out.println("\tmatch success " + substitutions + " " + dbg);
        return substitutions;
    }

    static Call substitute(Call a, Map<String, Call> substitutions) {
        if (substitutions.containsKey(a.name)) {
            if (a.args.size() != 0) throw new UnsupportedOperationException(a.toString());
            return substitutions.get(a.name);
        }
        return new Call(a.name, a.args.stream().map(c -> substitute(c, substitutions)).collect(Collectors.toList()));
    }

    static boolean isSatisfied(Bound query) {
        System.out.println("checking bound " + query);
        for (int i = 0; i < facts.size(); i++) {
            Fact fact = facts.get(i);
            System.out.println("attempting fact " + i + " " + fact);
            Map<String, Call> subs = match(fact.existentials, query.type, fact.truth.type);
            if (subs == null) {
                System.out.println("fact fail");
            } else {
                System.out.println("fact pass!");
                boolean good = true;
                for(Bound bound : fact.bounds) {
                    if(!isSatisfied(bound)) {
                        System.out.println("but bound failed");
                        good = false;
                        break;
                    }
                }
                if(good) {
                    System.out.println("fact pass!");
                    return true;
                } else {
                    System.out.println("fact fail!");
                }
            }
            // Does this fact prove our bound?
        }
        return false;
    }

    /*
    Vec(T) is a function mapping one-to-one X to newtype X, or more realistically, "X" to "Vec_X_"
    Clone(X) is a predicate

    impl Clone for u8 is a bounded fact:
        EXISTENTIAL
        BOUNDS
        IMPLIES      Clone(u8)

    <X: Clone> impl Clone for Vec<X> is a bounded fact:
        EXISTENTIAL  X
        BOUNDS       Clone(X)
        IMPLIES      Clone(Vec(X))

    NOTES
    Existentials are unique to definitions, so this code
            <X> struct A { p: u8 }
            <X> struct B { q: A<X> }
            <X> struct C { r: B<X> }
    Is equivalent to this, letting me do string comparisons or something of that sort
            <X1> struct A { p: u8 }
            <X2> struct B { q: A<X2> }
            <X3> struct C { r: B<X3> }

    Matching functions with different arity never passes
        This includes u8 to Vec(u8) because u8 is technically a zero-arity function to produce the type u8


    QUERY
    Q
        FACT N
        Match Q to FACT N.IMPLIES, otherwise next fact, return existential mapping
        Sanity check: assert all existentials do in fact exist
        Check bounds:
            BOUND N
            Substitute existentials
            IF !QUERY bound continue to next fact
        Bounds passed, fact applies, exit with explanation and trace

    Clone(Vec(u8))?
        FACT 1
        Match Clone(Vec(u8)) to Clone(u8)
            Match Vec(u8) to u8
                Match failed, continue

        FACT 2
        Match Clone(Vec(u8)) to Clone(Vec(X))
            Match Vec(u8) to Vec(X)
                Match u8 to X
                Success! return { "X": "u8" }
            Success! return { "X": "u8" }
        Success! we know now the existential mapping is { "X": "u8" }
        Sanity check: "X" in { "X" : "u8" }
        Check bounds:
            BOUND 1
            Clone(X)
            Substitute X = u8 to get
            Clone(u8)
            QUERY Clone(u8)?
                FACT 1
                Match Clone(u8) to Clone(u8)
                    Match u8 to u8
                        Success! return {}
                Success! we know the existential mapping is {}
                Sanity check: nil
                Check bounds: nil
                Bounds satisfied, fact 1 applies
            Clone(u8), trace fact 1!
        Bounds satisfied, fact 2 applies
    Clone(Vec(u8)), trace fact 2, fact 1!


     */

    public static void main(String[] args) {

    }

}
