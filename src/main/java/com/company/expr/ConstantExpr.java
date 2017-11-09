package com.company.expr;

import parsii.eval.Constant;

/**
 *  Solve the simple equation of one dollar.
 *
 *  @author zonghai.szh
 */
public class ConstantExpr extends Constant {

    double calculateValue;
    boolean exclude;

    public ConstantExpr(double value) {
        this(value, false);
    }

    public ConstantExpr(double value, boolean exclude) {
        super(value);
        if(exclude) {
            this.calculateValue = 0;
        }
        this.exclude = exclude;
    }

    @Override
    public double evaluate() {
        if(exclude){
            return this.calculateValue;
        }
        return super.evaluate();
    }

    @Override
    public String toString() {
        if(evaluate() < 0) {
            return "(" + evaluate() + ")";
        }
        return super.toString();
    }
}
