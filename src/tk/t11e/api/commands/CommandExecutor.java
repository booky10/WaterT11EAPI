package tk.t11e.api.commands;
// Created by booky10 in BungeeT11E (19:07 03.02.20)

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import tk.t11e.api.main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public abstract class CommandExecutor {

    private final String command, permission,usage;
    private final String[] aliases;
    private final Receiver receiver;
    private final Plugin plugin;
    private final ProxyServer proxy;

    public CommandExecutor(Plugin plugin, String command, String usage, Receiver receiver) {
        this(plugin, command,usage, "",receiver);
    }

    public CommandExecutor(Plugin plugin, String command, String usage, String permission,
                           Receiver receiver, String... aliases) {
        this.command = command;
        this.permission = permission;
        this.usage=usage;
        this.aliases = aliases;
        this.receiver=receiver;
        this.plugin = plugin;
        this.proxy = plugin.getProxy();

        class Executor extends Command implements TabExecutor {

            public Executor() {
                super(command, "", aliases);
            }

            @Override
            public void execute(CommandSender sender, String[] args) {
                if (sender instanceof ProxiedPlayer && arePlayersAllowed()) {
                    ProxiedPlayer player = (ProxiedPlayer) sender;
                    if (getPermission().equals(""))
                        onExecute(player, args, args.length);
                    else if (hasPermission(player))
                        onExecute(player, args, args.length);
                    else
                        player.sendMessage(Main.NO_PERMISSION);
                } else if (areConsolesAllowed())
                    onExecute(sender, args, args.length);
                else
                    sender.sendMessage("You must execute this command as a player!");
            }

            @Override
            public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
                if (args.length == 0)
                    return convertTab(args, onComplete(sender, args, 0));
                else
                    return convertTab(args, onComplete(sender, args, args.length - 1));
            }
        }

        proxy.getPluginManager().registerCommand(plugin, new Executor());
    }

    public Boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }

    public String getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }

    public Boolean arePlayersAllowed() {
        return receiver.equals(Receiver.ALL)||receiver.equals(Receiver.PLAYER);
    }

    public Boolean areConsolesAllowed() {
        return receiver.equals(Receiver.ALL)||receiver.equals(Receiver.CONSOLE);
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public void help(CommandSender sender) {
        if(sender instanceof ProxiedPlayer)
            sender.sendMessage(Main.PREFIX+"Usage: "+getUsage());
        else
            sender.sendMessage("Usage: "+getUsage());
    }

    public List<String> getOnlinePlayerNames() {
        List<String> names=new ArrayList<>();
        for (ProxiedPlayer player : proxy.getPlayers())
            names.add(player.getDisplayName());
        return names;
    }

    public List<String> convertTab(String[] args, List<String> completions) {
        List<String> list = new ArrayList<>();

        if (args.length < 1)
            return Collections.emptyList();
        String word = args[args.length - 1];
        if (word.equalsIgnoreCase(""))
            return completions;

        for (String entry : completions)
            if (entry.startsWith(word)&& !entry.equals(word))
                list.add(entry);

        return list;
    }

    public abstract void onExecute(CommandSender sender, String[] args, Integer length);

    public abstract void onExecute(ProxiedPlayer player, String[] args, Integer length);

    public abstract List<String> onComplete(CommandSender sender, String[] args, Integer length);

    protected enum Receiver {
        ALL,
        PLAYER,
        CONSOLE
    }
}