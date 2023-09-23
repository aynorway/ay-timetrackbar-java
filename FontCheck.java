import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class FontCheck {

    public static void main(String[] args) {
        String s = "\u25B6";
        Font[] fonts = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getAllFonts();
        System.out.println("Total fonts: \t" + fonts.length);
        int count = 0;
        for (Font font : fonts) {
            if (font.canDisplayUpTo(s) < 0) {
                count++;
                System.out.println(font.getName());
            }
        }
        System.out.println("Compatible fonts: \t" + count);
        System.out.println(System.getProperty("os.name"));
    }
}