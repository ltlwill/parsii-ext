package com.efe.parsiiext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parsii.eval.BinaryOperation;
import parsii.eval.Constant;
import parsii.eval.Expression;
import parsii.eval.Variable;
import parsii.eval.VariableReference;

/***
 *
 * Solving the equation of one variable once.
 * eg: X + 1000-X*0.06*(0.02+0.03+0.07) = 67677.21, then will solve the result of x.
 *
 * <p>Here is the demo to use this program.
 * <pre>
 *     String function = "x-x*0.06*(0.02+0.03+0.01)=2359207.68"; // expect x = 2367731.51
 *     Expression expr;
 *     try {
 *         expr = Parser.parse(function);
 *     } catch (ParseException e) {
 *         throw new RuntimeException("Parse function '" + function + "' fail.", e);
 *     }
 *     BinaryExpr expr0 = new BinaryExpr(expr);
 *     return expr0.getValue();
 * </pre>
 *
 * @author zonghai.szh
 * @email  517618650@qq.com
 *
 */
public class BinaryExpr extends BinaryOperation {

    /**
     * Creates a new binary operator for the given operator and operands.
     *
     * @param op    the operator of the operation
     * @param left  the left operand
     * @param right the right operand
     */
    public BinaryExpr(Op op, Expression left, Expression right) {
        super(op, wrap(left), wrap(right));
    }

    /**
     * Creates a new binary operator for the given expression.
     *
     * @param expr the expression to be solved
     */
    public BinaryExpr(BinaryOperation expr) {
        this(expr.getOp(), expr.getLeft(), expr.getRight());
    }

    /**
     * Creates a new binary operator for the given expression.
     *
     * @param expr the expression to be solved
     */
    public BinaryExpr(Expression expr) {
        this(expr instanceof BinaryOperation ? ((BinaryOperation)expr).getOp() : Op.ADD,
             expr instanceof BinaryOperation ? ((BinaryOperation)expr).getLeft() : new ConstantExpr(0),
             expr instanceof BinaryOperation ? ((BinaryOperation)expr).getRight() : expr);
    }

    @Override
    public Expression simplify() {

        Expression left = getLeft().simplify();
        Expression right = getRight().simplify();
        // First of all we check of both sides are constant. If true, we can directly evaluate the result...
        if (left.isConstant() && right.isConstant()) {
            return new ConstantExpr(evaluate());
        }
        Op op = getOp();
        // + and * and are commutative and associative, therefore we can reorder operands as we desire
        if (op == Op.ADD || op == Op.MULTIPLY) {
            // We prefer the have the constant part at the left side, re-order if it is the other way round.
            // This simplifies further optimizations as we can concentrate on the left side
            if (right.isConstant()) {
                Expression tmp = right;
                right = left;
                left = tmp;
            }

            if (right instanceof BinaryExpr) {
                BinaryExpr childOp = (BinaryExpr) right;
                if (left.isConstant()) {
                    // Left side is constant, we therefore can combine constants. We can rely on the constant
                    // being on the left side, since we reorder commutative operations (see above)
                    if (childOp.getLeft().isConstant()) {
                        if(childOp.getOp() == op){
                            if (op == Op.ADD) {
                                return new BinaryExpr(op,
                                    new ConstantExpr(left.evaluate() + childOp.getLeft().evaluate()),
                                    childOp.getRight());
                            } else if (op == Op.MULTIPLY) {
                                return new BinaryExpr(op,
                                    new ConstantExpr(left.evaluate() * childOp.getLeft().evaluate()),
                                    new BinaryExpr(op, left, childOp.getRight()));
                            }
                        }
                        // if child op equals + or - , current op is * , remove brackets
                        if((childOp.getOp() == Op.ADD || childOp.getOp() == Op.SUBTRACT ) && op == Op.MULTIPLY){
                            return new BinaryExpr(childOp.getOp(),
                                new ConstantExpr(left.evaluate() * childOp.getLeft().evaluate()),
                                new BinaryExpr(op, left, childOp.getRight()));
                        }
                        if((childOp.getOp() == Op.ADD || childOp.getOp() == Op.SUBTRACT ) && op == Op.DIVIDE){
                            throw new RuntimeException("Current function '" + (new BinaryExpr(op, left, right)) + " not support.");
                        }
                    }
                }
                if (childOp.getLeft().isConstant()) {
                    // Since our left side is non constant, but the left side of the child expression is,
                    // we push the constant up, to support further optimizations
                    if(childOp.getOp() == op){
                        return new BinaryExpr(childOp.getOp(),
                            childOp.getLeft(),
                            new BinaryExpr(op, left, childOp.getRight()));
                    }
                }
            }
        }
        else if (op == Op.SUBTRACT) {
            // remove brackets
            if (right.isConstant()) {
                right = new ConstantExpr(-1 * right.evaluate());
                Expression tmp = right;
                right = left;
                left = tmp;
            }

            if (right instanceof BinaryExpr) {
                BinaryExpr childOp = (BinaryExpr) right;
                if (left.isConstant()) {
                    // Left side is constant, we therefore can combine constants. We can rely on the constant
                    // being on the left side, since we reorder commutative operations (see above)
                    if (childOp.getLeft().isConstant()) {
                        if(childOp.getOp() == Op.ADD || childOp.getOp() == Op.SUBTRACT){
                            return new BinaryExpr(childOp.getOp() == Op.SUBTRACT ? Op.ADD : childOp.getOp(),
                                        new ConstantExpr(left.evaluate() - childOp.getLeft().evaluate()),
                                        childOp.getRight());
                        }
                    }
                }
                if(childOp.getOp() == Op.ADD || childOp.getOp() == Op.SUBTRACT){
                    // remove `-` identify
                    Expression childOpLeft =  childOp.getLeft().isConstant()
                                                ? new ConstantExpr(-1 * childOp.getLeft().evaluate())
                                                : new BinaryExpr(Op.MULTIPLY, new ConstantExpr(-1), childOp.getLeft());
                    Expression childOpRight = childOp.getOp() == Op.SUBTRACT
                                                ? childOp.getRight()
                                                : new BinaryExpr(Op.MULTIPLY, new ConstantExpr(-1), childOp.getRight());
                    return new BinaryExpr(Op.ADD,
                                left,
                                new BinaryExpr(childOp.getOp() == Op.SUBTRACT ? Op.ADD : childOp.getOp(),
                                    childOpLeft, childOpRight));
                }
            }
        }

        return new BinaryExpr(op, left, right);
    }

