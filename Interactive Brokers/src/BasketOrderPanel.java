import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ib.client.Contract;

public class BasketOrderPanel extends JPanel {

	BasketTrader basketTrader;

	JFileChooser fileChooser;
	File file;

	JButton btnLoadBasket;
	JButton btnTransmitBasketOrder;
	JButton btnCancelBasketOrder;
	JButton btnCancelAndCorrect;
	JButton btnUpdatePNL;

	JLabel lblLoadBasket;
	JLabel lblTransmitBasketOrder;
	JLabel lblCancelBasketOrder;
	JLabel lblCancelAndCorrect;
	JLabel lblRealizedPNL;

	/**
	 * Create the panel.
	 */
	public BasketOrderPanel(BasketTrader trader) {
		basketTrader = trader;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0,
				1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		this.createLoadBasketButton();
		this.createLoadBasketLabel();

		this.createTransmitBasketOrderButton();
		this.createTransmitBasketOrderLabel();

		this.createCancelBasketOrderButton();
		this.createCancelBasketOrderLabel();

		this.createCancelAndCorrectButton();
		this.createCancelAndCorrectLabel();

		// this.createUpdatePNLButton();
		// this.createRealizedPNLLabel();
	}

	public void createLoadBasketButton() {
		btnLoadBasket = new JButton("Load Basket Order");

		btnLoadBasket.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// fileChooser = new JFileChooser("C:\\Bbrg\\Trades");

				fileChooser = new JFileChooser(System.getProperty("user.home")
						+ "/Desktop");

				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"CSV Files", "csv");
				fileChooser.setFileFilter(filter);
				fileChooser.setDialogTitle("Select Basket Order CSV File");
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
					ArrayList<Integer> csvErrors = basketTrader.readCSV(file);
					if (csvErrors.isEmpty()) {
						lblLoadBasket.setText("Basket Order (" + file.getName()
								+ ") Loaded");
						lblTransmitBasketOrder.setText("Basket Order ("
								+ file.getName() + ") Not Transmitted");
						lblCancelBasketOrder.setText("Basket Order ("
								+ file.getName() + ") Not Cancelled");
					} else {
						final JPanel panel = new JPanel();
						String errors = "";
						for (int i : csvErrors) {
							if (i == 0)
								errors += "m_action Error! \n";
							else if (i == 3)
								errors += "m_secType Error! \n";
							else if (i == 6)
								errors += "m_tif Error! \n";
							else if (i == 7)
								errors += "m_orderType Error! \n";
							else if (i == 14)
								errors += "m_outsideRth Error! \n";
							else if (i == 17)
								errors += "Action column may be incorrect. \n";
							else if (i == 18)
								errors += "Quantity column may be incorrect. \n";
							else if (i == 19)
								errors += "Symbol column may be incorrect. \n";
							else if (i == 20)
								errors += "SecType column may be incorrect. \n";
							else if (i == 21)
								errors += "Exchange column may be incorrect. \n";
							else if (i == 22)
								errors += "Currency column may be incorrect. \n";
							else if (i == 23)
								errors += "TimeInForce column may be incorrect. \n";
							else if (i == 24)
								errors += "OrderType column may be incorrect. \n";
							else if (i == 25)
								errors += "LmtPrice column may be incorrect. \n";
							else if (i == 26)
								errors += "BasketTag column may be incorrect. \n";
							else if (i == 27)
								errors += "Account column may be incorrect. \n";
							else if (i == 28)
								errors += "Group column may be incorrect. \n";
							else if (i == 29)
								errors += "Method column may be incorrect. \n";
							else if (i == 30)
								errors += "OrderCreationTime column may be incorrect. \n";
							else if (i == 31)
								errors += "OutsideRth column may be incorrect. \n";
							else if (i == 32)
								errors += "OrderRef column may be incorrect. \n";
							else if (i == 33)
								errors += "Profile column may be incorrect. \n";
						}
						JOptionPane.showMessageDialog(panel, errors, "Error",
								JOptionPane.ERROR_MESSAGE);
					}

				} else {
					lblLoadBasket.setText("Basket Order Not Selected");
				}
			}
		});

		GridBagConstraints gbc_btnLoadBasket = new GridBagConstraints();
		gbc_btnLoadBasket.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadBasket.gridx = 1;
		gbc_btnLoadBasket.gridy = 1;
		add(btnLoadBasket, gbc_btnLoadBasket);
	}

	public void createTransmitBasketOrderButton() {
		btnTransmitBasketOrder = new JButton("Transmit Basket Order");

		btnTransmitBasketOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<Contract> brokenContracts = basketTrader
						.transmitOrders();
				if (brokenContracts.isEmpty()) {
					lblTransmitBasketOrder.setText("Basket Order ("
							+ file.getName() + ")  Transmitted");
				} else {
					final JPanel panel = new JPanel();
					String errors = "Market data could not be retrieved for the following stock(s): \n";
					for (Contract c : brokenContracts)
						errors += c.m_symbol + "\n";
					System.out.println(errors);
					JOptionPane.showMessageDialog(panel, errors, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		GridBagConstraints gbc_btnTransmitBasketOrder = new GridBagConstraints();
		gbc_btnTransmitBasketOrder.insets = new Insets(0, 0, 5, 5);
		gbc_btnTransmitBasketOrder.gridx = 3;
		gbc_btnTransmitBasketOrder.gridy = 1;
		add(btnTransmitBasketOrder, gbc_btnTransmitBasketOrder);
	}

	public void createCancelBasketOrderButton() {
		btnCancelBasketOrder = new JButton("Cancel Basket Order");

		btnCancelBasketOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				basketTrader.cancelOrders();
				lblLoadBasket.setText("No Basket Loaded");
				lblTransmitBasketOrder.setText("No Basket Order Transmitted");
				lblCancelBasketOrder.setText("Basket Order (" + file.getName()
						+ ")  Canceled");
			}
		});

		GridBagConstraints gbc_btnCancelBasketOrder = new GridBagConstraints();
		gbc_btnCancelBasketOrder.insets = new Insets(0, 0, 5, 0);
		gbc_btnCancelBasketOrder.gridx = 5;
		gbc_btnCancelBasketOrder.gridy = 1;
		add(btnCancelBasketOrder, gbc_btnCancelBasketOrder);
	}

	public void createLoadBasketLabel() {
		lblLoadBasket = new JLabel("No Basket Loaded");
		GridBagConstraints gbc_lblLoadBasket = new GridBagConstraints();
		gbc_lblLoadBasket.insets = new Insets(0, 0, 5, 5);
		gbc_lblLoadBasket.gridx = 1;
		gbc_lblLoadBasket.gridy = 2;
		add(lblLoadBasket, gbc_lblLoadBasket);
	}

	public void createTransmitBasketOrderLabel() {
		lblTransmitBasketOrder = new JLabel("No Basket Order Transmitted");
		GridBagConstraints gbc_lblTransmitBasketOrder = new GridBagConstraints();
		gbc_lblTransmitBasketOrder.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransmitBasketOrder.gridx = 3;
		gbc_lblTransmitBasketOrder.gridy = 2;
		add(lblTransmitBasketOrder, gbc_lblTransmitBasketOrder);
	}

	public void createCancelBasketOrderLabel() {
		lblCancelBasketOrder = new JLabel("No Basket Order Canceled");
		GridBagConstraints gbc_lblCancelBasketOrder = new GridBagConstraints();
		gbc_lblCancelBasketOrder.insets = new Insets(0, 0, 5, 0);
		gbc_lblCancelBasketOrder.gridx = 5;
		gbc_lblCancelBasketOrder.gridy = 2;
		add(lblCancelBasketOrder, gbc_lblCancelBasketOrder);
	}

	public void createCancelAndCorrectButton() {
		btnCancelAndCorrect = new JButton("Cancel and Correct");

		btnCancelAndCorrect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				basketTrader.cancelAndCorrectOrders();
				lblCancelAndCorrect.setText("Basket Order (" + file.getName()
						+ ")  Cancelled and Corrected");
			}
		});

		GridBagConstraints gbc_btnCancelAndCorrect = new GridBagConstraints();
		gbc_btnCancelAndCorrect.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancelAndCorrect.gridx = 3;
		gbc_btnCancelAndCorrect.gridy = 4;
		add(btnCancelAndCorrect, gbc_btnCancelAndCorrect);
	}

	public void createCancelAndCorrectLabel() {
		lblCancelAndCorrect = new JLabel("No Basket Canceled and Corrected");
		GridBagConstraints gbc_lblCancelAndCorrect = new GridBagConstraints();
		gbc_lblCancelAndCorrect.insets = new Insets(0, 0, 5, 5);
		gbc_lblCancelAndCorrect.gridx = 3;
		gbc_lblCancelAndCorrect.gridy = 5;
		add(lblCancelAndCorrect, gbc_lblCancelAndCorrect);
	}

	public void createUpdatePNLButton() {
		btnUpdatePNL = new JButton("Update P&L");

		btnUpdatePNL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				basketTrader.manualPNLUpdate();
			}
		});

		GridBagConstraints gbc_btnUpdatePNL = new GridBagConstraints();
		gbc_btnUpdatePNL.insets = new Insets(0, 0, 5, 5);
		gbc_btnUpdatePNL.gridx = 3;
		gbc_btnUpdatePNL.gridy = 7;
		add(btnUpdatePNL, gbc_btnUpdatePNL);
	}

	public void createRealizedPNLLabel() {
		lblRealizedPNL = new JLabel("Realized P&L:");
		GridBagConstraints gbc_lblRealizedPNL = new GridBagConstraints();
		gbc_lblRealizedPNL.insets = new Insets(0, 0, 0, 5);
		gbc_lblRealizedPNL.gridx = 3;
		gbc_lblRealizedPNL.gridy = 8;
		add(lblRealizedPNL, gbc_lblRealizedPNL);
	}

	public void updatePNL(double pnl) {
		lblRealizedPNL.setText("Realized P&L: " + pnl);
	}
}
