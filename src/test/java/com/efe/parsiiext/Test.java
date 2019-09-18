package com.efe.parsiiext;

import parsii.eval.Expression;
import parsii.eval.Parser;

public class Test {

	/**
	 * 
	 * <p>使用原始的 Parser: </p>
	 * @param
	 * @author Liu TianLong
	 * @date 2019年9月18日 下午4:07:07
	 * @return void
	 */
	@org.junit.Test
	public void test01() throws Exception{
		// 一元一次方程式 
		String expStr = "(x) - (x*0.2 + x*0.11 + 8.87) = 0"; // ok
//		String expStr = "x = (x*0.2 + x*0.11 + 8.87)";   // wrong
		Expression expr = Parser.parse(expStr);
		BinaryExpr bExpr = new BinaryExpr(expr);
		double x =  bExpr.getValue();
		System.out.println(x);
	}
	
	/**
	 * 
	 * <p>使用自定义的 EscapeParser: </p>
	 * @param
	 * @author Liu TianLong
	 * @date 2019年9月18日 下午4:06:52
	 * @return void
	 */
	@org.junit.Test
	public void test02() throws Exception{
		// 一元一次方程式
//		String expStr = "(x) - (x*0.2 + x*0.11 + 8.87) = 0"; // ok
		String expStr = "x = (x*0.2 + x*0.11 + 8.87)";       // ok
		Expression expr = EscapeParser.parse(expStr);
		BinaryExpr bExpr = new BinaryExpr(expr);
		double x =  bExpr.getValue();
		System.out.println(x);
	}
}
