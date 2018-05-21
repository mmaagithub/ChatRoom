package com.qqText;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client4 {
		
	public JFrame frame;
//	public JTextArea viewArea;
	public JTextPane textPane;
	public JTextField viewField,jt_login ;
	public JButton button1,buttonLogin,buttonExit;
	public JLabel jlable;
	public JTextField MyName;
	public DefaultListModel listModel;
	public static JList userList;		//显示对象列表
	public JSplitPane centerSplit;
	public JScrollPane spFriend ;
	public static boolean isConnected = false;
	static Client4 objClient;
	static DefaultComboBoxModel model;
	static ObjectOutputStream out;
	boolean loginFlag = false;
	public Socket s =null;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
		public void run() {
			objClient = new Client4();
			objClient.createClientSocket(objClient);
			}
		});		
	}
	public void createClientSocket(Client4 client) {
		try {
			 //s = new Socket("192.168.137.1", 8848);
			s = new Socket("47.93.204.33", 8848);
			out = new ObjectOutputStream(s.getOutputStream());
			client.clientSurface();	
			new MyClientReader(s).start();																									
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class MyClientReader extends Thread {
		Socket socket;

		public MyClientReader(Socket socket) {
			this.socket = socket;
		}
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));				
				model = (DefaultComboBoxModel) userList.getModel();// 获得列表框的模型
				while(true){
					String info = in.readLine().trim();// 读取信息
					if (!info.startsWith("MSG:")) {// 接收到的不是消息
						if (info.startsWith("退出：")) {// 接收到的是退出消息
							model.removeElement(info.substring(3));// 从用户列表中移除用户
						} else {// 接收到的是登录用户			
							boolean itemFlag = false;// 标记是否为列表框添加列表项，为true不添加，为false添加
							for (int i = 0; i < model.getSize(); i++) {// 对用户列表进行遍历
								if (info.equals((String) model.getElementAt(i))) {// 如果用户列表中存在该用户名
									itemFlag = true;// 设置为true，表示不添加到用户列表
									break;// 结束for循环
								}
							}
							if (!itemFlag) {			
								model.addElement(info);// 将登录用户添加到用户列表
							}
					}
					}else{				
						DateFormat df = DateFormat.getDateInstance();// 获得DateFormat实例
						String dateString = df.format(new Date()); // 格式化为日期
						df = DateFormat.getTimeInstance(DateFormat.MEDIUM);// 获得DateFormat实例
						String timeString = df.format(new Date()); // 格式化为时间
						String id = info.substring(4,info.indexOf("：发送给："));  //发送端的id
						String message = info.substring(info.indexOf("：的信息是：")+6);
						String text = new String("  " + id + "    " + dateString+ "  " + timeString + "\n  " + message+ "\n");
				
						SimpleAttributeSet aSet = new SimpleAttributeSet();     
					    StyleConstants.setForeground(aSet, Color.red);    
					    StyleConstants.setBackground(aSet, Color.orange);    
					    StyleConstants.setFontFamily(aSet, "lucida bright italic");    
					    StyleConstants.setFontSize(aSet, 18);    					
						//StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_LEFT); 
						objClient.textPane.setParagraphAttributes(aSet,false); 	
						objClient.viewField.setText(null);// 清空文本框	
						Document docs = objClient.textPane.getDocument();//获得文本对象
					        try {
					            docs.insertString(docs.getLength(), text,aSet);//对文本进行追加
					        } catch (BadLocationException e) {
					            e.printStackTrace();
					        }
						}
					}		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage() {
		if (objClient.viewField.getText().equals("")) {
			return;// 如果没输入信息则返回，即不发送
		}
		Vector<String> v = new Vector<String>();// 创建向量对象，用于存储发送的消息
		Object[] receiveUserNames = userList.getSelectedValues();// 获得选择的用户数组
		if (receiveUserNames.length <= 0) {
			return;// 如果没选择用户则返回
		}
		for (int i = 0; i < receiveUserNames.length; i++) {
			String msg = objClient.jt_login.getText() + "：发送给："+ (String) receiveUserNames[i] + "：的信息是： "+ objClient.viewField.getText();// 定义发送的信息
			v.add(msg);// 将信息添加到向量
		}
		try {
			out.writeObject(v);// 将向量写入输出流，完成信息的发送
			out.flush();// 刷新输出缓冲区
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		DateFormat df = DateFormat.getDateInstance();// 获得DateFormat实例
		String dateString = df.format(new Date()); // 格式化为日期
		df = DateFormat.getTimeInstance(DateFormat.MEDIUM);// 获得DateFormat实例
		String timeString = df.format(new Date()); // 格式化为时间	
		String text = new String("   " + userList.getSelectedValue() + "    " + dateString+ "  " + timeString + "\n  " + viewField.getText()+ "\n");						
		
		SimpleAttributeSet aSet=new SimpleAttributeSet(); 		//接受时只改变第一次接收到的方向
		StyleConstants.setForeground(aSet, Color.blue);        
		StyleConstants.setFontFamily(aSet, "lucida bright italic");    
		StyleConstants.setFontSize(aSet, 18);   
		//StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_RIGHT); 
		System.out.println(StyleConstants.ALIGN_RIGHT);
		objClient.textPane.setParagraphAttributes(aSet,false); 
		Document docs = objClient.textPane.getDocument();//获得文本对象
		System.out.println(docs);
        try {
            docs.insertString(docs.getLength(), text,aSet);//对文本进行追加
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
	}
	
	public void clientSurface() {
		frame = new JFrame("Chat Room");
		textPane = new JTextPane();
		jlable = new JLabel();
		jlable.setText("在线");
		button1 = new JButton("Send");
		MyName = new JTextField();		
		MyName.setColumns(9);
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(8, 1));
		panel.add(jlable);
		panel.add(MyName);
		panel.add(button1);
		
		JPanel jp_input = new JPanel();
		jp_input.setBorder(new TitledBorder("发送消息")); 
		viewField = new JTextField(50);
		jp_input.add(viewField);
		
		buttonLogin = new JButton("登录");
		buttonExit = new JButton("退出");
		jt_login = new JTextField(20);
		JPanel panelLogin = new JPanel();
		panelLogin.setBorder(new TitledBorder("登录区")); 
		panelLogin.add(jt_login);
		panelLogin.add(buttonLogin);
		panelLogin.add(buttonExit);
		
		listModel = new DefaultListModel();  
		
	    userList = new JList(listModel);  
		JScrollPane sp = new JScrollPane(textPane);
		sp.setBorder(new TitledBorder("消息显示区")); 
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);//水平滚动轴
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);//垂直滚动条
		spFriend = new JScrollPane(userList);
		spFriend.setBorder(new TitledBorder("好友列表")); 
		userList.setModel(new DefaultComboBoxModel(new String[] { "" }));
		spFriend.setViewportView(userList);
		spFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);//垂直滚动条		
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spFriend, sp);  
        centerSplit.setDividerLocation(130);  
        
 		frame.add("Center", centerSplit);
		frame.add("North", panelLogin);
		frame.add("East", panel);
		frame.add("South", jp_input);
		frame.setSize(700, 400);
		frame.setLocation(200,100);
		frame.setVisible(true);
		
		// 文本框按回车键时事件  
		viewField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendMessage();  
            }  
        });  
		//发送按钮
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == button1) {
					sendMessage();
				}
			}
		});
		//单击登录按钮
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (loginFlag) {// 已登录标记为true
					JOptionPane.showMessageDialog(null, "在同一窗口只能登录一次。");
					return;
				}
				String userName = jt_login.getText().trim();// 获得登录用户名
				Vector v = new Vector();// 定义向量，用于存储登录用户
				v.add("用户：" + userName);// 添加登录用户
				try {
					out.writeObject(v);// 将用户向量发送到服务器
					out.flush();// 刷新输出缓冲区
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				jt_login.setEnabled(false);// 禁用用户文本框
				buttonLogin.setEnabled(false);
				loginFlag = true;// 将已登录标记设置为true
				MyName.setText(userName);
			}
		});
		//退出按钮
		buttonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String exitUser = jt_login.getText().trim();
				Vector v = new Vector();
				v.add("退出：" + exitUser);
				loginFlag = false;
				try {
					out.writeObject(v);
					out.flush();// 刷新输出缓冲区
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		frame.addWindowListener(new WindowAdapter(){
			   public void windowClosing(WindowEvent e) {
				   if(loginFlag){
					   frame.setDefaultCloseOperation(0);
					   JOptionPane.showMessageDialog(null, "请先退出登录", "警告",JOptionPane.ERROR_MESSAGE);
				   }else{
					   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				   }
			   }
		 });
		
//		创建托盘
		 if (SystemTray.isSupported())
	      {
	         // 获取图片所在的URL
	         URL url = Client4.class.getResource("qq.jpg");
	         // 实例化图像对象
	         ImageIcon icon = new ImageIcon(url);
	         // 获得Image对象
	         Image image = icon.getImage();
	         // 创建托盘图标
	         TrayIcon trayIcon = new TrayIcon(image);
	         // 为托盘添加鼠标适配器
	         trayIcon.addMouseListener(new MouseAdapter()
	         {
	            // 鼠标事件
	            public void mouseClicked(MouseEvent e)
	            {
	               if (e.getClickCount() == 2)
	               {
	                  JOptionPane.showMessageDialog(null, "已登陆成功");
	               }
	            }
	         });
	         // 添加工具提示文本
	         trayIcon.setToolTip("QQ:本地连接\r\n速度：100.0 Mbps\r\n状态：已连接上");
	         // 创建弹出菜单
	         PopupMenu popupMenu = new PopupMenu();
	         popupMenu.add(new MenuItem("我在线上"));
	         popupMenu.add(new MenuItem("忙碌"));
	         popupMenu.add(new MenuItem("隐身"));
	         popupMenu.add(new MenuItem("离线"));
	         popupMenu.addSeparator();
	         popupMenu.add(new MenuItem("关闭所有声音"));
	         popupMenu.add(new MenuItem("关闭头像闪动"));
	         popupMenu.addSeparator();
	         popupMenu.add(new MenuItem("退出"));

	         // 为托盘图标加弹出菜弹
	         trayIcon.setPopupMenu(popupMenu);
	         // 获得系统托盘对象
	         SystemTray systemTray = SystemTray.getSystemTray();
	         try
	         {
	            // 为系统托盘加托盘图标
	            systemTray.add(trayIcon);
	         }
	         catch (Exception e)
	         {
	            e.printStackTrace();
	         }
	      }
	      else
	      {
	         JOptionPane.showMessageDialog(null, "not support");
	      }
	}	
}
