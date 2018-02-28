package layout;

import com.inspira.lnj.LibInspira;

import static com.inspira.lnj.IndexInternal.global;

/**
 * Created by Arta on 01-Dec-17.
 */

public class ChatMsgContainer {

    private String id; // id msg
    private String from_id;
    private String from_nama;
    private String type; //msg, log
    private String msgType; // msg, pic, vid
    private String status; //send, delivered, read;
    private String message;
    private String idRoom; // id room
    private String sendTime;

    public static final String typeLOG = "0";
    public static final String typeMSG = "1";
    public static final String typeLOG_Full = "2";

    public static final String message_data_type_string = "0";
    public static final String message_data_type_picture = "1";
    public static final String message_data_type_video = "2";

    public static final String statusPraSend = "0";
    public static final String statusSend = "1";
    public static final String statusDelivered = "2";
    public static final String statusRead = "3";
    public static final String statusReadDelivered = "4";

    public static final String id_logDate = "logDate";
    public static final String id_UnreadMsg = "logUnreadMsg";

    public ChatMsgContainer() {}
    public ChatMsgContainer(String nama, String message, String type) {
        this.id = "0";
        this.from_nama = nama;
        this.type = type; //msg, log
        this.msgType = message_data_type_string; // msg, pic, vid
        this.status= "";
        this.message = message;
    }

    public ChatMsgContainer(String id,
                            String message_type,
                            String message,
                            String message_data_type,
                            String idroom,
                            String status,
                            String from_id,
                            String from_nama,
                            String sendTime) {

        this.id = id;
        this.idRoom = idroom;
        this.from_id = from_id;
        this.from_nama = from_nama;
        this.type = message_type;
        this.msgType = message_data_type;
        this.status = status;
        this.message = message;
        this.sendTime = sendTime;
    }

    //log constractor
    public ChatMsgContainer(String message, int logtype) {
        if(logtype == 1) {
            this.id = id_logDate;
            this.type = typeLOG;
            this.message = message;
        }
        else if (logtype == 2)
        {
            this.id = id_UnreadMsg;
            this.type = typeLOG_Full;
            this.message = message;
        }
    }

    public ChatMsgContainer (ChatMsgContainer newData)
    {
        this.id = newData.getId();
        this.idRoom = newData.getIdRoom();
        this.from_id = newData.getFrom_id();
        this.from_nama = newData.getFrom_nama();
        this.type = newData.getType();
        this.msgType = newData.getMsgType();
        this.status = newData.getStatus();
        this.message = newData.getMessage();
        this.sendTime = newData.getSendTime();
    }

    public void copy(ChatMsgContainer newData)
    {
        this.id = newData.getId();
        this.idRoom = newData.getIdRoom();
        this.from_id = newData.getFrom_id();
        this.from_nama = newData.getFrom_nama();
        this.type = newData.getType();
        this.msgType = newData.getMsgType();
        this.status = newData.getStatus();
        this.message = newData.getMessage();
        this.sendTime = newData.getSendTime();
    }

    public void copy(ChatMsgContainer newData, String MSGtype)
    {
        if(msgType.equals(ChatMsgContainer.message_data_type_picture))
        {
            this.id = newData.getId();
            this.idRoom = newData.getIdRoom();
            this.from_id = newData.getFrom_id();
            this.from_nama = newData.getFrom_nama();
            this.type = newData.getType();
            this.msgType = newData.getMsgType();
            this.status = newData.getStatus();
            //this.message = newData.getMessage();
            this.sendTime = newData.getSendTime();
        }
        else {
            this.id = newData.getId();
            this.idRoom = newData.getIdRoom();
            this.from_id = newData.getFrom_id();
            this.from_nama = newData.getFrom_nama();
            this.type = newData.getType();
            this.msgType = newData.getMsgType();
            this.status = newData.getStatus();
            this.message = newData.getMessage();
            this.sendTime = newData.getSendTime();
        }
    }

    public static boolean isYou(ChatMsgContainer data)
    {
        //mengecek apakah from id sama dengan user yang sedang login
        String iduser = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
        if(iduser.equals(data.getFrom_id()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getId() {return id;}
    public void setId(String _param) {this.id = _param;}

    public String getFrom_nama() {return from_nama;}
    public void setFrom_nama(String _param) {this.from_nama = _param;}

    public void setFrom_id(String idUser) {
        this.from_id = idUser;
    }
    public String getFrom_id() {
        return from_id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getMsgType() {
        return msgType;
    }
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public void setIdRoom(String room) {
        this.idRoom = room;
    }
    public String getIdRoom() {
        return idRoom;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
    public String getSendTime() {
        return sendTime;
    }
}
