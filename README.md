# Vector-Clocks
Client

Startup:

1. Prompt the user to select username A, B, or C.
2. Connect to the server over a socket and register the username.
a. When the client is connected, the user should be notified of the active connection.
b. If the provided username is already in use, the client should disconnect and prompt the user to select a different username.
3. Proceed to send and receive messages (e.g., vector clocks) until manually killed by the user.

Sending Messages:

1. Every two to ten second, the client will randomly choose one other username.
2. The client will update its vector clock according to Tanenbaum and van Steen, section 6.2.2.
3. The client will print its updated vector clock and its intended recipient to the GUI, then send the message to the server.
4. Return to Sending Messages: Step 1 until manually disconnected by the user.

Receiving Messages:

1. Upon receiving a message, the client will update its vector clock according to Tanenbaum and van Steen, section 6.2.2.
2. The client will print its updated vector clock and message sender’s ID to its GUI.
3. Return to Receiving Messages: Step 1 until manually disconnected by the user.

Server

The server should support three concurrently connected clients. The server should indicate which users are presently connected. The lab run will not commence until all three users are connected and it is assumed that all three users will remain connected for the duration of the lab run.

The server will execute the following sequence of steps:
1. Startup and listen for incoming connections.
2. Print that a client has connected, and:
a. If the client username is available (e.g., not currently being used by another client), fork a thread to handle that client. Or,
b. If the username is in use, reject the connection from that client.
3. The server will proceed to forward messages between clients. Each forwarded message, including the vector clock, and IDs of the sender and receiver, should be printed to the server’s GUI.
4. Begin at step 2 until the process is killed by the user.
