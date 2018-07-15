package com.example.floatwindow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 19160 on 2017/7/8.
 */

public class MarkSizeView extends View {

    private static final int DEFAULT_MARKED_COLOR = Color.parseColor("#00000000");
    private static final int DEFAULT_UNMARKED_COLOR = Color.parseColor("#80000000");
    private static final int DEFAULT_STROKE_COLOR = Color.parseColor("#009688");
    private static final int DEFAULT_STROKE_WIDTH = 2;//dp
    private static final int DEFAULT_VERTEX_COLOR = Color.parseColor("#009688");
    private static final int DEFAULT_CONFIRM_BUTTON_RES = R.mipmap.ic_done_white_36dp;
    private static final int DEFAULT_CANCEL_BUTTON_RES = R.mipmap.ic_close_capture;

    private static final int BUTTON_EXTRA_WIDTH = ViewUtil.dp2px(8);

    private static final int DEFAULT_VERTEX_WIDTH = 20;//dp

    private int markedColor = DEFAULT_MARKED_COLOR;
    private int unmarkedColor = DEFAULT_UNMARKED_COLOR;
    private int strokeColor = DEFAULT_STROKE_COLOR;//笔画颜色
    private int strokeWidth = (int) ViewUtil.dp2px(DEFAULT_STROKE_WIDTH);//笔画宽度
    private int vertexColor = DEFAULT_VERTEX_COLOR;//顶点颜色
    private int confirmButtonRes = DEFAULT_CONFIRM_BUTTON_RES;
    private int cancelButtonRes = DEFAULT_CANCEL_BUTTON_RES;
    private int vertexWidth = (int) ViewUtil.dp2px(DEFAULT_VERTEX_WIDTH);//顶点宽度
    private int mActionGap;

    private Paint unMarkPaint,markPaint,vertexPaint,mBitPaint;

    private int downX,downY;
    private int startX,startY;
    private int endX,endY;

    private Rect markedArea;
    private Rect confirmArea,cancelArea;
    private RectF ltVer,rtVer,lbVer,rbVer;
    private boolean isValid = false; //标识绘制的图形是否有效
    private boolean isUp = false; //标识是拖动过程中还是拖动完成
    private boolean isMoveMode = false; //拖动模式，能否拖动图形
    private boolean isAdjustMode = false;  //能否调节图形
    private boolean isButtonClicked = false;  //Button能否点击
    private int adjustNum = 0;  //顶点编号

    private Bitmap confirmBitmap,cancelBitmap;

    private onClickListener mOnClickListener;

    private boolean isMarkRect = true;
    private GraphicPath mGraphicPath;

    public MarkSizeView(Context context) {
        super(context);
        init(context,null);
    }

