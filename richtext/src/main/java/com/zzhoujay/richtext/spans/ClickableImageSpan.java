package com.zzhoujay.richtext.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.view.View;

import com.zzhoujay.richtext.callback.OnImageClickListener;
import com.zzhoujay.richtext.callback.OnImageLongClickListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by zhou on 2016/11/17.
 * ClickableImageSpan 支持点击的ImageSpan
 */
public class ClickableImageSpan extends ImageSpan implements LongClickableSpan {

    private float x;
    private final int position;
    private final List<String> imageUrls;
    private final WeakReference<OnImageLongClickListener> onImageLongClickListenerWeakReference;
    private final WeakReference<OnImageClickListener> onImageClickListenerWeakReference;

    public ClickableImageSpan(Drawable drawable, ClickableImageSpan clickableImageSpan, OnImageClickListener onImageClickListener, OnImageLongClickListener onImageLongClickListener) {
        super(drawable, clickableImageSpan.getSource());
        this.imageUrls = clickableImageSpan.imageUrls;
        this.position = clickableImageSpan.position;
        this.onImageClickListenerWeakReference = new WeakReference<>(onImageClickListener);
        this.onImageLongClickListenerWeakReference = new WeakReference<>(onImageLongClickListener);
    }

    public ClickableImageSpan(Drawable drawable, List<String> imageUrls, int position, OnImageClickListener onImageClickListener, OnImageLongClickListener onImageLongClickListener) {
        super(drawable, imageUrls.get(position));
        this.imageUrls = imageUrls;
        this.position = position;
        this.onImageClickListenerWeakReference = new WeakReference<>(onImageClickListener);
        this.onImageLongClickListenerWeakReference = new WeakReference<>(onImageLongClickListener);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getDrawable();
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;

            //对于这里我表示,我不知道为啥是这样。不应该是fontHeight/2?但是只有fontHeight/4才能对齐
            //难道是因为TextView的draw的时候top和bottom是大于实际的？具体请看下图
            //所以fontHeight/4是去除偏差?
            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;

            fontMetricsInt.ascent = -bottom;
            fontMetricsInt.top = -bottom;
            fontMetricsInt.bottom = top;
            fontMetricsInt.descent = top;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        this.x = x;
        Drawable drawable = getDrawable();
        canvas.save();
        int transY = 0;
        //获得将要显示的文本高度-图片高度除2等居中位置+top(换行情况)，如果要改为底部对齐，则去掉注释调上面的getSize()方法
        transY = ((bottom - top) - drawable.getBounds().bottom) / 2 + top - 10;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

    public boolean clicked(int position) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Rect rect = drawable.getBounds();
            if (position <= rect.right + x && position >= rect.left + x) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onClick(View widget) {
        OnImageClickListener onImageClickListener = onImageClickListenerWeakReference.get();
        if (onImageClickListener != null) {
            onImageClickListener.imageClicked(imageUrls, position);
        }
    }

    @Override
    public boolean onLongClick(View widget) {
        OnImageLongClickListener onImageLongClickListener = onImageLongClickListenerWeakReference.get();
        return onImageLongClickListener != null && onImageLongClickListener.imageLongClicked(imageUrls, position);
    }
}
