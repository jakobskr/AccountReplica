package spread;

import java.io.File;
import java.util.Scanner;

public class Test {
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
