package com.efe.parsiiext;

import java.io.Reader;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.tokenizer.ParseException;

/**
 * 
 * <p>
 * 解决表达式如：x = x*0.2 + x*0.11 + 8.87 时，计算不对的问题，思路是将"="号右边的表达式移到左边，
 * 且"="号右边为：0",如上述表达式转义后为：(x)-(x*0.2 + x*0.11 + 8.87)=0，才能计算正确
 * </p>
 * 
 * @author Liu TianLong 2019年9月18日 下午4:02:22
 */
public class EscapeParser extends Parser {

	private static final String EQUAL_CHAR = "=";
	private static final String LEFT_BRACKETS_CHAR = "(";
	private static final String RIGHT_BRACKETS_CHAR = ")";
	private static final String ZERO_CHAR = "0";
	private static final String MINUS_CHAR = "-";

	protected EscapeParser(Reader input, Scope scope) {
		super(input, scope);
	}

	public static Expression parse(String input) throws ParseException {
		return Parser.parse(escapeExpression(input));
	}

	public static Expression parse(String input, Scope scope)
			throws ParseException {
		return Parser.parse(escapeExpression(input), scope);
	}

	public static String escapeExpression(String exp) {
		if (exp.contains(EQUAL_CHAR)) {
			String[] arr = exp.split(EQUAL_CHAR);
			String leftExp = arr[0], rightExp = arr[1];
			if (isNumber(leftExp) || isNumber(rightExp)) {
				return exp;
			}
			return LEFT_BRACKETS_CHAR + leftExp + RIGHT_BRACKETS_CHAR
					+ MINUS_CHAR + LEFT_BRACKETS_CHAR + rightExp
					+ RIGHT_BRACKETS_CHAR + EQUAL_CHAR + ZERO_CHAR;
		}
		return exp;
	}

	public static boolean isNumber(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
