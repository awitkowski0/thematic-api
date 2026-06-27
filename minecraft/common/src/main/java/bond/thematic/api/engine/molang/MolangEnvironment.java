package bond.thematic.api.engine.molang;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class MolangEnvironment {
    private final Map<String, Double> variables = new HashMap<>();
    private final Map<String, Double> temporaries = new HashMap<>();
    private final Map<String, DoubleSupplier> queries = new HashMap<>();

    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    public double getVariable(String name) {
        return variables.getOrDefault(name, 0.0);
    }

    public void setTemp(String name, double value) {
        temporaries.put(name, value);
    }

    public double getTemp(String name) {
        return temporaries.getOrDefault(name, 0.0);
    }

    public void resetTemporaries() {
        temporaries.clear();
    }

    public void registerQuery(String name, DoubleSupplier supplier) {
        queries.put(name, supplier);
    }

    public double getQuery(String name) {
        DoubleSupplier supplier = queries.get(name);
        if (supplier == null) return 0.0;
        return supplier.getAsDouble();
    }

    public void clearVariables() {
        variables.clear();
    }

    public MolangEnvironment copy() {
        MolangEnvironment env = new MolangEnvironment();
        env.variables.putAll(this.variables);
        env.queries.putAll(this.queries);
        return env;
    }
}
