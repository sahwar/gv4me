/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gvME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author Matt Defenthaler
 */
public class serial {

    public synchronized static Vector deserializeKVPVector(int MAX, byte[] data)
    {
        String[] strArray = null;
        Vector vect = new Vector(10);
        try
        {
            strArray = deserialize(2*MAX, data);
        }
        catch(Exception e)
        {}

        int strLen = strArray.length;
        for(int i = 0; i < strLen && strArray[i] != null; i++)
        {
            vect.addElement(new KeyValuePair(strArray[i],strArray[++i]));
        }

        return vect;
    }

    public static byte[] serializeKVPVector(Vector vect) throws IOException
    {
        KeyValuePair kvp;
        int vectSize = vect.size();
        String[] strArray = new String[2*vectSize];
        
        for(int i = 0, j = 0; j < vectSize; i+=2, j++)
        {
            kvp = (KeyValuePair) vect.elementAt(j);
            strArray[i] = (String) kvp.getKey();
            strArray[i+1] = (String) kvp.getValue();
        }
        byte[] data = serialize(strArray);

        return data;
    }

    public synchronized static String[] deserialize(int numFields, byte[] data) throws IOException
    {
        String[] fields = new String[numFields];
        ByteArrayInputStream bis = null;
        DataInputStream dis = null;
        try {
            bis = new ByteArrayInputStream(data);
            dis = new DataInputStream(bis);

            for(int i=0; i < numFields; i++)
            {
                fields[i] = dis.readUTF();
            }
        }
        catch(IOException ex) {
            throw ex;
        }
        finally{
            dis.close();
            bis.close();

            return fields;
        }
    }

    /*
     * Serializes textMsg properties to an array of bytes.
     * @return array of bytes representing serialized setting.
     */
    public synchronized static byte[] serialize(String[] fields) throws IOException {
        byte[] data = null;
        ByteArrayOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);

            int fieldLen = fields.length;
            for(int i=0; i < fieldLen; i++)
            {
                dos.writeUTF(fields[i]);
            }
            data = bos.toByteArray();
        } catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
        finally{
            dos.flush();
            dos.close();
            bos.close();
            return data;
        }
    }
}
