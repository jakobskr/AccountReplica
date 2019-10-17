
import spread.*;

public class Transaction {
	String command;
	String unique_id;
	
	public Transaction(String command, String id) {
		this.command = command;
		this.unique_id = id;
	}
	
	
	public String toString() {
		return command + " " + unique_id;
	}
	
	public boolean equals(Transaction a) {
		return this.unique_id.equalsIgnoreCase(a.unique_id);		
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Transaction) {
			Transaction a = (Transaction) o;
			return a.unique_id.equalsIgnoreCase(this.unique_id);
		}
		
		return false;
	}
	
	
}