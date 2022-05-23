import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;

class LoginFrame extends JFrame {
	private final JTextField input=new JTextField(8);
	private final JPasswordField ps=new JPasswordField(8);
	private final DataOutputStream out;
	public LoginFrame(final ClientThread clientThread) {
		super("Gobang");
		this.setResizable(false);
		out=clientThread.out;
		JPanel inPane=new JPanel();
		inPane.add(new JLabel("ID"));
		inPane.add(input);
		JPanel psPane=new JPanel();
		psPane.add(new JLabel("Password"));
		psPane.add(ps);
		JPanel btnP=new JPanel();
		JButton logBtn = new JButton("Login");
		btnP.add(logBtn);
		JButton registerBtn = new JButton("Register");
		btnP.add(registerBtn);
		JButton exitBtn = new JButton("Exit");
		btnP.add(exitBtn);
		JPanel p=new JPanel();
		p.setLayout(new GridLayout(0,1));
		p.add(inPane);
		p.add(psPane);
		p.add(btnP);
		add(p);
		logBtn.addActionListener(arg0 -> {
			try {
				out.writeUTF("log "+input.getText()+" "+Arrays.toString(ps.getPassword()));
				input.setText("");
				ps.setText("");
			} catch (IOException ignored) {}
		});
		registerBtn.addActionListener(arg0 -> {
			try {
				out.writeUTF("registe "+input.getText()+" "+Arrays.toString(ps.getPassword()));
				input.setText("");
				ps.setText("");
			} catch (IOException ignored) {}
		});
		exitBtn.addActionListener(arg0 -> clientThread.setExit(true));
		pack();
		setVisible(true);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				try {
					out.writeUTF("log ** **");
					System.exit(0);
				} catch (IOException ignored) {}
			}
		});
	}
	public void close() {
		this.setVisible(false);
	}
}
