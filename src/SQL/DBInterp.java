package SQL;

import java.io.IOException;

import java.util.ArrayList;

import java.util.List;

import java.util.regex.Matcher;

import java.util.regex.Pattern;

/**
 * 
 * Interpreter executes commands
 * of BNF once compliance
 * determined by parser
 *
 */
public class DBInterp {

	CommandBuilder commandBuilder;
	DBParser theParser;
	BNF bnf;
	QueryLanguage command;

	// constructor
	public DBInterp()
	{
		command = new QueryLanguage();
		theParser = new DBParser();
		commandBuilder = new CommandBuilder();
		bnf = new BNF();
	}

	public List<String> parse(String input) throws GlobalErrorHandler, IOException
	{
		if (input == null) {
			throw new GlobalErrorHandler("Invalid query");
		}

		ArrayList<CommandInfo> lstCommands = commandBuilder.getCommandList(input);

		if (!lstCommands.get(lstCommands.size() - 1).equal(CommandType.SEMICOLON))
			throw new GlobalErrorHandler("Requires semicolon");
		lstCommands.remove(lstCommands.size() - 1);
		lstCommands.trimToSize();

		theParser.setCommands(lstCommands);
		command.parse(theParser, bnf);
		return bnf.result();
	}

	/**
	 * Inner class for command builder
	 *
	 */
	class CommandBuilder
	{

		Pattern pattern;

		public CommandBuilder()
		{
			StringBuilder strBuilder = new StringBuilder();
			for (CommandType commandType : CommandType.values()) {
				strBuilder.append(String.format("|(?<%s>%s)", commandType.name(), commandType.pattern()));
			}
			pattern = Pattern.compile(strBuilder.substring(1), Pattern.CASE_INSENSITIVE);
		}

		public ArrayList<CommandInfo> getCommandList(String input)
		{
			Matcher matcher = pattern.matcher(input);
			ArrayList<CommandInfo> lstCommands = new ArrayList<>();
			while (matcher.find()) {
				for (CommandType commandType : CommandType.values()) {
					if (matcher.group(commandType.name()) != null) {
						lstCommands.add(new CommandInfo(commandType, matcher.group(commandType.name())));
					}
				}
			}
			return lstCommands;
		}
	}
}
