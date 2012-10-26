package mc.alk.arena.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.PlayerStoreController.PInv;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.alk.virtualPlayer.VirtualPlayers;

/**
 *
 * @author alkarin
 *
 */
public class BAPlayerListener implements Listener  {
	public static HashSet<String> die = new HashSet<String>();
	public static HashSet<String> clearInventory= new HashSet<String>();
	public static HashMap<String,Integer> clearWool= new HashMap<String,Integer>();
	public static HashMap<String,Location> tp = new HashMap<String,Location>();

	public static HashMap<String,Integer> expRestore = new HashMap<String,Integer>();
	public static HashMap<String,PInv> itemRestore = new HashMap<String,PInv>();
	public static HashMap<String,List<ItemStack>> itemRemove = new HashMap<String,List<ItemStack>>();
	public static HashMap<String,GameMode> gamemodeRestore = new HashMap<String,GameMode>();
	public static HashMap<String,String> messagesOnRespawn = new HashMap<String,String>();

	BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
		die.clear();
		clearInventory.clear();
		clearWool.clear();
		tp.clear();
		expRestore.clear();
		itemRestore.clear();
		gamemodeRestore.clear();
		messagesOnRespawn.clear();
		this.bac = bac;
	}

	/**
	 *
	 * Why priority.HIGHEST: if an exception happens after we have already set their respawn location,
	 * they relog in at a separate time and will not get teleported to the correct place.
	 * As a workaround, try to handle this event last.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		final String name = p.getName();
		if (clearInventory.remove(name)){
			Log.warn("[BattleArena] clearing inventory for quitting during a match " + p.getName());
			for (ItemStack is: p.getInventory().getContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
//				FileLogger.log("d  itemstack="+ InventoryUtil.getItemString(is));
			}
			for (ItemStack is: p.getInventory().getArmorContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
//				FileLogger.log("d aitemstack="+ InventoryUtil.getItemString(is));
			}
			InventoryUtil.clearInventory(p);
		}
		playerReturned(p, null);
	}

	/**
	 * Why priority highest, some other plugins try to force respawn the player in spawn(or some other loc)
	 * problem is if they have come from the creative world, their game mode gets reset to creative
	 * but the other plugin force spawns them at spawn... so they now have creative in an area they shouldnt
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
		playerReturned(p, event);
	}

	private void playerReturned(final Player p, PlayerRespawnEvent event) {
		final String name = p.getName();
		final String msg = messagesOnRespawn.remove(p.getName());
		if (msg != null){
			MessageUtil.sendMessage(p, msg);
		}

		if (die.remove(name)){
			MessageUtil.sendMessage(p, "&eYou have been killed by the Arena for not being online");
			p.setHealth(0);
			return;
		}

		/// Teleport players, or set respawn point
		if (tp.containsKey(name)){
			Location loc = tp.get(name);
			if (loc != null){
				if (event == null){
					Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
						@Override
						public void run() {
							Player pl = Util.findPlayerExact(name);
							if (pl != null){
								TeleportController.teleport(p, tp.remove(name));
							} else {
								Util.printStackTrace();
							}
						}
					});
				} else {
					PermissionsUtil.givePlayerInventoryPerms(p);
					event.setRespawnLocation(tp.remove(name));
				}
			} else { /// this is bad, how did they get a null tp loc
				Log.err(name + " respawn loc =null");
			}
		}

		/// Do these after teleports
		/// Restore game mode
		if (gamemodeRestore.containsKey(name)){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
				@Override
				public void run() {
					PlayerStoreController.setGameMode(p, gamemodeRestore.remove(name));
				}
			});
		}

		/// Exp restore
		if (expRestore.containsKey(p.getName())){
			final int exp = expRestore.remove(p.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = Bukkit.getPlayerExact(name);
					if (pl != null){
						pl.giveExp(exp);
					}
				}
			});
		}
		/// Restore Items
		if (itemRestore.containsKey(p.getName())){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl;
					if (Defaults.DEBUG_VIRTUAL){ pl = VirtualPlayers.getPlayer(name);}
					else {pl = Bukkit.getPlayer(name);}
					//					System.out.println("### restoring items to " + name +"   pl = " + pl);
					if (pl != null){

						PInv pinv = itemRestore.remove(pl.getName());
						ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
						PlayerStoreController.setInventory(ap, pinv);
					}
				}
			});
		}
		/// Remove Items
		if (itemRemove.containsKey(p.getName())){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl;
					if (Defaults.DEBUG_VIRTUAL){ pl = VirtualPlayers.getPlayer(name);}
					else {pl = Bukkit.getPlayer(name);}
					if (pl != null){
						List<ItemStack> items = itemRemove.remove(pl.getName());
						PlayerStoreController.removeItems(BattleArena.toArenaPlayer(pl), items);
					}
				}
			});
		}
	}

	public static void killOnReenter(String playerName) {
		die.add(playerName);
	}

	public static void teleportOnReenter(String playerName, Location loc) {
		tp.put(playerName,loc);
	}

	public static void addMessageOnReenter(String playerName, String string) {
		messagesOnRespawn.put(playerName, string);
	}

	public static void restoreExpOnReenter(String playerName, Integer f) {
		if (expRestore.containsKey(playerName)){
			f += expRestore.get(playerName);}
		expRestore.put(playerName, f);
	}

	public static void restoreItemsOnReenter(String playerName, PInv pinv) {
		itemRestore.put(playerName,pinv);
	}

	public static void clearWoolOnReenter(String playerName, int color) {
		if (playerName==null || color == -1)
			return;
		clearWool.put(playerName, color);
	}

	public static void restoreGameModeOnEnter(String playerName, GameMode gamemode) {
		gamemodeRestore.put(playerName, gamemode);
	}

	public static void removeItemOnEnter(ArenaPlayer p, ItemStack is) {
		List<ItemStack> items = itemRemove.get(p.getName());
		if (items == null){
			items = new ArrayList<ItemStack>();
			itemRemove.put(p.getName(), items);
		}
		items.add(is);
	}

	public static void removeItemsOnEnter(ArenaPlayer p, List<ItemStack> itemsToRemove) {
		List<ItemStack> items = itemRemove.get(p.getName());
		if (items == null){
			items = new ArrayList<ItemStack>();
			itemRemove.put(p.getName(), items);
		}
		items.addAll(itemsToRemove);
	}
}
