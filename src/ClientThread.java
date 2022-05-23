import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

class ClientThread implements Runnable {
	private final GobangFrame frame;
	private final Socket server;
	public DataInputStream in;
	public DataOutputStream out;
	private LoginFrame logFrame;
	private boolean exit=false;
	public static String NEW_LINE=System.getProperty("line.separator");
	public ClientThread(GobangFrame fivePointFrame, Socket server) {
		this.frame=fivePointFrame;
		this.server=server;
		try {
			in=new DataInputStream(server.getInputStream());
			out=new DataOutputStream(server.getOutputStream());
		} catch (IOException ignored) {}
	}
	
	public void addChess(int x,int y) {
		try {
			out.writeUTF("opp "+x+" "+y);
		} catch (IOException ignored) {}
	}
	
	public void dealMessage(String str) {
		String[] message=str.split(" ");
		if(str.startsWith("chat")) {
			frame.notify.append(str.substring(5)+NEW_LINE);
			frame.notify.selectAll();		
		} else if(str.startsWith("opp")) {
			if(!frame.fivePointPane.isVictory)
				frame.fivePointPane.addNetChess(Integer.parseInt(message[1]),Integer.parseInt(message[2]));	
		} else if(str.startsWith("update")) {
			str=str.substring(8,str.length()-1);
			message=str.split(",");
			frame.user.setListData(message);
		} else if(str.startsWith("notify")) {
			frame.notify.append(str.substring(8)+NEW_LINE);
			frame.notify.selectAll();
		} else if(str.startsWith("start")) {
			frame.fivePointPane.clearChess();
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					frame.fivePointPane.repaint();		
				}
			});
			frame.notify.append("Your game  has already started"+NEW_LINE);
			frame.notify.selectAll();		
			frame.addBtn.setEnabled(false);
			frame.cancelBtn.setEnabled(false);
			frame.createBtn.setEnabled(false);
			frame.fivePointPane.setColor(-1);
			frame.fivePointPane.mouseAble=false;
			frame.fivePointPane.isGaming=true;
			frame.fivePointPane.isVictory =false;
		} else if(str.startsWith("exit")) {
			if(!frame.fivePointPane.isVictory && frame.fivePointPane.isGaming) {
				frame.notify.append("Your opponent leaves the game"+NEW_LINE);
				if(frame.fivePointPane.isGaming) {
					frame.addPeerEscape();
					frame.addWin();
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							frame.notify.append("You Win!"+NEW_LINE);
								frame.notify.selectAll();
						}
					});
				}
				frame.fivePointPane.setEnd();
			} else {
				 frame.notify.append(str.substring(5)+"离开了房间"+NEW_LINE);
				 frame.notify.selectAll();
				 frame.startBtn.setEnabled(false);
				 frame.addBtn.setEnabled(true);
				 frame.createBtn.setEnabled(true);
			}
			frame.setPeerInfo("未知", "0", "0", "0");
		} else if(str.startsWith("add")) {
			str=str.substring(4);
			message=str.split(" ");
			frame.notify.append(message[0]+" 进入了房间"+NEW_LINE);
			frame.setPeerInfo(message[0],message[1],message[2],message[3]);
			frame.notify.selectAll();
			frame.startBtn.setEnabled(true);
			frame.cancelBtn.setEnabled(true);
		} else if(str.startsWith("peer")) {
			str=str.substring(5);
			message=str.split(" ");
			frame.setPeerInfo(message[0],message[1],message[2],message[3]);
		} else if(str.startsWith("client")) {
			str=str.substring(8,str.length()-1);
			String[] clients=str.split(",");	
			frame.chatPane.clients.removeAllItems();
			for (String client : clients) frame.chatPane.clients.addItem(client.trim());
			frame.chatPane.clients.addItem("all");
		} else if(str.startsWith("remove")) {
			frame.chatPane.clients.removeItem(str.substring(7));
		} else if(str.startsWith("retry")) {
			message=str.split(" ");
			if(message[1].equals("repeat"))
				JOptionPane.showMessageDialog(null, "请勿重复登录!");
			else 
				JOptionPane.showMessageDialog(null,"用户名或密码错误!");
			
		} else if(str.startsWith("log")) {
			frame.setVisible(true);	
			message=str.substring(4).split(" ");
			frame.setTitle("欢迎 "+message[0]+" 加入游戏"+"————五子棋  by 洪福兴");
			frame.setInfo(message[0],message[1],message[2],message[3]);
			logFrame.close();
		} else if(str.startsWith("repeat registe")) {
			JOptionPane.showMessageDialog(null, "此ID已被注册","换个ID试下吧",JOptionPane.OK_OPTION);
		} else if(str.startsWith("refuse")) {
			JOptionPane.showMessageDialog(null, "此主机已被加入，请选择其他主机加入");
		} else if(str.startsWith("refuse")) {
			JOptionPane.showMessageDialog(null, "此主机已被加入");
		} else if(str.startsWith("notrefuse")) {
			frame.addBtn.setEnabled(false);
			frame.createBtn.setEnabled(false);
			
		} else if(str.startsWith("cancel")) {
			final boolean flag;
			message=str.split(" " );	
			final String name=message[1];
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {				
						frame.notify.append(name+" 取消了即将开始的游戏"+NEW_LINE);
						frame.addBtn.setEnabled(true);
						frame.cancelBtn.setEnabled(false);
						frame.createBtn.setEnabled(true);
						frame.startBtn.setEnabled(false);
		
					frame.notify.selectAll();
				}
			});
		}
	}
	
	public void conveyMessage(String str) {
		try {
			out.writeUTF(str);
		} catch (IOException ignored) {}
		
	}

	public void setExit(boolean b) {
		exit=b;	
		frame.close();
	}
	
	public void run() {
		logFrame=new LoginFrame(this);
		while(!exit) {
			try {
				String message=in.readUTF();
				dealMessage(message);	
			} catch (IOException ignored) {}
		}
		try {
			in.close();
			out.close();
			server.close();
		} catch (IOException ignored) {}
	}
}
