package com.sysu;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.pojo.User;

public class Server extends ServerSocket {

	private static final int SERVER_PORT = 2013;

	private static List<ServerThread> thread_list = new ArrayList<ServerThread>();// 服务器已启用线程集合
	private static LinkedList<MsgText> message_list = new LinkedList<MsgText>();// 存放消息队列

	private static Lock lock = new ReentrantLock(true);
	private static Condition notEmpty = lock.newCondition();

	/**
	 * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
	 */
	public Server() throws IOException {
		super(SERVER_PORT);// 创建ServerSocket
		new PrintOutThread();// 创建向客户端发送消息线程

		try {
			while (true) {// 监听客户端请求，启个线程处理
				Socket socket = accept();
				new ServerThread(socket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	/**
	 * 监听是否有输出消息请求线程类,向客户端发送消息
	 */
	class PrintOutThread extends Thread {

		public PrintOutThread() {
			start();
		}

		@Override
		public void run() {
			while (true) {
				lock.lock();
				try {
					while (message_list.size() <= 0) {
						notEmpty.await();
					}
					// 将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
					// System.out.println(thread_list.size());
					MsgText message = message_list.getFirst();
					for (ServerThread thread : thread_list) {
						User user = thread.getUser();
						if ("".equals(message.dst)) { // 聊天室的信息
							if (thread.isChatRoom) {
								thread.sendMessage(message);
							}
						} else { // p2p的信息
							// 对于p2p之间的信息，如果信息类型为0，说明为系统的命令信息，只需要转发到一方，否则双方都需要转发
							if ("0".equals(message.type) && user.getIp().equals(message.dst)) {
								thread.sendMessage(message);
							} else if (!"0".equals(message.type) && 
									(user.getChattingTarget().equals(message.dst)
									 || user.getIp().equals(message.dst))) {
								thread.sendMessage(message);
							}
						}
					}
					message_list.removeFirst();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	/**
	 * 服务器线程类
	 */
	class ServerThread extends Thread {
		private Socket client;
		private ObjectOutputStream oos;
		private ObjectInputStream ois;
		private User user;
		private boolean isChatRoom = false; // false:代表没有加入聊天室，true：代表加入聊天室

		public ServerThread(Socket s) throws IOException {
			client = s;
			oos = new ObjectOutputStream(client.getOutputStream());
			ois = new ObjectInputStream(client.getInputStream());
			MsgText msg = MsgText.fromText("系统消息", "成功连上系统,请输入你的名字：", "0");
			oos.writeObject(msg);
			start();
		}

		@Override
		public void run() {
			try {
				boolean isFirst = true;

				MsgText message;
				while ((message = (MsgText)ois.readObject()) != null) {
					String str = message.getText();

					// 查看在线用户列表
					if ("#showuser".equals(str)) {
						oos.writeObject(this.getOnlineUsers());
						continue;
					}
					
					// 断开socket连接，退出系统
					if ("#bye".equals(str)) {
						System.out.println(user.getName() + " leave system");
						break;
					}

//					if ("chat with:".equals(str.substring(0, 9))) {
//						user.setChattingTarget(str.substring(10));
//						System.out.println(user.getName() + "enter P2P chatting");
//						continue;
//					}
					// 申请与某人进行p2p聊天
					if ("#P2Pchat".equals(str)) {
						user.setChattingTarget(message.getDst());
						MsgText msgText = MsgText.fromText("系统消息", "#requestP2P", "0");
                        msgText.setDst(message.getDst());
                        msgText.setIp(user.getIp());
						this.pushMessage(msgText);
						System.out.println(user.getName() + " enter P2P chatting");
						continue;
					}
					
					// 同意与某人进行p2p聊天
					if ("#confirmP2P".equals(str)) {
						user.setChattingTarget(message.getDst());
						MsgText msgText = MsgText.fromText("系统消息", "#agreeP2P", "0");
                        msgText.setDst(message.getDst());
                        msgText.setIp(user.getIp());
						this.pushMessage(msgText);
						System.out.println(user.getName() + " enter P2P chatting");
						continue;
					}
					
					// 某一方主动断开p2p聊天，需要告诉另一方断开连接
					if ("#leaveP2Pchat".equals(str)) {
						user.setChattingTarget("");
						MsgText msgText = MsgText.fromText("系统消息", "#quitP2P", "0");
                        msgText.setDst(message.getDst());
                        msgText.setIp(user.getIp());
						this.pushMessage(msgText);
						System.out.println(user.getName() + " leave P2P chatting");
						continue;
					}
					
					// 另一方被动断开p2p连接
					if ("#leaveP2PchatPassive".equals(str)) {
						user.setChattingTarget("");
						System.out.println(user.getName() + " leave P2P chatting");
						continue;
					}
					
					// 进入聊天室
					if ("#enterRoom".equals(str)) {
						isChatRoom = true;
						this.pushMessage(MsgText.fromText("系统消息", "Client<" + user.getName() + ">进入聊天室...", "0"));
						System.out.println(user.getName() + " enter chatRoom");
						continue;
					}
					
					// 退出聊天室
					if ("#leaveRoom".equals(str)) {
						isChatRoom = false;
						this.pushMessage(MsgText.fromText("系统消息", "Client<" + user.getName() + ">离开聊天室...", "0"));
						System.out.println(user.getName() + " leave chatRoom");
						continue;
					}

					// 第一次进入系统，保存名字
					if (isFirst) {
						isFirst = false;
						user = new User();
						user.setIp(client.getInetAddress().getHostAddress());
						user.setName(str);
						thread_list.add(this);
						oos.writeObject(MsgText.fromText(user.getName(), "< " + user.getName() + " >你好,可以开始聊天了...", "0"));
//						this.pushMessage(MsgText.fromText("系统消息", "Client<" + user.getName() + ">进入聊天室...", "0"));
						System.out.println(user.getName() + " enter system");
					} else {
						this.pushMessage(message); // 直接转发
					}
				}
				oos.writeObject(MsgText.fromText("系统消息", "#byeClient", "0"));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {// 用户退出聊天室
				try {
					client.close();
					oos.close();
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				thread_list.remove(this);
//				pushMessage(MsgText.fromText("系统消息", "Client<" + user.getName() + ">退出了聊天室", "0"));
			}
		}

		// 获取该线程的user
		private User getUser() {
			return user;
		}

		// 放入消息队列末尾，准备发送给客户端
		private void pushMessage(MsgText msg) {
			lock.lock();
			try {
				if (msg == null) {
					return;
				}
				message_list.addLast(msg);
				notEmpty.signalAll();
			} catch (Exception e) {
			} finally {
				lock.unlock();
			}
		}

		// 向客户端发送一条消息
		private void sendMessage(MsgText msg) throws IOException {
			oos.writeObject(msg);
		}

		// 统计在线用户列表
		private MsgText getOnlineUsers() {
			String onlineUserList = "";
			for (ServerThread aThread_list : thread_list) {
				onlineUserList += "" + aThread_list.getUser().getName() + "$"
						+ aThread_list.getUser().getIp() + "#%#";
			}
			onlineUserList = onlineUserList.substring(0, onlineUserList.length() - 3);

			return MsgText.fromText("系统消息", onlineUserList, "3");
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("启动");
		new Server();// 启动服务端
	}
}
