package bond.thematic.api.engine.molang;

import java.util.List;

public sealed interface MolangAst {
    record NumberExpr(double value) implements MolangAst {}
    record StringExpr(String value) implements MolangAst {}
    record UnaryExpr(UnaryOp op, MolangAst expr) implements MolangAst {}
    record BinaryExpr(BinaryOp op, MolangAst left, MolangAst right) implements MolangAst {}
    record TernaryExpr(MolangAst condition, MolangAst thenExpr, MolangAst elseExpr) implements MolangAst {}
    record FunctionCallExpr(String name, List<MolangAst> args) implements MolangAst {}
    record QueryExpr(String name) implements MolangAst {}
    record VariableExpr(String name) implements MolangAst {}
    record TempExpr(String name) implements MolangAst {}
    record AssignmentExpr(String name, MolangAst value, boolean isTemp) implements MolangAst {}
    record StatementList(List<MolangAst> statements) implements MolangAst {}

    enum UnaryOp {
        NEGATE, NOT
    }

    enum BinaryOp {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO,
        EQUAL, NOT_EQUAL, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
        AND, OR
    }
}
