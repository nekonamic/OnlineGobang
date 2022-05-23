import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class ChatPane extends JPanel
{
	 JTextField input=new JTextField(50);
	 JComboBox clients;
	 JButton sendBtn =new JButton("Send Message");
	 final public GobangFrame frame;
	 public ChatPane(final GobangFrame frame)
	 {
		 this.frame=frame;
		 clients=new JComboBox();	 
		 input.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(!input.getText().equals(""))
				{

					frame.clientThread.conveyMessage("chat "+(String)clients.getSelectedItem()+" :"+input.getText());
					input.setText("");
				}
				else JOptionPane.showMessageDialog(null, "Message can't be empty");
			}	 
		 });
		sendBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(!input.getText().equals(""))
				{
					frame.clientThread.conveyMessage("chat "+(String)clients.getSelectedItem()+" :"+input.getText());
					input.setText("");
				}
				else JOptionPane.showMessageDialog(null, "Message can't be empty");
				}	 
			 });
		
		 this.add(clients);
		 this.add(input);
		 this.add(sendBtn);
	 }
	
	

}
