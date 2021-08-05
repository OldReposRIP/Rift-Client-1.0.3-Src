package live.rift.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

public class ChatMessage extends TextComponentBase {

    String text;

    public ChatMessage(String text) {
        Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String replacement = "§" + m.group().substring(1);

            m.appendReplacement(sb, replacement);
        }

        m.appendTail(sb);
        this.text = sb.toString();
    }

    public String getUnformattedComponentText() {
        return this.text;
    }

    public ITextComponent createCopy() {
        return new ChatMessage(this.text);
    }
}
