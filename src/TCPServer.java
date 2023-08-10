/**
 * TCP Server
 *
 * This program implements a simple TCP server that allows clients to perform CRUD operations on a key-value store.
 * It listens for client connections on a specified port and handles GET, PUT, and DELETE requests.
 *
 * Author: Gaurang Jotwani
 * Course: NEU Summer 23 CS 6650
 * Date: 05/31/2023
 */

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.HashMap;

public class TCPServer {
  // Command constants
  private static String get = new String("GET");
  private static String delete = new String("DELETE");
  private static String put = new String("PUT");
  private static String quit = new String("QUIT");

  // Key-value store to store data
  private static HashMap<String, String> keyValStore = new HashMap<String, String>();

  // Input and output streams
  private static DataInputStream dataIn;
  private static DataOutputStream dataOut;

  // Server socket and client socket
  private static ServerSocket serverSocket;
  private static Socket clientSocket;

  /**
   * Main entry point of the client program.
   * Accepts the command-line arguments.
   * Throws IOException if an I/O error occurs.
   * CMD LINE ARGUMENTS:
   *  String SERVER_PORT
   */
  public static void main(String[] args) throws IOException {
    // Check if the correct number of arguments is provided
    if (args.length != 1) {
      System.err.println("Provide Correct number of Arguments (Port)");
      System.exit(-1);
    }
    // Get the port from command line arguments
    int port = Integer.parseInt(args[0]);

    // Create a new socket and attempt to accept clients
    try {
      serverSocket = new ServerSocket(port);
    } catch (BindException e) {
      System.err.println(getCurrentTimeStamp() + "Cannot bind to the port");
      System.exit(-1);
    }

    while (true) {
      System.out.println("Server Started. Listening for Clients on port " + args[0] + "...");
      try {
        // Accept a client connection
        clientSocket = serverSocket.accept();
      }  catch (IOException e) {
        System.err.println(getCurrentTimeStamp() + "Unknown IO Error. Connection Not Successful");
      }

      // Get the client's IP address and port
      String clientSocketIP = clientSocket.getInetAddress().toString();
      int clientSocketPort = clientSocket.getPort();
      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + "Client Connection" +
              " Successful!");

      // Create input and output streams for communication with the client
      dataIn = new DataInputStream(clientSocket.getInputStream());
      dataOut = new DataOutputStream(clientSocket.getOutputStream());

      while (true) {
        // Receive message from the client
        String clientMessage = dataIn.readUTF();  // Receive message from client
        if (get.equals(clientMessage)) {
          handleGetRequest(clientSocketIP, clientSocketPort);
        } else if (delete.equals(clientMessage)) {
          handleDelRequest(clientSocketIP, clientSocketPort);
        } else if (put.equals(clientMessage)) {
          handlePutRequest(clientSocketIP, clientSocketPort);
        } else if (quit.equals(clientMessage)) {
          break;
        } else {
          System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + "Invalid Command: " + clientMessage);
          dataOut.writeUTF("-1:Invalid Command");
        }
      }

      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + "Connection Closed");
      // Close the input and output streams and the client socket
      dataIn.close();
      dataOut.close();
      clientSocket.close();
      // Close streams and socket
    }
  }

  /**
   * Function to handle PUT requests
   *
   * @param clientSocketIP   The IP address of the client
   * @param clientSocketPort The port number of the client
   */
  private static void handlePutRequest(String clientSocketIP, int clientSocketPort) {
    try {
      // Receive the key and value from the client
      String key = dataIn.readUTF();  // Receive message from client
      String val = dataIn.readUTF();  // Receive message from client
      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + "Received PUT " +
              "Request to PUT key \"" + key + "\" with value \"" + val + "\"");

      // Store the key-value pair in the key-value store
      keyValStore.put(key, val);
      String serverMessage = key + " with value \"" + val + "\" saved successfully";
      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + serverMessage);

      // Send success message to the client starting with "1"
      dataOut.writeUTF("1:" + serverMessage);  // Send message to client
    } catch (IOException e) {
      System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + "Unknown IO Error. PUT" +
              "Not Successful");
    }
  }

  /**
   * Function to handle DELETE requests
   *
   * @param clientSocketIP   The IP address of the client
   * @param clientSocketPort The port number of the client
   */
  private static void handleDelRequest(String clientSocketIP, int clientSocketPort){
    try {

      // Receive the key from the client
      String key = dataIn.readUTF();
      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + "Received DELETE" +
              " Request to Remove key \"" + key +"\"");
      String serverMessage;
      if (keyValStore.containsKey(key)) {
        // Remove the key from the key-value store if it exists
        keyValStore.remove(key);
        serverMessage = "Successfully removed key \"" + key +"\" ";
        System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + serverMessage);
        serverMessage = "1:" + serverMessage;
      } else {
        // Key not found. Send error message to client
        serverMessage = "[Err] The key \"" + key +"\" does not exists in the store";
        System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + serverMessage);
        serverMessage = "-1:" + serverMessage;
      }
      // Send the response to the client
      dataOut.writeUTF(serverMessage);
    } catch (IOException e) {
      System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + "Unknown IO Error. " +
              "DELETE Not Successful");
    }
  }

  /**
   * Function to handle GET requests
   *
   * @param clientSocketIP   The IP address of the client
   * @param clientSocketPort The port number of the client
   */
  private static void handleGetRequest(String clientSocketIP, int clientSocketPort) {
    try {
      // Receive the key from the client
      String key = dataIn.readUTF();  // Receive message from client
      System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + "Received GET " +
              "Request to read key \"" + key +"\"");
      String serverMessage;
      if (keyValStore.containsKey(key)) {
        // Get the value associated with the key from the key-value store
        String val = keyValStore.get(key);
        serverMessage = "Successfully read key \"" + key +"\" with val \"" + val + "\"";
        System.out.println(getLogHeader(clientSocketIP, clientSocketPort) + serverMessage);
        serverMessage = "1:" + val + ":" + serverMessage;
      } else {
        // Key not found. Send error message
        serverMessage = "[Err] The key \"" + key +"\" does not exists in the store";
        System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + serverMessage);
        serverMessage = "-1:" + serverMessage;
      }
      // Send the response to the client
      dataOut.writeUTF(serverMessage);
    } catch (IOException e) {
      System.err.println(getLogHeader(clientSocketIP, clientSocketPort) + "Unknown IO Error. " +
              "GET Not Successful");
    }
  }

  /**
   * Function to generate the log header with timestamp, IP address, and port number
   *
   * @param ip   The IP address of the client
   * @param port The port number of the client
   * @return The log header string
   */
  private static String getLogHeader(String ip, int port) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return "[" + timestamp.toString() + " ,IP: " + ip + " ,Port: " + port +"]  ";
  }

  /**
   * Function to get the current timestamp
   * @return The current timestamp string
   */
  private static String getCurrentTimeStamp() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return "[" + timestamp.toString() + "]  ";
  }
}