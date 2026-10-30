package com.quickcommands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores which command is attached to each key slot.
 * Saved to  .minecraft/config/quickcommands.json
 */
public final class CommandConfig {
	public static final int SLOTS = 12;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("quickcommands.json");

	/** What gets written to / read from the JSON file. */
	private static final class Data {
		List<String> commands = new ArrayList<>();
	}

	private static final String[] COMMANDS = new String[SLOTS];

	private CommandConfig() {
	}

	/** Slot is 1-based (1..SLOTS). Returns "" when empty. */
	public static String get(int slot) {
		String value = COMMANDS[slot - 1];
		return value == null ? "" : value;
	}

	public static void set(int slot, String command) {
		COMMANDS[slot - 1] = clean(command);
		save();
	}

	/** Removes a leading slash and surrounding spaces, so "/rtp" and "rtp" both work. */
	public static String clean(String command) {
		if (command == null) {
			return "";
		}
		String cleaned = command.strip();
		while (cleaned.startsWith("/")) {
			cleaned = cleaned.substring(1).strip();
		}
		return cleaned;
	}

	public static void load() {
		for (int i = 0; i < SLOTS; i++) {
			COMMANDS[i] = "";
		}

		if (!Files.exists(FILE)) {
			// First launch: write a file with a few examples so it's easy to see the format.
			COMMANDS[0] = "spawn";
			COMMANDS[1] = "home";
			COMMANDS[2] = "rtp";
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(FILE)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data != null && data.commands != null) {
				for (int i = 0; i < SLOTS && i < data.commands.size(); i++) {
					COMMANDS[i] = clean(data.commands.get(i));
				}
			}
		} catch (Exception e) {
			QuickCommandsClient.LOGGER.error("Could not read {}", FILE, e);
		}
	}

	public static void save() {
		Data data = new Data();
		for (int i = 0; i < SLOTS; i++) {
			data.commands.add(COMMANDS[i] == null ? "" : COMMANDS[i]);
		}

		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				GSON.toJson(data, writer);
			}
		} catch (IOException e) {
			QuickCommandsClient.LOGGER.error("Could not write {}", FILE, e);
		}
	}
}
