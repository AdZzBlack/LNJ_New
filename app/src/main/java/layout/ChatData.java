package layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arta on 10-Dec-17.
 */

//## holder class untuk save 1 data room dan list semua message yang room nya sama dengan itu
// pemisah berdasarkan room id
public class ChatData {
    List<ChatMsgContainer> chatMsgData = new ArrayList<>();
    roomInfo mroomInfo = new roomInfo();

    public ChatData()
    {
        mroomInfo = new roomInfo();
        chatMsgData = new ArrayList<>();
    }

    public ChatData(ChatData.roomInfo roominfo)
    {
        mroomInfo.copy(roominfo);
        chatMsgData = new ArrayList<>();
    }

    public ChatData(ChatData newChatData)
    {
        // create by pointer
        this.chatMsgData = newChatData.getChatMsgData();
        this.mroomInfo = newChatData.getMroomInfo();
    }

    public ChatData(roomInfo roominfo, List<ChatMsgContainer> chatMsgData)
    {
        this.mroomInfo.copy(roominfo);

        for(ChatMsgContainer temp : chatMsgData)
        {
            if(temp.getIdRoom().equals(this.getMroomInfo().getIdRoom()))
            {
                this.chatMsgData.add(new ChatMsgContainer(temp));
            }
        }
    }

    public void replaceAllData(roomInfo _roominfo, List<ChatMsgContainer> _ListChatMsgData)
        {
        replaceRoomInfo(_roominfo);
        replaceChatMsgData(_ListChatMsgData);
    }
    public void replaceRoomInfo(roomInfo roominfo)
    {
        this.mroomInfo.copy(roominfo);
    }
    public void replaceChatMsgData(List<ChatMsgContainer> _ListChatMsgData)
    {
        // repalce msg, sdh ada pengecekan jika id ga ketemu brati add baru
        //this.chatMsgData.clear(); // jngan di clear nanti data lama ilang semua
        for(ChatMsgContainer temp : _ListChatMsgData)
        {
//            Log.d("ggg",temp.getIdRoom());
//            Log.d("ggg",this.getMroomInfo().getIdRoom());
            if(temp.getIdRoom().equals(this.getMroomInfo().getIdRoom()))
            {
                boolean flag = false;
                for(int i = 0;i<getChatMsgData().size();i++)
                {
                    if(getChatMsgData().get(i).getId().equals(temp.getId()))
                    {
//                        if(getChatMsgData().get(i).getMsgType().equals(ChatMsgContainer.message_data_type_picture)
//                                && ChatMsgContainer.isYou(getChatMsgData().get(i)))
//                        {
//                            getChatMsgData().get(i).copy(temp,ChatMsgContainer.message_data_type_picture);
//                        }
//                        else {
//                            //replace
//                            getChatMsgData().get(i).copy(temp);
//                        }
                        getChatMsgData().get(i).copy(temp);
                        flag = true;
                        break;
                    }
                }

                if(!flag)
                {
                    //klo ga nemu add
                    this.chatMsgData.add(new ChatMsgContainer(temp));
                }
            }
        }
    }

    public void setChatMsgData(List<ChatMsgContainer> chatMsgData) {
        this.chatMsgData = new ArrayList<>(chatMsgData);
    }
    public List<ChatMsgContainer> getChatMsgData() {
        return chatMsgData;
    }

    public void setMroomInfo(roomInfo mroomInfo) {
        this.mroomInfo.copy(mroomInfo);
    }
    public roomInfo getMroomInfo() {
        return mroomInfo;
    }




    public static class roomInfo
    {
        public static String roomTypePC = "PC";
        public static String roomTypeGC = "GC";

        private String idRoom;
        private String idUser;
        private String roomName;
        private String type;
        private String creator;
        private String createdDate;
        private String member;
        private List<String> listMember = new ArrayList<>();

        public roomInfo()
        {
            idRoom = "";
            idUser  = "";
            roomName  = "";
            type  = "";
            creator  = "";
            createdDate  = "";
            member = "";
            listMember = new ArrayList<>();
        }
        public roomInfo(String idRoom,
                        String idUser,
                        String roomName,
                        String type,
                        String creator,
                        String createdDate,
                        String member)
        {
            this.idRoom = idRoom;
            this.idUser  = idUser;
            this.roomName  = roomName;
            this.type  = type;
            this.creator  = creator;
            this.createdDate  = createdDate;
            this.member = member;
            setListMember(this.member,"::");
        }

        public void copy(roomInfo newData)
        {
            idRoom = newData.getIdRoom();
            idUser  = newData.getIdUser();
            roomName  = newData.getRoomName();
            type  = newData.getType();
            creator  = newData.getCreator();
            createdDate  = newData.getCreatedDate();
            member = newData.getMember();
            setListMember(member,"::");
        }

        public String getIdRoom() {
            return idRoom;
        }
        public void setIdRoom(String id_room) {
            this.idRoom = id_room;
        }

        public void setIdUser(String idUser) {
            this.idUser = idUser;
        }
        public String getIdUser() {
            return idUser;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }
        public String getRoomName() {
            return roomName;
        }

        public void setType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
        public String getCreator() {
            return creator;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
        public String getCreatedDate() {
            return createdDate;
        }

        public void setMember(String member) {
            this.member = member;
        }
        public String getMember() {
            return member;
        }

        public void setListMember(List<String> listMember) {
            this.listMember = new ArrayList<>(listMember);
        }
        public void setListMember(String JsonDataMember,String splitBy) {
            //split by
            listMember.clear();
            String[] pieces = JsonDataMember.trim().split("\\"+splitBy);
            if(pieces.length != 0) {
                for (String data : pieces) {
                    // loop
                    if (!data.equals("")) {
                        // add di member
                        listMember.add(data);
                    }
                }
            }

        }
        public List<String> getListMember() {
            return listMember;
        }
    }

}
