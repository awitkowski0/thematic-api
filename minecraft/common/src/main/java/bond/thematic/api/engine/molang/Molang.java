package bond.thematic.api.engine.molang;

import bond.thematic.api.engine.molang.MolangLexer.MolangParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Molang {
    private final Map<String, MolangAst> cache = new HashMap<>();
    private final MolangEnvironment env = new MolangEnvironment();
    private final MolangEvaluator evaluator = new MolangEvaluator(env);
    private final BuiltinQueries builtins = new BuiltinQueries();
    private final EntityQueries entityQueries = new EntityQueries();
    private final AnimationQueries animationQueries = new AnimationQueries();

    public Molang() {
        builtins.applyTo(env);
        entityQueries.applyTo(env);
        animationQueries.applyTo(env);
    }

    public double eval(String expression) {
        if (expression == null || expression.isBlank()) return 0.0;
        MolangAst ast = cache.computeIfAbsent(expression, this::parse);
        env.resetTemporaries();
        return evaluator.evaluate(ast);
    }

    public double eval(String expression, double defaultValue) {
        try {
            return eval(expression);
        } catch (MolangParseException e) {
            return defaultValue;
        }
    }

    private MolangAst parse(String expression) {
        MolangLexer lexer = new MolangLexer(expression);
        List<MolangLexer.Token> tokens = lexer.tokenize();
        MolangParser parser = new MolangParser(tokens);
        return parser.parse();
    }

    public BuiltinQueries builtins() { return builtins; }
    public EntityQueries entityQueries() { return entityQueries; }
    public AnimationQueries animationQueries() { return animationQueries; }
    public MolangEnvironment env() { return env; }

    public void resetCache() {
        cache.clear();
    }
}
