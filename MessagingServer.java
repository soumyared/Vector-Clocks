/*
NAME: REDDAMMAGARI SREE SOUMYA
ID: 1001646494
NET-ID: sxr6494 */
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;


import java.awt.event.*;


public class MessagingServer extends JFrame implements ActionListener, Runnable {
    // A Hashmap to keep list of users
    private Map<String, User> users = new HashMap<>();
    private JPanel usersPanel = new JPanel();
    private JList messagesList = new JList(new DefaultListModel());


    

    // Set the UI of the server
    public MessagingServer() {
        setTitle("Messaging Server");
        setSize(300, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set the UI for disconnection
        /*used JPanel https://www.geeksforgeeks.org/java-swing-jpanel-examples/ link for setting up panel properties*/
        JPanel topPanel = new JPanel(new FlowLayout());
        add(BorderLayout.NORTH, topPanel);

        topPanel.add(new JLabel("Checked users are online."));

        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(this);
        topPanel.add(disconnectButton);

        // Set UI for users
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBackground(Color.WHITE);
        usersPanel.setPreferredSize(new Dimension(150, 0));

        usersPanel.setBorder(BorderFactory.createEtchedBorder());
        add(BorderLayout.WEST, usersPanel);
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(BorderLayout.CENTER, new JScrollPane(messagesList));

        add(BorderLayout.CENTER, messagesPanel);

        // Server now starts
        new Thread(this).start();
    }

    // Wait for client connections
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8080);

