package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.DmgDeathUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import mc.alk.battlescoreboardapi.scoreboard.SAPIDisplaySlot;

public class MobKills extends VictoryCondition implements ScoreTracker{
	final ArenaObjective mkills;

    public MobKills(Match match, ConfigurationSection section) {
        super(match);
        String displayName = section.getString("displayName", "Mob Kills");
        String criteria = section.getString("criteria", "Kill mobs");

        mkills = new ArenaObjective(getClass().getSimpleName(),displayName, criteria,
                SAPIDisplaySlot.SIDEBAR, 60);
    }

	@Override
	public List<ArenaTeam> getLeaders() {
		return mkills.getTeamLeaders();
	}

	@Override
	public TreeMap<Integer,Collection<ArenaTeam>> getRanks() {
		return mkills.getTeamRanks();
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void mobDeathEvent(EntityDeathEvent event) {
		switch(event.getEntityType()){
//		case BAT:
//			break;
		case BLAZE:
			break;
		case CAVE_SPIDER:
			break;
		case CHICKEN:
			break;
		case COW:
			break;
		case CREEPER:
			break;
		case ENDERMAN:
			break;
		case ENDER_DRAGON:
			break;
		case GHAST:
			break;
		case GIANT:
			break;
		case IRON_GOLEM:
			break;
		case MAGMA_CUBE:
			break;
		case MUSHROOM_COW:
			break;
		case OCELOT:
			break;
		case PIG:
			break;
		case PIG_ZOMBIE:
			break;
		case SHEEP:
			break;
		case SILVERFISH:
			break;
		case SKELETON:
			break;
		case SLIME:
			break;
		case SNOWMAN:
			break;
		case SPIDER:
			break;
		case SQUID:
			break;
		case VILLAGER:
			break;
//		case WITHER:
//			break;
		case WOLF:
			break;
		case ZOMBIE:
			break;
		default:
			return;
		}
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event.getEntity().getLastDamageCause());

		if (killer == null){
			return;}
		ArenaTeam t = match.getTeam(killer);
		if (t == null)
			return;
		t.addKill(killer);
		mkills.addPoints(t, 1);
		mkills.addPoints(killer, 1);
	}

	@ArenaEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
        event.setResult(mkills.getMatchResult(match));
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		this.mkills.setScoreBoard(scoreboard);
		scoreboard.addObjective(mkills);
	}

	@Override
	public void setDisplayTeams(boolean display) {
		mkills.setDisplayTeams(display);
	}
}
