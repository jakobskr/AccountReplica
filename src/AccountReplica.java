import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import spread.*;


public class AccountReplica {
	String account_name, server_adress;
	int number_of_replicas = 0;
	SpreadConnection connection;
	SpreadGroup group;
	double balance = 0.0;
	
	public AccountReplica() {
		
	}
	
	public AccountReplica(String an, String sa, int n) {
		this.account_name = an;
		this.server_adress = sa;
		this.number_of_replicas = n;
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
			ar.connection.connect(InetAddress.getByName(ar.server_adress),4803,id,false,false);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ar.group = new SpreadGroup();
		
		try {
			ar.group.join(ar.connection,"group");
		} catch (SpreadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("did it work?");
		System.out.println(ar);
		
		
		ar.waitForOthers();
		
		try {
			ar.group.leave();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ar.connection.disconnect();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		System.out.println("awoo");
		
	}
	
	public SpreadMessage waitForOthers() throws InterruptedIOException, SpreadException {
		int others = 1;
		
		System.out.println("waiting for something to happen :)");
		SpreadMessage message= this.connection.receive();
		System.out.println(message);
		
		return message;
	}
}
