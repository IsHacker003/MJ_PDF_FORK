package com.github.barteksc.pdfviewer.scroll;

import android.view.View;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

public interface ScrollHandle {

    /**
     * Used to move the handle, called internally by PDFView
     *
     * @param position current scroll ratio between 0 and 1
     */
    void setScroll(float position);

    /**
     * Method called by PDFView after setting scroll handle.
     * Do not call this method manually.
     * For usage sample see {@link DefaultScrollHandle}
     *
     * @param pdfView PDFView instance
     */
    void setupLayout(PDFView pdfView);

    /**
     * Method called by PDFView when handle should be removed from layout
     * Do not call this method manually.
     */
    void destroyLayout();

    /**
     * Set page number displayed on handle
     *
     * @param pageNum page number
     */
    void setPageNum(int pageNum);

    /**
     * Get handle visibility
     *
     * @return true if handle is visible, false otherwise
     */
    boolean shown();

    /**
     * Show handle
     */
    void show();

    /**
     * Hide handle immediately
     */
    void hide();

    /**
     * Hide handle after some time (defined by implementation)
     */
    void hideDelayed();

    /**
     * The functions below I made (with almost zero planning, just to get things done.
     *
     * the custom show/hide are called only when the view is tapped
     */
    boolean customShown();
    void customShow();
    void customHide();
    void permanentHide();
    void disablePermanentHide();
    void cancelHideRunner();
    void activateHandlerHideDelayed();
    TextView getReadingProgressText();

    void setOnTouchListener(View.OnTouchListener onTouchListener);

    void setOnClickListener(View.OnClickListener onClickListener);
}
