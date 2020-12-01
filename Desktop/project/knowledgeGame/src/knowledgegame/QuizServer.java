/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knowledgegame;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 *
 * @author neaam hariri
 */
public class QuizServer extends Application implements QuizConstants {

    private int sessionNo = 1; // Number a session

    @Override
    public void start(Stage stage) {

        TextArea taLog = new TextArea();
        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        stage.setTitle("QuizServer"); // Set the stage title
        stage.setScene(scene); // Place the scene in the stage
        stage.show(); // Display the stage

        new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> taLog.appendText(new Date()
                        + ": Server started at socket 8000\n"));

                // Ready to create a session for every two players
                while (true) {
                    Platform.runLater(() -> taLog.appendText(new Date()
                            + ": Wait for players to join session " + sessionNo + '\n'));

                    // Connect to player 1
                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() + ": Player 1 joined session "
                                + sessionNo + '\n');
                        taLog.appendText("Player 1's IP address"
                                + player1.getInetAddress().getHostAddress() + '\n');
                    });

                    // Notify that the player is Player 1
                    new DataOutputStream(
                            player1.getOutputStream()).writeInt(PLAYER1);

                    // Connect to player 2
                    Socket player2 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date()
                                + ": Player 2 joined session " + sessionNo + '\n');
                        taLog.appendText("Player 2's IP address"
                                + player2.getInetAddress().getHostAddress() + '\n');
                    });

                    // Notify that the player is Player 2
                    new DataOutputStream(
                            player2.getOutputStream()).writeInt(PLAYER2);

                    // Display this session and increment session number
                    Platform.runLater(()
                            -> taLog.appendText(new Date()
                                    + ": Start a thread for session " + sessionNo++ + '\n'));

                    // Launch a new thread for this session of two players
                    new Thread(new HandleASession(player1, player2,taLog)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
     public static void main(String[] args) {
        Application.launch(args);
    }
     
     
     public ArrayList<Quiz> quizSetUp() throws FileNotFoundException{
     
         Quiz q;
          
         ArrayList<Quiz> question = new ArrayList<>();
        
        File inputF = new File("Group16_GameCPIS334-Ch5.txt");//declarig file inputF to read from
        Scanner input = new Scanner(inputF);//creatig scanner to read data from the file inputF
        if (!inputF.exists()) {     //checking if the file exists then exit if it doesnt
            System.out.println("File not found");
            System.exit(0);
        }
        while (input.hasNext()) {
            String[] Ques = input.nextLine().split("\\s\\s");//reading the question and choices-splitting when encountring double space- then store it in an array 
            char ans=input.nextLine().charAt(0);
            String [] choice ={Ques[1],Ques[2],Ques[3],Ques[4]};
             q = new Quiz(Ques[0],choice,ans);
           // System.out.println(q.toString());
             
            question.add(q);
            
        
        }
        return question;
     
     
     }


    class HandleASession implements Runnable, QuizConstants {

        private Socket player1;
        private Socket player2;

        // Create and initialize cells
         private  ArrayList<Quiz> question ;
         private  TextArea taLog ;
        private int player1Score = 0;
        private int player2Score = 0;
        private int rounds = 5;
        

        private DataInputStream fromPlayer1;

        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;

        // Continue to play
        private boolean continueToPlay = true;

        /**
         * Construct a thread
         */
        public HandleASession(Socket player1, Socket player2, TextArea taLog ) throws FileNotFoundException {
            this.player1 = player1;
            this.player2 = player2;
            this.taLog = taLog;
            
              Platform.runLater(()
                            -> taLog.appendText( "before quiz setup \n"));
            question = quizSetUp();
             Platform.runLater(()
                            -> taLog.appendText( "after quiz setup \n"));



            
        }

        /**
         * Implement the run() method for the thread
         */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream fromPlayer1 = new DataInputStream(
                        player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(
                        player1.getOutputStream());
                DataInputStream fromPlayer2 = new DataInputStream(
                        player2.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(
                        player2.getOutputStream());

                // Write anything to notify player 1 to start
                // This is just to let player 1 know to start
                toPlayer1.writeInt(1);

               // Continuously serve the players and determine and report
                // the game status to the players
                while (true) {
                    
                    
                    // send question to pl
                    Collections.shuffle(question);
                    Quiz q = question.get(question.size()-1);
                    String queString = q.toString();
                    question.remove(question.size()-1);
                    
                    toPlayer1.writeUTF(queString);
              
                    // get answer from pl
                    int ans = fromPlayer1.readInt();
                    
                    // check p1 answer and send the result
                    if(ans == q.getAnswer() ){
                        player1Score++;
                         Platform.runLater(()
                            -> taLog.appendText( "player 1 answer is correct \n"));
                    
                    }
                    else{
                    Platform.runLater(()
                            -> taLog.appendText( "player 1 answer is wrong \n"));
                    
                    }
                    
                     // Receive a move from Player 2
                     Collections.shuffle(question);
                     q = question.get(question.size()-1);
                     queString = q.toString();
                    question.remove(question.size()-1);
                    toPlayer2.writeUTF(queString);
              
                     ans = fromPlayer2.readInt();
                    if(ans == q.getAnswer() ){
                        player2Score++;
                         Platform.runLater(()
                            -> taLog.appendText( "player 2 answer is correct \n"));
                    
                    }
                    else{
                    Platform.runLater(()
                            -> taLog.appendText( "player 2 answer is wrong \n"));
                    
                    }
                    //remaining rounds 
                    rounds--;
                    
                    //if player scores diff is larger than remaing round, then there is a winner 
                    // the max wins
                    if(rounds<(Math.abs(player1Score-player2Score))){
                        
                        // player 1 is greater so player 1 is the winner
                        if(player1Score>player2Score){
                            
                             Platform.runLater(()
                            -> taLog.appendText( "player 1 wins! player1 score "+player1Score+" player2 score "+player2Score+"\n"));
                        
                        }
                         // player 2 is greater so player 1 is the winner
                        else{
                        
                             Platform.runLater(()
                            -> taLog.appendText( "player 2 wins! player1 score "+player1Score+" player2 score "+player2Score+"\n"));
                        }
  
                         
                    
                    
                    break;// from tye loop
                    }
                    // we may not need this brunch 
                    else if(rounds==0){
                        
                        
                        
                        // you are both equal        
                        
                       if(player1Score>player2Score){
                           
                            Platform.runLater(()
                            -> taLog.appendText( "player 1 wins! player1 score "+player1Score+" player2 score "+player2Score+"\n"));
                        
                        }
                       else if(player1Score<player2Score){
                           
                            Platform.runLater(()
                            -> taLog.appendText( "player 2 wins! player1 score "+player1Score+" player2 score "+player2Score+"\n"));
                        
                        }
                        else{
                        Platform.runLater(()
                            -> taLog.appendText( "player 1 and 2 are equal "+player1Score+" player2 score "+player2Score+"\n"));
                       }
                        
                    break;// from tye loop because rounds ends
                    }
                    
            
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

       
        
        }
}