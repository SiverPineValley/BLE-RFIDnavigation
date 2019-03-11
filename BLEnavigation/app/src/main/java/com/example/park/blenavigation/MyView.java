package com.example.park.blenavigation;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class MyView extends View {

    Paint paint;
    Path path;
    private int[][] pointLocation = { {198, 295}, {198, 184}, {198,123}, {111,123}, {165,65}, {225,65}, {268,123}, {330,123}, {360,123} }; // Point Location

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();
    }

    public void drawLine(int fromX, int fromY, int toX, int toY) {
        path.moveTo(fromX, fromY);
        path.lineTo(toX, toY);
    }

    public void drawNewPath(int[] paths, int dm) {

        // (DP값) * (int)dm.density
        // 0: (198, 295), 1: (198, 184), 2: (198, 123), 3: (111, 123)
        // 4: (198, 65), 5: (225,65), 6: (268,123), 7: (330, 123), 8: (360,123)

        // 124(0): (198, 295), 101: (198, 265), 102: (198, 223), 103(1): (198, 184), 104: (215, 184), 105: (198, 152), 106(2): (198, 123)
        // 107: (111, 156), 108(3): (111, 123), 109: (111, 98), 110(4): (198, 65), 111: (212, 65), 112(5): (225,65), 113: (233, 123)
        // 114(6): (268,123), 115: (268, 98), 116: (268, 156), 117: (300, 123), 118(7): (330, 123), 119: (330, 156), 120: (345, 123)
        // 121(8): (360,123), 122: (360, 98), 123: (360, 156)

        path.rewind();

        for(int i = 0; i < paths.length-1; i++) {
            int now = paths[i];
            int next = paths[i+1];

            if( ((now == 24) && (next == 1)) || ((now == 1) && (next == 24)) ) this.drawLine(198*dm,295*dm,198*dm,265*dm);
            else if( ((now == 1) && (next == 2)) || ((now == 2) && (next == 1)) ) this.drawLine(198*dm, 265*dm, 198*dm, 223*dm);
            else if( ((now == 2) && (next == 3)) || ((now == 3) && (next == 2)) ) this.drawLine(198*dm, 223*dm, 198*dm, 184*dm);
            else if( ((now == 3) && (next == 4)) || ((now == 4) && (next == 3)) ) this.drawLine(198*dm, 184*dm, 215*dm, 184*dm);
            else if( ((now == 3) && (next == 5)) || ((now == 5) && (next == 3)) ) this.drawLine(198*dm, 184*dm, 198*dm, 152*dm);
            else if( ((now == 5) && (next == 6)) || ((now == 6) && (next == 5)) ) this.drawLine(198*dm, 152*dm, 198*dm, 123*dm);
            else if( ((now == 6) && (next == 8)) || ((now == 8) && (next == 6)) ) this.drawLine(198*dm, 123*dm, 111*dm, 123*dm);
            else if( ((now == 7) && (next == 8)) || ((now == 8) && (next == 7)) ) this.drawLine(111*dm, 123*dm, 111*dm, 156*dm);
            else if( ((now == 8) && (next == 9)) || ((now == 9) && (next == 8)) ) this.drawLine(111*dm, 123*dm, 111*dm, 98*dm);
            else if( ((now == 6) && (next == 10)) || ((now == 10) && (next == 6)) ) this.drawLine(198*dm, 123*dm, 198*dm, 65*dm);
            else if( ((now == 10) && (next == 11)) || ((now == 11) && (next == 10)) ) this.drawLine(198*dm, 65*dm, 212*dm, 65*dm);
            else if( ((now == 11) && (next == 12)) || ((now == 12) && (next == 11)) ) this.drawLine(212*dm, 65*dm, 225*dm, 65*dm);
            else if( ((now == 6) && (next == 13)) || ((now == 13) && (next == 6)) ) this.drawLine(198*dm, 123*dm, 233*dm, 123*dm);
            else if( ((now == 13) && (next == 14)) || ((now == 14) && (next == 13)) ) this.drawLine(233*dm, 123*dm, 268*dm, 123*dm);
            else if( ((now == 14) && (next == 15)) || ((now == 15) && (next == 14)) ) this.drawLine(268*dm, 123*dm, 268*dm, 98*dm);
            else if( ((now == 14) && (next == 16)) || ((now == 16) && (next == 14)) ) this.drawLine(268*dm, 123*dm, 268*dm, 156*dm);
            else if( ((now == 14) && (next == 17)) || ((now == 17) && (next == 14)) ) this.drawLine(268*dm, 123*dm, 300*dm, 123*dm);
            else if( ((now == 17) && (next == 18)) || ((now == 18) && (next == 17)) ) this.drawLine(300*dm, 123*dm, 330*dm, 123*dm);
            else if( ((now == 18) && (next == 19)) || ((now == 19) && (next == 18)) ) this.drawLine(330*dm, 123*dm, 330*dm, 156*dm);
            else if( ((now == 18) && (next == 20)) || ((now == 20) && (next == 18)) ) this.drawLine(330*dm, 123*dm, 345*dm, 123*dm);
            else if( ((now == 20) && (next == 21)) || ((now == 21) && (next == 20)) ) this.drawLine(345*dm, 123*dm, 360*dm, 123*dm);
            else if( ((now == 21) && (next == 22)) || ((now == 22) && (next == 21)) ) this.drawLine(360*dm, 123*dm, 360*dm, 98*dm);
            else if( ((now == 21) && (next == 23)) || ((now == 23) && (next == 21)) ) this.drawLine(360*dm, 123*dm, 360*dm, 156*dm);

            invalidate();
        }
    }

    public void rewind() {
        path.rewind();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

}
