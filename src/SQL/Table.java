package SQL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to implement Table
 * consisted of Rows (inner class)
 */
@SuppressWarnings("serial")
public class Table implements Serializable {

	ArrayList<String> field;

	HashMap<Integer, Row> hm;

	Integer pointer;

	public Table(ArrayList<String> attributes)
	{
		this.field = new ArrayList<>();
		this.field.add("id");
		this.field.addAll(attributes);

		hm = new HashMap<>();
		pointer = 1;
	}

	public void insert(ArrayList<CommandInfo> lstCommands) throws GlobalErrorHandler
	{
		if (lstCommands.size() != field.size() - 1)
			throw new GlobalErrorHandler("Invalid query");

		ArrayList<CommandInfo> inCommands = new ArrayList<>();

		inCommands.add(new CommandInfo(CommandType.LITERALINT, pointer.toString()));

		inCommands.addAll(lstCommands);

		hm.put(pointer, new Row(field, inCommands));

		pointer++;
	}

	/**
	 * Inner class Row represents Table Row
	 *
	 */
	class Row implements Serializable
	{

		HashMap<String, CommandInfo> map;

		Row(ArrayList<String> attributes, ArrayList<CommandInfo> lstCommands)
		{
			map = new HashMap<>();

			for (int i = 0; i < attributes.size(); i++) {
				map.put(attributes.get(i), lstCommands.get(i));
			}
		}
	}

}
