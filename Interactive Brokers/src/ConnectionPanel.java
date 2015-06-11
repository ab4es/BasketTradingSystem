import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public class ConnectionPanel extends JPanel {

	BasketTrader basketTrader;

	JLabel lblHost;
	JLabel lblPort;
	JLabel lblClientId;
	JLabel lblConnection;

	JTextField hostField;
	JFormattedTextField portField;
	JFormattedTextField clientIdField;

	JButton btnConnect;
	JButton btnDisconnect;

	/**
	 * Create the panel.
	 */
	public ConnectionPanel(BasketTrader trader) {
		basketTrader = trader;

		GridBagLayout gbl_connectionPanel = new GridBagLayout();
		gbl_connectionPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_connectionPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_connectionPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_connectionPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		this.setLayout(gbl_connectionPanel);

		this.createHostLabel();
		this.createHostField();
		this.createConnectButton();

		this.createPortLabel();
		this.createPortField();
		this.createDisconnectButton();

		this.createClientIdLabel();
		this.createClientIdField();

		this.createConnectionLabel();

	}

	public void createHostLabel() {
		lblHost = new JLabel("Host");
		GridBagConstraints gbc_lblHost = new GridBagConstraints();
		gbc_lblHost.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblHost.insets = new Insets(0, 0, 5, 5);
		gbc_lblHost.gridx = 1;
		gbc_lblHost.gridy = 1;
		this.add(lblHost, gbc_lblHost);
	}

	public void createHostField() {
		hostField = new JTextField();
		GridBagConstraints gbc_hostField = new GridBagConstraints();
		gbc_hostField.insets = new Insets(0, 0, 5, 5);
		gbc_hostField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hostField.gridx = 2;
		gbc_hostField.gridy = 1;
		this.add(hostField, gbc_hostField);
		hostField.setColumns(10);
	}

	public void createConnectButton() {
		btnConnect = new JButton("Connect");
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.insets = new Insets(0, 0, 5, 0);
		gbc_btnConnect.gridx = 3;
		gbc_btnConnect.gridy = 1;
		this.add(btnConnect, gbc_btnConnect);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(basketTrader.connect(hostField.getText(),
						Integer.parseInt(portField.getText()),
						Integer.parseInt(clientIdField.getText())))
					lblConnection.setText("Connected");
				else 
					lblConnection.setText("Connection failed!");
			}
		});
	}

	public void createPortLabel() {
		lblPort = new JLabel("Port");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.gridx = 1;
		gbc_lblPort.gridy = 2;
		this.add(lblPort, gbc_lblPort);
	}

	public void createPortField() {
		portField = new JFormattedTextField("7496");
		GridBagConstraints gbc_portField = new GridBagConstraints();
		gbc_portField.insets = new Insets(0, 0, 5, 5);
		gbc_portField.fill = GridBagConstraints.HORIZONTAL;
		gbc_portField.gridx = 2;
		gbc_portField.gridy = 2;
		this.add(portField, gbc_portField);
	}

	public void createDisconnectButton() {
		btnDisconnect = new JButton("Disconnect");
		GridBagConstraints gbc_btnDisconnect = new GridBagConstraints();
		gbc_btnDisconnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDisconnect.insets = new Insets(0, 0, 5, 0);
		gbc_btnDisconnect.gridx = 3;
		gbc_btnDisconnect.gridy = 2;
		this.add(btnDisconnect, gbc_btnDisconnect);
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(basketTrader.disconnect())
					lblConnection.setText("Disconnected");
				else
					lblConnection.setText("Disconnection failed!");
					
			}
		});
	}

	public void createClientIdLabel() {
		lblClientId = new JLabel("Client ID");
		GridBagConstraints gbc_lblClientId = new GridBagConstraints();
		gbc_lblClientId.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblClientId.insets = new Insets(0, 0, 5, 5);
		gbc_lblClientId.gridx = 1;
		gbc_lblClientId.gridy = 3;
		this.add(lblClientId, gbc_lblClientId);
	}

	public void createClientIdField() {
		clientIdField = new JFormattedTextField("2");
		GridBagConstraints gbc_clientIdField = new GridBagConstraints();
		gbc_clientIdField.insets = new Insets(0, 0, 5, 5);
		gbc_clientIdField.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientIdField.gridx = 2;
		gbc_clientIdField.gridy = 3;
		this.add(clientIdField, gbc_clientIdField);

	}

	public void createConnectionLabel() {
		lblConnection = new JLabel("");
		GridBagConstraints gbc_lblConnection = new GridBagConstraints();
		gbc_lblConnection.insets = new Insets(0, 0, 0, 5);
		gbc_lblConnection.gridx = 2;
		gbc_lblConnection.gridy = 4;
		this.add(lblConnection, gbc_lblConnection);
	}

}
