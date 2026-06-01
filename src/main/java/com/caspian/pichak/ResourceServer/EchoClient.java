package com.caspian.pichak.ResourceServer;

import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.MacKeyGenerator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EchoClient {

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public static void main(String[] args) {
        EchoClient client = new EchoClient();
        client.startConnection("127.0.0.1", 8001);
        byte[] local = "fa".getBytes(StandardCharsets.UTF_8);
        byte[] serviceCode = (new BigInteger("4")).toByteArray();
        byte[] date = (new BigInteger("14001129")).toByteArray();
        byte[] time = addPadding((new BigInteger("143030")).toByteArray(), 4);
        byte[] pan = client.hexToBytes("6221061069982116");
        byte[] nationalCode = "2259639461".getBytes();
        byte[] isoMsg = new byte[1024];
        int index = 0;
        System.arraycopy(local, 0, isoMsg, index, local.length);
        index += local.length;
        System.arraycopy(serviceCode, 0, isoMsg, index, serviceCode.length);
        index += serviceCode.length;
        System.arraycopy(date, 0, isoMsg, index, date.length);
        index += date.length;
        System.arraycopy(time, 0, isoMsg, index, time.length);
        index += time.length;
        System.arraycopy(pan, 0, isoMsg, index, pan.length);
        index += pan.length;
        byte[] ncType = (new BigInteger("1")).toByteArray();
        System.arraycopy(ncType, 0, isoMsg, index, ncType.length);
        index += ncType.length;
        byte[] ncLen = (new BigInteger(String.valueOf(nationalCode.length))).toByteArray();
        System.arraycopy(ncLen, 0, isoMsg, index, ncLen.length);
        index += ncLen.length;
        System.arraycopy(nationalCode, 0, isoMsg, index, nationalCode.length);
        index += nationalCode.length;
        isoMsg = Arrays.copyOf(isoMsg, index);
        MacKeyGenerator macKeyGenerator = new MacKeyGenerator();
        byte[] mac = macKeyGenerator.generateMac(client.hexToBytes("F200B1E948D327B5"), isoMsg);
        index += 2;
        byte[] finalMsg = new byte[index + mac.length];
        byte[] bLen = addPadding((new BigInteger(index + "")).toByteArray(), 2);
        index = 0;
        System.arraycopy(bLen, 0, finalMsg, index, bLen.length);
        index += bLen.length;
        System.arraycopy(isoMsg, 0, finalMsg, index, isoMsg.length);
        index += isoMsg.length;
        System.arraycopy(mac, 0, finalMsg, index, mac.length);
        byte[] response = client.sendMessage(finalMsg);
        if (macKeyGenerator.checkMac(client.hexToBytes("F200B1E948D327B5"), response, 2)) {
            StringBuilder stringBuilder = new StringBuilder();
            index = 0;
            byte[] tLen = new byte[2];
            System.arraycopy(response, index, tLen, 0, tLen.length);
            index += tLen.length;
            stringBuilder.append((new BigInteger(tLen)).intValue());
            byte[] locale = new byte[2];
            System.arraycopy(response, index, locale, 0, locale.length);
            index += locale.length;
            stringBuilder.append(new String(locale));
            serviceCode = new byte[1];
            System.arraycopy(response, index, serviceCode, 0, serviceCode.length);
            index += serviceCode.length;
            stringBuilder.append((new BigInteger(serviceCode)).intValue());
            date = new byte[4];
            System.arraycopy(response, index, date, 0, date.length);
            index += date.length;
            stringBuilder.append((new BigInteger(date)).intValue());
            time = new byte[4];
            System.arraycopy(response, index, time, 0, time.length);
            index += time.length;
            stringBuilder.append((new BigInteger(time)).intValue());
            byte[] error = new byte[1];
            System.arraycopy(response, index, error, 0, error.length);
            index += error.length;
            stringBuilder.append((new BigInteger(error)).intValue());
            byte[] rrn = new byte[8];
            System.arraycopy(response, index, rrn, 0, rrn.length);
            index += rrn.length;
            stringBuilder.append((new BigInteger(rrn)).longValue());
            if ((new BigInteger(error)).intValue() == 0) {
                if ((new BigInteger(serviceCode)).intValue() == 2) {
                    byte[] dueDate = new byte[4];
                    System.arraycopy(response, index, dueDate, 0, dueDate.length);
                    index += dueDate.length;
                    stringBuilder.append(new BigInteger(dueDate));
                    byte[] amount = new byte[8];
                    System.arraycopy(response, index, amount, 0, amount.length);
                    index += amount.length;
                    stringBuilder.append(new BigInteger(amount));
                    byte[] bankCode = new byte[1];
                    System.arraycopy(response, index, bankCode, 0, bankCode.length);
                    index += bankCode.length;
                    stringBuilder.append(new BigInteger(bankCode));
                    byte[] branchCode = new byte[4];
                    System.arraycopy(response, index, branchCode, 0, branchCode.length);
                    index += branchCode.length;
                    stringBuilder.append(new BigInteger(branchCode));
                    byte[] sayadId = new byte[8];
                    System.arraycopy(response, index, sayadId, 0, sayadId.length);
                    index += sayadId.length;
                    stringBuilder.append(new BigInteger(sayadId));
                    byte[] serialNo = new byte[4];
                    System.arraycopy(response, index, serialNo, 0, serialNo.length);
                    index += serialNo.length;
                    stringBuilder.append(new BigInteger(serialNo));
                    byte[] fromIban = new byte[12];
                    System.arraycopy(response, index, fromIban, 0, fromIban.length);
                    index += fromIban.length;
                    stringBuilder.append(new BigInteger(fromIban));
                    byte[] locked = new byte[1];
                    System.arraycopy(response, index, locked, 0, locked.length);
                    index += locked.length;
                    stringBuilder.append(new BigInteger(locked));
                    byte[] chequeStatus = new byte[1];
                    System.arraycopy(response, index, chequeStatus, 0, chequeStatus.length);
                    index += chequeStatus.length;
                    stringBuilder.append(new BigInteger(chequeStatus));
                    byte[] blockStatus = new byte[1];
                    System.arraycopy(response, index, blockStatus, 0, blockStatus.length);
                    index += blockStatus.length;
                    stringBuilder.append(new BigInteger(blockStatus));
                    byte[] guaranteeStatus = new byte[1];
                    System.arraycopy(response, index, guaranteeStatus, 0, guaranteeStatus.length);
                    index += guaranteeStatus.length;
                    stringBuilder.append(new BigInteger(guaranteeStatus));
                    byte[] chequeType = new byte[1];
                    System.arraycopy(response, index, chequeType, 0, chequeType.length);
                    index += chequeType.length;
                    stringBuilder.append(new BigInteger(chequeType));
                    byte[] chequeMediaType = new byte[1];
                    System.arraycopy(response, index, chequeMediaType, 0, chequeMediaType.length);
                    index += chequeMediaType.length;
                    stringBuilder.append(new BigInteger(chequeMediaType));
                    byte[] personalInfoCount = new byte[1];
                    System.arraycopy(response, index, personalInfoCount, 0, personalInfoCount.length);
                    index += personalInfoCount.length;
                    stringBuilder.append(new BigInteger(personalInfoCount));

                    for(int i = 0; i < (new BigInteger(personalInfoCount)).intValue(); ++i) {
                        byte[] nameLen = new byte[1];
                        System.arraycopy(response, index, nameLen, 0, nameLen.length);
                        index += nameLen.length;
                        stringBuilder.append(new BigInteger(nameLen));
                        byte[] name = new byte[(new BigInteger(nameLen)).intValue()];
                        System.arraycopy(response, index, name, 0, name.length);
                        index += name.length;
                        stringBuilder.append(new String(name));
                        byte[] customerIdLen = new byte[1];
                        System.arraycopy(response, index, customerIdLen, 0, customerIdLen.length);
                        index += customerIdLen.length;
                        stringBuilder.append(new BigInteger(customerIdLen));
                        byte[] customerId = new byte[(new BigInteger(customerIdLen)).intValue()];
                        System.arraycopy(response, index, customerId, 0, customerId.length);
                        index += customerId.length;
                        stringBuilder.append(new String(customerId));
                        byte[] shahabId = new byte[8];
                        System.arraycopy(response, index, shahabId, 0, shahabId.length);
                        index += shahabId.length;
                        stringBuilder.append(new BigInteger(shahabId));
                        byte[] customerType = new byte[1];
                        System.arraycopy(response, index, customerType, 0, nameLen.length);
                        index += customerType.length;
                        stringBuilder.append(new BigInteger(customerType));
                    }
                } else if ((new BigInteger(serviceCode)).intValue() == 4) {
                    byte[] nameLen = new byte[1];
                    System.arraycopy(response, index, nameLen, 0, nameLen.length);
                    index += nameLen.length;
                    stringBuilder.append(new BigInteger(nameLen));
                    byte[] name = new byte[(new BigInteger(nameLen)).intValue()];
                    System.arraycopy(response, index, name, 0, name.length);
                    int var10000 = index + name.length;
                    stringBuilder.append(new String(name));
                } else if ((new BigInteger(serviceCode)).intValue() == 12) {
                    byte[] branch = new byte[4];
                    System.arraycopy(response, index, branch, 0, branch.length);
                    index += branch.length;
                    stringBuilder.append(new BigInteger(branch));
                    byte[] series = new byte[4];
                    System.arraycopy(response, index, series, 0, series.length);
                    index += series.length;
                    stringBuilder.append(new BigInteger(series));
                    byte[] serial = new byte[4];
                    System.arraycopy(response, index, serial, 0, serial.length);
                    index += serial.length;
                    stringBuilder.append(new BigInteger(serial));
                    byte[] media = new byte[1];
                    System.arraycopy(response, index, media, 0, media.length);
                    index += media.length;
                    stringBuilder.append(new BigInteger(media));
                    byte[] expire = new byte[4];
                    System.arraycopy(response, index, expire, 0, expire.length);
                    int var98 = index + expire.length;
                    stringBuilder.append(new BigInteger(expire));
                }
            }

            AAAServer.logger.info(stringBuilder.toString());
        } else {
            AAAServer.logger.info("mac is mistake");
        }

        client.stopConnection();
    }

    private static final int getMsg(byte[] response, int index, int length, StringBuilder builder) {
        byte[] len = new byte[length];
        System.arraycopy(response, index, len, 0, len.length);
        index += len.length;
        builder.append((new BigInteger(len)).intValue());
        byte[] value = new byte[(new BigInteger(len)).intValue()];
        System.arraycopy(response, index, value, 0, value.length);
        index += value.length;
        String s = new String(value);
        char[] sc = s.toCharArray();
        if (sc.length == 1 && sc[0] == 0) {
            s = "0";
        }

        builder.append(s);
        return index;
    }

    private static final int getMsgInt(byte[] response, int index, int length, StringBuilder builder) {
        byte[] len = new byte[length];
        System.arraycopy(response, index, len, 0, len.length);
        index += len.length;
        builder.append((new BigInteger(len)).intValue());
        return index;
    }

    private static byte[] addPadding(byte[] in, int len) {
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

    private static int setParams(String keyName, String paramValue, byte[] isoMsg, int index) {
        byte[] key = keyName.getBytes(StandardCharsets.UTF_8);
        byte[] keyLen = (new BigInteger(key.length + "")).toByteArray();
        System.arraycopy(keyLen, 0, isoMsg, index, keyLen.length);
        index += keyLen.length;
        System.arraycopy(key, 0, isoMsg, index, key.length);
        index += key.length;
        byte[] value = String.valueOf(paramValue).getBytes(StandardCharsets.UTF_8);
        byte[] valueLen = (new BigInteger(value.length + "")).toByteArray();
        System.arraycopy(valueLen, 0, isoMsg, index, valueLen.length);
        index += valueLen.length;
        System.arraycopy(value, 0, isoMsg, index, value.length);
        index += value.length;
        return index;
    }

    public void startConnection(String ip, int port) {
        try {
            this.clientSocket = new Socket(ip, port);
            this.out = new DataOutputStream(this.clientSocket.getOutputStream());
            this.in = new DataInputStream(this.clientSocket.getInputStream());
        } catch (IOException e) {
            AAAServer.logger.debug("Error when initializing connection", e);
        }

    }

    public byte[] sendMessage(byte[] msg) {
        try {
            this.out.write(msg);
            byte[] b = new byte[8192];
            int len = this.in.read(b);
            return Arrays.copyOf(b, len);
        } catch (Exception var4) {
            return null;
        }
    }

    public void stopConnection() {
        try {
            this.in.close();
            this.out.close();
            this.clientSocket.close();
        } catch (IOException e) {
            AAAServer.logger.debug("error when closing", e);
        }

    }

    public byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    final String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for(byte b : bytes) {
            sb.append(String.format("%02x", b & 255));
        }

        return sb.toString();
    }
}
