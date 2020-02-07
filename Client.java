package comp346pa1w2020;

import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/** Client class
 *
 * @author Kerly Titus
 */

public class Client extends Thread{ 
    
    private static int numberOfTransactions;   		/* Number of transactions to process */
    private static int maxNbTransactions;      		/* Maximum number of transactions */
    private static Transactions [] transaction; 	/* Transactions to be processed */
    private static Network objNetwork;          	/* Client object to handle network operations */
    private String clientOperation;    				/* sending or receiving */
    
	/** Constructor method of Client class
 	 * 
     * @return 
     * @param
     */
     Client(String operation)
     { 
       if (operation.equals("sending"))
       { 
           System.out.println("\n Initializing client sending application ...");
           numberOfTransactions = 0;
           maxNbTransactions = 100;
           transaction = new Transactions[maxNbTransactions];  
           objNetwork = new Network("client");
           clientOperation = operation; 
           System.out.println("\n Initializing the transactions ... ");
           readTransactions();
           System.out.println("\n Connecting client to network ...");
           String cip = objNetwork.getClientIP();
           if (!(objNetwork.connect(cip)))
           {   System.out.println("\n Terminating client application, network unavailable");
               System.exit(0);
           }
       	}
       else
    	   if (operation.equals("receiving"))
           { 
    		   System.out.println("\n Initializing client receiving application ...");
    		   clientOperation = operation; 
           }
     }
           
    /** 
     * Accessor method of Client class
     * 
     * @return numberOfTransactions
     * @param
     */
     public int getNumberOfTransactions()
     {
         return numberOfTransactions;
     }
         
    /** 
     * Mutator method of Client class
     * 
     * @return 
     * @param nbOfTrans
     */
     public void setNumberOfTransactions(int nbOfTrans)
     { 
         numberOfTransactions = nbOfTrans;
     }
         
    /** 
     * Accessor method of Client class
     * 
     * @return clientOperation
     * @param
     */
     public String getClientOperation()
     {
         return clientOperation;
     }
         
    /** 
     * Mutator method of Client class
	 * 
	 * @return 
	 * @param operation
	 */
	 public void setClientOperation(String operation)
	 { 
	     clientOperation = operation;
	 }
         
    /** 
     * Reading of the transactions from an input file
     * 
     * @return 
     * @param
     */
     public void readTransactions()
     {
        Scanner inputStream = null;     /* Transactions input file stream */
        int i = 0;                      /* Index of transactions array */
        
        try
        {
        	inputStream = new Scanner(new FileInputStream("transaction.txt"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File transaction.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine( ))
        {
            try
            {   transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next());            /* Read account number */
                transaction[i].setOperationType(inputStream.next());            /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
                transaction[i].setTransactionStatus("pending");                 /* Set current transaction status */
                i++;
            }
             catch(InputMismatchException e)
            {
                System.out.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }
            
        }
        setNumberOfTransactions(i);		/* Record the number of transactions processed */
        
        System.out.println("\n DEBUG : Client.readTransactions() - " + getNumberOfTransactions() + " transactions processed");
        
        inputStream.close( );

     }
     
    /** 
     * Sending the transactions to the server 
     * 
     * @return 
     * @param
     */
     public void sendTransactions()
     {
         int i = 0;     /* index of transaction array */
         
         while (i < getNumberOfTransactions())
         {  
            while( objNetwork.getInBufferStatus().equals("full") ) {
            	System.out.println("DEBUG: Client - In-Buffer is fulll, now waiting");
            	Thread.yield();
            	System.out.println("DEBUG  Client: Is the In-buffer still full?");
            };     /* Alternatively, busy-wait until the network input buffer is available */
                                             	
            transaction[i].setTransactionStatus("sent");   /* Set current transaction status */
           
            System.out.println("\n DEBUG : Client.sendTransactions() - sending transaction on account " + transaction[i].getAccountNumber());
            
            objNetwork.send(transaction[i]);                            /* Transmit current transaction */
            i++;
         }
         objNetwork.setClientConnectionStatus("Idle");//finish sending, stay waiting.
    }
         
 	/** 
  	 * Receiving the completed transactions from the server
     * 
     * @return 
     * @param transact
     */
     public void receiveTransactions(Transactions transact)
     {
         int i = 0;     /* Index of transaction array */
         
         while (i < getNumberOfTransactions())//running until enough transaction is received
         {     
        	 while( objNetwork.getOutBufferStatus().equals("empty")) {
        		 System.out.println("DEBUG - Client: Nothing in Out-Buffer");
        		 Thread.yield();
        		 System.out.println("DEBUG - Client: Is there something in Out-Buffer?");
        	 };  	/* Alternatively, busy-wait until the network output buffer is available */
                                                                        	
            objNetwork.receive(transact);                               	/* Receive updated transaction from the network buffer */
            
            System.out.println("\n DEBUG : Client.receiveTransactions() - receiving updated transaction on account " + transact.getAccountNumber());
            
            System.out.println(transact);                               	/* Display updated transaction */    
            i++;
         }
         //update connection status
         objNetwork.setClientConnectionStatus("disconnected");//done !
    }
     
    /** 
     * Create a String representation based on the Client Object
     * 
     * @return String representation
     * @param 
     */
     public String toString() 
     {
    	 return ("\n client IP " + objNetwork.getClientIP() + " Connection status" + objNetwork.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
     }
    
    /** Code for the run method
     * 
     * @return 
     * @param
     */
    public void run()
    {   
    	Transactions transact = new Transactions();
    	long sendClientStartTime, sendClientEndTime, receiveClientStartTime, receiveClientEndTime;
    	if(this.getClientOperation().equals("sending")) {
    		sendClientStartTime=System.currentTimeMillis();
    		this.sendTransactions();
    		sendClientEndTime=System.currentTimeMillis();
    		System.out.println("Running time for Client-sending: "+(sendClientEndTime-sendClientStartTime));
    	}
    	else if(this.getClientOperation().equals("receiving")) {
    		receiveClientStartTime=System.currentTimeMillis();
    		this.receiveTransactions(transact);
    		receiveClientEndTime=System.currentTimeMillis();
    		System.out.println("Running time for Client-receiving: "+(receiveClientEndTime-receiveClientStartTime));//running time
    		System.out.println("Status of CLIENT network: "+objNetwork.getClientConnectionStatus());
    		System.out.println("Status of SEVER network: "+objNetwork.getServerConnectionStatus());
    	}
    	else {
    		System.out.println("Unexpected error - terminated - CLIENT"); 
    		System.exit(0);//terminating for error
    	}
	/* Implement the code for the run method */
    }
}
