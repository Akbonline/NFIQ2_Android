package com.example.aug12_2;

public class HexUtils {
    private static final int ONE = 1;

    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] CHARS_TABLES = "0123456789ABCDEF".toCharArray();

    public static String
    toString(byte data) {

        return HexUtils.toString(new byte[]{data});
    }

    public static String
    toString(byte[] aBytes) {

        return HexUtils.toString(aBytes, 0, aBytes.length);
    }

    public static String
    toString(byte[] aBytes,
             int aOffset,
             int aLength) {

        char[] dst = new char[aLength * 2];

        for (int si = aOffset, di = 0; si < aOffset + aLength; si++) {
            byte b = aBytes[si];
            dst[di++] = CHARS_TABLES[(b & 0xf0) >>> 4];
            dst[di++] = CHARS_TABLES[(b & 0x0f)];
        }

        return new String(dst);
    }

    public static byte
    toByte(String str) {

        if (null == str || str.length() < 2)
            return 0x00;

        return (byte)
                ((Character.digit(str.charAt(0), 16) << 4) + (Character.digit(str.charAt(1), 16)));
    }

    public static byte[]
    toByteArray(String hexString) {

        if (HexUtils.ONE == hexString.length() % 2)
            return new byte[0];

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2)
            bytes[i / 2] = HexUtils.toByte(hexString.substring(i, i + 2));

        return bytes;
    }
}