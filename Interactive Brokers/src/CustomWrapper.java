import java.sql.SQLException;

import com.ib.client.*;

public class CustomWrapper implements EWrapper {

	public CustomWrapper() {
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Exception e) {
		e.printStackTrace();
		System.out.println();

	}

	@Override
	public void error(String str) {
		System.out.println(str);
		System.out.println();
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		System.out.println(errorCode + ": " + errorMsg);
		System.out.println();

	}

	@Override
	public void connectionClosed() {
		System.out.println("Connection closed");
		System.out.println();
	}

	@Override
	public void tickPrice(int conId, int field, double price, int canAutoExecute)
			throws SQLException {
		Database.updateBidAskLastPrice(conId, field, price);
	}

	@Override
	public void tickSize(int conId, int field, int size) throws SQLException {
		Database.updateBidAskSizes(conId, field, size);
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) throws SQLException {
		Database.updateOrderStatus(orderId, remaining);

	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextValidId(int orderId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void historicalData(int reqId, String date, double open,
			double high, double low, double close, int volume, int count,
			double WAP, boolean hasGaps) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerParameters(String xml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		// basketTrader.updatePNL(commissionReport);

	}

	@Override
	public void position(String account, Contract contract, int pos,
			double avgCost) {
		System.out.println("Account: " + account);
		System.out.println(contract.toString());
		System.out.println("Pos: " + pos);
		System.out.println("Avg Cost: " + avgCost);

	}

	@Override
	public void positionEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountSummary(int reqId, String account, String tag,
			String value, String currency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountSummaryEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void verifyMessageAPI(String apiData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		// TODO Auto-generated method stub

	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		// TODO Auto-generated method stub

	}

}
