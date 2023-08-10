/**
 * TCP Client
 *
 * This program implements a simple TCP client that allows users to interact with a TCP server.
 * It sends GET, PUT, and DELETE requests to the server and displays the responses.
 *
 * Author: Gaurang Jotwani
 * Course: NEU Summer 23 CS 6650
 * Date: 05/31/2023
 */

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Scanner;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class TCPClient {

  // Client socket
  private static Socket socket;

  // Input and output streams
  private static DataInputStream dataIn;
  private static DataOutputStream dataOut;

  // Command constants
  private static String get = new String("GET");
  private static String delete = new String("DELETE");
  private static String put = new String("PUT");
  private static String quit = new String("QUIT");


  /**
   * Main entry point of the client program.
   * Accepts the command-line arguments.
   * Throws IOException if an I/O error occurs.
   * CMD LINE ARGUMENTS:
   *  String SERVER_IP_ADRESS
   *  String SERVER_PORT
   */
  public static void main(String [] args) throws IOException {
    // Check if the correct number of arguments is provided
    if (args.length >= 3) {
      System.err.println("Provide Correct number of Arguments (IP and Port of server)");
      System.exit(-1);
    }
    // Extract the server IP and port from command line arguments
    String ip = args[0];
    int port = Integer.parseInt(args[1]);

    // Create a new socket and attempt to connect to the server
    socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(ip, port), 1000);
      System.out.println(getCurrentTimeStamp() + "Connection Successful!");

      // set a timeout value of 1 second
      socket.setSoTimeout(1000);
    } catch (ConnectException e) {
      System.err.println(getCurrentTimeStamp() + "Connection Refused from Server. Make sure port " +
              "number and Ip is correct" +
              " and server is running. Try Again Later.");
      System.exit(-1);
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    }  catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. Connection Not Successful");
    }

    // Initialize input and output streams for communication with the server
    dataIn = new DataInputStream(socket.getInputStream());
    dataOut = new DataOutputStream(socket.getOutputStream());

    // Register a signal handler to handle termination signals
    SignalHandler handler = new SignalHandler() {
      public void handle(Signal signal) {
        // Perform actions when the signal is received
        cleanUp();
        System.exit(0); // Terminate the application
      }
    };
    Signal.handle(new Signal("INT"), handler);

    // Start reading user input and sending requests to the server
    Scanner input = new Scanner(System.in);
    System.out.print("Please Input Command in either of the following forms:\n\tGET " +
            "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");

    while(true){
      System.out.print("Enter Command: ");
      String cmd = input.nextLine();
      String[] splited = cmd.split(" ");
      cmd = splited[0];

      if (splited.length >= 2 && splited[1].length() > 1024) {
        System.err.println(getCurrentTimeStamp() + "Key length is too big.");
        System.out.print("Please Input Command in either of the following forms:\n\tGET " +
                "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");
        continue;
      }

      if (cmd.equals(get) && splited.length == 2) {
        handleGetRequest(cmd, splited[1]);
      } else if (cmd.equals(put) && splited.length == 3) {
        if (splited[2].length() > 1024) {
          System.err.println(getCurrentTimeStamp() + "Val length is too big.");
          System.out.print("Please Input Command in either of the following forms:\n\tGET " +
                  "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");
          continue;
        }
        handlePutRequest(cmd, splited[1], splited[2]);
      } else if (cmd.equals(delete) && splited.length == 2) {
        handleDelRequest(cmd, splited[1]);
      } else if (cmd.equals(quit) && splited.length == 1) break; // Exit the loop and end the program
        else {
        System.err.println(getCurrentTimeStamp() + "Wrong format of command.");
        System.out.print("Please Input Command in either of the following forms:\n\tGET " +
                "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");
      }
    }
    // Perform cleanup actions before exiting
    cleanUp();
  }

  /**
   * Cleans up resources and closes the connection with the server.
   */
  private static void cleanUp() {
    try {
      dataOut.writeUTF("QUIT");
      dataIn.close();
      dataOut.close();
      socket.close();
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. Command Not Successful");
    }
  }

  /**
   * Function to handle GET requests
   *
   * @param cmd The command (GET)
   * @param key     The key to retrieve from the server
   */
  private static void handleGetRequest(String cmd, String key) {
    try {
      dataOut.writeUTF(cmd);  // Send message to server
      if (key.length() > 1024) return;
      dataOut.writeUTF(key);  // Send message to server
      String serverMessage = dataIn.readUTF();  // Receive message from server
      if (serverMessage.charAt(0) == '1') { // if first char is 1, it is success
        String[] parts = serverMessage.split(":");
        System.out.println(getCurrentTimeStamp() + "Value Read: " + parts[1]);
        System.out.println(getCurrentTimeStamp() + "Message from server: " + parts[2]);
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. GET Command Not Successful");
    }
  }

  /**
   * Function to handle PUT requests
   *
   * @param cmd The command (PUT)
   * @param key     The key to store in the server
   * @param val   The value associated with the key
   */
  private static void handlePutRequest(String cmd, String key, String val) {
    try {
        dataOut.writeUTF(cmd);  // Send message to server
        dataOut.writeUTF(key);  // Send message to server
        dataOut.writeUTF(val);  // Send message to server
        String serverMessage = dataIn.readUTF();  // Receive message from server
      if (serverMessage.charAt(0) == '1') { // if first char is 1, it is success
        System.out.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(2));
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. PUT Command Not Successful");
    }
  }

  /**
   * Function to handle DELETE requests
   *
   * @param cmd The command (DELETE)
   * @param key     The key to delete from the server
   */
  private static void handleDelRequest(String cmd, String key) {
    try {
      dataOut.writeUTF(cmd);  // Send message to server
      dataOut.writeUTF(key);  // Send message to server
      String serverMessage = dataIn.readUTF();  // Receive message from server
      if (serverMessage.charAt(0) == '1') { // if first char is 1, it is success
        System.out.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(2));
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. DELETE Command Not Successful");
    }
  }
  /**
   * Function to get the current timestamp
   * @return The current timestamp in string format
   */
  private static String getCurrentTimeStamp() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return "[" + timestamp.toString() + "]  ";
  }
}

