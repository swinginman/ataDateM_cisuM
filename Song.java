package ataDateM_cisuM;
import java.lang.Object;
import java.lang.StringBuilder;

public class Song {
	private String title;
	private String album;
	private String albumArtist;
	private String track;
	private String year;
	private int bitRate;
	private String fileLocation;
	
	public Song(){
		title = "";
		album = "";
		albumArtist = "";
		track = "";
		year = "";
		bitRate = 0;
		fileLocation = null;
	}
	public Song(String f){
		title = "";
		album = "";
		albumArtist = "";
		track = "";
		year = "";
		bitRate = 0;
		fileLocation = f;
	}
	public Song(String t,String a,String aa,String tr, String y,int b, String f) {
		title = t;
		album = a;
		albumArtist = aa;
		track = tr;
		year = y;
		bitRate = b;
		fileLocation = f;
	}
	public String getFileName() {
		java.io.File file = new java.io.File(fileLocation);
		return file.getName();
	}
	public void setTitleFromFileName() {
		java.io.File file = new java.io.File(fileLocation);
		title = file.getName().substring(0, file.getName().lastIndexOf("."));
	}
	public void setFileNameFromTitle() {
        java.io.File old = new java.io.File(fileLocation);
        java.io.File newfile = new java.io.File("C:\\Users\\swinginman\\Desktop\\test\\" + title + ".mp3");
        old.renameTo(newfile);
        fileLocation = "C:\\Users\\swinginman\\Desktop\\test\\" + title + ".mp3";
        /*System.out.println(this.getFileName());
        System.out.println(this.getTitle()+".mp3");
        System.out.println(fileLocation);*/
        
	}
	public String getFileLocation() {
		return fileLocation;
	}
	public String getTitle() {
		return title;
	}
	public String getAlbum() {
		return album;
	}
	public String getAlbumArtist() {
		return albumArtist;
	}
	public String getTrack() {
		return track;
	}
	public String getYear() {
		return year;
	}
	public int getBitRate() {
		return bitRate;
	}
	
	public void setFileLocation(String f) {
		fileLocation = f;
	}
	public void setTitle(String f) {
		title = f;
	}
	public void setAlbum(String f) {
		album = f;
	}
	public void setAlbumArtist(String f) {
		albumArtist = f;
	}
	public void setTrack(String f) {
		track = f;
	}
	public void setYear(String f) {
		year = f;
	}
	public void setBitRate(int f) {
		bitRate = f;
	}
	public void cleanup() {
		StringBuilder t  = new StringBuilder(this.getTitle());
		StringBuilder a  = new StringBuilder(this.getAlbum());
		t.deleteCharAt(0);
		t.deleteCharAt(0);
		a.deleteCharAt(0);
		a.deleteCharAt(0);
		for(int i = 1; i< t.length();i++) {
				t.deleteCharAt(i);
		}
		for(int i = 1; i<a.length(); i++) {
			a.deleteCharAt(i);
		}
		this.setTitle(t.toString());
		this.setAlbum(a.toString());
	}
}
