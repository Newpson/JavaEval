import newpson.util.Parser;

public class Main
{
	public static void main(String args[])
	{
		try
		{
			double val = Parser.eval("sin(x - sin(2 * pow(1 + 3 + 4.88 / 45 - 9, 0.33 - x)) + sin(1 + 3.1415 / x)");
			System.out.println("Value: " + val);

			val = Parser.eval("3 + 3");
			System.out.println("Value: " + val);

			// We are lack of unary '-' support
			val = Parser.eval("0.0 -1.0");
			System.out.println("Value: " + val);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
