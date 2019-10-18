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
	boolean started;
	SpreadConnection connection;
	SpreadGroup group;
	ArrayList<SpreadGroup> group_members = new ArrayList<SpreadGroup>();
	double balance = 0.0;
	static String account_id;
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
		

		account_id = Long.toString(System.nanoTime());
		account_id = account_id.substring( account_id.length() / 2, account_id.length() - 1);
		
		ar.connection = new SpreadConnection();
		try {
			ar.connection.connect(InetAddress.getByName(ar.server_adress),4803,account_id,false,true);
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
		ar.waitForOthers();	
		try {
			ar.input_handler();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("we have started");
		
		try {
			Thread.sleep(5000);
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
		while(outstanding_collection.size()>0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(balance);
	}
	
	public void deposit(int amount) {
		Transaction t = new Transaction("deposit " + amount, account_id + "-" + order_counter);
		outstanding_collection.add(t);
		order_counter++;
		//sendMessage(t.command + " " + t.unique_id);
	}
	
	public void getInterest(int interest) {
		Transaction t = new Transaction("interest " + interest, account_id + "-" + order_counter);
		outstanding_collection.add(t);
		order_counter++;
		//sendMessage(t.command + " " + t.unique_id);
	}
	
	public void getHistory() {
		System.out.println("executed:");
		for(Transaction t : executed_list) {
			System.out.println(t.command);
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
		int others = 1;
		int i = 0;		
		System.out.println("entering etheral hellscape");
		
		while (true) {
			if(group_size >= number_of_replicas) {
				System.out.println(group_size + number_of_replicas);
				break;
			}
		}
				
		System.out.println("i have broken free from this ehteral hellscape");
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
		System.out.println(data);
		sendMessage(data);
		
	}
	
	public void input_handler() throws FileNotFoundException {
		long broadcast_delay = 10000;
		long next_broadcast = System.currentTimeMillis() + broadcast_delay;
		String filename = null;
		while(true) {
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
						int amount = Integer.parseInt(command[1]);
						this.deposit(amount);
						break;					
				case "addInterest":
					int interest = Integer.parseInt(command[1]);
					this.getInterest(interest);
					break;
				case "getHistory":
					this.getHistory();
					break;
				case "checkTxStatus":
					int transactionID = Integer.parseInt(command[1]);
					//TODO: complete this.
					break;
				case "cleanHistory":
					//TODO: complete this.
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
					String[] command = scanner.nextLine().split("\\s+");
					switch(command[0]) {
					case "getQuickBalance":
						//TODO: complete this
						break;
					case "getSyncedBalance":
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
	}
	
	public void handleDeposit(String command) {
		System.out.println("uugug " + command);
		
		String[] data = command.split("\\s+");
		Transaction trans = new Transaction(data[0] + " " + data[1], data[2]);
		if(outstanding_collection.contains(trans)) {
			System.out.println("it does!!");
			outstanding_collection.remove(trans);
		}
		
		if(!executed_list.contains(trans)) {
			executed_list.add(trans);
			balance += Double.parseDouble(data[1]);
			this.order_counter++;
		}
	}
	
	
	public void handleInterest(String command) {
		
		String[] data = command.split("\\s+");
		Transaction trans = new Transaction(data[0] + " " + data[1], data[2]);
		if(outstanding_collection.contains(trans)) {
			System.out.println("it does!!");
			outstanding_collection.remove(trans);
		}
		
		if(!executed_list.contains(trans)) {
			executed_list.add(trans);
			balance = balance * (1 + Double.parseDouble(data[1]) / 100);
			this.order_counter++;
		}
	}

	@Override
	public void messageReceived(SpreadMessage message) {
				
		if (message.isRegular()) {
			System.out.println("message recieved");
			System.out.println(new String (message.getData()));
			String[] commands = new String(message.getData()).split("\\|");
			//System.out.println(data[0] + " ugu");
			
			for(String s: commands) {
				String[] data = s.split("\\s+");
				switch(data[0]) {
				case "deposit":
					handleDeposit(s);
					
					//System.out.println(balance);

					break;
				case "interest":
					handleInterest(s);
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
