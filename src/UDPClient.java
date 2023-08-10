/**
 * UDP Client
 *
 * This program implements a simple UDP client that allows users to interact with a UDP server.
 * It sends GET, PUT, and DELETE requests to the server and displays the responses.
 *
 * Author: Gaurang Jotwani
 * Course: NEU Summer 23 CS 6650
 * Date: 05/31/2023
 */

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Scanner;

public class UDPClient {

  // Port and IP address of server
  private static InetAddress IPAddress;

  private static int port;

  // Command constants
  private static String get = new String("GET");
  private static String delete = new String("DELETE");
  private static String put = new String("PUT");
  private static String quit = new String("QUIT");

  // Input and output Packets
  private static DatagramPacket sendPacket;
  private static DatagramPacket receivePacket;

  // Initializing arrays to send and receive data. Assume no message is longer than 1024 bytes
  private static byte[] sendData;
  private static byte[] receiveData = new byte[1024];

  // Client socket
  private static DatagramSocket clientSocket;

  /**
   * Main entry point of the client program.
   * Accepts the command-line arguments.
   * Throws IOException if an I/O error occurs.
   * CMD LINE ARGUMENTS:
   *  String SERVER_IP_ADRESS
   *  String SERVER_PORT
   */
  public static void main(String[] args) throws IOException {
    // Check if the correct number of arguments is provided
    if (args.length >= 3) {
      System.err.println("Provide Correct number of Arguments (IP and Port of server)");
      System.exit(-1);
    }
    // Create a new socket and set timeout to 1 second
    clientSocket = new DatagramSocket();
    clientSocket.setSoTimeout(1000);

    // Extract the server IP and port from command line arguments
    IPAddress = InetAddress.getByName(args[0]);
    port = Integer.parseInt(args[1]);

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

    while(true) {
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


      if (cmd.equals(put) && splited.length == 3) {
        if (splited[2].length() > 1024) {
          System.err.println(getCurrentTimeStamp() + "Val length is too big.");
          System.out.print("Please Input Command in either of the following forms:\n\tGET " +
                  "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");
          continue;
        }
        handlePutRequest(cmd, splited[1], splited[2]);
      } else if (cmd.equals(get) && splited.length == 2) {
        handleGetRequest(cmd, splited[1]);
      } else if (cmd.equals(delete) && splited.length == 2) {
        handleDelRequest(cmd, splited[1]);
      } else if (cmd.equals(quit) && splited.length == 1) {
        cleanUp();
        break;
      }
      else {
        System.err.println(getCurrentTimeStamp() + "Wrong format of command.");
        System.out.print("Please Input Command in either of the following forms:\n\tGET " +
                "<key>\n\tPUT <key> <val>\n\tDELETE <key>\n\tQUIT\n");
      }
    }
  }


  /**
   * Cleans up resources and closes the connection with the server.
   */
  private static void cleanUp() {
    try {
      String cmd = "QUIT";
      sendDataPacket(cmd);
      clientSocket.close();
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. Command Not Successful");
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
      // Send cmd to server
      sendDataPacket(cmd);
      // Send key to server
      sendDataPacket(key);
      // Send val to server
      sendDataPacket(val);

      // Receive message from server
      String serverMessage = receiveDataPacket();

      if (serverMessage.charAt(0) == '1') {
        // if first char is 1, it is success
        System.out.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(2));
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. PUT Command Not Successful");
    } catch (IllegalArgumentException e) {
      System.err.println(getCurrentTimeStamp() + "Packet Size too big");
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
      // Send cmd to server
      sendDataPacket(cmd);
      // Send key to server
      sendDataPacket(key);

      // Receive message from server
      String serverMessage = receiveDataPacket();

      // if first char is 1, it is success
      if (serverMessage.charAt(0) == '1') {
        String[] parts = serverMessage.split(":");
        System.out.println(getCurrentTimeStamp() + "Value Read: " + parts[1]);
        System.out.println(getCurrentTimeStamp() + "Message from server: " + parts[2]);
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. PUT Command Not Successful");
    } catch (IllegalArgumentException e) {
      System.err.println(getCurrentTimeStamp() + "Packet Size too big");
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
      // Send cmd to server
      sendDataPacket(cmd);
      // Send key to server
      sendDataPacket(key);
      // Receive message from server
      String serverMessage = receiveDataPacket();

      if (serverMessage.charAt(0) == '1') {
        // if first char is 1, it is success
        System.out.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(2));
      } else {
        System.err.println(getCurrentTimeStamp() + "Message from server: " + serverMessage.substring(3));
      }
    } catch (SocketTimeoutException e) {
      System.err.println(getCurrentTimeStamp() + "Server taking too long to respond. Try again!");
    } catch (IOException e) {
      System.err.println(getCurrentTimeStamp() + "Unknown IO Error. PUT Command Not Successful");
    } catch (IllegalArgumentException e) {
      System.err.println(getCurrentTimeStamp() + "Packet Size too big");
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

  /**
   * Function to get the current timestamp
   * @param data The data to send
   */
  private static void sendDataPacket(String data) throws IOException {
    sendData = data.getBytes();
    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    clientSocket.send(sendPacket);
  }

  /**
   * Function to get the current timestamp
   * @return The server message
   */
  private static String receiveDataPacket() throws IOException {
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);  // Receive packet from server
    int packetLength = receivePacket.getLength();
    // Checking if size is too more than 1024 bytes. Throw error if it is
    if (packetLength > 1024) {
      throw new IllegalArgumentException("Packet length exceeds 1024 bytes");
    }
    String serverMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
    return serverMessage;
  }
}