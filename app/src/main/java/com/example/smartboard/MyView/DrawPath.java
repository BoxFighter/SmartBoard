package com.example.smartboard.MyView;

import android.graphics.Paint;
import android.graphics.Path;

import java.io.Serializable;

/**
 * Created by 刘宸睿 on 2016/8/28.
 */
public class DrawPath implements Serializable {
    private static final long serialVersionUID=1L;
    int status;
    float startx;
    float starty;
    float stopx;
    float stopy;
    Path path;
    Paint paint;
}