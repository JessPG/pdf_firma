/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package coneptum.com.android_pdf_viewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import coneptum.com.android_pdf_viewer.listener.OnDrawListener;
import coneptum.com.android_pdf_viewer.listener.OnPageChangeListener;
import coneptum.com.android_pdf_viewer.scroll.ScrollHandle;

import static coneptum.com.android_pdf_viewer.util.Constants.Pinch.MAXIMUM_ZOOM;
import static coneptum.com.android_pdf_viewer.util.Constants.Pinch.MINIMUM_ZOOM;

/**
 * This Manager takes care of moving the PDFView,
 * set its zoom track user actions.
 */
class DragPinchManager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener, OnDrawListener, OnPageChangeListener {

    // constants
    private static final int SIGNATURE_WIDTH = 286;
    private static final int SIGNATURE_HEIGHT = 68;
    private static final int SIGNATURE_HOR_MARGIN = 21;
    private static final int SIGNATURE_VER_MARGIN = 24;
    private static final float TOUCH_TOLERANCE = 4;

    private int pageH;

    private PDFView pdfView;
    private AnimationManager animationManager;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private boolean isSwipeEnabled;

    private boolean swipeVertical;

    private boolean scrolling;
    private boolean isLastPage;

    private Paint mPaint;
    private Bitmap mBitmap;
    private Bitmap hidden;
    private Canvas mCanvas;
    private Path mPath;
    private Paint   mBitmapPaint;
    private float mX, mY;


    public DragPinchManager(PDFView pdfView, AnimationManager animationManager) {
        this.pdfView = pdfView;
        this.animationManager = animationManager;
        this.isSwipeEnabled = false;
        this.swipeVertical = pdfView.isSwipeVertical();
        gestureDetector = new GestureDetector(pdfView.getContext(), this);
        scaleGestureDetector = new ScaleGestureDetector(pdfView.getContext(), this);

        scrolling = false;
        isLastPage = false;

        // trazado
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);


