import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClearableTextField extends JTextField { // 定义了一个新类ClearableTextField，它继承自JTextField。这意味ClearableTextField有JTextField的所有功能，还可再加上额外添加的功能。

    private final String CLEAR_ICON = "\u2715"; // 变量定义 常量 “✕”图标
    private boolean showIcon = false; // 变量定义 布尔变量 确定是否显示 “✕”图标。
    private boolean hoverOverIcon = false; // 添加一个新的类变量来检测鼠标是否悬停在“✕”上。

    public ClearableTextField() { // 类的构造函数 - 设置了文本框的所有自定义功能。当创建一个ClearableTextField对象时，此函数被调用。
        // 添加焦点监听器 - 当文本框获得或失去焦点时调用，导致组件重绘。
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        // 设置文档过滤器 - 用于监听文本更改。当文本插入、删除或替换时，它都会检查文本是否为空，然后确定是否显示“✕”图标。
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

        // 在ClearableTextField构造函数中，添加一个鼠标监听器：当用户点击文本字段时，它会检查点击位置是否在“✕”图标上，并相应地清除文本和图标。
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point mousePos = e.getPoint();
                // 找到 叉 的位置 
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
        });

        // 在ClearableTextField构造函数中，添加一个鼠标动作监听器
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // 当showIcon为真时，并且鼠标在文本字段空白的范围内，“✕”变为红色。
                Point mousePos = e.getPoint();
                if (showIcon && (getBounds().contains(mousePos))) {
                    hoverOverIcon = true;
                } else {
                    hoverOverIcon = false;
                }
                /* // 只在 叉 上悬停，“✕”变为红色。
                FontMetrics metrics = getFontMetrics(getFont());
                int x = getWidth() - metrics.stringWidth(CLEAR_ICON) - 5;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                Rectangle r = new Rectangle(x, y - metrics.getAscent(), metrics.stringWidth(CLEAR_ICON),
                        metrics.getHeight());

                if (r.contains(mousePos)) {
                    hoverOverIcon = true;
                } else {
                    hoverOverIcon = false;
                } */
                repaint(); // 每次鼠标移动，都需要重新绘制组件。
            }
        });
    }

    // 每次文本更改或组件重绘时，都会调用paintComponent方法。在此方法中，我们根据showIcon的值决定是否绘制“✕”图标。
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if (showIcon && this.hasFocus()) { //又要有文字，又要获得焦点（鼠标），才显示“✕”图标。
        if (showIcon) { // 只需检查showIcon，不需要检查hasFocus() //只要有文字就显示“✕”图标，无论是否获得焦点
            FontMetrics metrics = g.getFontMetrics(getFont());
            int x = getWidth() - metrics.stringWidth(CLEAR_ICON) - 5;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            // g.setColor(Color.GRAY);
            if (hoverOverIcon) {
                g.setColor(Color.RED); // 当鼠标悬停时，设置为红色
            } else {
                g.setColor(Color.GRAY); // 否则，设置为灰色
            }
            g.drawString(CLEAR_ICON, x, y);

            Rectangle r = new Rectangle(x, y - metrics.getAscent(), metrics.stringWidth(CLEAR_ICON),
                    metrics.getHeight());
            /*
             * // 解除备注，就可以实现：悬停即可删除文字的办法
             * Point mousePos = getMousePosition();
             * if (mousePos != null && r.contains(mousePos)) {
             * this.setText("");
             * showIcon = false;
             * repaint();
             * }
             */
        }
    }
}
