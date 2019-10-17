import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import spread.*;


public class AccountReplica  implements BasicMessageListener {
	String account_name, server_adress;
	int number_of_replicas = 0;
	int group_size = 0;
	SpreadConnection connection;
	SpreadGroup group;
	double balance = 0.0;
	ArrayList<Transaction> outstanding_collection; 
	ArrayList<Transaction> executed_list;
	
	int order_counter = 0;
	int outstanding_counter = 0;
	
	
	protected static final int UNRELIABLE_MESS        = 0x00000001;
	protected static final int RELIABLE_MESS          = 0x00000002;
	protected static final int FIFO_MESS              = 0x00000004;
	protected static final int CAUSAL_MESS            = 0x00000008;
	protected static final int AGREED_MESS            = 0x00000010;
	protected static final int SAFE_MESS              = 0x00000020;
	protected static final int REGULAR_MESS           = 0x0000003f;
	protected static final int SELF_DISCARD           = 0x00000040;
	protected static final int REG_MEMB_MESS          = 0x00001000;
	protected static final int TRANSITION_MESS        = 0x00002000;
	protected static final int CAUSED_BY_JOIN         = 0x00000100;
	protected static final int CAUSED_BY_LEAVE        = 0x00000200;
	protected static final int CAUSED_BY_DISCONNECT   = 0x00000400;
	protected static final int CAUSED_BY_NETWORK      = 0x00000800;
	protected static final int MEMBERSHIP_MESS        = 0x00003f00;
	
	public AccountReplica() {
		
	}
	
	public AccountReplica(String an, String sa, int n) {
		this.account_name = an;
		this.server_adress = sa;
		this.number_of_replicas = n;
		outstanding_collection = new ArrayList<Transaction>(); 
		executed_list = new ArrayList<Transaction>();
	}
	
	public String toString() {
		return server_adress + " " + account_name + " " + number_of_replicas;
	}
	
	public static void main(String[] args) throws InterruptedIOException, SpreadException {
		if (args.length < 3) {
			System.out.println("hey");
			return;
		}
		
		AccountReplica ar = new AccountReplica(args[1], args[0], Integer.parseInt(args[2]));
		
		String id = Long.toString(System.nanoTime());
		id = id.substring( id.length() / 2, id.length() - 1);
		
		ar.connection = new SpreadConnection();
		try {
			ar.connection.connect(InetAddress.getByName(ar.server_adress),4803,id,false,true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ar.group = new SpreadGroup();
		
		try {
			ar.group.join(ar.connection,ar.account_name);
		} catch (SpreadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ar.connection.add(ar);
		System.out.println("did it work?");
		System.out.println(ar);
		//ar.sendMessage();
		//ar.waitForOthers();
		
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ar.exit();
		System.out.println("awoo");
		
	}
	
	public void getQuickBalance() {
		System.out.println(balance);
	}
	
	public void getSynchedBalance() {
		String[] order;
		try {
			while(connection.poll()) {
				order = new String(connection.receive().getData()).split("//s+");
			}
		}
		catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public void deposit(int amount) {
		
	}
	
	public void getInterest(int interest) {
		
	}
	
	public void getHistory() {
		
	}
	
	public void cleanHistory() {
		
	}
	
	
	public void sendMessage() {
		byte[] a = "doot".getBytes();
		SpreadMessage message = new SpreadMessage();
		message.setData(a);
		message.addGroup(this.account_name);
		message.setAgreed();		
		try {
			connection.multicast(message);
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SpreadMessage waitForOthers() throws InterruptedIOException, SpreadException {
		int others = 1;
		int i = 0;		
		
		while ("hei".length() == 3) {
			System.out.println("waiting for something to happen! i = " + i);
			SpreadMessage message= this.connection.receive();
						
			if (message.isRegular()) {
				System.out.println("recieved a regular message");
				System.out.println(message.getSender());
			}
			
			else if (message.isMembership()) {
				System.out.println("recieved memberhship message :) " + message.getMembershipInfo().getMembers().length);
			}
			
			else {
				System.out.println(message.getMembershipInfo().getGroup());
			}
			i++;
		}
			
			return null;
	}
	
	public void input_handler() throws FileNotFoundException {
		String filename = null;
		while(true) {
			if(filename == null) {
				//Commands are written directly to the command line.
				Scanner scanner = new Scanner(System.in);
				System.out.println("Waiting for command...");
				String[] command = scanner.nextLine().split("\\s+");
				
				switch(command[0]) {
				case "getQuickBalance":
					//TODO: complete this
					break;
				case "getSyncedBalance":
					//TODO: complete this
					break;
				case "deposit:":
						int amount = Integer.parseInt(command[1]);
						//TODO: complete this
						break;					
				case "addInterest":
					int interest = Integer.parseInt(command[1]);
					break;
				case "getHistory":
					//TODO: complete this.
					break;
				case "checkTxStatus":
					int transactionID = Integer.parseInt(command[1]);
					//TODO: complete this.
					break;
				case "cleanHistory":
					//TODO: complete this.
					break;
				case "memberInfo":
					//TODO: complete this.
					break;
				case "sleep":
					int sleepDuration = Integer.parseInt(command[1]);
					break;
				case "exit":
					//TODO: complete this.
					break;			
				}			
			}
			
			else {
				//Commands are retrieved from a file.
				File file = new File(filename);
				Scanner scanner = new Scanner(file);
				System.out.println("reading commands from file...");
				while(scanner.hasNext()) {
					String[] command = scanner.nextLine().split("\\s+");
					switch(command[0]) {
					case "getQuickBalance":
						//TODO: complete this
						break;
					case "getSyncedBalance":
						//TODO: complete this
						break;
					case "deposit:":
							int amount = Integer.parseInt(command[1]);
							//TODO: complete this
							break;					
					case "addInterest":
						int interest = Integer.parseInt(command[1]);
						break;
					case "getHistory":
						//TODO: complete this.
						break;
					case "checkTxStatus":
						int transactionID = Integer.parseInt(command[1]);
						//TODO: complete this.
						break;
					case "cleanHistory":
						//TODO: complete this.
						break;
					case "memberInfo":
						//TODO: complete this.
						break;
					case "sleep":
						int sleepDuration = Integer.parseInt(command[1]);
						break;
					case "exit":
						//TODO: complete this.
						break;			
					}				
				}		
				
			}
		}	
	}

	
	public void exit() {
		
		try {
			group.leave();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			connection.disconnect();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void messageReceived(SpreadMessage message) {
		if (message.isRegular()) {
			
		}
		
		else if (message.isMembership()) {
			MembershipInfo info = message.getMembershipInfo();
			
			if (info.isCausedByJoin()) {
				
			}
			
		}
			
		
	}
	
}
