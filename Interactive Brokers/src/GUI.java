import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class GUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws SQLException
	 */
	public GUI() throws SQLException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
 
		ConnectionPanel connectionPanel = new ConnectionPanel();
		tabbedPane.addTab("Connection", null, connectionPanel, null);

		BasketOrderPanel basketOrderPanel = new BasketOrderPanel();
		tabbedPane.addTab("Basket Order", null, basketOrderPanel, null);
		
		tabbedPane.setSelectedComponent(basketOrderPanel);
		
		connectionPanel.btnConnect.doClick();
		
		System.out.println("CANCELING ANY REMAINING OPEN MARKET DATA CONNECTIONS");
		System.out.println();
		Socket.cancelAllMarketData();
		
		System.out.println("CLEARING ALL PREVIOUS CONTRACTS AND ORDERS");
		Database.deleteAllContracts();

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out
						.println("CANCELING ANY OPEN MARKET DATA CONNECTIONS");
				System.out.println();
				try {
					Socket.cancelAllMarketData();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});

	}

}
