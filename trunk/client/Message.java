/* -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: */


/*
    FSOG - Free Software Online Games
    Copyright (C) 2007 Bartlomiej Antoni Szymczak

    This file is part of FSOG.

    FSOG is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

/*
    You can contact the author, Bartlomiej Antoni Szymczak, by:
    - electronic mail: rhywek@gmail.com
    - paper mail:
        Bartlomiej Antoni Szymczak
        Boegesvinget 8, 1. sal
        2740 Skovlunde
        Denmark
*/


import java.util.*;

class Message{

    public static Vector<Byte> toVector(final byte[] rawData){
        final Vector<Byte> result=new Vector<Byte>();
        for(int i=0;i<rawData.length;i++)
            result.add(rawData[i]);
        return result;
    }

    public static byte[] toArray(final Vector<Byte> rawData){
        final byte[] result=new byte[rawData.size()];
        for(int i=0;i<result.length;i++)
            result[i]=rawData.get(i);
        return result;
    }

    /*
    public Iterator<Byte> iterator(){
        return this.data.iterator();
    }

    public Vector<Byte> getRawData(){
        return this.data;
    }
    */

    //Appends value to message
    public static void appendInteger(final long value,
                              final int numberOfBytes,
                              final Vector<Byte> message){
        for(int i=0;i<numberOfBytes;i++){
            message.add((byte)(0xFF & (value>>(8*(numberOfBytes-1-i)))));
        }
    }

    public static void append1Byte(final long value,final Vector<Byte> message){
        appendInteger(value,1,message);
    }

    public static void append2Bytes(final long value,final Vector<Byte> message){
        appendInteger(value,2,message);
    }

    public static void append3Bytes(final long value,final Vector<Byte> message){
        appendInteger(value,3,message);
    }

    public static void append4Bytes(final long value,final Vector<Byte> message){
        appendInteger(value,4,message);
    }

    public static void append8Bytes(final long value,final Vector<Byte> message){
        appendInteger(value,8,message);
    }

    public static void appendCString(final String string,final Vector<Byte> message){
        for(int i=0;i<string.length();i++)
            message.add((byte)string.charAt(i));
        message.add((byte)0);
    }

    public static void appendBinary(final Vector<Byte> value,
                             final Vector<Byte> message){
        final int length = value.size();
        if(value.size()>0x7FFF){
            //TODO: Better handling here.
            System.exit(3);
        }
        append2Bytes(value.size(),message);
        message.addAll(value);
    }

    //Read value from this message
    public static byte read1Byte(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            return iterator.next();
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }

    //Read value from this message
    public static short read2Bytes(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            short result=0;
            byte b;

            b = iterator.next();
            result|=(0xFF&(short)b)<<8;
            b = iterator.next();
            result|=(0xFF&(short)b);

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }

    //Read value from this message
    public static int read3Bytes(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            int result=0;
            byte b;

            b = iterator.next();
            result|=(0xFF&(int)b)<<16;
            b = iterator.next();
            result|=(0xFF&(int)b)<<8;
            b = iterator.next();
            result|=(0xFF&(int)b);

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }


    //Read value from this message
    public static int read4Bytes(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            int result=0;
            byte b;

            b = iterator.next();
            result|=(0xFF&(int)b)<<24;
            b = iterator.next();
            result|=(0xFF&(int)b)<<16;
            b = iterator.next();
            result|=(0xFF&(int)b)<<8;
            b = iterator.next();
            result|=(0xFF&(int)b);

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }
    
    //Read value from this message
    public static long read8Bytes(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            long result=0L;
            byte b;

            b = iterator.next();
            result|=(0xFFL&(long)b)<<56;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<48;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<40;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<32;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<24;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<16;
            b = iterator.next();
            result|=(0xFFL&(long)b)<<8;
            b = iterator.next();
            result|=(0xFFL&(long)b);

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }
    
    //Read value from this message
    public static String readCString(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{

            final StringBuffer stringBuffer
                = new StringBuffer();

            byte b;

            while((b=iterator.next())!=0)
                stringBuffer.append((char)b);

            return stringBuffer.toString();
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }

    //Read value from this message
    public static java.util.Vector<Byte> readBinary(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            final int incomingMessageLength
                = Message.read2Bytes(iterator);

            java.util.Vector<Byte> result = new java.util.Vector<Byte>();

            for(int i=0;i<incomingMessageLength;i++)
                result.add(iterator.next());

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }

    //Read value from this message
    public static java.util.Vector<Byte> readVector(Iterator<Byte> iterator)
        throws MessageDeserializationException
    {
        try{
            final int incomingMessageLength
                = Message.read2Bytes(iterator);

            java.util.Vector<Byte> result = new java.util.Vector<Byte>();
            
            for(int i=0;i<incomingMessageLength;i++)
                result.add(Message.read1Byte(iterator));

            return result;
        }catch(final NoSuchElementException e){
            throw new MessageDeserializationException(e);
        }
    }

    //Represent this message as string
    public static String toString(final Vector<Byte> message){

        final char[] hex =
            {'0','1','2','3','4','5','6','7',
             '8','9','a','b','c','d','e','f'};

        final StringBuffer output
            = new StringBuffer();

        output.append("Number of bytes: "+message.size()+"\n");

        output.append("Plain view:\n");

        for(int i=0; i<message.size()&&i<1024;i++){
            final byte b = message.get(i);
            if(b==' '||Character.isLetterOrDigit(b))
                output.append((char)b);
            else
                output.append('.');

            if(i%10==9)
                output.append("\n");
        }

        output.append("\nHex view:\n");

        for(int i=0; i<message.size()&&i<1024;i++){
            
            final byte b = message.get(i);

            output.append(""+hex[(b>>4) & 0x0F]+hex[b & 0x0F]+" ");

            if(i%10==9)
                output.append("\n");
        }
        
        return output.toString();
    }
}
