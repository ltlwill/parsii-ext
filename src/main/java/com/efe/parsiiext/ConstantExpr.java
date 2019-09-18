package com.efe.parsiiext;

import parsii.eval.Constant;

/**
 *  Solving the equation of one variable once.
 *
 *  @author zonghai.szh
 * @email  517618650@qq.com
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
