package model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Team {
    protected String _name;
    protected Set<Player> _players = new HashSet<>();
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        _name = name;
    }
    public Set<Player> getAllPlayers() {
        return Collections.unmodifiableSet(_players);
    }
    public Team() {
        this("team");
    }
    public Team(String name) {
        setName(name);
    }
    public boolean addPlayer(Player player) {
        if (player.getTeam() == null) {
            _players.add(player);
            player.setTeam(this);
            return true;
        } else {
            return false;
        }
    }
    public boolean removePlayer(Player player) {
        if (player.getTeam() == this) {
            _players.remove(player);
            player.setTeam(null);
            return true;
        } else {
            return false;
        }
    }
}