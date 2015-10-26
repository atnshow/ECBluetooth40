package com.ec.android.module.bluetooth40.protocol;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author EC
 */
public class Packet {
    //
    public static final Byte MAGIC_BYTE = 0x55;
    //保留位
    private Byte reserve;
    //错误位
    private Byte errFlag;
    //ACK位
    private Byte ackFlag;
    //开始位
    private Byte beginFlag;
    //结束位
    private Byte endFlag;
    //内容长度位
    private Byte payloadLength;
    //顺序位
    private Short sequenceId;
    //CRC校验位
    private Short crc;
    //具体内容位 (13个字节)
    private Byte[] payloadBytes;

    public Packet(Builder builder) {
        this.reserve = builder.reserve;
        this.errFlag = builder.errFlag;
        this.ackFlag = builder.ackFlag;
        this.beginFlag = builder.beginFlag;
        this.endFlag = builder.endFlag;
        this.payloadLength = builder.payloadLength;
        this.crc = builder.crc;
        this.sequenceId = builder.sequenceId;
        this.payloadBytes = builder.payloadBytes;
    }

    public static class Builder {
        //保留位
        private Byte reserve;
        //错误位
        private Byte errFlag;
        //ACK位
        private Byte ackFlag;
        //开始位
        private Byte beginFlag;
        //结束位
        private Byte endFlag;
        //内容长度位
        private Byte payloadLength;
        //顺序位
        private Short sequenceId;
        //CRC校验位
        private Short crc;
        //具体内容位 (13个字节)
        private Byte[] payloadBytes;

        public Builder() {

        }

        public Byte getReserve() {
            return reserve;
        }

        public Builder setReserve(Byte reserve) {
            this.reserve = reserve;
            return this;
        }

        public Byte getErrFlag() {
            return errFlag;
        }

        public Builder setErrFlag(Byte errFlag) {
            this.errFlag = errFlag;
            return this;
        }

        public Byte getAckFlag() {
            return ackFlag;
        }

        public Builder setAckFlag(Byte ackFlag) {
            this.ackFlag = ackFlag;
            return this;
        }

        public Byte getBeginFlag() {
            return beginFlag;
        }

        public Builder setBeginFlag(Byte beginFlag) {
            this.beginFlag = beginFlag;
            return this;
        }

        public Byte getEndFlag() {
            return endFlag;
        }

        public Builder setEndFlag(Byte endFlag) {
            this.endFlag = endFlag;
            return this;
        }

        public Byte getPayloadLength() {
            return payloadLength;
        }

        public Builder setPayloadLength(Byte payloadLength) {
            this.payloadLength = payloadLength;
            return this;
        }

        public Short getCrc() {
            return crc;
        }

        public Builder setCrc(Short crc) {
            this.crc = crc;
            return this;
        }

        public Short getSequenceId() {
            return sequenceId;
        }

        public Builder setSequenceId(Short sequenceId) {
            this.sequenceId = sequenceId;
            return this;
        }

        public Byte[] getPayloadBytes() {
            return payloadBytes;
        }

        public Builder setPayloadBytes(Byte[] payloadBytes) {
            this.payloadBytes = payloadBytes;
            return this;
        }

        public Packet create() {
            return new Packet(this);
        }
    }

    /**
     * 转成字符数组
     * 必须设置好所有参数
     *
     * @return
     */
    public Byte[] translateBytes() {
        Byte[] bytes = new Byte[7];

        bytes[0] = MAGIC_BYTE;
        //
        int i = this.reserve + this.errFlag + this.ackFlag + this.beginFlag + this.endFlag;
        Byte byteMuti = Integer.valueOf(i).byteValue();
        bytes[1] = byteMuti;
        //
        bytes[2] = this.payloadLength;
        //sequenceId高位
        short sequenceIdHigh = (short) (this.sequenceId >> 8);
        bytes[3] = Short.valueOf(sequenceIdHigh).byteValue();
        //sequenceId低位
        short sequenceIdLow = this.sequenceId;
        bytes[4] = Short.valueOf(sequenceIdLow).byteValue();
        //crc高位
        short crcHigh = (short) (this.crc >> 8);
        bytes[5] = Short.valueOf(crcHigh).byteValue();
        //crc低位
        short crcLow = this.crc;
        bytes[6] = Short.valueOf(crcLow).byteValue();
        //
        Byte[] objects = (Byte[]) ArrayUtils.addAll(bytes, payloadBytes);

        return objects;
    }

}
