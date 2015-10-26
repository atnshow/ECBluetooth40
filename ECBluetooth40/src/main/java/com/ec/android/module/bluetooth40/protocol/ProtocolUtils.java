package com.ec.android.module.bluetooth40.protocol;

import java.util.Random;

/**
 * @author EC
 */
public class ProtocolUtils {

    public static Byte getRealReserve(Byte myByte) {
        int i = myByte << 4;

        Byte b = Integer.valueOf(i).byteValue();
        //
        return b;
    }

    public static Byte getRealErrFlag(Byte myByte) {
        int i = myByte << 3;

        Byte b = Integer.valueOf(i).byteValue();
        //
        return b;
    }

    public static Byte getRealAckFlag(Byte myByte) {
        int i = myByte << 2;

        Byte b = Integer.valueOf(i).byteValue();
        //

        return b;
    }

    public static Byte getRealBeginFlag(Byte myByte) {
        int i = myByte << 1;

        Byte b = Integer.valueOf(i).byteValue();
        //
        return b;
    }

    public static Byte getRealEndFlag(Byte myByte) {
        return myByte;
    }

    /**
     * 获取校验码，注意，除了crc，任何参数都不可缺少值
     *
     * @param builder
     * @return
     */
    public static Short getCrc(Packet.Builder builder) {
        Byte magicByte = Packet.MAGIC_BYTE;

        Byte reserve = builder.getReserve();
        Byte errFlag = builder.getErrFlag();
        Byte ackFlag = builder.getAckFlag();
        Byte beginFlag = builder.getBeginFlag();
        Byte endFlag = builder.getEndFlag();
        Byte payloadLength = builder.getPayloadLength();
        Short sequenceId = builder.getSequenceId();
        if (magicByte == null || reserve == null || errFlag == null || ackFlag == null || beginFlag == null || endFlag == null || payloadLength == null || sequenceId == null) {
            //
            return null;
        }

        //异或得出CRC
        Short crc = (short) (magicByte ^ reserve ^ errFlag ^ ackFlag ^ beginFlag ^ endFlag ^ payloadLength ^ sequenceId);
        //////////
//        int i = reserve + errFlag + ackFlag + beginFlag + endFlag;
//
//        byte b = Integer.valueOf(i).byteValue();
//
//        Short crc2 = (short) (magicByte ^ b ^ payloadLength ^ sequenceId);

        ///////
        return crc;
    }

    /**
     * 随机获取sequenceId
     *
     * @return
     */
    public static Short getRandomSequenceId() {
        Short i = (short) new Random().nextInt();
        short abs = (short) Math.abs(i);
        return abs;
    }

}
