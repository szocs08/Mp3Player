package hu.application.gbor.mp3player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

public class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mAlpha;

    CustomDividerItemDecoration(Context context, int color, float heightDp) {
        mPaint = new Paint();
        mPaint.setColor(color);
        mAlpha = mPaint.getAlpha();
        final float thickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                heightDp, context.getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(thickness);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0,0,0,(int) mPaint.getStrokeWidth());
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int offset = (int) (mPaint.getStrokeWidth() / 2);

        for (int i = 0; i < parent.getChildCount()-1; i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            final int position = params.getViewAdapterPosition();
            mPaint.setAlpha((int) view.getAlpha() * mAlpha);

            if (position < state.getItemCount()) {
                c.drawLine(view.getLeft() + view.getTranslationX(),
                        view.getBottom() + offset + view.getTranslationY(),
                        view.getRight() + view.getTranslationX(),
                        view.getBottom() + offset + view.getTranslationY(),
                        mPaint) ;
            }
        }
    }

}
