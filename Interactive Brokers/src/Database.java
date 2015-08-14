import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.joda.money.Money;

import com.ib.client.Contract;
import com.ib.client.Order;

public class Database {

	/* The connection that will be used to connect to the database */
	static Connection connection = null;

	/* Fields used to connect to mySQL database */
	static String host;
	static int port;
	static String databaseName;
	static String userName;
	static String password;
	static String databaseURL;

	/*
	 * Connect to the mySQL database
	 */
	public static boolean connect(String host, int port, String databaseName,
			String username, String password) {
		setHost(host);
		setPort(port);
		setDatabaseName(databaseName);
		setUsername(username);
		setPassword(password);
		setDatabaseURL();
		try {
			connection = DriverManager.getConnection(databaseURL, username,
					password);
			System.out.println("Successful connection to " + databaseURL);
			System.out.println();
			return !connection.isClosed();
		} catch (SQLException ex) {
			System.out.println("Unable to connect to " + databaseName
					+ " database");
			System.out.println();
			System.out.println("SQLException: " + ex.getMessage());
		}
		return false;
	}

	/*
	 * Disconnect from the mySQL database
	 */
	public static boolean disconnect() {
		try {
			connection.close();
			System.out.println("Successful disconnection from "
					+ getDatabaseURL());
			System.out.println();
			return connection.isClosed();
		} catch (SQLException ex) {
			System.out.println("Unable to disconnect from " + getDatabaseURL());
			System.out.println();
			System.out.println("SQLException: " + ex.getMessage());
		}
		return false;
	}

	/*
	 * Adds the entire transaction, that is, both the contract and order, to the
	 * basketdata database
	 */
	public static void addTransaction(String currency, String exchange,
			String primaryExch, String secType, String symbol, String action,
			double lmtPrice, String orderType, int totalQuantity, String tif,
			String faGroup, String faMethod, String faProfile, String account,
			String orderRef, boolean outsideRth) throws SQLException {
		// This is used as the foreign key in the order to link the order with
		// its appropriate contract
		int conId = addContract(currency, exchange, primaryExch, secType,
				symbol);
		addOrder(conId, action, lmtPrice, orderType, totalQuantity, tif,
				faGroup, faMethod, faProfile, account, orderRef, outsideRth);
	}

	/*
	 * Add a unique Contract to the contracts table in the basketdata database
	 * 
	 * @Return m_conId This is used as the foreign key for all corresponding
	 * orders to this contract
	 */
	public static int addContract(String currency, String exchange,
			String primaryExch, String secType, String symbol)
			throws SQLException {
		String insertQuery = "INSERT INTO `contracts` (`m_conId`, `m_currency`, `m_exchange`, `m_primaryExch`, `m_secType`, `m_symbol`, `m_bid`, `m_ask`, `m_bidSize`, `m_askSize`, `m_lastPrice`)";
		insertQuery += " VALUES(NULL, '" + currency + "', '" + exchange
				+ "', '" + primaryExch + "', '" + secType + "', '" + symbol
				+ "', NULL, NULL, NULL, NULL, NULL);";
		String m_conIdQuery = "SELECT `m_conId` FROM `contracts` WHERE `m_symbol` = '"
				+ symbol + "';";

		// Used to determine if the contract being added already exists
		int existingContractm_conId = findm_conId(symbol);

		// If the contract does not exist in the contracts table yet
		if (existingContractm_conId == -1) {
			int m_conId = -1;
			connection.createStatement().executeUpdate(insertQuery);
			ResultSet resultSet = connection.createStatement().executeQuery(
					m_conIdQuery);
			resultSet.next();
			m_conId = resultSet.getInt(1);
			System.out.print(getTimestamp() + "ADDED: ");
			printContract(m_conId);
			return m_conId;
		}
		// If the contract already exists in the contracts, use the existing
		// contract's m_conId for the corresponding order's forign key
		else {
			System.out.print(getTimestamp() + "NOT ADDED: (DUPLICATE) ");
			printContract(existingContractm_conId);
			return existingContractm_conId;
		}

	}

