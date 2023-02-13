package SQL;


import java.io.Serializable;

/**
 * All BNF commands including parenthesis,
 * conditions, joins etc stored in public
 * enum.
 * Serialization inspired by https://www.programcreek.com/2014/01/java-serialization/
 */
public enum CommandType implements Serializable {

    // Enum inspired by
    // HTTPMethod enum from
    // https://github.com/oprvc/oprvc.github.io/tree/1a18852450c9a6da111f8d9e54c6cae5f8d26918

    // List of simplified query language
    // keywords using BNF grammar
    USE("USE"), CREATEDATABASE("CREATE DATABASE"), CREATETABLE("CREATE TABLE"), INSERTINTO("INSERT INTO"),
    VALUES("VALUES"), SELECT("SELECT"), ASTERISK("\\*"), COMMA(","), FROM("FROM"), WHERE("WHERE"),
    DROPDATABASE("DROP DATABASE"), DROPTABLE("DROP TABLE"), ALTERTABLE("ALTER TABLE"), ADDDROP("ADD|DROP"),
    UPDATE("UPDATE"), SET("SET"), LITERALSTRING("'[^']*'"), LITERALBOOL("true|false"),
    DELETEFROM("DELETE FROM"), JOIN("JOIN"), ON("ON"), ANDOR("AND|OR"),

    // List of synmbols
    LITERALFLOAT("[-+]?[0-9]*\\.[0-9]+"), LITERALINT("[-+]?[0-9]+"), MATHSSYMBOLS("\\>\\=|\\<\\=|\\=\\=|\\>|\\<|\\!\\=|LIKE"),
    EQUAL("\\="), SEMICOLON(";"), OPENBRA("\\("), CLOSEBRA("\\)"), STR("[A-Za-z][A-Za-z0-9_$.]+");

    private final String cmd;

    CommandType(String cmd) {
        this.cmd = cmd;
    }

    public final String pattern() {
        return cmd;
    }

}
