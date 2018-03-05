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
	public static JList userList;		//��ʾ�����б�
	public JSplitPane centerSplit;
	public static boolean isConnected = false;
	static Client4 objClient;
	static DefaultComboBoxModel model;
	static ObjectOutputStream out;
	boolean loginFlag = false;
	
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
			Socket s = new Socket("localhost", 8848);
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
				model = (DefaultComboBoxModel) userList.getModel();// ����б����ģ��
				while(true){
					String info = in.readLine().trim();// ��ȡ��Ϣ
					if (!info.startsWith("MSG:")) {// ���յ��Ĳ�����Ϣ
						if (info.startsWith("�˳���")) {// ���յ������˳���Ϣ
							model.removeElement(info.substring(3));// ���û��б����Ƴ��û�
						} else {// ���յ����ǵ�¼�û�			
							boolean itemFlag = false;// ����Ƿ�Ϊ�б��������б��Ϊtrue�����ӣ�Ϊfalse����
							for (int i = 0; i < model.getSize(); i++) {// ���û��б����б���
								if (info.equals((String) model.getElementAt(i))) {// ����û��б��д��ڸ��û���
									itemFlag = true;// ����Ϊtrue����ʾ�����ӵ��û��б�
									break;// ����forѭ��
								}
							}
							if (!itemFlag) {			
								model.addElement(info);// ����¼�û����ӵ��û��б�
							}
					}
					}else{				
						DateFormat df = DateFormat.getDateInstance();// ���DateFormatʵ��
						String dateString = df.format(new Date()); // ��ʽ��Ϊ����
						df = DateFormat.getTimeInstance(DateFormat.MEDIUM);// ���DateFormatʵ��
						String timeString = df.format(new Date()); // ��ʽ��Ϊʱ��
						String id = info.substring(4,info.indexOf("�����͸���"));  //���Ͷ˵�id
						String message = info.substring(info.indexOf("������Ϣ�ǣ�")+6);
						String text = new String("  " + id + "    " + dateString+ "  " + timeString + "\n  " + message+ "\n");
				
						SimpleAttributeSet aSet = new SimpleAttributeSet();     
					    StyleConstants.setForeground(aSet, Color.red);    
					    StyleConstants.setBackground(aSet, Color.orange);    
					    StyleConstants.setFontFamily(aSet, "lucida bright italic");    
					    StyleConstants.setFontSize(aSet, 18);    					
						//StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_LEFT); 
						objClient.textPane.setParagraphAttributes(aSet,false); 					
						objClient.viewField.setText(null);// ����ı���	
						Document docs = objClient.textPane.getDocument();//����ı�����
					        try {
					            docs.insertString(docs.getLength(), text,aSet);//���ı�����׷��
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
			return;// ���û������Ϣ�򷵻أ���������
		}
		Vector<String> v = new Vector<String>();// ���������������ڴ洢���͵���Ϣ
		Object[] receiveUserNames = userList.getSelectedValues();// ���ѡ����û�����
		if (receiveUserNames.length <= 0) {
			return;// ���ûѡ���û��򷵻�
		}
		for (int i = 0; i < receiveUserNames.length; i++) {
			String msg = objClient.jt_login.getText() + "�����͸���"+ (String) receiveUserNames[i] + "������Ϣ�ǣ� "+ objClient.viewField.getText();// ���巢�͵���Ϣ
			v.add(msg);// ����Ϣ���ӵ�����
		}
		try {
			out.writeObject(v);// ������д��������������Ϣ�ķ���
			out.flush();// ˢ�����������
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		DateFormat df = DateFormat.getDateInstance();// ���DateFormatʵ��
		String dateString = df.format(new Date()); // ��ʽ��Ϊ����
		df = DateFormat.getTimeInstance(DateFormat.MEDIUM);// ���DateFormatʵ��
		String timeString = df.format(new Date()); // ��ʽ��Ϊʱ��	
		String text = new String("   " + userList.getSelectedValue() + "    " + dateString+ "  " + timeString + "\n  " + viewField.getText()+ "\n");						
		
		SimpleAttributeSet aSet=new SimpleAttributeSet(); 		//����ʱֻ�ı��һ�ν��յ��ķ���
		StyleConstants.setForeground(aSet, Color.blue);        
		StyleConstants.setFontFamily(aSet, "lucida bright italic");    
		StyleConstants.setFontSize(aSet, 18);   
		StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_RIGHT); 
		objClient.textPane.setParagraphAttributes(aSet,false); 	
		Document docs = objClient.textPane.getDocument();//����ı�����
		System.out.println(docs);
        try {
            docs.insertString(docs.getLength(), text,aSet);//���ı�����׷��
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
	}
	
	public void clientSurface() {
		frame = new JFrame("Chat Room");
//		viewArea = new JTextArea(10, 40);
		textPane = new JTextPane();
		jlable = new JLabel();
		jlable.setText("����");
		button1 = new JButton("Send");
		MyName = new JTextField();		
		MyName.setColumns(9);
		JPanel panel = new JPanel(); 
		panel.setLayout(new GridLayout(8, 1));
		panel.add(jlable);
		panel.add(MyName);
		panel.add(button1);
		
		JPanel jp_input = new JPanel();
		jp_input.setBorder(new TitledBorder("������Ϣ")); 
		viewField = new JTextField(50);
		jp_input.add(viewField);
		
		buttonLogin = new JButton("��¼");
		buttonExit = new JButton("�˳�");
		jt_login = new JTextField(20);
		JPanel panelLogin = new JPanel();
		panelLogin.setBorder(new TitledBorder("��¼��")); 
		panelLogin.add(jt_login);
		panelLogin.add(buttonLogin);
		panelLogin.add(buttonExit);
		
		listModel = new DefaultListModel();  
		
	    userList = new JList(listModel);  
		JScrollPane sp = new JScrollPane(textPane);
		sp.setBorder(new TitledBorder("��Ϣ��ʾ��")); 
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);//ˮƽ������
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);//��ֱ������
		JScrollPane spFriend = new JScrollPane(userList);
		spFriend.setBorder(new TitledBorder("�����б�")); 
		userList.setModel(new DefaultComboBoxModel(new String[] { "" }));
		spFriend.setViewportView(userList);
		spFriend.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);//��ֱ������		
		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spFriend, sp);  
        centerSplit.setDividerLocation(130);  
        
 		frame.add("Center", centerSplit);
		frame.add("North", panelLogin);
		frame.add("East", panel);
		frame.add("South", jp_input);
		frame.setSize(700, 400);
		frame.setLocation(200,100);
		frame.setVisible(true);
		
		// �ı��򰴻س���ʱ�¼�  
		viewField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                sendMessage();  
            }  
        });  
		//���Ͱ�ť
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == button1) {
					sendMessage();
				}
			}
		});
		//������¼��ť
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (loginFlag) {// �ѵ�¼���Ϊtrue
					JOptionPane.showMessageDialog(null, "��ͬһ����ֻ�ܵ�¼һ�Ρ�");
					return;
				}
				String userName = jt_login.getText().trim();// ��õ�¼�û���
				Vector v = new Vector();// �������������ڴ洢��¼�û�
				v.add("�û���" + userName);// ���ӵ�¼�û�
				try {
					out.writeObject(v);// ���û��������͵�������
					out.flush();// ˢ�����������
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				jt_login.setEnabled(false);// �����û��ı���
				buttonLogin.setEnabled(false);
				loginFlag = true;// ���ѵ�¼�������Ϊtrue
				MyName.setText(userName);
			}
		});
		//�˳���ť
		buttonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String exitUser = jt_login.getText().trim();
				Vector v = new Vector();
				v.add("�˳���" + exitUser);
				try {
					out.writeObject(v);
					out.flush();// ˢ�����������
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				System.exit(0); // �˳�ϵͳ
			}
		});
