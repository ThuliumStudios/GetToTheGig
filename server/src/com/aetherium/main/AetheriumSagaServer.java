package com.aetherium.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import com.aetherium.login.LoginServer;
import com.aetherium.net.Registration;
import com.aetherium.net.packet.DisconnectionPacket;
import com.aetherium.net.packet.ExperiencePacket;
import com.aetherium.net.packet.MessagePacket;
import com.aetherium.net.packet.PlayerPacket;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class AetheriumSagaServer {
	private HashMap<Integer, MessagePacket> messages = new HashMap<>();
	private HashMap<Integer, Object> updatedTCP = new HashMap<>();
	private HashMap<Integer, Object> updatedUDP = new HashMap<>();
	private Random random;
	private boolean isRunning = true;

	// Tick values
	private double delta;
	private double loginDelta;
	private long lastTime;
	private final double tps = 15.0;	// ticks per second, usually 15
	
	private Server server;
	private LoginServer loginServer;

	public AetheriumSagaServer() {
		// Begin server creation - Open and bind to port
		System.out.println("Beginning server creation. . .");
		server = new Server();
		Registration.register(server.getKryo());
		try {
			server.bind(51428, 51429);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
		server.addListener(serverListener);
		
		loginServer = new LoginServer();
		beginServerTick();
	}

	public void sendServerData() {
		updatedUDP.forEach((k, v) -> server.sendToAllExceptUDP(k, v));
		updatedTCP.forEach((k, v) -> server.sendToAllExceptTCP(k, v));
		updatedUDP.clear();
		updatedTCP.clear();
	}

	public void queueUpdate(int ID, Object object, boolean tcp) {
		if (tcp) {
			updatedTCP.put(ID, object);
		} else {
			updatedUDP.put(ID, object);
		}
		// System.out.println("Adding " + ID + " to updates");
	}
	
	public void sendMessage(MessagePacket packet) {
		ExperiencePacket exp = new ExperiencePacket();
		exp.exp = packet.message.replace(" ", "").length();
		queueUpdate(-1, packet, true);
		queueUpdate(packet.id, exp, true);
	}

	/**
	 * Server listener -> handles packets received
	 */
	private Listener serverListener = new Listener() {
		@Override
		public void received(Connection connection, Object object) {
			switchType(object, caze(MessagePacket.class, packet -> sendMessage(packet)),
					caze(PlayerPacket.class, packet -> queueUpdate(connection.getID(), packet, false)));
		};

		@Override
		public void connected(Connection connection) {
			super.connected(connection);
		}

		@Override
		public void disconnected(Connection connection) {
			DisconnectionPacket packet = new DisconnectionPacket();
			packet.ID = connection.getID();
			server.sendToAllExceptTCP(connection.getID(), packet);
		};
	};

	/**
	 * Main method
	 * 
	 * @param args Default arguments
	 */
	public static void main(String[] args) {
		new AetheriumSagaServer();
	}
	
	public void beginServerTick() {
		random = new Random(System.currentTimeMillis());
		lastTime = System.nanoTime();
		while (isRunning) {
			tick();
		}
	}

	public void tick() {
		long now = System.nanoTime();

		delta += (now - lastTime) / (1000000000.0 / tps);
		loginDelta += (now - lastTime) / (1000000000.0);
		lastTime = now;

		while (delta >= 1) {
			sendServerData();
			delta--;
		}
		
		while (loginDelta >= 1) {
			loginServer.tick();
			loginDelta--;
		}
	}
	
	public void tickGUI() {
		// Only use when trying to view server GUI
		Timer tickTimer = new Timer();
		tickTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (isRunning)
					sendServerData();
			}
		}, 3000, 1000 / 10L);
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