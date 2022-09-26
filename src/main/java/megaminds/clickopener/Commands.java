package megaminds.clickopener;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import megaminds.clickopener.api.ClickType;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.item.BlockItem;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public class Commands {
	private static final SuggestionProvider<ServerCommandSource> BLOCK_ITEM_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestIdentifiers(Registry.ITEM.stream().filter(i->i instanceof BlockItem).map(Registry.ITEM::getId), builder);
	};
	private static final SuggestionProvider<ServerCommandSource> CLICK_TYPE_SUGGESTIONS = (context, builder) -> {
		return CommandSource.suggestMatching(Arrays.stream(ClickType.values()).filter(c->!ClickType.OTHER.equals(c)).map(Enum::name), builder);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
		var root = literal(ClickOpenerMod.MODID)
				.requires(s->s.hasPermissionLevel(4));

		var reload = literal("reload")
				.executes(Commands::reload);

		var entry = literal("entry")
				.then(argument("item", identifier())
						.suggests(BLOCK_ITEM_SUGGESTIONS)
						.then(argument("enabled", bool())
								.executes(Commands::entry)))
				.then(literal("inhand")
						.then(argument("enabled", bool())
								.requires(s->s.isExecutedByPlayer())
								.executes(Commands::entryNoItem)));

		var clickType = literal("clickType")
				.then(argument("clickType", word())
						.suggests(CLICK_TYPE_SUGGESTIONS)
						.executes(Commands::clickType));

		var def = literal("default")
				.then(argument("default", bool())
						.executes(Commands::def));

		root.then(reload);
		root.then(entry);
		root.then(clickType);
		root.then(def);
		dispatcher.register(root);
	}

	private static int def(CommandContext<ServerCommandSource> context) {
		var def = getBool(context, "default");
		Config.setDefault(def);
		context.getSource().sendFeedback(Text.of("Default set to "+def), false);
		return 1;
	}

	private static int clickType(CommandContext<ServerCommandSource> context) {
		var type = ClickType.tryValueOf(getString(context, "clickType"), ClickType.OTHER);
		if (ClickType.OTHER.equals(type)) {
			context.getSource().sendError(Text.of("Invalid ClickType"));
			return 0;
		}

		Config.setClickType(type);
		context.getSource().sendFeedback(Text.of("ClickType set to "+type), false);
		return 1;
	}

	private static int reload(CommandContext<ServerCommandSource> context) {
		Config.load();
		context.getSource().sendFeedback(Text.of("ClickOpener Config Reloaded"), false);
		return 1;
	}

	private static int entryNoItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var item = context.getSource().getPlayerOrThrow().getMainHandStack().getItem();
		if (!(item instanceof BlockItem)) {
			context.getSource().sendError(Text.of("Item must be a BlockItem"));
			return 0;
		}

		var id = Registry.ITEM.getId(item);
		var enabled = getBool(context, "enabled");
		Config.addItem(id, enabled);
		context.getSource().sendFeedback(Text.of(id+(enabled ? " enabled" : " disabled")), false);
		return 1;
	}

	private static int entry(CommandContext<ServerCommandSource> context) {
		var item = getIdentifier(context, "item");
		if (!(Registry.ITEM.get(item) instanceof BlockItem)) {
			context.getSource().sendError(Text.of("Item must be a BlockItem"));
			return 0;
		}

		var enabled = getBool(context, "enabled");
		Config.addItem(item, enabled);
		context.getSource().sendFeedback(Text.of(item+(enabled ? " enabled" : " disabled")), false);
		return 1;
	}
}