/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knowledgegame;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 *
 * @author neaam hariri
 */
public class QuizeClient extends Application
        implements QuizConstants {
    // Indicate whether the player has the turn 

    private boolean myTurn = false;

// Indicate the token for the player 
    private int myScore = 0;

// Indicate the token for the other player 
    private int otherScore = 0;

// Create and initialize cells 
    private Cell[][] cell = new Cell[3][3];

// Create and initialize a title label 
    private Label lblTitle = new Label();

// Create and initialize a status label 
    private Label lblStatus = new Label();

// Indicate selected row and column by the current move 
    private int rowSelected;
    private int columnSelected;

// Input and output streams from/to server 
    private DataInputStream fromServer;
    private DataOutputStream toServer;

// Continue to play? 
    private boolean continueToPlay = true;

// Wait for the player to mark a cell 
    private boolean waiting = true;

// Host name or ip 
    private String host = "localhost";

    private TextField textField = new TextField();
    private  TextArea taLog = new TextArea();
    private  Button submit = new Button("submit");

    @Override // Override the start method in the Application class 
    public void start(Stage primaryStage) {


       
        BorderPane borderPane = new BorderPane();

           
            
            HBox hb = new HBox();
            hb.setSpacing(10);
            hb.setAlignment(Pos.CENTER);           
            textField.setMaxWidth(Double.MAX_VALUE);
            submit.setMaxWidth(Double.MAX_VALUE);
            
            
            HBox.setHgrow(textField, Priority.ALWAYS);
            HBox.setHgrow(submit, Priority.ALWAYS);
            
            
            hb.getChildren().add(textField);
            hb.getChildren().add(submit);
            
            
            
        //   borderPane.setTop(lblTitle);
        borderPane.setCenter(taLog);
        borderPane.setBottom(hb);
       
        

        Scene scene = new Scene(borderPane, 400, 250);
        //      Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("Quizclient"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

          // Connect to the server 
        connectToServer(taLog);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void connectToServer(TextArea taLog) {
        try {
            // Create a socket to connect to the server 
            Socket socket = new Socket(host, 8000);
            Platform.runLater(() -> taLog.appendText(new Date()
                    + ": connection server\n"));

            // Create an input stream to receive data from the server 
            fromServer = new DataInputStream(socket.getInputStream());

             // Create an output stream to send data to the server 
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

                // Control the game on a separate thread 
        new Thread(() -> {
            try {

                 // Get notification from the server 
                int player = fromServer.readInt();

                Platform.runLater(() -> taLog.appendText(" you are player" + player + "\n"));

                //check player 2 
                // Am I player 1 or 2? 
                if (player == PLAYER1) {

                    Platform.runLater(() -> taLog.appendText(" Waiting for player 2 to join\n"));

                    // Receive startup notification from the server 
                    fromServer.readInt(); // Whatever read is ignored 

                    // The other player has joined 
                    Platform.runLater(() -> taLog.appendText(" Player 2 has joined. I start first\n"));

                   // It is my turn 
                    myTurn = true;
                    Platform.runLater(() -> taLog.appendText(" it is now your turn\n"));

                } else if (player == PLAYER2) {

                    // Platform.runLater(() -> taLog.appendText( " you are Player 2\n"));
                    Platform.runLater(() -> taLog.appendText(" Waiting for player 1 to start\n"));

                }

                 // Continue to play 
                while (continueToPlay) {
                    if (player == PLAYER1) {
                                           
                      displayQuestion();
                    //  waitForPlayerAction(); // Wait for player 1 to move       
                      sendAnswer();
                    //  receiveInfoFromServer(player);
                      
                        
                 
                      //  receiveInfoFromServer(); // Receive info from the server 
                    } else if (player == PLAYER2) {
//                        receiveInfoFromServer(); // Receive info from the server 
//                        waitForPlayerAction(); // Wait for player 2 to move 
//                        sendMove(); // Send player 2's move to the server 
 //  waitForPlayerAction(); // Wait for player 1 to move  
                        displayQuestion();                            
                        sendAnswer();
                      //  receiveInfoFromServer(player);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Wait for the player to mark a cell
     */
    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }

    /**
     * Send this player's move to the server
     */
    private void sendAnswer() throws IOException {
        
         submit.setOnAction(e -> {
       //      waiting = false;
              
             int ans = Integer.parseInt(textField.getText().trim());
             try {
                 toServer.writeInt(ans);      
                 // Platform.runLater(() -> taLog.appendText(textField.getText()));
             } catch (IOException ex) {
                 Logger.getLogger(QuizeClient.class.getName()).log(Level.SEVERE, null, ex);
             }
            
          
        });
        
        
    
    }

    private void displayQuestion() throws IOException{
    
         
        String readLine = fromServer.readUTF();
        String s = readLine;
        Platform.runLater(() -> taLog.appendText(s));

    }
    /**
     * Receive info from the server
     */
    private void receiveInfoFromServer(int player) throws IOException {
// Receive game status 
        int status = fromServer.readInt();

        if (status == PLAYER1_WON) {
// Player 1 won, stop playing 
            continueToPlay = false;
            if (player == PLAYER1) {
                Platform.runLater(() -> lblStatus.setText("I won! (X)"));
            } else if (player == PLAYER2) {
                Platform.runLater(()
                        -> lblStatus.setText("Player 1 (X) has won!"));
                receiveMove();
            }
        } else if (status == PLAYER2_WON) {
            // Player 2 won, stop playing 
            continueToPlay = false;
            if (player == PLAYER2) {
                Platform.runLater(() -> lblStatus.setText("I won! (O)"));
            } else if (player == PLAYER1) {
                Platform.runLater(()
                        -> lblStatus.setText("Player 2 (O) has won!"));
                receiveMove();
            }
            // game ends and score is equal
        } else if (status == DRAW) {
            // No winner, game is over 
            continueToPlay = false;
            Platform.runLater(()
                    -> lblStatus.setText("Game is over, no winner!"));

            
        } else {//cont game
            receiveMove();
            Platform.runLater(() -> lblStatus.setText("My turn"));
            myTurn = true; // It is my turn 
        }
    }

    private void receiveMove() throws IOException {
// Get the other player's move 
        int row = fromServer.readInt();
        int column = fromServer.readInt();

        Platform.runLater(() -> cell[row][column].setToken(otherToken));
    }

// An inner class for a cell 
    public class Cell extends Pane {
// Indicate the row and column of this cell in the board 

        private int row;
        private int column;

// Token used for this cell 
        private char token = ' ';

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            this.setPrefSize(2000, 2000); // What happens without this? 
            setStyle("-fx-border-color: black"); // Set cell's border 
            this.setOnMouseClicked(e -> handleMouseClick());
        }

        /**
         * Return token
         */
        public char getToken() {
            return token;
        }

        /**
         * Set a new token
         */
        public void setToken(char c) {
            token = c;
            repaint();
        }

        protected void repaint() {
            if (token == 'X') {
                Line line1 = new Line(10, 10,
                        this.getWidth() - 10, this.getHeight() - 10);
                line1.endXProperty().bind(this.widthProperty().subtract(10));
                line1.endYProperty().bind(this.heightProperty().subtract(10));
                Line line2 = new Line(10, this.getHeight() - 10,
                        this.getWidth() - 10, 10);
                line2.startYProperty().bind(
                        this.heightProperty().subtract(10));
                line2.endXProperty().bind(this.widthProperty().subtract(10));

// Add the lines to the pane 
                this.getChildren().addAll(line1, line2);
            } else if (token == 'O') {
                Ellipse ellipse = new Ellipse(this.getWidth() / 2,
                        this.getHeight() / 2, this.getWidth() / 2 - 10,
                        this.getHeight() / 2 - 10);
                ellipse.centerXProperty().bind(
                        this.widthProperty().divide(2));
                ellipse.centerYProperty().bind(
                        this.heightProperty().divide(2));
                ellipse.radiusXProperty().bind(
                        this.widthProperty().divide(2).subtract(10));
                ellipse.radiusYProperty().bind(
                        this.heightProperty().divide(2).subtract(10));
                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.WHITE);

                getChildren().add(ellipse); // Add the ellipse to the pane 
            }
        }

        /* Handle a mouse click event */
        private void handleMouseClick() {
// If cell is not occupied and the player has the turn 
            if (token == ' ' && myTurn) {
                setToken(myToken); // Set the player's token in the cell 
                myTurn = false;
                rowSelected = row;
                columnSelected = column;
                lblStatus.setText("Waiting for the other player to move");
                waiting = false; // Just completed a successful move 
            }
        }
    }
}
