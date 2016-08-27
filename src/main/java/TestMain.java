import java.io.InputStream;

import com.xtonic.bean.BooksBean;
import com.xtonic.xml2javaBeanUtil.XML2JaveBean;

public class TestMain {
	public static void main(String[] args) throws Exception {
		//String path = Thread.currentThread().getContextClassLoader().getResource("test.xml").getPath();
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		BooksBean bean = XML2JaveBean.XmlToJavaBean(stream,BooksBean.class); 
		System.out.println(bean.toString());
	}
}
