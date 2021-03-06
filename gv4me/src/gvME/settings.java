/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gvME;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author Matt Defenthaler
 */
public class settings {
    private static final String userSettingsStore = "userSettingsStore";
    private static Form changeSettingsMenu;
    private static TextField passwordTextField, usernameTextField, callFromTextField, intervalTextField, pinTextField, pauseCharTextField, gvNumberTextField;
    private static Command saveSettingsCmd, backCmd;
    private static String username = "";
    private static String password = "";
    private static String interval = "60";
    private static String callFrom = "";
    private static String pin = "";
    private static ChoiceGroup callWithChoice;
    private static CommandListener cl;
    private static final int numFields = 8;
    private static final int MAX_CONTACTS = 10;
    private static Vector recentContacts;
    private static final int callWithData = 0;
    private static final int callWithVoice = 1;
    private static int callWith = 1;
    private static String gvNumber = "";
    private static String pauseChar = "p";
    private static final String DEFAULT_PAUSE_CHAR = "p";

    public static void initialize() throws IOException
    {
        settings.cl = cl;
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(userSettingsStore, true);
            if (rs.getNumRecords() != 0) {
                String[] settingsStr = serial.deserialize(numFields, rs.getRecord(1));
                setSettings(settingsStr);
                if (rs.getNumRecords() == 2) {
                    byte[] data = rs.getRecord(2);
                    recentContacts = serial.deserializeKVPVector(MAX_CONTACTS, data);
                }
            }
        } catch (RecordStoreException ex) {
            Logger.add("settings", "initialize", ex.getMessage());
            ex.printStackTrace();
        }
        finally{
            try{
                rs.closeRecordStore();
            }
            catch(Exception ignore){}
        }
    }

    private static void changeSettings() throws RecordStoreException
    {
        String tfInterval = intervalTextField.getString();
        String tfUsername = usernameTextField.getString();
        String tfPassword = passwordTextField.getString();
        String tfCallFrom = callFromTextField.getString();
        String tfPIN      = pinTextField.getString();
        String tfGVNumber = gvNumberTextField.getString();
        String tfPauseChar = pauseCharTextField.getString();
        callWith = callWithChoice.getSelectedIndex();

        if(!tfInterval.equals(interval))
        {
            interval = tfInterval;
            gvME.cancelTimer();
            if(Integer.parseInt(tfInterval) > 0)
                gvME.createTimer();
        }
        if(!tfUsername.equals(username))
        {
            username = tfUsername;
        }
        if(!tfPassword.equals(""))
        {
            password = tfPassword;
        }
        if(!tfCallFrom.equals(callFrom))
        {
            settings.callFrom = tfCallFrom;
        }
        if(!tfPIN.equals(pin))
        {
            settings.pin = tfPIN;
        }
        if(tfPauseChar != null && !tfPauseChar.equals(""))
        {
            settings.pauseChar = tfPauseChar;
        }
        else
        {
            settings.pauseChar = DEFAULT_PAUSE_CHAR;
        }
        if(!tfGVNumber.equals(gvNumber))
        {
            settings.gvNumber = tfGVNumber;
        }
        updateSettings();
    }

    private static Command getSaveSettingsCmd() {
        if (saveSettingsCmd == null) {
            saveSettingsCmd = new Command("Save", Command.OK, 1);
        }
        return saveSettingsCmd;
    }

    private static Command getBackCmd()
    {
        if(backCmd == null)
        {
            backCmd = new Command("Back", Command.BACK, 0);
        }
        return backCmd;
    }

    public static Form getChangeSettingsMenu() {
        if (changeSettingsMenu == null) {
            changeSettingsMenu = new Form("Change Settings", new Item[] { getUsernameTextField(),
                                                                            getPasswordTextField(),
                                                                            getCallFromTextField(),
                                                                            getIntervalTextField(),
                                                                            getPINTextField(),
                                                                            getCallWithChoice(),
                                                                            getPauseCharTextField(),
                                                                            getGVNumberTextField() });
            changeSettingsMenu.addCommand(getSaveSettingsCmd());
            changeSettingsMenu.addCommand(getBackCmd());
            changeSettingsMenu.setCommandListener(new CommandListener() {

                public void commandAction(Command command, Displayable displayable) {
                    if(command == saveSettingsCmd)
                    {
                        try {
                            changeSettings();
                            gvME.dispMan.showMenu();
                        } catch (RecordStoreException ex) {
                            ex.printStackTrace();
                        }
                    }
                    else if(command == backCmd)
                    {
                        gvME.dispMan.showMenu();
                    }
                }
            });
        }
        return changeSettingsMenu;
    }

    private static TextField getUsernameTextField() {
        if (usernameTextField == null) {
            usernameTextField = new TextField("Username:", username, 40, TextField.ANY);
        }
        return usernameTextField;
    }

    private static TextField getPasswordTextField() {
        if (passwordTextField == null) {
            passwordTextField = new TextField("Password:", null, 40, TextField.PASSWORD);
        }
        return passwordTextField;
    }

    private static TextField getIntervalTextField() {
        if (intervalTextField == null) {
            intervalTextField = new TextField("Check Inbox (secs). 0 for never", interval, 10, TextField.NUMERIC);
        }
        return intervalTextField;
    }

    private static TextField getPINTextField() {
        if (pinTextField == null) {
            pinTextField = new TextField("PIN:", pin, 6, TextField.NUMERIC);
        }
        return pinTextField;
    }

    private static TextField getCallFromTextField() {
        if (callFromTextField == null) {
            callFromTextField = new TextField("Phone's Number:", callFrom, 15, TextField.PHONENUMBER);
        }
        return callFromTextField;
    }

    private static TextField getPauseCharTextField() {
        if (pauseCharTextField == null) {
            pauseCharTextField = new TextField("Pause Symbol:", "", 3, TextField.ANY);//.PHONENUMBER);
        }
        return pauseCharTextField;
    }

    private static TextField getGVNumberTextField() {
        if (gvNumberTextField == null) {
            gvNumberTextField = new TextField("GV Number:", gvNumber, 10, TextField.PHONENUMBER);
        }
        return gvNumberTextField;
    }

    private static ChoiceGroup getCallWithChoice()
    {
        if (callWithChoice == null)
        {
            callWithChoice = new ChoiceGroup("Call With", Choice.EXCLUSIVE);
            callWithChoice.insert(callWithData, "Data", null);
            callWithChoice.insert(callWithVoice, "Voice", null);
            callWithChoice.setSelectedIndex(callWith, true);
        }
        return callWithChoice;
    }

    public static void setSettings(String[] fields)
    {
        if(fields != null)
        {
            settings.username = fields[0];
            settings.password = fields[1];
            settings.interval = fields[2];
            settings.callFrom = fields[3];
            settings.pin      = fields[4];
            settings.gvNumber = fields[5];
            settings.pauseChar= fields[6];
            settings.callWith = Integer.parseInt(fields[7]);
        }
    }

    public static boolean callOutInfoExists()
    {
       return !((getCallWith() == callWithData && callFrom.equals("")) ||
                (getCallWith() == callWithVoice && gvNumber.equals("")));
    }

    public static int getNumFields()
    {
        return numFields;
    }

    public static String getCheckInterval()
    {
        return settings.interval;
    }

    public static String getUsername()
    {
        return settings.username;
    }

    public static String getPassword()
    {
        return settings.password;
    }

    public static String getCallFrom()
    {
        return callFrom;
    }

    public static int getCallWith()
    {
        return callWith;
    }

    public static int getCallWithVoice()
    {
        return callWithVoice;
    }

    public static int getCallWithData()
    {
        return callWithData;
    }

    public static String getPIN()
    {
        return pin;
    }

    public static String getPauseChar()
    {
        return pauseChar;
    }

    public static String getGVNumber()
    {
        return gvNumber;
    }

    public static void setGVNumber(String number)
    {
        settings.gvNumber = number;
    }

    public static void setCheckInterval(int interval)
    {
        settings.interval = String.valueOf(interval);
    }

    public static void setUsername(String username)
    {
        settings.username = username;
    }

    public static void setPassword(String password)
    {
        settings.password = password;
    }
