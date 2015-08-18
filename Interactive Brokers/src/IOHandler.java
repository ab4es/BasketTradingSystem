import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
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
	public static ArrayList readCSV(File file) throws SQLException {
		csvErrors.clear();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(file));
			// Ensures the each order from the CSV has the correct number of
			// fields
			boolean correctLength = true;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				String[] transaction = line.split(cvsSplitBy);

				if (transaction.length != 17)
					correctLength = false;

				if (correctLength) {
					// Ensures the first row of the CSV has the correct headers
					if (counter == 0) {
						if (!transaction[0].equalsIgnoreCase("Action"))
							csvErrors.add(17);
						if (!transaction[1].equalsIgnoreCase("Quantity"))
							csvErrors.add(18);
						if (!transaction[2].equalsIgnoreCase("Symbol"))
							csvErrors.add(19);
						if (!transaction[3].equalsIgnoreCase("SecType"))
							csvErrors.add(20);
						if (!transaction[4].equalsIgnoreCase("Exchange"))
							csvErrors.add(21);
						if (!transaction[5].equalsIgnoreCase("Currency"))
							csvErrors.add(22);
						if (!transaction[6].equalsIgnoreCase("TimeInForce"))
							csvErrors.add(23);
						if (!transaction[7].equalsIgnoreCase("OrderType"))
							csvErrors.add(24);
						if (!transaction[8].equalsIgnoreCase("LmtPrice"))
							csvErrors.add(25);
						if (!transaction[9].equalsIgnoreCase("BasketTag"))
							csvErrors.add(26);
						if (!transaction[10].equalsIgnoreCase("Account"))
							csvErrors.add(27);
						if (!transaction[11].equalsIgnoreCase("Group"))
							csvErrors.add(28);
						if (!transaction[12].equalsIgnoreCase("Method"))
							csvErrors.add(29);
						if (!transaction[13].equalsIgnoreCase("OrderCreationTime"))
							csvErrors.add(30);
						if (!transaction[14].equalsIgnoreCase("OutsideRth"))
							csvErrors.add(31);
						if (!transaction[15].equalsIgnoreCase("OrderRef"))
							csvErrors.add(32);
						if (!transaction[16].equalsIgnoreCase("Profile"))
							csvErrors.add(33);
					}

					// Ensures the remaining rows of the CSV have the correct
					// type
					// of data
					else {
						String action = "";
						if (transaction[0].equals("BUY") || transaction[0].equals("SELL")
								|| transaction[0].equals("SSHORT"))
							action = transaction[0];
						else
							csvErrors.add(0);

						int totalQuantity = Integer.parseInt(transaction[1]);

						String symbol = transaction[2].replaceAll("\\s+", "");

						String secType = "";
						if (transaction[3].equals("STK") || transaction[3].equals("OPT")
								|| transaction[3].equals("FUT")
								|| transaction[3].equals("IND")
								|| transaction[3].equals("FOP")
								|| transaction[3].equals("CASH")
								|| transaction[3].equals("BAG")
								|| transaction[3].equals("NEWS"))
							secType = transaction[3];
						else
							csvErrors.add(3);

						String[] exchangeString = transaction[4].split("/");
						String exchange = exchangeString[0];
						String primaryExch = exchangeString[1];

						String currency = transaction[5];

						String tif = "";
						if (transaction[6].equals("DAY") || transaction[6].equals("GTC")
								|| transaction[6].equals("IOC")
								|| transaction[6].equals("GTD"))
							tif = transaction[6];
						else
							csvErrors.add(6);

						String orderType = transaction[7];
						if (transaction[7].equals("LMT") || transaction[7].equals("MKT")
								|| transaction[7].equals("MTL")
								|| transaction[7].equals("STP")
								|| transaction[7].equals("STP LMT")
								|| transaction[7].equals("MIT")
								|| transaction[7].equals("LIT")
								|| transaction[7].equals("TRAIL")
								|| transaction[7].equals("TRAIL LIMIT")
								|| transaction[7].equals("TRAIL MIT")
								|| transaction[7].equals("TRAIL LIT")
								|| transaction[7].equals("REL")
								|| transaction[7].equals("RPI")
								|| transaction[7].equals("MOC")
								|| transaction[7].equals("LOC")
								|| transaction[7].equals("PEG BENCH"))
							orderType = transaction[7];
						else
							csvErrors.add(7);

						double lmtPrice = Double.parseDouble(transaction[8]);

						String basketTag = transaction[9];

						String account = transaction[10];

						String faGroup = transaction[11];

						String faMethod = transaction[12];

						String orderCreationTime = transaction[13];

						boolean outsideRth = false;
						if (transaction[14].equals("TRUE")
								|| transaction[14].equals("FALSE"))
							outsideRth = Boolean.parseBoolean(transaction[14]);
						else
							csvErrors.add(14);

						String orderRef = transaction[15];

						String faProfile = transaction[16] + "1";

						Database.addTransaction(currency, exchange,
								primaryExch, secType, symbol, action, lmtPrice,
								orderType, totalQuantity, tif, faGroup,
								faMethod, faProfile, account, orderRef,
								outsideRth);
					}
				}
				counter++;
			}
			if (!correctLength) {
				csvErrors.add(34);
				System.out.println("No orders loaded due to formatting error!");
				System.out.println();
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
