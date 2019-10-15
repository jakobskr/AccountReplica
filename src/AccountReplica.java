import java.net.InetAddress;
import java.net.UnknownHostException;

import spread.*;


public class AccountReplica {
	String account_name, server_adress;
	int number_of_replicas = 0;
	
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
	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("hey");
			return;
		}
		
		AccountReplica ar = new AccountReplica(args[1], args[0], Integer.parseInt(args[2]));
		
		
		SpreadConnection connection = new SpreadConnection();
		try {
			connection.connect(InetAddress.getByName(ar.server_adress),4803,ar.account_name,false,false);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("did it work?");
		System.out.println(ar);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("awoo");
		
	}
}
