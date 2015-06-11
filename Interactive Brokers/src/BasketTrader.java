import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Order;

public class BasketTrader {

	CustomWrapper cw;
	public static EClientSocket conn;
	ConnectionPanel connectionPanel = new ConnectionPanel(this);
	BasketOrderPanel basketOrderPanel = new BasketOrderPanel(this);

	ArrayList<Contract> contracts = new ArrayList<Contract>();
	ArrayList<Order> orders = new ArrayList<Order>();
	ArrayList<Contract> brokenContracts = new ArrayList<Contract>();

	int initialBasketSize = 0;
	int orderCounter = 0;
	Object[][] rowData = new Object[1][5];

	public BasketTrader() {
		cw = new CustomWrapper(this);
	}

	public boolean connect(String host, int port, int clientId) {
		System.out.println("Connecting...");
		System.out.println();
		conn = new EClientSocket(cw);
		conn.eConnect(host, port, clientId);
		boolean isConn = false;
		long startTime = System.currentTimeMillis();
		while (!conn.isConnected()
				&& (System.currentTimeMillis() - startTime) < 5000)
			System.out.print("");
		if (conn.isConnected()) {
			isConn = true;
			System.out.println();
			System.out.println("Connected!");
			System.out.println();
		} else {
			System.out.println();
			System.out.println("Connection failed!");
			System.out.println();
		}

		Scanner scanner;
		try {
			scanner = new Scanner(new File("./src/orderCounter.txt"));
			orderCounter = scanner.nextInt();
		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
		}
		return isConn;
	}

	public boolean disconnect() {
		System.out.println("Disconnecting...");
		System.out.println();
		conn.eDisconnect();
		boolean isConn = true;
		long startTime = System.currentTimeMillis();
		while (conn.isConnected()
				&& (System.currentTimeMillis() - startTime) < 5000)
			System.out.print("");
		if (!conn.isConnected()) {
			isConn = false;
			System.out.println();
			System.out.println("Disconnected!");
			System.out.println();
		} else {
			System.out.println();
			System.out.println("Disconnection failed!");
			System.out.println();
		}
		return !isConn;
	}

