package SQL;

import static SQL.CommandType.*;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Map;

/**
 * Helper Interface Used to implement
 * all commands
 *
 */
interface Query {
	void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException;
}

/**
 * General Command class with all
 * sub-classes for specific commands
 *
 */
public class QueryLanguage implements Query {

	Map<CommandType, Query> cmd;

	public QueryLanguage()
	{
		cmd =
				Map.ofEntries(
						Map.entry(USE, new Use()), Map.entry(CREATEDATABASE, new CreateDatabase()),
						Map.entry(CREATETABLE, new CreateTable()), Map.entry(DROPDATABASE, new DropDatabase()),
						Map.entry(DROPTABLE, new DropTable()), Map.entry(INSERTINTO, new Insert()),
						Map.entry(SELECT, new Select()), Map.entry(UPDATE, new Update()),
						Map.entry(DELETEFROM, new Delete()), Map.entry(JOIN, new Join()),
						Map.entry(ALTERTABLE, new Alter())
						);
	}

	public void parse(DBParser p, BNF eng) throws GlobalErrorHandler, IOException
	{

		Query commandType = cmd.get(p.fetchCommand().selectCommand());

		if (commandType == null) {
			throw new GlobalErrorHandler("Invalid query");
		}

		p.iterate(1, true);
		commandType.parse(p, eng);
	}

