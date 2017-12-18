/**
 * 
 */
package ataDateM_cisuM;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author swinginman
 *
 */
class SongTest {
	Song song0, song1, song2, song3;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		song0 = new Song();
		song1 = new Song("sweet", "saturation 2","brockhampton","13","2017",0, "C:\\Users\\swinginman\\Desktop\\test\\sweet.mp3");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void setFileNameFromTitleTest() {
		song1.setFileNameFromTitle();
		String filename = song1.getFileName();
		String title = song1.getTitle()+".mp3";
		assertTrue(filename.equals(title));
	}
	void setTitleFromFileNameTest() {
		song1.setTitleFromFileName();
		String filename = song1.getFileName();
		String title = song1.getTitle()+".mp3";
		assertTrue(filename.equals(title));
	}
	@Test
	void titleNotNull() {
		assertNotNull(song1.getTitle());
	}
	@Test
	void albumNotNull() {
		assertNotNull(song1.getAlbum());
	}
	@Test
	void albumArtistNotNull() {
		assertNotNull(song1.getAlbumArtist());
	}
	@Test
	void trackNotNull() {
		assertNotNull(song1.getTrack());
	}
	@Test
	void yearNotNull() {
		assertNotNull(song1.getYear());
	}
	@Test
	void bitRateNotNull() {
		assertNotNull(song1.getBitRate());
	}
	@Test
	void titleEquals() {
		song1.setTitleFromFileName();
		assertEquals(song1.getTitle(), "sweet");
	}
	@Test
	void albumEquals() {
		assertEquals(song1.getAlbum(), "saturation 2");
	}
	@Test
	void albumArtistEquals() {
		assertEquals(song1.getAlbumArtist(), "brockhampton");
	}
	@Test
	void trackEquals() {
		assertEquals(song1.getTrack(), 13);
	}
	@Test
	void yearEquals() {
		assertEquals(song1.getYear(), 2017);
	}
	@Test
	void bitRateEquals() {
		assertEquals(song1.getBitRate(), 0);
	}
	
	
	
	
	
	
	
	

}
