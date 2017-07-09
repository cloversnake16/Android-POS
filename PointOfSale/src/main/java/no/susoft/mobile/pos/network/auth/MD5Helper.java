/*
 * Copyright (c) 2016. SuSoft AS. All rights reserved.
 */

package no.susoft.mobile.pos.network.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created on 18.10.2016.
 */
class MD5Helper {

    private final static String[] strDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;

        if (iRet < 0) {
            iRet += 256;
        }

        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    private static String byteToString(byte[] bByte) {
        StringBuilder sBuffer = new StringBuilder();
        for (byte aBByte : bByte) sBuffer.append(byteToArrayString(aBByte));
        return sBuffer.toString();
    }

    public static String encrypt(String strObj) throws NoSuchAlgorithmException {
        return byteToString(MessageDigest.getInstance("MD5").digest(strObj.getBytes()));
    }
}
