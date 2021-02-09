package net.ysq.webchat.component;

import net.dreamlu.mica.xss.core.XssCleaner;
import org.springframework.web.util.HtmlUtils;

public class MyXssCleaner implements XssCleaner {

	@Override
	public String clean(String html) {
		return HtmlUtils.htmlEscape(html, "UTF-8");
	}

}