	/*
	 * Add a unique Order to the orders table in the basketdata database
	 */
	public static void addOrder(int conId, String action, double lmtPrice,
			String orderType, int totalQuantity, String tif, String faGroup,
			String faMethod, String faProfile, String account, String orderRef,
			boolean outsideRth) throws SQLException {
		String query = "INSERT INTO `orders` (`m_orderId`, `m_conId`, `m_action`, `m_lmtPrice`, `m_orderType`, `m_totalQuantity`, `m_tif`, `m_faGroup`, `m_faMethod`, `m_faProfile`, `m_account`, `m_orderRef`, `m_outsideRth`)";
		query += " VALUES (NULL, '" + conId + "', '" + action + "', '"
				+ lmtPrice + "', '" + orderType + "', '" + totalQuantity
				+ "', '" + tif + "', ";
		String m_orderIdQuery = "SELECT `m_orderId` FROM `orders` ORDER BY `m_orderId` DESC LIMIT 1;";

		// Used to determine if the contract being added already exists
		int existingOrderm_orderId = findm_orderId(conId, action, lmtPrice,
				orderType, totalQuantity, tif, faGroup, faMethod, faProfile,
				account, orderRef, outsideRth);
		// If the order does not exist in the order table yet
		if (existingOrderm_orderId == -1) {
			// Convert any allowed null values to all capital for the SQL
			// statement
			if (faGroup == null)
				query += "NULL, ";
			else
				query += "'" + faGroup + "', ";
			if (faMethod == null)
				query += "NULL, ";
			else
				query += "'" + faMethod + "', ";
			if (faProfile == null)
				query += "NULL, ";
			else
				query += "'" + faProfile + "', ";

			query += "'" + account + "', '" + orderRef + "', '";

			// outsideRth has to be converted to binary for the SQL statement
			if (outsideRth)
				query += "1');";
			else
				query += "0');";

			int m_orderId = 0;

			connection.createStatement().executeUpdate(query);
			ResultSet resultSet = connection.createStatement().executeQuery(
					m_orderIdQuery);
			resultSet.next();
			m_orderId = resultSet.getInt(1);
			System.out.print(getTimestamp() + "ADDED: ");
			printOrder(conId);
		}
		// If the contract already exists in the contracts, use the existing
		// contract's m_conId for the corresponding order's forign key
		else {
			System.out.print(getTimestamp() + "NOT ADDED: (DUPLICATE) ");
			printOrder(existingOrderm_orderId);
		}
	}

	/*
	 * Update the Bid, Ask, and/or Last Price of the appropriate contract. The
	 * conId is being used to find the appropriate contract as it is the value
	 * that used to request such market data.
	 */
	public static void updateBidAskLastPrice(int conId, int field, double price)
			throws SQLException {
		String query = "";
		if (field == 1) {
			query = "UPDATE `contracts` SET `m_bid` = " + price
					+ " WHERE `m_conId` = " + conId + ";";
		} else if (field == 2) {
			query = "UPDATE `contracts` SET `m_ask` = " + price
					+ " WHERE `m_conId` = " + conId + ";";
		} else if (field == 4) {
			query = "UPDATE `contracts` SET `m_lastPrice` = " + price
					+ " WHERE `m_conId` = " + conId + ";";
		}
		connection.createStatement().executeUpdate(query);
		System.out.print(getTimestamp() + "UPDATED: ");
		printContract(conId);
	}

	/*
	 * Update the Bid and/or Ask sizes. The conId is being used to find the
	 * appropriate contract as it is the value that is used to request such
	 * market data.
	 */
	public static void updateBidAskSizes(int conId, int field, int size)
			throws SQLException {
		String query = "";
		if (field == 0) {
			query = "UPDATE `contracts` SET `m_bidSize` = " + size
					+ " WHERE `m_conId` = " + conId + ";";
		} else if (field == 3) {
			query = "UPDATE `contracts` SET `m_askSize` = " + size
					+ " WHERE `m_conId` = " + conId + ";";
		}
		connection.createStatement().executeUpdate(query);
		System.out.print(getTimestamp() + "UPDATED: ");
		printContract(conId);
	}

