package com.efe.parsiiext;

import java.math.BigDecimal;
import java.math.MathContext;

import com.efe.parsiiext.BinaryExpr;

import junit.framework.TestCase;

import org.junit.Test;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.tokenizer.ParseException;

public class BinaryExprTest extends TestCase {

    @SuppressWarnings("unused")
	@Test
    public void test_solve_simple_function(){

        String expr0 = "x-x*0.06*(0.02+0.03+0.01)=3638930.77"; // expect x = 3652078.25
        String expr1 = "100 - x + 0.06*x = 3638930.77"; // expect x = -3871096.56
        String expr2 = "x-x*0.06*(0.02+0.03+0.01)=2359207.68"; // expect x = 2367731.51
        String expr3 = "x=100"; // expect x = 100
        String expr4 = "x-x*0.06*(0.02+0.03+0.01)=8000000000"; // expect x = 8.028904054596548E9
        String expr5 = "1 + 2 + 3 * 5"; // expect 18

        String expr6 = "100/20%5*7";

        expect(testExpr(expr6), 0);

        //expect(testExpr(expr0), 3652078.25);
        //expect(testExpr(expr1), -3871096.56);
        //expect(testExpr(expr2), 2367731.51);
        //expect(testExpr(expr3), 100);
        //expect(testExpr(expr4), 8.028904054596548E9);
        //expect(testExpr(expr5), 18);
    }

    static double testExpr(String expr){
        Scope scope = new Scope();
        Expression expression;
        try {
            expression = Parser.parse(expr, scope);
        } catch (ParseException e) {
            throw new RuntimeException("Parse expr fail. expr: " + expr, e);
        }
        BinaryExpr expr0 = new BinaryExpr(expression);
        return expr0.getValue();
    }

    static void expect(double actual, double expected){
        MathContext context = new MathContext(2);
        if(new BigDecimal(actual, context).equals(new BigDecimal(expected, context))){
            return;
        }
        throw new RuntimeException("expected '" + expected + "', actual '" + actual + "'");
    }
}
