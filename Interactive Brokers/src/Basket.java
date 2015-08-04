import java.io.File;
import java.util.ArrayList;

import com.ib.client.Contract;
import com.ib.client.Order;

public class Basket {

	// Stores all Contracts
	static ArrayList<Contract> contracts = new ArrayList<Contract>();
	// Stores all Orders
	static ArrayList<Order> orders = new ArrayList<Order>();
	// Stores all "broken" Contracts
	static ArrayList<Contract> brokenContracts = new ArrayList<Contract>();

	// The initial size of the loaded Basket
	private static int initialBasketSize = 0;
	// A counter to hold the global place of the Order
	private static int orderCounter = 0;

	/*
	 * Constructor
	 */
	public Basket(File file) {
		// Set the orderCounter
		orderCounter = IOHandler.readOrderCounter();
		// Fill the contracts and orders ArrayLists from the CSV file
		IOHandler.readCSV(file);
	}

	// Add a Contract
	public static void addContract(String symbol, String secType,
			String exchange, String primaryExch, String currency) {
		Contract c = new Contract();

		// Set Contract parameters
		c.m_symbol = symbol;
		c.m_secType = secType;
		c.m_exchange = exchange;
		c.m_primaryExch = primaryExch;
		c.m_currency = currency;

		// Store Contract
		contracts.add(c);

		System.out.println("Added " + c.toString());
	}

	// Add an Order
	public static void addOrder(String action, int quantity, String tif,
			String orderType, double lmtPrice, String account, String ocaGroup,
			int triggerMethod, boolean outsideRth, String orderRef,
			String faProfile) {
		Order o = new Order();

		// Set Order parameters
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

		// Store Order
		orders.add(o);

		System.out.println("Added " + o.toString());
		System.out.println();

		// Update the orderCounter and write it to file
		orderCounter++;
		IOHandler.updateorderCounter(orderCounter);
	}

	// Replace an Order within Orders with a changed Order
	public static void replaceOrder(Order newOrder) {
		for (Order order : orders)
			if (order.m_orderId == newOrder.m_orderId)
				order = newOrder;
	}

	/*
	 * Updaters
	 */
	// Update the Bid, Ask, and/or Last Price
	public static void updateBidAskLastPrice(int tickerId, int field,
			double price, int canAutoExecute) {
		int id = getOrderIndex(tickerId);

		// Update the Bid, Ask, and/or Last Price
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

	// Update the Bid Ask sizes
	public static void updateBidAskSizes(int tickerId, int field, int size) {
		int id = getOrderIndex(tickerId);

		// Update the Bid Ask sizes
		if (id >= 0 && id < orders.size()) {
			if (field == 0)
				orders.get(id).m_bidSize = size;
			if (field == 3)
				orders.get(id).m_askSize = size;
		}
	}

	// Update the Order status
	public static void updateOrderStatus(int orderId, String status,
			int filled, int remaining, double avgFillPrice, int permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld) {
		// If the Order has been filled, set its boolean m_filled to true
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
		}
		// If the Order has not been filled, update the quantity of shares
		// remaining
		else {
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
		// If the Order has been submitted, set its boolean m_submitted to true
		if (status.equals("Submitted")) {
			for (int x = 0; x < orders.size(); x++) {
				if (orders.get(x).m_orderId == orderId) {
					orders.get(x).m_submitted = true;
				}
			}
		}
	}

	/*
	 * Getters
	 */
	public static int getOrderCounter() {
		return orderCounter;
	}

	// Get the Order index from the tickerId
	public static int getOrderIndex(int tickerId) {
		int lowestId = orders.get(0).m_orderId;

		// Set the appropriate Order index
		int id = -1;
		if ((tickerId - lowestId) >= 0) {
			id = tickerId - lowestId;
		} else {
			id = tickerId - lowestId + initialBasketSize;
		}

		return id;
	}

	public static ArrayList<Contract> getContracts() {
		return contracts;
	}

	public static ArrayList<Contract> getBrokenContracts() {
		return brokenContracts;
	}

	public static ArrayList<Order> getOrders() {
		return orders;
	}

	/*
	 * Setters
	 */
	public static void setOrderCounter(int orderCounterTemp) {
		orderCounter = orderCounterTemp;
	}

	public static void setInitialBasketSize() {
		initialBasketSize = orders.size();
	}

	public static void addContract(Contract contract) {
		contracts.add(contract);
	}

	public static void addBrokenContract(Contract contract) {
		brokenContracts.add(contract);
	}

	public static void addOrder(Order order) {
		orders.add(order);
	}

	public static void clearContracts() {
		contracts.clear();
	}

	public static void clearBrokenContracts() {
		brokenContracts.clear();
	}

	public static void clearOrders() {
		orders.clear();
	}

	// Remove all filled Orders and their respective Contracts from Orders and
	// Contracts
	public static void clearFilledOrders() {
		clearBrokenContracts();

		ArrayList<Contract> tempContracts = new ArrayList<Contract>();
		ArrayList<Order> tempOrders = new ArrayList<Order>();

		// Loop through all outstanding Orders
		for (int i = 0; i < orders.size(); i++) {
			boolean hasData = Socket.requestMarketData(orders.get(i),
					contracts.get(i));
			if (hasData) {
				// If the Order has not been filled yet, it can be canceled and
				// corrected and so it is added to the temporary Order ArrayList
				if (!orders.get(i).m_filled) {
					tempContracts.add(contracts.get(i));
					tempOrders.add(orders.get(i));
					// Cancel that Order as it will soon be corrected with a
					// more market accurate spread
					Socket.cancelOrder(orders.get(i));
					System.out.println("Order " + (orders.get(i).m_orderId)
							+ " canceled");
					System.out.println();
				}
				Socket.cancelOrder(orders.get(i));
			} else {
				Socket.cancelOrder(orders.get(i));
				contracts.remove(i);
				orders.remove(i);
			}
		}

		Basket.clearContracts();
		Basket.clearOrders();

		for (int i = 0; i < tempOrders.size(); i++) {
			Contract contract = tempContracts.get(i);
			Order order = tempOrders.get(i);
			addContract(contract.m_symbol, contract.m_secType,
					contract.m_exchange, contract.m_primaryExch,
					contract.m_currency);
			addOrder(order.m_action, order.m_totalQuantity, order.m_tif,
					order.m_orderType, order.m_lmtPrice, order.m_account,
					order.m_ocaGroup, order.m_triggerMethod,
					order.m_outsideRth, order.m_orderRef, order.m_faProfile);
		}
	}

	public static void main(String[] args) {

	}

}
