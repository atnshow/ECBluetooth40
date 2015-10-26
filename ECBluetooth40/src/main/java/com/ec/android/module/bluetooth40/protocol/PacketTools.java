package com.ec.android.module.bluetooth40.protocol;

import android.text.TextUtils;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author EC
 */
public class PacketTools {
    //
    private Short mSequenceId;
    //
    private String mContent;

    //多少个包
    private int mPacketLength;

    public PacketTools() {
        init();
    }

    private void init() {
        this.mSequenceId = ProtocolUtils.getRandomSequenceId();
    }

    /**
     * 设置将要发送的内容
     *
     * @param content
     */
    public void setSendContent(String content) {
        this.mContent = content;
    }

    public List<Byte[]> translateBytes() {
        if (TextUtils.isEmpty(this.mContent)) {
            return null;
        }
        //
        List<Byte[]> list = new ArrayList<>();

        Byte[] bytesBefore = ArrayUtils.toObject(this.mContent.getBytes());
        if (bytesBefore.length <= 13) {
            //只能携带13个字节，小于等于13个字节直接搞
            list.add(bytesBefore);
        } else {
            //大于13个字节，分包咯
            boolean flag = true;
            Byte[] bytesTemp = bytesBefore;
            while (flag) {
                if (bytesTemp.length == 0) {
//                    flag = false;
                    break;
                }
                //
                if (bytesTemp.length > 13) {
                    Byte[] subarray = (Byte[]) ArrayUtils.subarray(bytesTemp, 0, 13);
                    //
                    list.add(subarray);
                    //
                    bytesTemp = (Byte[]) ArrayUtils.subarray(bytesTemp, 13, bytesTemp.length);
                } else {
                    Byte[] subarray = (Byte[]) ArrayUtils.subarray(bytesTemp, 0, bytesTemp.length);
                    //
                    list.add(subarray);
                    //
                    flag = false;
                }
            }
            //
        }
        //
        this.mPacketLength = list.size();
        //
        return list;

    }

    /**
     * 实例化一个只有一个packet的PacketBuilder
     *
     * @return
     */
    public Packet.Builder newJustOnePacketBuilder() {
        Packet.Builder builder = new Packet.Builder().setReserve(ProtocolUtils.getRealReserve((byte) 0x00))
                .setErrFlag(ProtocolUtils.getRealErrFlag((byte) 0x00))
                .setAckFlag(ProtocolUtils.getRealAckFlag((byte) 0x00))
                .setBeginFlag(ProtocolUtils.getRealBeginFlag((byte) 0x01))
                .setEndFlag(ProtocolUtils.getRealEndFlag((byte) 0x01))
                .setSequenceId(mSequenceId);
        //
        return builder;
    }

    /**
     * 实例化一个有多个packet的第一个Packet的PacketBuilder
     *
     * @return
     */
    public Packet.Builder newMorePacketFirstBuilder() {
        Packet.Builder builder = new Packet.Builder().setReserve(ProtocolUtils.getRealReserve((byte) 0x00))
                .setErrFlag(ProtocolUtils.getRealErrFlag((byte) 0x00))
                .setAckFlag(ProtocolUtils.getRealAckFlag((byte) 0x00))
                .setBeginFlag(ProtocolUtils.getRealBeginFlag((byte) 0x01))
                .setEndFlag(ProtocolUtils.getRealEndFlag((byte) 0x00))
                .setSequenceId(mSequenceId);
        //
        return builder;
    }

    /**
     * 实例化一个有多个packet的中间（不是开始也不是结束）Packet的PacketBuilder
     *
     * @return
     */
    public Packet.Builder newMorePacketCommonBuilder() {
        Packet.Builder builder = new Packet.Builder().setReserve(ProtocolUtils.getRealReserve((byte) 0x00))
                .setErrFlag(ProtocolUtils.getRealErrFlag((byte) 0x00))
                .setAckFlag(ProtocolUtils.getRealAckFlag((byte) 0x00))
                .setBeginFlag(ProtocolUtils.getRealBeginFlag((byte) 0x00))
                .setEndFlag(ProtocolUtils.getRealEndFlag((byte) 0x00))
                .setSequenceId(mSequenceId);
        //
        return builder;
    }

    /**
     * 实例化一个有多个packet的最后一个Packet的PacketBuilder
     *
     * @return
     */
    public Packet.Builder newMorePacketFinalBuilder() {
        Packet.Builder builder = new Packet.Builder().setReserve(ProtocolUtils.getRealReserve((byte) 0x00))
                .setErrFlag(ProtocolUtils.getRealErrFlag((byte) 0x00))
                .setAckFlag(ProtocolUtils.getRealAckFlag((byte) 0x00))
                .setBeginFlag(ProtocolUtils.getRealBeginFlag((byte) 0x00))
                .setEndFlag(ProtocolUtils.getRealEndFlag((byte) 0x01))
                .setSequenceId(mSequenceId);
        //
        return builder;
    }

    public Short getSequenceId() {
        return mSequenceId;
    }

    public int getPacketLength() {
        return mPacketLength;
    }
}
