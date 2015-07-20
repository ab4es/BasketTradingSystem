import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Order;

public class Socket {
	
	// Initialize the EClientSocket using the CustomerWrapper
	public static EClientSocket connection = new EClientSocket(
			new CustomWrapper());

	// Connect to the TWS
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

	// Disconnect from the TWS
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

	// Transmit all outstanding Orders
	public static void transmitOrders() {
		for (int x = 0; x < Basket.getOrders().size(); x++) {
			connection.placeOrder(Basket.getOrders().get(x).m_orderId, Basket
					.getContracts().get(x), Basket.getOrders().get(x));
			System.out.println("Order" + (Basket.getOrders().get(x).m_orderId)
					+ " Transmitted");
		}
		System.out.println();
	}

	// Cancel an order
	public static void cancelOrder(Order order) {
		while (!connection.cancelOrder(order.m_orderId))
			System.out.println("");
		;
	}

	// Cancel all outstanding Orders
	public static void cancelOrders() {
		// Loop through all Orders and cancel each individually
		for (int i = 0; i < Basket.getOrders().size(); i++) {
			connection.cancelOrder(Basket.getOrders().get(i).m_orderId);
			System.out.println("Order" + (Basket.getOrders().get(i).m_orderId)
					+ " canceled");
		}

		// Clear all Orders, Contracts, and Broken Contracts
		Basket.clearContracts();
		Basket.clearBrokenContracts();
		Basket.clearOrders();

		System.out.println();
	}

	// Request market data for a particular Contract and Order
	public static boolean requestMarketData(Order order, Contract contract) {
		boolean marketDataFound = true;

		connection.reqMktData(order.m_orderId, contract, null, true, null);

		// Ensure market data is either requested or times out so that no
		// errors are thrown
		long startTime = System.currentTimeMillis();
		while ((order.m_bid == 0.0 || order.m_ask == 0.0
				|| order.m_lastPrice == 0.0 || order.m_bidSize == 0 || order.m_askSize == 0)
				&& (System.currentTimeMillis() - startTime) < 3000) {
			System.out.print("");
		}

		// TWS was not able to retrieve all needed market data
		if (order.m_bid == 0.0 || order.m_ask == 0.0
				|| order.m_lastPrice == 0.0 || order.m_bidSize == 0
				|| order.m_askSize == 0) {
			Basket.addBrokenContract(contract);
			marketDataFound = false;
		}

		return marketDataFound;
	}

	// Cancel market data for an Order
	public static void cancelMarketData(Order o) {
		connection.cancelMktData(o.m_orderId);
	}

	public static void main(String[] args) {
	}

}
