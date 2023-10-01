package com.aetherium.files;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.aetherium.net.packet.LoginPacket;
import com.aetherium.net.packet.RegistrationPacket;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.utils.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class PlayerFiles {
	// Stores all data that needs to be saved, clears when saved
	private HashMap<String, String> tmpAccts;
	// Stores all player login data
	private HashMap<String, String> players;
	private File credentials;

	private Gson gson;
	private FileWriter fw;
	private JsonWriter writer;

	public static final String PATH = System.getProperty("user.home") + "/aoe/";

	public PlayerFiles() {
		players = new HashMap<String, String>();
		tmpAccts = new HashMap<String, String>();
		credentials = new File(PATH + "plf.json");

		gson = new Gson();

		try {

			FileReader fr = new FileReader(credentials);
			JsonReader reader = new JsonReader(fr);

			String username = "";
			String password = "";
			while (reader.hasNext()) {
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case NAME:
					username = reader.nextName();
					break;
				case STRING:
					password = reader.nextString();
					break;
				default:
					break;
				}
				
				players.put(username, password);
			}

			fr.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 0: Successful sign in 1: Username does not exist 2: Username exists, password
	 * does not match
	 * 
	 * 
	 * @param login
	 * @return
	 */
	public int getCredentials(LoginPacket login) {
		if (players.containsKey(login.username)) {
			if (players.get(login.username).contentEquals(login.password)) {
				return 0;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}

	public int register(RegistrationPacket packet) {
		if (!players.containsKey(packet.username)) {
			tmpAccts.put(packet.username, packet.password);
			players.put(packet.username, packet.password);
			return 0;
		}

		return 1;
	}

	public void saveAccounts() {
		if (tmpAccts.size() < 1)
			return;

		try {
			fw = new FileWriter(credentials);
			writer = new JsonWriter(fw);
			
			gson.toJson(players, writer);
			
			writer.flush();
			writer.close();
		} catch (JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tmpAccts.clear();
	}

	/**
	 * Save any updates players have to their saved data profiles (paf.json).
	 */
	public void saveChanges(String name, String key, Object value) {

	}
}
