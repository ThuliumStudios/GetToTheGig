package com.aetherium.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import com.aetherium.files.PlayerFiles;
import com.aetherium.net.Registration;
import com.aetherium.net.packet.LoginPacket;
import com.aetherium.net.packet.RegistrationPacket;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class LoginServer {
	private HashMap<Integer, LoginPacket> loginRequests;

	private Server server;
	private PlayerFiles playerFiles;

	public LoginServer() {
		playerFiles = new PlayerFiles();

		// Begin server creation - Open and bind to port
		System.out.println("Beginning login server creation. . .");
		server = new Server();
		Registration.register(server.getKryo());
		try {
			server.bind(51426, 51427);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
		server.addListener(serverListener);

		System.out.println("Login server created");
	}

	public void createAccount(RegistrationPacket packet) {
		System.out.println("Attempting to register");
		packet.result = playerFiles.register(packet);
		System.out.println("Registration attempt returned code=" + packet.result);
		server.sendToTCP(packet.id, packet);
	}
	
	public void signIn(LoginPacket login) {
		System.out.println("Attempting to log in.");
		login.result = playerFiles.getCredentials(login);
		System.out.println("Login attempt returned code=" + login.result);
		server.sendToTCP(login.id, login);
	}
	
	/**
	 * Server listener -> handles packets received
	 */
	private Listener serverListener = new Listener() {
		@Override
		public void received(Connection connection, Object object) {
			switchType(object, caze(LoginPacket.class, o -> signIn(o)), 
					caze(RegistrationPacket.class, o -> createAccount(o)));
//			switchType(object, caze(MessagePacket.class, packet -> sendMessage(packet)),
//					caze(PlayerPacket.class, packet -> queueUpdate(connection.getID(), packet, false)));
		};

		@Override
		public void connected(Connection connection) {
			super.connected(connection);
		}

		@Override
		public void disconnected(Connection connection) {
		};
	};

	public void tick() {
		new Thread() {
			public void run() {
				playerFiles.saveAccounts();
			};
		}.start();
	}
	
	/**
	 * Consumer classes. Allows for a semblance of "switch case" statements with
	 * class types and packet reception.
	 */
	public static <T> void switchType(Object o, Consumer<?>... a) {
		for (Consumer consumer : a)
			consumer.accept(o);
	}

	public static <T> Consumer<?> caze(Class<T> cls, Consumer<T> c) {
		return obj -> Optional.of(obj).filter(cls::isInstance).map(cls::cast).ifPresent(c);
	}
}
