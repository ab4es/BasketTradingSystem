import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ib.client.Contract;
import com.ib.client.Order;

public class Strategy {

	public static void runSpreadStrategy(boolean isCancelAndCorrect)
			throws SQLException {
		ArrayList<Order> orders = Database.getAllOrders();
		if (orders != null && !orders.isEmpty()) {
			for (Order order : orders) {
				// Request market data
				boolean dataReceived = Socket.requestMarketData(order.m_conId);
				if (dataReceived) {
					Contract contract = Database.getContract(order.m_conId);
					// Cancel and correct
					if (isCancelAndCorrect) {
						System.out
								.println("Checking to see if correcting is needed");
						System.out.println();
						// order must be cancelled and corrected
						if (needsNewSpread(order, contract)) {
							System.out.println("CORRECTING...");
							System.out.println();
							cancelAndCorrectSpreadStrategy(order, contract);
							Socket.transmitOrder(order.m_orderId);
						}
					}
					// Normal transmission
					else {
						Strategy.normalSpreadStrategy(order, contract);
						Socket.transmitOrder(order.m_orderId);
					}
				} else {
					Socket.cancelMarketData(order.m_conId);
					System.out
							.println(Database.getTimestamp()
									+ "NOT TRANSMITTED: market data could not be received for");
					System.out.println("                          "
							+ Database.getContract(order.m_conId));
					System.out.println("                          " + order);
					System.out.println();
				}
			}
		} else {
			System.out.println("NO ORDERS TO BE TRANSMITTED");
			System.out.println();
		}

	}

