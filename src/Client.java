import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientFrame extends JFrame {
    public String NEW_LINE = System.getProperty("line.separator");
    public JTextArea notify;
    public GobangPane fivePointPane;
    public JList user = new JList();
    public ClientThread clientThread;
    public ChatPane chatPane;
    JTextArea info = new JTextArea(0, 10);
    JTextArea peerInfo = new JTextArea(0, 10);
    JButton createBtn = new JButton("Create");
    JButton startBtn = new JButton("Start");
    JButton addBtn = new JButton("Join");
    JButton exitBtn = new JButton("Exit");
    JButton cancelBtn = new JButton("Cancel Game");
    ExecutorService threadRunner = Executors.newFixedThreadPool(1);
    private String name;
    private String peerName;
    private int win;
    private int peerWin;
    private int lose;
    private int peerLose;
    private int escape;
    private int peerEscape;
    public ClientFrame() {
        chatPane = new ChatPane(this);
        this.add(chatPane, BorderLayout.SOUTH);
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(0, 1));
        info.setBorder(BorderFactory.createTitledBorder("My Info"));
        peerInfo.setBorder(BorderFactory.createTitledBorder("Opponent Info"));
        info.setEditable(false);
        peerInfo.setEditable(false);

        this.add(pane, BorderLayout.WEST);
        fivePointPane = new GobangPane(this);
        this.add(fivePointPane);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                clientThread.conveyMessage("exit");
                clientThread.setExit(true);
                threadRunner.shutdownNow();
                System.exit(0);
            }
        });
        notify = new JTextArea();
        notify.setLineWrap(true);
        notify.setEditable(false);

        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        user.setBorder(BorderFactory.createTitledBorder("Host"));
        notify.setBorder(BorderFactory.createTitledBorder("Game Info"));
        panel.add(new JScrollPane(user, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        panel.add(new JScrollPane(notify, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        JPanel btnPane = new JPanel();

        btnPane.setLayout(new GridLayout(2, 2));
        btnPane.add(createBtn);
        btnPane.add(addBtn);
        btnPane.add(cancelBtn);
        btnPane.add(exitBtn);

        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new BorderLayout());
        buttonsPane.add(btnPane);
        buttonsPane.add(startBtn, BorderLayout.EAST);
        panel.add(buttonsPane);
        startBtn.setEnabled(false);
        //panel.add(startBtn);
        cancelBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> {
            try {
                clientThread.out.writeUTF("cancel");
                createBtn.setEnabled(true);
                addBtn.setEnabled(true);
                startBtn.setEnabled(false);
                cancelBtn.setEnabled(false);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        createBtn.addActionListener(arg0 -> {
            clientThread.conveyMessage("create");
            notify.append("Created Successfully" + System.getProperty("line.separator"));
            createBtn.setEnabled(false);
            startBtn.setEnabled(true);
            addBtn.setEnabled(false);
            cancelBtn.setEnabled(true);
        });

        startBtn.addActionListener(arg0 -> {
            clientThread.conveyMessage("start");
            fivePointPane.clearChess();
            fivePointPane.setColor(1);
            startBtn.setEnabled(false);
            addBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            createBtn.setEnabled(false);
            fivePointPane.isVictory = false;
            fivePointPane.mouseAble = true;
            fivePointPane.isGaming = true;
            SwingUtilities.invokeLater(() -> {
                notify.append("You already started a game" + NEW_LINE);
                notify.selectAll();
                repaint();
            });
        });
        addBtn.addActionListener(arg0 -> {
            if (!user.isSelectionEmpty() && !user.getSelectedValue().equals(name)) {
                notify.append("Joined Successfully" + user.getSelectedValue() + System.getProperty("line.separator"));
                SwingUtilities.invokeLater(() -> {
                    addBtn.setEnabled(false);
                    createBtn.setEnabled(false);
                    cancelBtn.setEnabled(true);
                });
                clientThread.conveyMessage("add " + user.getSelectedIndex());
            } else
                SwingUtilities.invokeLater(() -> notify.append("Choose a server that not own!" + System.getProperty("line.separator")));
        });

        exitBtn.addActionListener(arg0 -> {
            clientThread.conveyMessage("exit");
            clientThread.setExit(true);
            threadRunner.shutdownNow();
            System.exit(0);
        });
        JLabel l = new JLabel();
        this.setSize(843, 650);
        BufferedImage icon = new BufferedImage(843, 120, BufferedImage.TYPE_INT_RGB);
        ImageIcon image = new ImageIcon(icon);
        l.setIcon(image);
        this.add(l, BorderLayout.NORTH);
        this.add(panel, BorderLayout.EAST);
        this.setLocation(150, 40);
        this.setResizable(false);

        try {
            InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) {
        }
        try {
            InetAddress serverIP = InetAddress.getByName("localhost");
            int SERVER_PORT = 2048;
            Socket server = new Socket(serverIP, SERVER_PORT);
            clientThread = new ClientThread(this, server);
            threadRunner.execute(clientThread);
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        new ClientFrame();
    }

    public void addPeerEscape() {
        this.peerEscape++;
        flushPeerInfo();
    }

    public void addWin() {
        this.win++;
        flushInfo();
    }

    public void addPeerWin() {
        this.peerWin++;
        flushPeerInfo();
    }

    public void addLose() {
        this.lose++;
        flushInfo();
    }

    public void addPeerLose() {
        this.peerLose++;
        flushPeerInfo();
    }

    public void setPeerInfo(String name, String win, String lose, String escape) {
        this.peerName = name;
        this.peerWin = Integer.parseInt(win);
        this.peerLose = Integer.parseInt(lose);
        this.peerEscape = Integer.parseInt(escape);
        flushPeerInfo();
    }

    public void setInfo(String name, String win, String lose, String escape) {
        this.name = name;
        this.win = Integer.parseInt(win);
        this.lose = Integer.parseInt(lose);
        this.escape = Integer.parseInt(escape);
        flushInfo();
    }

    public void flushPeerInfo() {
        SwingUtilities.invokeLater(() -> peerInfo.setText("ID:" + peerName + NEW_LINE
                + "Win:" + peerWin + NEW_LINE
                + "Lose:" + peerLose + NEW_LINE
                + "Escape:" + peerEscape + NEW_LINE
                + "Level:" + (3 * peerWin - 2 * peerLose - 5 * peerEscape) / 10
        ));
    }

    public void flushInfo() {
        SwingUtilities.invokeLater(() -> info.setText("ID:" + name + NEW_LINE
                + "Win:" + win + NEW_LINE
                + "Lose:" + lose + NEW_LINE
                + "Escape:" + escape + NEW_LINE
                + "Level:" + (3 * win - 2 * lose - 5 * escape) / 10
        ));
    }

    public void close() {
        System.exit(0);
    }
}

class ClientThread implements Runnable {
    public static String NEW_LINE = System.getProperty("line.separator");
    private final ClientFrame frame;
    private final Socket server;
    public DataInputStream in;
    public DataOutputStream out;
    private LoginFrame logFrame;
    private boolean exit = false;

    public ClientThread(ClientFrame fivePointFrame, Socket server) {
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

class ChatPane extends JPanel {
    final public ClientFrame frame;
    JTextField input = new JTextField(50);
    JComboBox clients;
    JButton sendBtn = new JButton("Send Message");

    public ChatPane(final ClientFrame frame) {
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

class LoginFrame extends JFrame {
    private final JTextField input = new JTextField(8);
    private final JPasswordField ps = new JPasswordField(8);
    private final DataOutputStream out;

    public LoginFrame(final ClientThread clientThread) {
        super("Gobang");
        this.setResizable(false);
        out = clientThread.out;
        JPanel inPane = new JPanel();
        inPane.add(new JLabel("ID"));
        inPane.add(input);
        JPanel psPane = new JPanel();
        psPane.add(new JLabel("Password"));
        psPane.add(ps);
        JPanel btnP = new JPanel();
        JButton logBtn = new JButton("Login");
        btnP.add(logBtn);
        JButton registerBtn = new JButton("Register");
        btnP.add(registerBtn);
        JButton exitBtn = new JButton("Exit");
        btnP.add(exitBtn);
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1));
        p.add(inPane);
        p.add(psPane);
        p.add(btnP);
        add(p);
        logBtn.addActionListener(arg0 -> {
            try {
                out.writeUTF("log " + input.getText() + " " + Arrays.toString(ps.getPassword()));
                input.setText("");
                ps.setText("");
            } catch (IOException ignored) {
            }
        });
        registerBtn.addActionListener(arg0 -> {
            try {
                out.writeUTF("registe " + input.getText() + " " + Arrays.toString(ps.getPassword()));
                input.setText("");
                ps.setText("");
            } catch (IOException ignored) {
            }
        });
        exitBtn.addActionListener(arg0 -> clientThread.setExit(true));
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    out.writeUTF("log ** **");
                    System.exit(0);
                } catch (IOException ignored) {
                }
            }
        });
    }

    public void close() {
        this.setVisible(false);
    }
}

class GobangPane extends JPanel {
    private static final int LIMT = 20;
    final private ClientFrame frame;
    private final int[][] map = new int[LIMT][LIMT];
    public boolean mouseAble = true;
    public boolean isVictory = false;
    public boolean isGaming = false;
    private int selfColor;
    private ImageIcon desktop;

    public GobangPane(final ClientFrame frame) {
        this.frame = frame;
        this.setBackground(Color.LIGHT_GRAY);
        for (int i = 0; i < LIMT; i++)
            for (int j = 0; j < LIMT; j++)
                map[i][j] = 0;

        this.setPreferredSize(new Dimension(20 * (LIMT + 2), 20 * (LIMT + 2)));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (mouseAble && isGaming) {
                    int x = (e.getX() + 10) / 20 - 1, y = (e.getY() + 10) / 20 - 1;
                    if (x >= 1 && x <= 20 && y >= 1 && y <= 20 && map[x - 1][y - 1] == 0) {
                        addChess(x - 1, y - 1, selfColor);
                        mouseAble = false;
                        frame.clientThread.addChess(x, y);
                        try {
                            frame.clientThread.out.writeUTF("opp " + x + " " + y);
                        } catch (IOException ignored) {
                        }
                        repaint();
                    }
                }
            }
        });
    }

    public void setColor(int color) {
        selfColor = color;
    }

    public void addNetChess(int x, int y) {
        addChess(x - 1, y - 1, -1 * selfColor);
        mouseAble = true;
        SwingUtilities.invokeLater(this::repaint);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.translate(20, 20);
        for (int i = 0; i < LIMT + 2; i++) {
            g.drawLine(0, i * 20, (LIMT + 1) * 20, i * 20);
            g.drawLine(i * 20, 0, i * 20, (LIMT + 1) * 20);
        }
        for (int i = 0; i < LIMT; i++)
            for (int j = 0; j < LIMT; j++) {
                if (map[i][j] == 1) g.setColor(Color.BLACK);
                else if (map[i][j] == -1) g.setColor(Color.WHITE);
                else continue;
                g.fillOval((i + 1) * 20 - 7, (j + 1) * 20 - 7, 16, 16);
            }
    }

    private void addChess(int xPos, int yPos, final int color) {
        map[xPos][yPos] = color;
        int left = 0, right = 0, leftup = 0, leftdown = 0, rightup = 0, rightdown = 0, up = 0, down = 0, grid = 1;
        while (grid <= 4)
            if (xPos + grid < LIMT && map[xPos + grid][yPos] == color) {
                right++;
                grid++;
            } else break;
        grid = 1;
        while (grid <= 4)
            if (xPos - grid >= 0 && map[xPos - grid][yPos] == color) {
                left++;
                grid++;
            } else break;
        if (right + left >= 4) {
            SwingUtilities.invokeLater(() -> {
                if (color == selfColor) {
                    frame.notify.append("You Win!" + System.getProperty("line.separator"));
                    frame.addWin();
                    frame.addPeerLose();
                    frame.clientThread.conveyMessage("win");
                } else {
                    frame.notify.append("You Lose!" + System.getProperty("line.separator"));
                    frame.addLose();
                    frame.addPeerWin();
                }
                frame.notify.selectAll();
            });
            setEnd();
        }
        grid = 1;
        while (grid <= 4)
            if (xPos + grid < LIMT && yPos + grid < LIMT && map[xPos + grid][yPos + grid] == color) {
                rightdown++;
                grid++;
            } else break;
        grid = 1;
        while (grid <= 4)
            if (xPos - grid >= 0 && yPos - grid >= 0 && map[xPos - grid][yPos - grid] == color) {
                leftup++;
                grid++;
            } else break;
        if (rightdown + leftup >= 4) {
            SwingUtilities.invokeLater(() -> {
                if (color == selfColor) {
                    frame.notify.append("You Win!" + System.getProperty("line.separator"));
                    frame.addWin();
                    frame.addPeerLose();
                    frame.clientThread.conveyMessage("win");
                } else {
                    frame.notify.append("You Lose!" + System.getProperty("line.separator"));
                    frame.addLose();
                    frame.addPeerWin();
                }
                frame.notify.selectAll();
            });
            setEnd();
        }
        grid = 1;
        while (grid <= 4)
            if (xPos + grid < LIMT && yPos - grid >= 0 && map[xPos + grid][yPos - grid] == color) {
                rightup++;
                grid++;
            } else break;
        grid = 1;
        while (grid <= 4)
            if (xPos - grid >= 0 && yPos + grid < LIMT && map[xPos - grid][yPos + grid] == color) {
                leftdown++;
                grid++;
            } else break;
        if (rightup + leftdown >= 4) {
            SwingUtilities.invokeLater(() -> {
                if (color == selfColor) {
                    frame.notify.append("You Win!" + System.getProperty("line.separator"));
                    frame.addWin();
                    frame.addPeerLose();
                    frame.clientThread.conveyMessage("win");
                } else {
                    frame.notify.append("You Lose!" + System.getProperty("line.separator"));
                    frame.addLose();
                    frame.addPeerWin();
                }
                frame.notify.selectAll();
            });
            setEnd();
        }
        grid = 1;
        while (grid <= 4)
            if (yPos + grid < LIMT && map[xPos][yPos + grid] == color) {
                down++;
                grid++;
            } else break;
        grid = 1;
        while (grid <= 4)
            if (yPos - grid >= 0 && map[xPos][yPos - grid] == color) {
                up++;
                grid++;
            } else break;
        if (down + up >= 4) {
            SwingUtilities.invokeLater(() -> {
                if (color == selfColor) {
                    frame.notify.append("You Win!" + System.getProperty("line.separator"));
                    frame.addWin();
                    frame.addPeerLose();
                    frame.clientThread.conveyMessage("win");
                } else {
                    frame.notify.append("You Lose!" + System.getProperty("line.separator"));
                    frame.addLose();
                    frame.addPeerWin();
                }
                frame.notify.selectAll();
            });
            setEnd();
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    public void clearChess() {
        for (int i = 0; i < LIMT; i++)
            for (int j = 0; j < LIMT; j++)
                map[i][j] = 0;
    }

    public void setEnd() {
        mouseAble = false;
        isVictory = true;
        isGaming = false;
        frame.createBtn.setEnabled(true);
        frame.addBtn.setEnabled(true);
    }
}
