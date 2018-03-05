package com.qqText;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputListener;

public class Server4 extends Thread {

	static DataOutputStream dos;
	static DataInputStream dis;
	public JFrame frame;
	public JTextArea viewArea;
	public JTextField viewField;
	public JButton button1, buttonLogin, buttonExit;
	public JLabel jlable;
	public JTextField MyName;
	public static DefaultListModel listModel;
	public static JList userList; // ��ʾ�����б�
	public JSplitPane centerSplit;
	public boolean isConnected = false;
	static Server4 Server4;
	static Socket s;

	public static HashMap<String,Socket> hash = new HashMap<String,Socket>();
	
	public static void main(String[] args) throws IOException {
		Server4 = new Server4();
		Server4.serverSurface("������");
		ServerSocket ss = new ServerSocket(8848);
		while (true) {
			Server4.viewArea.append("�ȴ��¿ͻ�����......\n");
			s = ss.accept();
			Server4.viewArea.append("���ӳɹ�......" + s + "\n");
			new ServerThread(s).start();
		}
	}

	static class ServerThread extends Thread {
		Socket socket;
		public ServerThread(Socket socket) {
			this.socket = socket;
		}
		public void run() {
			String key = null;		
			try {
				ObjectInputStream ins = new ObjectInputStream(socket.getInputStream());
				while(true){
					Vector v = null;
					try {
						v = (Vector)ins.readObject();					
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if(v!=null){
						for(int i = 0;i<v.size();i++){
							String info = (String) v.get(i);							
							if(info.startsWith("�û���")){
								key = info.substring(3);
								hash.put(key,socket);
								Set<String> set = hash.keySet();// ��ü��������м���Set��ͼ
								Iterator<String> keyIt = set.iterator();// ������м��ĵ�����,Ϊÿһ��key��������key
								while(keyIt.hasNext()){
									String receiveKey = keyIt.next();
									Socket s = hash.get(receiveKey);
									PrintWriter out = new PrintWriter(s.getOutputStream(),true);
									Iterator<String> keyIt1 = set.iterator();
									while(keyIt1.hasNext()){
										String receiveKey1 = keyIt1.next();
										out.println(receiveKey1);
										out.flush();
									}									
								}
								listModel.addElement(info);
								}else if(info.startsWith("�˳���")){
									key = info.substring(3);
									hash.remove(key);
									Set<String> set = hash.keySet();
									Iterator<String> it = set.iterator();
									while(it.hasNext()){
										String recivekey = it.next();
										Socket s = hash.get(recivekey);
										PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
										out.println("�˳���"+key);
										out.flush();
									}									
								}else{
									key = info.substring(info.indexOf("�����͸���") + 5,info.indexOf("������Ϣ�ǣ�"));// ��ý��շ���keyֵ,�����շ����û���
									String sendUser = info.substring(0,info.indexOf("�����͸���"));// ��÷��ͷ���keyֵ,�����ͷ����û���
									Set<String> set = hash.keySet();
									Iterator<String> it = set.iterator();
									while(it.hasNext()){
										String receiveKey = it.next();
										if(receiveKey.equals(key) && !sendUser.equals(receiveKey)){
											Socket s = hash.get(key);
											PrintWriter out = new PrintWriter(s.getOutputStream(),true);
											out.println("MSG:"+info);	
											out.flush();										
										}
									}									
								}
							}
						}																			
					}											
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void serverSendMessage() {
		String message = "";
		PrintWriter out = null ;
		message = "������˵��"+ viewField.getText();
		viewArea.setText(viewArea.getText() + message + "\n");
		Set<String> set = hash.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			String receiveKey = it.next();
			Socket s = hash.get(receiveKey);
			try {
				out = new PrintWriter(s.getOutputStream());			
			} catch (IOException e) {
				e.printStackTrace();
			}finally{	
				String msg = "������"+ "�����͸���"+ s + "������Ϣ�ǣ� "+ viewField.getText();// ���巢�͵���Ϣ
				out.println("MSG:"+msg);
				out.flush();
			}					
		}
	}
	
	// ����
	public void serverSurface(String name) {
		frame = new JFrame("Chat Room");
		viewArea = new JTextArea(10, 40);
		jlable = new JLabel();
		jlable.setText("��������ʼ����");
		button1 = new JButton("����");
		MyName = new JTextField();
		MyName.setColumns(9);
		MyName.setText(name);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(8, 1));
		panel.add(jlable);
		panel.add(MyName);
		panel.add(button1);

		JPanel jp_input = new JPanel();
		jp_input.setBorder(new TitledBorder("������Ϣ"));
		viewField = new JTextField(50);
		jp_input.add(viewField);
		
		listModel = new DefaultListModel();
		userList = new JList(listModel);
		JScrollPane sp = new JScrollPane(viewArea);
		sp.setBorder(new TitledBorder("��Ϣ��ʾ��"));
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);// ˮƽ������
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);// ��ֱ������
		JScrollPane spFriend = new JScrollPane(userList);
		spFriend.setBorder(new TitledBorder("������Ա"));
		spFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);// ��ֱ������
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spFriend, sp);
		centerSplit.setDividerLocation(130);

		frame.add("Center", centerSplit);
		frame.add("East", panel);
		frame.add("South", jp_input);
		frame.setSize(700, 400);
		frame.setLocation(200, 100);
		frame.setVisible(true);

		// �ı��򰴻س���ʱ�¼�
		viewField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverSendMessage();
			}
		});
		// ���Ͱ�ť
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == button1) {
					serverSendMessage();
				}
			}
		});
	}
}
