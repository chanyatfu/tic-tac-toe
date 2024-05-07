package comp2396.asm4;

import java.io.*;
import java.net.*;
import java.lang.Thread;


/**
 * TicTacToeServer class represents the server-side of the Tic Tac Toe game,
 * which listens for incoming client connections, manages the game state,
 * and sends updates to connected clients.
 * 
 * @author Chan Yat Fu
 * @version 1.0
 * @since 2023-05-02
 */
public class TicTacToeServer {
	private ServerSocket serverSocket;
	private int[][] ticTacToeBoard;
    
	static Socket client1;
    static Socket client2;
    static ObjectInputStream objectReader1;
    static ObjectInputStream objectReader2;
    static ObjectOutputStream objectWriter1;
    static ObjectOutputStream objectWriter2;
    
    /**
     * Constructor for TicTacToeServer class.
     * @param port the port number to listen for incoming connections.
     * @throws IOException if an I/O error occurs when opening the server socket.
     */
    public TicTacToeServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        ticTacToeBoard = new int[3][3];
        for (int i = 0; i < 3; ++i) {
        	for (int j = 0; j < 3; ++j) {
        		ticTacToeBoard[i][j] = 0;
        	}
        }
    }
    
    /**
     * Starts the server, waits for two clients to connect, and manages the game state until the game is over.
     * @throws IOException if an I/O error occurs when accepting a connection from a client.
     * @throws ClassNotFoundException if the class of a serialized object can not be found.
     * @throws InterruptedException if any thread has interrupted the current thread.
     */
    public void start() throws IOException, ClassNotFoundException, InterruptedException {
    	client1 = serverSocket.accept();
        System.out.println("Player 1 connected.");
        client2 = serverSocket.accept();
        System.out.println("Player 2 connected.");
        
    	objectWriter1 = new ObjectOutputStream(client1.getOutputStream());
    	objectWriter2 = new ObjectOutputStream(client2.getOutputStream());
        
    	Runnable inputListener1 = new inputListener(client1);
    	Thread inputListenerThread1 = new Thread(inputListener1);
    	inputListenerThread1.start();
    	
    	Runnable inputListener2 = new inputListener(client2);
    	Thread inputListenerThread2 = new Thread(inputListener2);
    	inputListenerThread2.start();
        
    	System.out.println("Game start");
    	boolean enable1 = true;
    	boolean enable2 = false;
    	objectWriter1.writeObject(enable1);
    	objectWriter1.writeObject(ticTacToeBoard);
    	objectWriter1.flush();
    	objectWriter2.writeObject(enable2);
    	objectWriter2.writeObject(ticTacToeBoard);
    	objectWriter2.flush();
    	
    	inputListenerThread1.join();
		inputListenerThread2.join();
		
        client1.close();
        client2.close();
    }
    
    /**
     * Inner class inputListener implements Runnable interface to listen
     * for incoming messages from the clients and updates the game state.
     */
    public class inputListener implements Runnable {
    	private Socket socket;
    	
    	public inputListener(Socket arg) {
    		socket = arg;
        }
    	
    	/**
    	 * Listens for incoming messages from the clients and
    	 * updates the game state until the game is over.
    	*/
    	public void run() {
    		try {
    			ObjectInputStream objectReader = new ObjectInputStream(socket.getInputStream());
    			while (true) {
    				ticTacToeBoard = (int[][]) objectReader.readObject();
    				System.out.println(ticTacToeBoard[0][0] + " " + ticTacToeBoard[0][1] + " " + ticTacToeBoard[0][2]);
    				System.out.println(ticTacToeBoard[1][0] + " " + ticTacToeBoard[1][1] + " " + ticTacToeBoard[1][2]);
    				System.out.println(ticTacToeBoard[2][0] + " " + ticTacToeBoard[2][1] + " " + ticTacToeBoard[2][2]);
    				boolean enable1;
    				boolean enable2;
    				int totalVal = 0;
    				for (int i = 0; i < 3; ++i) {
    					for (int j = 0; j < 3; ++j) {
    						totalVal += ticTacToeBoard[i][j];
    					}
    				}
    				if (totalVal == 0) {
    					enable1 = true;
    					enable2 = false;
    				} else {
    					enable1 = false;
    					enable2 = true;
    				}
    		    	objectWriter1.writeObject(enable1);
    		    	objectWriter1.writeObject(ticTacToeBoard);
    		    	objectWriter1.flush();
    		    	objectWriter2.writeObject(enable2);
    		    	objectWriter2.writeObject(ticTacToeBoard);
    		    	objectWriter2.flush();
    				
    			}
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			System.exit(0);
    		}
    	}
    }
    
    
    /**
     * The main entry point for the Tic Tac Toe Server application.
     * This function creates a new instance of the TicTacToeServer class and starts it by calling its start() method.
     * @param args The command-line arguments passed to the application. Not used in this case.
     * @throws IOException if there is an I/O error when creating the server socket.
     * @throws ClassNotFoundException if there is an error while reading objects from the input stream.
     * @throws InterruptedException if a thread is interrupted while waiting for another thread to terminate.
     */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		TicTacToeServer server = new TicTacToeServer(12345);
		server.start();
        
	}

}
