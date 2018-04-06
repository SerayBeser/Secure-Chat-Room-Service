package Chat;
//
//  CertificateAuthorityActivity.java
//
//  Written by : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  GUI class for displaying information about the Certificate Authority
//
//  You should not need to modify this class.

import java.awt.*;
import javax.swing.*;

public class CertificateAuthorityActivityPanel extends JPanel {

    private JTextArea _outputArea;
    private CertificateAuthority _ca;

    CertificateAuthorityActivityPanel(CertificateAuthority ca) {
        _ca = ca;

        try {
            componentInit();
        } catch (Exception e) {
            System.out.println( "[ERROR]: CertificateAuthorityActivity error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void componentInit() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout( gridBag );

        addLabel( gridBag, SwingConstants.LEFT );
        _outputArea = addArea( gridBag, new Dimension( 400, 192 ) );
        _outputArea.setEditable( false );

        JButton _quitButton = new JButton( "Exit" );
        _quitButton.addActionListener( e -> quit() );
        c.insets = new Insets( 4, 4, 4, 4 );
        c.weighty = 1.0;
        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.SOUTHEAST;
        gridBag.setConstraints( _quitButton, c );
        add( _quitButton );

    }

    private void addLabel(GridBagLayout gridBag, int align) {
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel( "CA Activity: " );
        if (align == SwingConstants.LEFT) {
            c.insets = new Insets( 10, 4, 0, 4 );
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        gridBag.setConstraints( label, c );
        add( label );

    }

    private JTextArea addArea(GridBagLayout gridBag, Dimension prefSize) {
        JScrollPane scroller;
        JTextArea area = new JTextArea();
        GridBagConstraints c = new GridBagConstraints();

        area.setLineWrap( true );
        area.setWrapStyleWord( true );
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets( 4, 4, 4, 4 );
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        scroller = new JScrollPane( area );
        scroller.setPreferredSize( prefSize );
        gridBag.setConstraints( scroller, c );
        add( scroller );

        return area;
    }

    public JTextArea getOutputArea() {

        return _outputArea;
    }

    private void quit() {

        _ca.quit();
    }
}
