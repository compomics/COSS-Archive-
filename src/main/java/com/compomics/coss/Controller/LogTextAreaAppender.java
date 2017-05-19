package com.compomics.coss.Controller;

import com.compomics.coss.View.MainFrame;
import javax.swing.SwingUtilities;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender class for writing log messages to a JTextArea.
 *
 * @author Niels Hulstaert
 */
public class LogTextAreaAppender extends WriterAppender {

    /**
     * The dialog to log to.
     */
    private MainFrame frm;

    public void setLogArea(MainFrame frame) {
        this.frm = frame;
    }

    @Override
    public void append(LoggingEvent event) {
        final String message = this.layout.format(event);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frm.gettxtLog().append(message);
                //repaint view
                frm.validate();
                frm.repaint();
            }
        });
    }

}
