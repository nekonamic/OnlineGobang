import javax.swing.*;

class ChatPane extends JPanel {
    final public GobangFrame frame;
    JTextField input = new JTextField(50);
    JComboBox clients;
    JButton sendBtn = new JButton("Send Message");

    public ChatPane(final GobangFrame frame) {
        this.frame = frame;
        clients = new JComboBox();
        input.addActionListener(e -> {
            if (!input.getText().equals("")) {

                frame.clientThread.conveyMessage("chat " + clients.getSelectedItem() + " :" + input.getText());
                input.setText("");
            } else JOptionPane.showMessageDialog(null, "Message can't be empty");
        });
        sendBtn.addActionListener(e -> {
            if (!input.getText().equals("")) {
                frame.clientThread.conveyMessage("chat " + clients.getSelectedItem() + " :" + input.getText());
                input.setText("");
            } else JOptionPane.showMessageDialog(null, "Message can't be empty");
        });

        this.add(clients);
        this.add(input);
        this.add(sendBtn);
    }


}
