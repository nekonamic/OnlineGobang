import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.Serializable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


class ServerThread extends JFrame {

    private final Map<ResponseThread, ResponseThread> map = new HashMap<>();
    private final ArrayList<ResponseThread> clients = new ArrayList<>();
    private final ExecutorService app = Executors.newCachedThreadPool();
    public ArrayList<ResponseThread> host = new ArrayList<>();
    private ServerSocket server;
    private ServerSocket verifySocket;
    private Map<String, Data> IDPass = new HashMap<>();
    private int count = 0;
    public ServerThread() throws IOException {
        super("Server");
        ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream("data.dat"));
        try {
            IDPass = (HashMap<String, Data>) objectIn.readObject();
            objectIn.close();
        } catch (ClassNotFoundException ignored) {
        }
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                try {
                    ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream("data.dat"));
                    objectOut.writeObject(IDPass);
                    objectOut.close();
                } catch (IOException ignored) {
                }
                app.shutdownNow();
                System.exit(0);
            }
        });
        JTextArea text = new JTextArea();
        text.setText("" + InetAddress.getLocalHost());
        add(text);
        pack();
        ServerSocket server = new ServerSocket(2048);
        int limit = 10;
        while (count < limit) {
            Socket client = server.accept();
            ResponseThread clientThread = new ResponseThread(client, this);
            app.execute(clientThread);
        }
    }

    public static void main(String[] args) throws IOException {
        new ServerThread();
    }

    public ResponseThread nameToThread(String name) {
        for (ResponseThread client : clients)
            if (client.toString().equals(name))
                return client;
        return null;
    }

    public void dealWithMessage(String str, ResponseThread thread) throws IOException {
        String[] message = str.split(" ");
        if (str.startsWith("chat")) {
            String s = "chat " + thread + " " + str.substring(str.indexOf(":"));
            if (message[1].equals("all")) {
                for (ResponseThread client : clients) client.out.writeUTF(s);

            } else {
                nameToThread(message[1]).out.writeUTF(s);
            }
        } else if (str.startsWith("create")) {
            if (host.contains(thread)) return;
            host.add(thread);
            for (ResponseThread client : clients) client.out.writeUTF("update " + host.toString());
        } else if (str.startsWith("add")) {
            int addedIndex = Integer.parseInt(message[1]);
            if (addedIndex < 0 || addedIndex >= host.size()) {
                thread.out.writeUTF("refuse");
                return;
            }
            ResponseThread addedHost = host.get(addedIndex);
            if (map.containsKey(addedHost)) {
                thread.out.writeUTF("refuse");
                return;
            }
            thread.out.writeUTF("notrefuse");
            host.remove(addedHost);
            synchronized (map) {
                map.put(addedHost, thread);
                map.put(thread, addedHost);
                for (ResponseThread client : clients) client.out.writeUTF("update " + host.toString());
                addedHost.out.writeUTF("add " + thread + " " + IDPass.get(thread.toString()));//
                thread.out.writeUTF("peer " + addedHost + " " + IDPass.get(addedHost.toString()));
            }
        } else if (str.startsWith("exit")) {
            if (thread.isGaming()) {
                synchronized (map) {
                    map.get(thread).setGame(false);
                    IDPass.get(thread.toString()).addEscape();
                    IDPass.get(map.get(thread).toString()).addWin();
                    map.get(thread).setGame(false);
                }
            }
            clients.remove(thread);
            host.remove(thread);
            for (ResponseThread client : clients) {
                client.out.writeUTF("update " + host.toString());
                client.out.writeUTF("remove " + thread);
            }
            synchronized (map) {
                if (map.containsKey(thread)) map.get(thread).out.writeUTF("exit " + thread);
                map.remove(map.get(thread));
                map.remove(thread);
            }
            thread.close();
        } else if (str.startsWith("start")) {
            synchronized (map) {
                map.get(thread).out.writeUTF(str);
                host.remove(thread);
                thread.setGame(true);
                map.get(thread).setGame(true);
            }
            for (ResponseThread client : clients) client.out.writeUTF("update " + host.toString());
        } else if (str.startsWith("opp")) {
            synchronized (map) {
                map.get(thread).out.writeUTF(str);
            }
        } else if (str.startsWith("win")) {
            IDPass.get(thread.toString()).addWin();
            synchronized (map) {
                map.get(thread).setGame(false);
                thread.setGame(false);
                IDPass.get(map.get(thread).toString()).addLose();
                map.remove(map.get(thread));
                map.remove(thread);
            }

        } else if (str.startsWith("log")) {
            message = str.substring(4).split(" ");
            if (IDPass.containsKey(message[0]) && IDPass.get(message[0]).getPassword().equals(message[1]) && !isUserIn(message[0])) {
                thread.setName(message[0]);
                clients.add(thread);
                for (int i = 0; i < clients.size(); i++) {
                    clients.get(i).out.writeUTF("client " + clients);
                }
                count++;

                thread.out.writeUTF("log " + message[0] + " " + IDPass.get(message[0]));
            } else {
                if (IDPass.containsKey(message[0]) && IDPass.get(message[0]).getPassword().equals(message[1])) {
                    thread.out.writeUTF("retry repeat");
                    return;
                }
                thread.out.writeUTF("retry error");
            }
        } else if (str.startsWith("error to log")) {
            thread.close();
        } else if (str.startsWith("registe")) {
            message = str.substring(8).split(" ");
            if (!IDPass.containsKey(message[0]))
                IDPass.put(message[0], new Data(message[1], 0, 0, 0));
            else
                thread.out.writeUTF("repeat registe");
        } else if (str.startsWith("cancel")) {
            if (map.containsKey(thread)) {
                map.get(thread).out.writeUTF("cancel " + thread);
                map.remove(map.get(thread));
                map.remove(thread);
            }
            host.remove(thread);
            for (ResponseThread client : clients) client.out.writeUTF("update " + host.toString());
        }
    }

    public boolean isUserIn(String str) {
        for (ResponseThread client : clients)
            if (client.toString().equals(str))
                return true;
        return false;
    }
}

