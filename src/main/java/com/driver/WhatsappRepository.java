package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public  String createUser(String name, String mobile) throws Exception{
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        else{
            User user = new User(name, mobile);
            userMobile.add(mobile);
            return "SUCCESS";
        }
    }
    public Group createGroup(List<User> users){
        int size  = users.size();
        Group group;
        if(size == 2){
            String name = users.get(1).getName();
            group = new Group(name,2);

        }
        else{
            this.customGroupCount++;
            String name = "Group "+this.customGroupCount;
            group = new Group(name,size);

        }
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        groupMessageMap.put(group,new ArrayList<>());
        return group;
    }
    public int createMessage(String content){
        this.messageId++;
        //Date date = new Date();
        Message message = new Message(this.messageId,content);
        return this.messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        else{
            List<User> users = groupUserMap.get(group);
            if(!users.contains(sender)){
                throw new Exception("You are not allowed to send message");
            }
            List<Message> msg = groupMessageMap.get(group);
            msg.add(message);
            senderMap.put(message,sender);
            return msg.size();
        }

    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!groupUserMap.containsKey(group)){
            throw  new Exception("Group does not exist");
        }
        else if(!adminMap.get(group).equals(approver)){
            throw  new Exception("Approver does not have rights");
        }
        else if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        else{
            adminMap.put(group,user);
        }
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        Group group  = null;
        int count_msg = 0;
        int res = 0;
        for(Group g : groupUserMap.keySet()){
            List<User> users = groupUserMap.get(g);
            if(users.contains(user)){
                group = g;

                break;
            }
        }
        if(group == null){
            throw new Exception("User not found");
        }
        else if(adminMap.get(group).equals(user)){
            throw new Exception("Cannot remove admin");
        }
        else{
            for(Message m : senderMap.keySet()){
                if(senderMap.get(m).equals(user)){
                    for(Group g : groupMessageMap.keySet()){
                        List<Message> msg  = groupMessageMap.get(g);
                        if(msg.contains(m)){ msg.remove(m);
                            count_msg++;
                        }
                    }

                    senderMap.remove(m);
                }
            }
            groupUserMap.get(group).remove(user);

            res+=groupUserMap.get(group).size();
            res+=groupMessageMap.get(group).size();
            res = res + (this.messageId-count_msg);
            return res;
        }

    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> messages = new ArrayList<>(senderMap.keySet());
        for(Message m : messages){
            if(m.getTimestamp().compareTo(start) <= 0 || m.getTimestamp().compareTo(end) >= 0){
                messages.remove(m);
            }
        }
        if(messages.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(messages ,(a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        return messages.get(messages.size()-K).getContent();
    }
}