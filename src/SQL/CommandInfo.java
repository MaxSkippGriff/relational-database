package SQL;

import java.io.Serializable;

/**
 * Store information about each
 * of the commands
 */
@SuppressWarnings("serial")
public class CommandInfo implements Serializable {

    CommandType cmd;
    String input;

    public CommandInfo(CommandType type, String input)
    {
        this.cmd = type;

        if (type.equals(CommandType.LITERALSTRING)) {
            input = input.substring(1,input.length()-1);
        }
        this.input = input;
    }

    public CommandType selectCommand()
    {
        return cmd;
    }

    public String selectInput()
    {
        return input;
    }

    public boolean equal(CommandType type)
    {
        return this.cmd.equals(type);
    }

    public String toString()
    {
        return input;
    }
}