	/*
	 * Update the order filled quantity. The orderId is being used to find the
	 * appropriate order as it the value that is used to place the order itself.
	 */
	public static void updateOrderStatus(int orderId, int remaining)
			throws SQLException {
		String query = "UPDATE `orders` SET `m_totalQuantity` = " + remaining
				+ " WHERE `m_orderId` = " + orderId + ";";
		connection.createStatement().executeUpdate(query);
		System.out.print(getTimestamp() + "UPDATED: ");
		printOrder(orderId);
	}

	/*
	 * Delete a contract from the basketdata database.
	 * 
	 * NOTE: This will also delete any associated orders to this contract.
	 */
	public static void deleteContract(int conId) throws SQLException {
		// This query will return the orderId's of the orders to be deleted.
		// This is needed for the console output upon deletion.
		String orderMsg = "";
		String toBeDeletedQuery = "SELECT `m_orderId` FROM `orders` WHERE `m_conId` = "
				+ conId + ";";
		ResultSet resultSet = connection.createStatement().executeQuery(
				toBeDeletedQuery);
		int counter = 0;

		while (resultSet.next()) {
			if (counter != 0)
				orderMsg += "                  ";
			orderMsg += orderPrintMsg(resultSet.getInt("m_orderId"));
			counter++;
		}
		// If there are no orders to be deleted and the resultSet was empty
		if (orderMsg.isEmpty()) {
			orderMsg += "no order(s) with m_conId = " + conId + "!";
		}

		// Delete orders linked to this contract first
		String contractMsg = contractPrintMsg(conId);
		String orderDeletionQuery = "DELETE FROM `orders` WHERE `m_conId` = "
				+ conId + ";";
		connection.createStatement().executeUpdate(orderDeletionQuery);
		System.out.print(getTimestamp() + "DELETED: " + contractMsg);

		// Now delete the intended contract
		String contractDeletionQuery = "DELETE FROM `contracts` WHERE `m_conId` = "
				+ conId + ";";
		connection.createStatement().executeUpdate(contractDeletionQuery);
		System.out.print(getTimestamp() + "DELETED: " + orderMsg);
	}

	/*
	 * Delete all contracts from the basketdata database
	 * 
	 * NOTE: This will also delete all orders as well.
	 */
	public static void deleteAllContracts() throws SQLException {
		deleteAllOrders();
		String query = "DELETE FROM `contracts`";
		connection.createStatement().executeUpdate(query);
		System.out.println(getTimestamp() + "DELETED ALL CONTRACTS");
	}

	/*
	 * Delete all orders from the basketdata database
	 */
	public static void deleteAllOrders() throws SQLException {
		String query = "DELETE FROM `orders`;";
		connection.createStatement().executeUpdate(query);
		System.out.println(getTimestamp() + "DELETED ALL ORDERS");
	}

	/*
	 * Delete an order from the basketdata database
	 */
	public static void deleteOrder(int orderId) throws SQLException {
		String msg = orderPrintMsg(orderId);
		String query = "DELETE FROM `orders` WHERE `m_orderId` = " + orderId
				+ ";";
		connection.createStatement().executeUpdate(query);
		System.out.print(getTimestamp() + "DELETED: " + msg + "\n");
	}