    public double getValue(){
        Expression expression = simplify();
        if(expression instanceof BinaryExpr){
            return ((BinaryExpr)expression).getValue0();
        }
        return expression.evaluate();
    }

    /** resolve function value */
    private double getValue0(){
        Expression right = getRight();
        List<ConstantExpr> constants = getConstantExpr();
        List<Variable> variables = getVariables();
        Map<Variable, Double> saveVariables = new HashMap<>(variables.size());
        try{
            initializeIfNeed(variables, saveVariables);
            return evaluate(right, constants) / getLeft().evaluate();
        }finally {
            setExclude(constants, false);
            restore(variables, saveVariables);
        }
    }

    private void restore(List<Variable> variables, Map<Variable, Double> saveVariables) {
        for(Variable variable : variables){
            Double saved = saveVariables.get(variable);
            if(saved != null) {
                variable.withValue(saved.doubleValue());
            }
        }
    }

    private void initializeIfNeed(List<Variable> variables, Map<Variable, Double> saveVariables) {
        for(Variable variable : variables){
            Double saved = saveVariables.get(variable);
            if(saved == null) {
                saveVariables.put(variable, Double.valueOf(variable.getValue()));
            }
            if(variable.getValue() == 0) {
                variable.setValue(1);
            }
        }
    }

    double evaluate(Expression right, List<ConstantExpr> constants){
        double value = right.evaluate();
        for(ConstantExpr constant : constants){
            value -= constant.evaluate();
            setExclude(constant, true);
        }
        return value;
    }

    void setExclude(List<ConstantExpr> constants, boolean exclude){
        for(ConstantExpr constant : constants){
            setExclude(constant, exclude);
        }
    }

    void setExclude(ConstantExpr constant, boolean exclude){
        constant.exclude = exclude;
    }

    List<ConstantExpr> getConstantExpr(){
        List<ConstantExpr> constants = getConstantExpr0(getLeft());
        return constants;
    }

    List<Variable> getVariables(){
        List<Variable> constants = getVariables(getLeft());
        return constants;
    }

    private List<Variable> getVariables(Expression expression){
        List<Variable> constants = new ArrayList<>();
        if(expression instanceof VariableReference){
            VariableReference reference = (VariableReference)expression;
            try {
                Field var = reference.getClass().getDeclaredField("var");
                var.setAccessible(true);
                constants.add((Variable)var.get(reference));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // ignore error
            }
        }
        if(expression instanceof BinaryExpr){
            BinaryExpr expr = (BinaryExpr)expression;
            Expression left = expr.getLeft();
            Expression right = expr.getRight();
            constants.addAll(getVariables(left));
            constants.addAll(getVariables(right));
        }
        return constants;
    }

    private List<ConstantExpr> getConstantExpr0(Expression expression){
        List<ConstantExpr> constants = new ArrayList<>();
        if(expression instanceof ConstantExpr){
            constants.add((ConstantExpr)expression);
        }
        if(expression instanceof BinaryExpr){
            BinaryExpr expr = (BinaryExpr)expression;
            if(expr.getOp() == Op.ADD || expr.getOp() == Op.SUBTRACT){
                Expression left = expr.getLeft();
                Expression right = expr.getRight();
                constants.addAll(getConstantExpr0(left));
                constants.addAll(getConstantExpr0(right));
            }
        }
        return constants;
    }

    public static Expression wrap(Expression expression){
        if(expression instanceof BinaryExpr
            || expression instanceof ConstantExpr){
            return expression;
        }
        if(expression instanceof Constant){
            return new ConstantExpr(expression.evaluate());
        }
        if(expression instanceof BinaryOperation){
            BinaryOperation expr = (BinaryOperation)expression;
            Expression left = expr.getLeft();
            Expression right = expr.getRight();
            return new BinaryExpr(expr.getOp(), wrap(left), wrap(right));
        }
        return expression;
    }

    private String toString(Op op) {
        switch (op){
            case ADD     : return "+";
            case SUBTRACT: return "-";
            case MULTIPLY: return "*";
            case DIVIDE  : return "/";
            case MODULO  : return "%";
            case POWER   : return "^";
            case LT      : return "<";
            case LT_EQ   : return "<=";
            case EQ      : return "=";
            case GT_EQ   : return ">=";
            case GT      : return ">";
            case NEQ     : return "!=";
            case AND     : return "&";
            case OR      : return "|";
        }
        return " ";
    }

    @Override
    public String toString() {
        if(getOp() == Op.EQ ){
            return getLeft() + " " + toString(getOp()) + " " + getRight();
        }
        return "(" + getLeft() + " " + toString(getOp()) + " " + getRight() + ")";
    }
}
