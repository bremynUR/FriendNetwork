package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import model.Friend;
import model.FriendGroup;

public class Network {
    
    Friend f = new Friend("", false);
    Double t = f.getMaxTime();
    Double initialStandardValue = t * 3.0;

    private static final Integer minGroupSize = 3;
    private static final Double hesitance = 2.0;
    private static final Boolean printDetails = true;
    private static final Boolean simplePrintDetails = true;
    private Scanner scanner = new Scanner(System.in);
    String input;
    Integer iterations = 0;
    private ArrayList<Friend> requestingNewGroup;
    private ArrayList<Friend> activeFriends;
    private ArrayList<FriendGroup> activeFriendGroups;
    private Integer nextFriendCount = 7;
    private int nextGroupCount = 3;

    public Network(ArrayList<Friend> activeFriends) {
        this.requestingNewGroup = new ArrayList<>();
        this.activeFriendGroups = new ArrayList<>();
        this.activeFriends = activeFriends;
    }

    public void generateNetwork() {
        Friend f1 = new Friend("f1", printDetails);
        Friend f2 = new Friend("f2", printDetails);
        Friend f3 = new Friend("f3", printDetails);
        Friend f4 = new Friend("f4", printDetails);
        Friend f5 = new Friend("f5", printDetails);
        Friend f6 = new Friend("f6", printDetails);
        activeFriends.add(f1);
        activeFriends.add(f2);
        activeFriends.add(f3);
        activeFriends.add(f4);
        activeFriends.add(f5);
        activeFriends.add(f6);
        ArrayList<Friend> g1 = new ArrayList<>();
        g1.add(f1);
        g1.add(f2);
        g1.add(f3);
        ArrayList<Friend> g2 = new ArrayList<>();
        g2.add(f1);
        g2.add(f2);
        g2.add(f3);
        g2.add(f4);
        g2.add(f5);
        g2.add(f6);
        FriendGroup fg1 = new FriendGroup(g1, "fg1");
        for (Friend f : g1) {
            f.SetInitialValues(fg1, initialStandardValue);
        }
        FriendGroup fg2 = new FriendGroup(g2, "fg2");
        for (Friend f : g2) {
            f.SetInitialValues(fg2, initialStandardValue);
        }
        activeFriendGroups.add(fg1);
        activeFriendGroups.add(fg2);
        f1.devoteInitialHours(fg1, 2);
        f1.devoteInitialHours(fg2, 3);
        f2.devoteInitialHours(fg1, 1);
        f2.devoteInitialHours(fg2, 4);
        f3.devoteInitialHours(fg1, 6);
        f3.devoteInitialHours(fg2, 2);
        f4.devoteInitialHours(fg2, 1);
        f5.devoteInitialHours(fg2, 5);
        f6.devoteInitialHours(fg2, 3);
        iterations = 0;
    }

    public void iterateNetwork() {
        // for (Friend f : activeFriends) {
        //     System.out.println(f.getName() + " is part of " + f.getNumGroupsMemberOf() + " groups.");
        // }
        System.out.println("\nActive Groups: ");
        for (FriendGroup fg : activeFriendGroups) {
            String memberString = fg.MemberString();
            System.out.println(fg.getName() + " with members: " + memberString);
        }
        System.out.println("Total active friends: " + activeFriends.size());
        //System.out.print(activeFriends.size() + ", ");
        System.out.println("----------------------------------------------");

        // Update Values and Gossip
        flowBackward();
        updateEdges();
        initiateGossiping();

        // Update Time
        flowForward();
        
        // Create destroy nodes
        DestroyNodes();
        CreateNodes();

        // repeat, potentially many times
        System.out.println("press enter to iterate again");
        input = this.scanner.nextLine();
        //iterations += 1;
        if (iterations < 100) {
            iterateNetwork();
        }
    }

    private void updateEdges() {
        for (Friend f : activeFriends) {
            f.updateValues();
        }
    }

    private void initiateGossiping() {
        for (Friend f : activeFriends) {
            f.gossip();
        }
    }

    public void flowBackward() {
        for (Friend f : activeFriends) {
            f.recieveValues();
        }
    }

    public void flowForward() {
        for (FriendGroup fg : activeFriendGroups) {
            fg.clear();
        }
        for (Friend f : activeFriends) {
            f.devoteHours();   
        }
    }
    
    public void DestroyNodes() {
        ArrayList<FriendGroup> disbandingGroups = new ArrayList<>();
        for (FriendGroup fg : activeFriendGroups) {
            if (fg.size() < minGroupSize) {
                disbandingGroups.add(fg);
            }
        }
        for (FriendGroup fg : disbandingGroups) {
            if (printDetails || simplePrintDetails) {
                System.out.println(fg.getName() + " has disbanded :(");
            }
            fg.deactivate();
            fg.loseDevotion();
            activeFriendGroups.remove(fg);
        }
        ArrayList<Friend> leavingFriends = new ArrayList<>();
        for (Friend f : activeFriends) {
            if (f.isLeaving()) {
                leavingFriends.add(f);
            } else if (f.wantsNewGroup()) {
                requestingNewGroup.add(f);
            }
        }
        for (Friend f : leavingFriends) {
            if (printDetails || simplePrintDetails) {
                System.out.println(f.getName() + " has left the network :(");
            }
            activeFriends.remove(f);
        }
    }

    public void CreateNodes() {
        Collections.sort(requestingNewGroup, (friend1, friend2) 
            -> Double.compare(friend1.getFreeTime(), friend2.getFreeTime()));
        ArrayList<Friend> foundingMembers = new ArrayList<>();
        while (requestingNewGroup.size() >= minGroupSize) {
            for (int i = 0; i < minGroupSize; i++) {
                foundingMembers.add(requestingNewGroup.get(0));
                requestingNewGroup.remove(0);
            }
            FriendGroup newGroup = new FriendGroup(foundingMembers, "fg" + Integer.toString(nextGroupCount));
            nextGroupCount += 1;
            activeFriendGroups.add(newGroup);
            String founders = "";
            for (Friend f : foundingMembers) {
                f.devoteInitialHours(newGroup, f.getFreeTime() / hesitance);
                f.SetInitialValues(newGroup, initialStandardValue);
            }
            for (Friend f : foundingMembers) {
                f.learnOfGroup(newGroup, newGroup.getValue(f.getFreeTime() / hesitance));
                founders += f.getName() + " ";
            }
            if (printDetails || simplePrintDetails) {
                System.out.println(founders + "just formed a new friend group called " + newGroup.getName());
            }
            foundingMembers = new ArrayList<>();
        }
        if (requestingNewGroup.size() > 0) {
            ArrayList<Friend> newFriends = new ArrayList<>();
            for (int i = 0; i < (minGroupSize - requestingNewGroup.size()); i++) {
                Friend newFriend = new Friend("f" + Integer.toString(nextFriendCount), printDetails);
                nextFriendCount += 1;
                activeFriends.add(newFriend);
                newFriends.add(newFriend);
                if (printDetails || simplePrintDetails) {
                    System.out.println("A new friend, " + newFriend.getName() + ", joins the network!");
                }
            }
            for (Friend f : newFriends) {
                requestingNewGroup.add(f);
            }
            CreateNodes();
        }
    }
}
