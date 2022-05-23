import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
