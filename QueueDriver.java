package ataDateM_cisuM;

import java.util.ArrayList;

public class QueueDriver {
	public static void main(String[] args) throws Exception {
        SongQueue s = new SongQueue("C:\\Users\\swinginman\\eclipse-workspace\\ataDateM_cisuM\\src\\ataDateM_cisuM\\BROCKHAMPTON-SATURATION-2");
        s.setFileLocations();
        ArrayList<Song> q = s.getQueue();
        s.setMetaData();
        q.get(q.size()-1).cleanup();
        System.out.println(q.get(q.size()-1).getTitle());
        System.out.println(q.get(q.size()-1).getTrack());
        System.out.println(q.get(q.size()-1).getAlbum());
        System.out.println(q.get(q.size()-1).getAlbumArtist());
        System.out.println(q.get(q.size()-1).getYear());
        s.writeMetaData();
        
    }

}
