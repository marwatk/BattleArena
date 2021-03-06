package mc.alk.arena.events.prizes;

import java.util.Collection;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.teams.ArenaTeam;

/**
 * Event that is called when the prizes for drawers is called
 *
 */
public class ArenaDrawersPrizeEvent extends ArenaPrizeEvent {

    public ArenaDrawersPrizeEvent(Competition competition, Collection<ArenaTeam> teams) {
        super(competition, teams);
    }

}
