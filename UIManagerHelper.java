// UIManagerHelper.java
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class UIManagerHelper {

    public static void setDefaultUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource)
                UIManager.put(key, f);
        }
    }
}
