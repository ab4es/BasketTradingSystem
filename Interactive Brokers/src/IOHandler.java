import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class IOHandler {

	public final static String orderCounterFilePath = "./src/orderCounter.txt";

	// Used to store any errors that may result from incorrect formatting in
	// the CSV order file
	static ArrayList<Integer> csvErrors = new ArrayList<Integer>();

	// Read the orderCounter from the text file
	public static int readOrderCounter() {
		int orderCounter = 0;
		Scanner scanner;
		try {
			scanner = new Scanner(new File(orderCounterFilePath));
			orderCounter = scanner.nextInt();
		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
		}
		return orderCounter;
	}

	// Read all Contracts and Orders from selected CSV file
	public static ArrayList readCSV(File file) {
		Basket.clearContracts();
		Basket.clearBrokenContracts();
		Basket.clearOrders();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(file));
			int counter = 0;
			while ((line = br.readLine()) != null) {
				String[] order = line.split(cvsSplitBy);
				// Ensures the first row of the CSV has the correct headers
				if (counter == 0) {
					if (!order[0].equalsIgnoreCase("Action"))
						csvErrors.add(17);
					if (!order[1].equalsIgnoreCase("Quantity"))
						csvErrors.add(18);
					if (!order[2].equalsIgnoreCase("Symbol"))
						csvErrors.add(19);
					if (!order[3].equalsIgnoreCase("SecType"))
						csvErrors.add(20);
					if (!order[4].equalsIgnoreCase("Exchange"))
						csvErrors.add(21);
					if (!order[5].equalsIgnoreCase("Currency"))
						csvErrors.add(22);
					if (!order[6].equalsIgnoreCase("TimeInForce"))
						csvErrors.add(23);
					if (!order[7].equalsIgnoreCase("OrderType"))
						csvErrors.add(24);
					if (!order[8].equalsIgnoreCase("LmtPrice"))
						csvErrors.add(25);
					if (!order[9].equalsIgnoreCase("BasketTag"))
						csvErrors.add(26);
					if (!order[10].equalsIgnoreCase("Account"))
						csvErrors.add(27);
					if (!order[11].equalsIgnoreCase("Group"))
						csvErrors.add(28);
					if (!order[12].equalsIgnoreCase("Method"))
						csvErrors.add(29);
					if (!order[13].equalsIgnoreCase("OrderCreationTime"))
						csvErrors.add(30);
					if (!order[14].equalsIgnoreCase("OutsideRth"))
						csvErrors.add(31);
					if (!order[15].equalsIgnoreCase("OrderRef"))
						csvErrors.add(32);
					if (!order[16].equalsIgnoreCase("Profile"))
						csvErrors.add(33);
				}

				// Ensures the remaining rows of the CSV have the correct type
				// of data
				else {
					String action = "";
					if (order[0].equals("BUY") || order[0].equals("SELL")
							|| order[0].equals("SSHORT"))
						action = order[0];
					else
						csvErrors.add(0);

					int quantity = Integer.parseInt(order[1]);

					String symbol = order[2].replaceAll("\\s+", "");

					String secType = "";
					if (order[3].equals("STK") || order[3].equals("OPT")
							|| order[3].equals("FUT") || order[3].equals("IND")
							|| order[3].equals("FOP")
							|| order[3].equals("CASH")
							|| order[3].equals("BAG")
							|| order[3].equals("NEWS"))
						secType = order[3];
					else
						csvErrors.add(3);

					String[] exchangeString = order[4].split("/");
					String exchange = exchangeString[0];
					String primaryExch = exchangeString[1];

					String currency = order[5];

					String tif = "";
					if (order[6].equals("DAY") || order[6].equals("GTC")
							|| order[6].equals("IOC") || order[6].equals("GTD"))
						tif = order[6];
					else
						csvErrors.add(6);

					String orderType = order[7];
					if (order[7].equals("LMT") || order[7].equals("MKT")
							|| order[7].equals("MTL") || order[7].equals("STP")
							|| order[7].equals("STP LMT")
							|| order[7].equals("MIT") || order[7].equals("LIT")
							|| order[7].equals("TRAIL")
							|| order[7].equals("TRAIL LIMIT")
							|| order[7].equals("TRAIL MIT")
							|| order[7].equals("TRAIL LIT")
							|| order[7].equals("REL") || order[7].equals("RPI")
							|| order[7].equals("MOC") || order[7].equals("LOC")
							|| order[7].equals("PEG BENCH"))
						orderType = order[7];
					else
						csvErrors.add(7);

					double lmtPrice = Double.parseDouble(order[8]);

					String basketTag = order[9];

					String account = order[10];

					String ocaGroup = order[11];

					int triggerMethod = 0;
					if (!order[12].isEmpty())
						triggerMethod = Integer.parseInt(order[12]);

					String orderCreationTime = order[13];

					boolean outsideRth = false;
					if (order[14].equals("TRUE") || order[14].equals("FALSE"))
						outsideRth = Boolean.parseBoolean(order[14]);
					else
						csvErrors.add(14);

					String orderRef = order[15];

					String faProfile = order[16] + "1";

					// Load Contract into Basket
					Basket.addContract(symbol, secType, exchange, primaryExch,
							currency);
					// Load Order into Basket
					Basket.addOrder(action, quantity, tif, orderType, lmtPrice,
							account, ocaGroup, triggerMethod, outsideRth,
							orderRef, faProfile);
				}
				counter++;
				Basket.setInitialBasketSize();
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return csvErrors;
	}

	// Write the updated orderCounter to the respective text file
	public static void updateorderCounter(int orderCounter) {
		System.out.println("Order Counter: " + orderCounter);
		System.out.println();

		// Write to file
		try {
			File file = new File(orderCounterFilePath);
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("" + orderCounter);
			bw.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	/*
	 * Getters
	 */
	public static ArrayList getCSVErrors() {
		return csvErrors;
	}

	public static void main(String[] args) {

	}

}