    public MarkSizeView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context,null);
    }

    private void init(Context context,AttributeSet attrs) { //初始化所有资源
        if (attrs != null) {
            //获取资源文件
            TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MarkSizeView);
            markedColor = typedArray.getColor(R.styleable.MarkSizeView_markedColor,DEFAULT_MARKED_COLOR);
            unmarkedColor = typedArray.getColor(R.styleable.MarkSizeView_unmarkedColor,DEFAULT_UNMARKED_COLOR);
            strokeColor = typedArray.getColor(R.styleable.MarkSizeView_strokeColor,DEFAULT_STROKE_COLOR);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.MarkSizeView_strokeWidth,(int) ViewUtil.dp2px(DEFAULT_STROKE_WIDTH));
            vertexColor = typedArray.getColor(R.styleable.MarkSizeView_vertexColor,DEFAULT_VERTEX_COLOR);
            vertexWidth = typedArray.getDimensionPixelSize(R.styleable.MarkSizeView_vertexWidth,(int) ViewUtil.dp2px(DEFAULT_VERTEX_WIDTH));
            confirmButtonRes = typedArray.getResourceId(R.styleable.MarkSizeView_confirmButtonRes,DEFAULT_CONFIRM_BUTTON_RES);
            cancelButtonRes = typedArray.getResourceId(R.styleable.MarkSizeView_cancelButtonRes,DEFAULT_CANCEL_BUTTON_RES);
        }

        unMarkPaint = new Paint();
        unMarkPaint.setColor(unmarkedColor);
        unMarkPaint.setAntiAlias(true);//去锯齿

        markPaint = new Paint();
        markPaint.setColor(markedColor);
        markPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        markPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));//清楚画布上图像
        markPaint.setColor(markedColor);
        markPaint.setStrokeWidth(strokeWidth);
        markPaint.setAntiAlias(true);

        vertexPaint = new Paint();
        vertexPaint.setColor(vertexColor);
        vertexPaint.setDither(true);

        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);

        markedArea = new Rect();
        confirmArea = new Rect();
        cancelArea = new Rect();

        ltVer = new RectF();//左上顶点
        rtVer = new RectF();//右上顶点
        lbVer = new RectF();//左下顶点
        rbVer = new RectF();//右下顶点

        confirmBitmap = BitmapFactory.decodeResource(getResources(),confirmButtonRes);
        cancelBitmap = BitmapFactory.decodeResource(getResources(),cancelButtonRes);

        mActionGap = (int) ViewUtil.dp2px(15);

        mGraphicPath = new GraphicPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {  //绘制拖动轨迹
        int width = getWidth();
        int height = getHeight();
        //draw unmarked
        canvas.drawRect(0,0,width,height,unMarkPaint);

        //draw marked
        if (isMarkRect) { //绘制矩形
            if (isValid || !isEnabled()) {
                canvas.drawRect(markedArea,markPaint);
            }
            if (!isEnabled()) {
                return;
            }
            //draw vertex
            if (isValid && isUp) {
                canvas.drawOval(ltVer,vertexPaint);
                canvas.drawOval(rtVer,vertexPaint);
                canvas.drawOval(lbVer,vertexPaint);
                canvas.drawOval(rbVer,vertexPaint);

            }
            //draw button
            if (isValid && isUp) {
                canvas.drawBitmap(confirmBitmap,null,confirmArea,mBitPaint);
                canvas.drawBitmap(cancelBitmap,null,cancelArea,mBitPaint);
            }
        } else { //绘制曲线
            if (!isEnabled()) {
                return;
            }

            if (isUp) {
                if (isValid) {
                    Path path = new Path();
                    if (mGraphicPath.size() > 1) {
                        path.moveTo(mGraphicPath.pathX.get(0), mGraphicPath.pathY.get(0));
                        for (int i = 1; i < mGraphicPath.size(); i++) {
                            path.lineTo(mGraphicPath.pathX.get(i), mGraphicPath.pathY.get(i));
                        }
                    } else {
                        return;
                    }
                    canvas.drawPath(path, markPaint);
                }
            } else { //没有封闭的曲线，自动补全
                if (mGraphicPath.size() > 1) {
                    for (int i = 1; i < mGraphicPath.size(); i++) {
                        canvas.drawLine(mGraphicPath.pathX.get(i - 1), mGraphicPath.pathY.get(i - 1),
                                mGraphicPath.pathX.get(i), mGraphicPath.pathY.get(i), markPaint);
                    }
                }
            }

            //draw button
            if (isValid&&isUp) {
                canvas.drawBitmap(confirmBitmap,null,confirmArea,mBitPaint);
                canvas.drawBitmap(cancelBitmap,null,cancelArea,mBitPaint);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {//记录用户触摸事件
        if (!isEnabled()) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (isMarkRect) {  //矩形
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUp = false;
                    isAdjustMode = false;
                    isMoveMode = false;
                    isButtonClicked = false;
                    isValid = false;
                    adjustNum = 0;
                    downX = x;
                    downY = y;
                    if (mOnClickListener != null) {
                        mOnClickListener.onTouch();
                    }
                    if (isAreaContainPoint(confirmArea,x,y)) { //触摸确认区域
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onConfirm(markedArea);
                        }
                    }else if (isAreaContainPoint(cancelArea,x,y)) { //触摸取消区域
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                            isValid = false;
                            startX = startY = endX = endY = 0;
                            adjustMark(0,0);
                        }
                    } else if (isAreaContainPoint(ltVer,x,y)) {
                        isAdjustMode = true;
                        adjustNum = 1;
                    } else if (isAreaContainPoint(rtVer,x,y)) {
                        isAdjustMode = true;
                        adjustNum = 2;
                    } else if (isAreaContainPoint(lbVer,x,y)) {
                        isAdjustMode = true;
                        adjustNum = 3;
                    } else if (isAreaContainPoint(rbVer,x,y)) {
                        isAdjustMode = true;
                        adjustNum = 4;
                    } else if (markedArea.contains(x,y)) {
                        isMoveMode = true;
                    } else {
                        isMoveMode = false;
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        endX = startX;
                        endY = startY;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isButtonClicked) {
                        break;
                    }
                    adjustMark(x,y);
                    break;
                case MotionEvent.ACTION_UP:
                    isUp = true;
                    if (isButtonClicked) {
                        break;
                    }
                    adjustMark(x,y);
                    startX = markedArea.left;
                    startY = markedArea.top;
                    endX = markedArea.right;
                    endY = markedArea.bottom;

                    if (markedArea.width() > confirmBitmap.getWidth() * 3 + mActionGap * 3 &&
                            markedArea.height() > confirmBitmap.getHeight() * 5) {
                        //显示再选区的内底部
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap,endY - confirmBitmap.getHeight() - mActionGap,
                                endX - mActionGap, endY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2,endY - confirmBitmap.getHeight() - mActionGap,
                                endX - confirmBitmap.getWidth() - mActionGap * 2,endY - mActionGap);
                    } else if (endY > getHeight() - confirmBitmap.getHeight() * 3) {
                        //显示在选区的上面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap,
                                startY - confirmBitmap.getHeight() - mActionGap, endX - mActionGap, startY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, startY - confirmBitmap.getHeight() - mActionGap,
                                endX - confirmBitmap.getWidth() - mActionGap * 2, startY - mActionGap);
                    } else {
                        //显示在选区的下面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, endY + mActionGap,
                                endX - mActionGap, endY + confirmBitmap.getHeight() + mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, endY + mActionGap,
                                endX - confirmBitmap.getWidth() - mActionGap * 2, endY + confirmBitmap.getHeight() + mActionGap);
                    }

                    if (cancelArea.left < 0) {
                        int cancelAreaLeftMargin = Math.abs(cancelArea.left) + mActionGap;
                        cancelArea.left = cancelArea.left + cancelAreaLeftMargin;
                        cancelArea.right = cancelArea.right + cancelAreaLeftMargin;
                        confirmArea.left = confirmArea.left + cancelAreaLeftMargin;
                        confirmArea.right = confirmArea.right + cancelAreaLeftMargin;
                    }

                    if (!isValid) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                        }
                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                    isUp = true;
                    break;
            }
        } else { //曲线
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUp = false;
                    isAdjustMode = false;
                    isMoveMode = false;
                    isButtonClicked = false;
                    isValid = false;
                    adjustNum = 0;
                    downX = 0;
                    downY = 0;
                    if (mOnClickListener != null) {
                        mOnClickListener.onTouch();
                    }
                    if (isAreaContainPoint(confirmArea,x,y)) { //触摸确认区域
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onConfirm(mGraphicPath);
                        }
                    } else if (isAreaContainPoint(cancelArea,x,y)) {//触摸取消区域
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                            isValid = false;
                            startX = startY = endX = endY = 0;
                            adjustMark(0,0);
                        }
                        mGraphicPath.clear();
                    } else {
                        isMoveMode = false;
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        endX = startX;
                        endY = startY;
                        mGraphicPath.clear();
                        mGraphicPath.addPath(x,y);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isButtonClicked) {
                        break;
                    }
                    mGraphicPath.addPath(x,y);
                    break;
                case MotionEvent.ACTION_UP:
                    isUp = true;
                    if (isButtonClicked) {
                        break;
                    }
                    mGraphicPath.addPath(x,y);

                    startX = mGraphicPath.getLeft();
                    startY = mGraphicPath.getTop();
                    endX = mGraphicPath.getRight();
                    endY = mGraphicPath.getBottom();

                    if ((endX - startX)*(endY-startY)>200) {
                        isValid = true;
                    }
                    markedArea.set(startX,startY,endX,endY);

                    if (endY < getHeight() - confirmBitmap.getHeight() * 3){
                        //显示在选区的下面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, endY + mActionGap, endX - mActionGap, endY + confirmBitmap.getHeight() + mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, endY + mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, endY + confirmBitmap.getHeight() + mActionGap);
                    } else
                    if (startY > confirmBitmap.getHeight() * 3) {
                        //显示在选区的上面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, startY - confirmBitmap.getHeight() - mActionGap, endX - mActionGap, startY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, startY - confirmBitmap.getHeight() - mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, startY - mActionGap);
                    }else
