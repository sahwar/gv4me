/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ui;

import gvME.Logger;
import gvME.gvME;
import gvME.settings;
import gvME.textConvo;
import gvME.textMsg;
import gvME.tools;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author Matt Defenthaler
 */
public class MsgList extends List implements CommandListener {

    private final int itemLength = 17;
    private Vector MsgListToItemMap;
    private Command viewMsgCmd;
    private Command replyCmd;
    private Command backCmd;
    private Command fwdCmd;
    private Command callCmd;
    private Command msgPropsCmd;
    private Command replyReadCmd;
    private Form readMsg;
    private Form msgProps;
    private textConvo convo;

    public MsgList(textConvo crnt)
    {
        super(crnt.getSender(), List.IMPLICIT);
        addCommand(getViewMsgCmd());
        addCommand(getReplyCmd());
        addCommand(getFwdCmd());
        addCommand(getBackCmd());
        addCommand(getCallCmd());
        setCommandListener(this);
        setSelectCommand(getViewMsgCmd());

        this.convo = crnt;
        getMsgList(this.convo.getMessages());
    }

    private Form getReadMsg(String title)
    {
        if(readMsg == null)
        {
            readMsg = new Form(title);
            readMsg.addCommand(getReplyReadCmd());
            readMsg.addCommand(fwdCmd);
            readMsg.addCommand(getMsgPropsCmd());
            readMsg.addCommand(backCmd);
            readMsg.addCommand(getCallCmd());
            readMsg.setCommandListener(this);
        }
        else
        {
            readMsg.delete(0);
        }
        return readMsg;
    }

    private Form getMsgProps(textConvo msg)
    {
        msgProps = new Form("Properties");
        msgProps.addCommand(getBackCmd());
        msgProps.addCommand(getCallCmd());
        StringItem sender = new StringItem("Sender: ", msg.getSender());
        msgProps.append(sender);
        StringItem replyNum = new StringItem("Phone Number: ", msg.getReplyNum());
        msgProps.append(replyNum);
        StringItem date = new StringItem("Sent: ", msg.getMsg().getTimeReceived()+" "+msg.getDate());
        msgProps.append(date);
        msgProps.setCommandListener(this);
        return msgProps;
    }

    /**
     * Creates String representations of textMsg's and inserts them into the 
     * @param msgs Vector of textMsg's in the current textConvo
     */
    private void getMsgList(Vector msgs)
    {
        MsgListToItemMap = new Vector();
        Enumeration addMsgEnum = msgs.elements();
        textMsg crntMsg;
        StringBuffer itemBuff = new StringBuffer();
        while(addMsgEnum.hasMoreElements())
        {
            crntMsg = (textMsg) addMsgEnum.nextElement();
            itemBuff = new StringBuffer(crntMsg.getMessage());

            if(itemBuff.length() > itemLength)
            {
                itemBuff.setLength(itemLength);
                itemBuff.append("...");
            }
            MsgListToItemMap.insertElementAt(crntMsg, 0);
           // String itemBuffStr = new String(itemBuff);
            this.insert(0, new String(itemBuff), null);

        }
    }

    public void commandAction(Command command, Displayable displayable) {
        int selIndex = this.getSelectedIndex();
        textMsg original = null;

        if(displayable == this && command == backCmd)
        {
            try {
                MsgListToItemMap = null;
                gvME.dispMan.switchDisplayable(null, gvME.getInbox());
            } catch (IOException ex) {
                Logger.add(getClass().getName(), ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex) {
                Logger.add(getClass().getName(), ex.getMessage());
                ex.printStackTrace();
            }
        }
        else if(displayable == readMsg && command == backCmd)
        {
            gvME.dispMan.switchDisplayable(null, this);
        }
        else if(!MsgListToItemMap.isEmpty())
        {
            original = (textMsg) MsgListToItemMap.elementAt(selIndex);

            if(command == fwdCmd)
            {
                WriteMsg wm = new WriteMsg("Forward", convo);
                gvME.dispMan.switchDisplayable(null, wm);
            }
            else if(command == replyCmd)
            {
                WriteMsg wm = new WriteMsg("Reply", convo);
                gvME.dispMan.switchDisplayable(null, wm);
            }
            else if(command == callCmd)
            {
                try{
                    MakeCall mc = new MakeCall(convo.getReplyNum(), convo.getSender());
                    gvME.dispMan.switchDisplayable(null, mc);
                }
                catch(Exception ex)
                {
                    if(ex.toString().indexOf("no call from") >= 0)
                    {
                        gvME.dispMan.switchDisplayable(gvME.getNoCallFromAlert(), settings.getChangeSettingsMenu());
                    }
                }
            }
            else if(displayable == this && command == viewMsgCmd)
            {
                gvME.dispMan.switchDisplayable(null, getReadMsg(convo.getSender()));
                readMsg.append(tools.decodeString(original.getMessage())); //decodes string from utf8
            }
            else if(displayable == readMsg && command == msgPropsCmd)
            {
                textConvo propsConvo = convo;
                propsConvo.setLastMsg(original);
                gvME.dispMan.switchDisplayable(null, getMsgProps(propsConvo));
            }
            else if(displayable == readMsg && command == replyReadCmd)
            {
                WriteMsg wm = new WriteMsg("Reply", convo);
                gvME.dispMan.switchDisplayable(null, wm);
            }
            else if(displayable == msgProps && command == backCmd)
            {
                gvME.dispMan.switchDisplayable(null, this);
            }
        }
    }

    private Command getViewMsgCmd() {
        if (viewMsgCmd == null) {
            viewMsgCmd = new Command("Read", Command.ITEM, 1);
        }
        return viewMsgCmd;
    }

    private Command getCallCmd()
    {
        if(callCmd == null)
        {
            callCmd = new Command("Call", Command.ITEM, 2);
        }
        return callCmd;
    }

    private Command getBackCmd() {
        if (backCmd == null) {
            backCmd = new Command("Back", Command.BACK, 1);
        }
        return backCmd;
    }

    private Command getMsgPropsCmd() {
        if (msgPropsCmd == null) {
            msgPropsCmd = new Command("Properties", Command.ITEM, 3);
        }
        return msgPropsCmd;
    }

    private Command getReplyReadCmd()
    {
        if (replyReadCmd == null)
        {
            replyReadCmd = new Command("Reply", Command.ITEM, 2);
        }
        return replyReadCmd;
    }

    private Command getReplyCmd() {
        if (replyCmd == null) {
            replyCmd = new Command("Reply", Command.ITEM, 2);
        }
        return replyCmd;
    }

    private Command getFwdCmd() {
        if (fwdCmd == null) {
            fwdCmd = new Command("Forward", Command.ITEM, 3);
        }
        return fwdCmd;
    }
}
