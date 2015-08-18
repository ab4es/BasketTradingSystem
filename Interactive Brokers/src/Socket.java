import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Order;

public class Socket {

	/*
	 * Initialize the EClientSocket using the CustomerWrapper
	 */
	public static EClientSocket connection = new EClientSocket(
			new CustomWrapper());

	/*
	 * Connect to Interactive Broker's Trade Workstation
	 */
	public static boolean connect(String host, int port, int clientId) {
		boolean isConnected = false;
		long startTime = System.currentTimeMillis();

		System.out.println("Connecting...");
		System.out.println();

		connection.eConnect(host, port, clientId);

		// Ensure connection is either made or times out so that no
		// errors are thrown
		while (!connection.isConnected()
				&& (System.currentTimeMillis() - startTime) < 5000)
			System.out.print("");
		if (connection.isConnected()) {
			isConnected = true;
			System.out.println("Connected!");
			System.out.println();
		} else {
			System.out.println("Connection failed!");
			System.out.println();
		}

		return isConnected;
	}

	/*
	 * Disconnect to Interactive Broker's Trade Workstation
	 */
	public static boolean disconnect() {
		boolean isConn = true;
		long startTime = System.currentTimeMillis();

		System.out.println("Disconnecting...");
		System.out.println();

		connection.eDisconnect();

		// Ensure connection is either made or times out so that no
		// errors are thrown
		while (connection.isConnected()
				&& (System.currentTimeMillis() - startTime) < 5000)
			System.out.print("");
		if (!connection.isConnected()) {
			isConn = false;
			System.out.println("Disconnected!");
			System.out.println();
		} else {
			System.out.println("Disconnection failed!");
			System.out.println();
		}

		return !isConn;
	}

	/*
	 * Transmit an order using its orderId
	 */
	public static void transmitOrder(int orderId) throws SQLException {
		Contract contract = Database.getContractWithOrderId(orderId);
		Order order = Database.getOrder(orderId);
		connection.placeOrder(order.m_orderId, contract, order);
		System.out
				.println(Database.getTimestamp() + "TRANSMITTED: " + contract);
		System.out.println("                      " + order);
		System.out.println();
	}

	/*
	 * Transmit all outstanding Orders
	 */
	public static void transmitOrders() throws SQLException {
		ArrayList<Order> orders = Database.getAllOrders();
		if (!orders.isEmpty()) {
			for (Order order : orders) {
				transmitOrder(order.m_orderId);
			}
		} else {
			System.out.println("NO ORDERS TO BE TRANSMITTED");
			System.out.println();
		}
	}

	/*
	 * Cancel an order using its orderId
	 * 
	 * NOTE: This will remove the order from the database as well. It will also
	 * remove its linked contract if the contract is not linked to any other
	 * outstanding orders.
	 */
	public static void cancelOrder(int orderId) throws SQLException {
		Contract contract = Database.getContractWithOrderId(orderId);
		Order order = Database.getOrder(orderId);

		Socket.cancelMarketData(contract.m_conId);
		
		int linkedOrdersSize = Database.findLinkedOrders(contract.m_conId)
				.size();

		connection.cancelOrder(order.m_orderId);

		// If this order is linked to a contract that is not linked to any other
		// outstanding orders, delete the contract from the database
		if (linkedOrdersSize <= 0) {
			System.out
					.println("ERROR: NO CONTRACTS ARE LINKED TO THIS ORDER BY m_conId = "
							+ contract.m_conId);
			System.out.println();
			Database.deleteOrder(order.m_orderId);

		} else if (linkedOrdersSize == 1) {
			System.out.println(Database.getTimestamp() + "CANCELLED: "
					+ contract);
			System.out.println("                    " + order);
			System.out.println();
			Database.deleteOrder(order.m_orderId);
			Database.deleteContract(contract.m_conId);
		} else {
			System.out
					.println("CONTRACT WAS NOT DELETED FROM DATABASE AS IT IS LINKED TO OTHER OUTSTANDING ORDERS");
			System.out.println();
			Database.deleteOrder(order.m_orderId);
		}
	}

	/*
	 * Cancel all orders
	 * 
	 * NOTE: This will remove all orders from the database as well. It will also
	 * remove any of said orders' linked contracts.
	 */
	public static void cancelOrders() throws SQLException {
		ArrayList<Order> orders = Database.getAllOrders();
		if (orders != null && !orders.isEmpty()) {
			for (Order order : orders) {
				cancelOrder(order.m_orderId);
			}
		} else {
			System.out.println("NO ORDERS TO BE CANCELLED");
			System.out.println();
		}
	}

	/*
	 * Request market data for a contract
	 */
	public static boolean requestMarketData(int conId) throws SQLException {
		boolean marketDataFound = true;
		Contract contract = Database.getContract(conId);
		connection.reqMktData(contract.m_conId, contract, null, true, null);

		// Ensure market data is either requested or times out so that no
		// errors are thrown
		long startTime = System.currentTimeMillis();
		while ((Database.getBid(contract.m_conId) <= 0.0
				|| Database.getAsk(contract.m_conId) <= 0.0
				|| Database.getLastPrice(contract.m_conId) <= 0.0
				|| Database.getBidSize(contract.m_conId) <= 0 || Database
				.getAskSize(contract.m_conId) <= 0)
				&& (System.currentTimeMillis() - startTime) < 5000) {
			System.out.print("");
		}

		// TWS was not able to retrieve all needed market data within the time
		// allocated
		if (Database.getBid(contract.m_conId) <= 0.0
				|| Database.getBid(contract.m_conId) <= 0.0
				|| Database.getLastPrice(contract.m_conId) <= 0.0
				|| Database.getBidSize(contract.m_conId) <= 0
				|| Database.getAskSize(contract.m_conId) <= 0) {
			// Basket.addBrokenContract(contract);
			marketDataFound = false;
		}

		if (marketDataFound) {
			System.out.println(Database.getTimestamp()
					+ "MARKET DATA RECEIVED: " + contract);
			System.out.println();
		} else {
			System.out.println(Database.getTimestamp()
					+ "MARKET DATA NOT RECEIVED: " + contract);
			System.out.println();
		}

		return marketDataFound;
	}

	/*
	 * Cancel market data for a contract
	 */
	public static void cancelMarketData(int conId) throws SQLException {
		Contract contract = Database.getContract(conId);
		System.out.println(Database.getTimestamp() + "MARKET DATA CANCELLED: "
				+ contract);
		System.out.println();
		connection.cancelMktData(conId);
	}
	
	/*
	 * Cancel market data for all contracts
	 */
	public static void cancelAllMarketData() throws SQLException {
		ArrayList<Contract> contracts = Database.getAllContracts();
		if (contracts != null && !contracts.isEmpty()) {
			for (Contract contract : contracts) {
				cancelMarketData(contract.m_conId);
			}
		}
	}

	public static void main(String[] args) {
	}

}