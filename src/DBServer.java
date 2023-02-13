import java.io.*;

import java.net.ServerSocket;

import java.net.Socket;

import SQL.DBInterp;

import SQL.GlobalErrorHandler;

class DBServer
{
    DBInterp interpreter;

    public DBServer(int portNumber)  {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Listening on " + portNumber);
            while(true) processNextConnection(serverSocket);
        } catch(IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

    private void processNextConnection(ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //System.out.println("Connection Established");
            interpreter = new DBInterp();
            while(true) processNextCommand(socketReader, socketWriter);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch(NullPointerException npe) {
        System.out.println("Connection Lost");
    	}
    }

    private void processNextCommand(BufferedReader socketReader, BufferedWriter socketWriter) throws IOException, NullPointerException {
        String incomingCommand = socketReader.readLine();
        System.out.println("Received message: " + incomingCommand);
        String msg;
        try {
        	// create instance of StringBuilder
        	StringBuilder out1 = new StringBuilder();
        	// execute line in interpreter, and join resulting List<String> into String
        	for (String str: interpreter.parse(incomingCommand)) {
        		 out1.append(str).append("\n");
        	}
        	msg = out1.toString();
        } catch(GlobalErrorHandler pe) {
            pe.printStackTrace();
            msg = pe.ToString();
        }
        //socketWriter.write("[OK] Thanks for your message: " + incomingCommand);
        socketWriter.write(msg +"\n"+ ((char)4) + "\n");
        socketWriter.flush();
    }
    
    public static void main(String[] args) {
    	DBServer server = new DBServer(8888);
    }

}