class Data implements Serializable {
    private final String password;
    private int win;
    private int lose;
    private int escape;

    public Data(String password, int win, int lose, int escape) {
        this.password = password;
        this.win = win;
        this.lose = lose;
        this.escape = escape;
    }

    public String getPassword() {
        return password;

    }

    public void addWin() {
        win++;
    }

    public void addLose() {
        lose++;
    }

    public void addEscape() {
        escape++;
    }

    public String toString() {
        return win + " " + lose + " " + escape;
    }
}

class ResponseThread implements Runnable {
    Socket client;
    DataInputStream in;
    DataOutputStream out;
    ServerThread agent;
    boolean exit = false;
    private String name;
    private boolean isGaming = false;

    public ResponseThread(Socket client, ServerThread agent) {
        this.client = client;
        this.agent = agent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() != getClass()) return false;
        ResponseThread t = (ResponseThread) o;
        return t.in.equals(in) && t.out.equals(out) && agent == t.agent;
    }

    public void close() {
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException ignored) {
        }
        exit = true;
    }

    public void setGame(boolean b) {
        isGaming = b;
    }

    public boolean isGaming() {
        return isGaming;
    }

    public void run() {
        try {
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            try {
                String mess = in.readUTF();
                agent.dealWithMessage(mess, this);
            } catch (IOException ignored) {
            }
        } catch (IOException ignored) {
        }
        try {
            if (!exit) out.writeUTF("update " + agent.host);
        } catch (IOException ignored) {
        }
        while (!exit) {
            try {
                String mess = in.readUTF();
                agent.dealWithMessage(mess, this);
            } catch (IOException ignored) {
            }
        }
    }
}
