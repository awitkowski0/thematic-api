package bond.thematic.api.engine.molang;

import bond.thematic.api.engine.molang.MolangAst.*;

import java.util.List;

public class MolangEvaluator {
    private final MolangEnvironment env;

    public MolangEvaluator(MolangEnvironment env) {
        this.env = env;
    }

    public double evaluate(MolangAst ast) {
        if (ast instanceof NumberExpr n) return n.value();
        if (ast instanceof StringExpr) return 0.0;
        if (ast instanceof UnaryExpr u) return evaluateUnary(u);
        if (ast instanceof BinaryExpr b) return evaluateBinary(b);
        if (ast instanceof TernaryExpr t) return evaluateTernary(t);
        if (ast instanceof FunctionCallExpr f) return evaluateFunction(f);
        if (ast instanceof QueryExpr q) return env.getQuery(q.name());
        if (ast instanceof VariableExpr v) return env.getVariable(v.name());
        if (ast instanceof TempExpr t) return env.getTemp(t.name());
        if (ast instanceof AssignmentExpr a) return evaluateAssignment(a);
        if (ast instanceof StatementList s) return evaluateStatements(s);
        return 0.0;
    }

    private double evaluateUnary(UnaryExpr u) {
        double value = evaluate(u.expr());
        return switch (u.op()) {
            case NEGATE -> -value;
            case NOT -> value != 0.0 ? 0.0 : 1.0;
        };
    }

    private double evaluateBinary(BinaryExpr b) {
        if (b.op() == BinaryOp.AND || b.op() == BinaryOp.OR) {
            double left = evaluate(b.left());
            if (b.op() == BinaryOp.AND) {
                return left != 0.0 ? (evaluate(b.right()) != 0.0 ? 1.0 : 0.0) : 0.0;
            } else {
                return left != 0.0 ? 1.0 : (evaluate(b.right()) != 0.0 ? 1.0 : 0.0);
            }
        }
        double left = evaluate(b.left());
        double right = evaluate(b.right());
        return switch (b.op()) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> right != 0.0 ? left / right : 0.0;
            case MODULO -> right != 0.0 ? left % right : 0.0;
            case EQUAL -> left == right ? 1.0 : 0.0;
            case NOT_EQUAL -> left != right ? 1.0 : 0.0;
            case LESS -> left < right ? 1.0 : 0.0;
            case GREATER -> left > right ? 1.0 : 0.0;
            case LESS_EQUAL -> left <= right ? 1.0 : 0.0;
            case GREATER_EQUAL -> left >= right ? 1.0 : 0.0;
            default -> 0.0;
        };
    }

    private double evaluateTernary(TernaryExpr t) {
        double condition = evaluate(t.condition());
        return condition != 0.0 ? evaluate(t.thenExpr()) : evaluate(t.elseExpr());
    }

    private double evaluateFunction(FunctionCallExpr f) {
        return switch (f.name()) {
            case "sin" -> Math.sin(arg(f, 0));
            case "cos" -> Math.cos(arg(f, 0));
            case "tan" -> Math.tan(arg(f, 0));
            case "asin" -> Math.asin(clamp1(arg(f, 0)));
            case "acos" -> Math.acos(clamp1(arg(f, 0)));
            case "atan" -> Math.atan(arg(f, 0));
            case "atan2" -> Math.atan2(arg(f, 0), arg(f, 1));
            case "abs" -> Math.abs(arg(f, 0));
            case "sqrt" -> Math.sqrt(Math.max(0, arg(f, 0)));
            case "floor" -> Math.floor(arg(f, 0));
            case "ceil" -> Math.ceil(arg(f, 0));
            case "round" -> Math.round(arg(f, 0));
            case "exp" -> Math.exp(arg(f, 0));
            case "log" -> Math.log(Math.max(0.0001, arg(f, 0)));
            case "pow" -> Math.pow(arg(f, 0), arg(f, 1));
            case "min" -> Math.min(arg(f, 0), arg(f, 1));
            case "max" -> Math.max(arg(f, 0), arg(f, 1));
            case "clamp" -> clamp(arg(f, 0), arg(f, 1), arg(f, 2));
            case "lerp" -> lerp(arg(f, 0), arg(f, 1), arg(f, 2));
            case "random" -> Math.random();
            case "print" -> evaluatePrint(f);
            case "math.sin" -> Math.sin(arg(f, 0));
            case "math.cos" -> Math.cos(arg(f, 0));
            case "math.tan" -> Math.tan(arg(f, 0));
            case "math.asin" -> Math.asin(clamp1(arg(f, 0)));
            case "math.acos" -> Math.acos(clamp1(arg(f, 0)));
            case "math.atan" -> Math.atan(arg(f, 0));
            case "math.atan2" -> Math.atan2(arg(f, 0), arg(f, 1));
            case "math.abs" -> Math.abs(arg(f, 0));
            case "math.sqrt" -> Math.sqrt(Math.max(0, arg(f, 0)));
            case "math.floor" -> Math.floor(arg(f, 0));
            case "math.ceil" -> Math.ceil(arg(f, 0));
            case "math.round" -> Math.round(arg(f, 0));
            case "math.exp" -> Math.exp(arg(f, 0));
            case "math.log" -> Math.log(Math.max(0.0001, arg(f, 0)));
            case "math.pow" -> Math.pow(arg(f, 0), arg(f, 1));
            case "math.min" -> Math.min(arg(f, 0), arg(f, 1));
            case "math.max" -> Math.max(arg(f, 0), arg(f, 1));
            case "math.clamp" -> clamp(arg(f, 0), arg(f, 1), arg(f, 2));
            case "math.lerp" -> lerp(arg(f, 0), arg(f, 1), arg(f, 2));
            case "math.random" -> Math.random();
            default -> 0.0;
        };
    }

    private double evaluatePrint(FunctionCallExpr f) {
        StringBuilder sb = new StringBuilder("[MoLang] ");
        for (int i = 0; i < f.args().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(evaluate(f.args().get(i)));
        }
        System.out.println(sb);
        return 0.0;
    }

    private double evaluateAssignment(AssignmentExpr a) {
        double value = evaluate(a.value());
        if (a.isTemp()) {
            env.setTemp(a.name(), value);
        } else {
            env.setVariable(a.name(), value);
        }
        return value;
    }

    private double evaluateStatements(StatementList s) {
        double result = 0.0;
        for (MolangAst stmt : s.statements()) {
            result = evaluate(stmt);
        }
        return result;
    }

    private double arg(FunctionCallExpr f, int index) {
        if (index >= f.args().size()) return 0.0;
        return evaluate(f.args().get(index));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp1(double value) {
        return clamp(value, -1.0, 1.0);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0.0, 1.0);
    }
}
