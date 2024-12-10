package model;

public class Gossip {
    
    FriendGroup group;
    Double value;
    String source;

    public Gossip(FriendGroup group, Double value, String source) {
        this.group = group;
        this.value = value;
        this.source = source;
    }

    public FriendGroup getGroup() {
        return group;
    }

    public Double getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

}
