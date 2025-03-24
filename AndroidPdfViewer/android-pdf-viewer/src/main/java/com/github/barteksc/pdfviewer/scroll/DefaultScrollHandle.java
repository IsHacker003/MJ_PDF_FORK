package com.github.barteksc.pdfviewer.scroll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.R;
import com.github.barteksc.pdfviewer.util.Util;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {

    private final static int HANDLE_LONG = 65;
    private final static int HANDLE_SHORT = 40;
    private final static int DEFAULT_TEXT_SIZE = 16;

    private float relativeHandlerMiddle = 0f;

    protected TextView textView;
    protected Context context;
    private boolean inverted;
    private PDFView pdfView;
    private float currentPos;

    private boolean isClickEvent = false;

    public TextView readingProgressText;

    private final Handler handler = new Handler();
    private final Runnable hidePageScrollerRunnable = new Runnable() {
        @Override
        public void run() {
            customHide();
        }
    };
    boolean permanentHidden = false;
    private View.OnTouchListener customOnTouchListener;
    private View.OnClickListener customOnClickListener;

    public DefaultScrollHandle(Context context) {
        this(context, false);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.context = context;
        this.inverted = inverted;
        textView = new TextView(context);
        setVisibility(INVISIBLE);
        setCustomColorForText(context);
//        setTextColor(Color.BLACK);
        //setTextColor(Color.parseColor("#CDCDCD"));
        setTextSize(DEFAULT_TEXT_SIZE);
    }

    private void setCustomColorForText(Context context) {
        TypedValue typedValue = new TypedValue();
        boolean found = context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true);
        if (found) {
            int colorOnBackground = typedValue.data;
            setTextColor(colorOnBackground);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setupLayout(PDFView pdfView) {
        int align, width, height;
        Drawable background;
        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical()) {
            width = HANDLE_LONG;
            height = HANDLE_SHORT;
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_left);
            } else { // right
                align = ALIGN_PARENT_RIGHT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_right);
            }
        } else {
            width = HANDLE_SHORT;
            height = HANDLE_LONG;
            if (inverted) { // top
                align = ALIGN_PARENT_TOP;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_top);
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_bottom);
            }
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(background);
        } else  {
            setBackground(background);
        }

        LayoutParams lp = new LayoutParams(Util.getDP(context, width), Util.getDP(context, height));
        lp.setMargins(0, 0, 0, 0);

        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(textView, tvlp);

        lp.addRule(align);
        pdfView.addView(this, lp);

        // Custom TextView to shor reading progress as 250/500 (50%)
        MaterialTextView textView = (MaterialTextView) LayoutInflater.from(context)
                .inflate(R.layout.read_progress_text_view, null);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        int margin = 50;
        layoutParams.setMargins(margin, margin, margin, margin);
        textView.setText(getProgressText());
        readingProgressText = textView;
        pdfView.addView(readingProgressText, layoutParams);

        this.pdfView = pdfView;
    }

    @Override
    public TextView getReadingProgressText() {
        return readingProgressText;
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        customOnTouchListener = onTouchListener;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        customOnClickListener = onClickListener;
        super.setOnClickListener(onClickListener);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void destroyLayout() {
        pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        if (pdfView != null) {
            setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
        }
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pdfViewSize = pdfView.getHeight();
        } else {
            pdfViewSize = pdfView.getWidth();
        }
        pos -= relativeHandlerMiddle;

        if (pos < 0) {
            pos = 0;
        } else if (pos > pdfViewSize - Util.getDP(context, HANDLE_SHORT)) {
            pos = pdfViewSize - Util.getDP(context, HANDLE_SHORT);
        }

        if (pdfView.isSwipeVertical()) {
            setY(pos);
        } else {
            setX(pos);
        }

        calculateMiddle();
        invalidate();
    }

    private void calculateMiddle() {
        float pos, viewSize, pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pos = getY();
            viewSize = getHeight();
            pdfViewSize = pdfView.getHeight();
        } else {
            pos = getX();
            viewSize = getWidth();
            pdfViewSize = pdfView.getWidth();
        }
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize;
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 3000);
    }

    @Override
    public void setPageNum(int pageNum) {
        String text = String.valueOf(pageNum);
        if (!textView.getText().equals(text)) {
            textView.setText(text);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == VISIBLE;
    }

    // I disables show and hide for it, only thru my own custom methods, which are called only
    // when the view is tapped
    @Override
    public void show() {
//        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
//        setVisibility(INVISIBLE);
    }

    @Override public void permanentHide() { permanentHidden = true; setVisibility(INVISIBLE); }
    @Override public void disablePermanentHide() { permanentHidden = false; }
    @Override public boolean customShown() { return getVisibility() == VISIBLE; }
    @Override public void customShow() { if (!permanentHidden) setVisibility(VISIBLE); }
    @Override public void customHide() { if (!permanentHidden) setVisibility(INVISIBLE); }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    /**
     * @param size text size in dp
     */
    public void setTextSize(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClickIfClickEvent(event);

        if (customOnTouchListener != null) {
            customOnTouchListener.onTouch(this, event);
        }

        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();
                }
                else {
                    currentPos = event.getRawX() - getX();
                }
                readingProgressText.setVisibility(VISIBLE);
                readingProgressText.setText(getProgressText());
            case MotionEvent.ACTION_MOVE:
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getHeight(), false);
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getWidth(), false);
                }
                readingProgressText.setText(getProgressText());
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                readingProgressText.setVisibility(GONE);
                pdfView.performPageSnap();
                return true;
        }

        return super.onTouchEvent(event);
    }

    @NonNull
    private String getProgressText() {
        int read = pdfView != null ? pdfView.getCurrentPage() + 1 : 0;
        int total = pdfView != null ? pdfView.getPageCount() : 0;
        String progress = read + "/" + total;
        return progress;
//        String percentage = calculatePercentage(read, total);
//        return String.format("%s (%s)", progress, percentage);
    }

    public static String calculatePercentage(int read, int total) {
        if (total <= 0 || read < 0) {
            return "";
        }
        double result = ((double) read / total) * 100;
        if (result % 1 == 0) {
            return (int) result + "%";                 // 23%
        } else {
            return String.format(Locale.US, "%.1f%%", result);  // 23.5%
        }
    }

    private void performClickIfClickEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isClickEvent = false;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            isClickEvent = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP && !isClickEvent) {
            performClick();
        }
    }


    @Override
    public void cancelHideRunner() {
        handler.removeCallbacks(hidePageScrollerRunnable);
    }

    @Override
    public void activateHandlerHideDelayed() {
        hideDelayed();
    }
}
