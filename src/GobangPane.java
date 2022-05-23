import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class GobangPane extends JPanel {
	final private GobangFrame frame;
	public boolean mouseAble = true;
	private int selfColor;
	private static final int LIMT=20;
	private final int[][] map=new int[LIMT][LIMT];
	private ImageIcon desktop;
	public boolean isVictory =false;
	public boolean isGaming=false;
	
	public GobangPane(final GobangFrame frame) {
		this.frame=frame;
		this.setBackground(Color.LIGHT_GRAY);
		for (int i=0;i<LIMT;i++)
			for (int j=0;j<LIMT;j++)
				map[i][j]=0;
		
		this.setPreferredSize(new Dimension(20*(LIMT+2),20*(LIMT+2)));
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(mouseAble && isGaming) {
					int x=(e.getX()+10)/20-1,y=(e.getY()+10)/20-1;
					if(x>=1 && x<=20 && y>=1 && y<=20 && map[x-1][y-1]==0) {
						addChess(x-1,y-1,selfColor);
						mouseAble=false;
						frame.clientThread.addChess(x,y);
						try {
							frame.clientThread.out.writeUTF("opp "+x+" "+y);
						} catch (IOException ignored) {}
						repaint();
					}
				}
			}
		});
	}
	
	public void setColor(int color) {
		selfColor=color;
	}

	public void addNetChess(int x,int y) {
		addChess(x-1,y-1,-1*selfColor);
		mouseAble=true;	
		SwingUtilities.invokeLater(this::repaint);
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(desktop.getImage(),0,0,this.getWidth(),this.getHeight(),null);
		g.translate(20,20);
		for(int i=0;i<LIMT+2;i++) {
			g.drawLine(0,i*20,(LIMT+1)*20,i*20);
			g.drawLine(i*20, 0, i*20,(LIMT+1)*20);		
		}
		for(int i=0;i<LIMT;i++)
			for(int j=0;j<LIMT;j++) {
				if(map[i][j]==1)g.setColor(Color.BLACK);
				else if(map[i][j]==-1)g.setColor(Color.WHITE);
				else continue;
				g.fillOval((i+1)*20-7,(j+1)*20-7,16,16);
			}
	}
	
	private void addChess(int xPos,int yPos,final int color) {
		map[xPos][yPos]=color;
		int left=0,right=0,leftup=0,leftdown=0,rightup=0,rightdown=0,up=0,down=0,grid=1;
			while(grid<=4)
				if(xPos+grid<LIMT && map[xPos+grid][yPos]==color) {
					right++;
					grid++;
				}
				else break;
			grid=1;
			while(grid<=4)
				if(xPos-grid>=0 && map[xPos-grid][yPos]==color) {
					left++;
					grid++;
				}
				else break;
				if (right + left >= 4) {
					SwingUtilities.invokeLater(() -> {
						if(color == selfColor) {
							frame.notify.append("You Win!"+System.getProperty("line.separator"));
							frame.addWin();
							frame.addPeerLose();
							frame.clientThread.conveyMessage("win");
						} else {
							frame.notify.append("You Lose!"+System.getProperty("line.separator"));
							frame.addLose();
							frame.addPeerWin();
						}
						frame.notify.selectAll();
					});
					setEnd();
				}
				grid=1;
				while(grid<=4)
				if(xPos+grid<LIMT && yPos+grid<LIMT && map[xPos+grid][yPos+grid]==color) {
					rightdown++;
					grid++;
				}
				else break;
				grid=1;
				while(grid<=4)
				if(xPos-grid>=0 && yPos-grid>=0 && map[xPos-grid][yPos-grid]==color) {
					leftup++;
					grid++;
				}
				else break;
				if(rightdown+leftup>=4) {
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							if(color == selfColor) {
								frame.notify.append("You Win!"+System.getProperty("line.separator"));
								frame.addWin();
								frame.addPeerLose();
								frame.clientThread.conveyMessage("win");
							} else {
								frame.notify.append("You Lose!"+System.getProperty("line.separator"));
								frame.addLose();
								frame.addPeerWin();
							}
							frame.notify.selectAll();
						}
					});
					setEnd();
				}
				grid=1;
				while(grid<=4)
				if(xPos+grid<LIMT && yPos-grid>=0 && map[xPos+grid][yPos-grid]==color) {
					rightup++;
					grid++;
				}
				else break;
				grid=1;
				while(grid<=4)
				if(xPos-grid>=0 && yPos+grid<LIMT && map[xPos-grid][yPos+grid]==color) {
					leftdown++;
					grid++;
				}
				else break;
				if(rightup + leftdown >= 4)
				{
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							if(color==selfColor) {
								frame.notify.append("You Win!"+System.getProperty("line.separator"));
								frame.addWin();
								frame.addPeerLose();
								frame.clientThread.conveyMessage("win");
							} else {
								frame.notify.append("You Lose!"+System.getProperty("line.separator"));
								frame.addLose();
								frame.addPeerWin();
							}
							frame.notify.selectAll();
						}	
					});
					setEnd();
				}
				grid=1;
				while(grid<=4)
				if(yPos+grid<LIMT && map[xPos][yPos+grid]==color) {
					down++;
					grid++;
				}
				else break;
				grid=1;
				while(grid<=4)
				if(yPos-grid>=0 && map[xPos][yPos-grid]==color) {
					up++;
					grid++;
				}
				else break;
				if(down+up>=4) {
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							if(color==selfColor) {
								frame.notify.append("You Win!"+System.getProperty("line.separator"));
								frame.addWin();
								frame.addPeerLose();
								frame.clientThread.conveyMessage("win");
							} else {
								frame.notify.append("You Lose!"+System.getProperty("line.separator"));
								frame.addLose();
								frame.addPeerWin();
							}
							frame.notify.selectAll();
						}	
					});
					setEnd();
				}
			SwingUtilities.invokeLater(this::repaint);
	}

	public void clearChess() {
		for(int i=0;i<LIMT;i++)
			for(int j=0;j<LIMT;j++)
				map[i][j]=0;
	}

	public void setEnd() {
		mouseAble=false;
		isVictory =true;
		isGaming=false;
		frame.createBtn.setEnabled(true);
		frame.addBtn.setEnabled(true);
	}
}
