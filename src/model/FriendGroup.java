package model;

import java.util.Random;

import java.util.ArrayList;

public class FriendGroup {
    
    ArrayList<Friend> members;
    Random random;
    String name;
    Boolean active;

    public FriendGroup(ArrayList<Friend> members, String name) {
        this.members = members;
        random = new Random();
        this.name = name;
        this.active = true;
    }
    
    public String getName() {
        return name;
    }

    public void deactivate() {
        this.active = false;
    }

    public Boolean isActive() {
        return active;
    }

    public void add(Friend f) {
        members.add(f);
    }

    public Boolean contains(Friend f) {
        return members.contains(f);
    }

    public void clear() {
        members = new ArrayList<>();
    }

    public Integer size() {
        return members.size();
    }

    public double getValue(Double devotedTime) {
        Double totalmemberValue = 0.0;
        Double totalMemberDevotion = 0.0;
        for (Friend f : members) {
            if (f.hasDevotedHours(this)) {
                totalMemberDevotion += f.getDevotedHours(this);
                totalmemberValue += f.getTotalValue();
            }
        }
        Double averageMemberValue = totalmemberValue / members.size();
        Double averageMemberDevotion = totalMemberDevotion / members.size();
        Double enjoyment = averageMemberValue / devotedTime;
        Double acceptance = averageMemberDevotion / members.size();
        Double value = acceptance * enjoyment;
        return value;
    }

    public Gossip generateGossip() {
        int randomInt = random.nextInt(members.size());
        Gossip gossip = members.get(randomInt).getRandomGossip();
        if (gossip.getSource().equals("groups")) {
            return gossip;
        } else {
            return members.get(randomInt).getRandomGossip();
        }
    }

    public String MemberString() {
        String str = "";
        for (Friend f : members) {
            str += f.getName() + " ";
        }
        return str;
    }

    public void loseDevotion() {
        for (Friend f : members) {
            f.loseDevotionTo(this);
        }
    }

}