        pdfView.setOnTouchListener(this);
    }

    public void enableDoubletap(boolean enableDoubletap) {
        if (enableDoubletap) {
            gestureDetector.setOnDoubleTapListener(this);
        } else {
            gestureDetector.setOnDoubleTapListener(null);
        }
    }

    public boolean isZooming() {
        return pdfView.isZooming();
    }

    private boolean isPageChange(float distance) {
        return Math.abs(distance) > Math.abs(pdfView.toCurrentScale(swipeVertical ? pdfView.getOptimalPageHeight() : pdfView.getOptimalPageWidth()) / 2);
    }

    public void setSwipeEnabled(boolean isSwipeEnabled) {
        this.isSwipeEnabled = isSwipeEnabled;
    }

    public void setSwipeVertical(boolean swipeVertical) {
        this.swipeVertical = swipeVertical;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        ScrollHandle ps = pdfView.getScrollHandle();
        if (ps != null && !pdfView.documentFitsView()) {
            if (!ps.shown()) {
                ps.show();
            } else {
                ps.hide();
            }
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!pdfView.isScrollLock()) {
            if (pdfView.getZoom() < pdfView.getMidZoom()) {
                pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.getMidZoom());
            } else if (pdfView.getZoom() < pdfView.getMaxZoom()) {
                pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.getMaxZoom());
            } else {
                pdfView.resetZoomWithAnimation();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        animationManager.stopFling();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!pdfView.isScrollLock()) {
            scrolling = true;
            if (isZooming() || isSwipeEnabled) {
                pdfView.moveRelativeTo(-distanceX, -distanceY);
            }
            pdfView.loadPageByOffset();

            return true;
        } else {
            return false;
        }
    }

    public void onScrollEnd(MotionEvent event) {
        pdfView.loadPages();
        hideHandle();
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!pdfView.isScrollLock()) {
            int xOffset = (int) pdfView.getCurrentXOffset();
            int yOffset = (int) pdfView.getCurrentYOffset();
            animationManager.startFlingAnimation(xOffset,
                    yOffset, (int) (velocityX),
                    (int) (velocityY),
                    xOffset * (swipeVertical ? 2 : pdfView.getPageCount()), 0,
                    yOffset * (swipeVertical ? pdfView.getPageCount() : 2), 0);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!pdfView.isScrollLock()) {
            float dr = detector.getScaleFactor();
            float wantedZoom = pdfView.getZoom() * dr;
            if (wantedZoom < MINIMUM_ZOOM) {
                dr = MINIMUM_ZOOM / pdfView.getZoom();
            } else if (wantedZoom > MAXIMUM_ZOOM) {
                dr = MAXIMUM_ZOOM / pdfView.getZoom();
            }
            pdfView.zoomCenteredRelativeTo(dr, new PointF(detector.getFocusX(), detector.getFocusY()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        pdfView.loadPages();
        hideHandle();
    }

    private boolean isInsideSignature(int x, int y) {
        if (x>=SIGNATURE_HOR_MARGIN &&
                x<=SIGNATURE_HOR_MARGIN+SIGNATURE_WIDTH &&
                y<=pageH-SIGNATURE_VER_MARGIN &&
                y>=pageH-SIGNATURE_VER_MARGIN-SIGNATURE_HEIGHT) {
            return true;
        }
        return false;
    }


    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        if (isInsideSignature((int)x, (int)y)) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        } else {
            mX = x;
            mY = y;
            mCanvas.drawPath(mPath,  mPaint);
            mPath.reset();
            mPath.moveTo(x, y);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean retVal = scaleGestureDetector.onTouchEvent(event);
        retVal = gestureDetector.onTouchEvent(event) || retVal;

        if (!pdfView.isScrollLock()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (scrolling) {
                    scrolling = false;
                    onScrollEnd(event);
                }
            }
        } else if (pdfView.isScrollLock() && isLastPage) {
            // relativos al canvas
            float x = event.getX()-pdfView.getCurrentXOffset();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    pdfView.redraw();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    pdfView.redraw();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    pdfView.redraw();
                    break;
            }
        }
        return retVal;
    }

    private void hideHandle() {
        if (pdfView.getScrollHandle() != null && pdfView.getScrollHandle().shown()) {
            pdfView.getScrollHandle().hideDelayed();
        }
    }

    // Se activa cuando se llama al método invalidate() de la vista.
    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage, float zoom) {
        if (isLastPage) {
            Bitmap finalBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(SIGNATURE_WIDTH*zoom), (int)(SIGNATURE_HEIGHT*zoom), false);
            canvas.drawBitmap(finalBitmap, SIGNATURE_HOR_MARGIN*zoom, pageHeight-(SIGNATURE_HEIGHT+SIGNATURE_VER_MARGIN)*zoom, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        } else {
            canvas.drawBitmap(hidden, SIGNATURE_HOR_MARGIN, pageHeight-SIGNATURE_HEIGHT-SIGNATURE_VER_MARGIN, mBitmapPaint);
        }
    }

    // Se activa cuando se dibuja la vista.
    @Override
    public void onSize(int pageWidth, int pageHeight) {
        this.pageH = pageHeight;
        mBitmap = Bitmap.createBitmap(SIGNATURE_WIDTH, SIGNATURE_HEIGHT, Bitmap.Config.ARGB_8888);
        hidden = Bitmap.createBitmap(SIGNATURE_WIDTH, SIGNATURE_HEIGHT, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.translate(-SIGNATURE_HOR_MARGIN, -pageHeight+SIGNATURE_HEIGHT+SIGNATURE_VER_MARGIN);
    }

    // Se activa cuando cambiamos de página.
    @Override
    public void onPageChanged(int page, int pageCount) {
        if (page==pageCount-1) {
            this.isLastPage=true;
        } else {
            this.isLastPage = false;
        }
    }
}
