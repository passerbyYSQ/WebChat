package net.ysq.webchat.config;

import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.beans.PropertyEditorSupport;

/**
 * String->String的类型转换器
 *
 * @author passerbyYSQ
 * @create 2021-02-22 17:26
 */
public class EscapeStringEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        Object value = getValue();
        return value != null ? value.toString() : "";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String escapedText = null;
        if (!StringUtils.isEmpty(text)) {
            escapedText = HtmlUtils.htmlEscape(text, "UTF-8");
        }
        setValue(escapedText);
    }
}