	/*
	 * @Return m_conid if the contract already exists in the contracts table
	 * 
	 * @Return -1 if the contract does not exist
	 */
	public static int findm_conId(String symbol) throws SQLException {
		String query = "SELECT `m_conId` FROM `contracts` WHERE `m_symbol` = '"
				+ symbol + "';";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.next())
			return resultSet.getInt(1);
		return -1;
	}

	/*
	 * @Return m_orderId if the order already exists in the orders table
	 * 
	 * @Return -1 if the order does not exist
	 */
	public static int findm_orderId(int conId, String action, double lmtPrice,
			String orderType, int totalQuantity, String tif, String faGroup,
			String faMethod, String faProfile, String account, String orderRef,
			boolean outsideRth) throws SQLException {
		String query = "SELECT `m_orderId` FROM `orders` WHERE `m_conId` = "
				+ conId + " AND `m_action` = '" + action
				+ "' AND `m_lmtPrice` = " + lmtPrice + " AND `m_orderType` = '"
				+ orderType + "' AND `m_totalQuantity` = " + totalQuantity
				+ " AND `m_tif` = '" + tif + "' AND `m_faGroup` ";
		// Convert any allowed null values to their appropriate SQL comparison
		if (faGroup == null)
			query += "IS NULL AND `m_faMethod` ";
		else
			query += " = '" + faGroup + "' AND `m_faMethod` ";
		if (faMethod == null)
			query += "IS NULL AND `m_faProfile` ";
		else
			query += " = '" + faMethod + "' AND `m_faProfile` ";
		if (faProfile == null)
			query += "IS NULL AND `m_account` = '";
		else
			query += " = '" + faProfile + "' AND `m_account` = '";
		query += account + "' AND `m_orderRef` = '" + orderRef
				+ "' AND `m_outsideRth` = ";
		// outsideRth has to be converted to binary for the SQL statement
		if (outsideRth)
			query += "1;";
		else
			query += "0;";

		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.next())
			return resultSet.getInt(1);
		return -1;
	}

	/*
	 * Console outputs
	 */
	public static void printContract(int conId) throws SQLException {
		String query = "SELECT * FROM `contracts` WHERE `m_conId` = " + conId
				+ ";";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		// contract was found with this conId
		if (resultSet.next()) {
			System.out.println("contract[m_symbol="
					+ resultSet.getString("m_symbol") + "; m_conId="
					+ resultSet.getInt("m_conId") + "; m_currency="
					+ resultSet.getString("m_currency") + "; m_exchange="
					+ resultSet.getString("m_exchange") + "; m_primaryExch="
					+ resultSet.getString("m_primaryExch") + "; m_secType="
					+ resultSet.getString("m_secType") + "; m_lastPrice="
					+ resultSet.getDouble("m_lastPrice") + "; m_bid="
					+ resultSet.getDouble("m_bid") + "; m_ask="
					+ resultSet.getDouble("m_ask") + "; m_bidSize="
					+ resultSet.getInt("m_bidSize") + "; m_askSize="
					+ resultSet.getInt("m_askSize") + "]");
		}
		// no contract was found with this conId
		else
			System.out.println("no contract found with m_conId = " + conId
					+ "!");
	}

	public static String contractPrintMsg(int conId) throws SQLException {
		String msg = "";
		String query = "SELECT * FROM `contracts` WHERE `m_conId` = " + conId
				+ ";";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		// contract was found with this conId
		if (resultSet.next()) {
			msg = "contract[m_symbol=" + resultSet.getString("m_symbol")
					+ "; m_conId=" + resultSet.getInt("m_conId")
					+ "; m_currency=" + resultSet.getString("m_currency")
					+ "; m_exchange=" + resultSet.getString("m_exchange")
					+ "; m_primaryExch=" + resultSet.getString("m_primaryExch")
					+ "; m_secType=" + resultSet.getString("m_secType")
					+ "; m_lastPrice=" + resultSet.getDouble("m_lastPrice")
					+ "; m_bid=" + resultSet.getDouble("m_bid") + "; m_ask="
					+ resultSet.getDouble("m_ask") + "; m_bidSize="
					+ resultSet.getInt("m_bidSize") + "; m_askSize="
					+ resultSet.getInt("m_askSize") + "]";
		}
		// no contract was found with this conId
		else
			msg = "no contract found with m_conId = " + conId + "!";
		return msg + "\n";
	}

	public static void printOrder(int orderId) throws SQLException {
		String query = "SELECT * FROM `orders` WHERE `m_orderId` = " + orderId
				+ ";";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		// order was found with this orderId
		if (resultSet.next()) {
			System.out.println("order[m_orderId="
					+ resultSet.getInt("m_orderId") + "; m_conId="
					+ resultSet.getInt("m_conId") + "; m_action="
					+ resultSet.getString("m_action") + "; m_lmtPrice="
					+ resultSet.getDouble("m_lmtPrice") + "; m_orderType="
					+ resultSet.getString("m_orderType") + "; m_totalQuantity="
					+ resultSet.getInt("m_totalQuantity") + "; m_tif="
					+ resultSet.getString("m_tif") + "; m_faGroup="
					+ resultSet.getString("m_faGroup") + "; m_faMethod="
					+ resultSet.getString("m_faMethod") + "; m_faProfile="
					+ resultSet.getString("m_faProfile") + "; m_account="
					+ resultSet.getString("m_account") + "; m_orderRef="
					+ resultSet.getString("m_orderRef") + "; m_outsideRth="
					+ resultSet.getString("m_outsideRth") + "]");
		}
		// no order was found with this orderId
		else
			System.out.println("no order found with m_orderId = " + orderId
					+ "!");
		System.out.println();
	}

	public static String orderPrintMsg(int orderId) throws SQLException {
		String msg = "";
		String query = "SELECT * FROM `orders` WHERE `m_orderId` = " + orderId
				+ ";";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		// order was found with this orderId
		if (resultSet.next()) {
			msg = "order[m_orderId=" + resultSet.getInt("m_orderId")
					+ "; m_conId=" + resultSet.getInt("m_conId")
					+ "; m_action=" + resultSet.getString("m_action")
					+ "; m_lmtPrice=" + resultSet.getDouble("m_lmtPrice")
					+ "; m_orderType=" + resultSet.getString("m_orderType")
					+ "; m_totalQuantity="
					+ resultSet.getInt("m_totalQuantity") + "; m_tif="
					+ resultSet.getString("m_tif") + "; m_faGroup="
					+ resultSet.getString("m_faGroup") + "; m_faMethod="
					+ resultSet.getString("m_faMethod") + "; m_faProfile="
					+ resultSet.getString("m_faProfile") + "; m_account="
					+ resultSet.getString("m_account") + "; m_orderRef="
					+ resultSet.getString("m_orderRef") + "; m_outsideRth="
					+ resultSet.getString("m_outsideRth") + "]";
		}
		// no order was found with this orderId
		else
			msg = "no order found with m_orderId = " + orderId + "!";
		return msg + "\n";
	}

	/*
	 * Getters
	 */
	public static Connection getConnection() {
		return connection;
	}

	public static String getHost() {
		return host;
	}

	public static int getPort() {
		return port;
	}

	public static String getDatabaseName() {
		return databaseName;
	}

	public static String getUserName() {
		return userName;
	}

	public static String getPassword() {
		return password;
	}

	public static String getDatabaseURL() {
		return databaseURL;
	}

	public static ResultSet getContractResultSet(int conId) throws SQLException {
		String query = "SELECT * FROM `contracts` WHERE `m_conId` = " + conId
				+ ";";
		// If there is a non-empty ResultSet
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.isBeforeFirst())
			return resultSet;
		System.out.println("No contract result set found with m_conId = "
				+ conId);
		System.out.println();
		return null;
	}

	public static ResultSet getOrderResultSet(int orderId) throws SQLException {
		String query = "SELECT * FROM `orders` WHERE `m_orderId` = " + orderId
				+ ";";
		// If there is a non-empty ResultSet
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.isBeforeFirst())
			return resultSet;
		System.out.println("No order result set found with m_orderId = "
				+ orderId);
		System.out.println();
		return null;
	}

	public static String getTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static Contract getContract(int conId) throws SQLException {
		Contract contract = new Contract();
		ResultSet resultSet = getContractResultSet(conId);
		if (resultSet != null) {
			resultSet.next();
			contract.m_conId = resultSet.getInt("m_conId");
			contract.m_currency = resultSet.getString("m_currency");
			contract.m_exchange = resultSet.getString("m_exchange");
			contract.m_primaryExch = resultSet.getString("m_primaryExch");
			contract.m_secType = resultSet.getString("m_secType");
			contract.m_symbol = resultSet.getString("m_symbol");
			contract.m_bid = Money.parse(contract.m_currency + " "
					+ resultSet.getDouble("m_bid"));
			contract.m_ask = Money.parse(contract.m_currency + " "
					+ resultSet.getDouble("m_ask"));
			contract.m_lastPrice = Money.parse(contract.m_currency + " "
					+ resultSet.getDouble("m_lastPrice"));
			contract.m_bidSize = resultSet.getInt("m_bidSize");
			contract.m_askSize = resultSet.getInt("m_askSize");
			return contract;
		}
		System.out
				.println("Null contract returned because no result set found with m_conId = "
						+ conId);
		System.out.println();
		return null;
	}

	public static Order getOrder(int orderId) throws SQLException {
		Order order = new Order();
		ResultSet resultSet = getOrderResultSet(orderId);
		if (resultSet != null) {
			resultSet.next();
			order.m_orderId = resultSet.getInt("m_orderId");
			order.m_conId = resultSet.getInt("m_conId");
			order.m_action = resultSet.getString("m_action");
			ResultSet currencyResultSet = getContractResultSet(order.m_conId);
			currencyResultSet.next();
			order.m_lmtPrice = Money.parse(currencyResultSet
					.getString("m_currency")
					+ " "
					+ resultSet.getDouble("m_lmtPrice"));
			order.m_orderType = resultSet.getString("m_orderType");
			order.m_totalQuantity = resultSet.getInt("m_totalQuantity");
			order.m_tif = resultSet.getString("m_tif");
			order.m_faGroup = resultSet.getString("m_faGroup");
			order.m_faMethod = resultSet.getString("m_faMethod");
			order.m_faProfile = resultSet.getString("m_faProfile");
			order.m_account = resultSet.getString("m_account");
			order.m_orderRef = resultSet.getString("m_orderRef");
			order.m_outsideRth = resultSet.getBoolean("m_outsideRth");
			return order;
		}
		System.out
				.println("Null order returned because no result set found with m_orderId = "
						+ orderId);
		System.out.println();
		return null;
	}

	public static ArrayList<Contract> getAllContracts() throws SQLException {
		ArrayList<Contract> contracts = new ArrayList<Contract>();
		String query = "SELECT `m_conId` FROM `contracts`;";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.isBeforeFirst()) {
			while (resultSet.next()) {
				contracts.add(getContract(resultSet.getInt("m_conId")));
			}
			return contracts;
		}
		System.out.println("No contracts in the basketdata database");
		System.out.println();
		return null;
	}

	public static ArrayList<Order> getAllOrders() throws SQLException {
		ArrayList<Order> orders = new ArrayList<Order>();
		;
		String query = "SELECT `m_orderId` FROM `orders`;";
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		if (resultSet.isBeforeFirst()) {
			while (resultSet.next()) {
				orders.add(getOrder(resultSet.getInt("m_orderId")));
			}
			return orders;
		}
		System.out.println("No orders in the basketdata database");
		System.out.println();
		return null;
	}

	/*
	 * Setters
	 */
	public static void setConnection(Connection connection) {
		Database.connection = connection;
	}

	public static void setHost(String host) {
		Database.host = host;
	}

	public static void setPort(int port) {
		Database.port = port;
	}

	public static void setDatabaseName(String databaseName) {
		Database.databaseName = databaseName;
	}

	public static void setUsername(String userName) {
		Database.userName = userName;
	}

	public static void setPassword(String password) {
		Database.password = password;
	}

	/*
	 * Set databaseURL from necessary fields
	 */
	public static void setDatabaseURL() {
		Database.databaseURL = "jdbc:mysql://" + getHost() + ":" + getPort()
				+ "/" + getDatabaseName();
	}

	public static void main(String[] args) throws SQLException {
		Database.connect("localhost", 3307, "basketdata", "root", "");
		// Database.addTransaction("USD", "SMART", "ARCA", "STK", "C", "SELL",
		// 10.04, "LMT", 50, "GTC", null, null, null, "account#", "Basket",
		// true);
		// Database.updateOrderStatus(31, 5120);
		// Database.deleteOrder(28);
		// Database.deleteContract(46);
		/*
		 * ArrayList<Contract> contracts = Database.getAllContracts(); for
		 * (Contract c : contracts) System.out.println(c);
		 */
		/*
		 * ArrayList<Order> orders = Database.getAllOrders(); for (Order o :
		 * orders) System.out.println(o);
		 */
		Database.deleteAllContracts();

	}
}
