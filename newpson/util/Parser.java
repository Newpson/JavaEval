package newpson.util;

import java.lang.Character;
import java.lang.Exception;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

public class Parser
{
	private static final int PRIORITY_MAX = 10;
	private static final int PRIORITY_MIN = 0;

	private static final int TYPE_UNDEFINED = -1;
	private static final int TYPE_LITERAL = 0;
	private static final int TYPE_VARIABLE = 1;
	private static final int TYPE_PARENTHESIS_LEFT = 2;
	private static final int TYPE_PARENTHESIS_RIGHT = 3;
	private static final int TYPE_COMMA = 4;
	private static final int TYPE_OPERATOR = 5;
	private static final int TYPE_FUNCTION = 6;

	// private static boolean shouldAskForVariables = true;
	private static String delimiters;
	private static Stack<Evaluable> evalStack;
	private static Stack<Double> valStack;
	static
	{
		evalStack = new Stack<Evaluable>();
		valStack = new Stack<Double>();
	}

	private static HashMap<String, Evaluable> library;
	static
	{
		library = new HashMap<>();
		library.put("(", new Evaluable() {
			@Override
			public int type() { return TYPE_PARENTHESIS_LEFT; }
		});
		library.put(")", new Evaluable() {
			@Override
			public double eval() throws Exception {
				Evaluable current = evalStack.peek();
				for (; current.type() != TYPE_PARENTHESIS_LEFT; current = evalStack.peek())
				{
					double value = evalStack.peek().eval();
					if (current.type() == TYPE_OPERATOR)
						valStack.push(value);
					evalStack.pop(); // pop just evaluated operator
				}
				evalStack.pop(); // pop "("
				if (!evalStack.empty()) {
					current = evalStack.peek();
					if (current.type() == TYPE_FUNCTION) {
						valStack.push(current.eval());
						evalStack.pop(); // pop just evaluated function
					}
				}
				return 0.0;
			}
			@Override
			public int type() { return TYPE_PARENTHESIS_RIGHT; }
		});
		library.put(",", new Evaluable() {
			@Override
			public int type() { return TYPE_COMMA; }
		});
		library.put("+", new Evaluable() {
			@Override
			public double eval() throws Exception {
				return pullLastValue() + pullLastValue();
			}
			@Override
			public int priority() { return PRIORITY_MAX-2; }
			@Override
			public int type() { return TYPE_OPERATOR; }
		});
		library.put("-", new Evaluable() {
			@Override
			public double eval() throws Exception {
				return -pullLastValue() + pullLastValue();
			}
			@Override
			public int priority() { return PRIORITY_MAX-2; }
			@Override
			public int type() { return TYPE_OPERATOR; }
		});
		library.put("*", new Evaluable() {
			@Override
			public double eval() throws Exception {
				return pullLastValue() * pullLastValue();
			}
			@Override
			public int priority() { return PRIORITY_MAX-1; }
			@Override
			public int type() { return TYPE_OPERATOR; }
		});
		library.put("/", new Evaluable() {
			@Override
			public double eval() throws Exception {
				double right = pullLastValue();
				double left = pullLastValue();
				return left / right;
			}
			@Override
			public int priority() { return PRIORITY_MAX-1; }
			@Override
			public int type() { return TYPE_OPERATOR; }
		});
		library.put("sin", new Evaluable() {
			@Override
			public double eval() throws Exception {
				return Math.sin(pullLastValue());
			}
			@Override
			public int type() { return TYPE_FUNCTION; }
		});
		library.put("pow", new Evaluable() {
			@Override
			public double eval() throws Exception {
				double degree = pullLastValue();
				double base = pullLastValue();
				return Math.pow(base, degree);
			}
			@Override
			public int type() { return TYPE_FUNCTION; }
		});
		library.put("x", new Evaluable() {
			@Override
			public double eval() throws Exception { return 10.0; }
			@Override
			public int type() { return TYPE_VARIABLE; }
		});

		updateDelimiters();
	}

	private interface Evaluable
	{
		default public double eval() throws Exception { return 0.0; }
		default public int type() { return TYPE_UNDEFINED; }
		default public int priority() { return PRIORITY_MAX; }
	}
	
	private static double pullLastValue() throws Exception
	{
		if (valStack.empty())
			throw new Exception("lack of values in the stack (check number of arguments and parentheses)");
		double value = valStack.peek();
		valStack.pop();
		return value;
	}

	private static boolean isLiteral(final String token)
	{
		return Character.isDigit(token.charAt(0));
	}

	private static void updateDelimiters()
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Evaluable> entry: library.entrySet())
		{
			int type = entry.getValue().type();
			switch (type)
			{
				case TYPE_OPERATOR:
				case TYPE_COMMA:
				case TYPE_PARENTHESIS_LEFT:
				case TYPE_PARENTHESIS_RIGHT:
					sb.append(entry.getKey());
					break;
			}
		}
		delimiters = sb.toString();
	}

	public static double eval(String expression) throws Exception
	{
		valStack.clear();
		evalStack.clear();
		expression = "(" + expression.replace(" ", "") + ")";
		StringTokenizer tokenizer = new StringTokenizer(expression, delimiters, true);
		
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if (isLiteral(token))
				valStack.push(Double.valueOf(token));
			else if (library.containsKey(token))
			{
				Evaluable current = library.get(token);
				switch (current.type()) {
					case TYPE_VARIABLE:
						valStack.push(current.eval());
						break;
					case TYPE_FUNCTION:
						evalStack.push(current);
						break;
					case TYPE_OPERATOR:
						Evaluable top = evalStack.empty() ? null : evalStack.peek();
						if (top != null && top.type() == TYPE_OPERATOR && top.priority() >= current.priority())
						{
							valStack.push(top.eval());
							evalStack.pop(); // pop just evaluated operator
						}
						evalStack.push(current);
						break;
					case TYPE_PARENTHESIS_LEFT:
					case TYPE_COMMA:
						evalStack.push(current);
					case TYPE_PARENTHESIS_RIGHT:
						current.eval();
						break;
					default:
						throw new Exception("can't evaluate undefined \"" + token + "\"");
				}
			}
			else 
				throw new Exception("unknown token \"" + token + "\"");
		}
		
		return valStack.peek();
	}
}
