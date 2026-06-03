package com.caspian.pichak.utility;

import com.pb.ouc.util.util.Utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.Arrays;

public class MacKeyGenerator {
    private Utility utility = new Utility();


    public byte[] generateMac(byte[] key, byte[] data) {
        int loc = 0;
        byte[] edata = new byte[8];
        byte[] key1 = Arrays.copyOf(key, 8);
        byte[] pdata = this.addPadding(data);

        System.out.println("dataForMac length = " + data.length);
        System.out.println("pdata length      = " + pdata.length);
        System.out.println("pdata             = " + utility.bytesToHex(pdata));

        try {
            SecretKey ka = new SecretKeySpec(key1, "DES");
            Cipher cipherA = Cipher.getInstance("DES/CBC/NoPadding");
            cipherA.init(1, ka, new IvParameterSpec(new byte[8]));
            byte[] x = new byte[8];

            for(int var12 = 0; var12 < pdata.length; var12 += 8) {
                System.arraycopy(pdata, var12, x, 0, 8);
                byte[] y = this.xor_array(edata, x);
                edata = cipherA.doFinal(y);
            }

            return edata;
        } catch (Exception e) {
            AAAServer.logger.error("Error generateMac", e);
            return null;
        }
    }

    public boolean checkMac(byte[] key, byte[] data, int msgLen) {
        byte[] mac = new byte[key.length];
        System.arraycopy(data, data.length - mac.length, mac, 0, mac.length);
        byte[] pdata = new byte[data.length - (msgLen + key.length)];
        System.arraycopy(data, msgLen, pdata, 0, pdata.length);
        byte[] genMac = new byte[key.length];

        try {
            genMac = this.generateMac(key, pdata);
        } catch (Exception e) {
            AAAServer.logger.error("Error checkMac", e);
            boolean var8 = false;
        } finally {
            return this.utility.toHex(genMac).equals(this.utility.toHex(mac));
        }
    }

    private byte[] addPadding(byte[] in) {
        if (in.length % 8 == 0) {
            return in;
        } else {
            int extra = 8 - in.length % 8;
            int newLength = in.length + extra;
            byte[] out = Arrays.copyOf(in, newLength);

            for(int offset = in.length; offset < newLength; ++offset) {
                out[offset] = 0;
            }

            return out;
        }
    }

    private byte[] xor_array(byte[] aFirstArray, byte[] aSecondArray) {
        byte[] result = new byte[aFirstArray.length];

        for(int i = 0; i < result.length; ++i) {
            result[i] = (byte)(aFirstArray[i] ^ aSecondArray[i]);
        }

        return result;
    }
}
