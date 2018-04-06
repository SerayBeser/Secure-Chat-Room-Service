package Chat;
//  ChatRoomPanel.java
//
//  Last modified 1/30/2000 by Alan Frindell
//
//  GUI class for the chat room.
//
//  You should not need to modify this class.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatRoomPanel extends JPanel {

    private JTextArea _inputArea;
    private JTextArea _outputArea;
    private ChatClient _client;

    ChatRoomPanel(ChatClient client) {
        _client = client;

        try {

            componentInit();

        } catch (Exception e) {

            System.out.println( "[ERROR]: ChatRoomPanel error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void componentInit() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout( gridBag );

        addLabel( gridBag, "Chat Room: ", SwingConstants.LEFT, 0 );
        _outputArea = addArea( gridBag, new Dimension( 400, 192 ), 1 );
        _outputArea.setEditable( false );

        addLabel( gridBag, "Your Message: ", SwingConstants.LEFT, 2 );
        _inputArea = addArea( gridBag, new Dimension( 400, 96 ), 3 );
        _inputArea.addKeyListener( new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                inputKeyTyped( e );
            }
        } );

        JButton _quitButton = new JButton( "Leave Chat Room" );
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

    private void addLabel(GridBagLayout gridBag, String labelStr, int align, int y) {
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel( labelStr );
        if (align == SwingConstants.LEFT) {
            c.insets = new Insets( 10, 4, 0, 4 );
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.gridheight = 1;
        gridBag.setConstraints( label, c );
        add( label );

    }

    private JTextArea addArea(GridBagLayout gridBag, Dimension prefSize, int y) {
        JScrollPane scroller;
        JTextArea area = new JTextArea();
        GridBagConstraints c = new GridBagConstraints();

        area.setLineWrap( true );
        area.setWrapStyleWord( true );
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets( 4, 4, 4, 4 );
        c.gridx = 0;
        c.gridy = y;
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

    private void inputKeyTyped(KeyEvent e) {
        String msg;

        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            msg = _inputArea.getText();
            _inputArea.setText( "" );
            if (msg.length() == 0) {
                return;
            }
            _client.sendMessage( msg );
        }
    }

    private void quit() {
        _client.quit();

    }
}
