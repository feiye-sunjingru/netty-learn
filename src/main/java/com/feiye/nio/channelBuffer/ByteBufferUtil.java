package com.feiye.nio.channelBuffer;

import java.nio.ByteBuffer;

import io.netty.util.internal.MathUtil;
import io.netty.util.internal.StringUtil;


/**
 * @author Panwen Chen
 * @date 2021/4/12 15:59
 */
public class ByteBufferUtil {
    /**
     * 对于ASCII码在0x20(32)到0x7E(126)范围内的可打印字符，直接转换为对应的字符
     */
    private static final char[] BYTE2CHAR = new char[256];
    /**
     * 0~256每个数值的高4位和低4位在DIGITS中映射的值：这里256*3也够用
     */
    private static final char[] HEXDUMP_TABLE = new char[256 * 4];
    /**
     * 填充空格个数
     */
    private static final String[] HEXPADDING = new String[16];
    /**
     * 在初始化十六进制字符查找表，把i*16转成十六进制字符串
     */
    private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
    /**
     * 将字节（byte）转换为两位十六进制字符串 如97=“61”
     */
    private static final String[] BYTE2HEX = new String[256];
    private static final String[] BYTEPADDING = new String[16];

    static {
        //0~f
        final char[] DIGITS = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; i++) {
            //i >>> 4 & 0x0F 提取字节的高4位（高半字节），作为第一个十六进制字符
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
            //提取字节的低4位（低半字节），作为第二个十六进制字符
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
        }

        int i;

        // Generate the lookup table for hex dump paddings
        for (i = 0; i < HEXPADDING.length; i++) {
            int padding = HEXPADDING.length - i;
            StringBuilder buf = new StringBuilder(padding * 3);
            for (int j = 0; j < padding; j++) {
                buf.append("   ");
            }
            HEXPADDING[i] = buf.toString();
        }

        // Generate the lookup table for the start-offset header in each row (up to 64KiB).
        for (i = 0; i < HEXDUMP_ROWPREFIXES.length; i++) {
            StringBuilder buf = new StringBuilder(12);
            buf.append(StringUtil.NEWLINE);
            //把i*16转成十六进制字符串
            buf.append(Long.toHexString(i << 4 & 0xFFFFFFFFL | 0x100000000L));
            buf.setCharAt(buf.length() - 9, '|');
            buf.append('|');
            HEXDUMP_ROWPREFIXES[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-hex-dump conversion 0~255:两位大写十六进制字符串，并在必要时补前导零
        for (i = 0; i < BYTE2HEX.length; i++) {
            //将字节（byte）转换为两位十六进制字符串 如97=“61”
            BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }

        // Generate the lookup table for byte dump paddings
        for (i = 0; i < BYTEPADDING.length; i++) {
            int padding = BYTEPADDING.length - i;
            StringBuilder buf = new StringBuilder(padding);
            for (int j = 0; j < padding; j++) {
                buf.append(' ');
            }
            BYTEPADDING[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-char conversion
        for (i = 0; i < BYTE2CHAR.length; i++) {
            if (i <= 0x1f || i >= 0x7f) {
                BYTE2CHAR[i] = '.';
            } else {
                BYTE2CHAR[i] = (char) i;
            }
        }
    }

    /**
     * 打印所有内容
     *
     * @param buffer
     */
    public static void debugAll(ByteBuffer buffer) {
        int oldlimit = buffer.limit();
        buffer.limit(buffer.capacity());
        StringBuilder origin = new StringBuilder(256);
        appendPrettyHexDump(origin, buffer, 0, buffer.capacity());
        System.out.println("+--------+-------------------- all ------------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), oldlimit);
        System.out.println(origin);
        buffer.limit(oldlimit);
    }

    /**
     * 打印可读取内容
     *
     * @param buffer
     */
    public static void debugRead(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder(256);
        appendPrettyHexDump(builder, buffer, buffer.position(), buffer.limit() - buffer.position());
        System.out.println("+--------+-------------------- read -----------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), buffer.limit());
        System.out.println(builder);
    }

    /**
     * @param dump   输出目标：拼接结果的 StringBuilder
     * @param buf    要转储的 ByteBuffer
     * @param offset 起始位置（相对于 buffer 的 0）
     * @param length 要转储的字节数
     */
    private static void appendPrettyHexDump(StringBuilder dump, ByteBuffer buf, int offset, int length) {
        //offset+length<capacity
        if (MathUtil.isOutOfBounds(offset, length, buf.capacity())) {
            throw new IndexOutOfBoundsException(
                    "expected: " + "0 <= offset(" + offset + ") <= offset + length(" + length
                            + ") <= " + "buf.capacity(" + buf.capacity() + ')');
        }
        if (length == 0) {
            return;
        }
        dump.append("         +-------------------------------------------------+").
                append(StringUtil.NEWLINE).
                append("         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |").
                append(StringUtil.NEWLINE).
                append("+--------+-------------------------------------------------+----------------+");

        // 相当于 length / 16（整除）
        final int fullRows = length >>> 4;
        // 相当于 length % 16
        final int remainder = length & 0xF;

        // Dump the rows which have 16 bytes.
        for (int row = 0; row < fullRows; row++) {
            // row * 16 + offset
            int rowStartIndex = (row << 4) + offset;

            // Per-row prefix. 行前缀
            appendHexDumpRowPrefix(dump, row, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + 16;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(" |");

            // ASCII dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append('|');
        }

        // Dump the last row which has less than 16 bytes.
        if (remainder != 0) {
            int rowStartIndex = (fullRows << 4) + offset;
            appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + remainder;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(HEXPADDING[remainder]);
            dump.append(" |");

            // Ascii dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append(BYTEPADDING[remainder]);
            dump.append('|');
        }

        dump.append(StringUtil.NEWLINE).append("+--------+-------------------------------------------------+----------------+");
    }

    /**
     * 打印行前缀（如 "0x00000000"）
     *
     * @param dump
     * @param row
     * @param rowStartIndex
     */
    private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
        //直接从查找表中获取格式化的行前缀并追加到输出中
        if (row < HEXDUMP_ROWPREFIXES.length) {
            dump.append(HEXDUMP_ROWPREFIXES[row]);
        } else {
            dump.append(StringUtil.NEWLINE);
            //转换 rowStartIndex 为十六进制字符串
            dump.append(Long.toHexString(rowStartIndex & 0xFFFFFFFFL | 0x100000000L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }
    }

    /**
     * 获取无符号byte值
     *
     * @param buffer ByteBuffer
     * @param index  指定索引位置
     * @return 无符号字节值
     */
    public static short getUnsignedByte(ByteBuffer buffer, int index) {
        //获取指定索引处的byte值（有符号，范围-128到127）,转成无符号整数
        return (short) (buffer.get(index) & 0xFF);
    }
}