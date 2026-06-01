package com.caspian.pichak.utility.iso;


import com.caspian.pichak.model.dto.ISOMessageDTO;
import com.caspian.pichak.utility.MacKeyGenerator;
import com.caspian.pichak.utility.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pb.ouc.util.util.Utility;
import lombok.NonNull;

import javax.xml.datatype.DatatypeConfigurationException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

public class IsoParser {
    private @NonNull String hexKey;
    private Utility utility = new Utility();
    private Util util = new Util();

    private IsoParser() {
    }

    public IsoParser(String hexKey) {
        this.hexKey = hexKey;
    }

    public static void main(String[] args) {
        new IsoParser();
        Utility utility = new Utility();
        byte[] msg = utility.hexToBytes("002866610200D66353000399B7112233445566778811122233344455500000000000EFF4DABA0876F262");
        IsoParser isoParser = new IsoParser();
        ISOMessageDTO isoMessageDTO = isoParser.parsIsoMessage(msg);
        System.out.printf(new String(msg));
    }

    public String createBlockWithLength(String text) {
        return text.length() + text;
    }

    private String subMsg(byte[] msg, int index, int length) {
        byte[] value = new byte[length];
        System.arraycopy(msg, index, value, 0, length);
        String retStr = String.valueOf((new BigInteger(value)).intValue());
        byte[] var6 = null;
        return retStr;
    }

    private String subMsgHex(byte[] msg, int index, int length) {
        byte[] value = new byte[length];
        System.arraycopy(msg, index, value, 0, length);
        String retStr = this.utility.toHex(value);
        byte[] var6 = null;
        return retStr;
    }

    private String subMsgStr(byte[] msg, int index, int length) {
        byte[] value = new byte[length];
        System.arraycopy(msg, index, value, 0, length);
        String retStr = new String(value);
        byte[] var6 = null;
        return retStr;
    }

    private String subMsgInt(byte[] msg, int index, int length) {
        byte[] value = new byte[length];
        System.arraycopy(msg, index, value, 0, length);
        BigInteger retStr = new BigInteger(value);
        byte[] var6 = null;
        return retStr.toString();
    }

    private JsonObject parsPayload(byte[] msg, int code) {
        JsonObject jsonObject = new JsonObject();
        switch (code) {
            case 2:
                jsonObject.addProperty("sayadId", this.subMsgHex(msg, 0, 8));
            case 3:
            case 5:
            case 7:
            case 9:
            case 11:
            default:
                break;
            case 4: {
                int index = 0;
                jsonObject.addProperty("customerNationalType", this.subMsgInt(msg, index, 1));
                ++index;
                int ncLen = (new BigInteger(this.subMsgInt(msg, index, 1))).intValue();
                ++index;
                jsonObject.addProperty("customerNationalCode", this.subMsgStr(msg, index, ncLen));
                break;
            }
            case 6:{
                int index = 0;
                jsonObject.addProperty("sayadId", this.subMsgHex(msg, index, 8));
                index += 8;
                jsonObject.addProperty("amount", this.subMsgInt(msg, index, 8));
                index += 8;
                jsonObject.addProperty("date", this.util.setDateFormat(this.subMsgInt(msg, index, 4)));
                index += 4;
                jsonObject.addProperty("iban", "IR" + this.subMsgHex(msg, index, 12));
                index += 12;
                jsonObject.addProperty("reason", this.subMsgStr(msg, index, 4));
                index += 4;
                jsonObject.addProperty("chequeType", this.subMsgInt(msg, index, 1));
                ++index;
                int personalInfoCount = (new BigInteger(this.subMsgInt(msg, index, 1))).intValue();
                ++index;
                JsonArray receivers = new JsonArray(personalInfoCount);

                for(int i = 0; i < personalInfoCount; ++i) {
                    JsonObject receiver = new JsonObject();
                    int nameLen = (new BigInteger(this.subMsgInt(msg, index, 1))).intValue();
                    ++index;
                    receiver.addProperty("name", this.subMsgStr(msg, index, nameLen));
                    int var37 = index + nameLen;
                    int nationalCodeLen = (new BigInteger(this.subMsgInt(msg, var37, 1))).intValue();
                    ++var37;
                    receiver.addProperty("customerID", this.subMsgStr(msg, var37, nationalCodeLen));
                    int var39 = var37 + nationalCodeLen;
                    receiver.addProperty("shahabId", this.subMsgHex(msg, var39, 8));
                    index = var39 + 8;
                    receiver.addProperty("nationalCodeType", this.subMsgInt(msg, index, 1));
                    ++index;
                    receivers.add(receiver);
                }

                jsonObject.add("receivers", receivers);
                break;
            }
            case 8: {
                int index = 0;
                jsonObject.addProperty("sayadId", this.subMsgHex(msg, 0, 8));
                index += 8;
                jsonObject.addProperty("accept", this.subMsgInt(msg, index, 1));
                break;
            }
            case 10: {
                int index = 0;
                jsonObject.addProperty("sayadId", this.subMsgHex(msg, index, 8));
                index += 8;
                jsonObject.addProperty("accept", this.subMsgInt(msg, index, 1));
                ++index;
                jsonObject.addProperty("giveBack", this.subMsgInt(msg, index, 1));
                ++index;
                jsonObject.addProperty("iban", "IR" + this.subMsgHex(msg, index, 12));
                index += 12;
                jsonObject.addProperty("reason", this.subMsgStr(msg, index, 4));
                index += 4;
                jsonObject.addProperty("chequeType", this.subMsgInt(msg, index, 1));
                ++index;
                int personalInfoCountTransfer = (new BigInteger(this.subMsgInt(msg, index, 1))).intValue();
                ++index;
                JsonArray receiversTransfer = new JsonArray(personalInfoCountTransfer);

                for (int i = 0; i < personalInfoCountTransfer; ++i) {
                    JsonObject receiver = new JsonObject();
                    int nameLen = (new BigInteger(this.subMsgInt(msg, index, 1))).intValue();
                    ++index;
                    receiver.addProperty("name", this.subMsgStr(msg, index, nameLen));
                    int var22 = index + nameLen;
                    int nationalCodeLen = (new BigInteger(this.subMsgInt(msg, var22, 1))).intValue();
                    ++var22;
                    receiver.addProperty("customerID", this.subMsgStr(msg, var22, nationalCodeLen));
                    int var24 = var22 + nationalCodeLen;
                    receiver.addProperty("shahabId", this.subMsgHex(msg, var24, 8));
                    index = var24 + 8;
                    receiver.addProperty("nationalCodeType", this.subMsgInt(msg, index, 1));
                    ++index;
                    receiversTransfer.add(receiver);
                }

                jsonObject.add("receivers", receiversTransfer);
                break;
            }
            case 12:
                jsonObject.addProperty("sayadId", this.subMsgHex(msg, 0, 8));
        }

        return jsonObject;
    }