	// Modifies the contract bid/ask to leverage the relative bid-ask spread
	// size so
	// as to shave the system's transaction costs using the market data
	public static void normalSpreadStrategy(Order order, Contract contract)
			throws SQLException {
		Database.setOrderType(order.m_orderId, "LMT");
		order.m_orderType = "LMT";

		// Create variable needed for the strategy
		BigDecimal bid = new BigDecimal(contract.m_bid.toString());
		BigDecimal ask = new BigDecimal(contract.m_ask.toString());
		BigDecimal price = new BigDecimal(contract.m_lastPrice.toString());
		int bidSize = contract.m_bidSize;
		int askSize = contract.m_askSize;
		BigDecimal spread = ask.subtract(bid);
		int scale = 2;
		if (price.toString().substring(0, 1).equals("0")) {
			scale = 4;
		}
		BigDecimal lmtMid = (bid.add(ask)).divide(new BigDecimal(2)).setScale(
				scale, RoundingMode.HALF_UP);
		BigDecimal ratio = BigDecimal.valueOf(bidSize).divide(
				BigDecimal.valueOf(askSize), 2, RoundingMode.HALF_UP);

		System.out.println(contract.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		// Normal strategy implementation
		if (spread.compareTo(new BigDecimal("0.01")) <= 0) {
			String outerMsg = "spread <= 0.01";
			Database.setOrderType(order.m_orderId, "MKT");
			order.m_orderType = "MKT";
			System.out.println("MKT[" + outerMsg + "]");
		} else if ((spread.compareTo(new BigDecimal("0.01")) > 0)
				&& (spread.compareTo(new BigDecimal("0.02")) <= 0)) {
			String outerMsg = "0.01 < spread <= 0.02";
			if (price.compareTo(new BigDecimal("5")) < 0) {
				Database.setLmtPrice(order.m_orderId, lmtMid);
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID[" + outerMsg + " & price < 5]");
			} else {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg + " & price >= 5]");
			}
		} else if ((spread.compareTo(new BigDecimal("0.02")) > 0)
				&& (spread.compareTo(new BigDecimal("0.05")) <= 0)) {
			String outerMsg = "0.02 < spread <= 0.05";
			if ((ratio.compareTo(new BigDecimal("10")) > 0)
					&& order.m_action.equalsIgnoreCase("BUY")) {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio > 10 & BUY order]");
			} else if ((ratio.compareTo(new BigDecimal("0.1")) < 0)
					&& order.m_action.equalsIgnoreCase("SELL")) {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio < 10 & SELL order]");
			} else {
				Database.setLmtPrice(order.m_orderId, lmtMid);
				order.m_lmtPrice = lmtMid;
				System.out
						.println("LMT MID["
								+ outerMsg
								+ " & NOT(ratio > 10 & BUY order) & NOT(ratio < 10 & SELL order)]");
			}
		} else if (spread.compareTo(new BigDecimal("0.05")) > 0) {
			String outerMsg = "spread > 0.05";
			Database.setLmtPrice(order.m_orderId, lmtMid);
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID[" + outerMsg + "]");
		} else {
			Database.setLmtPrice(order.m_orderId, lmtMid);
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		// System output for additional console information
		if (order.m_orderType.equals("MKT")) {
			System.out.println("TRANSMIT PRICE: (MKT) " + price.toString());
		} else {
			System.out.println("TRANSMIT PRICE: (LMT) " + order.m_lmtPrice);
		}
		System.out.println("LAST PRICE: " + price.toString());
		System.out.println("BID: " + bid.toString());
		System.out.println("ASK: " + ask.toString());
		System.out.println("MID: " + lmtMid.toString());
		System.out.println("SPREAD: " + spread.toString());
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio.toString());
		System.out.println();
	}

	// Cancels the previous Order and calculates a new spread to
	// leverage the relative bid-ask spread size so as to shave the spread costs
	public static void cancelAndCorrectSpreadStrategy(Order order,
			Contract contract) throws SQLException {
		// Create variable needed for the strategy
		BigDecimal bid = new BigDecimal(contract.m_bid.toString());
		BigDecimal ask = new BigDecimal(contract.m_ask.toString());
		BigDecimal price = new BigDecimal(contract.m_lastPrice.toString());
		int bidSize = contract.m_bidSize;
		int askSize = contract.m_askSize;
		BigDecimal spread = ask.subtract(bid);
		int scale = 2;
		if (price.toString().substring(0, 1).equals("0")) {
			scale = 4;
		}
		BigDecimal lmtMid = (bid.add(ask)).divide(new BigDecimal(2)).setScale(
				scale, RoundingMode.HALF_UP);
		BigDecimal ratio = BigDecimal.valueOf(bidSize).divide(
				BigDecimal.valueOf(askSize), 2, RoundingMode.HALF_UP);

		System.out.println(contract.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		// Cancel and correct strategy implementation
		if (order.m_orderType.equals("LMT")) {
			if ((order.m_action.equals("BUY") && order.m_lmtPrice
					.compareTo(bid) >= 0)
					|| (order.m_action.equals("SELL") && order.m_lmtPrice
							.compareTo(ask) <= 0)) {
				String outerMsg = "";
				if (order.m_action.equals("BUY"))
					outerMsg += "BUY & lmtPrice >= bid";
				else
					outerMsg += "SELL & lmtPrice <= ask";
				if ((spread.compareTo(new BigDecimal("0.01")) <= 0)
						&& price.compareTo(new BigDecimal("5")) > 0) {
					Database.setOrderType(order.m_orderId, "MKT");
					order.m_orderType = "MKT";
					System.out.println("MKT[" + outerMsg
							+ " & spread <= 0.01 & price > 5]");
				} else {
					System.out.println("HOLD[" + outerMsg
							+ " & (spread > 0.01 OR price <= 5)]");
				}
			}
		} else if (spread.compareTo(new BigDecimal("0.01")) <= 0) {
			String outerMsg = "spread <= 0.01";
			Database.setOrderType(order.m_orderId, "MKT");
			order.m_orderType = "MKT";
			System.out.println("MKT[" + outerMsg + "]");
		} else if ((spread.compareTo(new BigDecimal("0.01")) > 0)
				&& (spread.compareTo(new BigDecimal("0.02")) <= 0)) {
			String outerMsg = "0.01 < spread <= 0.02";
			if (price.compareTo(new BigDecimal("5")) < 0) {
				Database.setLmtPrice(order.m_orderId, lmtMid);
				order.m_lmtPrice = lmtMid;
				System.out.println("LMT MID[" + outerMsg + " & price < 5]");
			} else {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg + " & price <= 5]");
			}
		} else if ((spread.compareTo(new BigDecimal("0.02")) > 0)
				&& (spread.compareTo(new BigDecimal("0.05")) <= 0)) {
			String outerMsg = "0.02 < spread <= 0.05";
			if ((ratio.compareTo(new BigDecimal("10")) > 0)
					&& order.m_action.equalsIgnoreCase("BUY")) {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio > 10 & BUY order]");
			} else if ((ratio.compareTo(new BigDecimal("0.1")) < 0)
					&& order.m_action.equalsIgnoreCase("SELL")) {
				Database.setOrderType(order.m_orderId, "MKT");
				order.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio < 10 & SELL order]");
			} else {
				Database.setLmtPrice(order.m_orderId, lmtMid);
				order.m_lmtPrice = lmtMid;
				System.out
						.println("LMT MID["
								+ outerMsg
								+ " & NOT(ratio > 10 & BUY order) & NOT(ratio < 10 & SELL order)]");
			}
		} else if (spread.compareTo(new BigDecimal("0.05")) > 0) {
			String outerMsg = "spread > 0.05";
			Database.setLmtPrice(order.m_orderId, lmtMid);
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID[" + outerMsg + "]");
		} else {
			Database.setLmtPrice(order.m_orderId, lmtMid);
			order.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		// System output for additional console information
		if (order.m_orderType.equals("MKT")) {
			System.out.println("TRANSMIT PRICE: (MKT) " + price.toString());
		} else {
			System.out.println("TRANSMIT PRICE: (LMT) " + order.m_lmtPrice);
		}
		System.out.println("LAST PRICE: " + price.toString());
		System.out.println("BID: " + bid.toString());
		System.out.println("ASK: " + ask.toString());
		System.out.println("MID: " + lmtMid.toString());
		System.out.println("SPREAD: " + spread.toString());
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio.toString());
		System.out.println();
	}

	// Used to check if an order should be left as is when the cancel and
	// correct method is run
	public static boolean needsNewSpread(Order order, Contract contract) {
		Order tmpOrder = order;
		Contract tmpContract = contract;
		
		System.out.println("org" + order);
		System.out.println();
		
		// Create variable needed for the strategy
		BigDecimal bid = new BigDecimal(tmpContract.m_bid.toString());
		BigDecimal ask = new BigDecimal(tmpContract.m_ask.toString());
		BigDecimal price = new BigDecimal(tmpContract.m_lastPrice.toString());
		int bidSize = tmpContract.m_bidSize;
		int askSize = tmpContract.m_askSize;
		BigDecimal spread = ask.subtract(bid);
		int scale = 2;
		if (price.toString().substring(0, 1).equals("0")) {
			scale = 4;
		}
		BigDecimal lmtMid = (bid.add(ask)).divide(new BigDecimal(2)).setScale(
				scale, RoundingMode.HALF_UP);
		BigDecimal ratio = BigDecimal.valueOf(bidSize).divide(
				BigDecimal.valueOf(askSize), 2, RoundingMode.HALF_UP);

		System.out.println(tmpContract.m_symbol);
		System.out.println("====");
		System.out.print("STRATEGY: ");

		// Cancel and correct strategy implementation
		if (tmpOrder.m_orderType.equals("LMT")) {
			if ((tmpOrder.m_action.equals("BUY") && tmpOrder.m_lmtPrice
					.compareTo(bid) >= 0)
					|| (tmpOrder.m_action.equals("SELL") && tmpOrder.m_lmtPrice
							.compareTo(ask) <= 0)) {
				String outerMsg = "";
				if (tmpOrder.m_action.equals("BUY"))
					outerMsg += "BUY & lmtPrice >= bid";
				else
					outerMsg += "SELL & lmtPrice <= ask";
				if ((spread.compareTo(new BigDecimal("0.01")) <= 0)
						&& price.compareTo(new BigDecimal("5")) > 0) {
					tmpOrder.m_orderType = "MKT";
					System.out.println("MKT[" + outerMsg
							+ " & spread <= 0.01 & price > 5]");
				} else {
					System.out.println("HOLD[" + outerMsg
							+ " & (spread > 0.01 OR price <= 5)]");
				}
			}
		} else if (spread.compareTo(new BigDecimal("0.01")) <= 0) {
			String outerMsg = "spread <= 0.01";
			tmpOrder.m_orderType = "MKT";
			System.out.println("MKT[" + outerMsg + "]");
		} else if ((spread.compareTo(new BigDecimal("0.01")) > 0)
				&& (spread.compareTo(new BigDecimal("0.02")) <= 0)) {
			String outerMsg = "0.01 < spread <= 0.02";
			if (price.compareTo(new BigDecimal("5")) < 0) {
				tmpOrder.m_lmtPrice = lmtMid;
				System.out.println("LMT MID[" + outerMsg + " & price < 5]");
			} else {
				tmpOrder.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg + " & price <= 5]");
			}
		} else if ((spread.compareTo(new BigDecimal("0.02")) > 0)
				&& (spread.compareTo(new BigDecimal("0.05")) <= 0)) {
			String outerMsg = "0.02 < spread <= 0.05";
			if ((ratio.compareTo(new BigDecimal("10")) > 0)
					&& tmpOrder.m_action.equalsIgnoreCase("BUY")) {
				tmpOrder.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio > 10 & BUY tmpOrder]");
			} else if ((ratio.compareTo(new BigDecimal("0.1")) < 0)
					&& tmpOrder.m_action.equalsIgnoreCase("SELL")) {
				tmpOrder.m_orderType = "MKT";
				System.out.println("MKT[" + outerMsg
						+ " & ratio < 10 & SELL tmpOrder]");
			} else {
				tmpOrder.m_lmtPrice = lmtMid;
				System.out
						.println("LMT MID["
								+ outerMsg
								+ " & NOT(ratio > 10 & BUY tmpOrder) & NOT(ratio < 10 & SELL tmpOrder)]");
			}
		} else if (spread.compareTo(new BigDecimal("0.05")) > 0) {
			String outerMsg = "spread > 0.05";
			tmpOrder.m_lmtPrice = lmtMid;
			System.out.println("LMT MID[" + outerMsg + "]");
		} else {
			tmpOrder.m_lmtPrice = lmtMid;
			System.out.println("LMT MID");
		}

		// System output for additional console information
		if (tmpOrder.m_orderType.equals("MKT")) {
			System.out.println("TRANSMIT PRICE: (MKT) " + price.toString());
		} else {
			System.out.println("TRANSMIT PRICE: (LMT) " + tmpOrder.m_lmtPrice);
		}
		System.out.println("LAST PRICE: " + price.toString());
		System.out.println("BID: " + bid.toString());
		System.out.println("ASK: " + ask.toString());
		System.out.println("MID: " + lmtMid.toString());
		System.out.println("SPREAD: " + spread.toString());
		System.out.println("BID SIZE: " + bidSize);
		System.out.println("ASK SIZE: " + askSize);
		System.out.println("RATIO: " + ratio.toString());
		System.out.println();
		
		if (order.m_orderType.equals(tmpOrder.m_orderType)) {
			if (order.m_orderType.equals("MKT"))
				return false;
			else if (order.m_orderType.equals("LMT")) {
				if (order.m_lmtPrice != tmpOrder.m_lmtPrice)
					return true;
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {

	}

}