//                    if (markedArea.width() > confirmBitmap.getWidth() * 3 + mActionGap * 3 && markedArea.height() > confirmBitmap.getHeight() * 5)
                    {
                        //显示在选区的内底部
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, endY - confirmBitmap.getHeight() - mActionGap, endX - mActionGap, endY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, endY - confirmBitmap.getHeight() - mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, endY - mActionGap);
                    }

                    if (cancelArea.left < 0) {
                        int cancelAreaLeftMargin = Math.abs(cancelArea.left) + mActionGap;
                        cancelArea.left = cancelArea.left + cancelAreaLeftMargin;
                        cancelArea.right = cancelArea.right + cancelAreaLeftMargin;
                        confirmArea.left = confirmArea.left + cancelAreaLeftMargin;
                        confirmArea.right = confirmArea.right + cancelAreaLeftMargin;
                    }

                    if (!isValid) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isUp = true;
                    break;
            }
        }
        postInvalidate();
        return true;
    }

    private boolean isAreaContainPoint(Rect area,int x,int y) {
        Rect newArea = new Rect(area.left-BUTTON_EXTRA_WIDTH,area.top-BUTTON_EXTRA_WIDTH,
                area.right-BUTTON_EXTRA_WIDTH,area.bottom-BUTTON_EXTRA_WIDTH);
        if (newArea.contains(x,y)) {
            return true;
        }
        return false;
    }

    private boolean isAreaContainPoint(RectF area,int x,int y) {
        RectF newArea = new RectF(area.left-BUTTON_EXTRA_WIDTH,area.top-BUTTON_EXTRA_WIDTH,
                area.right-BUTTON_EXTRA_WIDTH,area.bottom-BUTTON_EXTRA_WIDTH);
        if (newArea.contains(x,y)) {
            return true;
        }
        return false;
    }

    private void adjustMark(int x,int y) { //调整标记区域
        if (isAdjustMode) {
            int moveMentX = x-downX;
            int moveMentY = y-downY;

            switch (adjustNum) {
                case 1:
                    startX = startX + moveMentX;
                    startY = startY + moveMentY;
                    break;
                case 2:
                    endX = endX + moveMentX;
                    startY = startY + moveMentY;
                    break;
                case 3:
                    startX = startX + moveMentX;
                    endY = endY + moveMentY;
                    break;
                case 4:
                    endX = endX + moveMentX;
                    endY = endY + moveMentY;
                    break;
            }
            downX = x;
            downY = y;
        } else if (isMoveMode) {
            int moveMentX = x - downX;
            int moveMentY = y - downY;

            startX = startX + moveMentX;
            startY = startY + moveMentY;

            endX = endX + moveMentX;
            endY = endY + moveMentY;

            downX = x;
            downY = y;
        } else {
            endX = x;
            endY = y;
        }
        markedArea.set(Math.min(startX,endX),Math.min(startY,endY),Math.max(startX,endX),Math.max(startY,endY));
        ltVer.set(markedArea.left - vertexWidth / 2,markedArea.top - vertexWidth / 2,markedArea.left + vertexWidth / 2,markedArea.top + vertexWidth / 2);
        rtVer.set(markedArea.right - vertexWidth / 2,markedArea.top - vertexWidth / 2,markedArea.right + vertexWidth / 2,markedArea.top + vertexWidth / 2);
        lbVer.set(markedArea.left - vertexWidth / 2,markedArea.bottom - vertexWidth / 2,markedArea.left + vertexWidth / 2,markedArea.bottom + vertexWidth / 2);
        rbVer.set(markedArea.right - vertexWidth / 2,markedArea.bottom - vertexWidth / 2,markedArea.right + vertexWidth / 2,markedArea.bottom + vertexWidth / 2);
        if (markedArea.height()*markedArea.width()>200) {
            isValid = true;
        } else {
            isValid = false;
        }
    }

    public interface onClickListener {
        void onConfirm(Rect markedArea);
        void onConfirm(GraphicPath path);
        void onCancel();
        void onTouch();
    }

    public void setmOnClickListener(onClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public void setUnmarkedColor(int unmarkedColor) {
        this.unmarkedColor = unmarkedColor;
        unMarkPaint.setColor(unmarkedColor);
        invalidate();
    }

    public void reset() {
        isUp = false;
        isValid = false;
        startX = startY = endX = endY = 0;
        mGraphicPath = new GraphicPath();
        adjustMark(0,0);
    }

    public void setIsMarkRect(boolean isMarkRect) {
        this.isMarkRect = isMarkRect;
    }

    public static class GraphicPath implements Parcelable { //保存用户绘制的图形

        protected GraphicPath(Parcel in) {
            int size = in.readInt();
            int[] x = new int[size];
            int[] y = new int[size];
            in.readIntArray(x);
            in.readIntArray(y);
            pathX = new ArrayList<>();
            pathY = new ArrayList<>();

            for (int i = 0;i < x.length;i++) {
                pathX.add(x[i]);
            }

            for (int i = 0;i < y.length;i++) {
                pathY.add(y[i]);
            }
        }

        public static final Creator<GraphicPath> CREATOR = new Creator<GraphicPath>() {
            @Override
            public GraphicPath createFromParcel(Parcel in) {
                return new GraphicPath(in);
            }

            @Override
            public GraphicPath[] newArray(int size) {
                return new GraphicPath[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(pathX.size());
            dest.writeIntArray(getXArray());
            dest.writeIntArray(getYArray());
        }

        public List<Integer> pathX;
        public List<Integer> pathY;

        public GraphicPath() {
            pathX = new ArrayList<>();
            pathY = new ArrayList<>();
        }

        private int[] getXArray() {
            int[] x = new int[pathX.size()];
            for (int i = 0;i < x.length;i++) {
                x[i] = pathX.get(i);
            }
            return x;
        }

        private int[] getYArray() {
            int[] y = new int[pathY.size()];
            for (int i = 0;i <y.length;i++) {
                y[i] = pathY.get(i);
            }
            return y;
        }

        public void addPath(int x,int y) {
            pathX.add(x);
            pathY.add(y);
        }

        public void clear() {
            pathX.clear();
            pathY.clear();
        }

        public int getTop() {
            int min = pathY.size()>0?pathY.get(0):0;
            for (int y:pathY) {
                if (y < min) {
                    min = y;
                }
            }
            return min;
        }

        public int getLeft() {
            int min = pathX.size()>0?pathX.get(0):0;
            for (int x:pathX) {
                if (x < min) {
                    min = x;
                }
            }
            return min;
        }

        public int getBottom() {
            int max = pathY.size()>0?pathY.get(0):0;
            for (int y:pathY) {
                if (y > max) {
                    max = y;
                }
            }
            return max;
        }

        public int getRight() {
            int max = pathX.size()>0?pathX.get(0):0;
            for (int x:pathX) {
                if (x > max) {
                    max = x;
                }
            }
            return max;
        }

        public int size() {
            return pathY.size();
        }
    }
}
