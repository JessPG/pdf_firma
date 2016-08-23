package coneptum.com.pdf_firma;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import coneptum.com.android_pdf_viewer.PDFView;
import coneptum.com.android_pdf_viewer.listener.OnLoadCompleteListener;
import coneptum.com.android_pdf_viewer.listener.OnPageChangeListener;

public class MainActivity extends AppCompatActivity implements OnLoadCompleteListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SAMPLE_FILE = "sample.pdf";
    private static final String DOWNLOADS_FOLDER = "/mnt/sdcard/download/";
    private static final int LAST_PAGE = 0;

    private String sampleBase64;

    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.pdfView = (PDFView) findViewById(R.id.pdfView);

        ToggleButton button = (ToggleButton) findViewById(R.id.b1);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pdfView.setScrollLock(isChecked);
            }
        });

        // TODO obtenir base 64 real
        sampleBase64 = getString(R.string.demo);
        getPdfFromBase64(sampleBase64);
    }

    /**
     * Carga el pdf
     * @param uri
     */
    private void displayPdf(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(LAST_PAGE)
                //.onPageChange(this)
                .enableAnnotationRendering(true)
                //.onDraw(this)
                .onLoad(this)
                .load();

        //pdfView.useBestQuality(true);

    }

    /**
     * Genera el archivo pdf desde un base64
     * @param base64
     */
    private void getPdfFromBase64 (String base64) {
        final File file = new File(DOWNLOADS_FOLDER + SAMPLE_FILE);
        byte[] pdfAsBytes = Base64.decode(base64, 0);
        FileOutputStream os;
        try {
            os = new FileOutputStream(file, false);
            os.write(pdfAsBytes);
            os.flush();
            os.close();
            Uri uri = Uri.fromFile(file);
            displayPdf(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
}
