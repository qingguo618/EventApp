package com.ptechpeople.beeker.model;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class DataModel {
	
	int id;
	String date_gmt;
	String guid;
	String modificated_gmt;
	String slug;
	String type;
	String link;
	String title;
	String content;
	String excerpt;
	int author;
	int featured_image;
	String comment_status;
	String ping_status;
	boolean sticky;
	String format;
	String thumbnail_links;
	String author_address;
	String author_coordinates;
	ArrayList<String> self_links;
	ArrayList<String> collection_links;
	ArrayList<String> author_links;
	ArrayList<String> replies_links;
	ArrayList<String> version_history_links;
	ArrayList<String> attachment_links;
	ArrayList<String> term_links;
	ArrayList<String> meta_links;
	public Bitmap bitmap = null;
	float miles;
	double distance;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDate_gmt() {
		return date_gmt;
	}
	public void setDate_gmt(String date_gmt) {
		this.date_gmt = date_gmt;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getModificated_gmt() {
		return modificated_gmt;
	}
	public void setModificated_gmt(String modificated_gmt) {
		this.modificated_gmt = modificated_gmt;
	}
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getExcerpt() {
		return excerpt;
	}
	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}
	public int getAuthor() {
		return author;
	}
	public void setAuthor(int author) {
		this.author = author;
	}
	public int getFeatured_image() {
		return featured_image;
	}
	public void setFeatured_image(int featured_image) {
		this.featured_image = featured_image;
	}
	public String getPing_status() {
		return ping_status;
	}
	public void setPing_status(String ping_status) {
		this.ping_status = ping_status;
	}
	public boolean isSticky() {
		return sticky;
	}
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public ArrayList<String> getSelf_links() {
		return self_links;
	}
	public void setSelf_links(ArrayList<String> self_links) {
		this.self_links = self_links;
	}
	public ArrayList<String> getCollection_links() {
		return collection_links;
	}
	public void setCollection_links(ArrayList<String> collection_links) {
		this.collection_links = collection_links;
	}
	public ArrayList<String> getAuthor_links() {
		return author_links;
	}
	public void setAuthor_links(ArrayList<String> author_links) {
		this.author_links = author_links;
	}
	public ArrayList<String> getReplies_links() {
		return replies_links;
	}
	public void setReplies_links(ArrayList<String> replies_links) {
		this.replies_links = replies_links;
	}
	public ArrayList<String> getVersion_history_links() {
		return version_history_links;
	}
	public void setVersion_history_links(ArrayList<String> version_history_links) {
		this.version_history_links = version_history_links;
	}
	public ArrayList<String> getAttachment_links() {
		return attachment_links;
	}
	public void setAttachment_links(ArrayList<String> attachment_links) {
		this.attachment_links = attachment_links;
	}
	public ArrayList<String> getTerm_links() {
		return term_links;
	}
	public void setTerm_links(ArrayList<String> term_links) {
		this.term_links = term_links;
	}
	public ArrayList<String> getMeta_links() {
		return meta_links;
	}
	public void setMeta_links(ArrayList<String> meta_links) {
		this.meta_links = meta_links;
	}
	public String getComment_status() {
		return comment_status;
	}
	public void setComment_status(String comment_status) {
		this.comment_status = comment_status;
	}
	public String getThumbnail_links() {
		return thumbnail_links;
	}
	public void setThumbnail_links(String thumbnail_links) {
		this.thumbnail_links = thumbnail_links;
	}
	public float getMiles() {
		return miles;
	}
	public void setMiles(float miles) {
		this.miles = miles;
	}
	public double getDistance(){ return distance; }
	public void setDistance(double distance){this.distance = distance; }
}