	/**
	 * Inner class to represent command
	 * "CREATE DATABASE"
	 *
	 */
	class CreateDatabase implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler
		{
			int length = 2;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);
			bnf.generateDatabase(p.fetchCommand().selectInput());
		}
	}

	/**
	 * Inner class to represent command
	 * "CREATE TABLE"
	 *
	 */
	class CreateTable implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 1;

			p.inspectLength(length);
			p.inspectCommand(CommandType.STR);
			String tableName = p.fetchCommand().selectInput();

			ArrayList<String> variables = new ArrayList<>();
			if (p.getCommands().size() > 2) {
				p.iterate(1, true);
				p.inspectCommand(CommandType.OPENBRA);
				p.inspectCommand(CommandType.CLOSEBRA, p.getCommands().size() - 1);
				p.iterate(1, true);
				while (!p.fetchCommand().equal(CommandType.CLOSEBRA)) {
					p.inspectCommand(CommandType.STR);
					variables.add(p.fetchCommand().selectInput());
					p.iterate(1, true);
					if (p.getPointer() == p.getCommands().size() - 1) {
						p.inspectCommand(CommandType.CLOSEBRA);
					}
					else {
						p.inspectCommand(CommandType.COMMA);
						p.iterate(1, true);
					}
				}
			}
			p.determineComplete();
			bnf.createTable(tableName, variables);
		}
	}

	/**
	 * Inner class to represent command
	 * "DROP DATABASE"
	 *
	 */
	class DropDatabase implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler
		{
			int length = 2;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);
			bnf.dropDatabase(p.fetchCommand().selectInput());
		}
	}

	/**
	 * Inner class to represent command
	 * "DROP TABLE"
	 *
	 */
	class DropTable implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler
		{
			int length = 2;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);
			bnf.dropTable(p.fetchCommand().selectInput());
		}
	}

	/**
	 * Inner class to represent command
	 * "INSERT INTO"
	 *
	 */
	class Insert implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 5;

			p.inspectLength(length);
			p.inspectCommand(CommandType.STR);
			String tableName = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.VALUES);
			p.iterate(1, true);
			p.inspectCommand(CommandType.OPENBRA);
			p.iterate(1, true);

			p.inspectCommand(CommandType.CLOSEBRA, p.getCommands().size() - 1);

			ArrayList<CommandInfo> variables = new ArrayList<>();

			while (!p.fetchCommand().equal(CommandType.CLOSEBRA)) {
				p.parseInput();
				variables.add(p.fetchCommand());
				p.iterate(1, true);

				if (p.getPointer() == p.getCommands().size() - 1) {
					p.inspectCommand(CommandType.CLOSEBRA);
				}
				else {
					p.inspectCommand(CommandType.COMMA);
					p.iterate(1, true);
				}
			}
			p.determineComplete();
			bnf.add(tableName, variables);
		}
	}

	/**
	 * Inner class to represent command
	 * "SELECT"
	 *
	 */
	class Select implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 3;

			p.inspectLength(length);
			ArrayList<String> variables;
			if (p.fetchCommand().equal(CommandType.ASTERISK)) {
				variables = null;
				p.iterate(1, true);
				p.inspectCommand(CommandType.FROM);
			}
			else {
				variables = new ArrayList<>();
				while (!p.fetchCommand().equal(CommandType.FROM)) {
					p.inspectCommand(CommandType.STR);
					variables.add(p.fetchCommand().selectInput());
					p.iterate(1, true);
					if (p.fetchCommand().equal(CommandType.COMMA)) {
						p.iterate(1, true);
					}
					else {
						p.inspectCommand(CommandType.FROM);
					}
				}
			}

			p.iterate(1, true);
			p.inspectCommand(CommandType.STR);
			String tableName = p.fetchCommand().selectInput();

			IExecution cond = null;

			if (p.getPointer() < p.getCommands().size() - 1) {
				p.iterate(1, true);
				cond = p.parseCondition();
			}
			bnf.selectAttribute(tableName, variables, cond);
		}
	}

	/**
	 * Inner class to represent command
	 * "DELETE"
	 *
	 */
	class Delete implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 5;

			p.inspectLength(length);
			p.inspectCommand(CommandType.STR);
			String tableName = p.fetchCommand().selectInput();
			p.iterate(1, true);
			IExecution cond = p.parseCondition();
			bnf.deleteFrom(tableName, cond);
		}
	}

	/**
	 * Inner class to represent command
	 * "UPDATE"
	 *
	 */
	class Update implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 9;

			p.inspectLength(length);
			p.inspectCommand(CommandType.STR);
			String tableName = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.SET);
			p.iterate(1, true);
			ArrayList<String> variables = new ArrayList<>();
			ArrayList<CommandInfo> commands = new ArrayList<>();

			while (!p.fetchCommand().equal(CommandType.WHERE)) {
				p.inspectCommand(CommandType.STR);
				variables.add(p.fetchCommand().selectInput());
				p.iterate(1, true);
				p.inspectCommand(CommandType.EQUAL);
				p.iterate(1, true);
				p.parseInput();
				commands.add(p.fetchCommand());
				p.iterate(1, true);

				if (p.fetchCommand().equal(CommandType.COMMA)) {
					p.iterate(1, true);
				}
				else {
					p.inspectCommand(CommandType.WHERE);
				}
			}

			IExecution cond = p.parseCondition();
			bnf.updateBNF(tableName, variables, commands, cond);
		}
	}

	/**
	 * Inner class to represent command
	 * "JOIN"
	 *
	 */
	class Join implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 8;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);

			String table1 = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.ANDOR, "AND");
			p.iterate(1, true);
			p.inspectCommand(CommandType.STR);

			String table2 = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.ON);
			p.iterate(1, true);
			p.inspectCommand(CommandType.STR);

			String attribute1 = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.ANDOR);
			p.iterate(1, true);
			p.inspectCommand(CommandType.STR);

			String attribute2 = p.fetchCommand().selectInput();
			bnf.joinBNF(table1, table2, attribute1, attribute2);
		}
	}

	/**
	 * Inner class to represent command
	 * "ALTER"
	 *
	 */
	class Alter implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler, IOException
		{
			int length = 4;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);

			String tableName = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.ADDDROP);

			String alteration = p.fetchCommand().selectInput();
			p.iterate(1, true);
			p.inspectCommand(CommandType.STR);

			String attribute = p.fetchCommand().selectInput();
			bnf.alterBNF(tableName, alteration, attribute);
		}
	}

	/**
	 * Inner class to represent command
	 * "USE"
	 *
	 */
	class Use implements Query
	{

		public void parse(DBParser p, BNF bnf) throws GlobalErrorHandler
		{
			int length = 2;

			p.inspectSize(length);
			p.inspectCommand(CommandType.STR);
			bnf.useDatabase(p.fetchCommand().selectInput());
		}
	}

}
