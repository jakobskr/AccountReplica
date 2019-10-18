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
	volatile int number_of_replicas = 0;
	int group_size = 0;
	long broadcast_delay = 10000;
	long next_broadcast;
	boolean started;
	boolean initialized = false;
	SpreadConnection connection;
	SpreadGroup group;
	ArrayList<SpreadGroup> group_members = new ArrayList<SpreadGroup>();
	double balance = 0.0;
	static String account_id;
	ArrayList<Transaction> outstanding_collection; 
	ArrayList<Transaction> executed_list;
	String filename;
	
	int order_counter = 0;
	int outstanding_counter = 0;

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
		if (args.length == 4) ar.filename = args[3];


		account_id = Long.toString(System.nanoTime());
		account_id = account_id.substring( account_id.length() / 2, account_id.length() - 1);
		
		ar.connection = new SpreadConnection();
		try {
			ar.connection.connect(InetAddress.getByName(ar.server_adress),4803,account_id,false,true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		ar.group = new SpreadGroup();
		
		try {
			ar.group.join(ar.connection,ar.account_name);
		} catch (SpreadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		ar.connection.add(ar);

		ar.waitForOthers();	
		
		try {
			ar.input_handler();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		ar.exit();
		
	}
	
	/*
	 * prints out the quickBalance no sync guaranteed
	 */
	public void getQuickBalance() {
		System.out.println(balance);
	}
	
	/*
	 * prints out the balance after our locally invoked commands have been applied. I.E when outstanding_collection size is 0.
	 * broadcasts the outstanding_collection each 10th second 
	 */
	public void getSynchedBalance() {
		while(outstanding_collection.size()>0) {
			if(next_broadcast <= System.currentTimeMillis()) {
				System.out.println("its next_broadcast time ");
				broadcast_outstanding();
				next_broadcast = System.currentTimeMillis() + broadcast_delay;
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(balance);
	}
	
	public void deposit(Double amount) {
		Transaction t = new Transaction("deposit " + amount, account_id + "-" + order_counter);
		outstanding_collection.add(t);
		order_counter++;
		//sendMessage(t.command + " " + t.unique_id);
	}
	
	public void addInterest(Double interest) {
		Transaction t = new Transaction("interest " + interest, account_id + "-" + order_counter);
		outstanding_collection.add(t);
		order_counter++;
		//sendMessage(t.command + " " + t.unique_id);
	}
	
	public void getHistory() {
		int counter = order_counter - executed_list.size();
		System.out.println("executed:");
		for(Transaction t : executed_list) {
			System.out.println(counter + " " + t.command);
		}
		System.out.println("\noutstanding:");
		for(Transaction t: outstanding_collection) {
			System.out.println(t.command);
		}
	}
	
	public void cleanHistory() {
		outstanding_collection = new ArrayList<Transaction>(); 
		executed_list = new ArrayList<Transaction>();
	}
	
	public void getMembers() {
		System.out.println("The members of " + account_name + ": " + group_size); 
		
		for(SpreadGroup sg: group_members) {
			System.out.println("\t" + sg);
		}
	}
	
	
	public void sendMessage(String content) {
		byte[] a = content.getBytes();
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
	
	public void waitForOthers() {
		boolean weAreNumberOne = false;
		System.out.println("Waiting until " + number_of_replicas + " replicas has joined...");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (group_size == 1) {
			weAreNumberOne = true; //hey!
			started = false;
			initialized = true;
		}

		while (true) {
			if(group_size >= number_of_replicas) {
				break;
			}
		}
				
		return;
	}
	
	public void broadcast_outstanding() {
		if(outstanding_collection.size() == 0) {
			return;
		}
		
		String data = "";
		int i = 0;
		for (Transaction trans: outstanding_collection) {
			data += trans;
			
			if(i == outstanding_collection.size() - 1) {
				continue;
			}
			
			data += "|";
			
			i++;
			
		}
		sendMessage(data);
		
	}
	
	
	/*
	 * The main loop of the program, if there is no supplied filename it reads command from the user through the terminal
	 */
	public void input_handler() throws FileNotFoundException {
		next_broadcast = System.currentTimeMillis() + broadcast_delay;
		String filename = this.filename;
		while(true) {
			if(group_size < number_of_replicas) {
				waitForOthers();
			}
			if(filename == null) {
				if(next_broadcast <= System.currentTimeMillis()) {
					System.out.println("its next_broadcast time ");
					broadcast_outstanding();
					next_broadcast = System.currentTimeMillis() + broadcast_delay;
				}
				//Commands are written directly to the command line.
				Scanner scanner = new Scanner(System.in);
				System.out.println("Waiting for command...");
				String[] command = scanner.nextLine().split("\\s+");
				
				switch(command[0]) {
				case "getQuickBalance":
					getQuickBalance();
					break;
				case "getSynchedBalance":
					this.getSynchedBalance();
					break;
				case "deposit":
						Double amount = Double.parseDouble(command[1]);
						this.deposit(amount);
						break;					
				case "addInterest":
					double interest = Double.parseDouble(command[1]);
					this.addInterest(interest);
					break;
				case "getHistory":
					this.getHistory();
					break;
				case "checkTxStatus":
					
					for(Transaction trans: executed_list) {
						if(trans.unique_id.equalsIgnoreCase(command[1])) {
							System.out.println("Transaction has been applied");
						}
					}
					
					//TODO: complete this.
					break;
				case "cleanHistory":
					sendMessage("cleanHistory");
					break;
				case "memberInfo":
					getMembers();
					break;
				case "sleep":
					int sleepDuration = Integer.parseInt(command[1]);
					try {
						Thread.sleep(sleepDuration * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case "exit":
					exit();
					break;	
				default:
					System.out.println("Invalid command");
					break;
				}
				
			}
			
			else {
				//Commands are retrieved from a file.
				File file = new File(filename);
				Scanner scanner = new Scanner(file);
				System.out.println("reading commands from file...");
				while(scanner.hasNext()) {
					if(next_broadcast <= System.currentTimeMillis()) {
						System.out.println("its next_broadcast time ");
						broadcast_outstanding();
						next_broadcast = System.currentTimeMillis() + broadcast_delay;
					}
					String s = scanner.nextLine();
					String[] command = s.split("\\s+");
					switch(command[0]) {
					case "getQuickBalance":
						//TODO: complete this
						getQuickBalance();
						break;
					case "getSynchedBalance":
						getSynchedBalance();
						break;
					case "deposit":
						deposit(Double.parseDouble(command[1]));
						//TODO: complete this
						break;					
					case "addInterest":
						addInterest(Double.parseDouble(command[1]));
						break;
					case "getHistory":
						getHistory();
						break;
					case "checkTxStatus":

						for(Transaction trans: executed_list) {
							if(trans.unique_id.equalsIgnoreCase(command[1])) {
								System.out.println("Transaction has been applied");
							}
						}
						break;
					case "cleanHistory":
						cleanHistory();
						break;
					case "memberInfo":
						getMembers();
						break;
					case "sleep":
						int sleepDuration = Integer.parseInt(command[1]);
						try {
							Thread.sleep(sleepDuration * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case "exit":
						exit();
						break;
					default:
						System.out.println("No such command '" + s.toString() + "'");
					}				
				}		
			}
		}	
	}

	
	
	/*
	 * Exits the program after leaving the group and connection.
	 * graceful exit.
	 */
	public void exit() {
		
		connection.remove(this);
		
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
		
		System.out.println("[EXITING]");
		System.exit(1);
	}
	
	/*
	 * Applies the received deposit command from the SPREAD server
	 */
	public void handleDeposit(String command) {
		
		String[] data = command.split("\\s+");
		Transaction trans = new Transaction(data[0] + " " + data[1], data[2]);
		if(outstanding_collection.contains(trans)) {
			outstanding_collection.remove(trans);
		}
		
		if(!executed_list.contains(trans)) {
			executed_list.add(trans);
			balance += Double.parseDouble(data[1]);
			this.order_counter++;
		}
	}
	
	
	/*
	 * Applies the received interes command from the spread server
	 */
	public void handleInterest(String command) {
		
		String[] data = command.split("\\s+");
		Transaction trans = new Transaction(data[0] + " " + data[1], data[2]);
		if(outstanding_collection.contains(trans)) {
			//System.out.println("it does!!");
			outstanding_collection.remove(trans);
		}
		
		if(!executed_list.contains(trans)) {
			executed_list.add(trans);
			balance = balance * (1 + Double.parseDouble(data[1]) / 100);
			this.order_counter++;
		}
	}

	
	/*
	 * * (non-Javadoc)
	 * @see spread.BasicMessageListener#messageReceived(spread.SpreadMessage)
	 * 
	 * Gets invoke when the connection has a new message, handles both membership messages and regular data messages
	 */
	@Override
	public void messageReceived(SpreadMessage message) {
				
		if (message.isRegular()) {
			//System.out.println("message recieved");
			System.out.println("received '" + new String (message.getData()) + "' from " + message.getSender());
			String[] commands = new String(message.getData()).split("\\|");
			
			for(String s: commands) {
				String[] data = s.split("\\s+");
				switch(data[0]) {
				case "deposit":
					handleDeposit(s);
					
					//System.out.println(balance);

					break;
				case "state":
					if(!initialized) {
						balance = Double.parseDouble(data[1]);
						initialized = true;
					}
					
					//System.out.println(balance);

					break;
				case "interest":
					handleInterest(s);
					break;
					
				case "cleanHistory":
					cleanHistory();
					break;
				default:
					System.out.println("Unknown command of '" + new String (message.getData())  + "'");
				}
			}
			
			
		}
		
		else if (message.isMembership()) {
			MembershipInfo info = message.getMembershipInfo();
			
			if (info.isCausedByJoin()) {
				System.out.println(info.getJoined() + " joined the group");
				if(initialized) {
					System.out.println("sending my state");
					sendMessage("state " + balance);					
				}
				
				
				group_members.add(info.getJoined());
				group_size++;
				
				SpreadGroup[] temp = info.getMembers();
				
				if(group_size < temp.length) {
					group_size = temp.length;
					
					for (SpreadGroup sg: info.getMembers()) {
						if(! group_members.contains(sg)) {
							group_members.add(sg);
						}
					}	
				}
				
				if(group_size >= number_of_replicas) {
					started = true;
				}
				
				getMembers();
			}
			
			else if (info.isCausedByLeave()) {
				
				if(info.isSelfLeave()) {
					return;
				}
				
				System.out.println(info.getLeft() + " left the group");
				group_members.remove(info.getLeft());
				group_size--;
			}
			
			else if (info.isCausedByDisconnect()) {
				System.out.println(info.getDisconnected() + " disconnected");
				group_members.remove(info.getDisconnected());
				group_size--;
			}
			
			else {
				System.out.println("unknown service from" + message.getServiceType());
			}
			
		}
			
		
	}
	
}
