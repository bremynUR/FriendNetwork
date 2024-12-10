package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Friend {
    
    private static final Double maxTime = 1.0;
    private static final Double pickiness = 12.0;
    private static final Double minTime = maxTime/8;
    Random random;
    Double freeTime;
    HashMap<FriendGroup, Double> recievedValues;
    HashMap<FriendGroup, Double> devotedTime;
    String name;
    Boolean printDeatils;
    private ArrayList<FriendGroup> toBeUpdatedKeys;
    private ArrayList<Double> toBeUpdatedValues;
    public Friend(String name, Boolean printDetails) {
        this.freeTime = maxTime;
        this.recievedValues = new HashMap<>();
        this.devotedTime = new HashMap<>();
        random = new Random();
        this.name = name;
        this.printDeatils = printDetails;
    }

    public Double getMaxTime() {
        return maxTime;
    }

    public Integer getNumGroupsMemberOf() {
        return devotedTime.size();
    }

    public Gossip getRandomGossip() {
        if (recievedValues.size() > 0) {
            int randomInt = random.nextInt(recievedValues.size());
            List<FriendGroup> keys = new ArrayList<>(recievedValues.keySet());
            FriendGroup group = keys.get(randomInt);
            Double value = recievedValues.get(group);
            String source = this.name;
            return new Gossip(group, value, source);
        } else {
            return new Gossip((new FriendGroup(null, "")), 0.0, "Null");
        }
    }

    public void learnOfGroup (FriendGroup fg, Double value) {
        recievedValues.put(fg, value);
    }

    public void recieveValues() {
        toBeUpdatedKeys = new ArrayList<>();
        toBeUpdatedValues = new ArrayList<>();
        for (FriendGroup key : devotedTime.keySet()) {
            Double value = key.getValue(devotedTime.get(key));
            toBeUpdatedKeys.add(key);
            toBeUpdatedValues.add(value);
            if (printDeatils) {
                System.out.println(this.name + " received a value of " + value + " from " + key.name);
            }
        }
    }

    public void updateValues() {
        for (FriendGroup f : toBeUpdatedKeys) {
            recievedValues.put(f, toBeUpdatedValues.get(toBeUpdatedKeys.indexOf(f)));
        }
    }

    public void devoteHours() {
        freeTime = maxTime;
        Boolean outOfTime = false;
        ArrayList<FriendGroup> setToBeLeft = new ArrayList<>();
        ArrayList<FriendGroup> listOfKnownGroups = new ArrayList<>();
        for (FriendGroup key : recievedValues.keySet()) {
            listOfKnownGroups.add(key);
        }
        Collections.sort(listOfKnownGroups, (friendGroup1, friendGroup2) 
            -> Double.compare(recievedValues.get(friendGroup1), recievedValues.get(friendGroup2)));
        for (FriendGroup key : listOfKnownGroups) {
            Double intendedTime = recievedValues.get(key) / pickiness;
            if (outOfTime) {
                devotedTime.remove(key);
                setToBeLeft.add(key);
                if (printDeatils) {
                    System.out.println(this.name + " left group " + key.name + " because they had no time for it!");
                }
            } else {
                if (intendedTime < minTime) {
                    devotedTime.remove(key);
                    setToBeLeft.add(key);
                    if (printDeatils) {
                        System.out.println(this.name + " left group " + key.name + " because it was providing too little value to them");
                    }
                } else {
                    if (!key.contains(this)) {
                        key.add(this);
                    }
                    if (freeTime > intendedTime) {
                        freeTime -= intendedTime;
                        devotedTime.put(key, intendedTime);
                        if (printDeatils) {
                            System.out.println(this.name + " wants to spend " + intendedTime + " hours with group " + key.name);
                        }
                    } else {
                        devotedTime.put(key, freeTime);
                        if (printDeatils) {
                            System.out.println(this.name + " wants to spend " + freeTime + " hours with group " + key.name);
                        }
                        freeTime = 0.0;
                        outOfTime = true;
                    }
                }
            }
        }
        for (FriendGroup key : setToBeLeft) {
            recievedValues.remove(key);
        }
    }

    public void gossip() {
        for (FriendGroup key : devotedTime.keySet()) {
            Gossip gossip = key.generateGossip();
            if (!recievedValues.containsKey(gossip.getGroup()) && !gossip.getSource().equals("null")) {
                learnOfGroup(gossip.getGroup(), gossip.getValue());
                if (printDeatils) {
                    System.out.println(this.name + " just heard a rumour from " + gossip.getSource()
                        + " that " + gossip.getGroup().getName() + " provides a value of " + gossip.getValue());
                }
            }
        }
    }

    public Boolean isLeaving() {
        ArrayList<FriendGroup> notActive = new ArrayList<>();
        for (FriendGroup fg : devotedTime.keySet()) {
            if (!fg.isActive()) {
                notActive.add(fg);
            }
        }
        for (FriendGroup fg : notActive) {
            System.out.println(this.name + " had a dupicate fg called " + fg.getName() + ". Current connections = " + devotedTime.size());
            devotedTime.remove(fg);
            System.out.println("removed a (for some reason) lingering group." + " Current connections = " + devotedTime.size());
        }
        return devotedTime.isEmpty();
    }

    public Boolean wantsNewGroup() {
        return freeTime > minTime;
    }

    public double getFreeTime() {
        return this.freeTime;
    }

    public void devoteInitialHours(FriendGroup newGroup, double hours) {
        devotedTime.put(newGroup, hours);
    }

    public void SetInitialValues(FriendGroup newGroup, double value) {
        recievedValues.put(newGroup, value);
    }

    public Double getDevotedHours(FriendGroup friendGroup) {
        return devotedTime.get(friendGroup);
    }

    public Double getTotalValue() {
        Double total = 0.0;
        for (FriendGroup key : recievedValues.keySet()) {
            total += recievedValues.get(key);
        }
        return total;
    }

    public String getName() {
        return name;
    }

    public void loseDevotionTo(FriendGroup friendGroup) {
        devotedTime.remove(friendGroup);
        recievedValues.remove(friendGroup);
    }

    public boolean hasDevotedHours(FriendGroup friendGroup) {
        return devotedTime.containsKey(friendGroup);
    }
}
