/*
NAME: REDDAMMAGARI SREE SOUMYA
ID: 1001646494
NET-ID: sxr6494 */
import java.awt.*;
import java.awt.List;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;




public class MessagingClient extends JFrame implements ActionListener {

    private JTextField serverAddressTextField = new JTextField(20);
    private JTextField usernameTextField = new JTextField(10);
    private JButton connectDisconnectButton = new JButton("Connect");
    private JButton stopMessageButton = new JButton("Stop");
    private JPanel usersPanel = new JPanel();
    private JList messagesList = new JList(new DefaultListModel());
	private Timer timer = new Timer();
    private Set<String> usernames = new HashSet<>();
    private Map<String, JCheckBox> usersCheckBox = new HashMap<>();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    // Setup the layout of the client
    public MessagingClient() {
        super("Messaging Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // UI for connecting to server
        /*used JPanel https://www.geeksforgeeks.org/java-swing-jpanel-examples/ link for seting up panel properties*/
        JPanel connectionPanel = new JPanel(new FlowLayout());
        add(BorderLayout.NORTH, connectionPanel);

        connectionPanel.add(new JLabel("Server Address:"));
        connectionPanel.add(serverAddressTextField);
        connectionPanel.add(new JLabel("Choose only Username: A or B or C:"));
        connectionPanel.add(usernameTextField);
        connectionPanel.add(connectDisconnectButton);

        connectDisconnectButton.addActionListener(this);

        // UI for displaying online users

        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBackground(Color.WHITE);
        usersPanel.setPreferredSize(new Dimension(150, 0));
        usersPanel.setBorder(BorderFactory.createEtchedBorder());
        add(BorderLayout.WEST, usersPanel);

        // UI for displaying messages
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(BorderLayout.CENTER, new JScrollPane(messagesList));




        JPanel sendMessagePanel = new JPanel(new FlowLayout());
        sendMessagePanel.add(stopMessageButton);
        stopMessageButton.addActionListener(this);
        messagesPanel.add(BorderLayout.SOUTH, sendMessagePanel);

        add(BorderLayout.CENTER, messagesPanel);
        stopMessageButton.setEnabled(false);

    }

    // Initiate a connection to server
    private void connectToServer() {
    	//Get the text in the server address text field and username text field
        String serverAddress = serverAddressTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        //if address is empty redo
        if (serverAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A server address is required.");
            return;
        }
        //if username is empty redo
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A username is required.");
            return;
        }
        //Allow user to choose only A,B,C as usernames
       if(username.equals("A")||username.equals("B")||username.equals("C")) {
       }
        else {
        	 JOptionPane.showMessageDialog(this, "Please choose only username A or B or C");
        	return;
        }


        try {
            Socket socket = new Socket(serverAddress, 8080);

            // Send a welcome message to the server
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            //Send a connect message to the user along with username to check if the username is allowed to use
            oos.writeObject(new Message("CONNECT", username));

            Message message = (Message) ois.readObject();
            JOptionPane.showMessageDialog(this, message.getData());
            // Indicates username is already online by server
            if (message.getMessage().equals("INVALID")) {
                return;
            }

            // Start a new thread if username is valid, request for active users
            oos.writeObject(new Message("ACTIVE USERS"));
            new Thread(new MessageThread()).start();
            //After every client connects check if vector clock can be started
            oos.writeObject(new Message("VECTOR CLOCK"));


            stopMessageButton.setEnabled(true);
            usernameTextField.setEditable(false);
            serverAddressTextField.setEditable(false);
            connectDisconnectButton.setText("Disconnect");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection invalid.");
        }
    }

