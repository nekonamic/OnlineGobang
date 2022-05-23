import java.io.Serializable;

class Data implements Serializable {
    private final String password;
    private int win;
    private int lose;
    private int escape;

    public Data(String password, int win, int lose, int escape) {
        this.password = password;
        this.win = win;
        this.lose = lose;
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
