/**
 * UDP Server
 *
 * This program implements a simple UDP server that allows clients to perform CRUD operations on a
 * key-value store.
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

public class UDPServer {

  // Initializing arrays to send and receive data. Assume no message is longer than 1024 bytes
  private static byte[] receiveData = new byte[1024];
  private static byte[] sendData;

  // Command constants
  private static String get = new String("GET");
  private static String delete = new String("DELETE");
  private static String put = new String("PUT");
  private static String quit = new String("QUIT");

  // Server socket
  private static DatagramSocket serverSocket;

  // Input and output Packets
  private static DatagramPacket sendPacket;
  private static DatagramPacket receivePacket;

  // Initializing client port and client IP address
  private static InetAddress IPAddress;
  private static int port;

  // Key-value store to store data
  private static HashMap<String, String> keyValStore = new HashMap<String, String>();


  /**
   * Main entry point of the client program.
   * Accepts the command-line arguments.
   * Throws IOException if an I/O error occurs.
   * CMD LINE ARGUMENTS:
   *  String SERVER_PORT
   */
  public static void main(String[] args) throws IOException {
    // Check if the correct number of arguments is provided
    if (args.length >= 2) {
      System.err.println("Provide Correct number of Arguments (IP and Port of server)");
      System.exit(-1);
    }

    // Get the port from command line arguments and Create a new socket
    serverSocket = new DatagramSocket(Integer.parseInt(args[0]));


    System.out.println("Server Started. Listening for Clients on port " + args[0] + "...");

    while (true) {
      // Get cmd from the recieve packet
      String cmd = receiveDataPacket();

      // Get the client's IP address and port
      IPAddress = receivePacket.getAddress();
      port = receivePacket.getPort();

      if (put.equals(cmd)) {
        handlePutRequest();
      } else if (delete.equals(cmd)) {
        handleDelRequest();
      } else if (get.equals(cmd)) {
        handleGetRequest();
      } else if (quit.equals(cmd)) {
        System.out.println(getLogHeader(IPAddress.toString(), port) + "Connection Closed");
        System.out.println("Server Started. Listening for Clients on port " + args[0] + "...");
      } else {
        System.err.println(getLogHeader(IPAddress.toString(), port) + "Invalid Command: " + cmd);
        String serverMessage = "-1: Invalid Command";
        sendDataPacket(serverMessage);
      }
    }
  }

  /**
   * Function to handle PUT requests
   */
  private static void handlePutRequest() {
    try {
      // Receive the key and value from the client
      String key = receiveDataPacket();
      String val = receiveDataPacket();
      System.out.println(getLogHeader(IPAddress.toString(), port) + "Received PUT " +
              "Request to PUT key \"" + key + "\" with value \"" + val + "\"");
      // Store the key-value pair in the key-value store
      keyValStore.put(key, val);
      String serverMessage = key + " with value \"" + val + "\" saved successfully";
      System.out.println(getLogHeader(IPAddress.toString(), port) + serverMessage);
      serverMessage = "1:" + serverMessage;
      // Send success message to the client starting with "1"
      sendDataPacket(serverMessage);
    } catch (IOException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Unknown IO Error. PUT" +
              "Not Successful");
    }  catch (IllegalArgumentException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Packet Size too big");
    }
  }

  /**
   * Function to handle DELETE requests
   */
  private static void handleDelRequest(){
    try {
      // Receive the key from the client
      String key = receiveDataPacket();
      System.out.println(getLogHeader(IPAddress.toString(), port) + "Received DELETE" +
              " Request to Remove key \"" + key +"\"");
      String serverMessage;
      if (keyValStore.containsKey(key)) {
        // Remove the key from the key-value store if it exists
        keyValStore.remove(key);
        serverMessage = "Successfully removed key \"" + key +"\" ";
        System.out.println(getLogHeader(IPAddress.toString(), port) + serverMessage);
        // Append 1 to the message as it is successful
        serverMessage = "1:" + serverMessage;
      } else {
        // Key not found. Send error message to client
        serverMessage = "[Err] The key \"" + key +"\" does not exists in the store";
        System.err.println(getLogHeader(IPAddress.toString(), port) + serverMessage);
        serverMessage = "-1:" + serverMessage;
      }
      // Send the response to the client
      sendDataPacket(serverMessage);
    } catch (IOException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Unknown IO Error. " +
              "DELETE Not Successful");
    }  catch (IllegalArgumentException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Packet Size too big");
    }
  }

  /**
   * Function to handle GET requestst
   */
  private static void handleGetRequest(){
    try {
      // Receive the key from the client
      String key = receiveDataPacket();
      System.out.println(getLogHeader(IPAddress.toString(), port) + "Received GET" +
              " Request to read key \"" + key +"\"");
      String serverMessage;

      if (keyValStore.containsKey(key)) {
        // Get the value associated with the key from the key-value store
        String val = keyValStore.get(key);
        serverMessage = "Successfully read key \"" + key +"\" with val \"" + val + "\"";
        System.out.println(getLogHeader(IPAddress.toString(), port) + serverMessage);
        serverMessage = "1:" + val + ":" + serverMessage;
      } else {
        // Key not found. Send error message (add -1 to message)
        serverMessage = "[Err] The key \"" + key +"\" does not exists in the store";
        System.err.println(getLogHeader(IPAddress.toString(), port) + serverMessage);
        serverMessage = "-1:" + serverMessage;
      }
      sendDataPacket(serverMessage);
    } catch (IOException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Unknown IO Error. " +
              "GET Not Successful");
    } catch (IllegalArgumentException e) {
      System.err.println(getLogHeader(IPAddress.toString(), port) + "Packet Size too big");
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
   * @param data The data to send
   */
  private static void sendDataPacket(String data) throws IOException {
    sendData = data.getBytes();
    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    serverSocket.send(sendPacket);
  }

  /**
   * Function to get the current timestamp
   * @return The server message
   */
  private static String receiveDataPacket() throws IOException {
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    serverSocket.receive(receivePacket);  // Receive packet from server
    // Check Packet Size.
    int packetLength = receivePacket.getLength();
    // Throw error if exceeds 1024 bytes
    if (packetLength > 1024) {
      throw new IllegalArgumentException("Packet length exceeds 1024 bytes");
    }
    String serverMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
    return serverMessage;
  }
}