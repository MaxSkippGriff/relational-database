package SQL;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;

import java.util.Map;

/**
 * Parse all commands with conditions
 * checking for compliance with BNF
 */
public class DBParser {

	int pointer;

	ArrayList<CommandInfo> lstCommands;

    Map<String, Condition> mathsSymbols;

	// Map containing maths symbols inspired by
	// https://www.concretepage.com/java/java-9/java-map-of-and-map-ofentries

    public DBParser() {
		mathsSymbols =
				Map.of(
						"==", new Equal(),
						">", new Greater(),
						"<", new Smaller(),
						">=", new GreaterEqual(),
						"<=", new SmallerEqual(),
						"!=", new NotEqual(),
						"LIKE", new Like());
    }

	private IExecution startParser(int scan, int scanFinish) throws GlobalErrorHandler
	{
		inspectCommand(CommandType.STR, scan);
		String variable = selectCommand(scan).selectInput();

		scan++;

		if (scan > scanFinish)
			throw new GlobalErrorHandler("Invalid query");
		inspectCommand(CommandType.MATHSSYMBOLS, scan);
		Condition theCondition;

		try {
			theCondition = mathsSymbols.get(lstCommands.get(scan).selectInput()).getClass().getConstructor()
					.newInstance();
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new GlobalErrorHandler("Invalid query");
		}

		scan++;

		if (scan > scanFinish)
			throw new GlobalErrorHandler("Invalid query");

		parseInput(scan);

		theCondition.setAttribute(variable);
		theCondition.setValue(selectCommand(scan));

		return theCondition;
	}

	private IExecution checkInput(int start, int finish) throws GlobalErrorHandler
	{
		int firstPointer = start + 1;
		int brackets = 1;

		while (!selectCommand(firstPointer).equal(CommandType.ANDOR) && brackets > 0)
		{
			if (lstCommands.get(firstPointer).equal(CommandType.OPENBRA))
				brackets++;

			if (lstCommands.get(firstPointer).equal(CommandType.OPENBRA))
				brackets--;
			firstPointer++;

			if (firstPointer > finish)
				throw new GlobalErrorHandler("Invalid query");
		}

		inspectCommand(CommandType.CLOSEBRA, firstPointer - 1);
		inspectCommand(CommandType.OPENBRA, firstPointer + 1);

		if (firstPointer + 2 >= finish)
			throw new GlobalErrorHandler("Invalid query");

		return new Execution(selectCondition(start + 1, firstPointer - 2),
				selectCommand(firstPointer).selectInput(),
				selectCondition(firstPointer + 2, finish - 1));
	}

	public void parseInput(int pointer) throws GlobalErrorHandler
	{
		if (!lstCommands.get(pointer).equal(CommandType.LITERALSTRING)
				&& !lstCommands.get(pointer).equal(CommandType.LITERALBOOL)
				&& !lstCommands.get(pointer).equal(CommandType.LITERALFLOAT)
				&& !lstCommands.get(pointer).equal(CommandType.LITERALINT))
			throw new GlobalErrorHandler("Invalid query");
	}

	public void parseInput() throws GlobalErrorHandler
	{
		parseInput(pointer);
	}

	public CommandInfo fetchCommand()
	{
		return lstCommands.get(pointer);
	}

	public CommandInfo selectCommand(int pointer)
	{
		return lstCommands.get(pointer);
	}

	public void inspectSize(int size) throws GlobalErrorHandler
	{
		if (lstCommands.size() != size)
			throw new GlobalErrorHandler("Invalid query");
	}

	public void inspectLength(int size) throws GlobalErrorHandler
	{
		if (lstCommands.size() <= size)
			throw new GlobalErrorHandler("Invalid query");
	}

	public void inspectCommand(CommandType type) throws GlobalErrorHandler
	{
		inspectCommand(type, pointer);
	}


	public void inspectCommand(CommandType type, String input) throws GlobalErrorHandler
	{
		inspectCommand(type, pointer);

		if (!lstCommands.get(pointer).selectInput().equalsIgnoreCase(input))
			throw new GlobalErrorHandler("Invalid query");
	}

	public void inspectCommand(CommandType type, int pointer) throws GlobalErrorHandler
	{
		if (!lstCommands.get(pointer).equal(type))
			throw new GlobalErrorHandler("Invalid query");
	}


	public IExecution parseCondition() throws GlobalErrorHandler
	{
		inspectCommand(CommandType.WHERE);

		iterate(1, true);

		return selectCondition(pointer, lstCommands.size() - 1);
	}

	private IExecution selectCondition(int start, int finish) throws GlobalErrorHandler
	{
		if (selectCommand(start).equal(CommandType.OPENBRA)
				&& selectCommand(finish).equal(CommandType.CLOSEBRA)) {
			return checkInput(start, finish);
		}
		else {
			return startParser(start, finish);
		}
	}

	public void determineComplete() throws GlobalErrorHandler
	{
		if (pointer != lstCommands.size() - 1)
			throw new GlobalErrorHandler("Invalid query");
	}

	public void setCommands(ArrayList<CommandInfo> lstCommands)
	{
		this.lstCommands = lstCommands;

		pointer = 0;
	}

	public ArrayList<CommandInfo> getCommands()
	{
		return lstCommands;
	}

	public int getPointer()
	{
		return pointer;
	}

	public void iterate(int i, boolean bool) throws GlobalErrorHandler
	{
		pointer += i;

		if (bool && pointer >= lstCommands.size())
			throw new GlobalErrorHandler("Invalid query");

	}


	
}