    public  ISOMessageDTO parsIsoMessage(byte[] isoMsg) {
        ISOMessageDTO isoMessageDTO = new ISOMessageDTO();
        MacKeyGenerator macKeyGenerator = new MacKeyGenerator();
        if (macKeyGenerator.checkMac(utility.hexToBytes(hexKey), isoMsg, 2)) {
            int index = 0;
            isoMessageDTO.setTotalLength(Integer.parseInt(this.subMsg(isoMsg, index, 2)));
            index += 2;
            isoMessageDTO.setLocale(this.subMsgStr(isoMsg, index, 2));
            index += 2;
            isoMessageDTO.setServiceCode(Integer.parseInt(this.subMsg(isoMsg, index, 1)));
            ++index;
            isoMessageDTO.setDate(this.subMsg(isoMsg, index, 4));
            index += 4;
            isoMessageDTO.setTime(this.subMsg(isoMsg, index, 4));
            index += 4;
            isoMessageDTO.setPan(this.subMsgHex(isoMsg, index, 8));
            index += 8;
            byte[] payload = new byte[isoMsg.length - index];
            System.arraycopy(isoMsg, index, payload, 0, payload.length);
            isoMessageDTO.setBody(this.parsPayload(payload, isoMessageDTO.getServiceCode()));
        }

        return isoMessageDTO;
    }

    public byte[] makeResponse(String msg, ISOMessageDTO isoMessageDTO, String errorCode) throws DatatypeConfigurationException, ParseException {
        byte[] bMsg = new byte[0];
        if (errorCode.equals("0")) {
            bMsg = this.utility.hexToBytes(msg);
        }

        String[] dateTime = this.utility.getPersianDateTime(new Date()).split(" ");
        byte[] date = (new BigInteger(dateTime[0].replaceAll("/", ""))).toByteArray();
        byte[] time = this.addPadding((new BigInteger(dateTime[1].replaceAll(":", ""))).toByteArray(), 4);
        byte[] locale = isoMessageDTO.getLocale().getBytes(StandardCharsets.UTF_8);
        byte[] serviceCode = (new BigInteger(String.valueOf(isoMessageDTO.getServiceCode()))).toByteArray();
        byte[] error = (new BigInteger(errorCode)).toByteArray();
        byte[] rrn = (new BigInteger(String.valueOf(isoMessageDTO.getRrn()))).toByteArray();
        rrn = this.utility.addPadding(rrn, 8);
        int len = bMsg.length + date.length + time.length + locale.length + serviceCode.length + error.length + 8;
        byte[] data = new byte[len];
        int index = 0;
        System.arraycopy(locale, 0, data, index, locale.length);
        index += locale.length;
        System.arraycopy(serviceCode, 0, data, index, serviceCode.length);
        index += serviceCode.length;
        System.arraycopy(date, 0, data, index, date.length);
        index += date.length;
        System.arraycopy(time, 0, data, index, time.length);
        index += time.length;
        System.arraycopy(error, 0, data, index, error.length);
        index += error.length;
        System.arraycopy(rrn, 0, data, index, rrn.length);
        index += rrn.length;
        System.arraycopy(bMsg, 0, data, index, bMsg.length);

        for(int var10000 = index + bMsg.length; len % 8 != 0; ++len) {
        }

        byte[] padData = new byte[len];
        index = 0;
        System.arraycopy(data, 0, padData, index, data.length);
        int var33 = index + date.length;
        MacKeyGenerator macKeyGenerator = new MacKeyGenerator();
        byte[] mac = macKeyGenerator.generateMac(this.utility.hexToBytes(this.hexKey), padData);
        len += mac.length;
        byte[] totalLen = new byte[2];
        totalLen = this.addPadding((new BigInteger(String.valueOf(len))).toByteArray(), totalLen.length);
        byte[] retMsg = new byte[len + totalLen.length];
        index = 0;
        System.arraycopy(totalLen, 0, retMsg, index, totalLen.length);
        index += totalLen.length;
        System.arraycopy(padData, 0, retMsg, index, padData.length);
        index += padData.length;
        System.arraycopy(mac, 0, retMsg, index, mac.length);
        var33 = index + mac.length;
        return retMsg;
    }

    private byte[] addPadding(byte[] in, int len) {
        if (in.length == len) {
            return in;
        } else {
            int extra = len - in.length % len;
            int newLength = in.length + extra;
            byte[] out = new byte[len];

            int offset;
            for(offset = 0; offset < newLength - in.length; ++offset) {
                out[offset] = 0;
            }

            System.arraycopy(in, 0, out, offset, in.length);
            return out;
        }
    }
}

