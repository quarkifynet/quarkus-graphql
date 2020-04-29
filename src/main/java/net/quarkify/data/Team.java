package net.quarkify.data;

public class Team {
    public Long id;
    public String name;
    public User[] users;

    public Team(Long id, String name, User... users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }
}