//
//    public void setCallFrom(String callFrom)
//    {
//        this.callFrom = callFrom;
//    }

    public static Vector getRecentContacts()
    {
        if(recentContacts == null)
        {
            recentContacts = new Vector();
        }
        return recentContacts;
    }

    public static void updateContactOrder(int index) throws RecordStoreException, IOException
    {
        KeyValuePair crnt = (KeyValuePair)getRecentContacts().elementAt(index);
        getRecentContacts().insertElementAt(crnt, 0);
        getRecentContacts().removeElementAt(index+1);
        updateContacts();
    }

    public static void addContact(KeyValuePair contact) throws RecordStoreException, IOException
    {
        if(getRecentContacts().indexOf(contact) < 0)
        {
            getRecentContacts().insertElementAt(contact, 0);
            if(getRecentContacts().size() > MAX_CONTACTS)
                getRecentContacts().setSize(MAX_CONTACTS);
            updateContacts();
        }
    }

    public static void updateContacts() throws RecordStoreException, IOException
    {
        RecordStore rs = null;
        try{
            byte[] data = serial.serializeKVPVector(getRecentContacts());
            rs = RecordStore.openRecordStore(userSettingsStore, true);
            if(rs.getNumRecords() != 0){
                try{
                    rs.setRecord(2, data, 0, data.length);
                }
                catch(InvalidRecordIDException ire)
                {
                    rs.addRecord(data, 0, data.length);
                }
            }
        }
        catch(RecordStoreException rse)
        {
            Logger.add("settings", "updateSettings", rse.getMessage());
            rse.printStackTrace();
        }
        finally{
            try{
                rs.closeRecordStore();
            }
            catch(Exception ignore){}
        }
    }

    public static void updateSettings() throws RecordStoreException
    {
        RecordStore rs = null;
        try{
            String[] fields = {username, password, interval, callFrom, pin, gvNumber, pauseChar, String.valueOf(callWith)};
            byte[] data = null;
            data = serial.serialize(fields);
            rs = RecordStore.openRecordStore(userSettingsStore, true);
            if(rs.getNumRecords() != 0)
            {
                rs.setRecord(1, data, 0, data.length);
            }
            else
            {
                rs.addRecord(data, 0, data.length);
            }
        }
        catch(Exception ex)
        {
            Logger.add("settings", "updateSettings", ex.getMessage());
            ex.printStackTrace();
        }
        finally{
            rs.closeRecordStore();
        }
    }
}
