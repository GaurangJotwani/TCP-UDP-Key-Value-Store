# TCP/UDP Key-Value Store

The TCP/UDP Key-Value Store is a simple server-client application that allows clients to interact with a key-value store using TCP or UDP connection. This README provides instructions on how to run the project, how to send commands to the server, information about error handling and logging, and some assumptions made in the implementation.

## How to Run the Project

1. Ensure that you have Java Development Kit (JDK) installed on your machine.
2. Compile the Java source files using the following command:
   

    javac TCPClient.java TCPServer.java UDPClient.java UDPServer.java

3. Start the TCP server by running the following command:


    java TCPServer <port>

Replace `<port>` with the desired port number to listen on for TCP connections.
4. Start the TCP client by running the following command:


    java TCPClient <server_ip> <server_port>

   Replace `<server_ip>` with the IP address of the server and `<server_port>` with the port number that the TCP server is listening on.
5. To use UDP connection, start the UDP server by running the following command:
   

    java UDPServer <port>

6. Start the UDP client by running the following command:


    java UDPClient <server_ip> <server_port>

Replace `<server_ip>` with the IP address of the server and `<server_port>` with the port number that the UDP server is listening on.

## Sending Commands to the Server

Once the server and client are running, you can send commands to the server using the client. The following commands are supported:

- **GET**: Retrieve the value associated with a specific key. 

`GET <key>`

The server will respond with the value associated with the key, or an error message if the key does not exist. Server will start the message with "1" if request is successful otherwise it's an error.

- **PUT**: Store a key-value pair in the server's key-value store.

`PUT <key> <val>`

The server will respond with a success message if the key-value pair is stored successfully. Server will start the message with "1" if request is successful otherwise it's an error.

- **DELETE**: Remove a key-value pair from the server's key-value store.

`DELETE <key>`

The server will respond with a success message if the key is found and removed, or an error message if the key does not exist. Server will start the message with "1" if request is successful otherwise it's an error.

- **QUIT**: Close the connection to the server and terminate the client.

`QUIT <key>`

## Error Handling

The server and client include basic error handling. The following errors are handled:

- **Timeout**: The server and client have a timeout of 1 second for network operations. If a response is not received within this timeout period, a timeout error will occur.

- **Input/Output (IO) or Connection Errors**: If an IO or connection error occurs during the execution of a command, an error message will be displayed. These errors can happen due to network issues, client disconnect, or other unforeseen circumstances.

- **User Errors**: If there are user errors like invalid keys or invalid commands, the server will respond with an error message prefixed with "-1" to indicate a failure.

- **KeyLength Error**: If length of key or value is more than 1024 bytes, it will ask the user to input it again.


## Assumptions

The following assumptions were made in the implementation of this project:

- The maximum message size for a command or value is limited to 1024 bytes.
- The server supports a single client connection at a time. If multiple clients attempt to connect simultaneously, they will be queued and served in a sequential manner.
- The server does not persist the key-value store in a database or file. If the server is restarted, all stored data will be lost.
- The key and val stored in the cache will be string type.
- The server will be available (run) forever.


## Logging

Logging is implemented in both the server and client to provide information about the execution and events happening during the communication process. The logs can be helpful for debugging purposes or for monitoring the system's behavior.

### Server-Side Logging

The server logs various events and actions using standard output (console). The following logs are generated by the server:

- **Server Started**: When the server starts, it logs a message indicating that the server has started and is listening for clients on the specified port.

- **Client Connection**: When a client successfully connects to the server, the server logs a message indicating the successful connection, along with the client's IP address and port number.

- **Received PUT Request**: When the server receives a "PUT" request from the client, it logs a message indicating the key and value received.

- **Received DELETE Request**: When the server receives a "DELETE" request from the client, it logs a message indicating the key to be removed.

- **Received GET Request**: When the server receives a "GET" request from the client, it logs a message indicating the key to be retrieved.

- **Connection Closed**: When a client connection is closed, either by the client or due to an error, the server logs a message indicating the closure of the connection.

- **Invalid Command**: If the server receives an invalid command from the client, it logs an error message indicating the invalid command.

- **Unknown IO Error**: If an unknown IO error occurs during the execution of a command, the server logs an error message.

All server logs include a timestamp, client IP address, and client port number to provide a clear context for each event.

### Client-Side Logging

The client also logs various events and actions using standard output (console). The following logs are generated by the client:

- **Connection Established**: When the client successfully establishes a connection with the server, it logs a message indicating the successful connection.

- **Sent Command**: Whenever the client sends a command to the server, it logs a message indicating the command sent.

- **Received Response**: When the client receives a response from the server, it logs the response received.

- **Invalid Command**: If the client receives an invalid command from the user, it logs an error message indicating the invalid command.

- **Timeout**: If the server takes too long to respond, the client logs an error message.

- **Connection Closed**: When the client closes the connection to the server, it logs a message indicating the closure of the connection.

- **Unknown IO Error**: If an unknown IO error occurs during the execution of a command, the client logs an error message.

Similar to the server logs, client logs also include a timestamp to provide a clear context for each event.

Both the server and client logging can be helpful for tracking the flow of communication, identifying errors, and understanding the overall behavior of the system.
