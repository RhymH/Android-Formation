package accelerometer.com.movingDetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CanvasView extends View {

    Context context;
    private Paint mPaint;
    private static final float TOLERANCE = 5;
    private int m_width;
    private int m_height;
    private int m_xCircle;
    private int m_yCircle;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        m_width = w;
        m_height = h;
        m_xCircle = w/2;
        m_yCircle = h/2;
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.LTGRAY);
        canvas.drawCircle(m_xCircle, m_yCircle, 100, mPaint);
    }

    public void clearCanvas() {
        invalidate();
    }

    public void setCirclePosition(float xPercent, float yPercent) {
        m_xCircle = (int)(100 + xPercent*(m_width-200));
        m_yCircle = (int)(100 + yPercent*(m_height-200));

        invalidate();
    }



}