    // Disconnect from server
    private void disconnectFromServer() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect?") != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            oos.writeObject(new Message("DISCONNECT"));
        } catch (Exception e) {
        }

        System.exit(0);
    }

   

    // Handle button events
    @Override
    public void actionPerformed(ActionEvent e) {
 
        if (e.getActionCommand().equals("Connect")) {
            connectToServer();
        } else if (e.getActionCommand().equals("Disconnect")) {
            disconnectFromServer();
        } 
        else if (e.getActionCommand().equals("Stop")) {
        	
        	try {
                oos.writeObject(new Message("STOP"));
            } catch (Exception e1) {
            }

       	    
       }
    }

    // Start the program
    public static void main(String[] args) {
        new MessagingClient().setVisible(true);
        
    }
    //TimerTask classes which you can use to schedule your task to run every n seconds.
    //https://stackoverflow.com/questions/12908412/print-hello-world-every-x-seconds
    class SayHello extends TimerTask {
    	//override the public void run() method, which will be executed everytime you pass an instance of that class to timer.schedule() method.
        public void run() {
           startMessage();
        }
    }
    
    //Select one random recipient 
    private void startMessage() {
 
        // Get only receipients selected


        for (String username : usersCheckBox.keySet()) {
            if (usersCheckBox.get(username).isSelected()) {
                usernames.add(username);
               

            }
        }

        try {
			// Select chosen random reciepent as well as sender
            oos.writeObject(new Message("DELIVER MESSAGE",getRandomElement(usernames), usernameTextField.getText()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection to server was lost.");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
   //Generate random number between a range
    //https://mkyong.com/java/java-generate-random-integers-in-a-range/
    private static int getRandomNumberInRange(int min, int max) {

    	if (min >= max) {
    		throw new IllegalArgumentException("max must be greater than min");
    	}

    	Random r = new Random();
    	return r.nextInt((max - min) + 1) + min;
    }
  //Selecting a random element from set  
 //https://www.javacodeexamples.com/get-random-elements-from-java-hashset-example/2765   
private static <E> E getRandomElement(Set<? extends E> set){
        
        /*
         * Generate a random number using nextInt
         * method of the Random class.
         */
        Random random = new Random();
        
        //this will generate a random number between 0 and HashSet.size - 1
        int randomNumber = random.nextInt(set.size());
 
        //get an iterator
        Iterator<? extends E> iterator = set.iterator();
        
        int currentIndex = 0;
        E randomElement = null;
        
        //iterate the HashSet
        while(iterator.hasNext()){            
                
            randomElement = iterator.next();
            
            //if current index is equal to random number
            if(currentIndex == randomNumber)
                return randomElement;            
            
            //increase the current index
            currentIndex++;
        }
        
        return randomElement;
    }    


    // Waits for server messages
    private class MessageThread implements Runnable {

        // Retrieve the server messages
        @Override
        public void run() {
            try {
                while (true) {
                	
                	
                  /*setting the state of checkbox
                  http://www.java2s.com/Code/JavaAPI/javax.swing/JCheckBoxisSelected.htm*/
                    Message message = (Message) ois.readObject();
                    //update userspanel with active users
                    if (message.getMessage().equals("ACTIVE USERS")) {
                        // We got active users we have to add
                        for (String username : (Set<String>) message.getData()) {
                            //if username is same as the usernametextfield then don't add it to userspanel
                        	if (username.equals(usernameTextField.getText())) {
                                continue;
                            }
                            
                            //JCheckBox checkBox = new JCheckBox(username);
                            JCheckBox checkBox=createReadOnlyCheckBox(username);
                            //set checkbox to true
                            checkBox.setSelected(true);
                            //add it to userspanel
                            usersPanel.add(checkBox);
                            //update the map
                            usersCheckBox.put(username, checkBox);
                        }

                        usersPanel.updateUI();
                    }
                    //remove user from the userspanel if a user disconnects
                    else if (message.getMessage().equals("DISCONNECTED USER")) {
                        // There's a disconnected user, we remove it
                        String username = (String) message.getData();
                        //update map by removing user and remove it from userspanel
                        if (usersCheckBox.containsKey(username)) {
                            usersPanel.remove(usersCheckBox.get(username));
                            usersPanel.updateUI();
                          //this//
                            oos.writeObject(new Message("STOP"));
                            usersCheckBox.remove(username);
                        }
                    } else if (message.getMessage().equals("CONNECTED USER")) {
                        // There's a new user, we add it
                        String username = (String) message.getData();
                      //   ArrayList<String> ulist = new ArrayList<String>();
                      //   int count=0;

                        if (!usersCheckBox.containsKey(username)) {
                            JCheckBox checkBox = new JCheckBox(username);
                            checkBox.setSelected(true);

                            usersPanel.add(checkBox);
                            usersCheckBox.put(username, checkBox);
                            usersPanel.updateUI();

                            usersCheckBox.put(username, checkBox);
                        }
                    }
                    //Display messages onto the messages panel
                    else if (message.getMessage().equals("MESSAGE DELIVERY")) {
                        // New message arrived
                        ((DefaultListModel) messagesList.getModel()).addElement(message.getData().toString());
                    }
                    //if all the three clients are connected start the vector clocks
                    else if (message.getMessage().equals("VECTOR CLOCK")) {
                    //Set random timer
                    	int i=0;
                    	int j=0;
                    	while(true) {
                    	i=getRandomNumberInRange(2000,10000);
                        j=getRandomNumberInRange(2000,10000);
                        if(i<j) {
                        	break;
                        }
                    	}
                    	System.out.println(i);
                    	System.out.println(j);
                     //schedule the timer instance for clients to exchange vector clocks
                   	 timer.schedule(new SayHello(), i, j);

                    }
                    //stop the vector clock exchange by stopping the timer thread
                    else if (message.getMessage().equals("STOP")) {
                    	timer.cancel();
                    	timer.purge();
                    }

                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connection to server was lost.");
                System.exit(0);
            }
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
