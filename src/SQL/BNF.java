package SQL;

import java.io.*;

import java.util.ArrayList;

import SQL.Table.Row;

/**
 * Commands from BNF responsible
 * for all operations regarding
 * the database.
 */
public class BNF {

	ArrayList<String> lstString;

	File databaseFile;

	final String ex = ".dbTable";

	public BNF() {

		databaseFile = null;

		lstString = null;
	}

	private ArrayList<String> printMessage()
	{
		ArrayList<String> message = new ArrayList<>();

		message.add("[OK]");

		return message;
	}


	public void generateDatabase(String d) throws GlobalErrorHandler
	{
		File f = new File(d);

		if (f.exists()) {
			throw new GlobalErrorHandler("Invalid query");
		}
		if (!f.mkdir()) {
			throw new GlobalErrorHandler("Invalid query");
		}

		lstString = printMessage();
	}

	public void dropDatabase(String d) throws GlobalErrorHandler
	{
		File db = new File(d);

		if (!db.exists())
			throw new GlobalErrorHandler("Invalid query");

		File[] t = db.listFiles();

		if (t == null)
			throw new GlobalErrorHandler("Invalid query");

		for (File table : t) {
			if (!table.delete())
				throw new GlobalErrorHandler("Invalid query");
		}
		if (!db.delete())
			throw new GlobalErrorHandler("Invalid query");

		lstString = printMessage();
	}

	public void createTable(String t, ArrayList<String> attributes)
			throws GlobalErrorHandler, IOException
	{
		File f = obtainTB(t, false);

		if (!f.createNewFile())
			throw new GlobalErrorHandler("Invalid query");

		Table tb = new Table(attributes);

		writeTB(tb, f);

		lstString = printMessage();
	}

	public void useDatabase(String d) throws GlobalErrorHandler
	{
		databaseFile = new File(d);

		if (!databaseFile.exists())
			throw new GlobalErrorHandler("Invalid query");

		lstString = printMessage();
	}

	public void dropTable(String t) throws GlobalErrorHandler
	{
		File f = obtainTB(t, true);

		if (!f.delete())
			throw new GlobalErrorHandler("Invalid query");

		lstString = printMessage();
	}

	public void add(String t, ArrayList<CommandInfo> values)
			throws GlobalErrorHandler, IOException
	{
		File f = obtainTB(t, true);

		Table table = readTB(f);
		table.insert(values);

		writeTB(table, f);

		lstString = printMessage();
	}

	public void selectAttribute(String t, ArrayList<String> a, IExecution cond)
			throws GlobalErrorHandler, IOException
	{
		File f = obtainTB(t, true);

		Table table = readTB(f);

		if (a == null) {
			a = table.field;
		}
		else {
			for (String attribute : a) {
				if (!table.field.contains(attribute))
					throw new GlobalErrorHandler("Invalid query");
			}
		}

		ArrayList<String> output = new ArrayList<>();
		output.add("[OK]");
		StringBuilder line = new StringBuilder();

		for (String attribute : a) {
			line.append(attribute).append("\t");
		}
		output.add(line.toString());

		for (Row y : table.hm.values()) {
			if (cond == null || cond.compare(y)) {
				line.setLength(0);
				for (String attribute : a) {
					line.append(y.map.get(attribute)).append("\t");
				}
				output.add(line.toString());
			}
		}
		this.lstString = output;
	}

	public void updateBNF(String t, ArrayList<String> attributes,
						  ArrayList<CommandInfo> values, IExecution cond)
			throws GlobalErrorHandler, IOException
	{
		File f = obtainTB(t, true);

		Table table = readTB(f);

		if (!table.field.containsAll(attributes))
			throw new GlobalErrorHandler("Invalid query");

		for (Row y : table.hm.values()) {
			if (cond.compare(y)) {
				for (int i = 0; i < attributes.size(); i++) {
					y.map.replace(attributes.get(i), values.get(i));
				}
			}
		}

		writeTB(table, f);
		lstString = printMessage();
	}

