import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class GobangFrame extends JFrame {
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
    public GobangFrame() {
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
        new GobangFrame();
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