            while (true) {
                // Wait for a new client to connect
                Socket s = ss.accept();
                //input and output streams
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                //set vector clock hashmap for a user initially to("A=>0,B=>0,C=>0")
                Map<String,Integer> vec=new HashMap<>();
                vec.put("A",0);
                vec.put("B",0);
                vec.put("C",0);

                Message message = (Message) ois.readObject();

                // At this point we should expect the client to welcome with a CONNECT message, otherwise
                // just reject the client
                if (!message.getMessage().equals("CONNECT")&&!message.getMessage().equals("MESSAGE DISPLAY")) {
                    oos.writeObject(new Message("INVALID", "Unknown welcome message."));
                    s.close();
                    continue;
                }
               
             if(message.getMessage().equals("CONNECT")) {
                /* Check the validity of the username
                setting the state of checkbox
                http://www.java2s.com/Code/JavaAPI/javax.swing/JCheckBoxisSelected.htm*/
                String username = (String) message.getData();
                User user;

                if (users.containsKey(username)) {
                    user = users.get(username);
                    //if checkbox is marked then username is online
                    if (user.checkBox.isSelected()) {
                        oos.writeObject(new Message("INVALID", "Username is online."));
                        continue;
                    }

                    user.inputStream = ois;
                    user.outputStream = oos;
                }                
                //if username is not in the list create a new user by adding readonly checkbox for the user,input and output streams
                else {
                    user = new User(createReadOnlyCheckBox(username), ois, oos,vec);
                    usersPanel.add(user.checkBox);
                }

                /* Broadcast to online users about the new user*/
                broadcastMessage(users.keySet(), new Message("CONNECTED USER", username));

                // Mark the user online
                user.checkBox.setSelected(true);
                usersPanel.updateUI();

                users.put(username, user);
                String x="";
                for ( String key : users.keySet() ) {
                     System.out.println( key );
                     x=x+","+key;
                     }

                // Create another thread that handles specificlly this user
                oos.writeObject(new Message("VALID", "Welcome " + username + "!\n"+" List of usernames\n"+x));
                //oos.writeObject(new Message("VALID", "Welcome " + "ola" + "!"));
                new Thread(new UserThread(username)).start();
            }
             //Display vector clock sent by client to its recipients
            else if (message.getMessage().equals("MESSAGE DISPLAY")) {
                    // New message arrived
                    ((DefaultListModel) messagesList.getModel()).addElement(message.getData().toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
  //used to handle sending messages to multiple clients input is list of usernames and 
    //message that needs to be sent to client for handling 
    private void broadcastMessage(Set<String> usernames, Message message) {
        for (String username : usernames) {
            if (!users.containsKey(username)) {
                // Don't send to a non-existing user
                continue;
            }

            User user = users.get(username);

            if (!user.checkBox.isSelected()) {
                // Don't send to an offline user
                continue;
            }

            try {
                user.outputStream.writeObject(message);
                
            } catch (Exception e) {
            }
        }
    }
    
    
    // Exchange and update vector clocks
    private void broadcastMessage1(String recipient, String username) {
        //sender
    	User user = users.get(username);
    	//recipient
        User user1 = users.get(recipient);
        
        
            try {
            	//increment the vector clock of sender by 1
            	user.vector.put(username,user.vector.get(username)+1);
            	//message TO be displayed on sender's UI
            	String message="SENDING TO "+recipient+":"+"("+user.vector.get("A")+","+user.vector.get("B")+","+user.vector.get("C")+")";
            	//intimate senders UI to display vector clock of sender and its recipient id
                user.outputStream.writeObject(new Message("MESSAGE DELIVERY", message));
                //message to be displayed on server's UI i.e sender, recipient and vector clock of sender
            	message="sender="+username+"  ,"+"recepients="+recipient+"  :"+"("+user.vector.get("A")+","+user.vector.get("B")+","+user.vector.get("C")+")";
            	//Write to server
            	Socket socket1 = new Socket("LOCALHOST", 8080);
            	ObjectInputStream ois = new ObjectInputStream(socket1.getInputStream());
            	ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());
            	oos.writeObject(new Message("MESSAGE DISPLAY", message));

                //update vector clocks of recipients accordingly if sender vector clockis higher
                if(user.vector.get("A")>user1.vector.get("A"))
                {
                	user1.vector.put("A",user.vector.get("A"));
                	
                }
                if(user.vector.get("B")>user1.vector.get("B"))
                {
                	user1.vector.put("B",user.vector.get("B"));
                	
                }
                if(user.vector.get("C")>user1.vector.get("C"))
                {
                	user1.vector.put("C",user.vector.get("C"));
                	
                }
                //increment recipients vector clock by 1
            	user1.vector.put(recipient,user1.vector.get(recipient)+1);
            	//message to be displayed on recipient's UI
            	message="RECEIVING FROM "+username+":"+"("+user1.vector.get("A")+","+user1.vector.get("B")+","+user1.vector.get("C")+")";
                //message to be displayed on recipient's UI i.e sender id and updated vector clock of recipient        
                user1.outputStream.writeObject(new Message("MESSAGE DELIVERY", message));
                    
                
            } catch (Exception e) {
            }
        
    }

    // Handle button actions, there's only one button at the moment, that is to disconnect the server
    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to stop the server?") == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Start the server
    public static void main(String[] args) {
        new MessagingServer().setVisible(true);
    }

    // Tracks user info
    private class User {

        public JCheckBox checkBox;
        public ObjectInputStream inputStream;
        public ObjectOutputStream outputStream; 
        // vector clock for each user
        public Map<String,Integer> vector;


        // Create a new user
        public User(JCheckBox checkBox, ObjectInputStream inputStream, ObjectOutputStream outputStream,Map<String,Integer> vector) {
            this.checkBox = checkBox;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.vector=vector;
            
        }
    }

    // A thread to accept and respond to a user's request
    private class UserThread implements Runnable {

        public String username;

        // Create a thread
        public UserThread(String username) {
            this.username = username;
        }

        // Run and wait for requests from user
        @Override
        public void run() {
            User user = users.get(username);

            try {
                ObjectInputStream ois = user.inputStream;
                ObjectOutputStream oos = user.outputStream;

                while (true) {
                    // Wait for a message to broadcast to other users
                    Message message = (Message) ois.readObject();

                    // Deliver Vector clocks appropriately
                    if (message.getMessage().equals("DELIVER MESSAGE")) {
                        // Deliver the vector clocks to respective recipients
                        String receipient = message.getData().toString();
                        broadcastMessage1(receipient,message.getExtraData().toString());
                    }
                    //Break out of loot if disconnected 
                    else if (message.getMessage().equals("DISCONNECT")) {
                        break;
                    }
                    //Send a set of active users to client 
                    else if (message.getMessage().equals("ACTIVE USERS")) {
                        // Respond all the active users
                        Set<String> usernames = new HashSet<>();

                        for (String aUsername : users.keySet()) {
                            if (users.get(aUsername).checkBox.isSelected()) {
                                usernames.add(aUsername);
                            }
                        }

                        oos.writeObject(new Message("ACTIVE USERS", usernames));
                    }
                    //checks if vector clock algoritm can be started
                    else if (message.getMessage().equals("VECTOR CLOCK")) {
                        // Respond all the active users
                        Set<String> usernames = new HashSet<>();

                        for (String aUsername : users.keySet()) {
                            if (users.get(aUsername).checkBox.isSelected()) {
                                usernames.add(aUsername);
                            }
                        }
                        //if all three clients are connected start vector clock algorithm
                        if(usernames.size()==3) {
                            broadcastMessage(usernames, new Message("VECTOR CLOCK"));

                        }
                    }
                    //stop the algorithm if stop butto is pressed or client disconnects
                    else if (message.getMessage().equals("STOP")) {
                        // Respond all the active users
                        Set<String> usernames = new HashSet<>();

                        for (String aUsername : users.keySet()) {
                            if (users.get(aUsername).checkBox.isSelected()) {
                            	users.get(aUsername).vector.put("C",0);
                            	users.get(aUsername).vector.put("A",0);
                            	users.get(aUsername).vector.put("B",0);
                                usernames.add(aUsername);
                            }
                        }
                           //intimate all the clients to stop algorithm
                            broadcastMessage(usernames, new Message("STOP"));

                        
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Disconnect this user, tell everyone this user got disconnected
            user.checkBox.setSelected(false);
            usersPanel.updateUI();
            broadcastMessage(users.keySet(), new Message("DISCONNECTED USER", username));
        }
    }

    // Create a read-only checkbox
    private JCheckBox createReadOnlyCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);

        // Remove mouse events
        for (MouseListener mouseListener : (MouseListener[]) checkBox.getListeners(MouseListener.class)) {
            checkBox.removeMouseListener(mouseListener);
        }

        /* Remove key events
           https://stackoverflow.com/questions/4472530/disabling-space-bar-triggering-click-for-jbutton
        */
        InputMap inputMap = checkBox.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "none");
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "none");

        return checkBox;
    }
}