//		��������
		 if (SystemTray.isSupported())
	      {
	         // ��ȡͼƬ���ڵ�URL
	         URL url = Client4.class.getResource("qq.jpg");
	         // ʵ����ͼ�����
	         ImageIcon icon = new ImageIcon(url);
	         // ���Image����
	         Image image = icon.getImage();
	         // ��������ͼ��
	         TrayIcon trayIcon = new TrayIcon(image);
	         // Ϊ�����������������
	         trayIcon.addMouseListener(new MouseAdapter()
	         {
	            // ����¼�
	            public void mouseClicked(MouseEvent e)
	            {
	               if (e.getClickCount() == 2)
	               {
	                  JOptionPane.showMessageDialog(null, "�ѵ�½�ɹ�");
	               }
	            }
	         });
	         // ���ӹ�����ʾ�ı�
	         trayIcon.setToolTip("QQ:��������\r\n�ٶȣ�100.0 Mbps\r\n״̬����������");
	         // ���������˵�
	         PopupMenu popupMenu = new PopupMenu();
	         popupMenu.add(new MenuItem("��������"));
	         popupMenu.add(new MenuItem("æµ"));
	         popupMenu.add(new MenuItem("����"));
	         popupMenu.add(new MenuItem("����"));
	         popupMenu.addSeparator();
	         popupMenu.add(new MenuItem("�ر���������"));
	         popupMenu.add(new MenuItem("�ر�ͷ������"));
	         popupMenu.addSeparator();
	         popupMenu.add(new MenuItem("�˳�"));

	         // Ϊ����ͼ��ӵ����˵�
	         trayIcon.setPopupMenu(popupMenu);
	         // ���ϵͳ���̶���
	         SystemTray systemTray = SystemTray.getSystemTray();
	         try
	         {
	            // Ϊϵͳ���̼�����ͼ��
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