	public void alterBNF(String t, String alteration, String attribute)
			throws IOException, GlobalErrorHandler
	{
		File f = obtainTB(t, true);

		Table table = readTB(f);

		switch (alteration.toUpperCase()) {
		case "ADD":

			if (table.field.contains(attribute))
				throw new GlobalErrorHandler("Invalid query");
			table.field.add(attribute);
			for (Row row : table.hm.values()) {
				row.map.put(attribute, new CommandInfo(CommandType.LITERALSTRING, "''"));
			}

			break;

		case "DROP":

			if (!table.field.contains(attribute))
				throw new GlobalErrorHandler("Invalid query");
			table.field.remove(attribute);
			for (Row row : table.hm.values()) {
				row.map.remove(attribute);
			}

			break;

		default:

			throw new GlobalErrorHandler("Invalid query");
		}

		writeTB(table, f);
		lstString = printMessage();
	}

	public void joinBNF(String firstTB, String secondTB, String firstA, String secondA)
			throws IOException, GlobalErrorHandler
	{
		File firstF = obtainTB(firstTB, true);

		File secondF = obtainTB(secondTB, true);

		Table firstT = readTB(firstF);

		Table secondT = readTB(secondF);

		if (!firstT.field.contains(firstA) || !secondT.field.contains(secondA))
			throw new GlobalErrorHandler("Invalid query");

		ArrayList<String> a = new ArrayList<>();

		for (String variable : firstT.field) {
			if (!variable.equals("id"))
				a.add(firstTB + "." + variable);
		}

		for (String variable : secondT.field) {
			if (!variable.equals("id"))
				a.add(secondTB + "." + variable);
		}

		Table combine = new Table(a);

		for (Row y1 : firstT.hm.values()) {
			for (Row y2 : secondT.hm.values()) {

				if (y1.map.get(firstA).selectInput().equals(y2.map.get(secondA).selectInput())) {
					ArrayList<CommandInfo> values = new ArrayList<>();

					for (String variable : firstT.field) {
						if (!variable.equals("id"))
							values.add(y1.map.get(variable));
					}
					for (String variable : secondT.field) {
						if (!variable.equals("id"))
							values.add(y2.map.get(variable));
					}
					combine.insert(values);
				}
			}
		}

		ArrayList<String> result = new ArrayList<>();

		StringBuilder line = new StringBuilder();

		for (String variable : combine.field) {
			line.append(variable).append("\t");
		}

		result.add(line.toString());

		for (Row y : combine.hm.values()) {
			System.out.println(" ");
			line.setLength(0);

			for (String variable : combine.field) {
				line.append(y.map.get(variable)).append("\t");
			}
			result.add(line.toString());
		}
		this.lstString = result;
	}

	public ArrayList<String> result()
	{
		return lstString;
	}

	public void deleteFrom(String t, IExecution cond)
			throws GlobalErrorHandler, IOException
	{
		File f = obtainTB(t, true);

		Table table = readTB(f);

		for (Integer i : new ArrayList<>(table.hm.keySet())) {
			Row y = table.hm.get(i);
			if (cond.compare(y)) {
				table.hm.remove(i, y);
			}
		}

		writeTB(table, f);
		lstString = printMessage();
	}

	private Table readTB(File f) throws IOException, GlobalErrorHandler
	{
		FileInputStream scan = new FileInputStream(f);
		ObjectInputStream obj = new ObjectInputStream(scan);

		try {
			Table tb = (Table) obj.readObject();
			obj.close();
			scan.close();
			obj.close();
			return tb;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			obj.close();

			throw new GlobalErrorHandler("Invalid query");
		}
	}

	private void writeTB(Table t, File f) throws IOException
	{
		FileOutputStream out = new FileOutputStream(f);

		ObjectOutputStream output = new ObjectOutputStream(out);
		output.writeObject(t);

		out.close();
		output.close();
	}

	private File obtainTB(String t, boolean bool) throws GlobalErrorHandler
	{
		if (databaseFile == null)
			throw new GlobalErrorHandler("Invalid query");

		File tf = new File(databaseFile.getName() +
				File.separator + t + ex);

		if (bool && !tf.exists()) {
			throw new GlobalErrorHandler("Invalid query");
		}

		return tf;
	}


}
