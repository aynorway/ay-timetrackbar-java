import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TimeTrackBar {

    private JFrame frame;         // 主窗口
    private JPanel taskPanel;     // 包含所有计时任务的面板

    public TimeTrackBar() {
        frame = new JFrame("老6时间进度条");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 100);  // 默认窗口大小为一个计时任务的高度

        taskPanel = new JPanel(); 
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS)); // 使用BoxLayout，使任务垂直堆叠
        frame.add(taskPanel, BorderLayout.CENTER);

        // 在程序开始时，添加一个计时任务
        addNewTimerTask(true);

        frame.setVisible(true);
    }

    // 添加新计时任务
    private void addNewTimerTask(boolean isFirst) {
        TimerTaskPanel timerTask = new TimerTaskPanel(isFirst);
        taskPanel.add(timerTask);
        frame.pack();
    }

    // 删除指定的计时任务
    private void removeTimerTask(TimerTaskPanel timerTask) {
        taskPanel.remove(timerTask);
        frame.pack();   // 重新调整窗口大小以适应内容
        frame.revalidate();
        frame.repaint();
    }

    // TimerTaskPanel 是每个计时任务的面板，包括命名字段、进度条和计时控件
    private class TimerTaskPanel extends JPanel {
        private JProgressBar progressBar;      // 显示进度的进度条
        private JTextField nameField, durationField;  // 名称和持续时间输入字段
        private JLabel remainingTimeLabel;     // 显示剩余时间的标签
        private int duration = 60;             // 默认任务持续时间
        private Timer timer;                   // 用于计时的计时器

        public TimerTaskPanel(boolean isFirst) {
            super(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JPanel westPanel = new JPanel();  // 控制按钮和名称字段面板
            JButton controlButton;
            if (isFirst) {
                controlButton = new JButton("+");  // 第一行只有加号按钮
                controlButton.addActionListener(e -> addNewTimerTask(false));
            } else {
                controlButton = new JButton("-");  // 其他行都有减号按钮
                controlButton.addActionListener(e -> removeTimerTask(this));
            }
            westPanel.add(controlButton);

            nameField = new JTextField(8);
            nameField.addKeyListener(new KeyAdapter() {
                // 限制名称字段的长度为8个字符
                public void keyTyped(KeyEvent e) {
                    if (nameField.getText().length() >= 8) {
                        e.consume();
                    }
                }
            });
            westPanel.add(nameField);
            add(westPanel, BorderLayout.WEST);

            progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            add(progressBar, BorderLayout.CENTER);

            JPanel eastPanel = new JPanel();  // 任务控制面板，包括持续时间、剩余时间和开始按钮
            durationField = new JTextField(4);
            durationField.setText(String.valueOf(duration));
            remainingTimeLabel = new JLabel("0/" + duration + " 秒");
            JButton startButton = new JButton("▶");  // 使用三角形字符作为开始按钮
            startButton.setForeground(Color.GREEN);
            startButton.addActionListener(e -> startCountdown());

            eastPanel.add(durationField);
            eastPanel.add(remainingTimeLabel);
            eastPanel.add(startButton);
            add(eastPanel, BorderLayout.EAST);
        }

        // 开始计时
        private void startCountdown() {
            if (timer != null) {
                timer.stop();
            }

            try {
                duration = Integer.parseInt(durationField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "请输入有效的时间数值.");
                return;
            }

            progressBar.setMaximum(duration);
            progressBar.setValue(0);

            timer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                if (currentValue < duration) {
                    progressBar.setValue(currentValue + 1);
                    remainingTimeLabel.setText(currentValue + 1 + "/" + duration + " 秒");
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });
            timer.start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
