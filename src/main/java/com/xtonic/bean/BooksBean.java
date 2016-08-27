package com.xtonic.bean;
import java.util.List;

import com.xtonic.xml2javaBeanUtil.CollectionType;

public class BooksBean {
	@CollectionType(className="com.xtonic.bean.BookBean",elementNode="book")
	private List<BookBean> books;
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BookBean> getBooks() {
		return books;
	}

	public void setBooks(List<BookBean> books) {
		this.books = books;
	}

	@Override
	public String toString() {
		return "BooksBean [books=" + books + ", name=" + name + "]";
	}
	
	
	
}
