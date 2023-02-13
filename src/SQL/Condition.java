package SQL;

import java.util.List;

import SQL.Table.Row;

/**
 * Interface to get abstraction of all operations
 */
interface IExecution {
	boolean compare(Row row) throws GlobalErrorHandler;
}

/**
 * Helper class to implement Operation
 *
 */
class Execution implements IExecution {
	IExecution operationLeft;
	IExecution operationRight;
	String typeOfJoin;
	
	// constructor
	public Execution(IExecution operationLeft, String typeOfJoin, IExecution operationRight)
	{
		this.operationLeft = operationLeft;
		this.typeOfJoin = typeOfJoin;
		this.operationRight = operationRight;

	}

	public boolean compare(Row row) throws GlobalErrorHandler
	{
		switch (typeOfJoin.toUpperCase()) {

		case "OR":
			return operationLeft.compare(row) || operationRight.compare(row);

		case "AND":
			return operationLeft.compare(row) && operationRight.compare(row);

		default:
			throw new GlobalErrorHandler("Invalid query");
		}

	}
}

/**
 * General class to implement Condition
 * All specific conditions will be implemented as sub-classes (inner classes)
 *
 */
public abstract class Condition implements IExecution {

	CommandInfo cmdInfo;

	String attribute;

	List<CommandType> cmdList = null;


	public void setValue(CommandInfo value) throws GlobalErrorHandler
	{
		if (!cmdList.contains(value.selectCommand()))
			throw new GlobalErrorHandler("Invalid query");

		cmdInfo = value;
	}

	public void setAttribute(String attribute)
	{
		this.attribute = attribute;
	}

	CommandInfo getAttribute(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = row.map.get(attribute);

		if (a == null)
			throw new GlobalErrorHandler("Invalid query");
		return a;
	}

	
}

/**
 * Inner class to represent condition "=="
 */
class Equal extends Condition {

	public Equal()
	{
		cmdList = List.of(CommandType.LITERALSTRING, CommandType.LITERALBOOL, CommandType.LITERALINT, CommandType.LITERALFLOAT);
	}
	
	@Override
	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		switch (a.selectCommand()) {

		case LITERALSTRING:

		case LITERALBOOL:

			if (!a.equal(cmdInfo.selectCommand()))
				throw new GlobalErrorHandler("Invalid query");
			return a.selectInput().equals(cmdInfo.selectInput());

		case LITERALINT:

		case LITERALFLOAT:

			if (!cmdInfo.equal(CommandType.LITERALINT) && !cmdInfo.equal(CommandType.LITERALFLOAT))
				throw new GlobalErrorHandler("Invalid query");

			return Float.parseFloat(a.selectInput()) == Float.parseFloat(cmdInfo.selectInput());

		default:
			throw new GlobalErrorHandler("Invalid query");
		}
	}
}

/**
 * Inner class to represent condition ">="
 */
class GreaterEqual extends Condition {

	public GreaterEqual()
	{
		cmdList = List.of(CommandType.LITERALINT, CommandType.LITERALFLOAT);
	}
	
	@Override
	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);
		if (!cmdList.contains(a.selectCommand()))
			throw new GlobalErrorHandler("Invalid query");
		return Float.parseFloat(a.selectInput()) >= Float.parseFloat(cmdInfo.selectInput());
	}
}

/**
 * Inner class to represent condition ">"
 */
class Greater extends Condition
{

	public Greater()
	{
		cmdList = List.of(CommandType.LITERALINT, CommandType.LITERALFLOAT);
	}

	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		if (!cmdList.contains(a.selectCommand()))
			throw new GlobalErrorHandler("Invalid query");
		return Float.parseFloat(a.selectInput()) > Float.parseFloat(cmdInfo.selectInput());
	}
}

/**
 * Inner class to represent condition "!="
 */
class NotEqual extends Condition {

	public NotEqual()
	{
		cmdList = List.of(CommandType.LITERALSTRING, CommandType.LITERALBOOL, CommandType.LITERALINT,
				CommandType.LITERALFLOAT);
	}
	
	@Override
	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		switch (a.selectCommand()) {

		case LITERALSTRING:

		case LITERALBOOL:

			if (!a.equal(cmdInfo.selectCommand()))
				throw new GlobalErrorHandler("Invalid query");
			return !a.selectInput().equals(cmdInfo.selectInput());

		case LITERALINT:

		case LITERALFLOAT:

			if (!cmdInfo.equal(CommandType.LITERALINT) && !cmdInfo.equal(CommandType.LITERALFLOAT))
				throw new GlobalErrorHandler("Invalid query");

			return Float.parseFloat(a.selectInput()) != Float.parseFloat(cmdInfo.selectInput());

		default:
			throw new GlobalErrorHandler("Invalid query");
		}
	}
}

/**
 * Inner class to represent condition "<="
 */
class SmallerEqual extends Condition {

	public SmallerEqual()
	{
		cmdList = List.of(CommandType.LITERALINT, CommandType.LITERALFLOAT);
	}

	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		if (!cmdList.contains(a.selectCommand()))
			throw new GlobalErrorHandler("Invalid query");

		return Float.parseFloat(a.selectInput()) <= Float.parseFloat(cmdInfo.selectInput());
	}
}

/**
 * Inner class to represent condition "<"
 */
class Smaller extends Condition {

	public Smaller()
	{
		cmdList = List.of(CommandType.LITERALINT, CommandType.LITERALFLOAT);
	}

	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		if (!cmdList.contains(a.selectCommand()))
			throw new GlobalErrorHandler("Invalid query");

		return Float.parseFloat(a.selectInput()) < Float.parseFloat(cmdInfo.selectInput());
	}
}

/**
 * Inner class to represent condition "LIKE"
 */
class Like extends Condition {

	public Like()
	{
		cmdList = List.of(CommandType.LITERALSTRING);
	}

	public boolean compare(Row row) throws GlobalErrorHandler
	{
		CommandInfo a = getAttribute(row);

		if (!a.equal(CommandType.LITERALSTRING))
			throw new GlobalErrorHandler("Invalid query");
		
		return a.selectInput().contains(cmdInfo.selectInput());
	}
}
