package coneptum.com.pdf_firma;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.shockwave.pdfium.PdfDocument;
import java.util.List;

import coneptum.com.android_pdf_viewer.DrawContract;
import coneptum.com.android_pdf_viewer.PDFView;
import coneptum.com.android_pdf_viewer.listener.OnLoadCompleteListener;

public class MainActivity extends Activity implements OnLoadCompleteListener, DrawContract.View{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SAMPLE_FILE = "sample.pdf";
    private static final String DOWNLOADS_FOLDER = "/mnt/sdcard/download/";
    private static final int LAST_PAGE = 1000;

    private PDFView pdfView;
    private DrawContract.ActionListener actionListener;

    private ToggleButton visto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.pdfView = (PDFView) findViewById(R.id.pdfView);

        // comunicadores
        this.actionListener = this.pdfView.getDragPinchManager();
        this.pdfView.setDragPinchManagerView(this);

        this.visto = (ToggleButton) findViewById(R.id.visto);

        ToggleButton button = (ToggleButton) findViewById(R.id.firmar);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pdfView.setScrollLock(isChecked);
            }
        });

        Button erase = (Button) findViewById(R.id.borrar);
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visto.setChecked(false);
                actionListener.erase();
            }
        });

        String path = getIntent().getExtras().getString("path");
        Uri uri = Uri.parse(path);
        displayPdf(uri);
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

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
//        Log.e(TAG, "title = " + meta.getTitle());
//        Log.e(TAG, "author = " + meta.getAuthor());
//        Log.e(TAG, "subject = " + meta.getSubject());
//        Log.e(TAG, "keywords = " + meta.getKeywords());
//        Log.e(TAG, "creator = " + meta.getCreator());
//        Log.e(TAG, "producer = " + meta.getProducer());
//        Log.e(TAG, "creationDate = " + meta.getCreationDate());
//        Log.e(TAG, "modDate = " + meta.getModDate());

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

    @Override
    public void setVisto() {
        this.visto.setChecked(true);
    }
}
