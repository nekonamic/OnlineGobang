import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientThread implements Runnable {
    public static String NEW_LINE = System.getProperty("line.separator");
    private final GobangFrame frame;
    private final Socket server;
    public DataInputStream in;
    public DataOutputStream out;
    private LoginFrame logFrame;
    private boolean exit = false;

    public ClientThread(GobangFrame fivePointFrame, Socket server) {
        this.frame = fivePointFrame;
        this.server = server;
        try {
            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());
        } catch (IOException ignored) {
        }
    }

    public void addChess(int x, int y) {
        try {
            out.writeUTF("opp " + x + " " + y);
        } catch (IOException ignored) {
        }
    }

    public void dealMessage(String str) {
        String[] message = str.split(" ");
        if (str.startsWith("chat")) {
            frame.notify.append(str.substring(5) + NEW_LINE);
            frame.notify.selectAll();
        } else if (str.startsWith("opp")) {
            if (!frame.fivePointPane.isVictory)
                frame.fivePointPane.addNetChess(Integer.parseInt(message[1]), Integer.parseInt(message[2]));
        } else if (str.startsWith("update")) {
            str = str.substring(8, str.length() - 1);
            message = str.split(",");
            frame.user.setListData(message);
        } else if (str.startsWith("notify")) {
            frame.notify.append(str.substring(8) + NEW_LINE);
            frame.notify.selectAll();
        } else if (str.startsWith("start")) {
            frame.fivePointPane.clearChess();
            SwingUtilities.invokeLater(() -> frame.fivePointPane.repaint());
            frame.notify.append("Your game  has already started" + NEW_LINE);
            frame.notify.selectAll();
            frame.addBtn.setEnabled(false);
            frame.cancelBtn.setEnabled(false);
            frame.createBtn.setEnabled(false);
            frame.fivePointPane.setColor(-1);
            frame.fivePointPane.mouseAble = false;
            frame.fivePointPane.isGaming = true;
            frame.fivePointPane.isVictory = false;
        } else if (str.startsWith("exit")) {
            if (!frame.fivePointPane.isVictory && frame.fivePointPane.isGaming) {
                frame.notify.append("Your opponent leaves the game" + NEW_LINE);
                if (frame.fivePointPane.isGaming) {
                    frame.addPeerEscape();
                    frame.addWin();
                    SwingUtilities.invokeLater(() -> {
                        frame.notify.append("You Win!" + NEW_LINE);
                        frame.notify.selectAll();
                    });
                }
                frame.fivePointPane.setEnd();
            } else {
                frame.notify.append(str.substring(5) + " leaves room" + NEW_LINE);
                frame.notify.selectAll();
                frame.startBtn.setEnabled(false);
                frame.addBtn.setEnabled(true);
                frame.createBtn.setEnabled(true);
            }
            frame.setPeerInfo("Unknown", "0", "0", "0");
        } else if (str.startsWith("add")) {
            str = str.substring(4);
            message = str.split(" ");
            frame.notify.append(message[0] + " Joined room" + NEW_LINE);
            frame.setPeerInfo(message[0], message[1], message[2], message[3]);
            frame.notify.selectAll();
            frame.startBtn.setEnabled(true);
            frame.cancelBtn.setEnabled(true);
        } else if (str.startsWith("peer")) {
            str = str.substring(5);
            message = str.split(" ");
            frame.setPeerInfo(message[0], message[1], message[2], message[3]);
        } else if (str.startsWith("client")) {
            str = str.substring(8, str.length() - 1);
            String[] clients = str.split(",");
            frame.chatPane.clients.removeAllItems();
            for (String client : clients) frame.chatPane.clients.addItem(client.trim());
            frame.chatPane.clients.addItem("all");
        } else if (str.startsWith("remove")) {
            frame.chatPane.clients.removeItem(str.substring(7));
        } else if (str.startsWith("retry")) {
            message = str.split(" ");
            if (message[1].equals("repeat"))
                JOptionPane.showMessageDialog(null, "Don't repeat login");
            else
                JOptionPane.showMessageDialog(null, "Wrong ID or password");

        } else if (str.startsWith("log")) {
            frame.setVisible(true);
            message = str.substring(4).split(" ");
            frame.setTitle("Welcome " + message[0]);
            frame.setInfo(message[0], message[1], message[2], message[3]);
            logFrame.close();
        } else if (str.startsWith("repeat registe")) {
            JOptionPane.showMessageDialog(null, "This ID has been used", "Message", JOptionPane.ERROR_MESSAGE);
        } else if (str.startsWith("refuse")) {
            JOptionPane.showMessageDialog(null, "This host has been joined");
        } else if (str.startsWith("notrefuse")) {
            frame.addBtn.setEnabled(false);
            frame.createBtn.setEnabled(false);

        } else if (str.startsWith("cancel")) {
            message = str.split(" ");
            final String name = message[1];
            SwingUtilities.invokeLater(() -> {
                frame.notify.append(name + " Canceled the coming up game " + NEW_LINE);
                frame.addBtn.setEnabled(true);
                frame.cancelBtn.setEnabled(false);
                frame.createBtn.setEnabled(true);
                frame.startBtn.setEnabled(false);

                frame.notify.selectAll();
            });
        }
    }

    public void conveyMessage(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException ignored) {
        }

    }

    public void setExit(boolean b) {
        exit = b;
        frame.close();
    }

    public void run() {
        logFrame = new LoginFrame(this);
        while (!exit) {
            try {
                String message = in.readUTF();
                dealMessage(message);
            } catch (IOException ignored) {
            }
        }
        try {
            in.close();
            out.close();
            server.close();
        } catch (IOException ignored) {
        }
    }
}
