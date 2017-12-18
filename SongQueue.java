package ataDateM_cisuM;
import java.util.ArrayList;

public class SongQueue{
	private String folderLocation;
	private	ArrayList<Song> queue;
	
	public SongQueue(String f) {
		folderLocation = f;
		queue = new ArrayList<Song>();
	}
	public ArrayList getQueue() {
		return queue;
	}
	public String getFileLocation() {
		return folderLocation;
	}
	public void setFileLocations() {
		java.io.File folder = new java.io.File(folderLocation);
		java.io.File[] listOfFiles = folder.listFiles();
		for(int i = 0; i<listOfFiles.length; i++) {
			queue.add(new Song(listOfFiles[i].toString()));
		}	
	}
	public void setMetaData() throws Exception {
		for(int i = 0; i<queue.size(); i++) {
			Metadata m = new Metadata(queue.get(i).getFileLocation()); 
			queue.get(i).setTitle(m.getTitle());
			queue.get(i).setAlbumArtist(m.getArtist());
			queue.get(i).setAlbum(m.getAlbum());
			queue.get(i).setYear(m.getYear());
			queue.get(i).setTrack(String.valueOf(m.getTrack()));
		}
	}
	public void writeMetaData() throws Exception {
		for(int i = 0; i<queue.size(); i++) {
			Metadata m = new Metadata(queue.get(i).getFileLocation());
			m.setTitle(queue.get(i).getTitle());
			m.setArtist(queue.get(i).getAlbumArtist());
			m.setAlbum(queue.get(i).getAlbum());
			m.setYear(queue.get(i).getYear());
			m.setTrack(Integer.valueOf(queue.get(i).getTrack()));
			m.write(queue.get(i).getFileLocation());
			
		}
	}
	
}