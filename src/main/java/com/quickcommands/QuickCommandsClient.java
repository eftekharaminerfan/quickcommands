package com.quickcommands;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickCommandsClient implements ClientModInitializer {
	public static final String MOD_ID = "quickcommands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keys"));

	private static final KeyMapping[] KEYS = new KeyMapping[CommandConfig.SLOTS];

	@Override
	public void onInitializeClient() {
		CommandConfig.load();

		// One key slot per command. They start unbound; set the key in Options > Controls > Key Binds.
		for (int i = 0; i < CommandConfig.SLOTS; i++) {
			KEYS[i] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
					"key." + MOD_ID + ".slot_" + (i + 1),
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_UNKNOWN,
					CATEGORY
			));
		}

		// Run the command the moment a key is pressed.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			for (int i = 0; i < KEYS.length; i++) {
				while (KEYS[i].consumeClick()) {
					run(client, CommandConfig.get(i + 1));
				}
			}
		});

		registerClientCommand();
	}

	private static void run(Minecraft client, String command) {
		if (client.player == null || command.isEmpty()) {
			return;
		}
		// Sends the command to the server exactly like typing it in chat (without the leading slash).
		client.player.connection.sendCommand(command);
	}

	/** In-game setup:  /quickcmd set 1 rtp   /quickcmd list   /quickcmd clear 1   /quickcmd reload */
	private static void registerClientCommand() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("quickcmd")
						.then(ClientCommands.literal("set")
								.then(ClientCommands.argument("slot", IntegerArgumentType.integer(1, CommandConfig.SLOTS))
										.then(ClientCommands.argument("command", StringArgumentType.greedyString())
												.executes(context -> {
													int slot = IntegerArgumentType.getInteger(context, "slot");
													String command = StringArgumentType.getString(context, "command");
													CommandConfig.set(slot, command);
													context.getSource().sendFeedback(Component.literal(
															"Slot " + slot + " -> /" + CommandConfig.get(slot)
															+ "  (set its key in Options > Controls > Key Binds)"));
													return 1;
												}))))
						.then(ClientCommands.literal("clear")
								.then(ClientCommands.argument("slot", IntegerArgumentType.integer(1, CommandConfig.SLOTS))
										.executes(context -> {
											int slot = IntegerArgumentType.getInteger(context, "slot");
											CommandConfig.set(slot, "");
											context.getSource().sendFeedback(Component.literal("Slot " + slot + " cleared."));
											return 1;
										})))
						.then(ClientCommands.literal("list")
								.executes(context -> {
									for (int slot = 1; slot <= CommandConfig.SLOTS; slot++) {
										String command = CommandConfig.get(slot);
										context.getSource().sendFeedback(Component.literal(
												"Slot " + slot + ": " + (command.isEmpty() ? "(empty)" : "/" + command)));
									}
									return 1;
								}))
						.then(ClientCommands.literal("reload")
								.executes(context -> {
									CommandConfig.load();
									context.getSource().sendFeedback(Component.literal("Quick Commands reloaded."));
									return 1;
								}))));
	}
}
