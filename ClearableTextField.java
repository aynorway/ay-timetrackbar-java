import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClearableTextField extends JTextField {

    private final String CLEAR_ICON = "\u2715"; // ✕ \u2715 
    private boolean showIcon = false;
    private boolean hoverOverTextField = false;

    public ClearableTextField() {

        ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(fb, offset, string, attr);
                showIcon = !getText().isEmpty();
                repaint();
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                showIcon = !getText().isEmpty();
                repaint();
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                showIcon = !getText().isEmpty();
                repaint();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePos = e.getPoint();
                FontMetrics metrics = getFontMetrics(getFont());
                int x = getWidth() - metrics.stringWidth(CLEAR_ICON) - 5;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                Rectangle r = new Rectangle(x, y - metrics.getAscent(), metrics.stringWidth(CLEAR_ICON),
                        metrics.getHeight());

                if (showIcon && r.contains(mousePos)) {
                    setText("");
                    showIcon = false;
                    repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hoverOverTextField = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverOverTextField = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showIcon) {
            FontMetrics metrics = g.getFontMetrics(getFont());
            int x = getWidth() - metrics.stringWidth(CLEAR_ICON) - 5;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            if (hoverOverTextField) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }
            g.drawString(CLEAR_ICON, x, y);
        }
    }
}
