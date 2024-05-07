package comp2396.ams4;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * This class represents the view of the Tic Tac Toe game. It contains the main
 * method to start the game, and all the other methods needed to handle user 
 * input and updates to the view. 
 * 
 * @author Chan Yat Fu
 * @version 1.0
 * @since 2023-05-02
 */
public class ViewController extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JLabel message;
	private JButton[][] board;
	private boolean[][] marked;
	private JMenuItem exitItem;
	private JMenuItem instructionItem;
	private JTextField nameField;
	private String name;
	
	static Socket socket;
	static ObjectOutputStream objectWriter;
	static ObjectInputStream objectReader;
	
	private int[][] ticTacToeBoard;
	private boolean enable;
	private int token = 0;

	/**
     * Constructor for the ViewController class. Sets up the UI for the game and
     * initializes the Tic Tac Toe board and game state.
     */
	public ViewController() {
		
		this.setTitle("Tic Tac Toe");
		ticTacToeBoard = new int[3][3];
		this.setSize(440,550);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		this.add(mainPanel);
		
		marked = new boolean[3][3];
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				marked[i][j] = false;
			}
		}
		
		JMenuBar menuBar = new JMenuBar();
		JMenu controlMenu = new JMenu("Control");
		exitItem = new JMenuItem("Exit");
		this.setJMenuBar(menuBar);
		menuBar.add(controlMenu);
		controlMenu.add(exitItem);
		JMenu helpMenu = new JMenu("Control");
		instructionItem = new JMenuItem("Instruction");
		menuBar.add(helpMenu);
		helpMenu.add(instructionItem);
		
        // Set up listeners for menu items
		ActionListener exitItemListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		exitItem.addActionListener(exitItemListener);
	
		ActionListener instructionItemListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, 
						"Some information about this game:\n"
						+ "Criteria for a valid move:\n"
						+ "- The move is not occupied by any mark.\n"
						+ "- The move is made in the player's turn\n"
						+ "- The move is move within the 3x3 board.\n"
						+ "The game would continue and switch among the opposite player until it reaches either one of the following conditions:\n"
						+ "- Player 1 win.\n"
						+ "- Player 2 win.\n"
						+ "- Draw.");
			}
		};
		instructionItem.addActionListener(instructionItemListener);
		
		JPanel instructionPanel = new JPanel();
		mainPanel.add(instructionPanel);
		message = new JLabel("Enter your player name...");
		instructionPanel.add(message);
		
		
		JPanel boardPanel = new JPanel();
		mainPanel.add(boardPanel);
		boardPanel.setLayout(new GridBagLayout());
		boardPanel.setBackground(Color.BLACK);
		boardPanel.setPreferredSize(new Dimension(420, 420));
		GridBagConstraints c = new GridBagConstraints();
		board = new JButton[3][3];
		for (int i = 0; i < 3; ++i) {
			c.gridy = i;
			for (int j = 0; j < 3; ++j) {
				c.gridx = j;
				board[i][j] = new JButton();
				board[i][j].setPreferredSize(new Dimension(140, 140));
				boardPanel.add(board[i][j], c);
			}
		}
		
		JPanel namePanel = new JPanel();
		mainPanel.add(namePanel);
		nameField = new JTextField(20);
		namePanel.add(nameField);
		JButton submitButton = new JButton("Submit");
		namePanel.add(submitButton);
		
		ActionListener submitButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = nameField.getText();
				if (text.trim() == "") {
					JOptionPane.showMessageDialog(null, "Please enter name");
				} else {
					name = text.trim();
					nameField.setEnabled(false);
					nameField.setText(name);
					setTitle("Tic Tac Toe - Player: " + name);
					message.setText("WELCOME " + name);
					submitButton.setEnabled(false);
					start();
				}
			}
		};
		
		submitButton.addActionListener(submitButtonListener);
		
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				board[i][j].addActionListener(new BoardListener(i,j));
			}
		}
		
	}
	
	
	/**
	 * Establishes a connection to the server at IP address "127.0.0.1" and port number 12345.
	 * Initializes objectWriter to the output stream of the socket and creates a new inputListener thread
	 * to listen for incoming messages from the server. Displays an error message and exits the program
	 * if an exception is caught during the connection process.
	 */
	private void start() {
		try {
			socket = new Socket("127.0.0.1", 12345);
			objectWriter = new ObjectOutputStream(socket.getOutputStream());
        	Runnable inputListener = new inputListener(socket);
        	Thread inputListenerThread = new Thread(inputListener);
        	inputListenerThread.start();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Game Ends. One of the player lefts.");
			System.exit(0);
		}
		
	}
	
	
	/**
	 * This method checks if the Tic Tac Toe game has ended and returns the winner or a tie.
	 * @return An integer representing the winner or tie in the game. The return value will be:
	 * 0 if the game is not over yet, 
	 * 1 if player 1 (represented by 'X') has won, 
	 * -1 if player 2 (represented by 'O') has won, 
	 * 2 if the game has ended in a tie.
	 */
	private int checkEnd() {
		for (int i = 0; i < 3; ++i) {
			int[] row = ticTacToeBoard[i];
			if (row[0] != 0 && row[0] == row[1] && row[1] == row[2]) {
				return row[0];
			}
			if (ticTacToeBoard[0][i] != 0 &&
					ticTacToeBoard[0][i] == ticTacToeBoard[1][i] &&
					ticTacToeBoard[1][i] == ticTacToeBoard[2][i]) {
				return ticTacToeBoard[0][i];
			}
		}
		if (ticTacToeBoard[0][0] != 0 &&
				ticTacToeBoard[0][0] == ticTacToeBoard[1][1] &&
				ticTacToeBoard[1][1] == ticTacToeBoard[2][2]) {
			return ticTacToeBoard[0][0];
		}
		if (ticTacToeBoard[0][2] != 0 &&
				ticTacToeBoard[0][2] == ticTacToeBoard[1][1] &&
				ticTacToeBoard[1][1] == ticTacToeBoard[2][0]) {
			return ticTacToeBoard[0][2];
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				if (ticTacToeBoard[i][j] == 0) {
					return 0;
				}
			}
		}
		return 2;
	}
	
	/**
	 * Checks if the tic-tac-toe board is empty.
	 * @return true if the board is empty, false otherwise
	 */
	private boolean checkEmpty() {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				if (ticTacToeBoard[i][j] != 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Updates the GUI board based on the current state of the Tic-Tac-Toe game board.
	 * The function iterates over the 2D array 'ticTacToeBoard' and sets the text and color of
	 * each button in the GUI board according to the value of the corresponding cell in the
	 * 'ticTacToeBoard'. If the value is 1, the button displays 'O' in green color, if the
	 * value is -1, the button displays 'X' in red color, and if the value is 0, the button
	 * remains empty.
	*/
	private void printBoard() {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				if (ticTacToeBoard[i][j] == 1) {
					board[i][j].setFont(new Font("Arial", Font.BOLD, 64));
					board[i][j].setForeground(Color.GREEN);
					board[i][j].setText("O");
					
				} else if (ticTacToeBoard[i][j] == -1) {
					board[i][j].setFont(new Font("Arial", Font.BOLD, 64));
					board[i][j].setForeground(Color.RED);
					board[i][j].setText("X");
				}
			}
		}
	}
	
	
	/**
	 * The BoardListener class implements ActionListener interface to listen for actions
	 * performed on the Tic Tac Toe board. It sets the value of the clicked cell on the board
	 * to the current player's token and sends the updated board to the opponent.
	 */
	private class BoardListener implements ActionListener
	{
		
		private int rowNum;
		private int colNum;
		
		
		/**
		 * Constructs a BoardListener object with the given row and column numbers.
		 * @param rowNum the row number of the clicked cell
		 * @param colNum the column number of the clicked cell
	 	*/
	    public BoardListener(int rowNum, int colNum)
	    {
	    	this.rowNum = rowNum;
	    	this.colNum = colNum;
	    }
	    
	    
	    /**
	     * Sets the value of the token to be placed on the board based on the total value of
	     * the board (sum of all cell values). If the total value is zero, the token is set to -1
	     * indicating that player X goes first. If the total value is non-zero, the token is set to 1
	     * indicating that the player O goes next.
	     */
	    public void setToken() {
			int totalVal = 0;
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < 3; ++j) {
					totalVal += ticTacToeBoard[i][j];
				}
			}
			if (totalVal == 0) {
				token = -1;
			} else {
				token = 1;
			}
	    }

	    
	    /**
	     * Responds to an action event, which in this case is a mouse click on a cell of the board.
	     * If the cell is empty and the game is still ongoing (enable is true), sets the cell value to
	     * the current player's token, updates the board display, and sends the updated board to the
	     * opponent via objectWriter. If the cell is not empty, the move is invalid and no action is taken.
	     * @param e the action event to be responded to
	     */
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
			if (enable == true && ticTacToeBoard[rowNum][colNum] == 0) {
				message.setText("Valid move, wait for your opponent.");
				enable = false;
				if (token == 0) {
					setToken();
				}
				ticTacToeBoard[rowNum][colNum] = token;
				try {
					objectWriter.writeObject(ticTacToeBoard);
			    	objectWriter.flush();
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}
	    }
	}
	
	
	/**
	 * This class listens to the input from the socket and updates the game state and UI accordingly.
	 */
    public class inputListener implements Runnable {
    	private Socket socket;
    	
    	/**
    	 * Constructs a new inputListener object with the given socket.
    	 * @param arg the socket object that the input stream reads from
    	 */
    	public inputListener(Socket arg) {
    		socket = arg;
        }
    	
    	
    	/**
    	 * Reads from the input stream and updates the game state and UI accordingly.
    	 * If the game has ended, displays the result in a pop-up dialog and exits the game.
    	 * If one of the players leaves, displays a pop-up dialog and exits the game.
    	 */
    	public void run() {
    		try {
    			objectReader = new ObjectInputStream(socket.getInputStream());
    			while (true) {
    				enable = (boolean) objectReader.readObject();
    				ticTacToeBoard = (int[][]) objectReader.readObject();
    				if (enable == true && checkEmpty() == false) {
    					message.setText("Your opponent has moved, now is your turn.");
    				}
    				printBoard();
    				int gameStatus = checkEnd();
    				if (gameStatus != 0) {
    					if (gameStatus == token) {
    						JOptionPane.showMessageDialog(null, "Congratulations. You Win");
    					} else if (gameStatus == -token) {
    						JOptionPane.showMessageDialog(null, "You lose.");
    					} else {
    						JOptionPane.showMessageDialog(null, "Draw");
    					}
    					System.exit(0);
    				}
    			}
    		} catch (Exception ex) {
    			JOptionPane.showMessageDialog(null, "Game Ends. One of the player lefts.");
    			System.exit(0);
    		}
    	}
    }
    
}
