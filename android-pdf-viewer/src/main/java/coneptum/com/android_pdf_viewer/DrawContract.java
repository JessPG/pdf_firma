package coneptum.com.android_pdf_viewer;

/**
 * Created by coneptum on 2/09/16.
 */
public interface DrawContract {
    // controlador avisa a vista
    interface View {
        void setVisto();
    }

    // vista avisa a controlador
    interface ActionListener {
        void erase();
    }
 }
