import java.net.URI;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Desktop;
import java.awt.event.ActionEvent;


public class MenuHelper {

    public static void setupCommonMenu(JFrame mainFrame) {
        // 创建 "窗口" 菜单
        JMenuBar menuBar = new JMenuBar();
        JMenu windowMenu = new JMenu("Window");
        menuBar.add(windowMenu);
        // 创建 "置顶" 菜单项
        JCheckBoxMenuItem alwaysOnTopItem = new JCheckBoxMenuItem("Always On Top 置顶");
        alwaysOnTopItem.addActionListener((ActionEvent e) -> {
            boolean isSelected = alwaysOnTopItem.getState();
            mainFrame.setAlwaysOnTop(isSelected);
        });
        windowMenu.add(alwaysOnTopItem);
        // 设置菜单栏
        mainFrame.setJMenuBar(menuBar);
        // }

        // 创建 "帮助" 菜单
        JMenu helpMenu = new JMenu("Help");
        // 创建 "About" 菜单项
        JMenuItem aboutItem = new JMenuItem("About 关于");
        aboutItem.addActionListener((ActionEvent e) -> {
            // 当用户点击 "About" 时，显示一个对话框
            String appInfo = "App Name: TimeTrackBar\n" +
                    "Version: 1.2\n" +
                    "© 2023 AdrianY\n";
            JOptionPane.showMessageDialog(mainFrame,
                    appInfo,
                    "About TimeTrackBar",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        JMenuItem developerItem = new JMenuItem("Developer 开发者");
        developerItem.addActionListener((ActionEvent e) -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    URI uri = new URI("https://github.com/aynorway/timetrackbar");
                    Desktop.getDesktop().browse(uri);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        helpMenu.add(developerItem);

        // 将 "帮助" 菜单添加到主菜单栏
        menuBar.add(helpMenu);

        // 将菜单栏设置到主框架上
        mainFrame.setJMenuBar(menuBar);
    }
}
