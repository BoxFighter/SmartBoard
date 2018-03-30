package com.example.smartboard.MyView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.smartboard.Activity.SmartpadClient;
import com.example.smartboard.Interfaces.SmartAction;
import com.example.smartboard.MyNet.SocketMessage;
import com.example.smartboard.MyNet.WifiServerThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 刘宸睿 on 2016/6/5.
 */
public class SmartpadView extends View{

    public static Paint smartPaint = null;
    public static Canvas cacheCanvas = null;
    public static Bitmap cacheBitmap = null;
    public Path path;
    public SmartAction action;

    public static int PEN_WIDTH = 10;
    public static int PAINT_STATUS;
    public static int PEN = 0;
    public static int ERASER= 1;
    public static int LINE= 2;
    public static int CIRCLE= 3;
    public static int RECT= 4;
    public static int OVAL= 5;
    public static int TABLE= 6;
    private static int m_strokeColor = Color.RED;
    public static boolean SC;

    public static float x;
    public static float y;
    private float preX;
    private float preY;

    private float startX;
    private float startY;
    private float stopX;
    private float stopY;

    public  ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    public DrawPath dp;

    public SmartpadView(Context context,AttributeSet attrs) {
        super(context,attrs);
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
        initCanvas();
        initPaint();
    }
    public void initCanvas() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        cacheBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(cacheBitmap);
        initAction();
        path = new Path();
        cacheCanvas.setBitmap(cacheBitmap);
    }
    public void initAction(){action=new SmartPath();}
    public void initPaint(){
        initAction();
        PAINT_STATUS=PEN;
        smartPaint = new Paint(Paint.DITHER_FLAG);
        smartPaint.setColor(Color.RED);
        smartPaint.setStyle(Paint.Style.STROKE);
        smartPaint.setStrokeWidth(PEN_WIDTH);
        smartPaint.setAntiAlias(true);
        smartPaint.setDither(true);
        smartPaint.setStrokeJoin(Paint.Join.ROUND);
        smartPaint.setStrokeCap(Paint.Cap.ROUND);
    }
    @Override
    public void onDraw(Canvas canvas) {
        Paint bmpSmartPaint = new Paint();
        canvas.drawBitmap(cacheBitmap, 0, 0, bmpSmartPaint); // ②
        if(PAINT_STATUS==PEN){
            canvas.drawPath(path, smartPaint);
        }else if(PAINT_STATUS==LINE){
            canvas.drawLine(startX,startY,stopX,stopY, smartPaint);
        }else if(PAINT_STATUS==CIRCLE){
            float radius=Math.abs(stopX-startX)>Math.abs(stopY-startY)?Math.abs(stopX-startX)/2:Math.abs(stopY-startY)/2;
            canvas.drawCircle((startX+stopX)/2,(startY+stopY)/2,radius, smartPaint);
        }else if(PAINT_STATUS==RECT){
            float tempStartX=startX;
            float tempStartY=startY;
            float tempStopX=stopX;
            float tempStopY=stopY;
            if(startX>stopX){
                tempStartX=stopX;
                tempStopX=startX;
            }
            if(startY>stopY){
                tempStartY=stopY;
                tempStopY=startY;
            }
            canvas.drawRect(tempStartX,tempStartY,tempStopX,tempStopY, smartPaint);
        }else if(PAINT_STATUS==OVAL){
            float tempStartX=startX;
            float tempStartY=startY;
            float tempStopX=stopX;
            float tempStopY=stopY;
            if(startX>stopX){
                tempStartX=stopX;
                tempStopX=startX;
            }
            if(startY>stopY){
                tempStartY=stopY;
                tempStopY=startY;
            }
            RectF r=new RectF(tempStartX,tempStartY,tempStopX,tempStopY);
            canvas.drawOval(r, smartPaint);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startX=x;
                startY=y;
                path.moveTo(x, y);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                action.move( x, y);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_UP:
                action.draw(); // ①
                path.reset();
                break;
        }
        invalidate(SC);
        return true;
    }
    class SmartPath implements SmartAction {

        public void draw() {
            dp = new DrawPath();
            dp.path = new Path(path);
            dp.paint = new Paint(smartPaint);
            dp.status=PEN;
            savePath.add(dp);
            cacheCanvas.drawPath(path, smartPaint);
        }

        public void move(float mx, float my) {
            if(SC==false){
                SmartpadClient.send();
            }else if(SC==true){
                SocketMessage msg=new SocketMessage();
                msg.from=0;
                msg.status=PAINT_STATUS;
                msg.startX=mx;
                msg.startY=my;
                WifiServerThread.mMsgList.add(msg);
            }
            path.quadTo(preX, preY, mx, my);
        }
    }
    class SmartLine implements SmartAction{

        @Override
        public void draw() {
            dp = new DrawPath();
            dp.path = null;
            dp.paint = new Paint(smartPaint);
            dp.status=LINE;
            dp.startx=startX;
            dp.starty=startY;
            dp.stopx=stopX;
            dp.stopy=stopY;
            savePath.add(dp);
            cacheCanvas.drawLine(startX,startY,stopX,stopY, smartPaint);
        }

        @Override
        public void move(float mx, float my) {
            if(SC==false){
                SmartpadClient.send();
            }else if(SC==true){
                SocketMessage msg=new SocketMessage();
                msg.from=0;
                msg.status=PAINT_STATUS;
                msg.startX=startX;
                msg.startY=startY;
                msg.stopX=mx;
                msg.stopY=my;
                WifiServerThread.mMsgList.add(msg);
            }
            stopX=mx;
            stopY=my;
        }
    }
    class SmartRect implements SmartAction{

        @Override
        public void draw() {
            dp = new DrawPath();
            dp.path = null;
            dp.paint = new Paint(smartPaint);
            dp.status=RECT;
            if(startX<stopX){
                dp.startx=startX;
                dp.stopx=stopX;
            }else {
                dp.startx=stopX;
                dp.stopx=startX;
            }
            if(startY<stopY){
                dp.starty=startY;
                dp.stopy=stopY;
            }else {
                dp.starty=stopY;
                dp.stopy=startY;
            }
            savePath.add(dp);
            cacheCanvas.drawRect(dp.startx,dp.starty,dp.stopx,dp.stopy, smartPaint);
            if(SC==false){
                SmartpadClient.send();
            }else if(SC==true){
                SocketMessage msg=new SocketMessage();
                msg.from=0;
                msg.status=PAINT_STATUS;
                msg.startX=dp.startx;
                msg.startY=dp.starty;
                msg.stopX=dp.stopx;
                msg.stopY=dp.stopy;
                WifiServerThread.mMsgList.add(msg);
            }
        }

        @Override
        public void move(float mx, float my) {
            stopX=mx;
            stopY=my;

        }
    }
    class SmartOval implements SmartAction{

        @Override
        public void draw() {
            dp = new DrawPath();
            dp.path = null;
            dp.paint = new Paint(smartPaint);
            dp.status=OVAL;
            if(startX<stopX){
                dp.startx=startX;
                dp.stopx=stopX;
            }else {
                dp.startx=stopX;
                dp.stopx=startX;
            }
            if(startY<stopY){
                dp.starty=startY;
                dp.stopy=stopY;
            }else {
                dp.starty=stopY;
                dp.stopy=startY;
            }
            savePath.add(dp);
            RectF rectF=new RectF(dp.startx,dp.starty,dp.stopx,dp.stopy);
            cacheCanvas.drawOval(rectF, smartPaint);
            if(SC==false){
                SmartpadClient.send();
            }else if(SC==true){
                SocketMessage msg=new SocketMessage();
                msg.from=0;
                msg.status=PAINT_STATUS;
                msg.startX=dp.startx;
                msg.startY=dp.starty;
                msg.stopX=dp.stopx;
                msg.stopY=dp.stopy;
                WifiServerThread.mMsgList.add(msg);
            }
        }

        @Override
        public void move(float mx, float my) {
            stopX=mx;
            stopY=my;
        }
    }
    class SmartCircle implements SmartAction{

        float radius;
        @Override
        public void draw() {
            dp = new DrawPath();
            dp.path = null;
            dp.paint = new Paint(smartPaint);
            dp.status=CIRCLE;
            dp.startx=startX;
            dp.starty=startY;
            dp.stopx=stopX;
            dp.stopy=stopY;
            savePath.add(dp);
            cacheCanvas.drawCircle((startX+stopX)/2, (startY+stopY)/2,radius, smartPaint);
            if(SC==false){
                SmartpadClient.send();
            }else if(SC==true){
                SocketMessage msg=new SocketMessage();
                msg.from=0;
                msg.status=PAINT_STATUS;
                msg.startX=dp.startx;
                msg.startY=dp.starty;
                msg.stopX=dp.stopx;
                msg.stopY=dp.stopy;
                WifiServerThread.mMsgList.add(msg);
            }
        }

        @Override
        public void move(float mx, float my) {
            stopX=mx;
            stopY=my;
            radius=Math.abs(stopX-startX)>Math.abs(stopY-startY)?Math.abs(stopX-startX)/2:Math.abs(stopY-startY)/2;
        }
    }
    public void undo(){
        if(savePath!=null&&savePath.size()>0){
            startX=0;startY=0;
            stopX=0;stopY=0;
            initCanvas();
            DrawPath temp=savePath.get(savePath.size()-1);
            deletePath.add(temp);
            savePath.remove(savePath.size()-1);

            for(int i=0;i<savePath.size();i++){
                DrawPath dp = savePath.get(i);
                if(dp.status==PEN){
                    cacheCanvas.drawPath(dp.path, dp.paint);
                }else if(dp.status==LINE){
                    cacheCanvas.drawLine(dp.startx,dp.starty,dp.stopx,dp.stopy,dp.paint);
                }else if(dp.status==RECT){
                    cacheCanvas.drawRect(dp.startx,dp.starty,dp.stopx,dp.stopy,dp.paint);
                }else if(dp.status==CIRCLE){
                    float radius=Math.abs(dp.stopx-dp.startx)>Math.abs(dp.stopy-dp.starty)?
                            Math.abs(dp.stopx-dp.startx)/2:Math.abs(dp.stopy-dp.starty)/2;
                    cacheCanvas.drawCircle((dp.startx+dp.stopx)/2,(dp.starty+dp.stopy)/2,radius,dp.paint);
                }else if(dp.status==OVAL){
                    RectF rectF=new RectF(dp.startx,dp.starty,dp.stopx,dp.stopy);
                    cacheCanvas.drawOval(rectF,dp.paint);
                }
            }
            invalidate();
        }
    }
    public void redo(){
        if(deletePath!=null&&deletePath.size()>0){
            DrawPath temp=deletePath.get(deletePath.size()-1);
            savePath.add(temp);
            deletePath.remove(deletePath.size()-1);

            if(temp.status==PEN){
                cacheCanvas.drawPath(temp.path, temp.paint);
            }else if(temp.status==LINE){
                cacheCanvas.drawLine(temp.startx,temp.starty,temp.stopx,temp.stopy,temp.paint);
            }else if(temp.status==RECT){
                cacheCanvas.drawRect(temp.startx,temp.starty,temp.stopx,temp.stopy,temp.paint);
            }else if(temp.status==CIRCLE){
                float radius=Math.abs(temp.stopx-temp.startx)>Math.abs(temp.stopy-temp.starty)?
                        Math.abs(temp.stopx-temp.startx)/2:Math.abs(temp.stopy-temp.starty)/2;
                cacheCanvas.drawCircle((temp.startx+temp.stopx)/2,(temp.starty+temp.stopy)/2,radius,temp.paint);
            }else if(temp.status==OVAL){
                RectF rectF=new RectF(temp.startx,temp.starty,temp.stopx,temp.stopy);
                cacheCanvas.drawOval(rectF,temp.paint);
            }
            invalidate();
        }
    }
    public static void setStrokeColor(int color){
        smartPaint.setColor(color);
    }
    public void setStrokeEraser(){
        PAINT_STATUS=ERASER;
        initAction();
        smartPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }
    public void setStrokePen(){
        PAINT_STATUS=PEN;
        smartPaint.setXfermode(null);
    }
    public static int getStrokeSize(){   //�õ����ʵĴ�С
        return PEN_WIDTH;
    }
    public static int getStrokeColor(){   //�õ����ʵĴ�С
        return m_strokeColor;
    }
    public void drawMyLine(){
        PAINT_STATUS=LINE;
        action=new SmartLine();
    }
    public void drawMyCircle(){
        PAINT_STATUS=CIRCLE;
        action=new SmartCircle();
    }
    public void drawMyOval(){
        PAINT_STATUS=OVAL;
        action=new SmartOval();
    }
    public void drawMyRect(){
        PAINT_STATUS=RECT;
        action=new SmartRect();
    }
    public void drawMyTable(){
        PAINT_STATUS=TABLE;
    }
    public void page_turning(){
        saveBitmap();
        initCanvas();
        savePath.clear();
        deletePath.clear();
        invalidate();
    }
    public void saveBitmap(){
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat formatter1 = new  SimpleDateFormat("yyyyMMddHHmm");
        String filename = formatter1.format(curDate);
        filename = filename + ".png";
        //获得系统当前时间，并以该时间作为文件夹名
        SimpleDateFormat formatter2   =   new   SimpleDateFormat   ("yyyyMMdd");
        String flodername = formatter2.format(curDate);
        String paintPath = "";
        String root= Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            File dir = new File(root+"/SmartboardHistory/"+flodername);
            File file = new File(root+"/SmartboardHistory/"+flodername+"/"+filename);
            if (!dir.exists()) {
                dir.mkdirs();
            } else{
                if(file.exists()){
                    file.delete();
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            cacheBitmap.compress(Bitmap.CompressFormat.PNG,100, out);
            out.flush();
            out.close();
            //保存绘图文件路径
            paintPath = root+"/SmartBoardHistory/"+flodername + filename;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void invalidate(boolean b){
        invalidate();
    }
    public void receiveDrawPath(SocketMessage sMsg){
        if(sMsg.status==0){
            int tempStatus=PAINT_STATUS;
            PAINT_STATUS=0;
            cacheCanvas.drawPoint(sMsg.startX, sMsg.startY, smartPaint);
            PAINT_STATUS=tempStatus;
        }else if(sMsg.status==1){
            int tempStatus=PAINT_STATUS;
            setStrokeEraser();
            cacheCanvas.drawPoint(sMsg.startX, sMsg.startY, smartPaint);
            smartPaint.setXfermode(null);
            PAINT_STATUS=tempStatus;
        }else{
            if(sMsg.status==LINE){
                dp = new DrawPath();
                dp.path = null;
                dp.paint = new Paint(smartPaint);
                dp.status=LINE;
                dp.startx=sMsg.startX;
                dp.starty=sMsg.startY;
                dp.stopx=sMsg.stopX;
                dp.stopy=sMsg.stopY;
                savePath.add(dp);
                cacheCanvas.drawLine(sMsg.startX,sMsg.startY,sMsg.stopX,sMsg.stopY,smartPaint);
            }else if(sMsg.status==CIRCLE){
                dp = new DrawPath();
                dp.path = null;
                dp.paint = new Paint(smartPaint);
                dp.status=CIRCLE;
                dp.startx=sMsg.startX;
                dp.starty=sMsg.startY;
                dp.stopx=sMsg.stopX;
                dp.stopy=sMsg.stopY;
                savePath.add(dp);
                float radius=Math.abs(sMsg.stopX-sMsg.startX)>Math.abs(sMsg.stopY-sMsg.startY)?
                        Math.abs(sMsg.stopX-sMsg.startX)/2:Math.abs(sMsg.stopY-sMsg.startY)/2;
                cacheCanvas.drawCircle((sMsg.stopX+sMsg.startX)/2,(sMsg.stopY+sMsg.startY)/2,radius,smartPaint);
            }else if(sMsg.status==RECT){
                dp = new DrawPath();
                dp.path = null;
                dp.paint = new Paint(smartPaint);
                dp.status=RECT;
                if(sMsg.startX<sMsg.stopX){
                    dp.startx=sMsg.startX;
                    dp.stopx=sMsg.stopX;
                }else {
                    dp.startx=sMsg.stopX;
                    dp.stopx=sMsg.startX;
                }
                if(sMsg.startY<sMsg.stopY){
                    dp.starty=sMsg.startY;
                    dp.stopy=sMsg.stopY;
                }else {
                    dp.starty=sMsg.stopY;
                    dp.stopy=sMsg.startY;
                }
                savePath.add(dp);
                cacheCanvas.drawRect(sMsg.startX,sMsg.startY,sMsg.stopX,sMsg.stopY,smartPaint);
            }else if(sMsg.status==OVAL){
                dp = new DrawPath();
                dp.path = null;
                dp.paint = new Paint(smartPaint);
                dp.status=OVAL;
                if(sMsg.startX<sMsg.stopX){
                    dp.startx=sMsg.startX;
                    dp.stopx=sMsg.stopX;
                }else {
                    dp.startx=sMsg.stopX;
                    dp.stopx=sMsg.startX;
                }
                if(sMsg.startY<sMsg.stopY){
                    dp.starty=sMsg.startY;
                    dp.stopy=sMsg.stopY;
                }else {
                    dp.starty=sMsg.stopY;
                    dp.stopy=sMsg.startY;
                }
                savePath.add(dp);
                RectF rectF=new RectF(sMsg.startX,sMsg.startY,sMsg.stopX,sMsg.stopY);
                cacheCanvas.drawOval(rectF,smartPaint);
            }else if(sMsg.status==7){
                m_strokeColor=sMsg.color;
                PEN_WIDTH=sMsg.size;
            }
        }
        invalidate();
    }
    public float getStartX() { return dp.startx;}
    public float getStartY() {
        return dp.starty;
    }
    public float getStopX() { return dp.stopx;}
    public float getStopY() {
        return dp.stopy;
    }
}
