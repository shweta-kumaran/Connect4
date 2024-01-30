package application;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SampleController {
	@FXML private GridPane root;
	@FXML private Circle btn00, btn01, btn02, btn03, btn04, btn05, btn06;
	@FXML private Circle btn10, btn11, btn12, btn13, btn14, btn15, btn16;
	@FXML private Circle btn20, btn21, btn22, btn23, btn24, btn25, btn26;
	@FXML private Circle btn30, btn31, btn32, btn33, btn34, btn35, btn36;
	@FXML private Circle btn40, btn41, btn42, btn43, btn44, btn45, btn46;
	@FXML private Circle btn50, btn51, btn52, btn53, btn54, btn55, btn56;
	@FXML private Button reset;
	@FXML private ToggleButton aiToggle;
	@FXML private Label modeDescription;
	@FXML private Button saveBtn;
	@FXML private ChoiceBox<String> gameBoards;
	private boolean aiMode = false;
	/*
	 * if bool aiMode = true --> smartAI, else --> randomAI
	 */
	private static final int ROWS = 6;
	private static final int COLS = 7;
	private static final Color PLAYER_COLOR = Color.RED;
	private static final Color AI_COLOR = Color.YELLOW;
	private int gamecount = 0;
	private ArrayList<String> log = new ArrayList<>();
	private int currPlayer = 1;
	/*
	 * set player to go first
	 * currPlayer = 1 means player
	 * currPlayer = 2 means ai
	 */
	private List<List<Integer>> board = new ArrayList<>();
	//private int[][] board = new int[6][7];
	/*
	 * board[row][col] = 0 means place not taken yet
	 * board[row][col] = 1 player has taken spot
	 * board[row][col] = 2 ai has taken spot
	 */
			
	public void initialize() {
		aiToggle.setText("Hard Mode: disabled");
		boardCleanup();
		File f = new File("boards");
		if (!f.exists())
		{
			f.mkdir();
		}
		File[] list = f.listFiles();
		gameBoards.getItems().clear();
		for (File i : list)
		{
			if(i.isFile())
			{
				gameBoards.getItems().add(i.getName());
				gamecount++;
			}
		}
	}
	
	public void reset(ActionEvent e)
	{
		gamecount++;
		boardCleanup();
	}
	
	public void aiToggleClick(ActionEvent e)
	{
		aiMode = !aiMode;
		if (aiMode) { aiToggle.setText("Hard Mode: enabled");}
		else {aiToggle.setText("Hard Mode: disabled");}
	}
	
	private Circle getCircleById(String id)
	{
		for(Node node: root.getChildren())
		{
			if (node instanceof Circle && node.getId() != null && node.getId().equals(id))
			{
				return (Circle) node;
			}
		}
		return null;
	}
	
	public int rowDrop(int c) {
		for (int i = 0; i < ROWS; i++)
		{
			if (board.get(i).get(c) == 0)
			{
				return i;
			}

		}
		return -1;
	}
	
	public boolean boardFull()
	{
		for (int i = 0; i < ROWS; i++)
		{
			for (int j = 0; j < COLS; j++)
			{
				if (board.get(i).get(j) == 0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public void slotPlayerClick(MouseEvent e)
	{
		//Create circle and set it as source
		Circle circ = new Circle(35);
		circ = (Circle) e.getSource();
		
		//read button btn and extract row and col 
		String id = circ.getId();
		char btnr = id.charAt(3);
		char btnc = id.charAt(4);
		
		//convert the column to an integer
		int c = Character.getNumericValue(btnc);
		
		//check if column is free for token
		int freeRow = rowDrop(c);
	
		
		if (freeRow != -1 && currPlayer == 1) {
			board.get(freeRow).set(c,  currPlayer);
			btnr = Character.forDigit(freeRow, 10);
			id = "btn" + btnr + btnc;
			circ = (Circle) getCircleById(id);
			circ.setFill(PLAYER_COLOR);
			log.add("Player: " + currPlayer + " " + freeRow + " " + c);
			//circ.setDisable(true);
			
			//check is board is full or if they win?
			if (currPlayerWin(freeRow, c))
			{
				winnerPopup();
			}
			else if (boardFull())
			{
				boardFullPopup();
				System.out.println("Game Over: Tie");
			} 
			
			//ai goes next if not game over
			//PauseTransition pause = new PauseTransition(Duration.seconds(2));
			System.out.println(aiMode);
			currPlayer = 2;
			if (aiMode) { smartAI(); } else { randomAI(); };
		
		}
		else
		{
			//col is full
			if (boardFull())
			{
				boardFullPopup();
				System.out.println("Game Over: Tie");
			}
			else
			{
				columnFullPopup();
				System.out.println("The column is filled, please pick a different column!");
			}
		}
		//at end check if board is complete
		if (boardFull())
		{
			boardFullPopup();
			System.out.println("Game Over: Tie");
		}
		
	}
	
	public void randomAI()
	{
		Random random = new Random();
		int randomCol;
		int row;
		char btnr;
		char btnc;
		Circle circ = new Circle(35);
		while(true) { 
			randomCol = random.nextInt(7);
			row = rowDrop(randomCol);
			if (row != -1)
			{
				break;
			}
		}
		board.get(row).set(randomCol, currPlayer);
		btnr = Character.forDigit(row, 10);
		btnc = Character.forDigit(randomCol, 10);
		String id = "btn" + btnr + btnc;
		circ = (Circle) getCircleById(id);
		circ.setFill(AI_COLOR);
		log.add("Player: " + currPlayer + " " + row + " " + randomCol);
	
		if (currPlayerWin(row, randomCol))
		{
			winnerPopup();
		}
		if (boardFull())
		{
			System.out.println("Game Over: Tie");
		} 
		currPlayer = 1;
	}
	
	public void smartAI()
	{
		char btnr, btnc;
		String id;
		Circle circ;
		// move that would win for ai
		for (int col = 0 ; col < COLS; col++)
		{
			int row = rowDrop(col);
			if (row != -1)
			{
				board.get(row).set(col, currPlayer);
				if (currPlayerWin(row, col))
				{
					btnr = Character.forDigit(row, 10);
					btnc = Character.forDigit(col, 10);
					id = "btn" + btnr + btnc;
					circ = (Circle) getCircleById(id);
					circ.setFill(AI_COLOR);
					log.add("Player: " + currPlayer + " " + row + " " + col);
					winnerPopup();
					currPlayer = 1;
					return;
				}
				board.get(row).set(col, 0);
			}
		}
		
		// block player where has 3 chips in any direction
		for (int col = 0 ; col < COLS; col++)
		{
			int row = rowDrop(col);
			if (row != -1)
			{
				board.get(row).set(col, 3 - currPlayer);
				if (currPlayerWin(row, col))
				{
					board.get(row).set(col, currPlayer);
					btnr = Character.forDigit(row, 10);
					btnc = Character.forDigit(col, 10);
					id = "btn" + btnr + btnc;
					circ = (Circle) getCircleById(id);
					circ.setFill(AI_COLOR);
					log.add("Player: " + currPlayer + " " + row + " " + col);
					currPlayer = 1;
					return;
				}
				board.get(row).set(col, 0);
			}
		}
		
		// ai most chips in line
		int mostChips = -1;
		int mostcol = -1;
		int chips = 0 ;
		for (int col = 0; col < COLS; col++)
		{
			int row = rowDrop(col);
			if (row != -1)
			{
				board.get(row).set(col,  currPlayer);
				chips = getMost(row, col);
				board.get(row).set(col, 0);
				if (chips > mostChips)
				{
					mostChips = chips;
					mostcol = col;
				}
			}
		}
		if (mostcol != -1)
		{
			int row = rowDrop(mostcol);
			board.get(row).set(mostcol,  currPlayer);
			btnr = Character.forDigit(row, 10);
			btnc = Character.forDigit(mostcol, 10);
			id = "btn" + btnr + btnc;
			circ = (Circle) getCircleById(id);
			circ.setFill(AI_COLOR);
			log.add("Player: " + currPlayer + " " + row + " " + mostcol);
			currPlayer = 1;
			return;
		}
		
		// else random
		randomAI();
	}
	
	private int getMost(int row, int col)
	{
		int a = horizontalWinTest(row, col);
		int b = verticalWinTest(row, col);
		int c = diag1WinTest(row, col);
		int d = diag2WinTest(row, col);
		
		if (a >= b && a >= c && a >= d)
		{
			return a;
		}
		else if (b >= a && b >= c && b >= d)
		{
			return b;
		}
		else if (c >= a && c >= b && c >= d)
		{
			return c;
		}
		else
		{
			return d;
		}
	}
	
	public void columnFullPopup()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Column Full!");
		alert.setHeaderText(null);
		alert.setContentText("The column you selected is FULL! Pick another column!");
		ButtonType exit = new ButtonType("Close");
		alert.getButtonTypes().setAll(exit);
		alert.showAndWait().ifPresent(buttonType -> {
			alert.close();
		});
		System.out.println("Column Full");
	}

	public void boardFullPopup()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Game Over!");
		alert.setHeaderText(null);
		alert.setContentText("Board is Full, TIE!");
		ButtonType reset = new ButtonType("Play Again");
		ButtonType exit = new ButtonType("Close");
		alert.getButtonTypes().setAll(reset, exit);
		alert.showAndWait().ifPresent(buttonType -> {
			if (buttonType == reset)
			{
				boardCleanup();
			} else if (buttonType == exit)
			{
				alert.close();
			}
		});
		System.out.println("Board Full, Tie!");
	}
	
	public void winnerPopup()
	{
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Game Over!");
		alert.setHeaderText(null);
		alert.setContentText("Player " + currPlayer + " Wins!");
		ButtonType reset = new ButtonType("Play Again");
		ButtonType exit = new ButtonType("Close");
		alert.getButtonTypes().setAll(reset, exit);
		alert.showAndWait().ifPresent(buttonType -> {
			if (buttonType == reset)
			{
				boardCleanup();
			} else if (buttonType == exit)
			{
				alert.close();
			}
		});
		for (List<Integer> r : board)
		{
			for (Integer element: r)
			{
				System.out.println(element + " ");
			}
			System.out.println();
		}
		System.out.println(currPlayer + " Wins!");
		
	}
	
	public boolean currPlayerWin(int row, int col)
	{
		//check vertical
		//check horizontal
		//check diagonal
		if (horizontalWinTest(row, col) >= 4 || verticalWinTest(row, col) >= 4|| diag1WinTest(row, col) >= 4 || diag2WinTest(row, col) >= 4)
		{
			return true;
		}
		return false;
	}
	
	private int horizontalWinTest(int row, int col)
	{
		int count = 1;
		for(int i = 1; i < 4; i++)
		{
			int nc1 = col + i;
			if (validPos(row, nc1) && board.get(row).get(nc1) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		
		for(int i = 1; i < 4; i++)
		{
			int nc2 = col - i;
			if (validPos(row, nc2) && board.get(row).get(nc2) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		
		return count;
	}
	
	private int verticalWinTest(int row, int col)
	{
		int count = 1;
		for(int i = 1; i < 4; i++)
		{
			int nr1 = row + i;
			if (validPos(nr1, col) && board.get(nr1).get(col) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		for(int i = 1; i < 4; i++)
		{
			int nr2 = row - i;
			if (validPos(nr2, col) && board.get(nr2).get(col) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		
		return count;
	}
	
	private int diag1WinTest(int row, int col)
	{
		int count = 1;
		for(int i = 1; i < 4; i++)
		{
			int nr1 = row + i;
			int nc1 = col + i;
			if (validPos(nr1, nc1) && board.get(nr1).get(nc1) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		for(int i = 1; i < 4; i++)
		{
			int nr2 = row - i;
			int nc2 = col - i;
			if (validPos(nr2, nc2) && board.get(nr2).get(nc2) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		
		return count;
	}
	
	private int diag2WinTest(int row, int col)
	{
		int count = 1;
		for(int i = 1; i < 4; i++)
		{
			int nr1 = row + i;
			int nc1 = col - i;
			if (validPos(nr1, nc1) && board.get(nr1).get(nc1) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		for(int i = 1; i < 4; i++)
		{
			int nr2 = row - i;
			int nc2 = col + i;
			if (validPos(nr2, nc2) && board.get(nr2).get(nc2) == board.get(row).get(col))
			{
				count++;
			}
			else
			{
				break;
			}
		}
		
		return count;
	}
	
	
	private boolean validPos(int r, int c)
	{
		if (r >= 0 && r < ROWS && c >= 0 && c < COLS)
		{
			return true;
		}	
		return false;
	}
	
	public void saveGame()
	{
		File f = new File("boards/gameBoardLog-" + gamecount + ".txt");
		gameBoards.getItems().add("gameBoardLog-" + gamecount + ".txt");
		try {
			FileWriter fout = new FileWriter("boards/gameBoardLog-" + gamecount + ".txt");
			
			for (String l : log)
			{
				fout.write(l);
				fout.write('\n');
			}
			fout.close();
		} catch (Exception e)
		{
			System.out.println("error");
		}
	}
	
	public void loadGame(ActionEvent e)
	{
		String fileSelected = gameBoards.getValue();
		File f = new File("boards/" + fileSelected);
		if (fileSelected == null || !f.exists())
		{
			System.out.println("Need to select a proper board");
		}
		else
		{
			try
			{
				Scanner s = new Scanner(f);
				log.clear();
				boardCleanup();
				while (s.hasNextLine())
				{
					String line = s.nextLine();
					log.add(line);
					int player = Character.getNumericValue(line.charAt(8));
					int row = Character.getNumericValue(line.charAt(10));
					int col = Character.getNumericValue(line.charAt(12));
					System.out.println(player);
					System.out.println(row);
					System.out.println(col);
					if (player == 1)
					{
						board.get(row).set(col, 1);
						char btnr = Character.forDigit(row, 10);
						char btnc = Character.forDigit(col, 10);
						String id = "btn" + btnr + btnc;
						Circle circ = (Circle) getCircleById(id);
						circ.setFill(PLAYER_COLOR);
					}
					else
					{
						board.get(row).set(col, 2);
						char btnr = Character.forDigit(row, 10);
						char btnc = Character.forDigit(col, 10);
						String id = "btn" + btnr + btnc;
						Circle circ = (Circle) getCircleById(id);
						circ.setFill(AI_COLOR);
					}
				}
			} catch (IOException e1)
			{
				System.out.println("Error");
			}
		}
	}
	
	public void boardCleanup(){
		board.clear();
		log.clear();
		for(int i = 0; i < ROWS; i++)
		{
			List<Integer> rowList =  new ArrayList<>();
			for(int j = 0; j < COLS; j++)
			{
				Circle c = (Circle) getCircleById("btn"+i+j);
				c.setFill(Color.web("#e0e2e4"));
				c.setDisable(false);
				rowList.add(0);
			}
			board.add(rowList);
		}
	}
}
