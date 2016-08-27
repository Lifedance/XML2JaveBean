package com.xtonic.bean;

public class BookBean {
	private String name;
	private double price;
	private String author;
	private String version;
	private boolean isSale;
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getAuthor() {
		return author;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public boolean isSale() {
		return isSale;
	}
	@Override
	public String toString() {
		return "BookBean [name=" + name + ", price=" + price + ", author=" + author + ", version=" + version
				+ ", isSale=" + isSale + "]";
	}
	public void setSale(boolean isSale) {
		this.isSale = isSale;
	}
	
	
}
