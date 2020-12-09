package com.arma.web.util;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Key;

import javax.crypto.Cipher;

public class KeyUtil
{
    private static boolean _nokey;
    private static Cipher _cipher_en;
    private static Cipher _cipher_de;
    private static String _KEY_NAME;

    public static void setKey(String path)
    {
        _nokey = false;
        _cipher_en = null;
        _cipher_de = null;
        _KEY_NAME = path;
    }

    // return hex string, content - utf-8 string
    public static String encrypt(String content)
    {
        if (WebUtil.empty(content))
            return "";
        Cipher cipher = cipher4en();
        if (cipher == null)
            return "";
        try
        {
            byte[] arr = content.getBytes(WebUtil.CHARSET_UTF_8);
            byte[] bytes = cipher.doFinal(arr);
            return WebUtil.bytes2hexs(bytes);
        }
        catch (Exception e)
        {
        }
        return "";
    }

    // return utf-8 string, hexs - hex string
    public static String decrypt(String hexs)
    {
        if (WebUtil.empty(hexs))
            return "";
        Cipher cipher = cipher4de();
        if (cipher == null)
            return "";
        try
        {
            byte[] arr = WebUtil.hexs2bytes(hexs);
            byte[] bytes = cipher.doFinal(arr);
            return new String(bytes, WebUtil.CHARSET_UTF_8);
        }
        catch (Exception e)
        {
        }
        return "";
    }

    private static Cipher cipher4en()
    {
        if (_nokey || _cipher_en != null)
            return _cipher_en;
        Key key = readKey();
        if (key == null)
            return _cipher_en;
        try
        {
            _cipher_en = Cipher.getInstance("AES");
            _cipher_en.init(Cipher.ENCRYPT_MODE, key);
        }
        catch (Exception e)
        {
        }
        return _cipher_en;
    }

    private static Cipher cipher4de()
    {
        if (_nokey || _cipher_de != null)
            return _cipher_de;
        Key key = readKey();
        if (key == null)
            return _cipher_de;
        try
        {
            _cipher_de = Cipher.getInstance("AES");
            _cipher_de.init(Cipher.DECRYPT_MODE, key);
        }
        catch (Exception e)
        {
        }
        return _cipher_de;
    }

    private static Key readKey()
    {
        if (_nokey)
            return null;

        Key key = null;
        ObjectInputStream in = null;
        try
        {
            if (WebUtil.empty(_KEY_NAME))
                in = new ObjectInputStream(KeyUtil.class.getResourceAsStream("key.aes"));
            else if (WebUtil.exist(1, _KEY_NAME) != null)
                in = new ObjectInputStream(new FileInputStream(_KEY_NAME));
            if (in != null)
                key = (Key) in.readObject();
        }
        catch (Exception e)
        {
        }
        finally
        {
            WebUtil.closeIn(in);
        }
        _nokey = (key == null);
        return key;
    }

    public static boolean check(boolean encrypt)
    {
        if (encrypt)
        {
            String str = KeyUtil.encrypt("123");
            return ("1fb2508b5b2acfecaa159abd96bb7ce7".equals(str));
        }
        String str = KeyUtil.decrypt("1fb2508b5b2acfecaa159abd96bb7ce7");
        return ("123".equals(str));
    }

    public static void main(final String[] args)
    {
        System.out.println("encrypt valid - " + check(true));
        System.out.println("decrypt valid - " + check(false));
    }
}
