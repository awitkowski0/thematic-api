package bond.thematic.api.engine.molang;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class MolangQueryRegistry {
    private final Map<String, DoubleSupplier> builtins = new HashMap<>();
    private final Map<String, DoubleSupplier> entityQueries = new HashMap<>();
    private final Map<String, DoubleSupplier> animationQueries = new HashMap<>();

    public void registerBuiltin(String name, DoubleSupplier supplier) {
        builtins.put(name, supplier);
    }

    public void registerEntityQuery(String name, DoubleSupplier supplier) {
        entityQueries.put(name, supplier);
    }

    public void registerAnimationQuery(String name, DoubleSupplier supplier) {
        animationQueries.put(name, supplier);
    }

    public void applyTo(MolangEnvironment env) {
        for (var entry : builtins.entrySet()) {
            env.registerQuery(entry.getKey(), entry.getValue());
        }
        for (var entry : entityQueries.entrySet()) {
            env.registerQuery(entry.getKey(), entry.getValue());
        }
        for (var entry : animationQueries.entrySet()) {
            env.registerQuery(entry.getKey(), entry.getValue());
        }
    }

    public MolangQueryRegistry copy() {
        MolangQueryRegistry reg = new MolangQueryRegistry();
        reg.builtins.putAll(this.builtins);
        reg.entityQueries.putAll(this.entityQueries);
        reg.animationQueries.putAll(this.animationQueries);
        return reg;
    }
}
