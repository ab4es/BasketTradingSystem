import java.util.ArrayList;

import com.ib.client.Contract;
import com.ib.client.Order;

public class Strategy {

	public static void runSpreadStrategy(boolean isCancelAndCorrect) {
		Basket.clearBrokenContracts();
		// Cancel and correction
		if (isCancelAndCorrect) {
			Basket.clearFilledOrders();
			// If Orders remain after clearing the filled Orders
			if (!Basket.getOrders().isEmpty()) {
				System.out.println("Orders being corrected...");
				System.out.println();
				
				// Loop through all Orders
				for (int i = 0; i < Basket.getOrders().size(); i++) {
					// Request market data
					boolean hasData = Socket.requestMarketData(Basket
							.getOrders().get(i), Basket.getContracts().get(i));
					// If market data can obtained
					if (hasData) {
						Strategy.cancelAndCorrectSpreadStrategy(Basket
								.getOrders().get(i),
								Basket.getContracts().get(i));
						Socket.cancelMarketData(Basket.getOrders().get(i));
					} 
					// If market data cannot be obtained
					else {
						Socket.cancelMarketData(Basket.getOrders().get(i));
						Basket.getContracts().remove(i);
						Basket.getOrders().remove(i);
					}
				}
			} else {
				System.out.println("No orders to be corrected");
				System.out.println();
			}
		}

		// Normal order transmission
		else {
			// Loop through all Orders
			for (int i = 0; i < Basket.getOrders().size(); i++) {
				// Request market data
				boolean hasData = Socket.requestMarketData(Basket.getOrders()
						.get(i), Basket.getContracts().get(i));
				// If market data can obtained
				if (hasData) {
					Strategy.normalSpreadStrategy(Basket.getOrders().get(i),
							Basket.getContracts().get(i));
					Socket.cancelMarketData(Basket.getOrders().get(i));
				}
				// If market data cannot be obtained
				else {
					Socket.cancelMarketData(Basket.getOrders().get(i));
					Basket.getContracts().remove(i);
					Basket.getOrders().remove(i);
				}
			}
		}
		
		Socket.transmitOrders();
	}

	// Modifies Order spread to leverage the relative bid-ask spread size so
	// as to shave the spread costs
	public static void normalSpreadStrategy(Order order, Contract contract) {
		order.m_orderType = "LMT";

		// Create variable needed for the strategy
		double bid = order.m_bid;
		double ask = order.m_ask;
		double price = order.m_lastPrice;
		int bidSize = order.m_bidSize;
		int askSize = order.m_askSize;
		double spread = ask - bid;
		double lmtMid = Math.round(((bid + ask) / 2) * 100.0) / 100.0;
		double ratio = (float) bidSize / askSize;

		System.out.println(contract.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		// Normal strategy implementation
		if (spread <= 0.01) {
			order.m_orderType = "MKT";
			System.out.println("MKT");
		} else if (spread > 0.01 && spread <= 0.02) {
			if (price < 5) {
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			} else {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			}
		} else if (spread > 0.02 && spread <= 0.05) {
			if (ratio > 10 && order.m_action.equalsIgnoreCase("BUY")) {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			} else if (ratio < 0.1 && order.m_action.equalsIgnoreCase("SELL")) {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			} else {
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			}
		} else if (spread > 0.05) {
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		} else {
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		// System output for additional console information
		System.out.println("PRICE: " + order.m_lastPrice);
		System.out.println("BID: " + order.m_bid);
		System.out.println("ASK: " + order.m_ask);
		System.out.println("MID: " + lmtMid);
		System.out.println("SPREAD: " + spread);
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio);
		System.out.println();

		// Replace or "update" the Orders with this calculated spread
		Basket.replaceOrder(order);
	}

	// Cancels the previous Order and calculates a new spread to
	// leverage the relative bid-ask spread size so as to shave the spread costs
	public static void cancelAndCorrectSpreadStrategy(Order order,
			Contract contract) {

		// Create variable needed for the strategy
		double bid = order.m_bid;
		double ask = order.m_ask;
		double price = order.m_lastPrice;
		double lmtPrice = order.m_lmtPrice;
		int bidSize = order.m_bidSize;
		int askSize = order.m_askSize;
		double spread = ask - bid;
		double lmtMid = Math.round(((bid + ask) / 2) * 100.0) / 100.0;
		double ratio = (float) bidSize / askSize;

		System.out.println(contract.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		// Cancel and correct strategy implementation
		if ((order.m_action.equals("BUY") && lmtPrice >= bid)
				|| (order.m_action.equals("SELL") && lmtPrice <= ask)) {
			if (spread <= 0.01 && price > 5) {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			} else {
				System.out.println("HOLD");
			}
		} else if (spread <= 0.01) {
			order.m_orderType = "MKT";
			System.out.println("MKT");
		} else if (spread > 0.01 && spread <= 0.02) {
			if (price < 5) {
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			} else {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			}
		} else if (spread > 0.02 && spread <= 0.05) {
			if (ratio > 10 && order.m_action.equalsIgnoreCase("BUY")) {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			} else if (ratio < 0.1 && order.m_action.equalsIgnoreCase("SELL")) {
				order.m_orderType = "MKT";
				System.out.println("MKT");
			} else {
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID");
			}
		} else if (spread > 0.05) {
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		} else {
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		// System output for additional console information
		System.out.println("PRICE: " + order.m_lastPrice);
		System.out.println("LMT PRICE: " + order.m_lmtPrice);
		System.out.println("BID: " + order.m_bid);
		System.out.println("ASK: " + order.m_ask);
		System.out.println("MID: " + lmtMid);
		System.out.println("SPREAD: " + spread);
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio);
		System.out.println();
		
		// Replace or "update" the Orders with this calculated spread
		Basket.replaceOrder(order);
	}

	public static void main(String[] args) {

	}

}
