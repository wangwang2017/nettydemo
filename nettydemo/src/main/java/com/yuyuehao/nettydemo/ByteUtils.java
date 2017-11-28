package com.yuyuehao.nettydemo;

import static android.R.attr.key;

/**
 * Created by Wang
 * on 2017-11-28
 */

public class ByteUtils {

    public static boolean bHasLF(byte[] b){

        boolean key = false;
        for (byte a:b){
            if (a == 10){
                key = true;
            }
        }
        return key;
    }

    public static Integer bIndexLF(byte[] b){

        Integer index = null;
        for (int i = 0; i <b.length ; i++) {
            if (b[i] ==10){
                index =i;
            }
        }
        return index;
    }
}