	public ArrayList readCSV(File file) {
		contracts.clear();
		orders.clear();

		ArrayList<Integer> csvErrors = new ArrayList<Integer>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		String[] columnNames = { "Symbol", "Action", "Quantity", "Exchange",
				"OrderType" };
		rowData[0] = columnNames;

		try {
			br = new BufferedReader(new FileReader(file));
			int counter = 0;
			while ((line = br.readLine()) != null) {
				String[] order = line.split(cvsSplitBy);
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

					this.addContract(symbol, secType, exchange, primaryExch,
							currency);
					this.addOrder(action, quantity, tif, orderType, lmtPrice,
							account, ocaGroup, triggerMethod, outsideRth,
							orderRef, faProfile);
					String[] row = { symbol, action, "" + quantity, exchange,
							orderType };
					Object[][] temp = new Object[rowData.length + 1][rowData[0].length];
					for (int x = 0; x < rowData.length; x++)
						for (int y = 0; y < rowData[0].length; y++)
							temp[x][y] = rowData[x][y];
					temp[temp.length - 1] = row;
					rowData = temp;
				}
				counter++;
				initialBasketSize = orders.size();
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return csvErrors;
	}

	public void addContract(String symbol, String secType, String exchange,
			String primaryExch, String currency) {
		Contract c = new Contract();
		c.m_symbol = symbol;
		c.m_secType = secType;
		c.m_exchange = exchange;
		c.m_primaryExch = primaryExch;
		c.m_currency = currency;

		contracts.add(c);

		System.out.println("Added " + c.toString());
	}

	public void addOrder(String action, int quantity, String tif,
			String orderType, double lmtPrice, String account, String ocaGroup,
			int triggerMethod, boolean outsideRth, String orderRef,
			String faProfile) {
		Order o = new Order();
		o.m_action = action;
		o.m_totalQuantity = quantity;
		o.m_tif = tif;
		o.m_orderType = orderType;
		o.m_lmtPrice = lmtPrice;
		o.m_account = account;
		o.m_ocaGroup = ocaGroup;
		o.m_triggerMethod = triggerMethod;
		o.m_outsideRth = outsideRth;
		o.m_orderRef = orderRef;
		o.m_faProfile = faProfile;
		o.m_orderId = orderCounter;
		o.m_permId = orderCounter;

		orders.add(o);

		System.out.println("Added " + o.toString());
		System.out.println();

		orderCounter++;
		this.updateorderCounter();
	}

	public void updateBidAskPrices(int tickerId, int field, double price,
			int canAutoExecute) {
		int lowestId = orders.get(0).m_orderId;

		int id = -1;
		if ((tickerId - lowestId) >= 0) {
			id = tickerId - lowestId;
		} else {
			id = tickerId - lowestId + initialBasketSize;
		}

		if (id >= 0 && id < orders.size()) {
			if (field == 1) {
				orders.get(id).m_bid = price;
			}
			if (field == 2) {
				orders.get(id).m_ask = price;
			}
			if (field == 4) {
				orders.get(id).m_lastPrice = price;
			}
		}
	}

	public void updateBidAskSizes(int tickerId, int field, int size) {
		int lowestId = orders.get(0).m_orderId;

		int id = -1;
		if ((tickerId - lowestId) >= 0) {
			id = tickerId - lowestId;
		} else {
			id = tickerId - lowestId + initialBasketSize;
		}
		if (id >= 0 && id < orders.size()) {
			if (field == 0)
				orders.get(id).m_bidSize = size;
			if (field == 3)
				orders.get(id).m_askSize = size;
		}
	}

	public void updatePNL(CommissionReport commissionReport) {
		System.out.println("P&L Updated: " + commissionReport.m_realizedPNL);
		System.out.println();
		basketOrderPanel.updatePNL(commissionReport.m_realizedPNL);
	}

	public void updateOrderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {
		if (status.equals("Filled")) {
			for (int x = 0; x < orders.size(); x++) {
				if (orders.get(x).m_orderId == orderId
						&& !orders.get(x).m_filled) {
					orders.get(x).m_filled = true;
					System.out.println("Order " + orders.get(x).m_orderId
							+ " filled");
					System.out.println();
				}
			}
		} else {
			for (int x = 0; x < orders.size(); x++) {
				if (orders.get(x).m_orderId == orderId
						&& remaining != orders.get(x).m_totalQuantity) {
					orders.get(x).m_totalQuantity = remaining;
					System.out.println("Order " + orders.get(x).m_orderId
							+ " quantity updated: "
							+ orders.get(x).m_totalQuantity);
					System.out.println();
				}
			}
		}
	}

	public void updateorderCounter() {
		System.out.println("Order Counter: " + orderCounter);
		System.out.println();
		try {
			File file = new File("./src/orderCounter.txt");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("" + orderCounter);
			bw.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public void manualPNLUpdate() {
		conn.reqExecutions(orderCounter, null);
	}

	public Object[][] getRowData() {
		Object[][] temp = rowData;
		rowData = null;
		return temp;
	}

	public boolean requestMarketData(Order o, Contract c) {
		boolean marketDataFound = true;

		conn.reqMktData(o.m_orderId, c, null, true, null);

		long startTime = System.currentTimeMillis();
		while ((o.m_bid == 0.0 || o.m_ask == 0.0 || o.m_lastPrice == 0.0
				|| o.m_bidSize == 0 || o.m_askSize == 0)
				&& (System.currentTimeMillis() - startTime) < 10000) {
			System.out.print("");
		}

		if (o.m_bid == 0.0 || o.m_ask == 0.0 || o.m_lastPrice == 0.0
				|| o.m_bidSize == 0 || o.m_askSize == 0) {
			brokenContracts.add(c);
			marketDataFound = false;
		}
		return marketDataFound;
	}

	public void cancelMarketData(Order o) {
		conn.cancelMktData(o.m_orderId);
	}

	public void cancelOrders() {
		for (int i = 0; i < orders.size(); i++) {
			conn.cancelOrder(orders.get(i).m_orderId);
			System.out.println("Order" + (orders.get(i).m_orderId)
					+ " canceled");
		}
		contracts.clear();
		orders.clear();
		System.out.println();
	}

	public void spreadStrategy_Transmit(Order o, Contract c) {
		o.m_orderType = "LMT";

		double bid = o.m_bid;
		double ask = o.m_ask;
		double price = o.m_lastPrice;
		int bidSize = o.m_bidSize;
		int askSize = o.m_askSize;
		double spread = ask - bid;
		double lmtMid = Math.round(((bid + ask) / 2) * 100.0) / 100.0;
		double ratio = (float) bidSize / askSize;

		System.out.println(c.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		if (spread <= 0.01) {
			o.m_orderType = "MKT";
			System.out.println("MKT");
		} else if (spread > 0.01 && spread <= 0.02) {
			if (price < 5) {
				o.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			} else {
				o.m_orderType = "MKT";
				System.out.println("MKT");
			}
		} else if (spread > 0.02 && spread <= 0.05) {
			if (ratio > 10 && o.m_action.equalsIgnoreCase("BUY")) {
				o.m_orderType = "MKT";
				System.out.println("MKT");
			} else if (ratio < 0.1 && o.m_action.equalsIgnoreCase("SELL")) {
				o.m_orderType = "MKT";
				System.out.println("MKT");
			} else {
				o.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			}
		} else if (spread > 0.05) {
			o.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		} else {
			o.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		System.out.println("PRICE: " + o.m_lastPrice);
		System.out.println("BID: " + o.m_bid);
		System.out.println("ASK: " + o.m_ask);
		System.out.println("MID: " + lmtMid);
		System.out.println("SPREAD: " + spread);
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio);
		System.out.println();
	}

	public ArrayList transmitOrders() {
		brokenContracts.clear();

		for (int i = 0; i < orders.size(); i++) {
			boolean hasData = this.requestMarketData(orders.get(i),
					contracts.get(i));
			if (hasData) {
				this.spreadStrategy_Transmit(orders.get(i), contracts.get(i));
				this.cancelMarketData(orders.get(i));
			} else {
				this.cancelMarketData(orders.get(i));
				contracts.remove(i);
				orders.remove(i);
			}
		}

		for (int x = 0; x < orders.size(); x++) {
			conn.placeOrder(orders.get(x).m_orderId, contracts.get(x),
					orders.get(x));
			System.out.println("Order" + (orders.get(x).m_orderId)
					+ " Transmitted");
		}
		System.out.println();
		return brokenContracts;
	}

	public ArrayList cancelAndCorrectOrders() {
		brokenContracts.clear();

		ArrayList<Contract> tempContracts = new ArrayList<Contract>();
		ArrayList<Order> tempOrders = new ArrayList<Order>();

		for (int i = 0; i < orders.size(); i++) {
			boolean hasData = this.requestMarketData(orders.get(i),
					contracts.get(i));
			if (hasData) {
				if (!orders.get(i).m_filled) {
					tempContracts.add(contracts.get(i));
					tempOrders.add(orders.get(i));
					while (!conn.cancelOrder(orders.get(i).m_orderId))
						System.out.println("");
					System.out.println("Order " + (orders.get(i).m_orderId)
							+ " canceled");
					System.out.println();
				}
				this.cancelMarketData(orders.get(i));
			} else {
				this.cancelMarketData(orders.get(i));
				contracts.remove(i);
				orders.remove(i);
			}
		}

		contracts.clear();
		orders.clear();

		for (int i = 0; i < tempOrders.size(); i++) {
			Contract c = tempContracts.get(i);
			Order o = tempOrders.get(i);
			this.addContract(c.m_symbol, c.m_secType, c.m_exchange,
					c.m_primaryExch, c.m_currency);
			this.addOrder(o.m_action, o.m_totalQuantity, o.m_tif,
					o.m_orderType, o.m_lmtPrice, o.m_account, o.m_ocaGroup,
					o.m_triggerMethod, o.m_outsideRth, o.m_orderRef,
					o.m_faProfile);
		}

		tempContracts.clear();
		tempOrders.clear();

		if (!orders.isEmpty()) {
			System.out.println("Orders being corrected...");
			System.out.println();

			for (int i = 0; i < orders.size(); i++) {
				conn.reqMktData(orders.get(i).m_orderId, contracts.get(i),
						null, true, null);

				while (orders.get(i).m_bid == 0.0 || orders.get(i).m_ask == 0.0
						|| orders.get(i).m_lastPrice == 0.0
						|| orders.get(i).m_bidSize == 0
						|| orders.get(i).m_askSize == 0) {
					System.out.print("");
				}

				double bid = orders.get(i).m_bid;
				double ask = orders.get(i).m_ask;
				double price = orders.get(i).m_lastPrice;
				double lmtPrice = orders.get(i).m_lmtPrice;
				int bidSize = orders.get(i).m_bidSize;
				int askSize = orders.get(i).m_askSize;
				double spread = ask - bid;
				double lmtMid = Math.round(((bid + ask) / 2) * 100.0) / 100.0;
				double ratio = (float) bidSize / askSize;

				System.out.println(contracts.get(i).m_symbol);
				System.out.println("====");
				System.out.print("STRATEGY: ");

				if ((orders.get(i).m_action.equals("BUY") && lmtPrice >= bid)
						|| (orders.get(i).m_action.equals("SELL") && lmtPrice <= ask)) {
					if (spread <= 0.01 && price > 5) {
						orders.get(i).m_orderType = "MKT";
						System.out.println("MKT");
					} else {
						System.out.println("HOLD");
					}
				} else if (spread <= 0.01) {
					orders.get(i).m_orderType = "MKT";
					System.out.println("MKT");
				} else if (spread > 0.01 && spread <= 0.02) {
					if (price < 5) {
						orders.get(i).m_lmtPrice = lmtMid;
						System.out.println("LMT MID");
					} else {
						orders.get(i).m_orderType = "MKT";
						System.out.println("MKT");
					}
				} else if (spread > 0.02 && spread <= 0.05) {
					if (ratio > 10
							&& orders.get(i).m_action.equalsIgnoreCase("BUY")) {
						orders.get(i).m_orderType = "MKT";
						System.out.println("MKT");
					} else if (ratio < 0.1
							&& orders.get(i).m_action.equalsIgnoreCase("SELL")) {
						orders.get(i).m_orderType = "MKT";
						System.out.println("MKT");
					} else {
						orders.get(i).m_lmtPrice = lmtMid;
						System.out.println("LMT MID");
					}
				} else if (spread > 0.05) {
					orders.get(i).m_lmtPrice = lmtMid;
					System.out.println("LMT MID");
				} else {
					orders.get(i).m_lmtPrice = lmtMid;
					System.out.println("LMT MID");
				}

				System.out.println("PRICE: " + orders.get(i).m_lastPrice);
				System.out.println("LMT PRICE: " + orders.get(i).m_lmtPrice);
				System.out.println("BID: " + orders.get(i).m_bid);
				System.out.println("ASK: " + orders.get(i).m_ask);
				System.out.println("MID: " + lmtMid);
				System.out.println("SPREAD: " + spread);
				System.out.println("BID SIZE: " + bidSize);
				System.out.println("ASK SIZE: " + askSize);
				System.out.println("RATIO: " + ratio);
				System.out.println();

				conn.cancelMktData(orders.get(i).m_orderId);
			}

			for (int x = 0; x < orders.size(); x++) {
				conn.placeOrder(orders.get(x).m_orderId, contracts.get(x),
						orders.get(x));
				System.out.println("Order " + (orders.get(x).m_orderId)
						+ " Corrected");
			}
		} else {
			System.out.println("No orders to be corrected");
		}
		System.out.println();
		return brokenContracts;
	}

	public void run() {
	}

	public static void main(String[] args) {
		BasketTrader t = new BasketTrader();
		t.run();
	}
}